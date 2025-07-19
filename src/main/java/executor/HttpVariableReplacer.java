package executor;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * HTTP变量替换器
 * 负责将命令中的占位符替换为实际的HTTP数据
 */
public class HttpVariableReplacer {
    
    private final HttpMessageParser parser;
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("%([a-zA-Z0-9._-]+)%");
    
    public HttpVariableReplacer(HttpMessageParser parser) {
        this.parser = parser;
    }
    
    /**
     * 替换命令中的HTTP请求变量
     * @param command 原始命令
     * @param httpRequest HTTP请求对象
     * @return 替换后的命令
     */
    public String replaceRequestVariables(String command, HttpRequest httpRequest) {
        if (command == null || command.isEmpty()) {
            return command;
        }
        
        Map<String, String> variables = parser.parseRequest(httpRequest);
        return replaceVariables(command, variables);
    }
    
    /**
     * 替换命令中的HTTP响应变量
     * @param command 原始命令
     * @param httpResponse HTTP响应对象
     * @return 替换后的命令
     */
    public String replaceResponseVariables(String command, HttpResponse httpResponse) {
        if (command == null || command.isEmpty()) {
            return command;
        }
        
        Map<String, String> variables = parser.parseResponse(httpResponse);
        return replaceVariables(command, variables);
    }
    
    /**
     * 同时替换请求和响应变量
     * @param command 原始命令
     * @param httpRequest HTTP请求对象
     * @param httpResponse HTTP响应对象
     * @return 替换后的命令
     */
    public String replaceAllVariables(String command, HttpRequest httpRequest, HttpResponse httpResponse) {
        if (command == null || command.isEmpty()) {
            return command;
        }
        
        Map<String, String> requestVars = parser.parseRequest(httpRequest);
        Map<String, String> responseVars = parser.parseResponse(httpResponse);
        
        // 合并变量，响应变量覆盖请求变量（如果有重名）
        requestVars.putAll(responseVars);
        
        return replaceVariables(command, requestVars);
    }
    
    /**
     * 替换命令中的变量占位符
     * @param command 原始命令
     * @param variables 变量映射
     * @return 替换后的命令
     */
    private String replaceVariables(String command, Map<String, String> variables) {
        if (variables == null || variables.isEmpty()) {
            return command;
        }
        
        Matcher matcher = VARIABLE_PATTERN.matcher(command);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            String replacement = variables.get(variableName);
            
            if (replacement != null) {
                // 转义特殊字符以避免正则表达式问题
                replacement = escapeForReplacement(replacement);
                matcher.appendReplacement(result, replacement);
            } else {
                // 如果变量不存在，保留原始占位符
                matcher.appendReplacement(result, matcher.group(0));
            }
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * 获取所有可用的变量列表（用于调试和配置验证）
     * @param httpRequest HTTP请求对象
     * @return 变量映射
     */
    public Map<String, String> getAvailableRequestVariables(HttpRequest httpRequest) {
        return parser.parseRequest(httpRequest);
    }
    
    /**
     * 获取所有可用的响应变量列表
     * @param httpResponse HTTP响应对象
     * @return 变量映射
     */
    public Map<String, String> getAvailableResponseVariables(HttpResponse httpResponse) {
        return parser.parseResponse(httpResponse);
    }
    
    /**
     * 验证命令中的变量是否都能被解析
     * @param command 命令字符串
     * @param httpRequest HTTP请求（可选）
     * @param httpResponse HTTP响应（可选）
     * @return 验证结果信息
     */
    public VariableValidationResult validateVariables(String command, HttpRequest httpRequest, HttpResponse httpResponse) {
        VariableValidationResult result = new VariableValidationResult();
        
        if (command == null || command.isEmpty()) {
            result.setValid(true);
            return result;
        }
        
        Map<String, String> variables = parser.parseRequest(httpRequest);
        if (httpResponse != null) {
            variables.putAll(parser.parseResponse(httpResponse));
        }
        
        Matcher matcher = VARIABLE_PATTERN.matcher(command);
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            result.addFoundVariable(variableName);
            
            if (!variables.containsKey(variableName)) {
                result.addMissingVariable(variableName);
            }
        }
        
        result.setValid(result.getMissingVariables().isEmpty());
        return result;
    }
    
    /**
     * 转义字符串以用于正则表达式替换
     * @param input 输入字符串
     * @return 转义后的字符串
     */
    private String escapeForReplacement(String input) {
        if (input == null) return "";
        
        // 转义反斜杠和美元符号
        return input.replace("\\", "\\\\").replace("$", "\\$");
    }
    
    /**
     * 变量验证结果类
     */
    public static class VariableValidationResult {
        private boolean valid;
        private java.util.List<String> foundVariables = new java.util.ArrayList<>();
        private java.util.List<String> missingVariables = new java.util.ArrayList<>();
        
        public boolean isValid() {
            return valid;
        }
        
        public void setValid(boolean valid) {
            this.valid = valid;
        }
        
        public java.util.List<String> getFoundVariables() {
            return foundVariables;
        }
        
        public void addFoundVariable(String variable) {
            this.foundVariables.add(variable);
        }
        
        public java.util.List<String> getMissingVariables() {
            return missingVariables;
        }
        
        public void addMissingVariable(String variable) {
            this.missingVariables.add(variable);
        }
        
        @Override
        public String toString() {
            return "VariableValidationResult{" +
                   "valid=" + valid +
                   ", foundVariables=" + foundVariables +
                   ", missingVariables=" + missingVariables +
                   '}';
        }
    }
} 