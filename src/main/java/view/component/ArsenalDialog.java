package view.component;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import controller.ToolController;
import executor.ToolExecutor;
import manager.ApiManager;
import manager.ConfigManager;
import model.Config;
import model.HttpTool;
import model.HttpToolCommand;
import util.OsUtils;
import view.SettingPanel;

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
    
    private JTextArea commandResultArea;
    private JButton runButton;  // 统一的运行按钮
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
        
        // 创建执行结果文本框
        commandResultArea = new JTextArea(8, 50);
        commandResultArea.setEditable(false);
        commandResultArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        commandResultArea.setBackground(Color.BLACK);
        commandResultArea.setForeground(Color.GREEN);
        commandResultArea.setBorder(BorderFactory.createTitledBorder("执行结果"));
        
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
        
        // 添加选项卡
        commandTabbedPane.addTab("原始命令", originalScrollPane);
        commandTabbedPane.addTab("渲染命令", renderedScrollPane);
        
        // 设置选项卡提示
        commandTabbedPane.setToolTipTextAt(0, "显示未经变量替换的原始命令模板，可以手动编辑");
        commandTabbedPane.setToolTipTextAt(1, "显示经过变量替换的命令，可以手动编辑后执行");
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
        buttonPanel.add(runButton);
        middlePanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 组合中部面板
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(tableScrollPane, BorderLayout.NORTH);
        centerPanel.add(middlePanel, BorderLayout.CENTER);
        
        // 底部：执行结果
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
                    } else {
                        selectedToolCommand = null;
                        clearCommandAreas();
                        runButton.setEnabled(false);
                    }
                } else {
                    selectedToolCommand = null;
                    clearCommandAreas();
                    runButton.setEnabled(false);
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
            // 手动进行变量替换
            String command = toolCommand.getCommand();
            if (command != null && request != null) {
                command = command.replace("%http.request.url%", request.url());
                command = command.replace("%http.request.host%", request.httpService().host());
                command = command.replace("%http.request.port%", String.valueOf(request.httpService().port()));
                command = command.replace("%http.request.method%", request.method());
                command = command.replace("%http.request.path%", request.path());
                
                // 可以添加更多变量替换...
                // command = command.replace("%http.request.headers%", request.headers().toString());
                // command = command.replace("%http.request.body%", request.bodyToString());
            }
            return command != null ? command : "";
        } catch (Exception e) {
            return "命令渲染失败: " + e.getMessage();
        }
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
        } else { // 渲染命令选项卡
            command = renderedCommandArea.getText();
            commandType = "渲染命令";
        }
        
        if (command == null || command.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "命令不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 禁用运行按钮防止重复执行
        runButton.setEnabled(false);
        runButton.setText("Running...");
        
        // 清空之前的结果
        commandResultArea.setText("正在执行" + commandType + "...\n");
        if (selectedToolCommand != null) {
            commandResultArea.append("工具: " + selectedToolCommand.getToolName() + "\n");
        }
        commandResultArea.append("命令: " + command + "\n");
        commandResultArea.append("---执行结果---\n");
        
        // 使用ToolExecutor异步执行命令
        CompletableFuture.runAsync(() -> {
            try {
                // 获取命令前缀设置，如果没有设置则使用系统默认
                String[] commandPrefix = getCommandPrefix();
                
                // 格式化命令
                String[] formattedCommand = new String[commandPrefix.length + 1];
                System.arraycopy(commandPrefix, 0, formattedCommand, 0, commandPrefix.length);
                formattedCommand[commandPrefix.length] = command.trim();
                
                // 创建进程并执行
                ProcessBuilder processBuilder = new ProcessBuilder(formattedCommand);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();
                
                // 读取命令输出
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), OsUtils.getSystemEncoding()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // 实时更新UI
                        final String currentLine = line;
                        SwingUtilities.invokeLater(() -> {
                            commandResultArea.append(currentLine + "\n");
                            commandResultArea.setCaretPosition(commandResultArea.getDocument().getLength());
                        });
                    }
                }
                
                // 等待进程完成
                int exitCode = process.waitFor();
                
                SwingUtilities.invokeLater(() -> {
                    commandResultArea.append("\n---执行完成---\n");
                    commandResultArea.append("退出码: " + exitCode + "\n");
                    
                    if (exitCode == 0) {
                        commandResultArea.append(commandType + "执行成功！\n");
                    } else {
                        commandResultArea.append(commandType + "执行失败，退出码: " + exitCode + "\n");
                    }
                    
                    // 恢复按钮状态
                    runButton.setEnabled(true);
                    runButton.setText("Run");
                    
                    // 滚动到底部
                    commandResultArea.setCaretPosition(commandResultArea.getDocument().getLength());
                });
                
                String toolName = selectedToolCommand != null ? selectedToolCommand.getToolName() : "未知工具";
                ApiManager.getInstance().getApi().logging().logToOutput("工具执行完成: " + toolName + " (" + commandType + ")");
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    commandResultArea.append("\n执行异常: " + e.getMessage() + "\n");
                    
                    // 恢复按钮状态
                    runButton.setEnabled(true);
                    runButton.setText("Run");
                    
                    // 滚动到底部
                    commandResultArea.setCaretPosition(commandResultArea.getDocument().getLength());
                });
                
                ApiManager.getInstance().getApi().logging().logToError("工具执行失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 获取命令前缀设置
     * @return 命令前缀数组
     */
    private String[] getCommandPrefix() {
        try {
            // 尝试从设置面板获取命令前缀
            // 这里我们直接使用工具类的方法，避免复杂的依赖关系
            return OsUtils.getDefaultCommandPrefix();
        } catch (Exception e) {
            // 如果出错，使用系统默认
            return OsUtils.getDefaultCommandPrefix();
        }
    }
    
    /**
     * 获取系统编码
     * @return 编码字符串
     */
    private String getSystemEncoding() {
        return OsUtils.getSystemEncoding();
    }
} 