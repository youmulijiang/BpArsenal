package view.contextmenu;

import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import manager.ApiManager;
import model.HttpToolCommand;
import view.component.ArsenalDialog;
import controller.ToolController;
import executor.ToolExecutor;
import executor.CommandRenderingStrategy;
import util.ContextMenuEventHandler;
import util.MenuUtils;

import javax.swing.*;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Arsenal上下文菜单提供者
 * 在右键菜单中提供Favorite和Arsenal选项
 */
public class ArsenalContextMenuProvider implements ContextMenuItemsProvider {
    
    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        List<Component> menuItems = new ArrayList<>();
        
        // 只在HTTP请求/响应上下文中显示菜单
//        if (event.invocationType() == InvocationType.MESSAGE_EDITOR_REQUEST ||
//            event.invocationType() == InvocationType.MESSAGE_EDITOR_RESPONSE ||
//            event.invocationType() == InvocationType.PROXY_HISTORY ||
//            event.invocationType() == InvocationType.SITE_MAP_TREE ||
//            event.invocationType() == InvocationType.SITE_MAP_TABLE) {
//
//            // 创建Favorite菜单项
//            JMenuItem favoriteItem = new JMenuItem("Favorite");
//            favoriteItem.addActionListener(e -> handleFavoriteAction(event));
//            menuItems.add(favoriteItem);
//
//            // 创建Arsenal菜单项
//            JMenuItem arsenalItem = new JMenuItem("Arsenal");
//            arsenalItem.addActionListener(e -> handleArsenalAction(event));
//            menuItems.add(arsenalItem);
//        }

        // 创建Favorite子菜单
        JMenu favoriteMenu = createFavoriteMenu(event);
        if (favoriteMenu != null) {
            menuItems.add(favoriteMenu);
        }

        // 创建Arsenal菜单项
        JMenuItem arsenalItem = new JMenuItem("Arsenal");
        arsenalItem.addActionListener(e -> handleArsenalAction(event));
        menuItems.add(arsenalItem);
        
        return menuItems;
    }
    
    /**
     * 创建Favorite菜单
     * @param event 上下文菜单事件
     * @return Favorite菜单，如果没有收藏工具则返回null
     */
    private JMenu createFavoriteMenu(ContextMenuEvent event) {
        try {
            // 使用工具类获取HTTP数据
            if (!ContextMenuEventHandler.hasValidHttpData(event)) {
                return null;
            }
            
            HttpRequest httpRequest = ContextMenuEventHandler.getHttpRequestFromEvent(event);
            HttpResponse httpResponse = ContextMenuEventHandler.getHttpResponseFromEvent(event);
            
            // 获取所有收藏的工具命令
            List<HttpToolCommand> favoriteCommands = getFavoriteToolCommands();
            
            if (favoriteCommands.isEmpty()) {
                return null;
            }
            
            // 使用MenuUtils创建主菜单
            JMenu favoriteMenu = MenuUtils.createMainMenu("Favorite");
            
            // 按分类分组收藏的命令
            Map<String, List<HttpToolCommand>> commandsByCategory = groupCommandsByCategory(favoriteCommands);
            
            // 为每个分类创建子菜单
            List<String> sortedCategories = commandsByCategory.keySet().stream()
                    .sorted()
                    .collect(Collectors.toList());
            
            for (String category : sortedCategories) {
                List<HttpToolCommand> categoryCommands = commandsByCategory.get(category);
                
                // 使用MenuUtils创建分类子菜单
                JMenu categoryMenu = MenuUtils.createCategoryMenu(category, categoryCommands.size());
                
                // 添加该分类下的所有命令
                for (HttpToolCommand command : categoryCommands) {
                    JMenuItem commandItem = createToolMenuItem(command, httpRequest, httpResponse, event);
                    categoryMenu.add(commandItem);
                }
                
                favoriteMenu.add(categoryMenu);
            }
            
            return favoriteMenu;
            
        } catch (Exception ex) {
            ApiManager.getInstance().getApi().logging().logToError("创建收藏菜单失败: " + ex.getMessage());
            return null;
        }
    }
    
    /**
     * 处理Arsenal菜单项点击事件
     * @param event 上下文菜单事件
     */
    private void handleArsenalAction(ContextMenuEvent event) {
        try {
            // 设置当前上下文菜单事件
            currentContextMenuEvent = event;
            
            // 使用工具类获取HTTP数据
            HttpRequest httpRequest = ContextMenuEventHandler.getHttpRequestFromEvent(event);
            HttpResponse httpResponse = ContextMenuEventHandler.getHttpResponseFromEvent(event);
            List<HttpRequest> allSelectedRequests = ContextMenuEventHandler.getAllSelectedRequests(event);
            
            // 记录选中的请求数量
            int selectedCount = ContextMenuEventHandler.getSelectedCount(event);
            ApiManager.getInstance().getApi().logging().logToOutput(
                String.format("BpArsenal: 选中了 %d 个HTTP请求", selectedCount)
            );

            if (httpRequest != null) {
                // 创建并显示Arsenal工具对话框
                SwingUtilities.invokeLater(() -> {
                    try {
                        ArsenalDialog dialog = new ArsenalDialog(httpRequest, httpResponse, allSelectedRequests);

                        // 获取Burp Suite主窗口并将对话框居中显示
                        Frame burpFrame = ApiManager.getInstance().getApi().userInterface().swingUtils().suiteFrame();
                        if (burpFrame != null) {
                            dialog.setLocationRelativeTo(null);
                            // 显示对话框后立即取消置顶，避免影响用户操作其他应用
                            SwingUtilities.invokeLater(() -> {
//                                dialog.setAlwaysOnTop(true);
                                dialog.toFront();
                                dialog.requestFocus();
                            });
                        }

                        dialog.setVisible(true);

                        // 再次确保对话框在最前面（解决某些系统的窗口管理问题）
                        dialog.bringToFront();

                        if (ApiManager.getInstance().isInitialized()) {
                            ApiManager.getInstance().getApi().logging().logToOutput(
                                "BpArsenal: Arsenal工具对话框已在Burp Suite主窗口上方居中显示"
                            );
                        }

                    } catch (Exception ex) {
                        ApiManager.getInstance().getApi().logging().logToError(
                            "BpArsenal: 创建Arsenal对话框失败: " + ex.getMessage()
                        );

                        // 显示错误提示
                        JOptionPane.showMessageDialog(
                            null,
                            "打开Arsenal对话框失败:\n" + ex.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                });

            } else {
                ApiManager.getInstance().getApi().logging().logToError("无法获取HTTP请求数据");
            }
        } catch (Exception ex) {
            ApiManager.getInstance().getApi().logging().logToError("打开Arsenal对话框失败: " + ex.getMessage());
        }
    }
    

    
    /**
     * 按分类分组命令
     */
    private Map<String, List<HttpToolCommand>> groupCommandsByCategory(List<HttpToolCommand> commands) {
        Map<String, List<HttpToolCommand>> grouped = new HashMap<>();
        
        for (HttpToolCommand command : commands) {
            String category = command.getCategory();
            if (category == null || category.trim().isEmpty()) {
                category = "未分类";
            }
            
            grouped.computeIfAbsent(category, k -> new ArrayList<>()).add(command);
        }
        
        return grouped;
    }
    
    /**
     * 创建工具菜单项
     */
    private JMenuItem createToolMenuItem(HttpToolCommand command, HttpRequest httpRequest, HttpResponse httpResponse, ContextMenuEvent event) {
        String displayText = String.format("%s - %s", 
            command.getDisplayName(), 
            MenuUtils.truncateText(command.getCommand(), 60));
        
        String toolTip = MenuUtils.createToolTipHtml(
            command.getDisplayName(),
            command.getCommand(),
            command.getCategory()
        );
        
        return MenuUtils.createToolMenuItem(displayText, toolTip, e -> {
            // 设置当前上下文菜单事件
            currentContextMenuEvent = event;
            executeToolCommand(command, httpRequest, httpResponse);
        });
    }
    
    /**
     * 获取所有收藏的工具命令
     */
    private List<HttpToolCommand> getFavoriteToolCommands() {
        try {
            List<HttpToolCommand> allCommands = ToolController.getInstance().getAllToolCommands();
            return allCommands.stream()
                    .filter(HttpToolCommand::isFavor)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            ApiManager.getInstance().getApi().logging().logToError("获取收藏工具失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    

    
    /**
     * 执行工具命令
     */
    private void executeToolCommand(HttpToolCommand toolCommand, HttpRequest httpRequest, HttpResponse httpResponse) {
        try {
            // 记录执行信息
            String toolName = toolCommand.getToolName();
            ApiManager.getInstance().getApi().logging().logToOutput(
                "BpArsenal: 执行收藏工具 - " + toolName
            );
            
            // 渲染命令
            String renderedCommand = generateRenderedCommand(toolCommand, httpRequest, httpResponse);
            
            if (renderedCommand == null || renderedCommand.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, 
                    "命令渲染失败或为空，无法执行", 
                    "执行失败", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 执行命令（使用工作目录支持）
            executeToolCommandWithWorkDir(toolCommand, renderedCommand, toolName);
            
        } catch (Exception e) {
            String errorMsg = "执行工具命令失败: " + e.getMessage();
            ApiManager.getInstance().getApi().logging().logToError(errorMsg);
            JOptionPane.showMessageDialog(null, errorMsg, "执行失败", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 生成渲染后的命令
     */
    private String generateRenderedCommand(HttpToolCommand toolCommand, HttpRequest httpRequest, HttpResponse httpResponse) {
        try {
            // 获取多个选中的请求（用于httpList变量）
            ContextMenuEvent currentEvent = getCurrentContextMenuEvent();
            List<HttpRequest> allSelectedRequests = ContextMenuEventHandler.getAllSelectedRequests(currentEvent);
            
            if (allSelectedRequests.isEmpty() && httpRequest != null) {
                allSelectedRequests.add(httpRequest);
            }
            
            // 使用第一个请求作为主要请求（向后兼容）
            HttpRequest primaryRequest = allSelectedRequests.isEmpty() ? httpRequest : allSelectedRequests.get(0);
            
            // 使用策略模式渲染命令
            return CommandRenderingStrategy.renderCommand(
                toolCommand, primaryRequest, httpResponse, allSelectedRequests
            );
            
        } catch (Exception e) {
            ApiManager.getInstance().getApi().logging().logToError("命令渲染失败: " + e.getMessage());
            return toolCommand.getCommand();
        }
    }

    
    // 添加一个成员变量来存储当前的上下文菜单事件
    private ContextMenuEvent currentContextMenuEvent;
    
    /**
     * 获取当前的上下文菜单事件
     * @return 当前事件
     */
    private ContextMenuEvent getCurrentContextMenuEvent() {
        return currentContextMenuEvent;
    }
    
    /**
     * 通过ToolExecutor执行命令
     */
    private void executeCommandViaScript(String command, String toolName) {
        try {
            // 使用ToolExecutor的命令执行功能，不传递工作目录（使用全局设置）
            ToolExecutor.getInstance().executeCommandSync(command, toolName, (String)null);
        } catch (Exception e) {
            ApiManager.getInstance().getApi().logging().logToError("命令执行失败: " + e.getMessage());
            throw new RuntimeException("命令执行失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 通过ToolExecutor执行工具命令（带工作目录支持）
     */
    private void executeToolCommandWithWorkDir(HttpToolCommand toolCommand, String command, String toolName) {
        try {
            // 获取工作目录并执行命令
            String workDir = toolCommand.getWorkDir();
            ToolExecutor.getInstance().executeCommandSync(command, toolName, workDir);
        } catch (Exception e) {
            ApiManager.getInstance().getApi().logging().logToError("命令执行失败: " + e.getMessage());
            throw new RuntimeException("命令执行失败: " + e.getMessage(), e);
        }
    }
} 