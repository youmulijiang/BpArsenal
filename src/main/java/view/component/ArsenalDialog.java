package view.component;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import controller.ToolController;
import executor.ToolExecutor;
import executor.AdvancedHttpParser;
import executor.BasicHttpParser;
import manager.ApiManager;
import manager.ConfigManager;
import model.Config;
import model.HttpTool;
import model.HttpToolCommand;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Arsenal工具对话框
 * 显示工具列表、命令预览和执行结果，支持筛选功能
 */
public class ArsenalDialog extends JDialog {
    
    private JTable toolTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private JTextField toolNameFilterField;
    private JComboBox<String> categoryFilterCombo;
    private JButton clearFilterButton;
    
    // 修改为选项卡面板
    private JTabbedPane commandTabbedPane;
    private JTextArea originalCommandArea;  // 原始命令（未渲染）
    private JTextArea renderedCommandArea;  // 渲染后的命令
    private JTextArea variablesPreviewArea; // 变量预览
    
    private JTextArea commandResultArea;
    private JButton runButton;  // 统一的运行按钮
    private JButton refreshVariablesButton; // 刷新变量按钮
    private JScrollPane resultScrollPane;
    
    private HttpRequest httpRequest;
    private HttpResponse httpResponse;
    private List<HttpToolCommand> allToolCommands;
    private List<HttpToolCommand> filteredToolCommands;
    private HttpToolCommand selectedToolCommand;
    private Set<String> allCategories;
    
    /**
     * 构造函数
     * @param httpRequest HTTP请求对象
     * @param httpResponse HTTP响应对象
     */
    public ArsenalDialog(HttpRequest httpRequest, HttpResponse httpResponse) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
        this.allToolCommands = loadAllToolCommands();
        this.filteredToolCommands = new ArrayList<>(allToolCommands);
        this.allCategories = extractAllCategories();
        
        initializeDialog();
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        loadToolData();
    }
    
    /**
     * 初始化对话框属性
     */
    private void initializeDialog() {
        setTitle("Arsenal - 武器库");
        setSize(950, 800);  // 增加高度以适应选项卡
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setModal(false); // 非模态对话框
        
        // 设置图标
        try {
            setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());
        } catch (Exception e) {
            // 图标加载失败，忽略
        }
    }
    
    /**
     * 初始化UI组件
     */
    private void initializeComponents() {
        // 创建筛选组件
        initializeFilterComponents();
        
        // 创建工具表格
        String[] columnNames = {"工具名称", "命令", "分类"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格只读
            }
        };
        
        toolTable = new JTable(tableModel);
        toolTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        toolTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        toolTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        
        // 设置表格行高
        toolTable.setRowHeight(25);
        
        // 设置列宽
        toolTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        toolTable.getColumnModel().getColumn(1).setPreferredWidth(400);
        toolTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        
        // 创建表格排序器
        tableSorter = new TableRowSorter<>(tableModel);
        toolTable.setRowSorter(tableSorter);
        
        // 创建命令选项卡面板
        initializeCommandTabs();
        
        // 创建运行按钮
        runButton = new JButton("Run");
        runButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        runButton.setBackground(new Color(40, 167, 69));
        runButton.setForeground(Color.WHITE);
        runButton.setEnabled(false);
        runButton.setPreferredSize(new Dimension(100, 30));
        
        // 创建刷新变量按钮
        refreshVariablesButton = new JButton("刷新变量");
        refreshVariablesButton.setFont(new Font("微软雅黑", Font.BOLD, 11));
        refreshVariablesButton.setBackground(new Color(0, 123, 255));
        refreshVariablesButton.setForeground(Color.WHITE);
        refreshVariablesButton.setEnabled(false);
        refreshVariablesButton.setPreferredSize(new Dimension(100, 30));
        
        // 创建执行历史文本框 - 修改为白色背景
        commandResultArea = new JTextArea(8, 50);
        commandResultArea.setEditable(false);
        commandResultArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));  // 使用支持中文的字体
        commandResultArea.setBackground(Color.WHITE);  // 修改为白色背景
        commandResultArea.setForeground(Color.BLACK);  // 修改为黑色文字
        commandResultArea.setBorder(BorderFactory.createTitledBorder("执行历史"));  // 修改标题
        commandResultArea.setText("点击Run按钮执行命令，命令将在新窗口中运行...\n");
        
        resultScrollPane = new JScrollPane(commandResultArea);
        resultScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }
    
    /**
     * 初始化命令选项卡
     */
    private void initializeCommandTabs() {
        commandTabbedPane = new JTabbedPane();
        commandTabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        
        // 原始命令选项卡
        originalCommandArea = new JTextArea(5, 50);
        originalCommandArea.setEditable(true);  // 可编辑
        originalCommandArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        originalCommandArea.setBackground(new Color(255, 255, 240));  // 浅黄色背景
        originalCommandArea.setLineWrap(true);
        originalCommandArea.setWrapStyleWord(true);
        originalCommandArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane originalScrollPane = new JScrollPane(originalCommandArea);
        originalScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        originalScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // 渲染命令选项卡
        renderedCommandArea = new JTextArea(5, 50);
        renderedCommandArea.setEditable(true);  // 可编辑
        renderedCommandArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        renderedCommandArea.setBackground(new Color(240, 255, 240));  // 浅绿色背景
        renderedCommandArea.setLineWrap(true);
        renderedCommandArea.setWrapStyleWord(true);
        renderedCommandArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane renderedScrollPane = new JScrollPane(renderedCommandArea);
        renderedScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        renderedScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // 变量预览选项卡
        variablesPreviewArea = new JTextArea(5, 50);
        variablesPreviewArea.setEditable(false);  // 只读
        variablesPreviewArea.setFont(new Font("Consolas", Font.PLAIN, 10));
        variablesPreviewArea.setBackground(new Color(248, 248, 255));  // 浅蓝色背景
        variablesPreviewArea.setLineWrap(true);
        variablesPreviewArea.setWrapStyleWord(true);
        variablesPreviewArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane variablesScrollPane = new JScrollPane(variablesPreviewArea);
        variablesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        variablesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // 添加选项卡
        commandTabbedPane.addTab("原始命令", originalScrollPane);
        commandTabbedPane.addTab("渲染命令", renderedScrollPane);
        commandTabbedPane.addTab("变量预览", variablesScrollPane);
        
        // 设置选项卡提示
        commandTabbedPane.setToolTipTextAt(0, "显示未经变量替换的原始命令模板，可以手动编辑");
        commandTabbedPane.setToolTipTextAt(1, "显示经过变量替换的命令，可以手动编辑后执行");
        commandTabbedPane.setToolTipTextAt(2, "显示当前HTTP请求/响应解析出的所有可用变量");
    }
    
    /**
     * 初始化筛选组件
     */
    private void initializeFilterComponents() {
        // 工具名称筛选框
        toolNameFilterField = new JTextField(15);
        toolNameFilterField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        toolNameFilterField.setBorder(BorderFactory.createTitledBorder("工具名称"));
        
        // 分类筛选下拉框
        categoryFilterCombo = new JComboBox<>();
        categoryFilterCombo.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        categoryFilterCombo.addItem("全部分类");
        
        // 清除筛选按钮
        clearFilterButton = new JButton("清除筛选");
        clearFilterButton.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        clearFilterButton.setPreferredSize(new Dimension(80, 25));
    }
    
    /**
     * 布局组件
     */
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // 顶部：筛选面板
        JPanel filterPanel = createFilterPanel();
        add(filterPanel, BorderLayout.NORTH);
        
        // 中部：工具表格
        JScrollPane tableScrollPane = new JScrollPane(toolTable);
        tableScrollPane.setPreferredSize(new Dimension(930, 200));
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("工具列表"));
        
        // 命令预览选项卡和运行按钮面板
        JPanel middlePanel = new JPanel(new BorderLayout());
        
        // 选项卡面板
        commandTabbedPane.setPreferredSize(new Dimension(930, 150));
        middlePanel.add(commandTabbedPane, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.add(refreshVariablesButton);
        buttonPanel.add(runButton);
        middlePanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 组合中部面板
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(tableScrollPane, BorderLayout.NORTH);
        centerPanel.add(middlePanel, BorderLayout.CENTER);
        
        // 底部：执行历史
        resultScrollPane.setPreferredSize(new Dimension(930, 180));
        
        // 添加到主面板
        add(centerPanel, BorderLayout.CENTER);
        add(resultScrollPane, BorderLayout.SOUTH);
    }
    
    /**
     * 创建筛选面板
     * @return 筛选面板
     */
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder("筛选条件"));
        filterPanel.setPreferredSize(new Dimension(930, 80));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 工具名称标签和输入框
        gbc.gridx = 0; gbc.gridy = 0;
        filterPanel.add(new JLabel("工具名称:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.4;
        filterPanel.add(toolNameFilterField, gbc);
        
        // 分类标签和下拉框
        gbc.gridx = 2; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        filterPanel.add(new JLabel("分类:"), gbc);
        
        gbc.gridx = 3; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.3;
        filterPanel.add(categoryFilterCombo, gbc);
        
        // 清除筛选按钮
        gbc.gridx = 4; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        filterPanel.add(clearFilterButton, gbc);
        
        // 占位符，推送其他组件到左侧
        gbc.gridx = 5; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        filterPanel.add(Box.createHorizontalGlue(), gbc);
        
        return filterPanel;
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 表格选择事件
        toolTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = toolTable.getSelectedRow();
                if (selectedRow >= 0) {
                    // 获取实际的工具索引（考虑筛选和排序）
                    int modelRow = toolTable.convertRowIndexToModel(selectedRow);
                    if (modelRow >= 0 && modelRow < filteredToolCommands.size()) {
                        selectedToolCommand = filteredToolCommands.get(modelRow);
                        updateCommandPreview();
                        runButton.setEnabled(true);
                        refreshVariablesButton.setEnabled(true);
                    } else {
                        selectedToolCommand = null;
                        clearCommandAreas();
                        runButton.setEnabled(false);
                        refreshVariablesButton.setEnabled(false);
                    }
                } else {
                    selectedToolCommand = null;
                    clearCommandAreas();
                    runButton.setEnabled(false);
                    refreshVariablesButton.setEnabled(false);
                }
            }
        });
        
        // 运行按钮点击事件
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executeSelectedCommand();
            }
        });
        
        // 刷新变量按钮点击事件
        refreshVariablesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateVariablesPreview();
            }
        });
        
        // 工具名称筛选事件
        toolNameFilterField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
        });
        
        // 分类筛选事件
        categoryFilterCombo.addActionListener(e -> applyFilters());
        
        // 清除筛选按钮事件
        clearFilterButton.addActionListener(e -> clearFilters());
        
        // 选项卡切换事件（可选，用于同步编辑）
        commandTabbedPane.addChangeListener(e -> {
            // 可以在这里添加选项卡切换时的逻辑
        });
    }
    
    /**
     * 清空命令文本区域
     */
    private void clearCommandAreas() {
        originalCommandArea.setText("");
        renderedCommandArea.setText("");
        variablesPreviewArea.setText("");
    }
    
    /**
     * 更新变量预览
     */
    private void updateVariablesPreview() {
        if (httpRequest == null) {
            variablesPreviewArea.setText("# 无HTTP请求数据\n请在Burp Suite中拦截或选择一个HTTP请求，然后右键选择Arsenal工具。");
            return;
        }
        
        try {
            // 使用AdvancedHttpParser解析请求
            AdvancedHttpParser advancedParser = new AdvancedHttpParser();
            Map<String, String> requestVariables = advancedParser.parseRequest(httpRequest);
            
            // 解析响应（如果有）
            Map<String, String> responseVariables = new HashMap<>();
            if (httpResponse != null) {
                responseVariables = advancedParser.parseResponse(httpResponse);
            }
            
            // 生成变量预览文本
            StringBuilder preview = new StringBuilder();
            preview.append("# HTTP请求变量预览\n");
            preview.append("# 用法: 在命令中使用 %变量名% 进行替换\n\n");
            
            // 按分类显示变量
            addVariablesByCategory(preview, "基础信息", requestVariables, 
                new String[]{"http.request.url", "http.request.method", "http.request.host", 
                           "http.request.port", "http.request.path", "http.request.protocol"});
            
            addVariablesByCategory(preview, "头部信息", requestVariables, 
                new String[]{"http.request.headers.user-agent", "http.request.headers.cookie", 
                           "http.request.headers.referer", "http.request.headers.authorization"});
            
            addVariablesByCategory(preview, "请求体信息", requestVariables,
                new String[]{"http.request.body", "body.type", "body.json.field.count"});
            
            addVariablesByCategory(preview, "文件信息", requestVariables,
                new String[]{"file.name", "file.extension", "path.directory"});
            
            addVariablesByCategory(preview, "认证信息", requestVariables,
                new String[]{"auth.type", "auth.username", "auth.password", "auth.token"});
            
            // 响应变量
            if (!responseVariables.isEmpty()) {
                preview.append("\n## HTTP响应变量\n");
                addVariablesByCategory(preview, "响应信息", responseVariables,
                    new String[]{"http.response.status", "http.response.headers.content-type", 
                               "response.format", "response.html.title"});
            }
            
            // 显示所有可用变量
            preview.append("\n## 所有可用变量 (").append(requestVariables.size() + responseVariables.size()).append(" 个)\n");
            Map<String, String> allVariables = new HashMap<>();
            allVariables.putAll(requestVariables);
            allVariables.putAll(responseVariables);
            
            allVariables.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String value = entry.getValue();
                    if (value.length() > 50) {
                        value = value.substring(0, 50) + "...";
                    }
                    preview.append(String.format("%%%-40s%% = %s\n", entry.getKey(), value));
                });
            
            variablesPreviewArea.setText(preview.toString());
            variablesPreviewArea.setCaretPosition(0);
            
        } catch (Exception e) {
            String errorMsg = "变量解析失败: " + e.getMessage();
            variablesPreviewArea.setText("# 错误\n" + errorMsg);
            ApiManager.getInstance().getApi().logging().logToError(errorMsg);
        }
    }
    
    /**
     * 添加指定分类的变量到预览
     * @param preview 预览文本构建器
     * @param category 分类名称
     * @param variables 变量映射
     * @param keys 要显示的变量键
     */
    private void addVariablesByCategory(StringBuilder preview, String category, 
                                      Map<String, String> variables, String[] keys) {
        preview.append("\n## ").append(category).append("\n");
        for (String key : keys) {
            String value = variables.get(key);
            if (value != null && !value.isEmpty()) {
                if (value.length() > 50) {
                    value = value.substring(0, 50) + "...";
                }
                preview.append(String.format("%%%-40s%% = %s\n", key, value));
            }
        }
    }
    
    /**
     * 应用筛选条件
     */
    private void applyFilters() {
        String toolNameFilter = toolNameFilterField.getText().trim().toLowerCase();
        String categoryFilter = (String) categoryFilterCombo.getSelectedItem();
        
        filteredToolCommands.clear();
        
        for (HttpToolCommand toolCommand : allToolCommands) {
            boolean matchesName = true;
            boolean matchesCategory = true;
            
            // 工具名称筛选
            if (!toolNameFilter.isEmpty()) {
                String toolName = toolCommand.getToolName() != null ? toolCommand.getToolName().toLowerCase() : "";
                matchesName = toolName.contains(toolNameFilter);
            }
            
            // 分类筛选
            if (categoryFilter != null && !categoryFilter.equals("全部分类")) {
                String toolCategory = toolCommand.getCategory();
                matchesCategory = categoryFilter.equals(toolCategory);
            }
            
            if (matchesName && matchesCategory) {
                filteredToolCommands.add(toolCommand);
            }
        }
        
        loadToolData();
    }
    
    /**
     * 清除筛选条件
     */
    private void clearFilters() {
        toolNameFilterField.setText("");
        categoryFilterCombo.setSelectedIndex(0);
        filteredToolCommands.clear();
        filteredToolCommands.addAll(allToolCommands);
        loadToolData();
    }
    
    /**
     * 提取所有分类
     * @return 分类集合
     */
    private Set<String> extractAllCategories() {
        Set<String> categories = new HashSet<>();
        
        try {
            Config config = ConfigManager.getInstance().getConfig();
            if (config.getHttpTool() != null) {
                for (Config.HttpToolCategory category : config.getHttpTool()) {
                    if (category.getType() != null) {
                        categories.add(category.getType());
                    }
                }
            }
        } catch (Exception e) {
            ApiManager.getInstance().getApi().logging().logToError("提取分类失败: " + e.getMessage());
        }
        
        return categories;
    }
    
    /**
     * 加载所有工具命令数据
     * @return 工具命令列表
     */
    private List<HttpToolCommand> loadAllToolCommands() {
        try {
            List<HttpToolCommand> toolCommands = ToolController.getInstance().getAllToolCommands();
            
            // 初始化分类下拉框
            SwingUtilities.invokeLater(() -> {
                categoryFilterCombo.removeAllItems();
                categoryFilterCombo.addItem("全部分类");
                
                Set<String> categories = extractAllCategories();
                for (String category : categories.stream().sorted().collect(Collectors.toList())) {
                    categoryFilterCombo.addItem(category);
                }
            });
            
            return toolCommands;
        } catch (Exception e) {
            ApiManager.getInstance().getApi().logging().logToError("加载工具数据失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 加载工具数据到表格
     */
    private void loadToolData() {
        tableModel.setRowCount(0); // 清空表格
        
        for (HttpToolCommand toolCommand : filteredToolCommands) {
            String toolName = toolCommand.getDisplayName() != null ? toolCommand.getDisplayName() : "未知工具";
            String command = toolCommand.getCommand() != null ? toolCommand.getCommand() : "";
            String category = toolCommand.getCategory() != null ? toolCommand.getCategory() : "未分类";
            
            // 截断过长的命令显示
            String displayCommand = command.length() > 50 ? 
                command.substring(0, 50) + "..." : command;
            
            tableModel.addRow(new Object[]{toolName, displayCommand, category});
        }
        
        // 自动调整列宽和重绘表格
        toolTable.revalidate();
        toolTable.repaint();
        
        // 更新筛选结果统计
        updateFilterStatus();
    }
    
    /**
     * 更新筛选状态显示
     */
    private void updateFilterStatus() {
        String title = String.format("Arsenal - 武器库 (显示 %d/%d 个命令)", 
                                    filteredToolCommands.size(), allToolCommands.size());
        setTitle(title);
    }
    
    /**
     * 获取工具分类
     * @param tool HTTP工具对象
     * @return 分类名称
     */
    private String getToolCategory(HttpTool tool) {
        try {
            Config config = ConfigManager.getInstance().getConfig();
            if (config.getHttpTool() != null) {
                for (Config.HttpToolCategory category : config.getHttpTool()) {
                    if (category.getContent() != null && category.getContent().contains(tool)) {
                        return category.getType();
                    }
                }
            }
        } catch (Exception e) {
            ApiManager.getInstance().getApi().logging().logToError("获取工具分类失败: " + e.getMessage());
        }
        return "未分类";
    }
    
    /**
     * 更新命令预览
     */
    private void updateCommandPreview() {
        if (selectedToolCommand != null) {
            try {
                // 设置原始命令（未渲染）
                String originalCommand = selectedToolCommand.getCommand() != null ? selectedToolCommand.getCommand() : "";
                originalCommandArea.setText(originalCommand);
                originalCommandArea.setCaretPosition(0);
                
                // 设置渲染后的命令
                if (httpRequest != null) {
                    String renderedCommand = generateRenderedCommand(selectedToolCommand, httpRequest);
                    renderedCommandArea.setText(renderedCommand);
                    renderedCommandArea.setCaretPosition(0);
                } else {
                    renderedCommandArea.setText("无HTTP请求数据，无法渲染变量");
                }
                
                // 更新变量预览
                updateVariablesPreview();
                
            } catch (Exception e) {
                originalCommandArea.setText("命令加载失败: " + e.getMessage());
                renderedCommandArea.setText("命令渲染失败: " + e.getMessage());
                ApiManager.getInstance().getApi().logging().logToError("命令预览更新失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 生成渲染后的命令
     * @param toolCommand HTTP工具命令
     * @param request HTTP请求
     * @return 渲染后的命令字符串
     */
    private String generateRenderedCommand(HttpToolCommand toolCommand, HttpRequest request) {
        try {
            String command = toolCommand.getCommand();
            if (command == null || command.isEmpty()) {
                return "";
            }
            
            if (request == null) {
                return command + "\n\n# 警告: 无HTTP请求数据，无法渲染变量";
            }
            
            // 使用AdvancedHttpParser解析请求，获取完整的变量映射
            AdvancedHttpParser advancedParser = new AdvancedHttpParser();
            Map<String, String> requestVariables = advancedParser.parseRequest(request);
            
            // 如果有响应数据，也进行解析
            Map<String, String> responseVariables = new HashMap<>();
            if (httpResponse != null) {
                responseVariables = advancedParser.parseResponse(httpResponse);
            }
            
            // 合并变量映射
            Map<String, String> allVariables = new HashMap<>();
            allVariables.putAll(requestVariables);
            allVariables.putAll(responseVariables);
            
            // 进行变量替换
            String renderedCommand = replaceVariables(command, allVariables);
            
            // 添加调试信息（可选）
            if (renderedCommand.equals(command)) {
                renderedCommand += "\n\n# 提示: 命令中未找到可替换的变量";
            } else {
                int variableCount = countReplacedVariables(command, allVariables);
                renderedCommand += String.format("\n\n# 已替换 %d 个变量", variableCount);
            }
            
            return renderedCommand;
            
        } catch (Exception e) {
            String errorMsg = "命令渲染失败: " + e.getMessage();
            ApiManager.getInstance().getApi().logging().logToError(errorMsg);
            return toolCommand.getCommand() + "\n\n# 错误: " + errorMsg;
        }
    }
    
    /**
     * 替换命令中的变量
     * @param command 原始命令
     * @param variables 变量映射
     * @return 替换后的命令
     */
    private String replaceVariables(String command, Map<String, String> variables) {
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
                    // 对特殊字符进行转义，避免命令执行问题
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
    private String escapeCommandValue(String value) {
        if (value == null) {
            return "";
        }
        
        // 移除可能危险的字符
        String escaped = value.replace("\n", " ")
                             .replace("\r", " ")
                             .replace("\t", " ");
        
        // 如果包含空格，在Windows下用双引号包围，在Unix下转义空格
        if (escaped.contains(" ")) {
            if (ToolExecutor.isWindows()) {
                // Windows: 用双引号包围，内部的双引号转义
                escaped = "\"" + escaped.replace("\"", "\\\"") + "\"";
            } else {
                // Unix: 转义空格和特殊字符
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
     * 统计已替换的变量数量
     * @param originalCommand 原始命令
     * @param variables 变量映射
     * @return 替换的变量数量
     */
    private int countReplacedVariables(String originalCommand, Map<String, String> variables) {
        int count = 0;
        for (String key : variables.keySet()) {
            String placeholder = "%" + key + "%";
            if (originalCommand.contains(placeholder)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 执行选中的命令
     */
    private void executeSelectedCommand() {
        // 获取当前选中的选项卡中的命令
        int selectedTab = commandTabbedPane.getSelectedIndex();
        String command;
        String commandType;
        
        if (selectedTab == 0) { // 原始命令选项卡
            command = originalCommandArea.getText();
            commandType = "原始命令";
        } else if (selectedTab == 1) { // 渲染命令选项卡
            command = renderedCommandArea.getText();
            commandType = "渲染命令";
        } else {
            JOptionPane.showMessageDialog(this, "请选择原始命令或渲染命令选项卡进行执行！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (command == null || command.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "命令不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 获取工具名称
        String toolName = selectedToolCommand != null ? selectedToolCommand.getToolName() : "手动命令";
        
        // 记录到执行历史
        addToExecutionHistory(toolName, commandType, command);
        
        // 创建并显示命令执行窗口
        CommandExecutionDialog executionDialog = new CommandExecutionDialog(this, toolName, command.trim(), commandType);
        executionDialog.setVisible(true);
    }
    
    /**
     * 添加到执行历史
     * @param toolName 工具名称
     * @param commandType 命令类型
     * @param command 执行的命令
     */
    private void addToExecutionHistory(String toolName, String commandType, String command) {
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = formatter.format(new java.util.Date());
        
        StringBuilder historyEntry = new StringBuilder();
        historyEntry.append("[").append(timestamp).append("] ");
        historyEntry.append("工具: ").append(toolName).append(" | ");
        historyEntry.append("类型: ").append(commandType).append("\n");
        historyEntry.append("命令: ").append(command).append("\n");
        historyEntry.append("状态: 已启动执行窗口\n");
        historyEntry.append(createSeparator(50)).append("\n");
        
        commandResultArea.append(historyEntry.toString());
        commandResultArea.setCaretPosition(commandResultArea.getDocument().getLength());
    }
    
    /**
     * 创建分隔符字符串
     * @param length 分隔符长度
     * @return 分隔符字符串
     */
    private String createSeparator(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append("=");
        }
        return sb.toString();
    }
    
    /**
     * 获取命令前缀设置
     * @return 命令前缀数组
     */
    private String[] getCommandPrefix() {
        try {
            // 尝试从设置面板获取命令前缀
            // 这里我们直接使用工具类的方法，避免复杂的依赖关系
            return ToolExecutor.getDefaultCommandPrefix();
        } catch (Exception e) {
            // 如果出错，使用系统默认
            return ToolExecutor.getDefaultCommandPrefix();
        }
    }
    
    /**
     * 获取系统编码
     * @return 编码字符串
     */
    private String getSystemEncoding() {
        return ToolExecutor.getSystemEncoding();
    }
} 