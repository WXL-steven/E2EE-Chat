/**
 * 表单验证和交互功能
 */

// 验证结果类
class ValidationResult {
    constructor(isValid, error = '') {
        this.isValid = isValid;
        this.error = error;
    }

    static success() {
        return new ValidationResult(true);
    }

    static error(message) {
        return new ValidationResult(false, message);
    }
}

// 验证规则基类
class ValidationRule {
    async validate(value) {
        throw new Error('ValidationRule.validate() must be implemented');
    }
}

// 文本验证规则
class TextValidationRules {
    static minLength(length) {
        return new class extends ValidationRule {
            async validate(value) {
                return value.length >= length ?
                    ValidationResult.success() :
                    ValidationResult.error(`最少需要${length}个字符`);
            }
        };
    }

    static maxLength(length) {
        return new class extends ValidationRule {
            async validate(value) {
                return value.length <= length ?
                    ValidationResult.success() :
                    ValidationResult.error(`最多允许${length}个字符`);
            }
        };
    }

    static pattern(regex, message) {
        return new class extends ValidationRule {
            async validate(value) {
                return regex.test(value) ?
                    ValidationResult.success() :
                    ValidationResult.error(message);
            }
        };
    }

    static custom(validator, message) {
        return new class extends ValidationRule {
            async validate(value) {
                try {
                    const result = await validator(value);
                    // 如果验证器返回了 ValidationResult 对象，直接使用它
                    if (result instanceof ValidationResult) {
                        return result;
                    }
                    // 否则，根据布尔值创建结果
                    return result ? 
                        ValidationResult.success() : 
                        ValidationResult.error(message);
                } catch (error) {
                    console.error('验证器执行失败:', error);
                    return ValidationResult.error(error.message || '验证失败');
                }
            }
        };
    }
}

// 验证器基类
class Validator {
    constructor() {
        this.rules = [];
    }

    addRule(rule) {
        this.rules.push(rule);
        return this;
    }

    async validate(value) {
        for (const rule of this.rules) {
            const result = await rule.validate(value);
            if (!result.isValid) {
                return result;
            }
        }
        return ValidationResult.success();
    }
}

// 用户名验证器
class UsernameValidator extends Validator {
    constructor() {
        super();
        this.addRule(TextValidationRules.minLength(3))
            .addRule(TextValidationRules.maxLength(20))
            .addRule(
                TextValidationRules.pattern(
                    /^[a-zA-Z0-9_-]+$/,
                    '用户名只能包含字母、数字、下划线和短横线'
                )
            );
    }
}

// 昵称验证器
class DisplayNameValidator extends Validator {
    constructor() {
        super();
        this.addRule(TextValidationRules.minLength(1))
            .addRule(TextValidationRules.maxLength(20));
    }
}

// 密码验证器
class PasswordValidator extends Validator {
    constructor() {
        super();
        this.addRule(TextValidationRules.minLength(6))
            .addRule(TextValidationRules.maxLength(20))
            .addRule(
                TextValidationRules.pattern(
                    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/,
                    '密码必须包含大小写字母和数字'
                )
            );
    }
}

// 表单验证类
class FormValidation {
    updateUI(input, result) {
        const errorElement = input.parentElement.querySelector('.input-error-message');
        if (!errorElement) return;

        if (result.isValid) {
            input.classList.remove('input-error');
            errorElement.textContent = '';
        } else {
            input.classList.add('input-error');
            errorElement.textContent = result.error;
        }
    }

    async setupValidation(input, validators) {
        // 移除旧的事件监听器
        const oldListener = input._validationListener;
        if (oldListener) {
            input.removeEventListener('blur', oldListener);
        }

        // 创建新的验证函数
        const validateInput = async () => {
            // 如果已经验证通过，不再重复验证
            if (input.classList.contains('input-valid')) {
                return true;
            }

            // 显示加载状态
            input.classList.remove('input-error', 'input-valid');
            input.classList.add('input-validating');
            
            // 清除错误消息
            const errorElement = input.parentElement.querySelector('.input-error-message');
            if (errorElement) {
                errorElement.textContent = '';
            }

            try {
                for (const validator of validators) {
                    console.log('正在执行验证器:', validator.constructor.name);
                    const result = await validator.validate(input.value);
                    if (!result.isValid) {
                        console.log('验证器失败:', {
                            validator: validator.constructor.name,
                            rules: validator.rules.map(rule => ({
                                ruleName: rule.constructor.name,
                                error: result.error
                            }))
                        });
                        input.classList.remove('input-validating');
                        this.updateUI(input, result);
                        return false;
                    }
                }
                
                console.log('所有验证器通过');
                input.classList.remove('input-validating');
                input.classList.add('input-valid');
                this.updateUI(input, ValidationResult.success());
                return true;
            } catch (error) {
                console.error('验证过程中发生错误:', error);
                input.classList.remove('input-validating');
                this.updateUI(input, ValidationResult.error('验证过程中发生错误'));
                return false;
            }
        };

        // 保存验证函数引用以便后续使用
        input._validateInput = validateInput;

        // 添加事件监听器
        input.addEventListener('blur', validateInput);
    }

    async validate(input) {
        if (typeof input._validateInput !== 'function') {
            console.error('输入未设置验证器:', input);
            return false;
        }
        return await input._validateInput();
    }

    async validateAll(inputs) {
        for (const input of inputs) {
            const isValid = await this.validate(input);
            if (!isValid) {
                return false;
            }
        }
        return true;
    }

    setupUsernameValidation(input) {
        this.setupValidation(input, [new UsernameValidator()]);
    }

    setupDisplayNameValidation(input) {
        this.setupValidation(input, [new DisplayNameValidator()]);
    }

    setupPasswordValidation(input) {
        this.setupValidation(input, [new PasswordValidator()]);
    }

    setupCustomValidation(input, validators) {
        this.setupValidation(input, validators);
    }
}

// PIN输入框类
class PinInput {
    static validate(element) {
        element.value = element.value.replace(/[^0-9]/g, '').slice(0, 1);
        if (element.value) {
            const next = element.nextElementSibling;
            if (next?.classList.contains('input-pin')) {
                next.focus();
            }
        }
    }

    static handleBackspace(event) {
        const element = event.target;
        if (event.key === 'Backspace' && !element.value) {
            const prev = element.previousElementSibling;
            if (prev?.classList.contains('input-pin')) {
                prev.focus();
            }
        }
    }

    static setup(input) {
        input.addEventListener('input', () => PinInput.validate(input));
        input.addEventListener('keydown', PinInput.handleBackspace);
    }
}

// 下拉菜单类
class Dropdown {
    static toggle(element) {
        const menu = element.nextElementSibling;
        menu.classList.toggle('show');
    }

    static handleOutsideClick(event) {
        if (!event.target.closest('.dropdown')) {
            document.querySelectorAll('.dropdown-menu.show')
                .forEach(menu => menu.classList.remove('show'));
        }
    }

    static setup() {
        document.addEventListener('click', Dropdown.handleOutsideClick);
    }
}
