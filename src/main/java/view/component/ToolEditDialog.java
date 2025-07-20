package view.component;

import model.HttpTool;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * HTTP工具编辑对话框 (View层组件)
 * 用于添加和编辑HTTP工具配置
 */
public class ToolEditDialog extends JDialog {
    
    private HttpTool tool;
    private boolean confirmed = false;
    
    private JTextField nameField;
    private JTextArea commandArea;
    private JCheckBox favorCheckBox;
    private JComboBox<String> categoryComboBox;
    private JButton okButton;
    private JButton cancelButton;
    
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
        setSize(700, 550);
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
        
        // 分类选择
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0; gbc.weighty = 0;
        gbc.insets = new Insets(10, 5, 5, 10);
        formPanel.add(new JLabel("工具分类:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        categoryComboBox = new JComboBox<>(new String[]{
            "sql-inject", "xss", "scanner", "brute-force", "exploit", "其他"
        });
        categoryComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        formPanel.add(categoryComboBox, gbc);
        
        // 命令
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.insets = new Insets(10, 5, 5, 10);
        formPanel.add(new JLabel("执行命令 *:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        commandArea = new JTextArea(10, 50);
        commandArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        commandArea.setLineWrap(true);
        commandArea.setWrapStyleWord(true);
        commandArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane scrollPane = new JScrollPane(commandArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createTitledBorder("命令内容"));
        formPanel.add(scrollPane, gbc);
        
        // 收藏
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0; gbc.weighty = 0;
        gbc.insets = new Insets(10, 5, 5, 5);
        favorCheckBox = new JCheckBox("添加到收藏");
        favorCheckBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        formPanel.add(favorCheckBox, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // 帮助面板 - 添加占位符文档
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
        helpPanel.setBorder(BorderFactory.createTitledBorder("占位符文档"));
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // 常用占位符
        JTextArea commonPlaceholders = new JTextArea(4, 70);
        commonPlaceholders.setEditable(false);
        commonPlaceholders.setFont(new Font("Consolas", Font.PLAIN, 9));
        commonPlaceholders.setBackground(new Color(248, 248, 248));
        commonPlaceholders.setText(
            "# 请求基础\n" +
            "%http.request.url%              - 完整请求URL\n" +
            "%http.request.host%             - 目标主机\n" +
            "%http.request.port%             - 端口号\n" +
            "%http.request.path%             - 请求路径\n" +
            "%http.request.method%           - 请求方法(GET/POST等)\n" +
            "\n" +
            "# 请求头部\n" +
            "%http.request.headers.user.agent%    - User-Agent头\n" +
            "%http.request.headers.cookies%       - Cookie信息\n" +
            "%http.request.headers.authorization% - Authorization头\n" +
            "%http.request.headers.referer%       - Referer头\n" +
            "\n" +
            "# 请求体\n" +
            "%http.request.body%             - 请求体内容\n" +
            "%http.request.body.len%         - 请求体长度"
        );
        
        JScrollPane commonScroll = new JScrollPane(commonPlaceholders);
        tabbedPane.addTab("常用", commonScroll);
        
        // 完整文档
        JTextArea fullDoc = new JTextArea(4, 70);
        fullDoc.setEditable(false);
        fullDoc.setFont(new Font("Consolas", Font.PLAIN, 8));
        fullDoc.setBackground(new Color(248, 248, 248));
        fullDoc.setText(generateCompactDocumentation());
        
        JScrollPane fullScroll = new JScrollPane(fullDoc);
        tabbedPane.addTab("完整", fullScroll);
        
        helpPanel.add(tabbedPane, BorderLayout.CENTER);
        
        return helpPanel;
    }
    
    /**
     * 生成紧凑的占位符文档
     * @return 文档字符串
     */
    private String generateCompactDocumentation() {
        return "# HTTP占位符完整列表\n\n" +
               "## 请求基础\n" +
               "%http.request.url%              - 完整请求URL\n" +
               "%http.request.host%             - 目标主机\n" +
               "%http.request.port%             - 端口号\n" +
               "%http.request.path%             - 请求路径\n" +
               "%http.request.query%            - 查询字符串\n" +
               "%http.request.method%           - 请求方法\n" +
               "%http.request.protocol%         - 协议版本\n\n" +
               
               "## 请求头部\n" +
               "%http.request.headers.user.agent%    - User-Agent头\n" +
               "%http.request.headers.cookies%       - Cookie信息\n" +
               "%http.request.headers.authorization% - Authorization头\n" +
               "%http.request.headers.referer%       - Referer头\n" +
               "%http.request.headers.content.type%  - Content-Type头\n\n" +
               
               "## 请求参数\n" +
               "%http.request.params.get%       - GET参数(JSON)\n" +
               "%http.request.params.post%      - POST参数(JSON)\n" +
               "%http.request.params.all%       - 所有参数(JSON)\n\n" +
               
               "## 请求体\n" +
               "%http.request.body%             - 请求体内容\n" +
               "%http.request.body.len%         - 请求体长度\n\n" +
               
               "## 响应基础\n" +
               "%http.response.status%          - 响应状态码\n" +
               "%http.response.headers%         - 响应头(JSON)\n" +
               "%http.response.body%            - 响应体内容\n" +
               "%http.response.body.len%        - 响应体长度\n\n" +
               
               "示例用法:\n" +
               "sqlmap -u \"%http.request.url%\" --cookie=\"%http.request.headers.cookies%\"";
    }
    
    /**
     * 创建按钮面板
     * @return 按钮面板
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        
        JButton cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        cancelButton.setPreferredSize(new Dimension(80, 30));
        
        okButton = new JButton("确定");
        okButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        okButton.setPreferredSize(new Dimension(80, 30));
        okButton.setBackground(new Color(46, 125, 50));
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        
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
        
        // 获取取消按钮并设置事件
        JButton cancelButton = (JButton) ((JPanel) getContentPane().getComponent(1)).getComponent(0);
        cancelButton.addActionListener(e -> dispose());
        
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
            
            // 根据工具名称推断分类
            String category = inferCategory(tool.getToolName().toLowerCase());
            categoryComboBox.setSelectedItem(category);
        } else {
            // 设置默认值
            favorCheckBox.setSelected(true);
            categoryComboBox.setSelectedIndex(0);
            
            // 设置默认模板
            commandArea.setText("# 在此输入命令，支持以下占位符:\n" +
                              "# %http.request.url% - 请求URL\n" +
                              "# %http.request.headers.cookies% - Cookie\n" +
                              "# 完整占位符列表请查看下方文档\n\n" +
                              "");
        }
    }
    
    /**
     * 根据工具名称推断分类
     * @param toolName 工具名称
     * @return 分类
     */
    private String inferCategory(String toolName) {
        if (toolName.contains("sql") || toolName.contains("inject")) {
            return "sql-inject";
        } else if (toolName.contains("xss")) {
            return "xss";
        } else if (toolName.contains("scan") || toolName.contains("dir")) {
            return "scanner";
        } else if (toolName.contains("brute") || toolName.contains("hydra")) {
            return "brute-force";
        } else if (toolName.contains("exploit") || toolName.contains("msf")) {
            return "exploit";
        }
        return "其他";
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
    
    /**
     * 获取选中的分类
     * @return 分类字符串
     */
    public String getSelectedCategory() {
        return (String) categoryComboBox.getSelectedItem();
    }
} 