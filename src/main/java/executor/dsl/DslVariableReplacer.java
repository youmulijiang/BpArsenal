package executor.dsl;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DSL变量替换器
 * 统一处理命令模板中的变量替换，支持函数调用和链式表达式
 * 
 * 变量语法：%expression%
 * - 链式访问: %http.request.headers.cookies.token%
 * - 函数调用: %hash(http.request.body, "sha256")%
 * - 嵌套: %base64(json(http.request.body, "$.data"), "encode")%
 */
public class DslVariableReplacer {
    
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("%([^%]+)%");
    private final DslExpressionParser parser;
    
    public DslVariableReplacer() {
        this.parser = new DslExpressionParser();
    }
    
    /**
     * 替换命令中的所有变量
     * @param command 命令模板
     * @param montoyaRequest Montoya HTTP请求
     * @param montoyaResponse Montoya HTTP响应（可选）
     * @return 替换后的命令
     */
    public String replace(String command, HttpRequest montoyaRequest, HttpResponse montoyaResponse) {
        if (command == null || command.isEmpty()) {
            return command;
        }
        
        HttpContext context = HttpContext.from(montoyaRequest, montoyaResponse);
        return replace(command, context);
    }
    
    /**
     * 替换命令中的所有变量（支持批量请求）
     * @param command 命令模板
     * @param montoyaRequests Montoya HTTP请求列表
     * @param montoyaResponses Montoya HTTP响应列表（可选）
     * @return 替换后的命令
     */
    public String replaceWithList(String command, List<HttpRequest> montoyaRequests, List<HttpResponse> montoyaResponses) {
        if (command == null || command.isEmpty()) {
            return command;
        }
        
        HttpContext context = HttpContext.fromList(montoyaRequests, montoyaResponses);
        return replace(command, context);
    }
    
    /**
     * 核心替换逻辑
     * @param command 命令模板
     * @param context HTTP上下文
     * @return 替换后的命令
     */
    public String replace(String command, HttpContext context) {
        if (command == null || command.isEmpty()) {
            return command;
        }
        
        Matcher matcher = VARIABLE_PATTERN.matcher(command);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String expression = matcher.group(1);
            
            try {
                // 使用DSL解析器求值
                Object value = parser.evaluate(expression, context);
                String replacement = formatValue(value);
                
                // 转义特殊字符以避免正则表达式问题
                replacement = escapeForReplacement(replacement);
                matcher.appendReplacement(result, replacement);
            } catch (Exception e) {
                // 解析失败，保留原样或替换为错误信息
                String errorMsg = "[DSL Error: " + e.getMessage() + "]";
                matcher.appendReplacement(result, Matcher.quoteReplacement(errorMsg));
            }
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    /**
     * 格式化值为字符串
     * 处理List等复杂类型
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "";
        }
        
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            // 将列表转换为换行分隔的字符串
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append("\n");
                sb.append(list.get(i) != null ? list.get(i).toString() : "");
            }
            return sb.toString();
        }
        
        return value.toString();
    }
    
    /**
     * 转义字符串以用于正则表达式替换
     */
    private String escapeForReplacement(String input) {
        if (input == null) {
            return "";
        }
        // 转义反斜杠和美元符号
        return input.replace("\\", "\\\\").replace("$", "\\$");
    }
    
    /**
     * 验证命令中的变量（用于调试）
     * @param command 命令模板
     * @return 找到的变量列表
     */
    public java.util.List<String> findVariables(String command) {
        java.util.List<String> variables = new java.util.ArrayList<>();
        
        if (command == null || command.isEmpty()) {
            return variables;
        }
        
        Matcher matcher = VARIABLE_PATTERN.matcher(command);
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        
        return variables;
    }
}

