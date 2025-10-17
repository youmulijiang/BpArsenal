package executor.dsl;

import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.http.message.HttpHeader;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP响应上下文
 * 封装HTTP响应的所有信息
 */
public class HttpResponseContext {
    private int status;
    private String reason;
    private Map<String, String> headers;
    private Map<String, String> cookies;
    private BodyContext body;
    
    private HttpResponseContext() {
        this.headers = new HashMap<>();
        this.cookies = new HashMap<>();
    }
    
    /**
     * 从Montoya API响应对象构建响应上下文
     */
    public static HttpResponseContext from(HttpResponse montoyaResponse) {
        if (montoyaResponse == null) {
            return null;
        }
        
        HttpResponseContext context = new HttpResponseContext();
        
        // 状态信息
        context.status = montoyaResponse.statusCode();
        context.reason = montoyaResponse.reasonPhrase() != null ? montoyaResponse.reasonPhrase() : "";
        
        // 解析Headers
        for (HttpHeader header : montoyaResponse.headers()) {
            String headerName = header.name().toLowerCase().replace("-", ".");
            context.headers.put(headerName, header.value());
            
            // 特殊处理Set-Cookie
            if (header.name().equalsIgnoreCase("Set-Cookie")) {
                String[] parts = header.value().split("=", 2);
                if (parts.length >= 1) {
                    String cookieName = parts[0].trim();
                    context.cookies.put(cookieName, header.value());
                }
            }
        }
        
        // 解析Body
        context.body = BodyContext.from(montoyaResponse.bodyToString());
        
        return context;
    }
    
    // Getters
    public int getStatus() { return status; }
    public String getReason() { return reason; }
    public Map<String, String> getHeaders() { return headers; }
    public Map<String, String> getCookies() { return cookies; }
    public BodyContext getBody() { return body; }
    
    /**
     * 获取属性（用于反射和动态访问）
     */
    public Object getProperty(String propertyName) {
        switch (propertyName.toLowerCase()) {
            case "status": return status;
            case "reason": return reason;
            case "headers": return headers;
            case "cookies": return cookies;
            case "body": return body;
            default: return null;
        }
    }
}

