package view.component;

import model.HttpTool;
import util.I18nManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * HTTP工具编辑对话框 (View层组件)
 * 用于添加和编辑HTTP工具配置
 */
public class ToolEditDialog extends JDialog implements I18nManager.LanguageChangeListener {
    
    private HttpTool tool;
    private boolean confirmed = false;
    
    private JTextField nameField;
    private JTextArea commandArea;
    private JCheckBox favorCheckBox;
    private JComboBox<String> categoryComboBox;
    private JButton okButton;
    private JButton cancelButton;
    private JTabbedPane tabbedPane;
    private JTextArea commonPlaceholders;
    private JTextArea fullDoc;
    
    public ToolEditDialog(Window parent, HttpTool tool) {
        super(parent);
        this.tool = tool;
        
        // 注册语言变更监听器
        I18nManager.getInstance().addLanguageChangeListener(this);
        
        initializeUI();
        setupEventHandlers();
        loadData();
        
        setLocationRelativeTo(parent);
    }
    
    /**
     * 初始化UI组件
     */
    private void initializeUI() {
        I18nManager i18n = I18nManager.getInstance();
        
        // 设置对话框标题和属性
        setTitle(tool == null ? i18n.getText("tool.edit.dialog.title.add") : i18n.getText("tool.edit.dialog.title.edit"));
        setModalityType(ModalityType.APPLICATION_MODAL);
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
        I18nManager formI18n = I18nManager.getInstance();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 10);
        formPanel.add(new JLabel(formI18n.getText("tool.edit.dialog.label.tool.name")), gbc);
        
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
        formPanel.add(new JLabel(formI18n.getText("tool.edit.dialog.label.category")), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        categoryComboBox = new JComboBox<>(new String[]{
            formI18n.getText("tool.edit.category.sql.inject"),
            formI18n.getText("tool.edit.category.xss"),
            formI18n.getText("tool.edit.category.scanner"),
            formI18n.getText("tool.edit.category.brute.force"),
            formI18n.getText("tool.edit.category.exploit"),
            formI18n.getText("tool.edit.category.other")
        });
        categoryComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        formPanel.add(categoryComboBox, gbc);
        
        // 命令
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.insets = new Insets(10, 5, 5, 10);
        formPanel.add(new JLabel(formI18n.getText("tool.edit.dialog.label.command")), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        commandArea = new JTextArea(10, 50);
//        commandArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        commandArea.setLineWrap(true);
        commandArea.setWrapStyleWord(true);
        commandArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane scrollPane = new JScrollPane(commandArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createTitledBorder(formI18n.getText("tool.edit.dialog.border.command.content")));
        formPanel.add(scrollPane, gbc);
        
        // 收藏
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0; gbc.weighty = 0;
        gbc.insets = new Insets(10, 5, 5, 5);
        favorCheckBox = new JCheckBox(formI18n.getText("tool.edit.dialog.checkbox.add.favorites"));
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
        I18nManager helpI18n = I18nManager.getInstance();
        JPanel helpPanel = new JPanel(new BorderLayout());
        helpPanel.setBorder(BorderFactory.createTitledBorder(helpI18n.getText("tool.edit.dialog.border.placeholder.doc")));
        
        tabbedPane = new JTabbedPane();
        
        // 常用占位符
        commonPlaceholders = new JTextArea(4, 70);
        commonPlaceholders.setEditable(false);
        // 设置支持中文的字体，优先使用等宽字体
//        commonPlaceholders.setFont(getUnicodeFont(9));
        commonPlaceholders.setBackground(new Color(248, 248, 248));
        // 确保正确显示UTF-8编码的中文
        setupTextAreaForUTF8(commonPlaceholders);
        commonPlaceholders.setText(generateCommonPlaceholderText());
        
        JScrollPane commonScroll = new JScrollPane(commonPlaceholders);
        tabbedPane.addTab(helpI18n.getText("tool.edit.dialog.tab.common"), commonScroll);
        
        // 完整文档
        fullDoc = new JTextArea(4, 70);
        fullDoc.setEditable(false);
        // 设置支持中文的字体，优先使用等宽字体
//        fullDoc.setFont(getUnicodeFont(8));
        fullDoc.setBackground(new Color(248, 248, 248));
        // 确保正确显示UTF-8编码的中文
        setupTextAreaForUTF8(fullDoc);
        fullDoc.setText(generateCompactDocumentation());
        
        JScrollPane fullScroll = new JScrollPane(fullDoc);
        tabbedPane.addTab(helpI18n.getText("tool.edit.dialog.tab.full"), fullScroll);
        
        helpPanel.add(tabbedPane, BorderLayout.CENTER);
        
        return helpPanel;
    }
    
    /**
     * 获取支持Unicode的字体
     * @param size 字体大小
     * @return 字体对象
     */
    private Font getUnicodeFont(int size) {
        // 优先尝试等宽字体，支持中文
        String[] fontNames = {
            "JetBrains Mono",     // 现代等宽字体
            "Consolas",           // Windows等宽字体
            "Monaco",             // macOS等宽字体
            "DejaVu Sans Mono",   // Linux等宽字体
            "Courier New",        // 通用等宽字体
            "Microsoft YaHei",    // 中文支持字体
            "SimSun",             // 宋体
            "Dialog"              // Java默认字体
        };
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] availableFonts = ge.getAvailableFontFamilyNames();
        
        for (String fontName : fontNames) {
            for (String availableFont : availableFonts) {
                if (availableFont.equals(fontName)) {
                    return new Font(fontName, Font.PLAIN, size);
                }
            }
        }
        
        // 如果都没找到，使用系统默认等宽字体
        return new Font(Font.MONOSPACED, Font.PLAIN, size);
    }
    
    /**
     * 设置JTextArea以正确显示UTF-8编码的文本
     * @param textArea 文本区域组件
     */
    private void setupTextAreaForUTF8(JTextArea textArea) {
        // 确保使用UTF-8字符集
        textArea.putClientProperty("charset", "UTF-8");
        
        // 设置字符编码相关属性
        textArea.setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
        
        // 如果有必要，可以设置字符输入方法
        try {
            textArea.getInputContext().selectInputMethod(java.util.Locale.getDefault());
        } catch (Exception e) {
            // 忽略输入法设置失败的情况
        }
    }
    
    /**
     * 生成常用占位符文本
     * @return 常用占位符文本
     */
    private String generateCommonPlaceholderText() {
        I18nManager i18n = I18nManager.getInstance();
        return "# " + i18n.getText("tool.edit.placeholder.doc.request.basic") + "\n" +
               "%http.request.url%              - " + i18n.getText("tool.edit.placeholder.url") + "\n" +
               "%http.request.host%             - " + i18n.getText("tool.edit.placeholder.host") + "\n" +
               "%http.request.port%             - " + i18n.getText("tool.edit.placeholder.port") + "\n" +
               "%http.request.path%             - " + i18n.getText("tool.edit.placeholder.path") + "\n" +
               "%http.request.method%           - " + i18n.getText("tool.edit.placeholder.method") + "\n" +
               "\n" +
               "# " + i18n.getText("tool.edit.placeholder.doc.request.headers") + "\n" +
               "%http.request.headers.user.agent%    - " + i18n.getText("tool.edit.placeholder.user.agent") + "\n" +
               "%http.request.headers.cookies%       - " + i18n.getText("tool.edit.placeholder.cookies") + "\n" +
               "%http.request.headers.authorization% - " + i18n.getText("tool.edit.placeholder.authorization") + "\n" +
               "%http.request.headers.referer%       - " + i18n.getText("tool.edit.placeholder.referer") + "\n" +
               "\n" +
               "# " + i18n.getText("tool.edit.placeholder.doc.request.body") + "\n" +
               "%http.request.body%             - " + i18n.getText("tool.edit.placeholder.body") + "\n" +
               "%http.request.body.len%         - " + i18n.getText("tool.edit.placeholder.body.len");
    }
    
    /**
     * 生成紧凑的占位符文档
     * @return 文档字符串
     */
    private String generateCompactDocumentation() {
        I18nManager i18n = I18nManager.getInstance();
        return "# " + i18n.getText("tool.edit.placeholder.doc.title") + "\n\n" +
               "## " + i18n.getText("tool.edit.placeholder.doc.request.basic") + "\n" +
               "%http.request.url%              - " + i18n.getText("tool.edit.placeholder.url") + "\n" +
               "%http.request.host%             - " + i18n.getText("tool.edit.placeholder.host") + "\n" +
               "%http.request.port%             - " + i18n.getText("tool.edit.placeholder.port") + "\n" +
               "%http.request.path%             - " + i18n.getText("tool.edit.placeholder.path") + "\n" +
               "%http.request.query%            - " + i18n.getText("tool.edit.placeholder.query") + "\n" +
               "%http.request.method%           - " + i18n.getText("tool.edit.placeholder.method") + "\n" +
               "%http.request.protocol%         - " + i18n.getText("tool.edit.placeholder.protocol") + "\n\n" +
               
               "## " + i18n.getText("tool.edit.placeholder.doc.request.headers") + "\n" +
               "%http.request.headers.user.agent%    - " + i18n.getText("tool.edit.placeholder.user.agent") + "\n" +
               "%http.request.headers.cookies%       - " + i18n.getText("tool.edit.placeholder.cookies") + "\n" +
               "%http.request.headers.authorization% - " + i18n.getText("tool.edit.placeholder.authorization") + "\n" +
               "%http.request.headers.referer%       - " + i18n.getText("tool.edit.placeholder.referer") + "\n" +
               "%http.request.headers.content.type%  - " + i18n.getText("tool.edit.placeholder.content.type") + "\n\n" +
               
               "## " + i18n.getText("tool.edit.placeholder.doc.request.params") + "\n" +
               "%http.request.params.get%       - " + i18n.getText("tool.edit.placeholder.params.get") + "\n" +
               "%http.request.params.post%      - " + i18n.getText("tool.edit.placeholder.params.post") + "\n" +
               "%http.request.params.all%       - " + i18n.getText("tool.edit.placeholder.params.all") + "\n\n" +
               
               "## " + i18n.getText("tool.edit.placeholder.doc.request.body") + "\n" +
               "%http.request.body%             - " + i18n.getText("tool.edit.placeholder.body") + "\n" +
               "%http.request.body.len%         - " + i18n.getText("tool.edit.placeholder.body.len") + "\n\n" +
               
               "## " + i18n.getText("tool.edit.placeholder.doc.response.basic") + "\n" +
               "%http.response.status%          - " + i18n.getText("tool.edit.placeholder.response.status") + "\n" +
               "%http.response.headers%         - " + i18n.getText("tool.edit.placeholder.response.headers") + "\n" +
               "%http.response.body%            - " + i18n.getText("tool.edit.placeholder.response.body") + "\n" +
               "%http.response.body.len%        - " + i18n.getText("tool.edit.placeholder.response.body.len") + "\n\n" +
               
               i18n.getText("tool.edit.placeholder.doc.example") + ":\n" +
               "sqlmap -u \"%http.request.url%\" --cookie=\"%http.request.headers.cookies%\"";
    }
    
    /**
     * 创建按钮面板
     * @return 按钮面板
     */
    private JPanel createButtonPanel() {
        I18nManager buttonI18n = I18nManager.getInstance();
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        
        cancelButton = new JButton(buttonI18n.getText("tool.edit.dialog.button.cancel"));
        cancelButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        cancelButton.setPreferredSize(new Dimension(80, 30));
        
        okButton = new JButton(buttonI18n.getText("tool.edit.dialog.button.ok"));
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
        
        // 设置取消按钮事件
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
            
            // 如果工具已有分类，使用现有分类；否则根据工具名称推断分类
            String category;
            if (tool.getToolName() != null && !tool.getToolName().isEmpty()) {
                // 假设存在一个获取工具分类的方法，如果没有则推断
                category = inferCategory(tool.getToolName().toLowerCase());
            } else {
                category = inferCategory("");
            }
            categoryComboBox.setSelectedItem(category);
        } else {
            // 设置默认值
            favorCheckBox.setSelected(true);
            categoryComboBox.setSelectedIndex(0);
            
            // 设置默认模板
//            I18nManager loadI18n = I18nManager.getInstance();
//            commandArea.setText(loadI18n.getText("tool.edit.dialog.template.comment"));
        }
    }
    
    /**
     * 根据工具名称推断分类
     * @param toolName 工具名称
     * @return 分类
     */
    private String inferCategory(String toolName) {
        I18nManager categoryI18n = I18nManager.getInstance();
        if (toolName.contains("sql") || toolName.contains("inject")) {
            return categoryI18n.getText("tool.edit.category.sql.inject");
        } else if (toolName.contains("xss")) {
            return categoryI18n.getText("tool.edit.category.xss");
        } else if (toolName.contains("scan") || toolName.contains("dir")) {
            return categoryI18n.getText("tool.edit.category.scanner");
        } else if (toolName.contains("brute") || toolName.contains("hydra")) {
            return categoryI18n.getText("tool.edit.category.brute.force");
        } else if (toolName.contains("exploit") || toolName.contains("msf")) {
            return categoryI18n.getText("tool.edit.category.exploit");
        }
        return categoryI18n.getText("tool.edit.category.other");
    }
    
    /**
     * 将国际化的分类转换为内部分类代码
     * @param localizedCategory 国际化的分类名称
     * @return 内部分类代码
     */
    private String getCategoryCode(String localizedCategory) {
        I18nManager i18n = I18nManager.getInstance();
        if (localizedCategory.equals(i18n.getText("tool.edit.category.sql.inject"))) {
            return "sql-inject";
        } else if (localizedCategory.equals(i18n.getText("tool.edit.category.xss"))) {
            return "xss";
        } else if (localizedCategory.equals(i18n.getText("tool.edit.category.scanner"))) {
            return "scanner";
        } else if (localizedCategory.equals(i18n.getText("tool.edit.category.brute.force"))) {
            return "brute-force";
        } else if (localizedCategory.equals(i18n.getText("tool.edit.category.exploit"))) {
            return "exploit";
        } else {
            return "other";
        }
    }
    
    /**
     * 将内部分类代码转换为国际化的分类名称
     * @param categoryCode 内部分类代码
     * @return 国际化的分类名称
     */
    private String getLocalizedCategory(String categoryCode) {
        I18nManager i18n = I18nManager.getInstance();
        switch (categoryCode) {
            case "sql-inject":
                return i18n.getText("tool.edit.category.sql.inject");
            case "xss":
                return i18n.getText("tool.edit.category.xss");
            case "scanner":
                return i18n.getText("tool.edit.category.scanner");
            case "brute-force":
                return i18n.getText("tool.edit.category.brute.force");
            case "exploit":
                return i18n.getText("tool.edit.category.exploit");
            default:
                return i18n.getText("tool.edit.category.other");
        }
    }
    
    /**
     * 验证输入
     * @return 验证结果
     */
    private boolean validateInput() {
        I18nManager validateI18n = I18nManager.getInstance();
        String name = nameField.getText().trim();
        String command = commandArea.getText().trim();
        
        if (name.isEmpty()) {
            showError(validateI18n.getText("tool.edit.dialog.error.name.empty"));
            nameField.requestFocus();
            return false;
        }
        
        if (command.isEmpty()) {
            showError(validateI18n.getText("tool.edit.dialog.error.command.empty"));
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
        I18nManager errorI18n = I18nManager.getInstance();
        JOptionPane.showMessageDialog(this, message, errorI18n.getText("tool.edit.dialog.error.title"), JOptionPane.ERROR_MESSAGE);
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
        String localizedCategory = (String) categoryComboBox.getSelectedItem();
        return getCategoryCode(localizedCategory);
    }

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
        setTitle(tool == null ? i18n.getText("tool.edit.dialog.title.add") : i18n.getText("tool.edit.dialog.title.edit"));
        
        // 更新按钮文本
        if (okButton != null) {
            okButton.setText(i18n.getText("tool.edit.dialog.button.ok"));
        }
        if (cancelButton != null) {
            cancelButton.setText(i18n.getText("tool.edit.dialog.button.cancel"));
        }
        
        // 更新复选框文本
        if (favorCheckBox != null) {
            favorCheckBox.setText(i18n.getText("tool.edit.dialog.checkbox.add.favorites"));
        }
        
        // 更新选项卡标题
        if (tabbedPane != null) {
            tabbedPane.setTitleAt(0, i18n.getText("tool.edit.dialog.tab.common"));
            tabbedPane.setTitleAt(1, i18n.getText("tool.edit.dialog.tab.full"));
        }
        
        // 更新下拉框选项
        if (categoryComboBox != null) {
            Object selectedItem = categoryComboBox.getSelectedItem();
            categoryComboBox.removeAllItems();
            categoryComboBox.addItem(i18n.getText("tool.edit.category.sql.inject"));
            categoryComboBox.addItem(i18n.getText("tool.edit.category.xss"));
            categoryComboBox.addItem(i18n.getText("tool.edit.category.scanner"));
            categoryComboBox.addItem(i18n.getText("tool.edit.category.brute.force"));
            categoryComboBox.addItem(i18n.getText("tool.edit.category.exploit"));
            categoryComboBox.addItem(i18n.getText("tool.edit.category.other"));
            
            // 尝试恢复原来的选择
            if (selectedItem != null) {
                categoryComboBox.setSelectedItem(selectedItem);
            }
        }
        
        // 重新生成文档内容
        if (commonPlaceholders != null) {
            commonPlaceholders.setText(generateCommonPlaceholderText());
        }
        if (fullDoc != null) {
            fullDoc.setText(generateCompactDocumentation());
        }
    }
} 