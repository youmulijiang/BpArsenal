import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.extension.ExtensionUnloadingHandler;
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
            I18nManager i18n = I18nManager.getInstance();
            api.extension().setName(i18n.getText("main.title"));

            api.logging().logToOutput(String.format(
                    "[   Pwn The Planet, One HTTP at a Time  ]\n" +
                            "[#] Author: youmulijiang\n" +
                            "[#] Github: https://github.com/youmulijiang/BpArsenal\n" +
                            "[#] Version: 1.0.0\n" +
                            "[#] Desc: Burp Suite Arsenal plugin - one-click convert HTTP requests to CLI tools, launch third-party tools, and access security bookmarks.\n"
            ));


            // 初始化API管理器
            ApiManager.getInstance().setApi(api);

            // 初始化配置管理器
            ConfigManager.getInstance();

            // 创建主面板（会自动初始化所有子面板
            MainPanel mainPanel = new MainPanel();

            api = ApiManager.getInstance().getApi();

            // 注册主面板到Burp Suite
            api.userInterface().registerSuiteTab(i18n.getText("main.title"), mainPanel);

            // 注册菜单栏
            try {
                api.userInterface().menuBar().registerMenu(
                    ArsenalMenuProvider.createBpArsenalMenu()
                );
                
                
            } catch (Exception e) {
                // 菜单注册失败，但不影响扩展加载
            }

            // 注册上下文菜单
            try {
                api.userInterface().registerContextMenuItemsProvider(
                    new ArsenalContextMenuProvider()
                );
                
                
            } catch (Exception e) {
                // 上下文菜单注册失败，但不影响扩展加载
            }


        } catch (Exception e) {
            // 初始化失败，静默处理
            e.printStackTrace();
        }
    }
}

