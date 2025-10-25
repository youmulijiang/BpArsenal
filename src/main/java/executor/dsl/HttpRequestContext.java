package executor.dsl;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.params.ParsedHttpParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP请求上下文
 * 封装HTTP请求的所有信息，提供结构化访问接口
 */
public class HttpRequestContext {
    private String url;
    private String method;
    private String host;
    private int port;
    private String protocol;
    private String path;
    private String query;
    private Map<String, String> headers;
    private Map<String, String> cookies;
    private ParameterContext params;
    private BodyContext body;
    
    private HttpRequestContext() {
        this.headers = new HashMap<>();
        this.cookies = new HashMap<>();
    }
    
    /**
     * 从Montoya API请求对象构建请求上下文
     * @param montoyaRequest Montoya HTTP请求对象
     * @return HTTP请求上下文
     */
    public static HttpRequestContext from(HttpRequest montoyaRequest) {
        if (montoyaRequest == null) {
            return null;
        }
        
        HttpRequestContext context = new HttpRequestContext();
        
        // 基础信息
        context.url = montoyaRequest.url();
        context.method = montoyaRequest.method();
        context.path = montoyaRequest.path();
        
        // 服务信息
        if (montoyaRequest.httpService() != null) {
            context.host = montoyaRequest.httpService().host();
            context.port = montoyaRequest.httpService().port();
            context.protocol = montoyaRequest.httpService().secure() ? "https" : "http";
        }
        
        // 查询字符串
        String urlString = montoyaRequest.url();
        if (urlString != null && urlString.contains("?")) {
            context.query = urlString.substring(urlString.indexOf("?") + 1);
        } else {
            context.query = "";
        }
        
        // 解析Headers
        for (HttpHeader header : montoyaRequest.headers()) {
            String headerName = header.name().toLowerCase().replace("-", ".");
            context.headers.put(headerName, header.value());
        }
        
        // 解析Cookies
        context.cookies = parseCookies(montoyaRequest);
        
        // 解析参数
        context.params = ParameterContext.from(montoyaRequest.parameters());
        
        // 解析Body
        context.body = BodyContext.from(montoyaRequest.bodyToString());
        
        return context;
    }
    
    /**
     * 从请求中提取Cookie
     */
    private static Map<String, String> parseCookies(HttpRequest request) {
        Map<String, String> cookies = new HashMap<>();
        String cookieHeader = request.headerValue("Cookie");
        
        if (cookieHeader != null && !cookieHeader.isEmpty()) {
            for (String cookie : cookieHeader.split(";")) {
                String[] parts = cookie.trim().split("=", 2);
                if (parts.length == 2) {
                    cookies.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
        
        return cookies;
    }
    
    // Getters
    public String getUrl() { return url; }
    public String getMethod() { return method; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getProtocol() { return protocol; }
    public String getPath() { return path; }
    public String getQuery() { return query; }
    public Map<String, String> getHeaders() { return headers; }
    public Map<String, String> getCookies() { return cookies; }
    public ParameterContext getParams() { return params; }
    public BodyContext getBody() { return body; }
    
    /**
     * 获取属性（用于反射和动态访问）
     */
    public Object getProperty(String propertyName) {
        switch (propertyName.toLowerCase()) {
            case "url": return url;
            case "method": return method;
            case "host": return host;
            case "port": return port;
            case "protocol": return protocol;
            case "path": return path;
            case "query": return query;
            case "headers": return headers;
            case "cookies": return cookies;
            case "params": return params;
            case "body": return body;
            default: return null;
        }
    }
}

