package view.component;

import executor.ToolExecutor;
import manager.ApiManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 命令执行对话框
 * 在独立窗口中执行命令并显示实时输出
 */
public class CommandExecutionDialog extends JDialog {
    
    private JTextArea outputArea;
    private JScrollPane scrollPane;
    private JButton closeButton;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    
    private String toolName;
    private String command;
    private boolean isCompleted = false;
    
    /**
     * 构造函数
     * @param parent 父窗口
     * @param toolName 工具名称
     * @param command 要执行的命令
     * @param commandType 命令类型（原始命令/渲染命令）
     */
    public CommandExecutionDialog(Window parent, String toolName, String command, String commandType) {
        super(parent, "命令执行 - " + toolName, ModalityType.MODELESS);
        this.toolName = toolName;
        this.command = command;
        
        initializeDialog();
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        
        // 显示初始信息
        appendToOutput(">>> 开始执行: " + toolName);
        appendToOutput(">>> 命令类型: " + commandType);
        appendToOutput(">>> 执行命令: " + command);
        appendToOutput(">>> 系统平台: " + ToolExecutor.getOsType());
        appendToOutput(createSeparator(60));
        
        // 开始执行命令
        executeCommand();
    }
    
    /**
     * 初始化对话框属性
     */
    private void initializeDialog() {
        setSize(800, 600);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // 禁止直接关闭
        
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
        // 创建输出文本区域
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        outputArea.setBackground(Color.BLACK);
        outputArea.setForeground(Color.WHITE);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        
        // 创建滚动面板
        scrollPane = new JScrollPane(outputArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // 创建状态标签
        statusLabel = new JLabel("正在执行...");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        
        // 创建进度条
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(true);
        progressBar.setString("执行中...");
        
        // 创建关闭按钮
        closeButton = new JButton("关闭");
        closeButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        closeButton.setEnabled(false); // 初始禁用
        closeButton.setPreferredSize(new Dimension(80, 30));
    }
    
    /**
     * 布局组件
     */
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // 中央输出区域
        add(scrollPane, BorderLayout.CENTER);
        
        // 底部状态面板
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 创建状态面板
     * @return 状态面板
     */
    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 左侧状态信息
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(statusLabel, BorderLayout.NORTH);
        leftPanel.add(progressBar, BorderLayout.CENTER);
        
        // 右侧按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        
        statusPanel.add(leftPanel, BorderLayout.CENTER);
        statusPanel.add(buttonPanel, BorderLayout.EAST);
        
        return statusPanel;
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 关闭按钮事件
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        // 窗口关闭事件
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (isCompleted) {
                    dispose();
                } else {
                    int option = JOptionPane.showConfirmDialog(
                        CommandExecutionDialog.this,
                        "命令正在执行中，确定要关闭窗口吗？",
                        "确认关闭",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                    );
                    if (option == JOptionPane.YES_OPTION) {
                        dispose();
                    }
                }
            }
        });
    }
    
    /**
     * 执行命令
     */
    private void executeCommand() {
        ToolExecutor.getInstance().executeCommandSync(command, toolName, new ToolExecutor.CommandExecutionCallback() {
            @Override
            public void onCommandStart(String toolName, String command) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("正在执行: " + toolName);
                });
            }
            
            @Override
            public void onOutputReceived(String output) {
                SwingUtilities.invokeLater(() -> {
                    appendToOutput(output);
                });
            }
            
            @Override
            public void onCommandComplete(String toolName, int exitCode, String fullOutput) {
                SwingUtilities.invokeLater(() -> {
                    appendToOutput(createSeparator(60));
                    if (exitCode == 0) {
                        appendToOutput(">>> 执行成功: " + toolName + " (退出码: " + exitCode + ")");
                        statusLabel.setText("执行成功 - " + toolName);
                        progressBar.setString("执行成功");
                    } else {
                        appendToOutput(">>> 执行失败: " + toolName + " (退出码: " + exitCode + ")");
                        statusLabel.setText("执行失败 - " + toolName);
                        progressBar.setString("执行失败");
                    }
                    
                    // 添加时间戳
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    appendToOutput(">>> 完成时间: " + formatter.format(new Date()));
                    
                    // 更新UI状态
                    isCompleted = true;
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                    closeButton.setEnabled(true);
                    
                    // 滚动到底部
                    outputArea.setCaretPosition(outputArea.getDocument().getLength());
                });
            }
            
            @Override
            public void onCommandError(String toolName, Exception error) {
                SwingUtilities.invokeLater(() -> {
                    appendToOutput(createSeparator(60));
                    appendToOutput(">>> 执行异常: " + toolName);
                    appendToOutput(">>> 错误信息: " + error.getMessage());
                    
                    // 添加时间戳
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    appendToOutput(">>> 异常时间: " + formatter.format(new Date()));
                    
                    // 更新UI状态
                    statusLabel.setText("执行异常 - " + toolName);
                    progressBar.setString("执行异常");
                    isCompleted = true;
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(0);
                    closeButton.setEnabled(true);
                    
                    // 滚动到底部
                    outputArea.setCaretPosition(outputArea.getDocument().getLength());
                });
            }
        });
    }
    
    /**
     * 添加输出到文本区域
     * @param text 输出文本
     */
    private void appendToOutput(String text) {
        outputArea.append(text + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
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
} 