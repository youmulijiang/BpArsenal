package executor;

import model.HttpTool;
import model.ThirdPartyTool;
import manager.ApiManager;
import burp.api.montoya.http.message.requests.HttpRequest;
import java.io.IOException;
import java.awt.Desktop;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import javax.swing.JOptionPane;

/**
 * 工具执行器，负责执行外部工具和命令
 * 采用单例模式确保全局唯一实例
 */
public class ToolExecutor {
    private static ToolExecutor instance;
    
    /**
     * 私有构造函数，防止外部实例化
     */
    private ToolExecutor() {}
    
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
        CompletableFuture.runAsync(() -> {
            try {
                String command = replaceVariables(tool.getCommand(), httpRequest);
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
     * 替换命令中的变量
     * @param command 原始命令
     * @param httpRequest HTTP请求对象
     * @return 替换后的命令
     */
    private String replaceVariables(String command, HttpRequest httpRequest) {
        if (httpRequest == null) {
            return command;
        }
        
        String result = command;
        
        // 替换URL变量
        result = result.replace("%http.url%", httpRequest.url());
        
        // 替换主机名变量
        result = result.replace("%http.host%", httpRequest.httpService().host());
        
        // 替换路径变量
        result = result.replace("%http.path%", httpRequest.path());
        
        // 替换HTTP方法变量
        result = result.replace("%http.method%", httpRequest.method());
        
        return result;
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