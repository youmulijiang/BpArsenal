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
import util.I18nManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

/**
 * Arsenal工具对话框
 * 显示工具列表、命令预览和执行结果，支持筛选功能
 */
public class ArsenalDialog extends JDialog implements I18nManager.LanguageChangeListener {
    
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
    private JButton copyCommandButton; // 复制命令按钮（替换原来的刷新变量按钮）
    private JScrollPane resultScrollPane;
    
    private HttpRequest httpRequest;
    private HttpResponse httpResponse;
    private List<HttpRequest> allSelectedRequests; // 新增：所有选中的HTTP请求
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
        this.allSelectedRequests = new ArrayList<>();
        if (httpRequest != null) {
            this.allSelectedRequests.add(httpRequest);
        }
        this.allToolCommands = loadAllToolCommands();
        this.filteredToolCommands = new ArrayList<>(allToolCommands);
        this.allCategories = extractAllCategories();
        
        initializeDialog();
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        loadToolData();
        
        // 注册语言变更监听器
        I18nManager.getInstance().addLanguageChangeListener(this);
    }
    
    /**
     * 构造函数（支持多个HTTP请求）
     * @param httpRequest 主HTTP请求对象
     * @param httpResponse HTTP响应对象
     * @param allSelectedRequests 所有选中的HTTP请求列表
     */
    public ArsenalDialog(HttpRequest httpRequest, HttpResponse httpResponse, List<HttpRequest> allSelectedRequests) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
        this.allSelectedRequests = allSelectedRequests != null ? new ArrayList<>(allSelectedRequests) : new ArrayList<>();
        if (this.allSelectedRequests.isEmpty() && httpRequest != null) {
            this.allSelectedRequests.add(httpRequest);
        }
        this.allToolCommands = loadAllToolCommands();
        this.filteredToolCommands = new ArrayList<>(allToolCommands);
        this.allCategories = extractAllCategories();
        
        initializeDialog();
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        loadToolData();
        
        // 注册语言变更监听器
        I18nManager.getInstance().addLanguageChangeListener(this);
    }
    
    /**
     * 初始化对话框属性
     */
    private void initializeDialog() {
        I18nManager i18n = I18nManager.getInstance();
        setTitle(i18n.getText("dialog.arsenal.title"));
        setSize(1200, 800);  // 增加宽度以适应新增的列
        // 不在这里设置位置，由调用方决定位置
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
     * 重写setVisible方法，确保对话框总是在前面显示
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            // 延迟执行，避免递归调用
            SwingUtilities.invokeLater(() -> {
                toFront();
                requestFocus();
            });
        }
    }
    
    /**
     * 将对话框置于前台
     */
    public void bringToFront() {
        SwingUtilities.invokeLater(() -> {
            // 多重保障确保窗口在前面
            toFront();
            requestFocus();

            // 确保窗口可见且获得焦点
            if (!isVisible()) {
                super.setVisible(true);  // 使用super避免递归
            }
            toFront();
            requestFocus();
        });
    }
    
    /**
     * 初始化UI组件
     */
    private void initializeComponents() {
        // 创建筛选组件
        initializeFilterComponents();
        
        // 创建工具表格
        I18nManager i18n = I18nManager.getInstance();
        String[] columnNames = {
            i18n.getText("arsenal.dialog.table.column.tool.name"),
            i18n.getText("arsenal.dialog.table.column.command"), 
            i18n.getText("arsenal.dialog.table.column.note"),
            i18n.getText("arsenal.dialog.table.column.work.dir"),
            i18n.getText("arsenal.dialog.table.column.category")
        };
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
        toolTable.getColumnModel().getColumn(0).setPreferredWidth(120);  // 工具名称
        toolTable.getColumnModel().getColumn(1).setPreferredWidth(300);  // 命令
        toolTable.getColumnModel().getColumn(2).setPreferredWidth(150);  // 备注
        toolTable.getColumnModel().getColumn(3).setPreferredWidth(120);  // 工作目录
        toolTable.getColumnModel().getColumn(4).setPreferredWidth(100);  // 分类
        
        // 创建表格排序器
        tableSorter = new TableRowSorter<>(tableModel);
        toolTable.setRowSorter(tableSorter);
        
        // 创建命令选项卡面板
        initializeCommandTabs();
        
        // 创建运行按钮
        runButton = new JButton(i18n.getText("arsenal.dialog.button.run"));
        runButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        runButton.setBackground(new Color(40, 167, 69));
        runButton.setForeground(Color.WHITE);
        runButton.setEnabled(false);
        runButton.setPreferredSize(new Dimension(100, 30));
        
        // 创建复制命令按钮（替换原来的刷新变量按钮）
        copyCommandButton = new JButton(i18n.getText("arsenal.dialog.button.copy.command"));
        copyCommandButton.setFont(new Font("微软雅黑", Font.BOLD, 11));
        copyCommandButton.setBackground(new Color(255, 193, 7));
        copyCommandButton.setForeground(Color.BLACK);
        copyCommandButton.setEnabled(false);
        copyCommandButton.setPreferredSize(new Dimension(100, 30));
        
        // 创建执行历史文本框 - 修改为白色背景
        commandResultArea = new JTextArea(8, 50);
        commandResultArea.setEditable(false);
        commandResultArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));  // 使用支持中文的字体
        commandResultArea.setBackground(Color.WHITE);  // 修改为白色背景
        commandResultArea.setForeground(Color.BLACK);  // 修改为黑色文字
        commandResultArea.setBorder(BorderFactory.createTitledBorder(i18n.getText("arsenal.dialog.execution.history")));  // 修改标题
        commandResultArea.setText(i18n.getText("arsenal.dialog.execution.hint") + "\n");
        
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
        I18nManager tabI18n = I18nManager.getInstance();
        commandTabbedPane.addTab(tabI18n.getText("arsenal.dialog.tab.original.command"), originalScrollPane);
        commandTabbedPane.addTab(tabI18n.getText("arsenal.dialog.tab.rendered.command"), renderedScrollPane);
        commandTabbedPane.addTab(tabI18n.getText("arsenal.dialog.tab.variables.preview"), variablesScrollPane);
        
        // 设置选项卡提示
        commandTabbedPane.setToolTipTextAt(0, tabI18n.getText("arsenal.dialog.tab.tooltip.original"));
        commandTabbedPane.setToolTipTextAt(1, tabI18n.getText("arsenal.dialog.tab.tooltip.rendered"));
        commandTabbedPane.setToolTipTextAt(2, tabI18n.getText("arsenal.dialog.tab.tooltip.variables"));
    }
    
    /**
     * 初始化筛选组件
     */
    private void initializeFilterComponents() {
        // 工具名称筛选框
        I18nManager filterI18n = I18nManager.getInstance();
        toolNameFilterField = new JTextField(15);
        toolNameFilterField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        toolNameFilterField.setBorder(BorderFactory.createTitledBorder(filterI18n.getText("arsenal.dialog.filter.tool.name")));
        
        // 分类筛选下拉框
        categoryFilterCombo = new JComboBox<>();
        categoryFilterCombo.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        categoryFilterCombo.addItem(filterI18n.getText("arsenal.dialog.filter.category.all"));
        
        // 清除筛选按钮
        clearFilterButton = new JButton(filterI18n.getText("arsenal.dialog.button.clear.filter"));
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
        tableScrollPane.setPreferredSize(new Dimension(1180, 200));
        I18nManager layoutI18n = I18nManager.getInstance();
        tableScrollPane.setBorder(BorderFactory.createTitledBorder(layoutI18n.getText("arsenal.dialog.border.tool.list")));
        
        // 命令预览选项卡和运行按钮面板
        JPanel middlePanel = new JPanel(new BorderLayout());
        
        // 选项卡面板
        commandTabbedPane.setPreferredSize(new Dimension(1180, 150));
        middlePanel.add(commandTabbedPane, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.add(copyCommandButton);
        buttonPanel.add(runButton);
        middlePanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 组合中部面板
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(tableScrollPane, BorderLayout.NORTH);
        centerPanel.add(middlePanel, BorderLayout.CENTER);
        
        // 底部：执行历史
        resultScrollPane.setPreferredSize(new Dimension(1180, 180));
        
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
        I18nManager panelI18n = I18nManager.getInstance();
        filterPanel.setBorder(BorderFactory.createTitledBorder(panelI18n.getText("arsenal.dialog.border.filter.conditions")));
        filterPanel.setPreferredSize(new Dimension(1180, 80));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 工具名称标签和输入框
        gbc.gridx = 0; gbc.gridy = 0;
        filterPanel.add(new JLabel(panelI18n.getText("arsenal.dialog.filter.label.tool.name")), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.4;
        filterPanel.add(toolNameFilterField, gbc);
        
        // 分类标签和下拉框
        gbc.gridx = 2; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        filterPanel.add(new JLabel(panelI18n.getText("arsenal.dialog.filter.label.category")), gbc);
        
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
                        copyCommandButton.setEnabled(true);
                    } else {
                        selectedToolCommand = null;
                        clearCommandAreas();
                        runButton.setEnabled(false);
                        copyCommandButton.setEnabled(false);
                    }
                } else {
                    selectedToolCommand = null;
                    clearCommandAreas();
                    runButton.setEnabled(false);
                    copyCommandButton.setEnabled(false);
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
        
        // 复制命令按钮点击事件（替换原来的刷新变量按钮事件）
        copyCommandButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyRenderedCommand();
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
            I18nManager variableI18n = I18nManager.getInstance();
            variablesPreviewArea.setText(variableI18n.getText("arsenal.dialog.variables.no.request"));
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
            I18nManager parseErrorI18n = I18nManager.getInstance();
            String errorMsg = parseErrorI18n.getText("arsenal.dialog.error.variable.parse", e.getMessage());
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
                                    I18nManager applyI18n = I18nManager.getInstance();
                        if (categoryFilter != null && !categoryFilter.equals(applyI18n.getText("arsenal.dialog.filter.category.all"))) {
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
                I18nManager loadI18n = I18nManager.getInstance();
                categoryFilterCombo.addItem(loadI18n.getText("arsenal.dialog.filter.category.all"));
                
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
        
        I18nManager loadDataI18n = I18nManager.getInstance();
        for (HttpToolCommand toolCommand : filteredToolCommands) {
            String toolName = toolCommand.getDisplayName() != null ? toolCommand.getDisplayName() : 
                loadDataI18n.getText("arsenal.dialog.unknown.tool");
            String command = toolCommand.getCommand() != null ? toolCommand.getCommand() : "";
            String note = toolCommand.getNote() != null ? toolCommand.getNote() : "";
            String workDir = toolCommand.getWorkDir() != null ? toolCommand.getWorkDir() : "";
            String category = toolCommand.getCategory() != null ? toolCommand.getCategory() : 
                loadDataI18n.getText("arsenal.dialog.uncategorized");
            
            // 截断过长的命令显示
            String displayCommand = command.length() > 40 ? 
                command.substring(0, 40) + "..." : command;
            
            // 截断过长的备注显示
            String displayNote = note.length() > 20 ? 
                note.substring(0, 20) + "..." : note;
                
            // 截断过长的工作目录显示
            String displayWorkDir = workDir.length() > 15 ? 
                "..." + workDir.substring(workDir.length() - 15) : workDir;
            
            tableModel.addRow(new Object[]{toolName, displayCommand, displayNote, displayWorkDir, category});
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
        I18nManager statusI18n = I18nManager.getInstance();
        String title = statusI18n.getText("arsenal.dialog.title.pattern", 
                                    String.valueOf(filteredToolCommands.size()), 
                                    String.valueOf(allToolCommands.size()));
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
        I18nManager categoryI18n = I18nManager.getInstance();
        return categoryI18n.getText("arsenal.dialog.uncategorized");
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
                
                // 设置渲染后的命令 - 只显示纯命令，不包含调试注释
                if (httpRequest != null) {
                    String renderedCommand = generateRenderedCommand(selectedToolCommand, httpRequest);
                    renderedCommandArea.setText(renderedCommand);
                    renderedCommandArea.setCaretPosition(0);
                } else {
                    I18nManager renderI18n = I18nManager.getInstance();
                    renderedCommandArea.setText(renderI18n.getText("arsenal.dialog.no.request.render"));
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
     * @return 渲染后的命令字符串（纯命令，不包含注释）
     */
    private String generateRenderedCommand(HttpToolCommand toolCommand, HttpRequest request) {
        try {
            String command = toolCommand.getCommand();
            if (command == null || command.isEmpty()) {
                return "";
            }
            
            if (request == null) {
                return command; // 返回原始命令，不添加警告注释
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
            
            // 处理httpList相关变量
            addHttpListVariables(allVariables, allSelectedRequests);
            
            // 进行变量替换
            String renderedCommand = replaceVariables(command, allVariables);
            
            return renderedCommand;
            
        } catch (Exception e) {
            // 记录错误到日志，但返回原始命令
            ApiManager.getInstance().getApi().logging().logToError("命令渲染失败: " + e.getMessage());
            return toolCommand.getCommand();
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
        
        I18nManager execI18n = I18nManager.getInstance();
        if (selectedTab == 0) { // 原始命令选项卡
            command = originalCommandArea.getText();
            commandType = execI18n.getText("arsenal.dialog.execution.original.command");
        } else if (selectedTab == 1) { // 渲染命令选项卡
            command = renderedCommandArea.getText();
            commandType = execI18n.getText("arsenal.dialog.execution.rendered.command");
        } else {
            JOptionPane.showMessageDialog(this, execI18n.getText("arsenal.dialog.message.select.tab"), 
                execI18n.getText("dialog.title.warning"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (command == null || command.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, execI18n.getText("arsenal.dialog.message.command.empty"), 
                execI18n.getText("dialog.title.error"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 获取工具名称
        String toolName = selectedToolCommand != null ? selectedToolCommand.getToolName() : 
            execI18n.getText("arsenal.dialog.manual.command");
        
        // 禁用运行按钮防止重复执行
        runButton.setEnabled(false);
        runButton.setText(execI18n.getText("arsenal.dialog.button.running"));
        
        // 记录执行开始
        addExecutionLogEntry(execI18n.getText("arsenal.dialog.execution.start"), toolName, commandType, command.trim());
        
        try {
            // 对于原始命令，需要进行变量替换；对于渲染命令，直接使用
            String finalCommand = command.trim();
            if (selectedTab == 0 && httpRequest != null && selectedToolCommand != null) {
                // 原始命令需要进行变量替换
                finalCommand = generateRenderedCommand(selectedToolCommand, httpRequest);
            }
            // 渲染命令选项卡中的内容已经是纯命令，直接使用
            
            // 记录最终执行的命令
            addExecutionLogEntry(execI18n.getText("arsenal.dialog.execution.actual"), toolName, 
                execI18n.getText("arsenal.dialog.execution.system.command"), finalCommand);
            
            // 获取工作目录（优先使用工具配置的工作目录）
            String toolWorkDir = null;
            if (selectedToolCommand != null) {
                toolWorkDir = selectedToolCommand.getWorkDir();
            }
            
            // 记录工作目录信息
            if (toolWorkDir != null && !toolWorkDir.trim().isEmpty()) {
                addExecutionLogEntry(execI18n.getText("arsenal.dialog.execution.workdir"), toolName, 
                    execI18n.getText("arsenal.dialog.execution.tool.workdir"), toolWorkDir);
            }
            
            // 使用ToolExecutor执行命令，支持工作目录
            ToolExecutor.getInstance().executeCommandWithWorkDirAsync(finalCommand, toolName, toolWorkDir);
            
            // 立即恢复按钮状态（因为是异步执行）
            SwingUtilities.invokeLater(() -> {
                runButton.setEnabled(true);
                runButton.setText(execI18n.getText("arsenal.dialog.button.run"));
            });
            
        } catch (Exception e) {
            addExecutionLogEntry(execI18n.getText("arsenal.dialog.execution.script.exception"), toolName, 
                execI18n.getText("arsenal.dialog.execution.error"), e.getMessage());
            // 恢复按钮状态
            runButton.setEnabled(true);
            runButton.setText(execI18n.getText("arsenal.dialog.button.run"));
            
            // 显示错误对话框
            JOptionPane.showMessageDialog(this, 
                execI18n.getText("arsenal.dialog.copy.failed.pattern", e.getMessage()), 
                execI18n.getText("dialog.title.error"), 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 通过临时脚本执行命令
     * @param command 要执行的命令
     * @param toolName 工具名称
     */
    private void executeCommandViaScript(String command, String toolName) {
        try {
            // 获取插件路径
            String extensionPath = getExtensionPath();
            if (extensionPath == null) {
                I18nManager pathErrorI18n = I18nManager.getInstance();
                throw new Exception(pathErrorI18n.getText("arsenal.dialog.error.no.plugin.path"));
            }
            
            // 生成临时脚本文件
            java.io.File scriptFile = createTemporaryScript(command, extensionPath);
            I18nManager scriptI18n = I18nManager.getInstance();
            addExecutionLogEntry(scriptI18n.getText("arsenal.dialog.execution.script.generated"), toolName, 
                scriptI18n.getText("arsenal.dialog.execution.script.path"), scriptFile.getAbsolutePath());
            
            // 执行脚本
            executeScript(scriptFile, toolName);
            
        } catch (Exception e) {
            I18nManager scriptExecI18n = I18nManager.getInstance();
            addExecutionLogEntry(scriptExecI18n.getText("arsenal.dialog.execution.script.exception"), toolName, 
                scriptExecI18n.getText("arsenal.dialog.execution.error"), e.getMessage());
            // 恢复按钮状态
            runButton.setEnabled(true);
            runButton.setText(scriptExecI18n.getText("arsenal.dialog.button.run"));
            
            // 显示错误对话框
            JOptionPane.showMessageDialog(this, 
                scriptExecI18n.getText("arsenal.dialog.copy.failed.pattern", e.getMessage()), 
                scriptExecI18n.getText("dialog.title.error"), 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 获取插件路径
     * @return 插件路径
     */
    private String getExtensionPath() {
        try {
            if (ApiManager.getInstance().isInitialized()) {
                String filename = ApiManager.getInstance().getApi().extension().filename();
                if (filename != null && !filename.isEmpty()) {
                    // 获取插件所在目录
                    java.io.File file = new java.io.File(filename);
                    return file.getParent();
                }
            }
            // 如果无法获取插件路径，使用系统临时目录
            return System.getProperty("java.io.tmpdir");
        } catch (Exception e) {
            I18nManager pathI18n = I18nManager.getInstance();
            addExecutionLogEntry(pathI18n.getText("arsenal.dialog.execution.tool"), pathI18n.getText("arsenal.dialog.execution.system.command"), 
                pathI18n.getText("arsenal.dialog.execution.warning"), pathI18n.getText("arsenal.dialog.error.plugin.path.use.temp", e.getMessage()));
            return System.getProperty("java.io.tmpdir");
        }
    }
    
    /**
     * 创建临时脚本文件
     * @param command 要执行的命令
     * @param extensionPath 插件路径
     * @return 脚本文件
     * @throws Exception 创建异常
     */
    private java.io.File createTemporaryScript(String command, String extensionPath) throws Exception {
        java.io.File scriptFile;
        String scriptContent;
        
        if (ToolExecutor.isWindows()) {
            // Windows: 创建批处理文件
            scriptFile = new java.io.File(extensionPath, "bparsenal_temp_" + System.currentTimeMillis() + ".bat");
            scriptContent = generateBatchScript(command);
        } else {
            // Linux/Unix: 创建shell脚本
            scriptFile = new java.io.File(extensionPath, "bparsenal_temp_" + System.currentTimeMillis() + ".sh");
            scriptContent = generateShellScript(command);
        }
        
        // 写入脚本内容，Windows使用系统默认编码(GBK)，Linux使用UTF-8
        String encoding = ToolExecutor.isWindows() ? "GBK" : "UTF-8";
        try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(scriptFile), encoding)) {
            writer.write(scriptContent);
        } catch (java.io.UnsupportedEncodingException e) {
            // 如果指定编码不支持，使用默认编码
            try (java.io.FileWriter writer = new java.io.FileWriter(scriptFile)) {
                writer.write(scriptContent);
            }
        }
        
        // 设置脚本为可执行
        if (!ToolExecutor.isWindows()) {
            scriptFile.setExecutable(true);
        }
        
        // 设置脚本在程序退出时自动删除
        scriptFile.deleteOnExit();
        
        return scriptFile;
    }
    
    /**
     * 生成Windows批处理脚本
     * @param command 要执行的命令
     * @return 脚本内容
     */
    private String generateBatchScript(String command) {
        StringBuilder script = new StringBuilder();
        script.append("@echo off\r\n");
        script.append("title BpArsenal Tool Execution\r\n");
        script.append("color 0A\r\n"); // 设置绿色文本
        script.append("echo.\r\n");
        script.append("echo ========================================\r\n");
        I18nManager batchI18n = I18nManager.getInstance();
        script.append("echo ").append(batchI18n.getText("arsenal.dialog.shell.title.bparsenal")).append("\r\n");
        script.append("echo Time: %date% %time%\r\n");
        script.append("echo ========================================\r\n");
        script.append("echo.\r\n");
        script.append("echo Executing command:\r\n");
        
        // 显示命令但转义特殊字符
        String displayCommand = command.replace("%", "%%").replace("^", "^^");
        script.append("echo ").append(displayCommand).append("\r\n");
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
        script.append("pause\r\n"); // 暂停以便查看结果
        
        return script.toString();
    }
    
    /**
     * 生成Linux Shell脚本
     * @param command 要执行的命令
     * @return 脚本内容
     */
    private String generateShellScript(String command) {
        I18nManager shellI18n = I18nManager.getInstance();
        StringBuilder script = new StringBuilder();
        script.append("#!/bin/bash\n");
        script.append("# ").append(shellI18n.getText("arsenal.dialog.shell.comment")).append("\n");
        script.append("# 自动生成于: $(date)\n\n");
        
        script.append("echo \"========================================\"\n");
        script.append("echo \"").append(shellI18n.getText("arsenal.dialog.shell.title.bparsenal")).append("\"\n");
        script.append("echo \"").append(shellI18n.getText("arsenal.dialog.shell.time")).append("\"\n");
        script.append("echo \"========================================\"\n");
        script.append("echo\n");
        script.append("echo \"").append(shellI18n.getText("arsenal.dialog.shell.executing.command")).append("\"\n");
        script.append("echo \"").append(command.replace("\"", "\\\"")).append("\"\n"); // 转义双引号
        script.append("echo\n");
        script.append("echo \"========================================\"\n");
        script.append("echo\n");
        
        // 执行实际命令
        script.append(command).append("\n");
        script.append("EXIT_CODE=$?\n");
        
        script.append("\n");
        script.append("echo\n");
        script.append("echo \"========================================\"\n");
        script.append("echo \"").append(shellI18n.getText("arsenal.dialog.shell.command.completed")).append("\"\n");
        script.append("echo \"========================================\"\n");
        script.append("read -p \"").append(shellI18n.getText("arsenal.dialog.shell.press.enter")).append("\"\n"); // 暂停以便查看结果
        
        return script.toString();
    }
    
    /**
     * 执行脚本文件
     * @param scriptFile 脚本文件
     * @param toolName 工具名称
     */
    private void executeScript(java.io.File scriptFile, String toolName) {
        try {
            ProcessBuilder processBuilder = null;
            
            if (ToolExecutor.isWindows()) {
                // Windows: 在新命令窗口中执行批处理文件
                processBuilder = new ProcessBuilder("cmd", "/c", "start", "\"BpArsenal Tool Execution\"", 
                    "cmd", "/k", "\"" + scriptFile.getAbsolutePath() + "\"");
            } else {
                // Linux: 尝试在终端中执行
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
                            // 如果所有终端都失败，直接执行脚本
                            processBuilder = new ProcessBuilder("bash", scriptFile.getAbsolutePath());
                        }
                    }
                }
                
                // 如果仍然没有成功创建，使用默认方式
                if (processBuilder == null) {
                    processBuilder = new ProcessBuilder("bash", scriptFile.getAbsolutePath());
                }
            }
            
            // 设置工作目录
            processBuilder.directory(scriptFile.getParentFile());
            
            // 启动进程
            Process process = processBuilder.start();
            
            I18nManager launchI18n = I18nManager.getInstance();
            addExecutionLogEntry(launchI18n.getText("arsenal.dialog.execution.script.started"), toolName, 
                launchI18n.getText("arsenal.dialog.execution.success"), launchI18n.getText("arsenal.dialog.script.started.success"));
            
            // 异步等待进程完成并清理
            CompletableFuture.runAsync(() -> {
                try {
                    int exitCode = process.waitFor();
                    
                    SwingUtilities.invokeLater(() -> {
                        I18nManager processI18n = I18nManager.getInstance();
                        String status = exitCode == 0 ? processI18n.getText("arsenal.dialog.execution.success.hint") : 
                            processI18n.getText("arsenal.dialog.execution.completed.hint");
                        addExecutionLogEntry(processI18n.getText("arsenal.dialog.execution.script.completed"), toolName, 
                            status, processI18n.getText("arsenal.dialog.exit.code", String.valueOf(exitCode)));
                        
                        // 恢复按钮状态
                        runButton.setEnabled(true);
                        runButton.setText(processI18n.getText("arsenal.dialog.button.run"));
                    });
                    
                    // 延迟删除脚本文件（给进程时间完成）
                    Thread.sleep(5000);
                    if (scriptFile.exists()) {
                        scriptFile.delete();
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        I18nManager exceptionI18n = I18nManager.getInstance();
                        addExecutionLogEntry(exceptionI18n.getText("arsenal.dialog.execution.script.exception"), toolName, 
                            exceptionI18n.getText("arsenal.dialog.execution.error"), e.getMessage());
                        runButton.setEnabled(true);
                        runButton.setText(exceptionI18n.getText("arsenal.dialog.button.run"));
                    });
                }
            });
            
        } catch (Exception e) {
            I18nManager startFailI18n = I18nManager.getInstance();
            addExecutionLogEntry(startFailI18n.getText("arsenal.dialog.execution.script.failed"), toolName, 
                startFailI18n.getText("arsenal.dialog.execution.error"), e.getMessage());
            // 恢复按钮状态
            runButton.setEnabled(true);
            runButton.setText(startFailI18n.getText("arsenal.dialog.button.run"));
            throw new RuntimeException(startFailI18n.getText("arsenal.dialog.copy.failed.pattern", e.getMessage()), e);
        }
    }
    
    /**
     * 添加执行日志条目
     * @param action 操作类型
     * @param toolName 工具名称
     * @param type 类型
     * @param details 详细信息
     */
    private void addExecutionLogEntry(String action, String toolName, String type, String details) {
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = formatter.format(new java.util.Date());
        
        I18nManager logI18n = I18nManager.getInstance();
        StringBuilder logEntry = new StringBuilder();
        logEntry.append("[").append(timestamp).append("] ");
        logEntry.append(action).append(" - ");
        logEntry.append(logI18n.getText("arsenal.dialog.execution.tool")).append(": ").append(toolName).append(" | ");
        logEntry.append(logI18n.getText("arsenal.dialog.execution.type")).append(": ").append(type).append("\n");
        
        // 如果details太长，进行截断显示
        String displayDetails = details;
        if (details.length() > 200) {
            displayDetails = details.substring(0, 200) + "...";
        }
        logEntry.append(logI18n.getText("arsenal.dialog.execution.details")).append(": ").append(displayDetails).append("\n");
        logEntry.append(createSeparator(50)).append("\n");
        
        commandResultArea.append(logEntry.toString());
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

    /**
     * 复制渲染后的命令到剪贴板
     */
    private void copyRenderedCommand() {
        I18nManager copyI18n = I18nManager.getInstance();
        if (selectedToolCommand == null) {
            JOptionPane.showMessageDialog(this, copyI18n.getText("arsenal.dialog.message.select.tool"), 
                copyI18n.getText("dialog.title.warning"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // 总是复制渲染后的命令，不依赖当前选中的选项卡
            String commandToCopy = renderedCommandArea.getText();
            String commandType = copyI18n.getText("arsenal.dialog.copy.type.rendered");
            
            if (commandToCopy == null || commandToCopy.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, copyI18n.getText("arsenal.dialog.message.rendered.empty"), 
                    copyI18n.getText("dialog.title.warning"), JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // 复制到系统剪贴板
            StringSelection stringSelection = new StringSelection(commandToCopy.trim());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            
            // 显示成功提示
            String toolName = selectedToolCommand.getToolName();
            String message = copyI18n.getText("arsenal.dialog.copy.success.pattern", 
                commandType, toolName, String.valueOf(commandToCopy.trim().length()));
            JOptionPane.showMessageDialog(this, message, copyI18n.getText("arsenal.dialog.copy.success.title"), 
                JOptionPane.INFORMATION_MESSAGE);
            
            // 记录到执行历史
            addExecutionLogEntry(copyI18n.getText("arsenal.dialog.execution.tool"), toolName, commandType, 
                copyI18n.getText("arsenal.dialog.copied.clipboard", String.valueOf(commandToCopy.trim().length())));
            
            // 记录到Burp日志
            if (ApiManager.getInstance().isInitialized()) {
                ApiManager.getInstance().getApi().logging().logToOutput(
                    copyI18n.getText("arsenal.dialog.log.copy.command", toolName, commandType)
                );
            }
            
        } catch (Exception ex) {
            String errorMsg = copyI18n.getText("arsenal.dialog.copy.failed.pattern", ex.getMessage());
            JOptionPane.showMessageDialog(this, errorMsg, copyI18n.getText("arsenal.dialog.copy.failed.title"), 
                JOptionPane.ERROR_MESSAGE);
            
            if (ApiManager.getInstance().isInitialized()) {
                ApiManager.getInstance().getApi().logging().logToError(errorMsg);
            }
        }
    }
    
    /**
     * 添加httpList相关变量
     * @param variables 变量映射
     * @param allRequests 所有选中的HTTP请求
     */
    private void addHttpListVariables(Map<String, String> variables, List<HttpRequest> allRequests) {
        try {
            if (allRequests == null || allRequests.isEmpty()) {
                return;
            }
            
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
            
            // 统计信息
            I18nManager summaryI18n = I18nManager.getInstance();
            variables.put("httpList.summary", summaryI18n.getText("arsenal.dialog.requests.summary",
                String.valueOf(allRequests.size()), String.valueOf(urls.size()), String.valueOf(hosts.size())));
                
        } catch (Exception e) {
            I18nManager errorI18n = I18nManager.getInstance();
            ApiManager.getInstance().getApi().logging().logToError(
                errorI18n.getText("arsenal.dialog.error.httplist.failed", e.getMessage()));
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
            String tempDir = System.getProperty("java.io.tmpdir");
            
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
            I18nManager tempFileI18n = I18nManager.getInstance();
            ApiManager.getInstance().getApi().logging().logToOutput(
                tempFileI18n.getText("arsenal.dialog.log.temp.file", 
                    tempFile.getAbsolutePath(), String.valueOf(items.size())));
            
            return tempFile.getAbsolutePath();
            
        } catch (Exception e) {
            throw new IOException("创建临时文件失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 语言变更监听器实现
     */
    @Override
    public void onLanguageChanged(I18nManager.SupportedLanguage newLanguage) {
        SwingUtilities.invokeLater(() -> {
            updateUITexts();
            revalidate();
            repaint();
        });
    }
    
    /**
     * 更新UI文本
     */
    private void updateUITexts() {
        I18nManager i18n = I18nManager.getInstance();
        
        // 更新对话框标题
        setTitle(i18n.getText("dialog.arsenal.title"));
        
        // 更新按钮文本
        updateButtonTexts(i18n);
        
        // 更新表格列标题
        updateTableHeaders(i18n);
        
        // 更新标签文本
        updateLabelTexts(i18n);
    }
    
    /**
     * 更新按钮文本
     */
    private void updateButtonTexts(I18nManager i18n) {
        try {
            if (runButton != null) {
                runButton.setText(i18n.getText("arsenal.dialog.button.run"));
            }
            if (copyCommandButton != null) {
                copyCommandButton.setText(i18n.getText("arsenal.dialog.button.copy.command"));
            }
            if (clearFilterButton != null) {
                clearFilterButton.setText(i18n.getText("arsenal.dialog.button.clear.filter"));
            }
        } catch (Exception e) {
            // 忽略更新错误
        }
    }
    
    /**
     * 更新表格列标题
     */
    private void updateTableHeaders(I18nManager i18n) {
        if (tableModel != null) {
            try {
                // 更新表格列名
                tableModel.setColumnIdentifiers(new String[]{
                    i18n.getText("arsenal.dialog.table.column.tool.name"),
                    i18n.getText("arsenal.dialog.table.column.command"),
                    i18n.getText("arsenal.dialog.table.column.note"),
                    i18n.getText("arsenal.dialog.table.column.work.dir"),
                    i18n.getText("arsenal.dialog.table.column.category")
                });
            } catch (Exception e) {
                // 忽略更新错误
            }
        }
    }
    
    /**
     * 更新标签文本
     */
    private void updateLabelTexts(I18nManager i18n) {
        try {
            // 更新选项卡标题
            if (commandTabbedPane != null) {
                commandTabbedPane.setTitleAt(0, i18n.getText("arsenal.dialog.tab.original.command"));
                commandTabbedPane.setTitleAt(1, i18n.getText("arsenal.dialog.tab.rendered.command"));
                commandTabbedPane.setTitleAt(2, i18n.getText("arsenal.dialog.tab.variables.preview"));
                
                // 更新选项卡提示
                commandTabbedPane.setToolTipTextAt(0, i18n.getText("arsenal.dialog.tab.tooltip.original"));
                commandTabbedPane.setToolTipTextAt(1, i18n.getText("arsenal.dialog.tab.tooltip.rendered"));
                commandTabbedPane.setToolTipTextAt(2, i18n.getText("arsenal.dialog.tab.tooltip.variables"));
            }
            
            // 更新边框标题
            if (commandResultArea != null) {
                commandResultArea.setBorder(BorderFactory.createTitledBorder(i18n.getText("arsenal.dialog.execution.history")));
            }
            
            // 更新分类下拉框
            if (categoryFilterCombo != null && categoryFilterCombo.getItemCount() > 0) {
                String selectedItem = (String) categoryFilterCombo.getSelectedItem();
                String allCategoriesText = i18n.getText("arsenal.dialog.filter.category.all");
                categoryFilterCombo.removeItemAt(0);
                categoryFilterCombo.insertItemAt(allCategoriesText, 0);
                if (selectedItem != null && selectedItem.equals(allCategoriesText)) {
                    categoryFilterCombo.setSelectedIndex(0);
                }
            }
            
        } catch (Exception e) {
            // 忽略更新错误
        }
    }
} 