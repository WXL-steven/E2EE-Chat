<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>设置PIN - E2EE Chat</title>
    <link href="${pageContext.request.contextPath}/assets/css/dist/styles.css" rel="stylesheet">
</head>
<body class="bg-gray-100">
    <div class="content-panel-wrapper">
        <div class="content-panel">
            <div class="flex justify-start">
                <h1 class="text-2xl font-bold mb-8">初始化保险库</h1>
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

            <h2 id="pin-subtitle" class="text-center text-lg font-semibold text-gray-700">请设置保险库PIN</h2>
        </div>
    </div>

    <script>
        let firstPin = '';
        let isConfirming = false;

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

        // 清空PIN输入
        function clearPins() {
            document.querySelectorAll('.input-pin').forEach(input => {
                input.value = '';
            });
            document.querySelector('.input-pin').focus();
        }

        // 获取当前PIN
        function getCurrentPin() {
            return Array.from(document.querySelectorAll('.input-pin'))
                       .map(input => input.value)
                       .join('');
        }

        // 处理PIN设置
        function handlePinSetup() {
            const currentPin = getCurrentPin();
            
            if (!isConfirming) {
                firstPin = currentPin;
                isConfirming = true;
                clearPins();
                document.getElementById('pin-subtitle').textContent = '请再次输入PIN';
            } else {
                if (currentPin === firstPin) {
                    initializeVault(currentPin).catch(error => {
                        console.error('初始化保险库失败:', error);
                        showMessage('初始化保险库失败: ' + error.message, 'error');
                    });
                } else {
                    showMessage('两次输入的PIN不一致，请重新设置', 'error');
                    document.getElementById('pin-subtitle').textContent = '请设置保险库PIN';
                    isConfirming = false;
                    firstPin = '';
                    clearPins();
                }
            }
        }

        // Base64 编解码工具函数
        function base64ToBytes(base64) {
            const binString = atob(base64);
            return Uint8Array.from(binString, (m) => m.charCodeAt(0));
        }

        function bytesToBase64(bytes) {
            const binString = Array.from(bytes, (x) => String.fromCharCode(x)).join("");
            return btoa(binString);
        }

        // 从字符串生成密钥
        async function generateKey(password, salt) {
            const encoder = new TextEncoder();
            const passwordData = encoder.encode(password);
            
            // 导入密钥材料
            const keyMaterial = await crypto.subtle.importKey(
                "raw",
                passwordData,
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

        // 初始化保险库
        async function initializeVault(pin) {
            // 锁定输入并更新状态
            document.querySelectorAll('.input-pin').forEach(input => input.disabled = true);
            document.getElementById('pin-subtitle').textContent = '正在配置保险库';
            
            try {
                // 获取保险库信息
                const response = await fetch('${pageContext.request.contextPath}/vault/info');
                const vaultInfo = await response.json();
                
                if (!vaultInfo.vaultMasterKey) {
                    throw new Error('无法获取保险库主密钥');
                }

                // 生成加密所需的随机数
                const salt = crypto.getRandomValues(new Uint8Array(16));
                const iv = crypto.getRandomValues(new Uint8Array(12));
                
                // 生成密钥对
                const keyPair = await crypto.subtle.generateKey(
                    {
                        name: "ECDSA",
                        namedCurve: "P-256"
                    },
                    true,
                    ["sign", "verify"]
                );
                
                // 导出公钥
                const publicKeyRaw = await crypto.subtle.exportKey(
                    "raw",
                    keyPair.publicKey
                );
                
                // 导出私钥
                const privateKeyRaw = await crypto.subtle.exportKey(
                    "pkcs8",
                    keyPair.privateKey
                );
                
                // 从PIN和主密钥派生加密密钥
                const masterKeyBytes = base64ToBytes(vaultInfo.vaultMasterKey);
                const combinedSecret = new Uint8Array(pin.length + masterKeyBytes.length);
                combinedSecret.set(new TextEncoder().encode(pin));
                combinedSecret.set(masterKeyBytes, pin.length);
                
                const encryptionKey = await generateKey(
                    bytesToBase64(combinedSecret),
                    salt
                );
                
                // 加密私钥
                const encryptedPrivateKey = await crypto.subtle.encrypt(
                    {
                        name: "AES-GCM",
                        iv: iv
                    },
                    encryptionKey,
                    privateKeyRaw
                );
                
                // 准备请求数据
                const requestData = {
                    vaultSalt: bytesToBase64(salt),
                    vaultIv: bytesToBase64(iv),
                    encryptedPrivateKey: bytesToBase64(new Uint8Array(encryptedPrivateKey)),
                    publicKey: bytesToBase64(new Uint8Array(publicKeyRaw))
                };
                
                // 发送创建请求
                const createResponse = await fetch('${pageContext.request.contextPath}/vault/setup', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(requestData)
                });
                
                const result = await createResponse.json();
                
                if (result.success) {
                    showMessage('保险库初始化成功', 'success');
                    document.getElementById('pin-subtitle').textContent = '初始化成功';
                    // 保存私钥到sessionStorage
                    sessionStorage.setItem('privateKey', bytesToBase64(new Uint8Array(privateKeyRaw)));
                    // 5秒后跳转
                    setTimeout(() => {
                        window.location.href = '${pageContext.request.contextPath}/sessions';
                    }, 5000);
                } else {
                    throw new Error(result.message || '创建保险库失败');
                }
                
            } catch (error) {
                // 解锁输入框
                document.querySelectorAll('.input-pin').forEach(input => input.disabled = false);
                document.getElementById('pin-subtitle').textContent = '请设置保险库PIN';
                throw error;
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
                        handlePinSetup();
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
