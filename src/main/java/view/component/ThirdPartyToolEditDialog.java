package view.component;

import model.ThirdPartyTool;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 第三方工具编辑对话框
 * 用于添加和编辑第三方工具
 */
public class ThirdPartyToolEditDialog extends JDialog {
    
    private JTextField toolNameField;
    private JTextField startCommandField;
    private JCheckBox favorCheckBox;
    private JCheckBox autoStartCheckBox;
    private JComboBox<String> categoryCombo;
    private JButton confirmButton;
    private JButton cancelButton;
    
    private ThirdPartyTool tool;
    private boolean confirmed = false;
    private boolean isEditing = false;
    
    public ThirdPartyToolEditDialog(Window parent, ThirdPartyTool tool) {
        super(parent, tool == null ? "添加第三方工具" : "编辑第三方工具", ModalityType.APPLICATION_MODAL);
        this.tool = tool;
        this.isEditing = (tool != null);
        
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
            "基本信息",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 12)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 工具名称
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameLabel = new JLabel("工具名称:");
        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        basicPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        toolNameField = new JTextField(25);
        toolNameField.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        toolNameField.setToolTipText("输入工具名称，如: Burp Suite, VS Code");
        basicPanel.add(toolNameField, gbc);
        
        // 启动命令
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel commandLabel = new JLabel("启动命令:");
        commandLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        basicPanel.add(commandLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        startCommandField = new JTextField(25);
        startCommandField.setFont(new Font("Consolas", Font.PLAIN, 11));
        startCommandField.setToolTipText("输入启动命令，如: java -jar burpsuite.jar");
        basicPanel.add(startCommandField, gbc);
        
        // 分类
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel categoryLabel = new JLabel("工具分类:");
        categoryLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        basicPanel.add(categoryLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        categoryCombo = new JComboBox<>(new String[]{
            "渗透工具", "编辑器", "网络工具", "分析工具", "其他"
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
        favorCheckBox = new JCheckBox("收藏此工具");
        favorCheckBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        favorCheckBox.setToolTipText("将此工具标记为收藏");
        settingsPanel.add(favorCheckBox, gbc);
        
        // 自动启动设置
        gbc.gridx = 1; gbc.gridy = 0;
        autoStartCheckBox = new JCheckBox("启动时自动运行");
        autoStartCheckBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        autoStartCheckBox.setToolTipText("在Burp Suite启动时自动运行此工具");
        settingsPanel.add(autoStartCheckBox, gbc);
        
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
        String toolName = toolNameField.getText().trim();
        String startCommand = startCommandField.getText().trim();
        
        if (toolName.isEmpty()) {
            showError("工具名称不能为空");
            toolNameField.requestFocus();
            return false;
        }
        
        if (startCommand.isEmpty()) {
            showError("启动命令不能为空");
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
        tool.setFavor(favorCheckBox.isSelected());
        tool.setAutoStart(autoStartCheckBox.isSelected());
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
        
        // 将显示名称转换为配置文件中的类型
        switch (displayName) {
            case "渗透工具": return "exploit";
            case "编辑器": return "编辑器";
            case "网络工具": return "network";
            case "分析工具": return "analysis";
            default: return displayName.toLowerCase();
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
} 