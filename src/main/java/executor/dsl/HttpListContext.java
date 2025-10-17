package executor.dsl;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HTTP列表上下文
 * 封装批量HTTP请求-响应对，提供聚合操作
 */
public class HttpListContext {
    private List<HttpRequestResponsePair> requests;
    
    private HttpListContext() {
        this.requests = new ArrayList<>();
    }
    
    /**
     * 从Montoya API请求列表构建列表上下文
     */
    public static HttpListContext from(List<HttpRequest> montoyaRequests, List<HttpResponse> montoyaResponses) {
        HttpListContext context = new HttpListContext();
        
        if (montoyaRequests == null) {
            return context;
        }
        
        for (int i = 0; i < montoyaRequests.size(); i++) {
            HttpRequest req = montoyaRequests.get(i);
            HttpResponse resp = (montoyaResponses != null && i < montoyaResponses.size()) 
                              ? montoyaResponses.get(i) 
                              : null;
            context.requests.add(HttpRequestResponsePair.from(req, resp));
        }
        
        return context;
    }
    
    // 基础访问
    public List<HttpRequestResponsePair> getRequests() {
        return requests;
    }
    
    public int getCount() {
        return requests.size();
    }
    
    // 索引访问
    public HttpRequestResponsePair get(int index) {
        if (index >= 0 && index < requests.size()) {
            return requests.get(index);
        }
        return null;
    }
    
    // 语义化访问
    public HttpRequestResponsePair getFirst() {
        return requests.isEmpty() ? null : requests.get(0);
    }
    
    public HttpRequestResponsePair getLast() {
        return requests.isEmpty() ? null : requests.get(requests.size() - 1);
    }
    
    // 聚合方法
    public List<String> getUrls() {
        return requests.stream()
            .map(pair -> pair.getRequest().getUrl())
            .collect(Collectors.toList());
    }
    
    public List<String> getHosts() {
        return requests.stream()
            .map(pair -> pair.getRequest().getHost())
            .distinct()
            .collect(Collectors.toList());
    }
    
    public List<String> getPaths() {
        return requests.stream()
            .map(pair -> pair.getRequest().getPath())
            .collect(Collectors.toList());
    }
    
    public List<String> getMethods() {
        return requests.stream()
            .map(pair -> pair.getRequest().getMethod())
            .distinct()
            .collect(Collectors.toList());
    }
    
    /**
     * 获取属性（用于反射和动态访问）
     */
    public Object getProperty(String propertyName) {
        switch (propertyName.toLowerCase()) {
            case "requests": return requests;
            case "count": return getCount();
            case "first": return getFirst();
            case "last": return getLast();
            case "urls": return getUrls();
            case "hosts": return getHosts();
            case "paths": return getPaths();
            case "methods": return getMethods();
            default: return null;
        }
    }
}

