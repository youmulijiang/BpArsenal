package model;

import util.I18nManager;
import util.JsonUtil;
import util.OsUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Locale;

/**
 * 设置数据模型 (Model层)
 * 负责管理插件设置数据和配置文件操作
 */
public class SettingModel {
    
    // 配置文件路径
    private static final String TOOL_CONFIG_FILE = "tool_settings.properties";
    private static final String CONFIG_FILE_PATH = "src/main/resources/config.json";
    
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
                System.err.println("加载工具设置失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 保存工具设置
     * @throws IOException 保存失败异常
     */
    public void saveToolSettings() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(TOOL_CONFIG_FILE)) {
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
        Files.createDirectories(Paths.get(directoryPath));
    }
    
    /**
     * 备份当前配置文件
     * @return 备份文件路径
     * @throws IOException 备份失败异常
     */
    public String backupCurrentConfig() throws IOException {
        Path configPath = Paths.get(CONFIG_FILE_PATH);
        if (Files.exists(configPath)) {
            String backupFileName = "config_backup_" + System.currentTimeMillis() + ".json";
            Path backupPath = Paths.get("src/main/resources/" + backupFileName);
            Files.copy(configPath, backupPath);
            return backupPath.toString();
        }
        return null;
    }
    
    /**
     * 导入配置文件
     * @param configFilePath 配置文件路径
     * @throws Exception 导入失败异常
     */
    public void importConfiguration(String configFilePath) throws Exception {
        // 读取文件内容
        String jsonContent = new String(Files.readAllBytes(Paths.get(configFilePath)), StandardCharsets.UTF_8);
        
        // 验证JSON格式
        Config config = JsonUtil.fromJson(jsonContent, Config.class);
        
        // 写入新配置
        Path configPath = Paths.get(CONFIG_FILE_PATH);
        Files.write(configPath, jsonContent.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 导出配置文件
     * @param exportFilePath 导出文件路径
     * @param config 配置对象
     * @throws Exception 导出失败异常
     */
    public void exportConfiguration(String exportFilePath, Config config) throws Exception {
        // 转换为JSON
        String jsonContent = JsonUtil.toJson(config);
        
        // 写入文件
        Files.write(Paths.get(exportFilePath), jsonContent.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 重置配置文件
     * @throws Exception 重置失败异常
     */
    public void resetConfiguration() throws Exception {
        // 创建默认配置
        Config defaultConfig = createDefaultConfig();
        
        // 写入默认配置
        String jsonContent = JsonUtil.toJson(defaultConfig);
        Path configPath = Paths.get(CONFIG_FILE_PATH);
        Files.write(configPath, jsonContent.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 创建默认配置
     * @return 默认配置对象
     */
    private Config createDefaultConfig() {
        Config config = new Config();
        config.setHttpTool(new java.util.ArrayList<>());
        config.setThirtyPart(new java.util.ArrayList<>());
        config.setWebSite(new java.util.ArrayList<>());
        return config;
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