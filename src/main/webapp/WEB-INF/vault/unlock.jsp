<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>解密保险库 - E2EE Chat</title>
    <link href="${pageContext.request.contextPath}/assets/css/dist/styles.css" rel="stylesheet">
</head>
<body class="bg-gray-100">
    <div class="content-panel-wrapper">
        <div class="content-panel">
            <div class="flex justify-start">
                <h1 class="text-2xl font-bold mb-8">解密保险库</h1>
            </div>

            <!-- 消息提示框 -->
            <div id="message-container" class="w-full mb-8 max-w-md hidden">
                <div id="message-content" class="px-4 py-3 rounded relative" role="alert">
                    <span class="block sm:inline"></span>
                </div>
            </div>

            <!-- PIN输入框 -->
            <div class="flex justify-center gap-2 mb-8">
                <input type="text" class="input-pin" maxlength="1" placeholder="#">
                <input type="text" class="input-pin" maxlength="1" placeholder="#">
                <input type="text" class="input-pin" maxlength="1" placeholder="#">
                <input type="text" class="input-pin" maxlength="1" placeholder="#">
            </div>

            <h2 id="pin-subtitle" class="text-center text-lg font-semibold text-gray-700">请输入保险库PIN</h2>
        </div>
    </div>

    <script>
        // Base64 编解码工具函数
        function base64ToBytes(base64) {
            const binString = atob(base64);
            return Uint8Array.from(binString, (m) => m.charCodeAt(0));
        }

        function bytesToBase64(bytes) {
            const binString = Array.from(bytes, (x) => String.fromCharCode(x)).join("");
            return btoa(binString);
        }

        // 从PIN和主密钥派生加密密钥
        async function generateKey(secret, salt) {
            const encoder = new TextEncoder();
            const secretData = encoder.encode(secret);
            
            // 导入密钥材料
            const keyMaterial = await crypto.subtle.importKey(
                "raw",
                secretData,
                "PBKDF2",
                false,
                ["deriveBits", "deriveKey"]
            );
            
            // 使用PBKDF2派生HKDF的输入密钥
            const pbkdf2Key = await crypto.subtle.deriveBits(
                {
                    name: "PBKDF2",
                    salt: salt,
                    iterations: 100000,
                    hash: "SHA-256"
                },
                keyMaterial,
                256
            );
            
            // 使用HKDF派生最终的AES密钥
            const hkdfKey = await crypto.subtle.importKey(
                "raw",
                pbkdf2Key,
                {
                    name: "HKDF",
                },
                false,
                ["deriveBits"]
            );
            
            const finalKey = await crypto.subtle.deriveBits(
                {
                    name: "HKDF",
                    hash: "SHA-256",
                    salt: salt,
                    info: encoder.encode("AES-GCM")
                },
                hkdfKey,
                256
            );
            
            return await crypto.subtle.importKey(
                "raw",
                finalKey,
                { name: "AES-GCM" },
                false,
                ["encrypt", "decrypt"]
            );
        }

        // 显示消息
        function showMessage(message, type = 'error') {
            const container = document.getElementById('message-container');
            const content = container.querySelector('#message-content');
            
            container.classList.remove('hidden');
            content.className = 'px-4 py-3 rounded relative alert-' + type;
            content.querySelector('span').textContent = message;
            
            setTimeout(() => {
                container.classList.add('hidden');
            }, 5000);
        }

        // 获取当前PIN
        function getCurrentPin() {
            return Array.from(document.querySelectorAll('.input-pin'))
                       .map(input => input.value)
                       .join('');
        }

        // 处理PIN验证
        async function handlePinUnlock() {
            const pin = getCurrentPin();
            
            // 锁定输入框
            const inputs = document.querySelectorAll('.input-pin');
            inputs.forEach(input => input.disabled = true);
            
            // 更新副标题
            document.getElementById('pin-subtitle').textContent = '正在解密...';
            
            try {
                // 获取保险库信息
                const response = await fetch('${pageContext.request.contextPath}/vault/info');
                const vaultInfo = await response.json();
                
                if (!vaultInfo.ready) {
                    throw new Error('保险库未初始化');
                }

                // 从PIN和主密钥派生加密密钥
                const masterKeyBytes = base64ToBytes(vaultInfo.vaultMasterKey);
                const combinedSecret = new Uint8Array(pin.length + masterKeyBytes.length);
                combinedSecret.set(new TextEncoder().encode(pin));
                combinedSecret.set(masterKeyBytes, pin.length);
                
                const salt = base64ToBytes(vaultInfo.vaultSalt);
                const iv = base64ToBytes(vaultInfo.vaultIv);
                const encryptedPrivateKey = base64ToBytes(vaultInfo.encryptedPrivateKey);
                
                const decryptionKey = await generateKey(
                    bytesToBase64(combinedSecret),
                    salt
                );
                
                try {
                    // 尝试解密私钥
                    const privateKeyRaw = await crypto.subtle.decrypt(
                        {
                            name: "AES-GCM",
                            iv: iv
                        },
                        decryptionKey,
                        encryptedPrivateKey
                    );
                    
                    // 验证成功
                    showMessage('保险库解密成功', 'success');
                    document.getElementById('pin-subtitle').textContent = '解密成功';
                    
                    // 保存私钥到sessionStorage
                    sessionStorage.setItem('privateKey', bytesToBase64(new Uint8Array(privateKeyRaw)));
                    
                    // 跳转
                    setTimeout(() => {
                        window.location.href = '${pageContext.request.contextPath}/sessions';
                    }, 3000);
                    
                } catch (e) {
                    // PIN错误导致解密失败
                    throw new Error('PIN不正确');
                }
                
            } catch (error) {
                console.error('解密失败:', error);
                showMessage(error.message, 'error');
                document.getElementById('pin-subtitle').textContent = '请输入保险库PIN';
                // 解锁输入框
                inputs.forEach(input => {
                    input.disabled = false;
                    input.value = '';
                });
                inputs[0].focus();
            }
        }

        // 设置PIN输入框事件
        document.addEventListener('DOMContentLoaded', () => {
            // 检查sessionStorage中是否有已解密的私钥
            if (sessionStorage.getItem('privateKey')) {
                window.location.href = '${pageContext.request.contextPath}/sessions';
            }

            const inputs = document.querySelectorAll('.input-pin');
            
            inputs.forEach((input, index) => {
                // 只允许输入数字
                input.addEventListener('input', (e) => {
                    const value = e.target.value;
                    if (!/^\d*$/.test(value)) {
                        input.value = '';
                        return;
                    }
                    
                    if (value && index < inputs.length - 1) {
                        inputs[index + 1].focus();
                    }
                    
                    // 检查是否所有输入框都已填写
                    if (getCurrentPin().length === 4) {
                        handlePinUnlock();
                    }
                });

                // 处理退格键
                input.addEventListener('keydown', (e) => {
                    if (e.key === 'Backspace' && !input.value && index > 0) {
                        inputs[index - 1].focus();
                    }
                });
            });
        });
    </script>
</body>
</html>
