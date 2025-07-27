package view.component;

import model.WebSite;
import util.I18nManager;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

/**
 * 网站编辑对话框
 * 用于添加和编辑网站
 */
public class WebSiteEditDialog extends JDialog implements I18nManager.LanguageChangeListener {
    
    private JTextField urlField;
    private JTextField descField;
    private JCheckBox favorCheckBox;
    private JComboBox<String> categoryCombo;
    private JButton confirmButton;
    private JButton cancelButton;
    private JButton testButton;
    
    private WebSite website;
    private boolean confirmed = false;
    private boolean isEditing = false;
    
    public WebSiteEditDialog(Window parent, WebSite website) {
        super(parent);
        this.website = website;
        this.isEditing = (website != null);
        
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
        setTitle(website == null ? i18n.getText("website.edit.dialog.title.add") : i18n.getText("website.edit.dialog.title.edit"));
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
            basicI18n.getText("website.edit.dialog.border.website.info"),
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 12)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 网站描述
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel descLabel = new JLabel(basicI18n.getText("website.edit.dialog.label.name"));
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        basicPanel.add(descLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        descField = new JTextField(25);
        descField.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        descField.setToolTipText(basicI18n.getText("website.edit.dialog.tooltip.name"));
        basicPanel.add(descField, gbc);
        
        // 网站URL
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel urlLabel = new JLabel(basicI18n.getText("website.edit.dialog.label.url"));
        urlLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        basicPanel.add(urlLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        urlField = new JTextField(25);
        urlField.setFont(new Font("Consolas", Font.PLAIN, 11));
        urlField.setToolTipText(basicI18n.getText("website.edit.dialog.tooltip.url"));
        basicPanel.add(urlField, gbc);
        
        // 测试按钮
        gbc.gridx = 2; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        testButton = new JButton(basicI18n.getText("website.edit.dialog.button.test"));
        testButton.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        testButton.setPreferredSize(new Dimension(60, 25));
        testButton.setBackground(new Color(102, 187, 106));
        testButton.setForeground(Color.WHITE);
        testButton.setFocusPainted(false);
        testButton.setToolTipText(basicI18n.getText("website.edit.dialog.tooltip.test"));
        basicPanel.add(testButton, gbc);
        
        // 分类
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel categoryLabel = new JLabel(basicI18n.getText("website.edit.dialog.label.category"));
        categoryLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        basicPanel.add(categoryLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        categoryCombo = new JComboBox<>(new String[]{
            basicI18n.getText("website.category.osint"),
            basicI18n.getText("website.category.recon"),
            basicI18n.getText("website.category.vulnerability.db"),
            basicI18n.getText("website.category.online.tools"),
            basicI18n.getText("website.category.learning"),
            basicI18n.getText("website.category.other")
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
            settingsI18n.getText("website.edit.dialog.border.settings"),
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 12)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 收藏设置
        gbc.gridx = 0; gbc.gridy = 0;
        favorCheckBox = new JCheckBox(settingsI18n.getText("website.edit.dialog.checkbox.favor"));
        favorCheckBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        favorCheckBox.setToolTipText(settingsI18n.getText("website.edit.dialog.tooltip.favor"));
        settingsPanel.add(favorCheckBox, gbc);
        
        return settingsPanel;
    }
    
    /**
     * 创建按钮面板
     * @return 按钮面板
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEtchedBorder());
        
        I18nManager buttonI18n = I18nManager.getInstance();
        confirmButton = new JButton(isEditing ? buttonI18n.getText("website.edit.dialog.button.update") : buttonI18n.getText("website.edit.dialog.button.add"));
        confirmButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        confirmButton.setPreferredSize(new Dimension(80, 30));
        confirmButton.setBackground(new Color(46, 125, 50));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFocusPainted(false);
        
        cancelButton = new JButton(buttonI18n.getText("website.edit.dialog.button.cancel"));
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
        
        testButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                testWebsite();
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
        if (website != null) {
            descField.setText(website.getDesc());
            urlField.setText(website.getUrl());
            favorCheckBox.setSelected(website.isFavor());
            
            // 设置分类
            String category = getCategoryDisplayName(website);
            for (int i = 0; i < categoryCombo.getItemCount(); i++) {
                if (categoryCombo.getItemAt(i).equals(category)) {
                    categoryCombo.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            // 默认值
            favorCheckBox.setSelected(false);
            categoryCombo.setSelectedIndex(0);
        }
    }
    
    /**
     * 验证输入
     * @return 是否有效
     */
    private boolean validateInput() {
        I18nManager validateI18n = I18nManager.getInstance();
        String desc = descField.getText().trim();
        String url = urlField.getText().trim();
        
        if (desc.isEmpty()) {
            showError(validateI18n.getText("website.edit.dialog.error.name.empty"));
            descField.requestFocus();
            return false;
        }
        
        if (url.isEmpty()) {
            showError(validateI18n.getText("website.edit.dialog.error.url.empty"));
            urlField.requestFocus();
            return false;
        }
        
        // 验证URL格式
        if (!isValidUrl(url)) {
            showError(validateI18n.getText("website.edit.dialog.error.url.invalid"));
            urlField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * 验证URL格式
     * @param url URL字符串
     * @return 是否有效
     */
    private boolean isValidUrl(String url) {
        try {
            new URL(url);
            return url.startsWith("http://") || url.startsWith("https://");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 测试网站
     */
    private void testWebsite() {
        I18nManager testI18n = I18nManager.getInstance();
        String url = urlField.getText().trim();
        
        if (url.isEmpty()) {
            showError(testI18n.getText("website.edit.dialog.error.url.test.empty"));
            return;
        }
        
        if (!isValidUrl(url)) {
            showError(testI18n.getText("website.edit.dialog.error.url.test.invalid"));
            return;
        }
        
        try {
            // 在系统默认浏览器中打开网站
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(new URL(url).toURI());
                showInfo(testI18n.getText("website.edit.dialog.info.opening", url));
            } else {
                showError(testI18n.getText("website.edit.dialog.error.browser.not.supported"));
            }
        } catch (Exception e) {
            showError(testI18n.getText("website.edit.dialog.error.open.failed", e.getMessage()));
        }
    }
    
    /**
     * 保存数据
     */
    private void saveData() {
        if (website == null) {
            website = new WebSite();
        }
        
        website.setDesc(descField.getText().trim());
        website.setUrl(urlField.getText().trim());
        website.setFavor(favorCheckBox.isSelected());
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
            errorI18n.getText("website.edit.dialog.error.title"),
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * 显示信息消息
     * @param message 信息消息
     */
    private void showInfo(String message) {
        I18nManager infoI18n = I18nManager.getInstance();
        JOptionPane.showMessageDialog(
            this,
            message,
            infoI18n.getText("website.edit.dialog.info.title"),
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * 获取分类显示名称
     * @param website 网站对象
     * @return 分类名称
     */
    private String getCategoryDisplayName(WebSite website) {
        // 这里可以根据网站来源获取分类，暂时返回默认值
        return "OSINT";
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
        if (localizedCategory.equals(i18n.getText("website.category.osint"))) {
            return "OSINT";
        } else if (localizedCategory.equals(i18n.getText("website.category.recon"))) {
            return "Recon";
        } else if (localizedCategory.equals(i18n.getText("website.category.vulnerability.db"))) {
            return "漏洞库";
        } else if (localizedCategory.equals(i18n.getText("website.category.online.tools"))) {
            return "tools";
        } else if (localizedCategory.equals(i18n.getText("website.category.learning"))) {
            return "learning";
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
            case "OSINT":
                return i18n.getText("website.category.osint");
            case "Recon":
                return i18n.getText("website.category.recon");
            case "漏洞库":
                return i18n.getText("website.category.vulnerability.db");
            case "tools":
                return i18n.getText("website.category.online.tools");
            case "learning":
                return i18n.getText("website.category.learning");
            default:
                return i18n.getText("website.category.other");
        }
    }
    
    /**
     * 获取网站对象
     * @return 网站对象
     */
    public WebSite getWebSite() {
        return website;
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
        setTitle(website == null ? i18n.getText("website.edit.dialog.title.add") : i18n.getText("website.edit.dialog.title.edit"));
        
        // 更新按钮文本
        if (confirmButton != null) {
            confirmButton.setText(isEditing ? i18n.getText("website.edit.dialog.button.update") : i18n.getText("website.edit.dialog.button.add"));
        }
        if (cancelButton != null) {
            cancelButton.setText(i18n.getText("website.edit.dialog.button.cancel"));
        }
        if (testButton != null) {
            testButton.setText(i18n.getText("website.edit.dialog.button.test"));
        }
        
        // 更新复选框文本
        if (favorCheckBox != null) {
            favorCheckBox.setText(i18n.getText("website.edit.dialog.checkbox.favor"));
        }
        
        // 更新工具提示
        updateTooltips(i18n);
        
        // 更新分类下拉框
        updateCategoryCombo();
    }
    
    /**
     * 更新工具提示
     */
    private void updateTooltips(I18nManager i18n) {
        if (descField != null) {
            descField.setToolTipText(i18n.getText("website.edit.dialog.tooltip.name"));
        }
        if (urlField != null) {
            urlField.setToolTipText(i18n.getText("website.edit.dialog.tooltip.url"));
        }
        if (testButton != null) {
            testButton.setToolTipText(i18n.getText("website.edit.dialog.tooltip.test"));
        }
        if (favorCheckBox != null) {
            favorCheckBox.setToolTipText(i18n.getText("website.edit.dialog.tooltip.favor"));
        }
    }
    
    /**
     * 更新分类下拉框
     */
    private void updateCategoryCombo() {
        if (categoryCombo != null) {
            I18nManager i18n = I18nManager.getInstance();
            Object selectedItem = categoryCombo.getSelectedItem();
            categoryCombo.removeAllItems();
            categoryCombo.addItem(i18n.getText("website.category.osint"));
            categoryCombo.addItem(i18n.getText("website.category.recon"));
            categoryCombo.addItem(i18n.getText("website.category.vulnerability.db"));
            categoryCombo.addItem(i18n.getText("website.category.online.tools"));
            categoryCombo.addItem(i18n.getText("website.category.learning"));
            categoryCombo.addItem(i18n.getText("website.category.other"));
            
            // 尝试恢复选择
            if (selectedItem != null) {
                categoryCombo.setSelectedItem(selectedItem);
            }
        }
    }
} 