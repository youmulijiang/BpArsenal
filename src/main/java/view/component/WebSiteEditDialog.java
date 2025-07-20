package view.component;

import model.WebSite;
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
public class WebSiteEditDialog extends JDialog {
    
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
        super(parent, website == null ? "添加网站" : "编辑网站", ModalityType.APPLICATION_MODAL);
        this.website = website;
        this.isEditing = (website != null);
        
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
        JPanel basicPanel = new JPanel(new GridBagLayout());
        basicPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "网站信息",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 12)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 网站描述
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel descLabel = new JLabel("网站名称:");
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        basicPanel.add(descLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        descField = new JTextField(25);
        descField.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        descField.setToolTipText("输入网站名称，如: Google搜索, Shodan");
        basicPanel.add(descField, gbc);
        
        // 网站URL
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel urlLabel = new JLabel("网站地址:");
        urlLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        basicPanel.add(urlLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        urlField = new JTextField(25);
        urlField.setFont(new Font("Consolas", Font.PLAIN, 11));
        urlField.setToolTipText("输入完整的网站地址，如: https://www.google.com");
        basicPanel.add(urlField, gbc);
        
        // 测试按钮
        gbc.gridx = 2; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        testButton = new JButton("测试");
        testButton.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        testButton.setPreferredSize(new Dimension(60, 25));
        testButton.setBackground(new Color(102, 187, 106));
        testButton.setForeground(Color.WHITE);
        testButton.setFocusPainted(false);
        testButton.setToolTipText("测试网站是否可访问");
        basicPanel.add(testButton, gbc);
        
        // 分类
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel categoryLabel = new JLabel("网站分类:");
        categoryLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        basicPanel.add(categoryLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        categoryCombo = new JComboBox<>(new String[]{
            "OSINT", "信息收集", "漏洞库", "在线工具", "学习资源", "其他"
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
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "设置选项",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 12)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 收藏设置
        gbc.gridx = 0; gbc.gridy = 0;
        favorCheckBox = new JCheckBox("收藏此网站");
        favorCheckBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        favorCheckBox.setToolTipText("将此网站标记为收藏");
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
        
        confirmButton = new JButton(isEditing ? "更新" : "添加");
        confirmButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        confirmButton.setPreferredSize(new Dimension(80, 30));
        confirmButton.setBackground(new Color(46, 125, 50));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFocusPainted(false);
        
        cancelButton = new JButton("取消");
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
        String desc = descField.getText().trim();
        String url = urlField.getText().trim();
        
        if (desc.isEmpty()) {
            showError("网站名称不能为空");
            descField.requestFocus();
            return false;
        }
        
        if (url.isEmpty()) {
            showError("网站地址不能为空");
            urlField.requestFocus();
            return false;
        }
        
        // 验证URL格式
        if (!isValidUrl(url)) {
            showError("网站地址格式不正确\n请输入完整的URL，如: https://www.example.com");
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
        String url = urlField.getText().trim();
        
        if (url.isEmpty()) {
            showError("请先输入网站地址");
            return;
        }
        
        if (!isValidUrl(url)) {
            showError("网站地址格式不正确");
            return;
        }
        
        try {
            // 在系统默认浏览器中打开网站
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(new URL(url).toURI());
                showInfo("正在打开网站: " + url);
            } else {
                showError("系统不支持打开浏览器");
            }
        } catch (Exception e) {
            showError("打开网站失败: " + e.getMessage());
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
        JOptionPane.showMessageDialog(
            this,
            message,
            "输入错误",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * 显示信息消息
     * @param message 信息消息
     */
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "提示",
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
        
        // 将显示名称转换为配置文件中的类型
        switch (displayName) {
            case "OSINT": return "OSINT";
            case "信息收集": return "Recon";
            case "漏洞库": return "漏洞库";
            case "在线工具": return "tools";
            case "学习资源": return "learning";
            default: return displayName.toLowerCase();
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
} 