package view.contextmenu;

import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.InvocationType;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import manager.ApiManager;
import manager.ConfigManager;
import model.HttpTool;
import model.HttpToolCommand;
import model.Config;
import view.component.ArsenalDialog;
import controller.ToolController;
import executor.AdvancedHttpParser;
import executor.ToolExecutor;

import javax.swing.*;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Arsenal上下文菜单提供者
 * 在右键菜单中提供Favorite和Arsenal选项
 */
public class ArsenalContextMenuProvider implements ContextMenuItemsProvider {
    
    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        List<Component> menuItems = new ArrayList<>();
        
        // 只在HTTP请求/响应上下文中显示菜单
//        if (event.invocationType() == InvocationType.MESSAGE_EDITOR_REQUEST ||
//            event.invocationType() == InvocationType.MESSAGE_EDITOR_RESPONSE ||
//            event.invocationType() == InvocationType.PROXY_HISTORY ||
//            event.invocationType() == InvocationType.SITE_MAP_TREE ||
//            event.invocationType() == InvocationType.SITE_MAP_TABLE) {
//
//            // 创建Favorite菜单项
//            JMenuItem favoriteItem = new JMenuItem("Favorite");
//            favoriteItem.addActionListener(e -> handleFavoriteAction(event));
//            menuItems.add(favoriteItem);
//
//            // 创建Arsenal菜单项
//            JMenuItem arsenalItem = new JMenuItem("Arsenal");
//            arsenalItem.addActionListener(e -> handleArsenalAction(event));
//            menuItems.add(arsenalItem);
//        }

        // 创建Favorite子菜单
        JMenu favoriteMenu = createFavoriteMenu(event);
        if (favoriteMenu != null) {
            menuItems.add(favoriteMenu);
        }

        // 创建Arsenal菜单项
        JMenuItem arsenalItem = new JMenuItem("Arsenal");
        arsenalItem.addActionListener(e -> handleArsenalAction(event));
        menuItems.add(arsenalItem);
        
        return menuItems;
    }
    
    /**
     * 创建Favorite菜单
     * @param event 上下文菜单事件
     * @return Favorite菜单，如果没有收藏工具则返回null
     */
    private JMenu createFavoriteMenu(ContextMenuEvent event) {
        try {
            // 获取选中的HTTP消息
            HttpRequest httpRequest = getHttpRequestFromEvent(event);
            HttpResponse httpResponse = getHttpResponseFromEvent(event);
            
            if (httpRequest == null) {
                return null;
            }
            
            // 获取所有收藏的工具命令
            List<HttpToolCommand> favoriteCommands = getFavoriteToolCommands();
            
            if (favoriteCommands.isEmpty()) {
                return null;
            }
            
            // 创建Favorite主菜单
            JMenu favoriteMenu = new JMenu("Favorite");
            favoriteMenu.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            
            // 按分类分组收藏的命令
            Map<String, List<HttpToolCommand>> commandsByCategory = groupCommandsByCategory(favoriteCommands);
            
            // 为每个分类创建子菜单
            List<String> sortedCategories = commandsByCategory.keySet().stream()
                    .sorted()
                    .collect(Collectors.toList());
            
            for (String category : sortedCategories) {
                List<HttpToolCommand> categoryCommands = commandsByCategory.get(category);
                
                // 创建分类子菜单
                JMenu categoryMenu = new JMenu(category + " (" + categoryCommands.size() + ")");
                categoryMenu.setFont(new Font("微软雅黑", Font.PLAIN, 11));
                categoryMenu.setIcon(createCategoryIcon(category));
                
                // 添加该分类下的所有命令
                for (HttpToolCommand command : categoryCommands) {
                    JMenuItem commandItem = createToolMenuItem(command, httpRequest, httpResponse, event);
                    categoryMenu.add(commandItem);
                }
                
                favoriteMenu.add(categoryMenu);
            }
            
            return favoriteMenu;
            
        } catch (Exception ex) {
            ApiManager.getInstance().getApi().logging().logToError("创建收藏菜单失败: " + ex.getMessage());
            return null;
        }
    }
    
    /**
     * 处理Arsenal菜单项点击事件
     * @param event 上下文菜单事件
     */
    private void handleArsenalAction(ContextMenuEvent event) {
        try {
            // 设置当前上下文菜单事件
            currentContextMenuEvent = event;
            
            // 获取选中的HTTP消息
            HttpRequest httpRequest = getHttpRequestFromEvent(event);
            HttpResponse httpResponse = getHttpResponseFromEvent(event);
            
            // 获取所有选中的请求
            List<HttpRequest> allSelectedRequests = getAllSelectedRequests(event);
            
            // 记录选中的请求数量
            ApiManager.getInstance().getApi().logging().logToOutput(
                String.format("BpArsenal: 选中了 %d 个HTTP请求", allSelectedRequests.size())
            );

            if (httpRequest != null) {
                // 创建并显示Arsenal工具对话框
                SwingUtilities.invokeLater(() -> {
                    try {
                        ArsenalDialog dialog = new ArsenalDialog(httpRequest, httpResponse, allSelectedRequests);

                        // 获取Burp Suite主窗口并将对话框居中显示
                        Frame burpFrame = ApiManager.getInstance().getApi().userInterface().swingUtils().suiteFrame();
                        if (burpFrame != null) {
                            dialog.setLocationRelativeTo(null);
                            // 确保对话框始终在最前面
                            dialog.setAlwaysOnTop(true);
                            // 显示对话框后立即取消置顶，避免影响用户操作其他应用
                            SwingUtilities.invokeLater(() -> {
                                dialog.setAlwaysOnTop(true);
                                dialog.toFront();
                                dialog.requestFocus();
                            });
                        }

                        dialog.setVisible(true);

                        // 再次确保对话框在最前面（解决某些系统的窗口管理问题）
                        dialog.bringToFront();

                        if (ApiManager.getInstance().isInitialized()) {
                            ApiManager.getInstance().getApi().logging().logToOutput(
                                "BpArsenal: Arsenal工具对话框已在Burp Suite主窗口上方居中显示"
                            );
                        }

                    } catch (Exception ex) {
                        ApiManager.getInstance().getApi().logging().logToError(
                            "BpArsenal: 创建Arsenal对话框失败: " + ex.getMessage()
                        );

                        // 显示错误提示
                        JOptionPane.showMessageDialog(
                            null,
                            "打开Arsenal对话框失败:\n" + ex.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                });

            } else {
                ApiManager.getInstance().getApi().logging().logToError("无法获取HTTP请求数据");
            }
        } catch (Exception ex) {
            ApiManager.getInstance().getApi().logging().logToError("打开Arsenal对话框失败: " + ex.getMessage());
        }
    }
    

    
    /**
     * 按分类分组命令
     */
    private Map<String, List<HttpToolCommand>> groupCommandsByCategory(List<HttpToolCommand> commands) {
        Map<String, List<HttpToolCommand>> grouped = new HashMap<>();
        
        for (HttpToolCommand command : commands) {
            String category = command.getCategory();
            if (category == null || category.trim().isEmpty()) {
                category = "未分类";
            }
            
            grouped.computeIfAbsent(category, k -> new ArrayList<>()).add(command);
        }
        
        return grouped;
    }
    
    /**
     * 创建分类图标
     */
    private Icon createCategoryIcon(String category) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                // 根据分类显示不同颜色的图标
                Color iconColor = getCategoryColor(category);
                g.setColor(iconColor);
                g.fillOval(x + 2, y + 2, 8, 8);
                g.setColor(iconColor.darker());
                g.drawOval(x + 2, y + 2, 8, 8);
            }
            
            @Override
            public int getIconWidth() { return 12; }
            
            @Override
            public int getIconHeight() { return 12; }
        };
    }
    
    /**
     * 根据分类获取对应颜色
     */
    private Color getCategoryColor(String category) {
        switch (category.toLowerCase()) {
            case "sql注入":
            case "sql":
                return new Color(220, 53, 69);    // 红色
            case "xss":
            case "跨站脚本":
                return new Color(255, 193, 7);    // 黄色
            case "扫描工具":
            case "扫描":
                return new Color(40, 167, 69);    // 绿色
            case "爆破工具":
            case "爆破":
                return new Color(255, 87, 34);    // 橙色
            case "漏洞利用":
            case "exploit":
                return new Color(156, 39, 176);   // 紫色
            case "信息收集":
            case "reconnaissance":
                return new Color(3, 169, 244);    // 蓝色
            default:
                return new Color(108, 117, 125);  // 灰色
        }
    }
    
    /**
     * 创建工具菜单项
     */
    private JMenuItem createToolMenuItem(HttpToolCommand command, HttpRequest httpRequest, HttpResponse httpResponse, ContextMenuEvent event) {
        String displayText = String.format("%s - %s", 
            command.getDisplayName(), 
            truncateCommand(command.getCommand(), 60));
        
        JMenuItem menuItem = new JMenuItem(displayText);
        menuItem.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        menuItem.setToolTipText(String.format("<html><b>%s</b><br/>%s<br/><i>分类: %s</i></html>", 
            command.getDisplayName(), 
            command.getCommand(),
            command.getCategory()));
        
        // 添加点击事件处理器
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 设置当前上下文菜单事件
                currentContextMenuEvent = event;
                executeToolCommand(command, httpRequest, httpResponse);
            }
        });
        
        return menuItem;
    }
    
    /**
     * 获取所有收藏的工具命令
     */
    private List<HttpToolCommand> getFavoriteToolCommands() {
        try {
            List<HttpToolCommand> allCommands = ToolController.getInstance().getAllToolCommands();
            return allCommands.stream()
                    .filter(HttpToolCommand::isFavor)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            ApiManager.getInstance().getApi().logging().logToError("获取收藏工具失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 截断命令显示
     */
    private String truncateCommand(String command, int maxLength) {
        if (command == null) return "";
        if (command.length() <= maxLength) return command;
        return command.substring(0, maxLength) + "...";
    }
    
    /**
     * 执行工具命令
     */
    private void executeToolCommand(HttpToolCommand toolCommand, HttpRequest httpRequest, HttpResponse httpResponse) {
        try {
            // 记录执行信息
            String toolName = toolCommand.getToolName();
            ApiManager.getInstance().getApi().logging().logToOutput(
                "BpArsenal: 执行收藏工具 - " + toolName
            );
            
            // 渲染命令
            String renderedCommand = generateRenderedCommand(toolCommand, httpRequest, httpResponse);
            
            if (renderedCommand == null || renderedCommand.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, 
                    "命令渲染失败或为空，无法执行", 
                    "执行失败", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 执行命令
            executeCommandViaScript(renderedCommand, toolName);
            
        } catch (Exception e) {
            String errorMsg = "执行工具命令失败: " + e.getMessage();
            ApiManager.getInstance().getApi().logging().logToError(errorMsg);
            JOptionPane.showMessageDialog(null, errorMsg, "执行失败", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 生成渲染后的命令
     */
    private String generateRenderedCommand(HttpToolCommand toolCommand, HttpRequest httpRequest, HttpResponse httpResponse) {
        try {
            String command = toolCommand.getCommand();
            if (command == null || command.isEmpty()) {
                return "";
            }
            
            // 获取多个选中的请求（用于httpList变量）
            ContextMenuEvent currentEvent = getCurrentContextMenuEvent();
            List<HttpRequest> allSelectedRequests = getAllSelectedRequests(currentEvent);
            
            if (allSelectedRequests.isEmpty() && httpRequest != null) {
                allSelectedRequests.add(httpRequest);
            }
            
            // 使用第一个请求作为主要请求（向后兼容）
            HttpRequest primaryRequest = allSelectedRequests.isEmpty() ? httpRequest : allSelectedRequests.get(0);
            
            if (primaryRequest == null) {
                return command;
            }
            
            // 使用AdvancedHttpParser解析主要请求
            AdvancedHttpParser advancedParser = new AdvancedHttpParser();
            Map<String, String> requestVariables = advancedParser.parseRequest(primaryRequest);
            
            // 解析响应（如果有）
            Map<String, String> responseVariables = new HashMap<>();
            if (httpResponse != null) {
                responseVariables = advancedParser.parseResponse(httpResponse);
            }
            
            // 合并变量映射
            Map<String, String> allVariables = new HashMap<>();
            allVariables.putAll(requestVariables);
            allVariables.putAll(responseVariables);
            
            // 处理httpList相关变量
            addHttpListVariables(allVariables, allSelectedRequests);
            
            // 进行变量替换
            return replaceVariables(command, allVariables);
            
        } catch (Exception e) {
            ApiManager.getInstance().getApi().logging().logToError("命令渲染失败: " + e.getMessage());
            return toolCommand.getCommand();
        }
    }
    
    /**
     * 添加httpList相关变量
     * @param variables 变量映射
     * @param allRequests 所有选中的HTTP请求
     */
    private void addHttpListVariables(Map<String, String> variables, List<HttpRequest> allRequests) {
        try {
            // 添加请求数量
            variables.put("httpList.count", String.valueOf(allRequests.size()));
            
            // 处理URLs
            List<String> urls = allRequests.stream()
                .map(request -> {
                    try {
                        return request.url();
                    } catch (Exception e) {
                        ApiManager.getInstance().getApi().logging().logToError(
                            "获取请求URL失败: " + e.getMessage());
                        return null;
                    }
                })
                .filter(url -> url != null && !url.isEmpty())
                .distinct() // 去重
                .collect(Collectors.toList());
            
            // 创建URLs临时文件
            if (!urls.isEmpty()) {
                String urlsFilePath = createTemporaryUrlsFile(urls);
                variables.put("httpList.requests.urls", urlsFilePath);
                
                // 添加其他httpList变量
                variables.put("httpList.requests.urls.count", String.valueOf(urls.size()));
                variables.put("httpList.requests.urls.list", String.join("\n", urls));
                variables.put("httpList.requests.urls.comma", String.join(",", urls));
                variables.put("httpList.requests.urls.space", String.join(" ", urls));
            }
            
            // 处理主机名
            List<String> hosts = allRequests.stream()
                .map(request -> {
                    try {
                        return request.httpService().host();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(host -> host != null && !host.isEmpty())
                .distinct()
                .collect(Collectors.toList());
            
            if (!hosts.isEmpty()) {
                String hostsFilePath = createTemporaryHostsFile(hosts);
                variables.put("httpList.requests.hosts", hostsFilePath);
                variables.put("httpList.requests.hosts.count", String.valueOf(hosts.size()));
                variables.put("httpList.requests.hosts.list", String.join("\n", hosts));
                variables.put("httpList.requests.hosts.comma", String.join(",", hosts));
            }
            
            // 处理端口
            List<String> ports = allRequests.stream()
                .map(request -> {
                    try {
                        return String.valueOf(request.httpService().port());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(port -> port != null)
                .distinct()
                .collect(Collectors.toList());
            
            if (!ports.isEmpty()) {
                variables.put("httpList.requests.ports.list", String.join(",", ports));
                variables.put("httpList.requests.ports.count", String.valueOf(ports.size()));
            }
            
            // 处理路径
            List<String> paths = allRequests.stream()
                .map(request -> {
                    try {
                        return request.path();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(path -> path != null && !path.isEmpty())
                .distinct()
                .collect(Collectors.toList());
            
            if (!paths.isEmpty()) {
                String pathsFilePath = createTemporaryPathsFile(paths);
                variables.put("httpList.requests.paths", pathsFilePath);
                variables.put("httpList.requests.paths.count", String.valueOf(paths.size()));
                variables.put("httpList.requests.paths.list", String.join("\n", paths));
            }
            
            // 添加协议信息
            List<String> protocols = allRequests.stream()
                .map(request -> {
                    try {
                        return request.httpService().secure() ? "https" : "http";
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(protocol -> protocol != null)
                .distinct()
                .collect(Collectors.toList());
            
            if (!protocols.isEmpty()) {
                variables.put("httpList.requests.protocols.list", String.join(",", protocols));
            }
            
            // 统计信息
            variables.put("httpList.summary", String.format(
                "总请求数: %d, 唯一URL: %d, 唯一主机: %d", 
                allRequests.size(), urls.size(), hosts.size()));
                
        } catch (Exception e) {
            ApiManager.getInstance().getApi().logging().logToError(
                "添加httpList变量失败: " + e.getMessage());
            variables.put("httpList.error", e.getMessage());
        }
    }
    
    /**
     * 创建包含URLs的临时文件
     * @param urls URL列表
     * @return 临时文件路径
     */
    private String createTemporaryUrlsFile(List<String> urls) throws IOException {
        return createTemporaryListFile(urls, "bparsenal_urls_", ".txt");
    }
    
    /**
     * 创建包含主机名的临时文件
     * @param hosts 主机名列表
     * @return 临时文件路径
     */
    private String createTemporaryHostsFile(List<String> hosts) throws IOException {
        return createTemporaryListFile(hosts, "bparsenal_hosts_", ".txt");
    }
    
    /**
     * 创建包含路径的临时文件
     * @param paths 路径列表
     * @return 临时文件路径
     */
    private String createTemporaryPathsFile(List<String> paths) throws IOException {
        return createTemporaryListFile(paths, "bparsenal_paths_", ".txt");
    }
    
    /**
     * 创建包含列表数据的临时文件
     * @param items 数据项列表
     * @param prefix 文件名前缀
     * @param suffix 文件名后缀
     * @return 临时文件路径
     */
    private String createTemporaryListFile(List<String> items, String prefix, String suffix) throws IOException {
        try {
            // 获取临时目录
            String tempDir = getExtensionPath();
            if (tempDir == null) {
                tempDir = System.getProperty("java.io.tmpdir");
            }
            
            // 创建临时文件
            File tempFile = new File(tempDir, prefix + System.currentTimeMillis() + suffix);
            
            // 写入数据
            try (FileWriter writer = new FileWriter(tempFile)) {
                for (String item : items) {
                    writer.write(item);
                    writer.write(System.lineSeparator());
                }
            }
            
            // 设置文件在程序退出时删除
            tempFile.deleteOnExit();
            
            // 记录创建的临时文件
            ApiManager.getInstance().getApi().logging().logToOutput(
                String.format("BpArsenal: 创建临时文件 %s，包含 %d 项数据", 
                    tempFile.getAbsolutePath(), items.size()));
            
            return tempFile.getAbsolutePath();
            
        } catch (Exception e) {
            throw new IOException("创建临时文件失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取所有选中的HTTP请求
     * @param event 上下文菜单事件
     * @return HTTP请求列表
     */
    private List<HttpRequest> getAllSelectedRequests(ContextMenuEvent event) {
        List<HttpRequest> requests = new ArrayList<>();
        
        try {
            if (event != null && event.selectedRequestResponses() != null) {
                for (burp.api.montoya.http.message.HttpRequestResponse requestResponse : event.selectedRequestResponses()) {
                    if (requestResponse != null && requestResponse.request() != null) {
                        requests.add(requestResponse.request());
                    }
                }
            }
        } catch (Exception e) {
            ApiManager.getInstance().getApi().logging().logToError(
                "获取选中请求列表失败: " + e.getMessage());
        }
        
        return requests;
    }
    
    // 添加一个成员变量来存储当前的上下文菜单事件
    private ContextMenuEvent currentContextMenuEvent;
    
    /**
     * 获取当前的上下文菜单事件
     * @return 当前事件
     */
    private ContextMenuEvent getCurrentContextMenuEvent() {
        return currentContextMenuEvent;
    }
    
    /**
     * 替换命令中的变量
     */
    private String replaceVariables(String command, Map<String, String> variables) {
        String result = command;
        
        // 按变量名长度排序，优先替换长变量名
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
     */
    private String escapeCommandValue(String value) {
        if (value == null) {
            return "";
        }
        
        String escaped = value.replace("\n", " ")
                             .replace("\r", " ")
                             .replace("\t", " ");
        
        if (escaped.contains(" ")) {
            if (ToolExecutor.isWindows()) {
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
     * 通过临时脚本执行命令
     */
    private void executeCommandViaScript(String command, String toolName) {
        try {
            String extensionPath = getExtensionPath();
            if (extensionPath == null) {
                throw new Exception("无法获取插件路径");
            }
            
            java.io.File scriptFile = createTemporaryScript(command, extensionPath);
            executeScript(scriptFile, toolName);
            
        } catch (Exception e) {
            ApiManager.getInstance().getApi().logging().logToError("脚本执行失败: " + e.getMessage());
            throw new RuntimeException("脚本执行失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取插件路径
     */
    private String getExtensionPath() {
        try {
            if (ApiManager.getInstance().isInitialized()) {
                String filename = ApiManager.getInstance().getApi().extension().filename();
                if (filename != null && !filename.isEmpty()) {
                    java.io.File file = new java.io.File(filename);
                    return file.getParent();
                }
            }
            return System.getProperty("java.io.tmpdir");
        } catch (Exception e) {
            return System.getProperty("java.io.tmpdir");
        }
    }
    
    /**
     * 创建临时脚本文件
     */
    private java.io.File createTemporaryScript(String command, String extensionPath) throws Exception {
        java.io.File scriptFile;
        String scriptContent;
        
        if (ToolExecutor.isWindows()) {
            scriptFile = new java.io.File(extensionPath, "bparsenal_favorite_" + System.currentTimeMillis() + ".bat");
            scriptContent = generateBatchScript(command);
        } else {
            scriptFile = new java.io.File(extensionPath, "bparsenal_favorite_" + System.currentTimeMillis() + ".sh");
            scriptContent = generateShellScript(command);
        }
        
        String encoding = ToolExecutor.isWindows() ? "GBK" : "UTF-8";
        try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(scriptFile), encoding)) {
            writer.write(scriptContent);
        } catch (java.io.UnsupportedEncodingException e) {
            try (java.io.FileWriter writer = new java.io.FileWriter(scriptFile)) {
                writer.write(scriptContent);
            }
        }
        
        if (!ToolExecutor.isWindows()) {
            scriptFile.setExecutable(true);
        }
        
        scriptFile.deleteOnExit();
        return scriptFile;
    }
    
    /**
     * 生成Windows批处理脚本
     */
    private String generateBatchScript(String command) {
        StringBuilder script = new StringBuilder();
        script.append("@echo off\r\n");
        script.append("title BpArsenal Favorite Tool Execution\r\n");
        script.append("color 0A\r\n");
        script.append("echo.\r\n");
        script.append("echo ========================================\r\n");
        script.append("echo BpArsenal 收藏工具执行\r\n");
        script.append("echo Time: %date% %time%\r\n");
        script.append("echo ========================================\r\n");
        script.append("echo.\r\n");
        script.append("echo Executing favorite command:\r\n");
        
        String displayCommand = command.replace("%", "%%").replace("^", "^^");
        script.append("echo ").append(displayCommand).append("\r\n");
        script.append("echo.\r\n");
        script.append("echo ========================================\r\n");
        script.append("echo.\r\n");
        
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
     * 生成Linux Shell脚本
     */
    private String generateShellScript(String command) {
        StringBuilder script = new StringBuilder();
        script.append("#!/bin/bash\n");
        script.append("# BpArsenal 收藏工具执行脚本\n\n");
        
        script.append("echo \"========================================\"\n");
        script.append("echo \"BpArsenal 收藏工具执行\"\n");
        script.append("echo \"时间: $(date)\"\n");
        script.append("echo \"========================================\"\n");
        script.append("echo\n");
        script.append("echo \"正在执行收藏命令:\"\n");
        script.append("echo \"").append(command.replace("\"", "\\\"")).append("\"\n");
        script.append("echo\n");
        script.append("echo \"========================================\"\n");
        script.append("echo\n");
        
        script.append(command).append("\n");
        script.append("EXIT_CODE=$?\n");
        
        script.append("\n");
        script.append("echo\n");
        script.append("echo \"========================================\"\n");
        script.append("echo \"命令执行完成，退出码: $EXIT_CODE\"\n");
        script.append("echo \"========================================\"\n");
        script.append("read -p \"按回车键继续...\"\n");
        
        return script.toString();
    }
    
    /**
     * 执行脚本文件
     */
    private void executeScript(java.io.File scriptFile, String toolName) {
        try {
            ProcessBuilder processBuilder = null;
            
            if (ToolExecutor.isWindows()) {
                processBuilder = new ProcessBuilder("cmd", "/c", "start", "\"BpArsenal Favorite Tool\"", 
                    "cmd", "/k", "\"" + scriptFile.getAbsolutePath() + "\"");
            } else {
                String[] terminalCommands = {
                    "x-terminal-emulator", "-e", "bash", scriptFile.getAbsolutePath(),
                    "gnome-terminal", "--", "bash", scriptFile.getAbsolutePath(),
                    "xterm", "-e", "bash", scriptFile.getAbsolutePath(),
                    "konsole", "-e", "bash", scriptFile.getAbsolutePath()
                };
                
                for (int i = 0; i < terminalCommands.length; i += 3) {
                    try {
                        processBuilder = new ProcessBuilder(terminalCommands[i], terminalCommands[i+1], 
                            terminalCommands[i+2], scriptFile.getAbsolutePath());
                        break;
                    } catch (Exception e) {
                        if (i + 3 >= terminalCommands.length) {
                            processBuilder = new ProcessBuilder("bash", scriptFile.getAbsolutePath());
                        }
                    }
                }
                
                if (processBuilder == null) {
                    processBuilder = new ProcessBuilder("bash", scriptFile.getAbsolutePath());
                }
            }
            
            processBuilder.directory(scriptFile.getParentFile());
            Process process = processBuilder.start();
            
            ApiManager.getInstance().getApi().logging().logToOutput(
                "BpArsenal: 收藏工具脚本已启动 - " + toolName
            );
            
            // 异步等待进程完成并清理
            new Thread(() -> {
                try {
                    process.waitFor();
                    Thread.sleep(5000);
                    if (scriptFile.exists()) {
                        scriptFile.delete();
                    }
                } catch (Exception e) {
                    // 忽略清理异常
                }
            }).start();
            
        } catch (Exception e) {
            throw new RuntimeException("脚本启动失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从上下文菜单事件中获取HTTP请求
     * @param event 上下文菜单事件
     * @return HTTP请求对象，如果无法获取则返回null
     */
    private HttpRequest getHttpRequestFromEvent(ContextMenuEvent event) {
        try {
            if (event.messageEditorRequestResponse().isPresent()) {
                return event.messageEditorRequestResponse().get().requestResponse().request();
            }
            
            if (event.selectedRequestResponses() != null && !event.selectedRequestResponses().isEmpty()) {
                return event.selectedRequestResponses().get(0).request();
            }
        } catch (Exception e) {
            ApiManager.getInstance().getApi().logging().logToError("获取HTTP请求失败: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 从上下文菜单事件中获取HTTP响应
     * @param event 上下文菜单事件
     * @return HTTP响应对象，如果无法获取则返回null
     */
    private HttpResponse getHttpResponseFromEvent(ContextMenuEvent event) {
        try {
            if (event.messageEditorRequestResponse().isPresent()) {
                return event.messageEditorRequestResponse().get().requestResponse().response();
            }
            
            if (event.selectedRequestResponses() != null && !event.selectedRequestResponses().isEmpty()) {
                return event.selectedRequestResponses().get(0).response();
            }
        } catch (Exception e) {
            ApiManager.getInstance().getApi().logging().logToError("获取HTTP响应失败: " + e.getMessage());
        }
        
        return null;
    }
} 