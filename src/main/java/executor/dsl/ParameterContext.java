package executor.dsl;

import burp.api.montoya.http.message.params.ParsedHttpParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 参数上下文
 * 封装HTTP请求参数（URL参数、Body参数、Cookie参数）
 */
public class ParameterContext {
    private Map<String, String> url;
    private Map<String, String> body;
    private Map<String, String> cookie;
    
    private ParameterContext() {
        this.url = new HashMap<>();
        this.body = new HashMap<>();
        this.cookie = new HashMap<>();
    }
    
    /**
     * 从Montoya API参数列表构建参数上下文
     */
    public static ParameterContext from(List<ParsedHttpParameter> parameters) {
        ParameterContext context = new ParameterContext();
        
        if (parameters == null) {
            return context;
        }
        
        for (ParsedHttpParameter param : parameters) {
            String paramName = param.name();
            String paramValue = param.value();
            String paramType = param.type().toString().toLowerCase();
            
            switch (paramType) {
                case "url":
                    context.url.put(paramName, paramValue);
                    break;
                case "body":
                    context.body.put(paramName, paramValue);
                    break;
                case "cookie":
                    context.cookie.put(paramName, paramValue);
                    break;
            }
        }
        
        return context;
    }
    
    // Getters
    public Map<String, String> getUrl() { return url; }
    public Map<String, String> getBody() { return body; }
    public Map<String, String> getCookie() { return cookie; }
    
    /**
     * 获取属性（用于反射和动态访问）
     */
    public Object getProperty(String propertyName) {
        switch (propertyName.toLowerCase()) {
            case "url": return url;
            case "body": return body;
            case "cookie": return cookie;
            default: return null;
        }
    }
}

