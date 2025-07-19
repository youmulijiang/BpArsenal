package executor;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import java.util.HashMap;
import java.util.Map;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * 基础HTTP解析器实现
 * 提供标准的HTTP报文解析功能，使用规范化的占位符命名体系
 */
public class BasicHttpParser implements HttpMessageParser {
    
    @Override
    public Map<String, String> parseRequest(HttpRequest httpRequest) {
        Map<String, String> variables = new HashMap<>();
        
        if (httpRequest == null) {
            return variables;
        }
        
        try {
            // === HTTP请求基础信息 ===
            variables.put("http.request.url", httpRequest.url());
            variables.put("http.request.method", httpRequest.method());
            variables.put("http.request.path", httpRequest.path());
            
            // 从URL中提取查询字符串
            String url = httpRequest.url();
            String query = "";
            if (url.contains("?")) {
                query = url.substring(url.indexOf("?") + 1);
            }
            variables.put("http.request.query", query);
            
            // === HTTP请求服务信息 ===
            variables.put("http.request.host", httpRequest.httpService().host());
            variables.put("http.request.port", String.valueOf(httpRequest.httpService().port()));
            variables.put("http.request.secure", String.valueOf(httpRequest.httpService().secure()));
            variables.put("http.request.protocol", httpRequest.httpService().secure() ? "https" : "http");
            
            // === HTTP请求头部信息 ===
            parseRequestHeaders(httpRequest.headers(), variables);
            
            // === HTTP请求参数 ===
            parseRequestParameters(httpRequest.parameters(), variables);
            
            // === HTTP请求体信息 ===
            parseRequestBody(httpRequest, variables);
            
        } catch (Exception e) {
            variables.put("http.request.parse.error", e.getMessage());
        }
        
        return variables;
    }
    
    @Override
    public Map<String, String> parseResponse(HttpResponse httpResponse) {
        Map<String, String> variables = new HashMap<>();
        
        if (httpResponse == null) {
            return variables;
        }
        
        try {
            // === HTTP响应状态信息 ===
            variables.put("http.response.status", String.valueOf(httpResponse.statusCode()));
            variables.put("http.response.reason", httpResponse.reasonPhrase() != null ? httpResponse.reasonPhrase() : "");
            
            // === HTTP响应头部信息 ===
            parseResponseHeaders(httpResponse.headers(), variables);
            
            // === HTTP响应体信息 ===
            parseResponseBody(httpResponse, variables);
            
        } catch (Exception e) {
            variables.put("http.response.parse.error", e.getMessage());
        }
        
        return variables;
    }
    
    @Override
    public String getParserType() {
        return "BasicHttpParser";
    }
    
    /**
     * 解析HTTP请求头部信息
     * @param headers HTTP头部列表
     * @param variables 变量映射
     */
    private void parseRequestHeaders(java.util.List<HttpHeader> headers, Map<String, String> variables) {
        if (headers == null) return;
        
        for (HttpHeader header : headers) {
            String headerName = header.name().toLowerCase().replace("-", ".");
            String key = "http.request.headers." + headerName;
            variables.put(key, header.value());
        }
        
        // 特殊处理Cookie头部
        String cookieHeader = getHeaderValue(headers, "Cookie");
        if (cookieHeader != null && !cookieHeader.isEmpty()) {
            variables.put("http.request.headers.cookies", cookieHeader);
            
            // 解析每个Cookie值
            String[] cookies = cookieHeader.split(";");
            for (String cookie : cookies) {
                String[] parts = cookie.trim().split("=", 2);
                if (parts.length == 2) {
                    String cookieName = parts[0].trim().toLowerCase();
                    String cookieValue = parts[1].trim();
                    variables.put("http.request.cookies." + cookieName, cookieValue);
                }
            }
        }
    }
    
    /**
     * 解析HTTP响应头部信息
     * @param headers HTTP头部列表
     * @param variables 变量映射
     */
    private void parseResponseHeaders(java.util.List<HttpHeader> headers, Map<String, String> variables) {
        if (headers == null) return;
        
        for (HttpHeader header : headers) {
            String headerName = header.name().toLowerCase().replace("-", ".");
            String key = "http.response.headers." + headerName;
            variables.put(key, header.value());
        }
        
        // 特殊处理Set-Cookie头部
        for (HttpHeader header : headers) {
            if (header.name().equalsIgnoreCase("Set-Cookie")) {
                String setCookieValue = header.value();
                // 提取Cookie名称
                String[] parts = setCookieValue.split("=", 2);
                if (parts.length >= 1) {
                    String cookieName = parts[0].trim().toLowerCase();
                    variables.put("http.response.cookies." + cookieName, setCookieValue);
                }
            }
        }
    }
    
    /**
     * 解析HTTP请求参数
     * @param parameters HTTP参数列表
     * @param variables 变量映射
     */
    private void parseRequestParameters(java.util.List<ParsedHttpParameter> parameters, Map<String, String> variables) {
        if (parameters == null) return;
        
        int urlParamCount = 0;
        int bodyParamCount = 0;
        int cookieParamCount = 0;
        
        for (ParsedHttpParameter param : parameters) {
            String paramName = param.name().toLowerCase();
            String paramValue = param.value();
            String paramType = param.type().toString().toLowerCase();
            
            // 根据参数类型分类存储
            switch (paramType) {
                case "url":
                    variables.put("http.request.params.url." + paramName, paramValue);
                    urlParamCount++;
                    break;
                case "body":
                    variables.put("http.request.params.body." + paramName, paramValue);
                    bodyParamCount++;
                    break;
                case "cookie":
                    variables.put("http.request.params.cookie." + paramName, paramValue);
                    cookieParamCount++;
                    break;
                default:
                    variables.put("http.request.params." + paramType + "." + paramName, paramValue);
                    break;
            }
        }
        
        // 参数统计信息
        variables.put("http.request.params.url.count", String.valueOf(urlParamCount));
        variables.put("http.request.params.body.count", String.valueOf(bodyParamCount));
        variables.put("http.request.params.cookie.count", String.valueOf(cookieParamCount));
        variables.put("http.request.params.total.count", String.valueOf(parameters.size()));
    }
    
    /**
     * 解析HTTP请求体信息
     * @param httpRequest HTTP请求对象
     * @param variables 变量映射
     */
    private void parseRequestBody(HttpRequest httpRequest, Map<String, String> variables) {
        String body = httpRequest.bodyToString();
        
        // 基础请求体信息
        variables.put("http.request.body", body != null ? body : "");
        variables.put("http.request.body.len", String.valueOf(body != null ? body.length() : 0));
        variables.put("http.request.body.empty", String.valueOf(body == null || body.isEmpty()));
        
        if (body != null && !body.isEmpty()) {
            // 请求体类型检测
            detectRequestBodyType(body, variables);
            
            // 请求体编码检测
            detectRequestBodyEncoding(body, variables);
        }
    }
    
    /**
     * 解析HTTP响应体信息
     * @param httpResponse HTTP响应对象
     * @param variables 变量映射
     */
    private void parseResponseBody(HttpResponse httpResponse, Map<String, String> variables) {
        String body = httpResponse.bodyToString();
        
        // 基础响应体信息
        variables.put("http.response.body", body != null ? body : "");
        variables.put("http.response.body.len", String.valueOf(body != null ? body.length() : 0));
        variables.put("http.response.body.empty", String.valueOf(body == null || body.isEmpty()));
        
        if (body != null && !body.isEmpty()) {
            // 响应体类型检测
            detectResponseBodyType(body, variables);
            
            // 响应体编码检测
            detectResponseBodyEncoding(body, variables);
        }
    }
    
    /**
     * 检测请求体类型
     * @param body 请求体内容
     * @param variables 变量映射
     */
    private void detectRequestBodyType(String body, Map<String, String> variables) {
        String trimmedBody = body.trim();
        
        if (trimmedBody.startsWith("{") && trimmedBody.endsWith("}")) {
            variables.put("http.request.body.type", "json");
            variables.put("http.request.body.format.json", "true");
        } else if (trimmedBody.startsWith("<") && trimmedBody.endsWith(">")) {
            variables.put("http.request.body.type", "xml");
            variables.put("http.request.body.format.xml", "true");
        } else if (body.contains("=") && body.contains("&")) {
            variables.put("http.request.body.type", "form");
            variables.put("http.request.body.format.form", "true");
        } else if (trimmedBody.startsWith("[") && trimmedBody.endsWith("]")) {
            variables.put("http.request.body.type", "json_array");
            variables.put("http.request.body.format.json", "true");
        } else {
            variables.put("http.request.body.type", "text");
            variables.put("http.request.body.format.text", "true");
        }
    }
    
    /**
     * 检测响应体类型
     * @param body 响应体内容
     * @param variables 变量映射
     */
    private void detectResponseBodyType(String body, Map<String, String> variables) {
        String trimmedBody = body.trim();
        
        if (trimmedBody.startsWith("{") && trimmedBody.endsWith("}")) {
            variables.put("http.response.body.type", "json");
            variables.put("http.response.body.format.json", "true");
        } else if (trimmedBody.startsWith("<") && trimmedBody.endsWith(">")) {
            if (trimmedBody.toLowerCase().contains("<html")) {
                variables.put("http.response.body.type", "html");
                variables.put("http.response.body.format.html", "true");
            } else {
                variables.put("http.response.body.type", "xml");
                variables.put("http.response.body.format.xml", "true");
            }
        } else if (trimmedBody.startsWith("[") && trimmedBody.endsWith("]")) {
            variables.put("http.response.body.type", "json_array");
            variables.put("http.response.body.format.json", "true");
        } else {
            variables.put("http.response.body.type", "text");
            variables.put("http.response.body.format.text", "true");
        }
    }
    
    /**
     * 检测请求体编码
     * @param body 请求体内容
     * @param variables 变量映射
     */
    private void detectRequestBodyEncoding(String body, Map<String, String> variables) {
        // Base64检测
        if (isBase64(body)) {
            variables.put("http.request.body.encoding.base64", "true");
            try {
                String decoded = new String(java.util.Base64.getDecoder().decode(body), StandardCharsets.UTF_8);
                variables.put("http.request.body.base64.decoded", decoded);
                variables.put("http.request.body.base64.decoded.len", String.valueOf(decoded.length()));
            } catch (Exception e) {
                variables.put("http.request.body.base64.error", e.getMessage());
            }
        }
        
        // URL编码检测
        if (body.contains("%")) {
            variables.put("http.request.body.encoding.url", "true");
            try {
                String decoded = URLDecoder.decode(body, StandardCharsets.UTF_8.toString());
                variables.put("http.request.body.url.decoded", decoded);
                variables.put("http.request.body.url.decoded.len", String.valueOf(decoded.length()));
            } catch (Exception e) {
                variables.put("http.request.body.url.error", e.getMessage());
            }
        }
    }
    
    /**
     * 检测响应体编码
     * @param body 响应体内容
     * @param variables 变量映射
     */
    private void detectResponseBodyEncoding(String body, Map<String, String> variables) {
        // Base64检测
        if (isBase64(body)) {
            variables.put("http.response.body.encoding.base64", "true");
            try {
                String decoded = new String(java.util.Base64.getDecoder().decode(body), StandardCharsets.UTF_8);
                variables.put("http.response.body.base64.decoded", decoded);
                variables.put("http.response.body.base64.decoded.len", String.valueOf(decoded.length()));
            } catch (Exception e) {
                variables.put("http.response.body.base64.error", e.getMessage());
            }
        }
    }
    
    /**
     * 检测是否为Base64编码
     * @param input 输入字符串
     * @return 是否为Base64编码
     */
    private boolean isBase64(String input) {
        if (input == null || input.length() % 4 != 0) return false;
        return input.matches("^[A-Za-z0-9+/]*={0,2}$");
    }
    
    /**
     * 获取指定名称的头部值
     * @param headers 头部列表
     * @param headerName 头部名称
     * @return 头部值，如果不存在返回空字符串
     */
    private String getHeaderValue(java.util.List<HttpHeader> headers, String headerName) {
        if (headers == null) return "";
        
        for (HttpHeader header : headers) {
            if (header.name().equalsIgnoreCase(headerName)) {
                return header.value();
            }
        }
        return "";
    }
} 