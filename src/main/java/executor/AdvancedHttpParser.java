package executor;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 高级HTTP解析器实现
 * 提供扩展的HTTP报文解析功能，包括编码转换、正则提取等
 */
public class AdvancedHttpParser implements HttpMessageParser {
    
    private final BasicHttpParser basicParser = new BasicHttpParser();
    
    @Override
    public Map<String, String> parseRequest(HttpRequest httpRequest) {
        // 先获取基础解析结果
        Map<String, String> variables = basicParser.parseRequest(httpRequest);
        
        if (httpRequest == null) {
            return variables;
        }
        
        try {
            // URL编码/解码处理
            addEncodingVariables(variables);
            
            // 提取文件信息
            extractFileInfo(httpRequest.path(), variables);
            
                         // 提取查询参数的特殊格式
             String url = httpRequest.url();
             String query = "";
             if (url.contains("?")) {
                 query = url.substring(url.indexOf("?") + 1);
             }
             extractQueryDetails(query, variables);
            
            // JSON/XML/Form数据特殊处理
            analyzeRequestBody(httpRequest.bodyToString(), variables);
            
            // 提取认证信息
            extractAuthInfo(variables);
            
            // 计算请求特征
            calculateRequestFingerprint(httpRequest, variables);
            
        } catch (Exception e) {
            variables.put("advanced.parse.error", e.getMessage());
        }
        
        return variables;
    }
    
    @Override
    public Map<String, String> parseResponse(HttpResponse httpResponse) {
        // 先获取基础解析结果
        Map<String, String> variables = basicParser.parseResponse(httpResponse);
        
        if (httpResponse == null) {
            return variables;
        }
        
        try {
            // 分析响应内容类型
            analyzeResponseContent(httpResponse, variables);
            
            // 提取安全头部
            extractSecurityHeaders(httpResponse, variables);
            
            // 计算响应特征
            calculateResponseFingerprint(httpResponse, variables);
            
        } catch (Exception e) {
            variables.put("advanced.parse.error", e.getMessage());
        }
        
        return variables;
    }
    
    @Override
    public String getParserType() {
        return "AdvancedHttpParser";
    }
    
    /**
     * 添加编码相关的变量
     * @param variables 变量映射
     */
    private void addEncodingVariables(Map<String, String> variables) throws UnsupportedEncodingException {
        String url = variables.get("http.url");
        String path = variables.get("http.path");
        String query = variables.get("http.query");
        
                 if (url != null) {
             variables.put("http.url.encoded", URLEncoder.encode(url, StandardCharsets.UTF_8.toString()));
             try {
                 variables.put("http.url.decoded", URLDecoder.decode(url, StandardCharsets.UTF_8.toString()));
             } catch (Exception e) {
                 variables.put("http.url.decoded", url);
             }
         }
         
         if (path != null) {
             variables.put("http.path.encoded", URLEncoder.encode(path, StandardCharsets.UTF_8.toString()));
             try {
                 variables.put("http.path.decoded", URLDecoder.decode(path, StandardCharsets.UTF_8.toString()));
             } catch (Exception e) {
                 variables.put("http.path.decoded", path);
             }
         }
         
         if (query != null && !query.isEmpty()) {
             variables.put("http.query.encoded", URLEncoder.encode(query, StandardCharsets.UTF_8.toString()));
             try {
                 variables.put("http.query.decoded", URLDecoder.decode(query, StandardCharsets.UTF_8.toString()));
             } catch (Exception e) {
                 variables.put("http.query.decoded", query);
             }
         }
    }
    
    /**
     * 提取文件信息
     * @param path 请求路径
     * @param variables 变量映射
     */
    private void extractFileInfo(String path, Map<String, String> variables) {
        if (path == null || path.isEmpty()) return;
        
        // 提取文件名
        String fileName = "";
        String fileExt = "";
        
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < path.length() - 1) {
            fileName = path.substring(lastSlash + 1);
        }
        
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            fileExt = fileName.substring(lastDot + 1);
            fileName = fileName.substring(0, lastDot);
        }
        
        variables.put("file.name", fileName);
        variables.put("file.extension", fileExt);
        variables.put("file.full", fileName + (fileExt.isEmpty() ? "" : "." + fileExt));
        
        // 提取目录路径
        if (lastSlash > 0) {
            variables.put("path.directory", path.substring(0, lastSlash));
        } else {
            variables.put("path.directory", "/");
        }
    }
    
    /**
     * 提取查询参数详细信息
     * @param query 查询字符串
     * @param variables 变量映射
     */
    private void extractQueryDetails(String query, Map<String, String> variables) {
        if (query == null || query.isEmpty()) return;
        
        String[] params = query.split("&");
        variables.put("query.param.count", String.valueOf(params.length));
        
        StringBuilder paramNames = new StringBuilder();
        for (String param : params) {
            String[] parts = param.split("=", 2);
            if (parts.length > 0) {
                if (paramNames.length() > 0) paramNames.append(",");
                paramNames.append(parts[0]);
            }
        }
        variables.put("query.param.names", paramNames.toString());
    }
    
    /**
     * 分析请求体内容
     * @param body 请求体内容
     * @param variables 变量映射
     */
    private void analyzeRequestBody(String body, Map<String, String> variables) {
        if (body == null || body.isEmpty()) return;
        
        // 检测内容类型
        if (body.trim().startsWith("{") && body.trim().endsWith("}")) {
            variables.put("body.type", "json");
            extractJsonFields(body, variables, "body.json");
        } else if (body.trim().startsWith("<") && body.trim().endsWith(">")) {
            variables.put("body.type", "xml");
        } else if (body.contains("=") && body.contains("&")) {
            variables.put("body.type", "form");
            extractFormFields(body, variables);
        } else {
            variables.put("body.type", "text");
        }
        
        // Base64检测
        if (isBase64(body)) {
            try {
                String decoded = new String(Base64.getDecoder().decode(body), StandardCharsets.UTF_8);
                variables.put("body.base64.decoded", decoded);
            } catch (Exception e) {
                // 忽略解码错误
            }
        }
    }
    
    /**
     * 提取JSON字段（简单实现）
     * @param json JSON字符串
     * @param variables 变量映射
     * @param prefix 前缀
     */
    private void extractJsonFields(String json, Map<String, String> variables, String prefix) {
        // 简单的JSON字段提取（可以后续使用Gson替换）
        Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        
        int fieldCount = 0;
        while (matcher.find()) {
            String fieldName = matcher.group(1);
            String fieldValue = matcher.group(2);
            variables.put(prefix + "." + fieldName, fieldValue);
            fieldCount++;
        }
        variables.put(prefix + ".field.count", String.valueOf(fieldCount));
    }
    
    /**
     * 提取表单字段
     * @param formData 表单数据
     * @param variables 变量映射
     */
    private void extractFormFields(String formData, Map<String, String> variables) {
        String[] fields = formData.split("&");
        variables.put("body.form.field.count", String.valueOf(fields.length));
        
        for (String field : fields) {
            String[] parts = field.split("=", 2);
            if (parts.length == 2) {
                                 try {
                     String name = URLDecoder.decode(parts[0], StandardCharsets.UTF_8.toString());
                     String value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8.toString());
                    variables.put("body.form." + name, value);
                } catch (Exception e) {
                    variables.put("body.form." + parts[0], parts[1]);
                }
            }
        }
    }
    
    /**
     * 提取认证信息
     * @param variables 变量映射
     */
    private void extractAuthInfo(Map<String, String> variables) {
        String authHeader = variables.get("request.authorization");
        if (authHeader != null && !authHeader.isEmpty()) {
            if (authHeader.startsWith("Basic ")) {
                variables.put("auth.type", "basic");
                try {
                    String decoded = new String(Base64.getDecoder().decode(authHeader.substring(6)), StandardCharsets.UTF_8);
                    String[] parts = decoded.split(":", 2);
                    if (parts.length == 2) {
                        variables.put("auth.username", parts[0]);
                        variables.put("auth.password", parts[1]);
                    }
                } catch (Exception e) {
                    // 忽略解码错误
                }
            } else if (authHeader.startsWith("Bearer ")) {
                variables.put("auth.type", "bearer");
                variables.put("auth.token", authHeader.substring(7));
            }
        }
    }
    
    /**
     * 计算请求指纹
     * @param httpRequest HTTP请求
     * @param variables 变量映射
     */
    private void calculateRequestFingerprint(HttpRequest httpRequest, Map<String, String> variables) {
        // 简单的指纹计算
        String fingerprint = httpRequest.method() + ":" + httpRequest.path() + ":" + 
                           variables.get("request.user-agent");
        variables.put("request.fingerprint", String.valueOf(fingerprint.hashCode()));
    }
    
    /**
     * 分析响应内容
     * @param httpResponse HTTP响应
     * @param variables 变量映射
     */
    private void analyzeResponseContent(HttpResponse httpResponse, Map<String, String> variables) {
        String body = httpResponse.bodyToString();
        if (body != null && !body.isEmpty()) {
            // 检测响应类型
            String contentType = variables.get("response.content-type");
            if (contentType != null) {
                if (contentType.contains("json")) {
                    variables.put("response.format", "json");
                } else if (contentType.contains("xml")) {
                    variables.put("response.format", "xml");
                } else if (contentType.contains("html")) {
                    variables.put("response.format", "html");
                    extractHtmlInfo(body, variables);
                }
            }
        }
    }
    
    /**
     * 提取HTML信息
     * @param html HTML内容
     * @param variables 变量映射
     */
    private void extractHtmlInfo(String html, Map<String, String> variables) {
        // 提取标题
        Pattern titlePattern = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE);
        Matcher titleMatcher = titlePattern.matcher(html);
        if (titleMatcher.find()) {
            variables.put("response.html.title", titleMatcher.group(1).trim());
        }
        
        // 统计表单数量
        Pattern formPattern = Pattern.compile("<form", Pattern.CASE_INSENSITIVE);
        Matcher formMatcher = formPattern.matcher(html);
        int formCount = 0;
        while (formMatcher.find()) {
            formCount++;
        }
        variables.put("response.html.form.count", String.valueOf(formCount));
    }
    
    /**
     * 提取安全头部
     * @param httpResponse HTTP响应
     * @param variables 变量映射
     */
    private void extractSecurityHeaders(HttpResponse httpResponse, Map<String, String> variables) {
        String[] securityHeaders = {
            "X-Frame-Options", "X-XSS-Protection", "X-Content-Type-Options",
            "Strict-Transport-Security", "Content-Security-Policy"
        };
        
        for (String headerName : securityHeaders) {
            String value = getHeaderValue(httpResponse.headers(), headerName);
            if (value != null && !value.isEmpty()) {
                variables.put("security.header." + headerName.toLowerCase().replace("-", "_"), value);
            }
        }
    }
    
    /**
     * 计算响应指纹
     * @param httpResponse HTTP响应
     * @param variables 变量映射
     */
    private void calculateResponseFingerprint(HttpResponse httpResponse, Map<String, String> variables) {
        String fingerprint = httpResponse.statusCode() + ":" + 
                           variables.get("response.content-type") + ":" +
                           variables.get("response.body.length");
        variables.put("response.fingerprint", String.valueOf(fingerprint.hashCode()));
    }
    
    /**
     * 检测是否为Base64编码
     * @param input 输入字符串
     * @return 是否为Base64编码
     */
    private boolean isBase64(String input) {
        if (input == null || input.length() % 4 != 0) return false;
        Pattern base64Pattern = Pattern.compile("^[A-Za-z0-9+/]*={0,2}$");
        return base64Pattern.matcher(input).matches();
    }
    
    /**
     * 获取指定名称的头部值
     * @param headers 头部列表
     * @param headerName 头部名称
     * @return 头部值
     */
    private String getHeaderValue(java.util.List<burp.api.montoya.http.message.HttpHeader> headers, String headerName) {
        if (headers == null) return "";
        
        for (burp.api.montoya.http.message.HttpHeader header : headers) {
            if (header.name().equalsIgnoreCase(headerName)) {
                return header.value();
            }
        }
        return "";
    }
} 