package util;

import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import manager.ApiManager;
import java.util.ArrayList;
import java.util.List;

/**
 * 上下文菜单事件处理器
 * 负责从ContextMenuEvent中提取HTTP请求和响应数据
 */
public class ContextMenuEventHandler {
    
    /**
     * 从上下文菜单事件中获取HTTP请求
     * @param event 上下文菜单事件
     * @return HTTP请求对象，如果无法获取则返回null
     */
    public static HttpRequest getHttpRequestFromEvent(ContextMenuEvent event) {
        try {
            // 尝试从消息编辑器获取
            if (event.messageEditorRequestResponse().isPresent()) {
                return event.messageEditorRequestResponse().get().requestResponse().request();
            }
            
            // 尝试从选中的请求响应列表获取第一个
            if (event.selectedRequestResponses() != null && !event.selectedRequestResponses().isEmpty()) {
                return event.selectedRequestResponses().get(0).request();
            }
        } catch (Exception e) {
            logError("获取HTTP请求失败", e);
        }
        
        return null;
    }
    
    /**
     * 从上下文菜单事件中获取HTTP响应
     * @param event 上下文菜单事件
     * @return HTTP响应对象，如果无法获取则返回null
     */
    public static HttpResponse getHttpResponseFromEvent(ContextMenuEvent event) {
        try {
            // 尝试从消息编辑器获取
            if (event.messageEditorRequestResponse().isPresent()) {
                return event.messageEditorRequestResponse().get().requestResponse().response();
            }
            
            // 尝试从选中的请求响应列表获取第一个
            if (event.selectedRequestResponses() != null && !event.selectedRequestResponses().isEmpty()) {
                return event.selectedRequestResponses().get(0).response();
            }
        } catch (Exception e) {
            logError("获取HTTP响应失败", e);
        }
        
        return null;
    }
    
    /**
     * 获取所有选中的HTTP请求
     * @param event 上下文菜单事件
     * @return HTTP请求列表
     */
    public static List<HttpRequest> getAllSelectedRequests(ContextMenuEvent event) {
        List<HttpRequest> requests = new ArrayList<>();
        
        try {
            if (event != null && event.selectedRequestResponses() != null) {
                for (burp.api.montoya.http.message.HttpRequestResponse requestResponse : event.selectedRequestResponses()) {
                    if (requestResponse != null && requestResponse.request() != null) {
                        requests.add(requestResponse.request());
                    }
                }
            }
        } catch (Exception e) {
            logError("获取选中请求列表失败", e);
        }
        
        return requests;
    }
    
    /**
     * 获取所有选中的HTTP响应
     * @param event 上下文菜单事件
     * @return HTTP响应列表
     */
    public static List<HttpResponse> getAllSelectedResponses(ContextMenuEvent event) {
        List<HttpResponse> responses = new ArrayList<>();
        
        try {
            if (event != null && event.selectedRequestResponses() != null) {
                for (burp.api.montoya.http.message.HttpRequestResponse requestResponse : event.selectedRequestResponses()) {
                    if (requestResponse != null && requestResponse.response() != null) {
                        responses.add(requestResponse.response());
                    }
                }
            }
        } catch (Exception e) {
            logError("获取选中响应列表失败", e);
        }
        
        return responses;
    }
    
    /**
     * 检查事件是否有有效的HTTP数据
     * @param event 上下文菜单事件
     * @return true如果有有效数据
     */
    public static boolean hasValidHttpData(ContextMenuEvent event) {
        return getHttpRequestFromEvent(event) != null;
    }
    
    /**
     * 获取选中数据包的数量
     * @param event 上下文菜单事件
     * @return 数据包数量
     */
    public static int getSelectedCount(ContextMenuEvent event) {
        try {
            if (event != null && event.selectedRequestResponses() != null) {
                return event.selectedRequestResponses().size();
            }
        } catch (Exception e) {
            logError("获取选中数量失败", e);
        }
        return 0;
    }
    
    /**
     * 记录错误日志
     * @param message 错误消息
     * @param e 异常对象
     */
    private static void logError(String message, Exception e) {
        if (ApiManager.getInstance().isInitialized()) {
            ApiManager.getInstance().getApi().logging().logToError(message + ": " + e.getMessage());
        }
    }
} 