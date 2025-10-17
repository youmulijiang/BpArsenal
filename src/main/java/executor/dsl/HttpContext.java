package executor.dsl;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import java.util.List;

/**
 * HTTP上下文对象
 * 封装HTTP请求、响应和批量请求列表，提供统一的数据访问接口
 * 
 * 使用建造者模式构建，支持链式调用
 */
public class HttpContext {
    private HttpRequestContext request;
    private HttpResponseContext response;
    private HttpListContext httpList;
    
    private HttpContext() {
    }
    
    /**
     * 从Montoya API对象构建HTTP上下文
     * @param montoyaRequest Montoya HTTP请求对象
     * @param montoyaResponse Montoya HTTP响应对象（可选）
     * @return HTTP上下文对象
     */
    public static HttpContext from(HttpRequest montoyaRequest, HttpResponse montoyaResponse) {
        HttpContext context = new HttpContext();
        if (montoyaRequest != null) {
            context.request = HttpRequestContext.from(montoyaRequest);
        }
        if (montoyaResponse != null) {
            context.response = HttpResponseContext.from(montoyaResponse);
        }
        return context;
    }
    
    /**
     * 从批量请求构建HTTP上下文
     * @param montoyaRequests Montoya HTTP请求列表
     * @param montoyaResponses Montoya HTTP响应列表（可选）
     * @return HTTP上下文对象
     */
    public static HttpContext fromList(List<HttpRequest> montoyaRequests, List<HttpResponse> montoyaResponses) {
        HttpContext context = new HttpContext();
        
        // 设置主请求（第一个请求）
        if (montoyaRequests != null && !montoyaRequests.isEmpty()) {
            context.request = HttpRequestContext.from(montoyaRequests.get(0));
            if (montoyaResponses != null && !montoyaResponses.isEmpty()) {
                context.response = HttpResponseContext.from(montoyaResponses.get(0));
            }
        }
        
        // 设置批量请求列表
        if (montoyaRequests != null && !montoyaRequests.isEmpty()) {
            context.httpList = HttpListContext.from(montoyaRequests, montoyaResponses);
        }
        
        return context;
    }
    
    // Getters
    public HttpRequestContext getRequest() {
        return request;
    }
    
    public HttpResponseContext getResponse() {
        return response;
    }
    
    public HttpListContext getHttpList() {
        return httpList;
    }
    
    // Setters (用于构建器模式)
    public void setRequest(HttpRequestContext request) {
        this.request = request;
    }
    
    public void setResponse(HttpResponseContext response) {
        this.response = response;
    }
    
    public void setHttpList(HttpListContext httpList) {
        this.httpList = httpList;
    }
    
    /**
     * 获取属性（用于反射和动态访问）
     * @param propertyName 属性名称
     * @return 属性值
     */
    public Object getProperty(String propertyName) {
        switch (propertyName.toLowerCase()) {
            case "request":
                return request;
            case "response":
                return response;
            case "httplist":
                return httpList;
            default:
                return null;
        }
    }
}

