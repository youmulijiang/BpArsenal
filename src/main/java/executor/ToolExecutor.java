package executor;

import model.HttpTool;
import model.HttpToolCommand;
import model.ThirdPartyTool;
import model.SettingModel;
import manager.ApiManager;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import java.io.IOException;
import java.io.FileWriter;
import java.io.File;
import java.awt.Desktop;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.Map;
import java.util.UUID;
import javax.swing.JOptionPane;

/**
 * 工具执行器，负责执行外部工具和命令
 * 采用单例模式确保全局唯一实例
 * 使用策略模式处理HTTP报文解析
 * 集成操作系统工具功能
 * 统一使用脚本文件执行命令
 */
public class ToolExecutor {
    private static ToolExecutor instance;
    private final HttpVariableReplacer variableReplacer;
    private HttpMessageParser currentParser;
    private SettingModel settingModel;
    
    // 操作系统相关常量
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    
    // 临时脚本目录和文件管理
    private String tempScriptDir;
    private String extensionPath;
    
    /**
     * 私有构造函数，防止外部实例化
     * 初始化HTTP解析器和变量替换器
     */
    private ToolExecutor() {
        // 默认使用高级解析器
        this.currentParser = new AdvancedHttpParser();
        this.variableReplacer = new HttpVariableReplacer(currentParser);
        this.settingModel = new SettingModel();
        
        // 初始化脚本目录
        initializeScriptDirectory();
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
     * 刷新设置模型，重新加载用户配置
     * 当用户在设置面板修改配置后调用
     */
    public void refreshSettings() {
        if (settingModel != null) {
            settingModel.loadToolSettings();
            if (ApiManager.getInstance().isInitialized()) {
                ApiManager.getInstance().getApi().logging().logToOutput(
                    "BpArsenal: ToolExecutor设置已刷新"
                );
            }
        }
    }
    
    /**
     * 确定工作目录
     * 优先级：工具配置的工作目录 > 全局设置的工具目录 > 当前目录
     * @param toolWorkDir 工具配置的工作目录
     * @return 最终使用的工作目录
     */
    private String determineWorkingDirectory(String toolWorkDir) {
        // 1. 首先检查工具配置的工作目录
        if (toolWorkDir != null && !toolWorkDir.trim().isEmpty()) {
            String trimmedToolWorkDir = toolWorkDir.trim();
            File toolDir = new File(trimmedToolWorkDir);
            if (toolDir.exists() && toolDir.isDirectory()) {
                if (ApiManager.getInstance().isInitialized()) {
                    ApiManager.getInstance().getApi().logging().logToOutput(
                        "BpArsenal: 使用工具配置的工作目录 - " + trimmedToolWorkDir
                    );
                }
                return trimmedToolWorkDir;
            } else {
                if (ApiManager.getInstance().isInitialized()) {
                    ApiManager.getInstance().getApi().logging().logToError(
                        "BpArsenal: 工具配置的工作目录无效 - " + trimmedToolWorkDir
                    );
                }
            }
        }
        
        // 2. 检查全局设置的工具目录
        if (settingModel != null) {
            String globalToolDir = settingModel.getToolDirectory();
            if (globalToolDir != null && !globalToolDir.trim().isEmpty()) {
                String trimmedGlobalDir = globalToolDir.trim();
                File globalDir = new File(trimmedGlobalDir);
                if (globalDir.exists() && globalDir.isDirectory()) {
                    if (ApiManager.getInstance().isInitialized()) {
                        ApiManager.getInstance().getApi().logging().logToOutput(
                            "BpArsenal: 使用全局设置的工具目录 - " + trimmedGlobalDir
                        );
                    }
                    return trimmedGlobalDir;
                } else {
                    if (ApiManager.getInstance().isInitialized()) {
                        ApiManager.getInstance().getApi().logging().logToError(
                            "BpArsenal: 全局设置的工具目录无效 - " + trimmedGlobalDir
                        );
                    }
                }
            }
        }
        
        // 3. 都不可用时，返回null表示使用当前目录
        if (ApiManager.getInstance().isInitialized()) {
            ApiManager.getInstance().getApi().logging().logToOutput(
                "BpArsenal: 使用当前目录执行命令"
            );
        }
        return null;
    }
    
    /**
     * 初始化脚本目录
     */
    private void initializeScriptDirectory() {
        try {
            // 获取插件路径
            if (ApiManager.getInstance().isInitialized()) {
                extensionPath = ApiManager.getInstance().getApi().extension().filename();
                
                // 获取插件所在目录
                File extensionFile = new File(extensionPath);
                String extensionDir = extensionFile.getParent();
                
                // 创建临时脚本目录
//                tempScriptDir = extensionDir + File.separator + "temp_scripts";
//                File scriptDir = new File(tempScriptDir);
//                if (!scriptDir.exists()) {
//                    scriptDir.mkdirs();
//                }
                
                if (ApiManager.getInstance().isInitialized()) {
                    ApiManager.getInstance().getApi().logging().logToOutput(
                        "BpArsenal: 脚本目录初始化成功 - " + tempScriptDir
                    );
                }
            } else {
                // 如果API未初始化，使用系统临时目录
                tempScriptDir = System.getProperty("java.io.tmpdir") + File.separator + "bparsenal_scripts";
                File scriptDir = new File(tempScriptDir);
                if (!scriptDir.exists()) {
                    scriptDir.mkdirs();
                }
            }
        } catch (Exception e) {
            // fallback到系统临时目录
            tempScriptDir = System.getProperty("java.io.tmpdir") + File.separator + "bparsenal_scripts";
            File scriptDir = new File(tempScriptDir);
            if (!scriptDir.exists()) {
                scriptDir.mkdirs();
            }
            
            if (ApiManager.getInstance().isInitialized()) {
                ApiManager.getInstance().getApi().logging().logToError(
                    "BpArsenal: 使用fallback脚本目录 - " + tempScriptDir + ", 原因: " + e.getMessage()
                );
            }
        }
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
                
                // HttpTool本身没有workDir，使用全局设置
                String workDir = determineWorkingDirectory(null);
                executeCommandViaScript(command, tool.getToolName(), workDir);
                
            } catch (Exception e) {
                handleError("HTTP工具执行失败", tool.getToolName(), e);
            }
        });
    }
    
    /**
     * 执行HTTP工具命令
     * @param toolCommand 工具命令配置
     * @param httpRequest HTTP请求对象
     */
    public void executeHttpToolCommand(HttpToolCommand toolCommand, HttpRequest httpRequest) {
        executeHttpToolCommand(toolCommand, httpRequest, null);
    }
    
    /**
     * 执行HTTP工具命令（支持请求和响应）
     * @param toolCommand 工具命令配置
     * @param httpRequest HTTP请求对象
     * @param httpResponse HTTP响应对象（可选）
     */
    public void executeHttpToolCommand(HttpToolCommand toolCommand, HttpRequest httpRequest, HttpResponse httpResponse) {
        CompletableFuture.runAsync(() -> {
            try {
                // 验证命令中的变量
                HttpVariableReplacer.VariableValidationResult validation = 
                    variableReplacer.validateVariables(toolCommand.getCommand(), httpRequest, httpResponse);
                
                if (!validation.isValid()) {
                    logVariableValidationWarning(toolCommand.getToolName(), validation);
                }
                
                // 替换变量并执行命令
                String command;
                if (httpResponse != null) {
                    command = variableReplacer.replaceAllVariables(toolCommand.getCommand(), httpRequest, httpResponse);
                } else {
                    command = variableReplacer.replaceRequestVariables(toolCommand.getCommand(), httpRequest);
                }
                
                // 获取工作目录
                String workDir = determineWorkingDirectory(toolCommand.getWorkDir());
                executeCommandViaScript(command, toolCommand.getToolName(), workDir);
                
            } catch (Exception e) {
                handleError("HTTP工具执行失败", toolCommand.getToolName(), e);
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
                // 第三方工具没有独立的工作目录配置，使用全局设置
                String workDir = determineWorkingDirectory(null);
                executeCommandViaScript(tool.getStartCommand(), tool.getToolName(), workDir);
            } catch (Exception e) {
                handleError("第三方工具启动失败", tool.getToolName(), e);
            }
        });
    }
    
    /**
     * 通过脚本文件执行命令（统一入口）
     * @param command 命令字符串
     * @param toolName 工具名称
     * @throws IOException 执行异常
     */
    private void executeCommandViaScript(String command, String toolName) throws IOException {
        executeCommandViaScript(command, toolName, null);
    }
    
    /**
     * 直接执行命令（不使用临时脚本文件）
     * @param command 命令字符串
     * @param toolName 工具名称
     * @param workDir 工作目录（可为null）
     * @throws IOException 执行异常
     */
    private void executeCommandViaScript(String command, String toolName, String workDir) throws IOException {
        executeCommandDirectly(command, toolName, workDir);
    }
    
    /**
     * 直接使用ProcessBuilder执行命令
     * @param command 命令字符串
     * @param toolName 工具名称
     * @param workDir 工作目录（可为null）
     * @throws IOException 执行异常
     */
    private void executeCommandDirectly(String command, String toolName, String workDir) throws IOException {
        ProcessBuilder processBuilder;
        
        // 根据操作系统选择合适的命令执行方式
        if (isWindows()) {
            // Windows: 在新的命令窗口中执行
            processBuilder = new ProcessBuilder("cmd", "/c", "start", "\"" + toolName + "\"", "cmd", "/k", command + " & pause");
        } else {
            // Linux/Unix: 在终端中执行
            if (isMac()) {
                // macOS: 使用Terminal.app
                String applescript = String.format(
                    "tell application \"Terminal\" to do script \"%s; echo 'Press Enter to continue...'; read\"",
                    command.replace("\"", "\\\"")
                );
                processBuilder = new ProcessBuilder("osascript", "-e", applescript);
            } else {
                // Linux: 使用x-terminal-emulator
                processBuilder = new ProcessBuilder("x-terminal-emulator", "-e", "bash", "-c", 
                    command + "; echo 'Press Enter to continue...'; read");
            }
        }
        
        // 设置工作目录（如果指定）
        if (workDir != null && !workDir.trim().isEmpty()) {
            File workDirectory = new File(workDir.trim());
            if (workDirectory.exists() && workDirectory.isDirectory()) {
                processBuilder.directory(workDirectory);
                if (ApiManager.getInstance().isInitialized()) {
                    ApiManager.getInstance().getApi().logging().logToOutput(
                        "BpArsenal: 设置ProcessBuilder工作目录 - " + workDir
                    );
                }
            } else {
                if (ApiManager.getInstance().isInitialized()) {
                    ApiManager.getInstance().getApi().logging().logToError(
                        "BpArsenal: 工作目录无效，将使用默认目录 - " + workDir
                    );
                }
            }
        }
        
        // 设置环境变量
        Map<String, String> env = processBuilder.environment();
        env.put("PATH", System.getenv("PATH"));
        if (isWindows()) {
            // Windows环境下设置编码
            env.put("CHCP", "65001"); // UTF-8编码
        }
        
        // 启动进程
        Process process = processBuilder.start();
        
        // 记录执行日志
        if (ApiManager.getInstance().isInitialized()) {
            String logMsg = workDir != null ? 
                String.format("直接执行工具: %s (工作目录: %s) - %s", toolName, workDir, command) :
                String.format("直接执行工具: %s - %s", toolName, command);
            ApiManager.getInstance().getApi().logging().logToOutput(logMsg);
        }
        
        // 异步等待进程完成
        CompletableFuture.runAsync(() -> {
            try {
                int exitCode = process.waitFor();
                if (ApiManager.getInstance().isInitialized()) {
                    if (exitCode == 0) {
                        ApiManager.getInstance().getApi().logging().logToOutput(
                            toolName + " 命令执行完成，退出码: " + exitCode
                        );
                    } else {
                        ApiManager.getInstance().getApi().logging().logToError(
                            toolName + " 命令执行完成，退出码: " + exitCode
                        );
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (ApiManager.getInstance().isInitialized()) {
                    ApiManager.getInstance().getApi().logging().logToError(
                        toolName + " 命令执行被中断: " + e.getMessage()
                    );
                }
            }
        });
    }
    
    /**
     * 创建临时脚本文件
     * @param command 要执行的命令
     * @param toolName 工具名称
     * @return 创建的脚本文件
     * @throws IOException 文件创建异常
     */
    private File createTemporaryScript(String command, String toolName) throws IOException {
        return createTemporaryScript(command, toolName, null);
    }
    
    /**
     * 创建临时脚本文件（支持工作目录）
     * @param command 要执行的命令
     * @param toolName 工具名称
     * @param workDir 工作目录（可为null）
     * @return 创建的脚本文件
     * @throws IOException 文件创建异常
     */
    private File createTemporaryScript(String command, String toolName, String workDir) throws IOException {
        String scriptFileName;
        String scriptContent;
        
        if (isWindows()) {
            // Windows: 创建 .bat 文件
            scriptFileName = "bparsenal_" + sanitizeFileName(toolName) + "_" + UUID.randomUUID().toString().substring(0, 8) + ".bat";
            scriptContent = generateWindowsBatchScript(command, toolName, workDir);
        } else {
            // Linux/Unix: 创建 .sh 文件
            scriptFileName = "bparsenal_" + sanitizeFileName(toolName) + "_" + UUID.randomUUID().toString().substring(0, 8) + ".sh";
            scriptContent = generateLinuxShellScript(command, toolName, workDir);
        }
        
        File scriptFile = new File(tempScriptDir, scriptFileName);
        
        // 写入脚本内容 - 使用系统默认编码
        try (FileWriter writer = new FileWriter(scriptFile)) {
            writer.write(scriptContent);
        }
        
        // 设置可执行权限（Linux/Unix）
        if (!isWindows()) {
            scriptFile.setExecutable(true, true);
        }
        
        if (ApiManager.getInstance().isInitialized()) {
            ApiManager.getInstance().getApi().logging().logToOutput(
                "BpArsenal: 创建临时脚本 - " + scriptFile.getAbsolutePath()
            );
        }
        
        return scriptFile;
    }
    
    /**
     * 生成Windows批处理脚本内容
     * @param command 要执行的命令
     * @param toolName 工具名称
     * @return 脚本内容
     */
    private String generateWindowsBatchScript(String command, String toolName) {
        return generateWindowsBatchScript(command, toolName, null);
    }
    
    /**
     * 生成Windows批处理脚本内容（支持工作目录）
     * @param command 要执行的命令
     * @param toolName 工具名称
     * @param workDir 工作目录（可为null）
     * @return 脚本内容
     */
    private String generateWindowsBatchScript(String command, String toolName, String workDir) {
        StringBuilder script = new StringBuilder();
        script.append("@echo off\r\n");
        script.append("chcp 65001\r\n");
        script.append("title BpArsenal - ").append(toolName).append("\r\n");
        script.append("echo ================================================\r\n");
        script.append("echo BpArsenal Weapon Arsenal Tool Execution\r\n");
        script.append("echo Tool Name: ").append(toolName).append("\r\n");
        script.append("echo Time: %date% %time%\r\n");
        script.append("echo ================================================\r\n");
        script.append("echo.\r\n");
        
        // 切换工作目录（如果指定）
        if (workDir != null && !workDir.trim().isEmpty()) {
            script.append("echo Changing to work directory: ").append(workDir).append("\r\n");
            script.append("cd /d \"").append(workDir).append("\"\r\n");
            script.append("if errorlevel 1 (\r\n");
            script.append("    echo ERROR: Failed to change to work directory\r\n");
            script.append("    pause\r\n");
            script.append("    exit /b 1\r\n");
            script.append(")\r\n");
            script.append("echo Current directory: %cd%\r\n");
            script.append("echo.\r\n");
        }
        
        script.append("echo Executing command:\r\n");
        script.append("echo ").append(command).append("\r\n");
        script.append("echo.\r\n");
        script.append("echo ========================================\r\n");
        script.append("echo.\r\n");
        
        // 执行实际命令
        script.append(command).append("\r\n");
        script.append("set EXEC_CODE=%ERRORLEVEL%\r\n");
        script.append("\r\n");
        
        script.append("echo.\r\n");
        script.append("echo ========================================\r\n");
        script.append("echo Command completed with exit code: %EXEC_CODE%\r\n");
        script.append("echo ========================================\r\n");
        script.append("echo.\r\n");
        script.append("pause\r\n");
        
        return script.toString();
    }
    
    /**
     * 生成Linux Shell脚本内容
     * @param command 要执行的命令
     * @param toolName 工具名称
     * @return 脚本内容
     */
    private String generateLinuxShellScript(String command, String toolName) {
        return generateLinuxShellScript(command, toolName, null);
    }
    
    /**
     * 生成Linux Shell脚本内容（支持工作目录）
     * @param command 要执行的命令
     * @param toolName 工具名称
     * @param workDir 工作目录（可为null）
     * @return 脚本内容
     */
    private String generateLinuxShellScript(String command, String toolName, String workDir) {
        StringBuilder script = new StringBuilder();
        script.append("#!/bin/bash\n");
        script.append("\n");
        script.append("# BpArsenal 工具执行器\n");
        script.append("# 工具名称: ").append(toolName).append("\n");
        script.append("# 生成时间: $(date)\n");
        script.append("\n");
        script.append("echo \"================================================\"\n");
        script.append("echo \"BpArsenal 工具执行器\"\n");
        script.append("echo \"工具名称: ").append(toolName).append("\"\n");
        script.append("echo \"执行时间: $(date)\"\n");
        script.append("echo \"================================================\"\n");
        script.append("echo\n");
        
        // 切换工作目录（如果指定）
        if (workDir != null && !workDir.trim().isEmpty()) {
            script.append("echo \"切换到工作目录: ").append(workDir).append("\"\n");
            script.append("cd \"").append(workDir).append("\"\n");
            script.append("if [ $? -ne 0 ]; then\n");
            script.append("    echo \"错误: 无法切换到工作目录\"\n");
            script.append("    echo \"按 Enter 键继续...\"\n");
            script.append("    read\n");
            script.append("    exit 1\n");
            script.append("fi\n");
            script.append("echo \"当前目录: $(pwd)\"\n");
            script.append("echo\n");
        }
        
        script.append("echo \"执行命令: ").append(command).append("\"\n");
        script.append("echo\n");
        script.append("echo \"开始执行...\"\n");
        script.append("echo \"------------------------------------------------\"\n");
        script.append("\n");
        
        // 执行实际命令
        script.append(command).append("\n");
        script.append("EXIT_CODE=$?\n");
        script.append("\n");
        
        script.append("echo \"------------------------------------------------\"\n");
        script.append("echo \"命令执行完成\"\n");
        script.append("echo \"退出码: $EXIT_CODE\"\n");
        script.append("echo \"================================================\"\n");
        script.append("echo \"按 Enter 键继续...\"\n");
        script.append("read\n");
        
        return script.toString();
    }
    
    /**
     * 执行脚本文件
     * @param scriptFile 脚本文件
     * @param toolName 工具名称
     * @throws IOException 执行异常
     */
    private void executeScriptFile(File scriptFile, String toolName) throws IOException {
        executeScriptFile(scriptFile, toolName, null);
    }
    
    /**
     * 执行脚本文件（支持工作目录设置）
     * @param scriptFile 脚本文件
     * @param toolName 工具名称
     * @param workDir 工作目录（可为null）
     * @throws IOException 执行异常
     */
    private void executeScriptFile(File scriptFile, String toolName, String workDir) throws IOException {
        ProcessBuilder processBuilder;
        
        if (isWindows()) {
            // Windows: 在新窗口中执行 .bat 文件
            processBuilder = new ProcessBuilder("cmd", "/c", "start", "\"" + toolName + "\"", "cmd", "/c", scriptFile.getAbsolutePath());
        } else {
            // Linux: 在终端中执行 .sh 文件
            if (isMac()) {
                // macOS: 使用 Terminal.app
                String applescript = String.format(
                    "tell application \"Terminal\" to do script \"'%s'; exit\"",
                    scriptFile.getAbsolutePath().replace("'", "'\"'\"'")
                );
                processBuilder = new ProcessBuilder("osascript", "-e", applescript);
            } else {
                // Linux: 尝试使用常见的终端模拟器
                processBuilder = new ProcessBuilder("x-terminal-emulator", "-e", "bash", "-c", 
                    scriptFile.getAbsolutePath() + "; exec bash");
            }
        }
        
        // 设置工作目录（如果指定）
        if (workDir != null && !workDir.trim().isEmpty()) {
            File workDirectory = new File(workDir.trim());
            if (workDirectory.exists() && workDirectory.isDirectory()) {
                processBuilder.directory(workDirectory);
                if (ApiManager.getInstance().isInitialized()) {
                    ApiManager.getInstance().getApi().logging().logToOutput(
                        "BpArsenal: ProcessBuilder 设置工作目录 - " + workDir
                    );
                }
            } else {
                if (ApiManager.getInstance().isInitialized()) {
                    ApiManager.getInstance().getApi().logging().logToError(
                        "BpArsenal: ProcessBuilder 工作目录无效，将使用默认目录 - " + workDir
                    );
                }
            }
        }
        
        // 设置环境变量
        Map<String, String> env = processBuilder.environment();
        env.put("PATH", System.getenv("PATH"));
        
        // 启动进程
        Process process = processBuilder.start();
        
        // 异步等待进程完成
        CompletableFuture.runAsync(() -> {
            try {
                int exitCode = process.waitFor();
                if (exitCode != 0 && ApiManager.getInstance().isInitialized()) {
                    ApiManager.getInstance().getApi().logging().logToError(
                        toolName + " 脚本执行完成，退出码: " + exitCode
                    );
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
    
    /**
     * 安排脚本文件清理
     * @param scriptFile 要清理的脚本文件
     */
    private void scheduleScriptCleanup(File scriptFile) {
        // 延迟30秒后删除脚本文件，给执行足够的时间
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(30000); // 等待30秒
                if (scriptFile.exists() && scriptFile.delete()) {
                    if (ApiManager.getInstance().isInitialized()) {
                        ApiManager.getInstance().getApi().logging().logToOutput(
                            "BpArsenal: 清理临时脚本 - " + scriptFile.getName()
                        );
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
    
    /**
     * 清理文件名，移除不安全字符
     * @param fileName 原始文件名
     * @return 安全的文件名
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "unknown";
        }
        // 移除或替换不安全的文件名字符
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
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
     * 执行系统命令（保留原方法以兼容性）
     * @param command 命令字符串
     * @param toolName 工具名称
     * @throws IOException 执行异常
     */
    private void executeCommand(String command, String toolName) throws IOException {
        // 重定向到新的脚本执行方法
        executeCommandViaScript(command, toolName);
    }
    
    /**
     * 执行系统命令（同步版本，用于UI组件）
     * @param command 命令字符串
     * @param toolName 工具名称
     * @param callback 执行结果回调
     */
    public void executeCommandSync(String command, String toolName, CommandExecutionCallback callback) {
        executeCommandSync(command, toolName, null, callback);
    }
    
    /**
     * 执行系统命令（同步版本，简化版本）
     * @param command 命令字符串
     * @param toolName 工具名称
     * @param workDir 工作目录（可为null）
     */
    public void executeCommandSync(String command, String toolName, String workDir) {
        executeCommandSync(command, toolName, workDir, null);
    }
    
    /**
     * 执行系统命令（同步版本，支持工作目录）
     * @param command 命令字符串
     * @param toolName 工具名称
     * @param workDir 工作目录（可为null）
     * @param callback 执行结果回调
     */
    public void executeCommandSync(String command, String toolName, String workDir, CommandExecutionCallback callback) {
        CompletableFuture.runAsync(() -> {
            try {
                if (callback != null) {
                    callback.onCommandStart(toolName, command);
                }
                
                // 确定最终工作目录
                String finalWorkDir = determineWorkingDirectory(workDir);
                
                // 直接执行命令（支持工作目录）
                executeCommandDirectly(command, toolName, finalWorkDir);
                
                // 构建输出信息
                StringBuilder output = new StringBuilder();
                output.append("命令已直接启动执行\n");
                output.append("执行环境: ").append(isWindows() ? "Windows Command Prompt" : (isMac() ? "macOS Terminal" : "Linux Terminal")).append("\n");
                if (finalWorkDir != null) {
                    output.append("工作目录: ").append(finalWorkDir).append("\n");
                }
                
                if (callback != null) {
                    callback.onOutputReceived("命令已直接启动执行");
                    callback.onOutputReceived("执行环境: " + (isWindows() ? "Windows Command Prompt" : (isMac() ? "macOS Terminal" : "Linux Terminal")));
                    if (finalWorkDir != null) {
                        callback.onOutputReceived("工作目录: " + finalWorkDir);
                    }
                    callback.onCommandComplete(toolName, 0, output.toString());
                }
                
                // 记录到Burp日志
                if (ApiManager.getInstance().isInitialized()) {
                    String logMsg = finalWorkDir != null ? 
                        String.format("工具直接执行: %s (工作目录: %s)", toolName, finalWorkDir) :
                        String.format("工具直接执行: %s", toolName);
                    ApiManager.getInstance().getApi().logging().logToOutput(logMsg);
                }
                
            } catch (Exception e) {
                if (callback != null) {
                    callback.onCommandError(toolName, e);
                }
                
                if (ApiManager.getInstance().isInitialized()) {
                    ApiManager.getInstance().getApi().logging().logToError("脚本执行异常: " + e.getMessage());
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
        // 重定向到统一的脚本执行方法
        executeCommandSync(command, toolName, callback);
    }
    
    /**
     * 为ArsenalDialog提供的专用执行方法，支持传入HttpToolCommand的工作目录
     * @param command 要执行的命令
     * @param toolName 工具名称
     * @param toolWorkDir 工具配置的工作目录（可为null）
     * @throws IOException 执行异常
     */
    public void executeCommandWithWorkDir(String command, String toolName, String toolWorkDir) throws IOException {
        // 确定最终工作目录
        String finalWorkDir = determineWorkingDirectory(toolWorkDir);
        
        // 直接执行命令
        executeCommandDirectly(command, toolName, finalWorkDir);
    }
    
    /**
     * 为ArsenalDialog提供的异步执行方法，支持传入HttpToolCommand的工作目录
     * @param command 要执行的命令
     * @param toolName 工具名称
     * @param toolWorkDir 工具配置的工作目录（可为null）
     */
    public void executeCommandWithWorkDirAsync(String command, String toolName, String toolWorkDir) {
        CompletableFuture.runAsync(() -> {
            try {
                executeCommandWithWorkDir(command, toolName, toolWorkDir);
            } catch (Exception e) {
                handleError("命令执行失败", toolName, e);
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
     * 获取临时脚本目录路径（现在不再使用，保留方法以兼容）
     * @return 脚本目录路径
     */
    public String getTempScriptDirectory() {
        return tempScriptDir;
    }
    
    /**
     * 获取插件路径
     * @return 插件文件路径
     */
    public String getExtensionPath() {
        return extensionPath;
    }
    
    /**
     * 手动清理所有临时脚本文件（现在不再使用临时脚本文件，保留方法以兼容）
     */
    public void cleanupAllTempScripts() {
        if (ApiManager.getInstance().isInitialized()) {
            ApiManager.getInstance().getApi().logging().logToOutput(
                "BpArsenal: 现在使用直接命令执行，无需清理临时脚本文件"
            );
        }
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