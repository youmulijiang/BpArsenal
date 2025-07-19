package executor;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import java.util.Map;

/**
 * HTTP报文解析器接口
 * 定义策略模式，用于解析不同类型的HTTP数据
 */
public interface HttpMessageParser {
    
    /**
     * 解析HTTP请求，提取占位符变量
     * @param httpRequest HTTP请求对象
     * @return 占位符变量映射
     */
    Map<String, String> parseRequest(HttpRequest httpRequest);
    
    /**
     * 解析HTTP响应，提取占位符变量
     * @param httpResponse HTTP响应对象
     * @return 占位符变量映射
     */
    Map<String, String> parseResponse(HttpResponse httpResponse);
    
    /**
     * 获取解析器类型
     * @return 解析器类型名称
     */
    String getParserType();
} 