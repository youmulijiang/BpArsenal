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
import java.util.Map;
import javax.swing.JOptionPane;

/**
 * 工具执行器，负责执行外部工具和命令
 * 采用单例模式确保全局唯一实例
 * 使用策略模式处理HTTP报文解析
 * 集成操作系统工具功能
 */
public class ToolExecutor {
    private static ToolExecutor instance;
    private final HttpVariableReplacer variableReplacer;
    private HttpMessageParser currentParser;
    
    // 操作系统相关常量
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    
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
     * 预览HTTP工具命令（用于UI显示）
     * @param tool 工具配置
     * @param httpRequest HTTP请求对象
     * @return 替换变量后的命令字符串
     */
    public String previewCommand(HttpTool tool, HttpRequest httpRequest) {
        return previewCommand(tool, httpRequest, null);
    }
    
    /**
     * 预览HTTP工具命令（支持请求和响应）
     * @param tool 工具配置
     * @param httpRequest HTTP请求对象
     * @param httpResponse HTTP响应对象（可选）
     * @return 替换变量后的命令字符串
     */
    public String previewCommand(HttpTool tool, HttpRequest httpRequest, HttpResponse httpResponse) {
        try {
            String command;
            if (httpResponse != null) {
                command = variableReplacer.replaceAllVariables(tool.getCommand(), httpRequest, httpResponse);
            } else {
                command = variableReplacer.replaceRequestVariables(tool.getCommand(), httpRequest);
            }
            return command;
        } catch (Exception e) {
            return "命令预览失败: " + e.getMessage();
        }
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
        
        // 使用集成的操作系统工具格式化命令
        String[] formattedCommand = formatCommandForRunningOnOperatingSystem(command);
        processBuilder.command(formattedCommand);
        
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
     * 执行系统命令（同步版本，用于UI组件）
     * @param command 命令字符串
     * @param toolName 工具名称
     * @param callback 执行结果回调
     */
    public void executeCommandSync(String command, String toolName, CommandExecutionCallback callback) {
        CompletableFuture.runAsync(() -> {
            try {
                if (callback != null) {
                    callback.onCommandStart(toolName, command);
                }
                
                // 使用终端模式格式化命令，让用户能看到执行过程
                String[] formattedCommand = formatCommandForRunningInTerminal(command);
                ProcessBuilder processBuilder = new ProcessBuilder(formattedCommand);
                
                // 确保ProcessBuilder具有正确的环境变量
                ensureProperEnvironment(processBuilder);
                
                // 对于终端执行，不重定向输出流，让终端自己处理
                // processBuilder.redirectErrorStream(true);
                
                Process process = processBuilder.start();
                
                // 由于命令在新终端窗口中执行，我们无法直接读取输出
                // 但可以等待进程完成
                int exitCode = process.waitFor();
                
                // 构建模拟输出信息
                StringBuilder output = new StringBuilder();
                output.append("命令已在新终端窗口中启动执行\n");
                output.append("终端窗口应该已经弹出，请查看终端输出\n");
                
                if (callback != null) {
                    callback.onOutputReceived("命令已在新终端窗口中启动执行");
                    callback.onOutputReceived("终端窗口应该已经弹出，请查看终端输出");
                }
                
                if (callback != null) {
                    callback.onCommandComplete(toolName, exitCode, output.toString());
                }
                
                // 记录到Burp日志
                if (ApiManager.getInstance().isInitialized()) {
                    String logMsg = String.format("工具执行完成: %s (退出码: %d)", toolName, exitCode);
                    if (exitCode == 0) {
                        ApiManager.getInstance().getApi().logging().logToOutput(logMsg);
                    } else {
                        ApiManager.getInstance().getApi().logging().logToError(logMsg);
                    }
                }
                
            } catch (Exception e) {
                if (callback != null) {
                    callback.onCommandError(toolName, e);
                }
                
                if (ApiManager.getInstance().isInitialized()) {
                    ApiManager.getInstance().getApi().logging().logToError("命令执行异常: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 在终端窗口中执行命令（可见执行）
     * @param command 命令字符串
     * @param toolName 工具名称
     * @param callback 执行结果回调
     */
    public void executeCommandInTerminal(String command, String toolName, CommandExecutionCallback callback) {
        CompletableFuture.runAsync(() -> {
            try {
                if (callback != null) {
                    callback.onCommandStart(toolName, command);
                }
                
                // 使用终端模式格式化命令
                String[] formattedCommand = formatCommandForRunningInTerminal(command);
                ProcessBuilder processBuilder = new ProcessBuilder(formattedCommand);
                
                // 确保ProcessBuilder具有正确的环境变量
                ensureProperEnvironment(processBuilder);
                
                Process process = processBuilder.start();
                
                // 由于命令在新终端窗口中执行，我们无法直接读取输出
                int exitCode = process.waitFor();
                
                // 构建模拟输出信息
                StringBuilder output = new StringBuilder();
                output.append("命令已在新终端窗口中启动执行\n");
                output.append("终端窗口应该已经弹出，请查看终端输出\n");
                
                if (callback != null) {
                    callback.onOutputReceived("命令已在新终端窗口中启动执行");
                    callback.onOutputReceived("终端窗口应该已经弹出，请查看终端输出");
                    callback.onCommandComplete(toolName, exitCode, output.toString());
                }
                
                // 记录到Burp日志
                if (ApiManager.getInstance().isInitialized()) {
                    String logMsg = String.format("工具在终端中执行完成: %s (退出码: %d)", toolName, exitCode);
                    if (exitCode == 0) {
                        ApiManager.getInstance().getApi().logging().logToOutput(logMsg);
                    } else {
                        ApiManager.getInstance().getApi().logging().logToError(logMsg);
                    }
                }
                
            } catch (Exception e) {
                if (callback != null) {
                    callback.onCommandError(toolName, e);
                }
                
                if (ApiManager.getInstance().isInitialized()) {
                    ApiManager.getInstance().getApi().logging().logToError("终端命令执行异常: " + e.getMessage());
                }
            }
        });
    }
    
    // ===== 集成的操作系统工具方法 =====
    
    /**
     * 判断是否为Windows系统
     * @return 是否为Windows系统
     */
    public static boolean isWindows() {
        return OS_NAME.contains("win");
    }
    
    /**
     * 判断是否为Linux系统
     * @return 是否为Linux系统
     */
    public static boolean isLinux() {
        return OS_NAME.contains("nix") || OS_NAME.contains("nux") || OS_NAME.contains("aix");
    }
    
    /**
     * 判断是否为Mac系统
     * @return 是否为Mac系统
     */
    public static boolean isMac() {
        return OS_NAME.contains("mac");
    }
    
    /**
     * 判断是否为Unix-like系统（Linux, Mac, Unix等）
     * @return 是否为Unix-like系统
     */
    public static boolean isUnixLike() {
        return isLinux() || isMac() || OS_NAME.contains("freebsd") || OS_NAME.contains("openbsd") || OS_NAME.contains("netbsd");
    }
    
    /**
     * 获取系统类型字符串
     * @return 系统类型（Windows, Linux, Mac, Unknown）
     */
    public static String getOsType() {
        if (isWindows()) {
            return "Windows";
        } else if (isLinux()) {
            return "Linux";
        } else if (isMac()) {
            return "Mac";
        } else {
            return "Unknown";
        }
    }
    
    /**
     * 获取默认的命令执行前缀
     * @return 命令执行前缀数组
     */
    public static String[] getDefaultCommandPrefix() {
        if (isWindows()) {
            return new String[]{"cmd", "/c"};
        } else {
            return new String[]{"/bin/bash", "-c"};
        }
    }
    
    /**
     * 格式化命令以在当前操作系统上运行（后台执行）
     * @param command 原始命令
     * @return 格式化后的命令数组
     */
    public static String[] formatCommandForRunningOnOperatingSystem(String command) {
        if (isWindows()) {
            return new String[]{"cmd", "/c", command};
        } else {
            return new String[]{"/bin/bash", "-c", command};
        }
    }
    
    /**
     * 格式化命令以在终端窗口中运行（可见执行）
     * @param command 原始命令
     * @return 格式化后的命令数组
     */
    public static String[] formatCommandForRunningInTerminal(String command) {
        if (isWindows()) {
            // Windows: 使用start命令在新的命令提示符窗口中执行
            // 修复: 使用正确的start语法，并添加title参数
            return new String[]{"cmd", "/c", "start", "\"工具执行\"", "cmd", "/k", command + " & pause"};
        } else if (isMac()) {
            // macOS: 使用osascript在Terminal.app中执行
            return new String[]{"osascript", "-e", "tell application \"Terminal\" to do script \"" + command.replace("\"", "\\\"") + "\""};
        } else {
            // Linux: 尝试使用常见的终端模拟器
            return new String[]{"x-terminal-emulator", "-e", "/bin/bash", "-c", command + "; read -p 'Press Enter to continue...'"};
        }
    }
    
    /**
     * 使用自定义前缀格式化命令
     * @param command 原始命令
     * @param prefix 自定义前缀
     * @return 格式化后的命令数组
     */
    public static String[] formatCommandWithPrefix(String command, String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return formatCommandForRunningOnOperatingSystem(command);
        }
        
        String[] prefixParts = prefix.trim().split("\\s+");
        String[] result = new String[prefixParts.length + 1];
        System.arraycopy(prefixParts, 0, result, 0, prefixParts.length);
        result[prefixParts.length] = command;
        return result;
    }
    
    /**
     * 获取系统编码
     * @return 编码字符串
     */
    public static String getSystemEncoding() {
        if (isWindows()) {
            return "GBK";
        } else {
            return "UTF-8";
        }
    }
    
    /**
     * 确保ProcessBuilder具有正确的环境变量
     * @param processBuilder ProcessBuilder实例
     */
    public static void ensureProperEnvironment(ProcessBuilder processBuilder) {
        Map<String, String> env = processBuilder.environment();
        
        if (isWindows()) {
            // 确保Windows系统的关键路径在PATH中
            String path = env.get("PATH");
            if (path == null) {
                path = System.getenv("PATH");
            }
            
            if (path != null) {
                // 添加System32和System目录
                String winDir = System.getenv("WINDIR");
                if (winDir != null) {
                    String system32 = winDir + "\\System32";
                    String system = winDir + "\\System";
                    
                    if (!path.toLowerCase().contains("system32")) {
                        path += ";" + system32;
                    }
                    if (!path.toLowerCase().contains("system\\") && !path.toLowerCase().contains("system;")) {
                        path += ";" + system;
                    }
                    
                    env.put("PATH", path);
                }
            }
        }
    }
    
    /**
     * 命令执行回调接口
     */
    public interface CommandExecutionCallback {
        /**
         * 命令开始执行
         * @param toolName 工具名称
         * @param command 执行的命令
         */
        void onCommandStart(String toolName, String command);
        
        /**
         * 接收到输出
         * @param output 输出内容
         */
        void onOutputReceived(String output);
        
        /**
         * 命令执行完成
         * @param toolName 工具名称
         * @param exitCode 退出码
         * @param fullOutput 完整输出
         */
        void onCommandComplete(String toolName, int exitCode, String fullOutput);
        
        /**
         * 命令执行出错
         * @param toolName 工具名称
         * @param error 错误信息
         */
        void onCommandError(String toolName, Exception error);
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