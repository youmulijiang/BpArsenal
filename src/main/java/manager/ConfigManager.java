package manager;

import model.Config;
import util.JsonUtil;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;

/**
 * 配置管理器，负责配置文件的加载和管理
 * 采用单例模式确保全局唯一实例
 */
public class ConfigManager {
    private static ConfigManager instance;
    private Config config;
    private final String CONFIG_PATH = "/config.json";
    
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
     */
    private void loadConfig() {
        try {
            InputStream inputStream = getClass().getResourceAsStream(CONFIG_PATH);
            if (inputStream == null) {
                throw new IOException("配置文件不存在: " + CONFIG_PATH);
            }
            
            String jsonContent = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            this.config = JsonUtil.fromJson(jsonContent, Config.class);
            
            if (ApiManager.getInstance().isInitialized()) {
                ApiManager.getInstance().getApi().logging().logToOutput("配置文件加载成功");
            }
            
        } catch (Exception e) {
            String errorMsg = "加载配置文件失败: " + e.getMessage();
            
            if (ApiManager.getInstance().isInitialized()) {
                ApiManager.getInstance().getApi().logging().logToError(errorMsg);
            }
            
            // 创建默认配置
            this.config = createDefaultConfig();
            
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