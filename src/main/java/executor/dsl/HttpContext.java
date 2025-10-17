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
    
    // 内部包装类，用于支持 http.request.* 语法
    private HttpWrapper http;
    
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
        // 初始化http包装器，支持http.request.*和http.response.*语法
        context.http = new HttpWrapper(context.request, context.response);
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
        
        // 初始化http包装器
        context.http = new HttpWrapper(context.request, context.response);
        
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
     * 获取HTTP包装器（用于支持http.request.*语法）
     * @return HTTP包装器
     */
    public HttpWrapper getHttp() {
        return http;
    }
    
    /**
     * 获取属性（用于反射和动态访问）
     * @param propertyName 属性名称
     * @return 属性值
     */
    public Object getProperty(String propertyName) {
        switch (propertyName.toLowerCase()) {
            case "http":
                return http;
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
    
    /**
     * HTTP包装器内部类
     * 用于支持 http.request.* 和 http.response.* 语法
     */
    public static class HttpWrapper {
        private final HttpRequestContext request;
        private final HttpResponseContext response;
        
        public HttpWrapper(HttpRequestContext request, HttpResponseContext response) {
            this.request = request;
            this.response = response;
        }
        
        public HttpRequestContext getRequest() {
            return request;
        }
        
        public HttpResponseContext getResponse() {
            return response;
        }
        
        /**
         * 获取属性（用于反射和动态访问）
         */
        public Object getProperty(String propertyName) {
            switch (propertyName.toLowerCase()) {
                case "request":
                    return request;
                case "response":
                    return response;
                default:
                    return null;
            }
        }
    }
}

