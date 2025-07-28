package util;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * 设置验证器工厂 (Strategy + Factory Pattern)
 * 提供不同类型的设置验证策略
 */
public class SettingValidatorFactory {
    
    /**
     * 获取目录验证器
     * @return 目录验证器
     */
    public static SettingValidator getDirectoryValidator() {
        return new DirectoryValidator();
    }
    
    /**
     * 获取命令前缀验证器
     * @return 命令前缀验证器
     */
    public static SettingValidator getCommandPrefixValidator() {
        return new CommandPrefixValidator();
    }
    
    /**
     * 获取配置文件验证器
     * @return 配置文件验证器
     */
    public static SettingValidator getConfigFileValidator() {
        return new ConfigFileValidator();
    }
    
    /**
     * 获取组合验证器
     * @param validators 验证器列表
     * @return 组合验证器
     */
    public static SettingValidator getCompositeValidator(SettingValidator... validators) {
        return new CompositeValidator(validators);
    }
    
    /**
     * 设置验证器接口 (Strategy Pattern)
     */
    public interface SettingValidator {
        ValidationResult validate(String input);
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        private final ValidationLevel level;
        private final boolean canAutoFix;
        private final String suggestion;
        
        public ValidationResult(boolean valid, String message) {
            this(valid, message, ValidationLevel.ERROR, false, null);
        }
        
        public ValidationResult(boolean valid, String message, ValidationLevel level) {
            this(valid, message, level, false, null);
        }
        
        public ValidationResult(boolean valid, String message, ValidationLevel level, 
                              boolean canAutoFix, String suggestion) {
            this.valid = valid;
            this.message = message;
            this.level = level;
            this.canAutoFix = canAutoFix;
            this.suggestion = suggestion;
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public ValidationLevel getLevel() { return level; }
        public boolean canAutoFix() { return canAutoFix; }
        public String getSuggestion() { return suggestion; }
        
        @Override
        public String toString() {
            return String.format("ValidationResult{valid=%s, level=%s, message='%s'}", 
                               valid, level, message);
        }
    }
    
    /**
     * 验证级别枚举
     */
    public enum ValidationLevel {
        ERROR,
        WARNING,
        INFO,
        SUCCESS
    }
    
    /**
     * 目录验证器实现
     */
    private static class DirectoryValidator implements SettingValidator {
        @Override
        public ValidationResult validate(String input) {
            if (input == null || input.trim().isEmpty()) {
                return new ValidationResult(false, "目录路径不能为空", 
                    ValidationLevel.ERROR, false, "请选择一个有效的目录路径");
            }
            
            String path = input.trim();
            File directory = new File(path);
            
            if (!directory.exists()) {
                return new ValidationResult(false, "目录不存在", 
                    ValidationLevel.WARNING, true, "是否创建此目录？");
            }
            
            if (!directory.isDirectory()) {
                return new ValidationResult(false, "指定路径不是有效目录", 
                    ValidationLevel.ERROR, false, "请选择一个目录而不是文件");
            }
            
            if (!directory.canRead()) {
                return new ValidationResult(false, "目录不可读", 
                    ValidationLevel.WARNING, false, "请检查目录权限");
            }
            
            if (!directory.canWrite()) {
                return new ValidationResult(true, "目录有效但不可写", 
                    ValidationLevel.WARNING, false, "某些功能可能受限");
            }
            
            return new ValidationResult(true, "目录有效", ValidationLevel.SUCCESS);
        }
    }
    
    /**
     * 命令前缀验证器实现
     */
    private static class CommandPrefixValidator implements SettingValidator {
        @Override
        public ValidationResult validate(String input) {
            if (input == null || input.trim().isEmpty()) {
                return new ValidationResult(true, "将使用系统默认前缀", 
                    ValidationLevel.INFO, true, "推荐使用系统默认设置");
            }
            
            String prefix = input.trim();
            
            // 检查常见的命令前缀模式
            if (prefix.toLowerCase().contains("cmd") && prefix.toLowerCase().contains("/c")) {
                return new ValidationResult(true, "Windows命令前缀格式正确", ValidationLevel.SUCCESS);
            }
            
            if (prefix.toLowerCase().contains("bash") && prefix.toLowerCase().contains("-c")) {
                return new ValidationResult(true, "Linux/Unix命令前缀格式正确", ValidationLevel.SUCCESS);
            }
            
            if (prefix.toLowerCase().contains("powershell")) {
                return new ValidationResult(true, "PowerShell命令前缀格式正确", ValidationLevel.SUCCESS);
            }
            
            // 检查是否包含危险字符
            String[] dangerousChars = {";", "&", "|", ">", "<", "*", "?"};
            for (String dangerous : dangerousChars) {
                if (prefix.contains(dangerous)) {
                    return new ValidationResult(false, "命令前缀包含潜在危险字符: " + dangerous, 
                        ValidationLevel.WARNING, false, "请避免使用特殊字符");
                }
            }
            
            return new ValidationResult(true, "自定义命令前缀", ValidationLevel.INFO, false, 
                "请确保前缀格式正确");
        }
    }
    
    /**
     * 配置文件验证器实现
     */
    private static class ConfigFileValidator implements SettingValidator {
        @Override
        public ValidationResult validate(String input) {
            if (input == null || input.trim().isEmpty()) {
                return new ValidationResult(false, "文件路径不能为空", ValidationLevel.ERROR);
            }
            
            String filePath = input.trim();
            File file = new File(filePath);
            
            if (!file.exists()) {
                return new ValidationResult(false, "文件不存在", ValidationLevel.ERROR);
            }
            
            if (!file.isFile()) {
                return new ValidationResult(false, "指定路径不是文件", ValidationLevel.ERROR);
            }
            
            if (!file.canRead()) {
                return new ValidationResult(false, "文件不可读", ValidationLevel.ERROR);
            }
            
            // 检查文件扩展名
            if (!filePath.toLowerCase().endsWith(".json")) {
                return new ValidationResult(false, "文件类型不正确，需要JSON文件", 
                    ValidationLevel.WARNING, true, "自动添加.json扩展名？");
            }
            
            // 检查文件大小
            long fileSize = file.length();
            if (fileSize == 0) {
                return new ValidationResult(false, "文件为空", ValidationLevel.ERROR);
            }
            
            if (fileSize > 10 * 1024 * 1024) { // 10MB
                return new ValidationResult(false, "文件过大（>10MB）", 
                    ValidationLevel.WARNING, false, "配置文件可能过大");
            }
            
            return new ValidationResult(true, "配置文件有效", ValidationLevel.SUCCESS);
        }
    }
    
    /**
     * 组合验证器实现 (Composite Pattern)
     */
    private static class CompositeValidator implements SettingValidator {
        private final List<SettingValidator> validators;
        
        public CompositeValidator(SettingValidator... validators) {
            this.validators = new ArrayList<>();
            for (SettingValidator validator : validators) {
                this.validators.add(validator);
            }
        }
        
        @Override
        public ValidationResult validate(String input) {
            ValidationResult result = null;
            ValidationLevel highestLevel = ValidationLevel.SUCCESS;
            
            for (SettingValidator validator : validators) {
                ValidationResult current = validator.validate(input);
                
                // 如果当前验证失败，立即返回
                if (!current.isValid() && current.getLevel() == ValidationLevel.ERROR) {
                    return current;
                }
                
                // 保留最严重的验证结果
                if (current.getLevel().ordinal() > highestLevel.ordinal()) {
                    highestLevel = current.getLevel();
                    result = current;
                }
                
                // 如果还没有结果，使用当前结果
                if (result == null) {
                    result = current;
                }
            }
            
            return result != null ? result : new ValidationResult(true, "验证通过", ValidationLevel.SUCCESS);
        }
    }
    
    /**
     * 创建预定义的验证器组合
     */
    public static class PresetValidators {
        
        /**
         * 创建工具目录完整验证器
         * @return 工具目录验证器
         */
        public static SettingValidator createToolDirectoryValidator() {
            return getDirectoryValidator();
        }
        
        /**
         * 创建配置文件完整验证器
         * @return 配置文件验证器
         */
        public static SettingValidator createConfigFileValidator() {
            return getConfigFileValidator();
        }
        
        /**
         * 创建命令前缀完整验证器
         * @return 命令前缀验证器
         */
        public static SettingValidator createCommandPrefixValidator() {
            return getCommandPrefixValidator();
        }
    }
    
    /**
     * 验证器工具方法
     */
    public static class ValidationUtils {
        
        /**
         * 快速验证目录
         * @param directoryPath 目录路径
         * @return 验证结果
         */
        public static ValidationResult validateDirectory(String directoryPath) {
            return getDirectoryValidator().validate(directoryPath);
        }
        
        /**
         * 快速验证命令前缀
         * @param commandPrefix 命令前缀
         * @return 验证结果
         */
        public static ValidationResult validateCommandPrefix(String commandPrefix) {
            return getCommandPrefixValidator().validate(commandPrefix);
        }
        
        /**
         * 快速验证配置文件
         * @param configFilePath 配置文件路径
         * @return 验证结果
         */
        public static ValidationResult validateConfigFile(String configFilePath) {
            return getConfigFileValidator().validate(configFilePath);
        }
        
        /**
         * 批量验证
         * @param validator 验证器
         * @param inputs 输入列表
         * @return 验证结果列表
         */
        public static List<ValidationResult> batchValidate(SettingValidator validator, String... inputs) {
            List<ValidationResult> results = new ArrayList<>();
            for (String input : inputs) {
                results.add(validator.validate(input));
            }
            return results;
        }
        
        /**
         * 检查是否所有验证都通过
         * @param results 验证结果列表
         * @return 是否全部通过
         */
        public static boolean allValid(List<ValidationResult> results) {
            return results.stream().allMatch(ValidationResult::isValid);
        }
        
        /**
         * 获取第一个错误
         * @param results 验证结果列表
         * @return 第一个错误结果，如果没有则返回null
         */
        public static ValidationResult getFirstError(List<ValidationResult> results) {
            return results.stream()
                          .filter(r -> !r.isValid())
                          .findFirst()
                          .orElse(null);
        }
    }
} 