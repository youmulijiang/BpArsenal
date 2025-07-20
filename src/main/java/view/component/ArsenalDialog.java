package view.component;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import controller.ToolController;
import executor.ToolExecutor;
import manager.ApiManager;
import manager.ConfigManager;
import model.Config;
import model.HttpTool;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * Arsenal工具对话框
 * 显示工具列表、命令预览和执行结果
 */
public class ArsenalDialog extends JDialog {
    
    private JTable toolTable;
    private DefaultTableModel tableModel;
    private JTextArea commandPreviewArea;
    private JTextArea commandResultArea;
    private JButton runButton;
    private JScrollPane resultScrollPane;
    
    private HttpRequest httpRequest;
    private HttpResponse httpResponse;
    private List<HttpTool> allTools;
    private HttpTool selectedTool;
    
    /**
     * 构造函数
     * @param httpRequest HTTP请求对象
     * @param httpResponse HTTP响应对象
     */
    public ArsenalDialog(HttpRequest httpRequest, HttpResponse httpResponse) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
        this.allTools = loadAllTools();
        
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
        setSize(900, 700);
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
        
        // 设置列宽
        toolTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        toolTable.getColumnModel().getColumn(1).setPreferredWidth(400);
        toolTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        
        // 创建命令预览文本框
        commandPreviewArea = new JTextArea(4, 50);
        commandPreviewArea.setEditable(false);
        commandPreviewArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        commandPreviewArea.setBackground(new Color(248, 248, 248));
        commandPreviewArea.setBorder(BorderFactory.createTitledBorder("命令预览"));
        commandPreviewArea.setLineWrap(true);
        commandPreviewArea.setWrapStyleWord(true);
        
        // 创建运行按钮
        runButton = new JButton("Run");
        runButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        runButton.setBackground(new Color(0, 123, 255));
        runButton.setForeground(Color.WHITE);
        runButton.setEnabled(false);
        runButton.setPreferredSize(new Dimension(80, 30));
        
        // 创建执行结果文本框
        commandResultArea = new JTextArea(8, 50);
        commandResultArea.setEditable(false);
        commandResultArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        commandResultArea.setBackground(Color.BLACK);
        commandResultArea.setForeground(Color.GREEN);
        commandResultArea.setBorder(BorderFactory.createTitledBorder("执行结果"));
        
        resultScrollPane = new JScrollPane(commandResultArea);
        resultScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }
    
    /**
     * 布局组件
     */
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // 顶部：工具表格
        JScrollPane tableScrollPane = new JScrollPane(toolTable);
        tableScrollPane.setPreferredSize(new Dimension(880, 250));
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("工具列表"));
        
        // 中部：命令预览和运行按钮
        JPanel middlePanel = new JPanel(new BorderLayout());
        JScrollPane previewScrollPane = new JScrollPane(commandPreviewArea);
        middlePanel.add(previewScrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(runButton);
        middlePanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 底部：执行结果
        resultScrollPane.setPreferredSize(new Dimension(880, 200));
        
        // 添加到主面板
        add(tableScrollPane, BorderLayout.NORTH);
        add(middlePanel, BorderLayout.CENTER);
        add(resultScrollPane, BorderLayout.SOUTH);
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 表格选择事件
        toolTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = toolTable.getSelectedRow();
                if (selectedRow >= 0 && selectedRow < allTools.size()) {
                    selectedTool = allTools.get(selectedRow);
                    updateCommandPreview();
                    runButton.setEnabled(true);
                } else {
                    selectedTool = null;
                    commandPreviewArea.setText("");
                    runButton.setEnabled(false);
                }
            }
        });
        
        // 运行按钮点击事件
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executeSelectedTool();
            }
        });
    }
    
    /**
     * 加载所有工具数据
     * @return 工具列表
     */
    private List<HttpTool> loadAllTools() {
        try {
            return ToolController.getInstance().getAllTools();
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
        
        for (HttpTool tool : allTools) {
            String toolName = tool.getToolName() != null ? tool.getToolName() : "未知工具";
            String command = tool.getCommand() != null ? tool.getCommand() : "";
            String category = getToolCategory(tool);
            
            // 截断过长的命令显示
            String displayCommand = command.length() > 50 ? 
                command.substring(0, 50) + "..." : command;
            
            tableModel.addRow(new Object[]{toolName, displayCommand, category});
        }
        
        // 自动调整列宽
        toolTable.revalidate();
        toolTable.repaint();
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
        if (selectedTool != null && httpRequest != null) {
            try {
                // 使用ToolExecutor生成预览命令
                String previewCommand = generatePreviewCommand(selectedTool, httpRequest);
                commandPreviewArea.setText(previewCommand);
                commandPreviewArea.setCaretPosition(0); // 滚动到顶部
            } catch (Exception e) {
                commandPreviewArea.setText("命令预览生成失败: " + e.getMessage());
                ApiManager.getInstance().getApi().logging().logToError("命令预览生成失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 生成预览命令
     * @param tool HTTP工具
     * @param request HTTP请求
     * @return 预览命令字符串
     */
    private String generatePreviewCommand(HttpTool tool, HttpRequest request) {
        try {
            // 使用ToolExecutor的变量替换功能
            return ToolExecutor.getInstance().previewCommand(tool, request);
        } catch (Exception e) {
            // 如果ToolExecutor没有预览方法，手动进行简单替换
            String command = tool.getCommand();
            if (command != null && request != null) {
                command = command.replace("%http.request.url%", request.url());
                // 可以添加更多变量替换...
            }
            return command != null ? command : "";
        }
    }
    
    /**
     * 执行选中的工具
     */
    private void executeSelectedTool() {
        if (selectedTool == null || httpRequest == null) {
            return;
        }
        
        // 禁用运行按钮防止重复执行
        runButton.setEnabled(false);
        runButton.setText("Running...");
        
        // 清空之前的结果
        commandResultArea.setText("正在执行命令...\n");
        commandResultArea.append("命令: " + selectedTool.getCommand() + "\n");
        commandResultArea.append("---\n");
        
        // 异步执行工具
        CompletableFuture.runAsync(() -> {
            try {
                ToolExecutor.getInstance().executeHttpTool(selectedTool, httpRequest);
                
                SwingUtilities.invokeLater(() -> {
                    commandResultArea.append("命令执行完成\n");
                    commandResultArea.append("注意: 详细的执行结果请查看Burp Suite的输出日志\n");
                    
                    // 恢复按钮状态
                    runButton.setEnabled(true);
                    runButton.setText("Run");
                    
                    // 滚动到底部
                    commandResultArea.setCaretPosition(commandResultArea.getDocument().getLength());
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    commandResultArea.append("执行失败: " + e.getMessage() + "\n");
                    
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
} 