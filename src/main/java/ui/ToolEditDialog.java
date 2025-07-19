package ui;

import model.HttpTool;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * HTTP工具编辑对话框
 * 用于添加和编辑HTTP工具配置
 */
public class ToolEditDialog extends JDialog {
    
    private HttpTool tool;
    private boolean confirmed = false;
    
    private JTextField nameField;
    private JTextArea commandArea;
    private JCheckBox favorCheckBox;
    private JButton okButton;
    private JButton cancelButton;
    private JButton helpButton;
    
    public ToolEditDialog(Window parent, HttpTool tool) {
        super(parent, tool == null ? "添加HTTP工具" : "编辑HTTP工具", ModalityType.APPLICATION_MODAL);
        this.tool = tool;
        initializeUI();
        setupEventHandlers();
        loadData();
        
        setLocationRelativeTo(parent);
    }
    
    /**
     * 初始化UI组件
     */
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(600, 450);
        setResizable(true);
        
        // 创建主面板
        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);
        
        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 设置默认按钮
        getRootPane().setDefaultButton(okButton);
    }
    
    /**
     * 创建主面板
     * @return 主面板
     */
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        // 表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // 工具名称
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 10);
        formPanel.add(new JLabel("工具名称 *:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        nameField = new JTextField(20);
        nameField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        formPanel.add(nameField, gbc);
        
        // 命令
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.insets = new Insets(10, 5, 5, 10);
        formPanel.add(new JLabel("执行命令 *:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        commandArea = new JTextArea(8, 40);
        commandArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        commandArea.setLineWrap(true);
        commandArea.setWrapStyleWord(true);
        commandArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane scrollPane = new JScrollPane(commandArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createTitledBorder("命令内容"));
        formPanel.add(scrollPane, gbc);
        
        // 收藏
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0; gbc.weighty = 0;
        gbc.insets = new Insets(10, 5, 5, 5);
        favorCheckBox = new JCheckBox("添加到收藏");
        favorCheckBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        formPanel.add(favorCheckBox, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // 帮助面板
        JPanel helpPanel = createHelpPanel();
        mainPanel.add(helpPanel, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    /**
     * 创建帮助面板
     * @return 帮助面板
     */
    private JPanel createHelpPanel() {
        JPanel helpPanel = new JPanel(new BorderLayout());
        helpPanel.setBorder(BorderFactory.createTitledBorder("占位符帮助"));
        
        JTextArea helpText = new JTextArea(4, 50);
        helpText.setEditable(false);
        helpText.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        helpText.setBackground(new Color(248, 248, 248));
        helpText.setText(
            "常用占位符:\n" +
            "%http.request.url% - 完整请求URL\n" +
            "%http.request.host% - 目标主机\n" +
            "%http.request.headers.user.agent% - User-Agent头\n" +
            "%http.request.headers.cookies% - Cookie信息\n" +
            "点击'占位符帮助'按钮查看完整列表"
        );
        
        helpPanel.add(new JScrollPane(helpText), BorderLayout.CENTER);
        
        return helpPanel;
    }
    
    /**
     * 创建按钮面板
     * @return 按钮面板
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        
        helpButton = new JButton("? 占位符帮助");
        helpButton.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        helpButton.setPreferredSize(new Dimension(120, 30));
        
        cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        cancelButton.setPreferredSize(new Dimension(80, 30));
        
        okButton = new JButton("确定");
        okButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        okButton.setPreferredSize(new Dimension(80, 30));
        okButton.setBackground(new Color(46, 125, 50));
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        
        buttonPanel.add(helpButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);
        
        return buttonPanel;
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateInput()) {
                    saveData();
                    confirmed = true;
                    dispose();
                }
            }
        });
        
        cancelButton.addActionListener(e -> dispose());
        
        helpButton.addActionListener(e -> {
            PlaceholderHelpDialog helpDialog = new PlaceholderHelpDialog(this);
            helpDialog.setVisible(true);
        });
        
        // 回车键确定
        nameField.addActionListener(e -> okButton.doClick());
    }
    
    /**
     * 加载数据
     */
    private void loadData() {
        if (tool != null) {
            nameField.setText(tool.getToolName());
            commandArea.setText(tool.getCommand());
            favorCheckBox.setSelected(tool.isFavor());
        } else {
            // 设置默认值
            favorCheckBox.setSelected(true);
            commandArea.setText(""); // 可以设置默认模板
        }
    }
    
    /**
     * 验证输入
     * @return 验证结果
     */
    private boolean validateInput() {
        String name = nameField.getText().trim();
        String command = commandArea.getText().trim();
        
        if (name.isEmpty()) {
            showError("请输入工具名称");
            nameField.requestFocus();
            return false;
        }
        
        if (command.isEmpty()) {
            showError("请输入执行命令");
            commandArea.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * 保存数据
     */
    private void saveData() {
        if (tool == null) {
            tool = new HttpTool();
        }
        
        tool.setToolName(nameField.getText().trim());
        tool.setCommand(commandArea.getText().trim());
        tool.setFavor(favorCheckBox.isSelected());
    }
    
    /**
     * 显示错误消息
     * @param message 错误消息
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "输入错误", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * 获取编辑后的工具
     * @return HTTP工具对象
     */
    public HttpTool getTool() {
        return tool;
    }
    
    /**
     * 是否确认保存
     * @return 确认状态
     */
    public boolean isConfirmed() {
        return confirmed;
    }
} 