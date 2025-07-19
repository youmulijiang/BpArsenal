import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import manager.ApiManager;
import manager.ConfigManager;
import ui.MainPanel;

/**
 * BpArsenal Burp扩展主入口类
 * 实现BurpExtension接口，提供插件初始化功能
 */
public class BpArsenal implements BurpExtension {

    @Override
    public void initialize(MontoyaApi api) {
        try {
            // 初始化API管理器
            ApiManager.getInstance().setApi(api);
            api.logging().logToOutput("BpArsenal插件开始初始化...");
            
            // 初始化配置管理器
            ConfigManager.getInstance();
            api.logging().logToOutput("配置管理器初始化完成");
            
            // 注册UI组件
            MainPanel mainPanel = new MainPanel();
            api.userInterface().registerSuiteTab("BpArsenal", mainPanel);
            api.logging().logToOutput("UI组件注册完成");
            
            // TODO: 注册上下文菜单
            
            api.logging().logToOutput("BpArsenal插件初始化完成");
            
        } catch (Exception e) {
            api.logging().logToError("BpArsenal插件初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
