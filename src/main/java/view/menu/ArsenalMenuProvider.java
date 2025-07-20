package view.menu;

import burp.api.montoya.ui.menu.BasicMenuItem;
import burp.api.montoya.ui.menu.Menu;
import burp.api.montoya.ui.menu.MenuItem;
import controller.ToolController;
import model.ThirdPartyTool;
import model.WebSite;
import executor.ToolExecutor;
import manager.ApiManager;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Arsenal菜单提供者
 * 提供收藏的工具和网站的快速访问菜单
 */
public class ArsenalMenuProvider {
    
    /**
     * 创建BpArsenal主菜单
     * @return BpArsenal菜单
     */
    public static Menu createBpArsenalMenu() {
        List<MenuItem> menuItems = new ArrayList<>();
        
        // 添加收藏工具的快速访问
        menuItems.addAll(createFavoriteToolItems());
        
        // 添加分隔符
        if (!menuItems.isEmpty()) {
            menuItems.add(BasicMenuItem.basicMenuItem("───────────────")
                    .withAction(() -> {}));
        }
        
        // 添加收藏网站的快速访问
        menuItems.addAll(createFavoriteWebsiteItems());
        
        // 如果没有任何收藏项，添加默认项
        if (menuItems.isEmpty() || (menuItems.size() == 1 && menuItems.get(0).caption().contains("───"))) {
            menuItems.clear();
            menuItems.add(BasicMenuItem.basicMenuItem("打开BpArsenal面板")
                    .withAction(() -> openMainPanel()));
        } else {
            // 添加管理选项
            menuItems.add(BasicMenuItem.basicMenuItem("───────────────")
                    .withAction(() -> {}));
            menuItems.add(BasicMenuItem.basicMenuItem("打开BpArsenal面板")
                    .withAction(() -> openMainPanel()));
        }
        
        // 创建主菜单
        return Menu.menu("BpArsenal")
                .withMenuItems(menuItems);
    }
    
    /**
     * 创建收藏工具的菜单项列表
     * @return 工具菜单项列表
     */
    private static List<MenuItem> createFavoriteToolItems() {
        List<MenuItem> toolItems = new ArrayList<>();
        
        try {
            // 获取收藏的第三方工具
            List<ThirdPartyTool> favoriteTools = ToolController.getInstance()
                    .getAllThirdPartyTools()
                    .stream()
                    .filter(ThirdPartyTool::isFavor)
                    .collect(Collectors.toList());
            
            // 限制显示数量，避免菜单过长
            int maxItems = Math.min(favoriteTools.size(), 8);
            for (int i = 0; i < maxItems; i++) {
                ThirdPartyTool tool = favoriteTools.get(i);
                toolItems.add(BasicMenuItem.basicMenuItem("🔧 " + tool.getToolName())
                        .withAction(() -> launchTool(tool)));
            }
            
            // 如果有更多工具，添加提示
            if (favoriteTools.size() > maxItems) {
                toolItems.add(BasicMenuItem.basicMenuItem("... 更多工具请在面板中查看")
                        .withAction(() -> openMainPanel()));
            }
            
        } catch (Exception e) {
            // 错误处理
            toolItems.add(BasicMenuItem.basicMenuItem("🔧 加载工具失败")
                    .withAction(() -> {
                        JOptionPane.showMessageDialog(null, 
                                "加载工具列表时发生错误：" + e.getMessage(),
                                "错误", 
                                JOptionPane.ERROR_MESSAGE);
                    }));
        }
        
        return toolItems;
    }
    
    /**
     * 创建收藏网站的菜单项列表
     * @return 网站菜单项列表
     */
    private static List<MenuItem> createFavoriteWebsiteItems() {
        List<MenuItem> websiteItems = new ArrayList<>();
        
        try {
            // 获取收藏的网站
            List<WebSite> favoriteWebsites = ToolController.getInstance()
                    .getAllWebSites()
                    .stream()
                    .filter(WebSite::isFavor)
                    .collect(Collectors.toList());
            
            // 限制显示数量，避免菜单过长
            int maxItems = Math.min(favoriteWebsites.size(), 8);
            for (int i = 0; i < maxItems; i++) {
                WebSite website = favoriteWebsites.get(i);
                websiteItems.add(BasicMenuItem.basicMenuItem("🌐 " + website.getDesc())
                        .withAction(() -> openWebsite(website)));
            }
            
            // 如果有更多网站，添加提示
            if (favoriteWebsites.size() > maxItems) {
                websiteItems.add(BasicMenuItem.basicMenuItem("... 更多网站请在面板中查看")
                        .withAction(() -> openMainPanel()));
            }
            
        } catch (Exception e) {
            // 错误处理
            websiteItems.add(BasicMenuItem.basicMenuItem("🌐 加载网站失败")
                    .withAction(() -> {
                        JOptionPane.showMessageDialog(null, 
                                "加载网站列表时发生错误：" + e.getMessage(),
                                "错误", 
                                JOptionPane.ERROR_MESSAGE);
                    }));
        }
        
        return websiteItems;
    }
    
    /**
     * 启动第三方工具
     * @param tool 要启动的工具
     */
    private static void launchTool(ThirdPartyTool tool) {
        try {
            SwingUtilities.invokeLater(() -> {
                try {
                    ToolExecutor.getInstance().executeThirdPartyTool(tool);
                    
                    // 显示启动提示
                    ApiManager.getInstance().getApi().logging().logToOutput(
                            "BpArsenal: 已启动工具 - " + tool.getToolName());
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, 
                            "启动工具失败：" + e.getMessage(),
                            "错误", 
                            JOptionPane.ERROR_MESSAGE);
                    
                    ApiManager.getInstance().getApi().logging().logToError(
                            "BpArsenal: 启动工具失败 - " + tool.getToolName() + ": " + e.getMessage());
                }
            });
        } catch (Exception e) {
            ApiManager.getInstance().getApi().logging().logToError(
                    "BpArsenal: 菜单启动工具异常 - " + e.getMessage());
        }
    }
    
    /**
     * 在浏览器中打开网站
     * @param website 要打开的网站
     */
    private static void openWebsite(WebSite website) {
        try {
            SwingUtilities.invokeLater(() -> {
                try {
                    // 检查URL格式
                    String url = website.getUrl();
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "https://" + url;
                    }
                    
                    // 在默认浏览器中打开
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(URI.create(url));
                        
                        // 记录访问日志
                        ApiManager.getInstance().getApi().logging().logToOutput(
                                "BpArsenal: 已打开网站 - " + website.getDesc() + " (" + url + ")");
                    } else {
                        JOptionPane.showMessageDialog(null, 
                                "系统不支持自动打开浏览器\n请手动访问：" + url,
                                "提示", 
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, 
                            "打开网站失败：" + e.getMessage(),
                            "错误", 
                            JOptionPane.ERROR_MESSAGE);
                    
                    ApiManager.getInstance().getApi().logging().logToError(
                            "BpArsenal: 打开网站失败 - " + website.getDesc() + ": " + e.getMessage());
                }
            });
        } catch (Exception e) {
            ApiManager.getInstance().getApi().logging().logToError(
                    "BpArsenal: 菜单打开网站异常 - " + e.getMessage());
        }
    }
    
    /**
     * 打开主面板
     */
    private static void openMainPanel() {
        try {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, 
                        "请在Burp Suite中切换到BpArsenal标签页\n\n功能包括：\n" +
                        "• HTTP工具管理\n" +
                        "• 第三方工具管理\n" +
                        "• 网站导航管理\n" +
                        "• 配置导入/导出",
                        "BpArsenal武器库", 
                        JOptionPane.INFORMATION_MESSAGE);
            });
        } catch (Exception e) {
            ApiManager.getInstance().getApi().logging().logToError(
                    "BpArsenal: 打开主面板异常 - " + e.getMessage());
        }
    }
} 