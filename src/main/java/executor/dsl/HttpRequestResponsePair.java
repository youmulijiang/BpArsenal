package executor.dsl;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;

/**
 * HTTP请求-响应对
 * 封装单个HTTP请求和对应的响应
 */
public class HttpRequestResponsePair {
    private HttpRequestContext request;
    private HttpResponseContext response;
    
    private HttpRequestResponsePair() {
    }
    
    /**
     * 从Montoya API对象构建请求-响应对
     */
    public static HttpRequestResponsePair from(HttpRequest montoyaRequest, HttpResponse montoyaResponse) {
        HttpRequestResponsePair pair = new HttpRequestResponsePair();
        pair.request = HttpRequestContext.from(montoyaRequest);
        pair.response = HttpResponseContext.from(montoyaResponse);
        return pair;
    }
    
    // Getters
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
            case "request": return request;
            case "response": return response;
            default: return null;
        }
    }
}

