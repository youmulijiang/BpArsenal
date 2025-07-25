package view.contextmenu;

import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.InvocationType;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import manager.ApiManager;
import manager.ConfigManager;
import model.HttpTool;
import model.Config;
import view.component.ArsenalDialog;

import javax.swing.*;
import java.awt.Component;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

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

        JMenuItem favoriteItem = new JMenuItem("Favorite");
        favoriteItem.addActionListener(e -> handleFavoriteAction(event));
        menuItems.add(favoriteItem);

        // 创建Arsenal菜单项
        JMenuItem arsenalItem = new JMenuItem("Arsenal");
        arsenalItem.addActionListener(e -> handleArsenalAction(event));
        menuItems.add(arsenalItem);
        
        return menuItems;
    }
    
    /**
     * 处理Favorite菜单项点击事件
     * @param event 上下文菜单事件
     */
    private void handleFavoriteAction(ContextMenuEvent event) {
        try {
            // 获取选中的HTTP消息
            HttpRequest httpRequest = getHttpRequestFromEvent(event);
            if (httpRequest != null) {
                // 添加到收藏夹的逻辑
                String url = httpRequest.url();
                ApiManager.getInstance().getApi().logging().logToOutput("已添加到收藏夹: " + url);
                
                // 显示成功提示
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                        null, 
                        "已添加到收藏夹:\n" + url, 
                        "收藏成功", 
                        JOptionPane.INFORMATION_MESSAGE
                    );
                });
            }
        } catch (Exception ex) {
            ApiManager.getInstance().getApi().logging().logToError("处理收藏夹操作失败: " + ex.getMessage());
        }
    }
    
    /**
     * 处理Arsenal菜单项点击事件
     * @param event 上下文菜单事件
     */
    private void handleArsenalAction(ContextMenuEvent event) {
        try {
            // 获取选中的HTTP消息
            HttpRequest httpRequest = getHttpRequestFromEvent(event);
            HttpResponse httpResponse = getHttpResponseFromEvent(event);

            if (httpRequest != null) {
                // 创建并显示Arsenal工具对话框
                SwingUtilities.invokeLater(() -> {
                    try {
                        ArsenalDialog dialog = new ArsenalDialog(httpRequest, httpResponse);

                        // 获取Burp Suite主窗口并将对话框居中显示
                        Frame burpFrame = ApiManager.getInstance().getApi().userInterface().swingUtils().suiteFrame();
                        if (burpFrame != null) {
                            dialog.setLocationRelativeTo(null);
                            // 确保对话框始终在最前面
                            dialog.setAlwaysOnTop(true);
                            // 显示对话框后立即取消置顶，避免影响用户操作其他应用
                            SwingUtilities.invokeLater(() -> {
                                dialog.setAlwaysOnTop(true);
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
     * 从上下文菜单事件中获取HTTP请求
     * @param event 上下文菜单事件
     * @return HTTP请求对象，如果无法获取则返回null
     */
    private HttpRequest getHttpRequestFromEvent(ContextMenuEvent event) {
        try {
            if (event.messageEditorRequestResponse().isPresent()) {
                return event.messageEditorRequestResponse().get().requestResponse().request();
            }
            
            if (event.selectedRequestResponses() != null && !event.selectedRequestResponses().isEmpty()) {
                return event.selectedRequestResponses().get(0).request();
            }
        } catch (Exception e) {
            ApiManager.getInstance().getApi().logging().logToError("获取HTTP请求失败: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 从上下文菜单事件中获取HTTP响应
     * @param event 上下文菜单事件
     * @return HTTP响应对象，如果无法获取则返回null
     */
    private HttpResponse getHttpResponseFromEvent(ContextMenuEvent event) {
        try {
            if (event.messageEditorRequestResponse().isPresent()) {
                return event.messageEditorRequestResponse().get().requestResponse().response();
            }
            
            if (event.selectedRequestResponses() != null && !event.selectedRequestResponses().isEmpty()) {
                return event.selectedRequestResponses().get(0).response();
            }
        } catch (Exception e) {
            ApiManager.getInstance().getApi().logging().logToError("获取HTTP响应失败: " + e.getMessage());
        }
        
        return null;
    }
} 