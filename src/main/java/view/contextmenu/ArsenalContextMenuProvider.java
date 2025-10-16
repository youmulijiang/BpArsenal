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
            

            if (httpRequest != null) {
                // 创建并显示Arsenal工具对话框
                SwingUtilities.invokeLater(() -> {
                    try {
                        // 获取Burp Suite主窗口作为父窗口
                        Frame burpFrame = ApiManager.getInstance().getApi().userInterface().swingUtils().suiteFrame();
                        
                        // 创建模态对话框，传入父窗口
                        ArsenalDialog dialog = new ArsenalDialog(httpRequest, httpResponse, allSelectedRequests);
                        
                        // 设置对话框相对于Burp Suite主窗口居中
                        dialog.setLocationRelativeTo(burpFrame);
                        
                        // 显示模态对话框
                        dialog.setVisible(true);


                    } catch (Exception ex) {

                        // 显示错误提示
                        I18nManager i18n = I18nManager.getInstance();
                        JOptionPane.showMessageDialog(
                            ApiManager.getInstance().getApi().userInterface().swingUtils().suiteFrame(),
                            i18n.getText("context.menu.open.arsenal.failed", ex.getMessage()),
                            i18n.getText("error.title"),
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                });

            } else {
            }
        } catch (Exception ex) {
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
                I18nManager i18n = I18nManager.getInstance();
                category = i18n.getText("common.uncategorized");
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
            
            // 渲染命令
            String renderedCommand = generateRenderedCommand(toolCommand, httpRequest, httpResponse);
            
            if (renderedCommand == null || renderedCommand.trim().isEmpty()) {
                I18nManager i18n = I18nManager.getInstance();
                JOptionPane.showMessageDialog(null, 
                    i18n.getText("context.menu.command.render.failed"), 
                    i18n.getText("context.menu.execution.failed"), 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 执行命令（使用工作目录支持）
            executeToolCommandWithWorkDir(toolCommand, renderedCommand, toolName);
            
        } catch (Exception e) {
            I18nManager i18n = I18nManager.getInstance();
            String errorMsg = i18n.getText("context.menu.tool.execution.failed", e.getMessage());
            JOptionPane.showMessageDialog(null, errorMsg, i18n.getText("context.menu.execution.failed"), JOptionPane.ERROR_MESSAGE);
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
     * 通过ToolExecutor执行工具命令（带工作目录支持）
     */
    private void executeToolCommandWithWorkDir(HttpToolCommand toolCommand, String command, String toolName) {
        try {
            // 获取工作目录并执行命令
            String workDir = toolCommand.getWorkDir();
            ToolExecutor.getInstance().executeCommandSync(command, toolName, workDir);
        } catch (Exception e) {
            I18nManager i18n = I18nManager.getInstance();
            throw new RuntimeException(i18n.getText("context.menu.command.execution.failed", e.getMessage()), e);
        }
    }
} 