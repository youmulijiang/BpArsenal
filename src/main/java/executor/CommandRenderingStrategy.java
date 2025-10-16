package executor;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import model.HttpToolCommand;
import manager.ApiManager;
import util.OsUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 命令渲染策略处理器
 * 实现策略模式，处理不同类型的命令渲染逻辑
 */
public class CommandRenderingStrategy {
    
    /**
     * 渲染命令
     * @param toolCommand 工具命令
     * @param primaryRequest 主要HTTP请求
     * @param httpResponse HTTP响应（可选）
     * @param allSelectedRequests 所有选中的HTTP请求
     * @return 渲染后的命令
     */
    public static String renderCommand(HttpToolCommand toolCommand, 
                                     HttpRequest primaryRequest, 
                                     HttpResponse httpResponse,
                                     List<HttpRequest> allSelectedRequests) {
        try {
            String command = toolCommand.getCommand();
            if (command == null || command.isEmpty()) {
                return "";
            }
            
            if (primaryRequest == null) {
                return command;
            }
            
            // 解析主要请求变量
            Map<String, String> allVariables = parseRequestVariables(primaryRequest, httpResponse);
            
            // 处理httpList相关变量（如果有多个请求）
            if (allSelectedRequests != null && !allSelectedRequests.isEmpty()) {
                HttpListVariableProcessor.processHttpListVariables(allVariables, allSelectedRequests);
            }
            
            // 进行变量替换
            return replaceVariables(command, allVariables);
            
        } catch (Exception e) {
            return toolCommand.getCommand();
        }
    }
    
    /**
     * 解析请求变量
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应（可选）
     * @return 变量映射
     */
    private static Map<String, String> parseRequestVariables(HttpRequest httpRequest, HttpResponse httpResponse) {
        Map<String, String> allVariables = new HashMap<>();
        
        try {
            // 使用AdvancedHttpParser解析请求
            AdvancedHttpParser advancedParser = new AdvancedHttpParser();
            Map<String, String> requestVariables = advancedParser.parseRequest(httpRequest);
            allVariables.putAll(requestVariables);
            
            // 解析响应（如果有）
            if (httpResponse != null) {
                Map<String, String> responseVariables = advancedParser.parseResponse(httpResponse);
                allVariables.putAll(responseVariables);
            }
        } catch (Exception e) {
        }
        
        return allVariables;
    }
    
    /**
     * 替换命令中的变量
     * @param command 原始命令
     * @param variables 变量映射
     * @return 替换后的命令
     */
    private static String replaceVariables(String command, Map<String, String> variables) {
        String result = command;
        
        // 按变量名长度排序，优先替换长变量名，避免部分匹配问题
        List<String> sortedKeys = variables.keySet().stream()
                .sorted((a, b) -> b.length() - a.length())
                .collect(Collectors.toList());
        
        for (String key : sortedKeys) {
            String value = variables.get(key);
            if (value != null) {
                String placeholder = "%" + key + "%";
                if (result.contains(placeholder)) {
                    String escapedValue = escapeCommandValue(value);
                    result = result.replace(placeholder, escapedValue);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 转义命令值中的特殊字符
     * @param value 原始值
     * @return 转义后的值
     */
    private static String escapeCommandValue(String value) {
        if (value == null) {
            return "";
        }
        
        // 清理换行符和制表符
        String escaped = value.replace("\n", " ")
                             .replace("\r", " ")
                             .replace("\t", " ");
        
        // 如果包含空格，需要添加引号
        if (escaped.contains(" ")) {
            if (OsUtils.isWindows()) {
                escaped = "\"" + escaped.replace("\"", "\\\"") + "\"";
            } else {
                escaped = escaped.replace(" ", "\\ ")
                                .replace("\"", "\\\"")
                                .replace("'", "\\'")
                                .replace("`", "\\`")
                                .replace("$", "\\$");
            }
        }
        
        return escaped;
    }
    
    /**
     * 验证命令中的变量
     * @param command 命令
     * @param variables 可用变量
     * @return 验证结果
     */
    public static VariableValidationResult validateVariables(String command, Map<String, String> variables) {
        VariableValidationResult result = new VariableValidationResult();
        
        if (command == null || command.isEmpty()) {
            result.setValid(true);
            return result;
        }
        
        // 查找所有占位符
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("%([^%]+)%");
        java.util.regex.Matcher matcher = pattern.matcher(command);
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            if (!variables.containsKey(variableName)) {
                result.addMissingVariable(variableName);
            }
        }
        
        result.setValid(result.getMissingVariables().isEmpty());
        return result;
    }
    
    /**
     * 变量验证结果类
     */
    public static class VariableValidationResult {
        private boolean valid = false;
        private List<String> missingVariables = new java.util.ArrayList<>();
        
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public List<String> getMissingVariables() { return missingVariables; }
        public void addMissingVariable(String variable) { missingVariables.add(variable); }
    }
    
    /**
     * 记录错误日志
     * @param message 错误消息
     * @param e 异常对象
     */
    private static void logError(String message, Exception e) {
        // 日志记录已移除
    }
} 