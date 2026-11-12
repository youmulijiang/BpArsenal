package view.component;

import model.HttpTool;
import model.HttpToolCommand;
import controller.ToolController;
import util.I18nManager;
import util.PlaceholderDocumentation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * HTTP工具编辑对话框 (View层组件)
 * 用于添加和编辑HTTP工具配置
 */
public class ToolEditDialog extends JDialog implements I18nManager.LanguageChangeListener {

    private HttpTool tool;
    private HttpToolCommand toolCommand;  // 当前编辑的具体命令
    private boolean confirmed = false;

    // 编辑时的真实分类；用户未主动修改分类下拉框时保持该值，避免编辑时被错误归类
    private String originalCategory;
    private boolean categoryChangedByUser = false;
    private boolean suppressCategoryEvents = false;

    private JTextField nameField;
    private JTextArea commandArea;
    private JTextField noteField;
    private JTextField workDirField;
    private JCheckBox favorCheckBox;
    private JComboBox<String> categoryComboBox;
    private JButton okButton;
    private JButton cancelButton;
    private JTabbedPane tabbedPane;
    private JTextArea commonPlaceholders;
    private JTextArea fullDoc;
    
    public ToolEditDialog(Window parent, HttpTool tool) {
        this(parent, tool, null);
    }
    
    public ToolEditDialog(Window parent, HttpTool tool, HttpToolCommand toolCommand) {
        super(parent);
        this.tool = tool;
        this.toolCommand = toolCommand;
        
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

        // 用户主动修改分类时记录标记（程序性 setSelectedItem 会被 suppressCategoryEvents 抑制）
        categoryComboBox.addActionListener(e -> {
            if (!suppressCategoryEvents) {
                categoryChangedByUser = true;
            }
        });

        // 关闭时移除语言监听器，避免监听器泄漏（注册在构造函数中）
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                I18nManager.getInstance().removeLanguageChangeListener(ToolEditDialog.this);
            }
        });
    }

    /**
     * 静默设置分类下拉框选中项（匹配原始type值或对应的内部分类代码）
     * @param category 真实分类类型
     */
    private void selectCategorySilently(String category) {
        if (category == null || categoryComboBox == null) {
            return;
        }
        suppressCategoryEvents = true;
        try {
            for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
                String item = categoryComboBox.getItemAt(i);
                if (item.equals(category) || getCategoryCode(item).equals(category)) {
                    categoryComboBox.setSelectedIndex(i);
                    return;
                }
            }
        } finally {
            suppressCategoryEvents = false;
        }
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
        
        // 备注
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0; gbc.weighty = 0;
        gbc.insets = new Insets(5, 5, 5, 10);
        formPanel.add(new JLabel(formI18n.getText("tool.edit.dialog.label.note")), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        noteField = new JTextField(20);
        noteField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        formPanel.add(noteField, gbc);
        
        // 工作目录
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0; gbc.weighty = 0;
        gbc.insets = new Insets(5, 5, 5, 10);
        formPanel.add(new JLabel(formI18n.getText("tool.edit.dialog.label.work.dir")), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        workDirField = new JTextField(20);
        workDirField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        formPanel.add(workDirField, gbc);
        
        // 分类选择
        gbc.gridx = 0; gbc.gridy = 3;
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
        gbc.gridx = 0; gbc.gridy = 4;
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
        gbc.gridx = 0; gbc.gridy = 5;
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
        
        // 添加双击提示标签
//        JLabel hintLabel = new JLabel(helpI18n.getText("placeholder.help.dialog.description"));
//        hintLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));
//        hintLabel.setForeground(new Color(102, 102, 102));
//        hintLabel.setHorizontalAlignment(SwingConstants.CENTER);
////        hintLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
//        hintLabel.setName("hintLabel"); // 设置名称以便在updateUITexts中找到
//        helpPanel.add(hintLabel, BorderLayout.NORTH);
        
        tabbedPane = new JTabbedPane();
        
        // 常用占位符
        commonPlaceholders = new JTextArea(4, 70);
        commonPlaceholders.setEditable(false);
//        commonPlaceholders.setFont(getUnicodeFont(9));
        commonPlaceholders.setBackground(new Color(248, 248, 248));
        // 确保正确显示UTF-8编码的中文
        setupTextAreaForUTF8(commonPlaceholders);
        commonPlaceholders.setText(generateCommonPlaceholderText());
        
        // 添加双击事件监听器和工具提示
        commonPlaceholders.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showPlaceholderHelpDialog();
                }
            }
        });
        commonPlaceholders.setToolTipText("双击查看完整的占位符变量文档");
        
        JScrollPane commonScroll = new JScrollPane(commonPlaceholders);
        tabbedPane.addTab(helpI18n.getText("tool.edit.dialog.tab.common"), commonScroll);
        
        // 完整文档
        fullDoc = new JTextArea(4, 70);
        fullDoc.setEditable(false);
//        fullDoc.setFont(getUnicodeFont(8));
        fullDoc.setBackground(new Color(248, 248, 248));
        // 确保正确显示UTF-8编码的中文
        setupTextAreaForUTF8(fullDoc);
        fullDoc.setText(generateCompactDocumentation());
        
        // 添加双击事件监听器和工具提示
        fullDoc.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showPlaceholderHelpDialog();
                }
            }
        });
        fullDoc.setToolTipText("双击查看完整的占位符变量文档");
        
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
        StringBuilder text = new StringBuilder();
        
        // 使用PlaceholderDocumentation获取常用变量
        text.append("=== 常用HTTP占位符变量 ===\n\n");
        
        // 请求基础信息
        text.append("📋 请求基础信息：\n");
        for (PlaceholderDocumentation.PlaceholderVariable var : PlaceholderDocumentation.getRequestBasicVariables()) {
            text.append(String.format("  %-35s - %s\n", var.getName(), var.getDescription()));
        }
        text.append("\n");
        
        // 请求头部信息（只显示常用的）
        text.append("📋 常用请求头部：\n");
        text.append("  %http.request.headers.user.agent%    - User agent字符串\n");
        text.append("  %http.request.headers.cookies%       - 完整Cookie字符串\n");
        text.append("  %http.request.headers.authorization% - 认证头信息\n");
        text.append("  %http.request.headers.referer%       - 来源页面\n");
        text.append("  %http.request.headers.content.type%  - 请求内容类型\n");
        text.append("\n");
        
        // 请求体信息
        text.append("📋 请求体信息：\n");
        text.append("  %http.request.body%                  - 完整请求体内容\n");
        text.append("  %http.request.body.len%              - 请求体长度\n");
        text.append("\n");
        
        // 响应信息
        text.append("📋 响应信息：\n");
        text.append("  %http.response.status%               - HTTP响应状态码\n");
        text.append("  %http.response.body%                 - 完整响应体内容\n");
        text.append("  %http.response.body.len%             - 响应体长度\n");
        text.append("\n");
        
        // 使用提示
        text.append("💡 提示：双击此面板可查看完整的占位符文档");
        
        return text.toString();
    }
    
    /**
     * 生成紧凑的占位符文档
     * @return 文档字符串
     */
    private String generateCompactDocumentation() {
        // 直接使用PlaceholderDocumentation的完整文档
        return PlaceholderDocumentation.getAllPlaceholderDocumentation();
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
            
            // 如果有具体的命令对象，使用命令对象的数据
            if (toolCommand != null) {
                commandArea.setText(toolCommand.getCommand());
                favorCheckBox.setSelected(toolCommand.isFavor());
                noteField.setText(toolCommand.getNote() != null ? toolCommand.getNote() : "");
                workDirField.setText(toolCommand.getWorkDir() != null ? toolCommand.getWorkDir() : "");
            } else {
                // 否则使用工具的默认数据
                commandArea.setText(tool.getCommand());
                favorCheckBox.setSelected(tool.isFavor());
                noteField.setText("");
                workDirField.setText("");
            }
            
            // 优先使用真实分类（命令对象或按工具名查询），推断仅作兜底展示
            String category;
            if (toolCommand != null && toolCommand.getCategory() != null) {
                category = toolCommand.getCategory();
            } else if (tool != null && tool.getToolName() != null && !tool.getToolName().isEmpty()) {
                String type = ToolController.getInstance().getToolCategoryType(tool.getToolName());
                category = type != null ? type : inferCategory(tool.getToolName().toLowerCase());
            } else {
                category = inferCategory("");
            }
            selectCategorySilently(category);
            originalCategory = category;
        } else {
            // 设置默认值
            favorCheckBox.setSelected(true);
            categoryComboBox.setSelectedIndex(0);
            noteField.setText("");
            workDirField.setText("");
            
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
        
        // 保存note和workDir - 注意：这里可能需要调整数据模型
        // 目前HttpTool没有这些字段，需要另外处理
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
     * 编辑已有工具且用户未主动修改分类时，返回原分类，避免编辑操作把工具静默移到错误分类
     * @return 分类字符串
     */
    public String getSelectedCategory() {
        if (!categoryChangedByUser && originalCategory != null) {
            return originalCategory;
        }
        String localizedCategory = (String) categoryComboBox.getSelectedItem();
        return getCategoryCode(localizedCategory);
    }
    
    /**
     * 获取备注
     * @return 备注字符串
     */
    public String getNote() {
        return noteField.getText().trim();
    }
    
    /**
     * 获取工作目录
     * @return 工作目录字符串
     */
    public String getWorkDir() {
        return workDirField.getText().trim();
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
     * 显示占位符帮助对话框
     */
    private void showPlaceholderHelpDialog() {
        PlaceholderHelpDialog helpDialog = new PlaceholderHelpDialog(this);
        helpDialog.setVisible(true);
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
            suppressCategoryEvents = true;
            try {
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
            } finally {
                suppressCategoryEvents = false;
            }
        }
        
        // 重新生成文档内容（如果需要国际化的话，这里可以重新生成）
        if (commonPlaceholders != null) {
            commonPlaceholders.setText(generateCommonPlaceholderText());
        }
        if (fullDoc != null) {
            fullDoc.setText(generateCompactDocumentation());
        }
    }
} 