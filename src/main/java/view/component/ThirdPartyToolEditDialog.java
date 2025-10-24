package view.component;

import model.ThirdPartyTool;
import util.I18nManager;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 第三方工具编辑对话框
 * 用于添加和编辑第三方工具
 */
public class ThirdPartyToolEditDialog extends JDialog implements I18nManager.LanguageChangeListener {
    
    private JTextField toolNameField;
    private JTextField startCommandField;
    private JTextField workDirField;
    private JButton browseButton;
    private JCheckBox favorCheckBox;
    private JCheckBox autoStartCheckBox;
    private JComboBox<String> categoryCombo;
    private JButton confirmButton;
    private JButton cancelButton;
    
    private ThirdPartyTool tool;
    private boolean confirmed = false;
    private boolean isEditing = false;
    
    public ThirdPartyToolEditDialog(Window parent, ThirdPartyTool tool) {
        super(parent);
        this.tool = tool;
        this.isEditing = (tool != null);
        
        // 注册语言变更监听器
        I18nManager.getInstance().addLanguageChangeListener(this);
        
        initializeUI();
        setupEventHandlers();
        loadData();
        
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }
    
    /**
     * 初始化UI组件
     */
    private void initializeUI() {
        I18nManager i18n = I18nManager.getInstance();
        
        // 设置对话框标题和属性
        setTitle(tool == null ? i18n.getText("thirdparty.edit.dialog.title.add") : i18n.getText("thirdparty.edit.dialog.title.edit"));
        setModalityType(ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(10, 10));
        
        // 创建主面板
        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);
        
        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 设置对话框属性
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        getRootPane().setDefaultButton(confirmButton);
    }
    
    /**
     * 创建主面板
     * @return 主面板
     */
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        // 基本信息面板
        JPanel basicPanel = createBasicInfoPanel();
        mainPanel.add(basicPanel);
        
        mainPanel.add(Box.createVerticalStrut(15));
        
        // 设置面板
        JPanel settingsPanel = createSettingsPanel();
        mainPanel.add(settingsPanel);
        
        return mainPanel;
    }
    
    /**
     * 创建基本信息面板
     * @return 基本信息面板
     */
    private JPanel createBasicInfoPanel() {
        I18nManager basicI18n = I18nManager.getInstance();
        JPanel basicPanel = new JPanel(new GridBagLayout());
        basicPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            basicI18n.getText("thirdparty.edit.dialog.border.basic.info"),
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 12)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 工具名称
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameLabel = new JLabel(basicI18n.getText("thirdparty.edit.dialog.label.tool.name"));
        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        basicPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        toolNameField = new JTextField(25);
        toolNameField.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        toolNameField.setToolTipText(basicI18n.getText("thirdparty.edit.dialog.tooltip.name"));
        basicPanel.add(toolNameField, gbc);
        
        // 启动命令
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel commandLabel = new JLabel(basicI18n.getText("thirdparty.edit.dialog.label.start.command"));
        commandLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        basicPanel.add(commandLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        startCommandField = new JTextField(25);
        startCommandField.setFont(new Font("Consolas", Font.PLAIN, 11));
        startCommandField.setToolTipText(basicI18n.getText("thirdparty.edit.dialog.tooltip.command"));
        basicPanel.add(startCommandField, gbc);
        
        // 工作目录
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel workDirLabel = new JLabel(basicI18n.getText("thirdparty.edit.dialog.label.work.dir"));
        workDirLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        basicPanel.add(workDirLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JPanel workDirPanel = new JPanel(new BorderLayout(5, 0));
        workDirField = new JTextField(20);
        workDirField.setFont(new Font("Consolas", Font.PLAIN, 11));
        workDirField.setToolTipText(basicI18n.getText("thirdparty.edit.dialog.tooltip.work.dir"));
        browseButton = new JButton(basicI18n.getText("button.browse"));
        browseButton.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        browseButton.setPreferredSize(new Dimension(60, 25));
        browseButton.addActionListener(e -> browseWorkDir());
        workDirPanel.add(workDirField, BorderLayout.CENTER);
        workDirPanel.add(browseButton, BorderLayout.EAST);
        basicPanel.add(workDirPanel, gbc);
        
        // 分类
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel categoryLabel = new JLabel(basicI18n.getText("thirdparty.edit.dialog.label.category"));
        categoryLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        basicPanel.add(categoryLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        categoryCombo = new JComboBox<>(new String[]{
            basicI18n.getText("thirdparty.category.penetration"),
            basicI18n.getText("thirdparty.category.editor"),
            basicI18n.getText("thirdparty.category.network"),
            basicI18n.getText("thirdparty.category.analysis"),
            basicI18n.getText("thirdparty.category.other")
        });
        categoryCombo.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        categoryCombo.setEditable(true);
        basicPanel.add(categoryCombo, gbc);
        
        return basicPanel;
    }
    
    /**
     * 创建设置面板
     * @return 设置面板
     */
    private JPanel createSettingsPanel() {
        I18nManager settingsI18n = I18nManager.getInstance();
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            settingsI18n.getText("thirdparty.edit.dialog.border.settings"),
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 12)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 收藏设置
        gbc.gridx = 0; gbc.gridy = 0;
        favorCheckBox = new JCheckBox(settingsI18n.getText("thirdparty.edit.dialog.checkbox.favor"));
        favorCheckBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        favorCheckBox.setToolTipText(settingsI18n.getText("thirdparty.edit.dialog.tooltip.favor"));
        settingsPanel.add(favorCheckBox, gbc);
        
        // 自动启动设置
        gbc.gridx = 1; gbc.gridy = 0;
        autoStartCheckBox = new JCheckBox(settingsI18n.getText("thirdparty.edit.dialog.checkbox.auto.start"));
        autoStartCheckBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        autoStartCheckBox.setToolTipText(settingsI18n.getText("thirdparty.edit.dialog.tooltip.auto.start"));
        settingsPanel.add(autoStartCheckBox, gbc);
        
        return settingsPanel;
    }
    
    /**
     * 浏览工作目录
     */
    private void browseWorkDir() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle(I18nManager.getInstance().getText("thirdparty.edit.dialog.browse.work.dir"));
        
        // 设置当前目录
        String currentDir = workDirField.getText().trim();
        if (!currentDir.isEmpty()) {
            fileChooser.setCurrentDirectory(new java.io.File(currentDir));
        }
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            workDirField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    /**
     * 创建按钮面板
     * @return 按钮面板
     */
    private JPanel createButtonPanel() {
        I18nManager buttonI18n = I18nManager.getInstance();
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEtchedBorder());
        
        confirmButton = new JButton(isEditing ? buttonI18n.getText("thirdparty.edit.dialog.button.update") : buttonI18n.getText("thirdparty.edit.dialog.button.add"));
        confirmButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        confirmButton.setPreferredSize(new Dimension(80, 30));
        confirmButton.setBackground(new Color(46, 125, 50));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFocusPainted(false);
        
        cancelButton = new JButton(buttonI18n.getText("thirdparty.edit.dialog.button.cancel"));
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        cancelButton.setPreferredSize(new Dimension(80, 30));
        cancelButton.setBackground(new Color(158, 158, 158));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(confirmButton);
        
        return buttonPanel;
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateInput()) {
                    saveData();
                    confirmed = true;
                    dispose();
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = false;
                dispose();
            }
        });
        
        // ESC键取消
        getRootPane().registerKeyboardAction(
            e -> {
                confirmed = false;
                dispose();
            },
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }
    
    /**
     * 加载数据
     */
    private void loadData() {
        if (tool != null) {
            toolNameField.setText(tool.getToolName());
            startCommandField.setText(tool.getStartCommand());
            workDirField.setText(tool.getWorkDir() != null ? tool.getWorkDir() : "");
            favorCheckBox.setSelected(tool.isFavor());
            autoStartCheckBox.setSelected(tool.isAutoStart());
            
            // 设置分类
            String category = getCategoryDisplayName(tool);
            for (int i = 0; i < categoryCombo.getItemCount(); i++) {
                if (categoryCombo.getItemAt(i).equals(category)) {
                    categoryCombo.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            // 默认值
            workDirField.setText("");
            favorCheckBox.setSelected(false);
            autoStartCheckBox.setSelected(false);
            categoryCombo.setSelectedIndex(0);
        }
    }
    
    /**
     * 验证输入
     * @return 是否有效
     */
    private boolean validateInput() {
        I18nManager validateI18n = I18nManager.getInstance();
        String toolName = toolNameField.getText().trim();
        String startCommand = startCommandField.getText().trim();
        
        if (toolName.isEmpty()) {
            showError(validateI18n.getText("thirdparty.edit.dialog.error.name.empty"));
            toolNameField.requestFocus();
            return false;
        }
        
        if (startCommand.isEmpty()) {
            showError(validateI18n.getText("thirdparty.edit.dialog.error.command.empty"));
            startCommandField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * 保存数据
     */
    private void saveData() {
        if (tool == null) {
            tool = new ThirdPartyTool();
        }
        
        tool.setToolName(toolNameField.getText().trim());
        tool.setStartCommand(startCommandField.getText().trim());
        tool.setWorkDir(workDirField.getText().trim());
        tool.setFavor(favorCheckBox.isSelected());
        tool.setAutoStart(autoStartCheckBox.isSelected());
    }
    
    /**
     * 显示错误消息
     * @param message 错误消息
     */
    private void showError(String message) {
        I18nManager errorI18n = I18nManager.getInstance();
        JOptionPane.showMessageDialog(
            this,
            message,
            errorI18n.getText("thirdparty.edit.dialog.error.title"),
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * 获取分类显示名称
     * @param tool 工具对象
     * @return 分类名称
     */
    private String getCategoryDisplayName(ThirdPartyTool tool) {
        // 这里可以根据工具来源获取分类，暂时返回默认值
        return "渗透工具";
    }
    
    /**
     * 获取选中的分类
     * @return 分类类型
     */
    public String getSelectedCategory() {
        String displayName = (String) categoryCombo.getSelectedItem();
        return getCategoryCode(displayName);
    }
    
    /**
     * 将国际化的分类转换为内部分类代码
     * @param localizedCategory 国际化的分类名称
     * @return 内部分类代码
     */
    private String getCategoryCode(String localizedCategory) {
        I18nManager i18n = I18nManager.getInstance();
        if (localizedCategory.equals(i18n.getText("thirdparty.category.penetration"))) {
            return "exploit";
        } else if (localizedCategory.equals(i18n.getText("thirdparty.category.editor"))) {
            return "编辑器";
        } else if (localizedCategory.equals(i18n.getText("thirdparty.category.network"))) {
            return "network";
        } else if (localizedCategory.equals(i18n.getText("thirdparty.category.analysis"))) {
            return "analysis";
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
            case "exploit":
                return i18n.getText("thirdparty.category.penetration");
            case "编辑器":
                return i18n.getText("thirdparty.category.editor");
            case "network":
                return i18n.getText("thirdparty.category.network");
            case "analysis":
                return i18n.getText("thirdparty.category.analysis");
            default:
                return i18n.getText("thirdparty.category.other");
        }
    }
    
    /**
     * 获取工具对象
     * @return 工具对象
     */
    public ThirdPartyTool getTool() {
        return tool;
    }
    
    /**
     * 是否确认
     * @return 确认状态
     */
    public boolean isConfirmed() {
        return confirmed;
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
        setTitle(tool == null ? i18n.getText("thirdparty.edit.dialog.title.add") : i18n.getText("thirdparty.edit.dialog.title.edit"));
        
        // 更新按钮文本
        if (confirmButton != null) {
            confirmButton.setText(isEditing ? i18n.getText("thirdparty.edit.dialog.button.update") : i18n.getText("thirdparty.edit.dialog.button.add"));
        }
        if (cancelButton != null) {
            cancelButton.setText(i18n.getText("thirdparty.edit.dialog.button.cancel"));
        }
        
        // 更新复选框文本
        if (favorCheckBox != null) {
            favorCheckBox.setText(i18n.getText("thirdparty.edit.dialog.checkbox.favor"));
        }
        if (autoStartCheckBox != null) {
            autoStartCheckBox.setText(i18n.getText("thirdparty.edit.dialog.checkbox.auto.start"));
        }
        
        // 更新分类下拉框
        updateCategoryCombo();
    }
    
    /**
     * 更新分类下拉框
     */
    private void updateCategoryCombo() {
        if (categoryCombo != null) {
            I18nManager i18n = I18nManager.getInstance();
            Object selectedItem = categoryCombo.getSelectedItem();
            categoryCombo.removeAllItems();
            categoryCombo.addItem(i18n.getText("thirdparty.category.penetration"));
            categoryCombo.addItem(i18n.getText("thirdparty.category.editor"));
            categoryCombo.addItem(i18n.getText("thirdparty.category.network"));
            categoryCombo.addItem(i18n.getText("thirdparty.category.analysis"));
            categoryCombo.addItem(i18n.getText("thirdparty.category.other"));
            
            // 尝试恢复选择
            if (selectedItem != null) {
                categoryCombo.setSelectedItem(selectedItem);
            }
        }
    }
} 