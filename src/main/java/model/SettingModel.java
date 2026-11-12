package model;

import util.I18nManager;
import util.OsUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Locale;

/**
 * 设置数据模型 (Model层)
 * 负责管理插件设置数据和配置文件操作
 */
public class SettingModel {

    // 配置文件目录（用户主目录，避免依赖Burp进程工作目录）
    private static final String USER_SETTINGS_DIR =
            System.getProperty("user.home") + File.separator + ".bparsenal";
    private static final String TOOL_CONFIG_FILE =
            USER_SETTINGS_DIR + File.separator + "tool_settings.properties";

    // 工具设置
    private Properties toolSettings;
    
    // 插件信息
    private String pluginName = "BpArsenal - Burp Suite武器库";
    private String pluginVersion = "v1.0.0";
    private String pluginAuthor = "youmulijiang";
    
    /**
     * 构造函数
     */
    public SettingModel() {
        loadToolSettings();
    }
    
    /**
     * 加载工具设置
     */
    public void loadToolSettings() {
        toolSettings = new Properties();
        File settingsFile = new File(TOOL_CONFIG_FILE);

        if (settingsFile.exists()) {
            try (FileInputStream fis = new FileInputStream(settingsFile)) {
                toolSettings.load(fis);
            } catch (IOException e) {
            }
        }
    }

    /**
     * 保存工具设置
     * @throws IOException 保存失败异常
     */
    public void saveToolSettings() throws IOException {
        File settingsFile = new File(TOOL_CONFIG_FILE);
        File parentDir = settingsFile.getParentFile();
        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("无法创建设置目录: " + USER_SETTINGS_DIR);
        }

        try (FileOutputStream fos = new FileOutputStream(settingsFile)) {
            toolSettings.store(fos, "BpArsenal Tool Settings");
        }
    }
    
    /**
     * 获取工具目录设置
     * @return 工具目录路径
     */
    public String getToolDirectory() {
        return toolSettings.getProperty("tool.directory", "");
    }
    
    /**
     * 设置工具目录
     * @param directory 工具目录路径
     */
    public void setToolDirectory(String directory) {
        if (directory == null || directory.trim().isEmpty()) {
            toolSettings.remove("tool.directory");
        } else {
            toolSettings.setProperty("tool.directory", directory.trim());
        }
    }
    
    /**
     * 获取命令前缀设置
     * @return 命令前缀
     */
    public String getCommandPrefix() {
        return toolSettings.getProperty("command.prefix", "");
    }
    
    /**
     * 设置命令前缀
     * @param prefix 命令前缀
     */
    public void setCommandPrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            toolSettings.remove("command.prefix");
        } else {
            toolSettings.setProperty("command.prefix", prefix.trim());
        }
    }
    
    /**
     * 获取格式化后的命令前缀数组
     * @return 命令前缀数组
     */
    public String[] getCommandPrefixArray() {
        String prefix = getCommandPrefix();
        if (prefix.isEmpty()) {
            return OsUtils.getDefaultCommandPrefix();
        } else {
            return prefix.split("\\s+");
        }
    }
    
    /**
     * 获取默认命令前缀
     * @return 默认命令前缀字符串
     */
    public String getDefaultCommandPrefix() {
        String[] defaultPrefix = OsUtils.getDefaultCommandPrefix();
        return String.join(" ", defaultPrefix);
    }
    
    /**
     * 验证工具目录是否有效
     * @param directoryPath 目录路径
     * @return 验证结果
     */
    public DirectoryValidationResult validateToolDirectory(String directoryPath) {
        if (directoryPath == null || directoryPath.trim().isEmpty()) {
            return new DirectoryValidationResult(false, "工具目录不能为空");
        }
        
        File directory = new File(directoryPath.trim());
        if (!directory.exists()) {
            return new DirectoryValidationResult(false, "目录不存在", true);
        }
        
        if (!directory.isDirectory()) {
            return new DirectoryValidationResult(false, "指定路径不是有效目录");
        }
        
        return new DirectoryValidationResult(true, "目录有效");
    }
    
    /**
     * 创建目录
     * @param directoryPath 目录路径
     * @throws IOException 创建失败异常
     */
    public void createDirectory(String directoryPath) throws IOException {
        File directory = new File(directoryPath);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("无法创建目录: " + directoryPath);
        }
    }

    /**
     * 自动检测系统语言
     * @return 推荐的语言设置
     */
    public I18nManager.SupportedLanguage detectSystemLanguage() {
        try {
            Locale systemLocale = Locale.getDefault();
            String language = systemLocale.getLanguage();
            String country = systemLocale.getCountry();
            
            // 判断是否为中文地区
            if (isChineseLocale(language, country)) {
                return I18nManager.SupportedLanguage.CHINESE;
            } else {
                return I18nManager.SupportedLanguage.ENGLISH;
            }
        } catch (Exception e) {
            return I18nManager.SupportedLanguage.ENGLISH; // 默认英文
        }
    }
    
    /**
     * 判断是否为中文地区
     * @param language 语言代码
     * @param country 国家代码
     * @return 是否为中文地区
     */
    private boolean isChineseLocale(String language, String country) {
        // 检查语言代码
        if ("zh".equalsIgnoreCase(language)) {
            return true;
        }
        
        // 检查国家代码（中文地区）
        if ("CN".equalsIgnoreCase(country) ||    // 中国大陆
            "TW".equalsIgnoreCase(country) ||    // 台湾
            "HK".equalsIgnoreCase(country) ||    // 香港
            "MO".equalsIgnoreCase(country) ||    // 澳门
            "SG".equalsIgnoreCase(country)) {    // 新加坡（有中文用户）
            return true;
        }
        
        return false;
    }
    
    /**
     * 获取系统信息
     * @return 系统信息字符串
     */
    public String getSystemInfo() {
        return OsUtils.getOsType() + " (" + OsUtils.getOsName() + ")";
    }
    
    /**
     * 获取插件名称
     * @return 插件名称
     */
    public String getPluginName() {
        return pluginName;
    }
    
    /**
     * 获取插件版本
     * @return 插件版本
     */
    public String getPluginVersion() {
        return pluginVersion;
    }
    
    /**
     * 获取插件作者
     * @return 插件作者
     */
    public String getPluginAuthor() {
        return pluginAuthor;
    }
    
    /**
     * 获取设置统计信息
     * @return 统计信息
     */
    public SettingStatistics getStatistics() {
        boolean hasToolDirectory = !getToolDirectory().isEmpty();
        boolean hasCustomPrefix = !getCommandPrefix().isEmpty();
        I18nManager.SupportedLanguage currentLanguage = I18nManager.getInstance().getCurrentLanguage();
        I18nManager.SupportedLanguage systemLanguage = detectSystemLanguage();
        boolean languageMatchesSystem = currentLanguage == systemLanguage;
        
        return new SettingStatistics(hasToolDirectory, hasCustomPrefix, languageMatchesSystem, currentLanguage);
    }
    
    /**
     * 目录验证结果
     */
    public static class DirectoryValidationResult {
        private final boolean valid;
        private final String message;
        private final boolean canCreate;
        
        public DirectoryValidationResult(boolean valid, String message) {
            this(valid, message, false);
        }
        
        public DirectoryValidationResult(boolean valid, String message, boolean canCreate) {
            this.valid = valid;
            this.message = message;
            this.canCreate = canCreate;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
        
        public boolean canCreate() {
            return canCreate;
        }
    }
    
    /**
     * 设置统计信息
     */
    public static class SettingStatistics {
        private final boolean hasToolDirectory;
        private final boolean hasCustomPrefix;
        private final boolean languageMatchesSystem;
        private final I18nManager.SupportedLanguage currentLanguage;
        
        public SettingStatistics(boolean hasToolDirectory, boolean hasCustomPrefix, 
                               boolean languageMatchesSystem, I18nManager.SupportedLanguage currentLanguage) {
            this.hasToolDirectory = hasToolDirectory;
            this.hasCustomPrefix = hasCustomPrefix;
            this.languageMatchesSystem = languageMatchesSystem;
            this.currentLanguage = currentLanguage;
        }
        
        public boolean hasToolDirectory() {
            return hasToolDirectory;
        }
        
        public boolean hasCustomPrefix() {
            return hasCustomPrefix;
        }
        
        public boolean isLanguageMatchesSystem() {
            return languageMatchesSystem;
        }
        
        public I18nManager.SupportedLanguage getCurrentLanguage() {
            return currentLanguage;
        }
        
        public String getSummary() {
            return String.format("工具目录: %s, 自定义前缀: %s, 语言匹配: %s", 
                hasToolDirectory ? "已设置" : "未设置",
                hasCustomPrefix ? "已设置" : "未设置", 
                languageMatchesSystem ? "是" : "否");
        }
    }
} 