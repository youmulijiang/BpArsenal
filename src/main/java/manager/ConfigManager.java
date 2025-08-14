package manager;

import model.Config;
import util.JsonUtil;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.io.File;
import javax.swing.JOptionPane;

/**
 * 配置管理器，负责配置文件的加载和管理
 * 采用单例模式确保全局唯一实例
 * 支持用户目录持久化配置
 */
public class ConfigManager {
    private static ConfigManager instance;
    private Config config;
    private final String RESOURCE_CONFIG_PATH = "/config.json";
    private final String USER_CONFIG_DIR = System.getProperty("user.home") + File.separator + ".bparsenal";
    private final String USER_CONFIG_FILE = USER_CONFIG_DIR + File.separator + "config.json";
    
    /**
     * 私有构造函数，自动加载配置文件
     */
    private ConfigManager() {
        loadConfig();
    }
    
    /**
     * 获取ConfigManager单例实例
     * @return ConfigManager实例
     */
    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
    
    /**
     * 获取配置对象
     * @return Config配置对象
     */
    public Config getConfig() {
        return config;
    }
    
    /**
     * 重新加载配置文件
     */
    public void reloadConfig() {
        loadConfig();
    }
    
    /**
     * 加载配置文件
     * 优先加载用户目录配置，如不存在则从资源文件复制默认配置
     */
    private void loadConfig() {
        try {
            // 确保用户配置目录存在
            ensureUserConfigDirectory();
            
            // 首先尝试加载用户配置文件
            File userConfigFile = new File(USER_CONFIG_FILE);
            if (userConfigFile.exists()) {
                loadUserConfig();
            } else {
                // 用户配置不存在，从资源文件复制默认配置
                copyDefaultConfigToUserDirectory();
                loadUserConfig();
            }
            
            if (ApiManager.getInstance().isInitialized()) {
                ApiManager.getInstance().getApi().logging().logToOutput("配置文件加载成功: " + USER_CONFIG_FILE);
            }
            
        } catch (Exception e) {
            String errorMsg = "加载配置文件失败: " + e.getMessage();
            
            if (ApiManager.getInstance().isInitialized()) {
                ApiManager.getInstance().getApi().logging().logToError(errorMsg);
            }
            
            // 创建默认配置
            this.config = createDefaultConfig();
            
            // 尝试保存默认配置到用户目录
            try {
                saveConfig();
            } catch (Exception saveEx) {
                if (ApiManager.getInstance().isInitialized()) {
                    ApiManager.getInstance().getApi().logging().logToError("保存默认配置失败: " + saveEx.getMessage());
                }
            }
            
            // 显示错误提示
            JOptionPane.showMessageDialog(
                null, 
                "配置文件加载失败，使用默认配置\n" + e.getMessage(), 
                "配置错误", 
                JOptionPane.WARNING_MESSAGE
            );
        }
    }
    
    /**
     * 确保用户配置目录存在
     * @throws IOException 目录创建失败
     */
    private void ensureUserConfigDirectory() throws IOException {
        File configDir = new File(USER_CONFIG_DIR);
        if (!configDir.exists()) {
            if (!configDir.mkdirs()) {
                throw new IOException("无法创建用户配置目录: " + USER_CONFIG_DIR);
            }
            
            if (ApiManager.getInstance().isInitialized()) {
                ApiManager.getInstance().getApi().logging().logToOutput("创建用户配置目录: " + USER_CONFIG_DIR);
            }
        }
    }
    
    /**
     * 从资源文件复制默认配置到用户目录
     * @throws IOException 复制失败
     */
    private void copyDefaultConfigToUserDirectory() throws IOException {
        InputStream inputStream = getClass().getResourceAsStream(RESOURCE_CONFIG_PATH);
        if (inputStream == null) {
            throw new IOException("默认配置文件不存在: " + RESOURCE_CONFIG_PATH);
        }
        
        String jsonContent;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            jsonContent = reader.lines().collect(Collectors.joining("\n"));
        }
        
        // 写入用户配置文件
        try (FileWriter writer = new FileWriter(USER_CONFIG_FILE, StandardCharsets.UTF_8)) {
            writer.write(jsonContent);
        }
        
        if (ApiManager.getInstance().isInitialized()) {
            ApiManager.getInstance().getApi().logging().logToOutput("复制默认配置到用户目录: " + USER_CONFIG_FILE);
        }
    }
    
    /**
     * 加载用户配置文件
     * @throws IOException 加载失败
     */
    private void loadUserConfig() throws IOException {
        String jsonContent;
        try (FileReader reader = new FileReader(USER_CONFIG_FILE, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            jsonContent = bufferedReader.lines().collect(Collectors.joining("\n"));
        }
        
        this.config = JsonUtil.fromJson(jsonContent, Config.class);
        
        if (ApiManager.getInstance().isInitialized()) {
            ApiManager.getInstance().getApi().logging().logToOutput("从用户目录加载配置: " + USER_CONFIG_FILE);
        }
    }
    
    /**
     * 保存配置到用户目录
     * @throws IOException 保存失败
     */
    public void saveConfig() throws IOException {
        if (config == null) {
            throw new IllegalStateException("配置对象为空，无法保存");
        }
        
        // 确保用户配置目录存在
        ensureUserConfigDirectory();
        
        // 将配置序列化为JSON
        String jsonContent = JsonUtil.toJson(config);
        
        // 写入用户配置文件
        try (FileWriter writer = new FileWriter(USER_CONFIG_FILE, StandardCharsets.UTF_8)) {
            writer.write(jsonContent);
        }
        
        if (ApiManager.getInstance().isInitialized()) {
            ApiManager.getInstance().getApi().logging().logToOutput("配置已保存到用户目录: " + USER_CONFIG_FILE);
        }
    }
    
    /**
     * 重置为默认配置
     * @throws IOException 重置失败
     */
    public void resetToDefaultConfig() throws IOException {
        // 删除用户配置文件
        File userConfigFile = new File(USER_CONFIG_FILE);
        if (userConfigFile.exists()) {
            if (!userConfigFile.delete()) {
                if (ApiManager.getInstance().isInitialized()) {
                    ApiManager.getInstance().getApi().logging().logToError("无法删除用户配置文件: " + USER_CONFIG_FILE);
                }
            }
        }
        
        // 重新复制默认配置
        copyDefaultConfigToUserDirectory();
        
        // 重新加载配置
        loadUserConfig();
        
        if (ApiManager.getInstance().isInitialized()) {
            ApiManager.getInstance().getApi().logging().logToOutput("配置已重置为默认配置");
        }
    }
    
    /**
     * 导入配置文件
     * @param configFilePath 配置文件路径
     * @throws IOException 导入失败
     */
    public void importConfig(String configFilePath) throws IOException {
        String jsonContent;
        try (FileReader reader = new FileReader(configFilePath, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            jsonContent = bufferedReader.lines().collect(Collectors.joining("\n"));
        }
        
        // 验证配置文件格式
        Config importedConfig = JsonUtil.fromJson(jsonContent, Config.class);
        if (importedConfig == null) {
            throw new IOException("配置文件格式无效");
        }
        
        this.config = importedConfig;
        
        // 保存到用户目录
        saveConfig();
        
        if (ApiManager.getInstance().isInitialized()) {
            ApiManager.getInstance().getApi().logging().logToOutput("配置已从文件导入: " + configFilePath);
        }
    }
    
    /**
     * 导出配置文件
     * @param targetFilePath 目标文件路径
     * @throws IOException 导出失败
     */
    public void exportConfig(String targetFilePath) throws IOException {
        if (config == null) {
            throw new IllegalStateException("配置对象为空，无法导出");
        }
        
        String jsonContent = JsonUtil.toJson(config);
        
        try (FileWriter writer = new FileWriter(targetFilePath, StandardCharsets.UTF_8)) {
            writer.write(jsonContent);
        }
        
        if (ApiManager.getInstance().isInitialized()) {
            ApiManager.getInstance().getApi().logging().logToOutput("配置已导出到文件: " + targetFilePath);
        }
    }
    
    /**
     * 获取用户配置目录路径
     * @return 用户配置目录路径
     */
    public String getUserConfigDirectory() {
        return USER_CONFIG_DIR;
    }
    
    /**
     * 获取用户配置文件路径
     * @return 用户配置文件路径
     */
    public String getUserConfigFile() {
        return USER_CONFIG_FILE;
    }
    
    /**
     * 创建默认配置
     * @return 默认Config对象
     */
    private Config createDefaultConfig() {
        Config defaultConfig = new Config();
        defaultConfig.setHttpTool(new java.util.ArrayList<>());
        defaultConfig.setThirtyPart(new java.util.ArrayList<>());
        defaultConfig.setWebSite(new java.util.ArrayList<>());
        return defaultConfig;
    }
} 