package view.menu;

import controller.ToolController;
import model.ThirdPartyTool;
import model.WebSite;
import executor.ToolExecutor;
import manager.ApiManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
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
    public static JMenu createBpArsenalMenu() {
        // 创建主菜单
        JMenu mainMenu = new JMenu("BpArsenal");
        
        // 添加Tools子菜单
        JMenu toolsSubMenu = createToolsSubMenu();
        mainMenu.add(toolsSubMenu);
        
        // 添加Website子菜单
        JMenu websiteSubMenu = createWebsiteSubMenu();
        mainMenu.add(websiteSubMenu);
        
        return mainMenu;
    }
    
    /**
     * 创建Tools子菜单
     * @return Tools子菜单
     */
    private static JMenu createToolsSubMenu() {
        JMenu toolsMenu = new JMenu("Tools");
        
        try {
            // 获取收藏的第三方工具
            List<ThirdPartyTool> favoriteTools = ToolController.getInstance()
                    .getAllThirdPartyTools()
                    .stream()
                    .filter(ThirdPartyTool::isFavor)
                    .collect(Collectors.toList());
            
            if (favoriteTools.isEmpty()) {
                // 如果没有收藏的工具，添加提示菜单项
                JMenuItem noToolsItem = new JMenuItem("无收藏工具");
                noToolsItem.addActionListener(e -> {
                    JOptionPane.showMessageDialog(null, 
                            "暂无收藏的第三方工具\n请在BpArsenal面板中添加并收藏工具",
                            "提示", 
                            JOptionPane.INFORMATION_MESSAGE);
                });
                toolsMenu.add(noToolsItem);
            } else {
                // 限制显示数量，避免菜单过长
                int maxItems = Math.min(favoriteTools.size(), 10);
                for (int i = 0; i < maxItems; i++) {
                    ThirdPartyTool tool = favoriteTools.get(i);
                    JMenuItem toolItem = new JMenuItem(tool.getToolName());
                    toolItem.addActionListener(e -> launchTool(tool));
                    toolsMenu.add(toolItem);
                }
                
                // 如果有更多工具，添加提示
                if (favoriteTools.size() > maxItems) {
                    toolsMenu.addSeparator();
                    JMenuItem moreItem = new JMenuItem("更多工具请在面板中查看...");
                    moreItem.addActionListener(e -> openMainPanel());
                    toolsMenu.add(moreItem);
                }
            }
            
            // 添加分隔符和管理选项
            if (toolsMenu.getItemCount() > 0 && !favoriteTools.isEmpty()) {
                toolsMenu.addSeparator();
            }
            
            JMenuItem manageItem = new JMenuItem("管理工具...");
            manageItem.addActionListener(e -> openToolManagement());
            toolsMenu.add(manageItem);
            
        } catch (Exception e) {
            // 错误处理
            JMenuItem errorItem = new JMenuItem("加载工具失败");
            errorItem.addActionListener(ev -> {
                JOptionPane.showMessageDialog(null, 
                        "加载工具列表时发生错误：" + e.getMessage(),
                        "错误", 
                        JOptionPane.ERROR_MESSAGE);
            });
            toolsMenu.add(errorItem);
        }
        
        return toolsMenu;
    }
    
    /**
     * 创建Website子菜单
     * @return Website子菜单
     */
    private static JMenu createWebsiteSubMenu() {
        JMenu websiteMenu = new JMenu("Website");
        
        try {
            // 获取收藏的网站
            List<WebSite> favoriteWebsites = ToolController.getInstance()
                    .getAllWebSites()
                    .stream()
                    .filter(WebSite::isFavor)
                    .collect(Collectors.toList());
            
            if (favoriteWebsites.isEmpty()) {
                // 如果没有收藏的网站，添加提示菜单项
                JMenuItem noWebsitesItem = new JMenuItem("无收藏网站");
                noWebsitesItem.addActionListener(e -> {
                    JOptionPane.showMessageDialog(null, 
                            "暂无收藏的网站\n请在BpArsenal面板中添加并收藏网站",
                            "提示", 
                            JOptionPane.INFORMATION_MESSAGE);
                });
                websiteMenu.add(noWebsitesItem);
            } else {
                // 限制显示数量，避免菜单过长
                int maxItems = Math.min(favoriteWebsites.size(), 10);
                for (int i = 0; i < maxItems; i++) {
                    WebSite website = favoriteWebsites.get(i);
                    JMenuItem websiteItem = new JMenuItem(website.getDesc());
                    websiteItem.addActionListener(e -> openWebsite(website));
                    websiteMenu.add(websiteItem);
                }
                
                // 如果有更多网站，添加提示
                if (favoriteWebsites.size() > maxItems) {
                    websiteMenu.addSeparator();
                    JMenuItem moreItem = new JMenuItem("更多网站请在面板中查看...");
                    moreItem.addActionListener(e -> openMainPanel());
                    websiteMenu.add(moreItem);
                }
            }
            
            // 添加分隔符和管理选项
            if (websiteMenu.getItemCount() > 0 && !favoriteWebsites.isEmpty()) {
                websiteMenu.addSeparator();
            }
            
            JMenuItem manageItem = new JMenuItem("管理网站...");
            manageItem.addActionListener(e -> openWebsiteManagement());
            websiteMenu.add(manageItem);
            
        } catch (Exception e) {
            // 错误处理
            JMenuItem errorItem = new JMenuItem("加载网站失败");
            errorItem.addActionListener(ev -> {
                JOptionPane.showMessageDialog(null, 
                        "加载网站列表时发生错误：" + e.getMessage(),
                        "错误", 
                        JOptionPane.ERROR_MESSAGE);
            });
            websiteMenu.add(errorItem);
        }
        
        return websiteMenu;
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
     * 打开工具管理面板
     */
    private static void openToolManagement() {
        try {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, 
                        "请在Burp Suite中切换到BpArsenal标签页\n然后选择\"第三方工具\"选项卡来管理工具",
                        "工具管理", 
                        JOptionPane.INFORMATION_MESSAGE);
            });
        } catch (Exception e) {
            ApiManager.getInstance().getApi().logging().logToError(
                    "BpArsenal: 打开工具管理异常 - " + e.getMessage());
        }
    }
    
    /**
     * 打开网站管理面板
     */
    private static void openWebsiteManagement() {
        try {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, 
                        "请在Burp Suite中切换到BpArsenal标签页\n然后选择\"网站导航\"选项卡来管理网站",
                        "网站管理", 
                        JOptionPane.INFORMATION_MESSAGE);
            });
        } catch (Exception e) {
            ApiManager.getInstance().getApi().logging().logToError(
                    "BpArsenal: 打开网站管理异常 - " + e.getMessage());
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