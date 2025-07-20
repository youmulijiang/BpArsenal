import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import manager.ApiManager;
import manager.ConfigManager;
import view.MainPanel;
import view.menu.ArsenalMenuProvider;
import view.contextmenu.ArsenalContextMenuProvider;

/**
 * BpArsenal主入口类
 * 武器库安全测试工具集合
 */
public class BpArsenal implements BurpExtension {

    @Override
    public void initialize(MontoyaApi api) {
        try {
            // 设置扩展名称
            api.extension().setName("BpArsenal - 武器库");

            // 初始化API管理器
            ApiManager.getInstance().setApi(api);

            // 初始化配置管理器
            ConfigManager.getInstance();

            // 创建主面板（会自动初始化所有子面板）
            MainPanel mainPanel = new MainPanel();

            // 注册主面板到Burp Suite
            api.userInterface().registerSuiteTab("BpArsenal", mainPanel);

            // 注册菜单栏
            try {
                api.userInterface().menuBar().registerMenu(
                    ArsenalMenuProvider.createBpArsenalMenu()
                );
                
                // 记录菜单注册成功
                api.logging().logToOutput("BpArsenal: 菜单栏注册成功");
                
            } catch (Exception e) {
                // 菜单注册失败，记录错误但不影响扩展加载
                api.logging().logToError("BpArsenal: 菜单栏注册失败 - " + e.getMessage());
            }

            // 注册上下文菜单
            try {
                api.userInterface().registerContextMenuItemsProvider(
                    new ArsenalContextMenuProvider()
                );
                
                // 记录上下文菜单注册成功
                api.logging().logToOutput("BpArsenal: 上下文菜单注册成功");
                
            } catch (Exception e) {
                // 上下文菜单注册失败，记录错误但不影响扩展加载
                api.logging().logToError("BpArsenal: 上下文菜单注册失败 - " + e.getMessage());
            }

            // 记录扩展加载成功
            api.logging().logToOutput("BpArsenal 武器库扩展加载成功！");
            api.logging().logToOutput("功能: HTTP工具、第三方工具、网站导航、配置管理、快速菜单访问、右键菜单");

        } catch (Exception e) {
            // 记录初始化错误
            if (ApiManager.getInstance().isInitialized()) {
                ApiManager.getInstance().getApi().logging().logToError(
                    "BpArsenal 初始化失败: " + e.getMessage()
                );
            } else {
                System.err.println("BpArsenal 初始化失败: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }
}
