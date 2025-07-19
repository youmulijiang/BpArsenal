package executor;

import model.HttpTool;
import model.ThirdPartyTool;
import manager.ApiManager;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import java.io.IOException;
import java.awt.Desktop;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import javax.swing.JOptionPane;

/**
 * 工具执行器，负责执行外部工具和命令
 * 采用单例模式确保全局唯一实例
 * 使用策略模式处理HTTP报文解析
 */
public class ToolExecutor {
    private static ToolExecutor instance;
    private final HttpVariableReplacer variableReplacer;
    private HttpMessageParser currentParser;
    
    /**
     * 私有构造函数，防止外部实例化
     * 初始化HTTP解析器和变量替换器
     */
    private ToolExecutor() {
        // 默认使用高级解析器
        this.currentParser = new AdvancedHttpParser();
        this.variableReplacer = new HttpVariableReplacer(currentParser);
    }
    
    /**
     * 获取ToolExecutor单例实例
     * @return ToolExecutor实例
     */
    public static ToolExecutor getInstance() {
        if (instance == null) {
            instance = new ToolExecutor();
        }
        return instance;
    }
    
    /**
     * 执行HTTP工具
     * @param tool 工具配置
     * @param httpRequest HTTP请求对象
     */
    public void executeHttpTool(HttpTool tool, HttpRequest httpRequest) {
        executeHttpTool(tool, httpRequest, null);
    }
    
    /**
     * 执行HTTP工具（支持请求和响应）
     * @param tool 工具配置
     * @param httpRequest HTTP请求对象
     * @param httpResponse HTTP响应对象（可选）
     */
    public void executeHttpTool(HttpTool tool, HttpRequest httpRequest, HttpResponse httpResponse) {
        CompletableFuture.runAsync(() -> {
            try {
                // 验证命令中的变量
                HttpVariableReplacer.VariableValidationResult validation = 
                    variableReplacer.validateVariables(tool.getCommand(), httpRequest, httpResponse);
                
                if (!validation.isValid()) {
                    logVariableValidationWarning(tool.getToolName(), validation);
                }
                
                // 替换变量并执行命令
                String command;
                if (httpResponse != null) {
                    command = variableReplacer.replaceAllVariables(tool.getCommand(), httpRequest, httpResponse);
                } else {
                    command = variableReplacer.replaceRequestVariables(tool.getCommand(), httpRequest);
                }
                
                executeCommand(command, tool.getToolName());
                
            } catch (Exception e) {
                handleError("HTTP工具执行失败", tool.getToolName(), e);
            }
        });
    }
    
    /**
     * 执行第三方工具
     * @param tool 第三方工具配置
     */
    public void executeThirdPartyTool(ThirdPartyTool tool) {
        CompletableFuture.runAsync(() -> {
            try {
                executeCommand(tool.getStartCommand(), tool.getToolName());
            } catch (Exception e) {
                handleError("第三方工具启动失败", tool.getToolName(), e);
            }
        });
    }
    
    /**
     * 打开网站URL
     * @param url 网站URL
     * @param desc 网站描述
     */
    public void openWebsite(String url, String desc) {
        CompletableFuture.runAsync(() -> {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(URI.create(url));
                    
                    if (ApiManager.getInstance().isInitialized()) {
                        ApiManager.getInstance().getApi().logging().logToOutput("打开网站: " + desc + " - " + url);
                    }
                } else {
                    throw new UnsupportedOperationException("系统不支持Desktop操作");
                }
            } catch (Exception e) {
                handleError("打开网站失败", desc, e);
            }
        });
    }
    
    /**
     * 执行系统命令
     * @param command 命令字符串
     * @param toolName 工具名称
     * @throws IOException 执行异常
     */
    private void executeCommand(String command, String toolName) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        
        // 根据操作系统设置命令
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            processBuilder.command("cmd", "/c", command);
        } else {
            processBuilder.command("sh", "-c", command);
        }
        
        Process process = processBuilder.start();
        
        if (ApiManager.getInstance().isInitialized()) {
            ApiManager.getInstance().getApi().logging().logToOutput("执行工具: " + toolName + " - " + command);
        }
        
        // 异步等待进程完成
        CompletableFuture.runAsync(() -> {
            try {
                int exitCode = process.waitFor();
                if (exitCode != 0 && ApiManager.getInstance().isInitialized()) {
                    ApiManager.getInstance().getApi().logging().logToError(toolName + " 执行完成，退出码: " + exitCode);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
    
    /**
     * 设置HTTP解析器策略
     * @param parser HTTP解析器实例
     */
    public void setHttpParser(HttpMessageParser parser) {
        this.currentParser = parser;
    }
    
    /**
     * 获取当前HTTP解析器
     * @return 当前HTTP解析器
     */
    public HttpMessageParser getCurrentParser() {
        return currentParser;
    }
    
    /**
     * 获取可用的请求变量（用于调试）
     * @param httpRequest HTTP请求对象
     * @return 变量映射
     */
    public java.util.Map<String, String> getAvailableRequestVariables(HttpRequest httpRequest) {
        return variableReplacer.getAvailableRequestVariables(httpRequest);
    }
    
    /**
     * 获取可用的响应变量（用于调试）
     * @param httpResponse HTTP响应对象
     * @return 变量映射
     */
    public java.util.Map<String, String> getAvailableResponseVariables(HttpResponse httpResponse) {
        return variableReplacer.getAvailableResponseVariables(httpResponse);
    }
    
    /**
     * 记录变量验证警告
     * @param toolName 工具名称
     * @param validation 验证结果
     */
    private void logVariableValidationWarning(String toolName, HttpVariableReplacer.VariableValidationResult validation) {
        if (ApiManager.getInstance().isInitialized()) {
            String warningMsg = String.format("工具 %s 中包含未解析的变量: %s", 
                toolName, validation.getMissingVariables());
            ApiManager.getInstance().getApi().logging().logToError(warningMsg);
        }
    }
    
    /**
     * 处理执行错误
     * @param operation 操作描述
     * @param toolName 工具名称
     * @param e 异常对象
     */
    private void handleError(String operation, String toolName, Exception e) {
        String errorMsg = operation + ": " + toolName + " - " + e.getMessage();
        
        if (ApiManager.getInstance().isInitialized()) {
            ApiManager.getInstance().getApi().logging().logToError(errorMsg);
        }
        
        // 在UI线程中显示错误提示
        javax.swing.SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                null,
                errorMsg + "\n请检查工具配置和路径",
                "执行错误",
                JOptionPane.ERROR_MESSAGE
            );
        });
    }
} 