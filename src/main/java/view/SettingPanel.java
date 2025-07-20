package view;

import manager.ApiManager;
import manager.ConfigManager;
import util.JsonUtil;
import util.OsUtils;
import model.Config;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * 设置面板 (View层)
 * 用于插件配置和管理
 */
public class SettingPanel extends JPanel {
    
    // 配置文件相关组件
    private JButton importConfigButton;
    private JButton exportConfigButton;
    private JButton resetConfigButton;
    private JLabel configStatusLabel;
    
    // 工具目录设置相关组件
    private JTextField toolDirectoryField;
    private JButton browseDirectoryButton;
    private JButton applyDirectoryButton;
    private JLabel directoryStatusLabel;
    
    // 系统前缀设置相关组件
    private JTextField commandPrefixField;
    private JButton resetPrefixButton;
    private JButton applyPrefixButton;
    private JLabel prefixStatusLabel;
    private JLabel systemInfoLabel;
    
    // 插件信息组件
    private JLabel versionLabel;
    private JLabel authorLabel;
    private JTextArea changelogArea;
    
    // 配置文件路径
    private static final String TOOL_CONFIG_FILE = "tool_settings.properties";
    private Properties toolSettings;
    
    public SettingPanel() {
        loadToolSettings();
        initializeUI();
        loadCurrentSettings();
    }
    
    /**
     * 初始化UI组件
     */
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // 创建主滚动面板
        JScrollPane scrollPane = new JScrollPane(createMainPanel());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * 创建主面板
     * @return 主面板
     */
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        // 配置文件管理面板
        mainPanel.add(createConfigPanel());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // 工具目录设置面板
        mainPanel.add(createToolDirectoryPanel());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // 系统前缀设置面板
        mainPanel.add(createSystemPrefixPanel());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // 插件信息面板
        mainPanel.add(createPluginInfoPanel());
        mainPanel.add(Box.createVerticalGlue());
        
        return mainPanel;
    }
    
    /**
     * 创建配置文件管理面板
     * @return 配置面板
     */
    private JPanel createConfigPanel() {
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "配置文件管理",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 12)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 导入配置按钮
        gbc.gridx = 0; gbc.gridy = 0;
        importConfigButton = createStyledButton("导入配置", "从外部文件导入配置", new Color(46, 125, 50));
        configPanel.add(importConfigButton, gbc);
        
        // 导出配置按钮
        gbc.gridx = 1; gbc.gridy = 0;
        exportConfigButton = createStyledButton("导出配置", "将当前配置导出到文件", new Color(25, 118, 210));
        configPanel.add(exportConfigButton, gbc);
        
        // 重置配置按钮
        gbc.gridx = 2; gbc.gridy = 0;
        resetConfigButton = createStyledButton("重置配置", "恢复默认配置", new Color(211, 47, 47));
        configPanel.add(resetConfigButton, gbc);
        
        // 配置状态标签
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        configStatusLabel = new JLabel("配置状态: 已加载");
        configStatusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        configStatusLabel.setForeground(new Color(100, 100, 100));
        configPanel.add(configStatusLabel, gbc);
        
        // 配置说明
        gbc.gridy = 2;
        JTextArea configDescArea = new JTextArea(3, 50);
        configDescArea.setText("配置文件包含所有HTTP工具、第三方工具和网站导航的设置信息。\n" +
                              "导入: 选择JSON格式的配置文件进行导入，将覆盖当前配置。\n" +
                              "导出: 将当前所有配置保存为JSON文件，便于备份和分享。");
        configDescArea.setEditable(false);
        configDescArea.setBackground(getBackground());
        configDescArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        configDescArea.setForeground(new Color(100, 100, 100));
        configDescArea.setLineWrap(true);
        configDescArea.setWrapStyleWord(true);
        configPanel.add(configDescArea, gbc);
        
        return configPanel;
    }
    
    /**
     * 创建工具目录设置面板
     * @return 工具目录面板
     */
    private JPanel createToolDirectoryPanel() {
        JPanel directoryPanel = new JPanel(new GridBagLayout());
        directoryPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "工具目录设置",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 12)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 工具目录标签
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel directoryLabel = new JLabel("工具根目录:");
        directoryLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        directoryPanel.add(directoryLabel, gbc);
        
        // 工具目录输入框
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        toolDirectoryField = new JTextField(30);
        toolDirectoryField.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        toolDirectoryField.setToolTipText("设置渗透测试工具的根目录路径");
        directoryPanel.add(toolDirectoryField, gbc);
        
        // 浏览按钮
        gbc.gridx = 2; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        browseDirectoryButton = createStyledButton("浏览", "选择工具目录", new Color(255, 152, 0));
        browseDirectoryButton.setPreferredSize(new Dimension(70, 25));
        directoryPanel.add(browseDirectoryButton, gbc);
        
        // 应用按钮
        gbc.gridx = 3; gbc.gridy = 0;
        applyDirectoryButton = createStyledButton("应用", "应用目录设置", new Color(46, 125, 50));
        applyDirectoryButton.setPreferredSize(new Dimension(70, 25));
        directoryPanel.add(applyDirectoryButton, gbc);
        
        // 目录状态标签
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        directoryStatusLabel = new JLabel("目录状态: 未设置");
        directoryStatusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        directoryStatusLabel.setForeground(new Color(100, 100, 100));
        directoryPanel.add(directoryStatusLabel, gbc);
        
        // 目录说明
        gbc.gridy = 2;
        JTextArea directoryDescArea = new JTextArea(3, 50);
        directoryDescArea.setText("工具根目录用于快速访问常用的渗透测试工具。\n" +
                                "建议设置为: D:\\tools\\ 或 /usr/share/tools/\n" +
                                "设置后可以在配置中使用相对路径，如: tools\\sqlmap\\sqlmap.py");
        directoryDescArea.setEditable(false);
        directoryDescArea.setBackground(getBackground());
        directoryDescArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        directoryDescArea.setForeground(new Color(100, 100, 100));
        directoryDescArea.setLineWrap(true);
        directoryDescArea.setWrapStyleWord(true);
        directoryPanel.add(directoryDescArea, gbc);
        
        return directoryPanel;
    }
    
    /**
     * 创建系统前缀设置面板
     * @return 系统前缀面板
     */
    private JPanel createSystemPrefixPanel() {
        JPanel prefixPanel = new JPanel(new GridBagLayout());
        prefixPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "系统命令前缀设置",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 12)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 系统信息标签
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        systemInfoLabel = new JLabel("当前系统: " + OsUtils.getOsType() + " (" + OsUtils.getOsName() + ")");
        systemInfoLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        systemInfoLabel.setForeground(new Color(46, 125, 50));
        prefixPanel.add(systemInfoLabel, gbc);
        
        // 命令前缀标签
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        JLabel prefixLabel = new JLabel("命令前缀:");
        prefixLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        prefixPanel.add(prefixLabel, gbc);
        
        // 命令前缀输入框
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        commandPrefixField = new JTextField(20);
        commandPrefixField.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        commandPrefixField.setToolTipText("设置命令执行前缀，如: cmd /c 或 /bin/bash -c");
        prefixPanel.add(commandPrefixField, gbc);
        
        // 重置按钮
        gbc.gridx = 2; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        resetPrefixButton = createStyledButton("重置", "重置为系统默认前缀", new Color(255, 152, 0));
        resetPrefixButton.setPreferredSize(new Dimension(70, 25));
        prefixPanel.add(resetPrefixButton, gbc);
        
        // 应用按钮
        gbc.gridx = 3; gbc.gridy = 1;
        applyPrefixButton = createStyledButton("应用", "应用前缀设置", new Color(46, 125, 50));
        applyPrefixButton.setPreferredSize(new Dimension(70, 25));
        prefixPanel.add(applyPrefixButton, gbc);
        
        // 前缀状态标签
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        prefixStatusLabel = new JLabel("前缀状态: 使用系统默认");
        prefixStatusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        prefixStatusLabel.setForeground(new Color(100, 100, 100));
        prefixPanel.add(prefixStatusLabel, gbc);
        
        // 前缀说明
        gbc.gridy = 3;
        JTextArea prefixDescArea = new JTextArea(4, 50);
        prefixDescArea.setText("命令前缀用于在操作系统中执行工具命令。\n" +
                              "Windows系统默认: cmd /c\n" +
                              "Linux/Unix系统默认: /bin/bash -c\n" +
                              "您可以根据需要自定义前缀，如使用PowerShell: powershell -Command");
        prefixDescArea.setEditable(false);
        prefixDescArea.setBackground(getBackground());
        prefixDescArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        prefixDescArea.setForeground(new Color(100, 100, 100));
        prefixDescArea.setLineWrap(true);
        prefixDescArea.setWrapStyleWord(true);
        prefixPanel.add(prefixDescArea, gbc);
        
        return prefixPanel;
    }
    
    /**
     * 创建插件信息面板
     * @return 插件信息面板
     */
    private JPanel createPluginInfoPanel() {
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "插件信息",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 12)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 插件名称
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameLabel = new JLabel("插件名称:");
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        infoPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        JLabel nameValueLabel = new JLabel("BpArsenal - Burp Suite武器库");
        nameValueLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        infoPanel.add(nameValueLabel, gbc);
        
        // 版本信息
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel versionLabelTitle = new JLabel("版本:");
        versionLabelTitle.setFont(new Font("微软雅黑", Font.BOLD, 12));
        infoPanel.add(versionLabelTitle, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        versionLabel = new JLabel("v1.0.0");
        versionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        infoPanel.add(versionLabel, gbc);
        
        // 作者信息
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel authorLabelTitle = new JLabel("作者:");
        authorLabelTitle.setFont(new Font("微软雅黑", Font.BOLD, 12));
        infoPanel.add(authorLabelTitle, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        authorLabel = new JLabel("Security Team");
        authorLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        infoPanel.add(authorLabel, gbc);
        
        // 更新日志
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        
        JLabel changelogTitle = new JLabel("更新日志:");
        changelogTitle.setFont(new Font("微软雅黑", Font.BOLD, 12));
        infoPanel.add(changelogTitle, gbc);
        
        gbc.gridy = 4;
        changelogArea = new JTextArea(8, 50);
        changelogArea.setText("v1.0.0 (2024-01-15)\n" +
                             "- 初始版本发布\n" +
                             "- 支持HTTP工具管理和执行\n" +
                             "- 支持第三方工具启动\n" +
                             "- 支持网站导航功能\n" +
                             "- 支持上下文菜单Arsenal工具\n" +
                             "- 支持命令数组格式配置\n" +
                             "- 支持配置文件导入导出\n" +
                             "- 支持工具目录设置");
        changelogArea.setEditable(false);
        changelogArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        changelogArea.setBackground(new Color(248, 248, 248));
        changelogArea.setLineWrap(true);
        changelogArea.setWrapStyleWord(true);
        
        JScrollPane changelogScrollPane = new JScrollPane(changelogArea);
        changelogScrollPane.setPreferredSize(new Dimension(400, 150));
        changelogScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        infoPanel.add(changelogScrollPane, gbc);
        
        return infoPanel;
    }
    
    /**
     * 创建样式化按钮
     * @param text 按钮文本
     * @param tooltip 提示文本
     * @param color 背景颜色
     * @return 按钮
     */
    private JButton createStyledButton(String text, String tooltip, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        button.setPreferredSize(new Dimension(90, 30));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setToolTipText(tooltip);
        
        // 添加事件监听器
        button.addActionListener(createButtonActionListener(text));
        
        return button;
    }
    
    /**
     * 创建按钮事件监听器
     * @param buttonText 按钮文本
     * @return 事件监听器
     */
    private ActionListener createButtonActionListener(String buttonText) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switch (buttonText) {
                    case "导入配置":
                        importConfiguration();
                        break;
                    case "导出配置":
                        exportConfiguration();
                        break;
                    case "重置配置":
                        resetConfiguration();
                        break;
                    case "浏览":
                        browseToolDirectory();
                        break;
                    case "应用":
                        if (e.getSource() == applyDirectoryButton) {
                            applyToolDirectory();
                        } else if (e.getSource() == applyPrefixButton) {
                            applyCommandPrefix();
                        }
                        break;
                    case "重置":
                        resetCommandPrefix();
                        break;
                }
            }
        };
    }
    
    /**
     * 导入配置文件
     */
    private void importConfiguration() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON配置文件 (*.json)", "json"));
        fileChooser.setDialogTitle("选择要导入的配置文件");
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            try {
                // 读取文件内容
                String jsonContent = new String(Files.readAllBytes(selectedFile.toPath()), StandardCharsets.UTF_8);
                
                // 验证JSON格式
                Config config = JsonUtil.fromJson(jsonContent, Config.class);
                
                // 确认导入
                int confirmResult = JOptionPane.showConfirmDialog(
                    this,
                    "确定要导入配置文件吗？\n这将覆盖当前的所有配置！\n\n文件: " + selectedFile.getName(),
                    "确认导入",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                
                if (confirmResult == JOptionPane.YES_OPTION) {
                    // 备份当前配置文件
                    backupCurrentConfig();
                    
                    // 写入新配置
                    Path configPath = Paths.get("src/main/resources/config.json");
                    Files.write(configPath, jsonContent.getBytes(StandardCharsets.UTF_8));
                    
                    // 重新加载配置
                    ConfigManager.getInstance().reloadConfig();
                    
                    updateConfigStatus("配置导入成功: " + selectedFile.getName(), Color.GREEN);
                    
                    // 记录日志
                    logInfo("配置文件导入成功: " + selectedFile.getAbsolutePath());
                    
                    JOptionPane.showMessageDialog(
                        this,
                        "配置导入成功！\n请重启插件以确保所有更改生效。",
                        "导入成功",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }
                
            } catch (Exception ex) {
                updateConfigStatus("配置导入失败: " + ex.getMessage(), Color.RED);
                logError("配置文件导入失败: " + ex.getMessage());
                
                JOptionPane.showMessageDialog(
                    this,
                    "配置导入失败！\n错误: " + ex.getMessage(),
                    "导入失败",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    /**
     * 导出配置文件
     */
    private void exportConfiguration() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON配置文件 (*.json)", "json"));
        fileChooser.setDialogTitle("选择配置文件保存位置");
        fileChooser.setSelectedFile(new File("bparsenal_config_" + System.currentTimeMillis() + ".json"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // 确保文件有.json扩展名
            if (!selectedFile.getName().toLowerCase().endsWith(".json")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".json");
            }
            
            try {
                // 获取当前配置
                Config config = ConfigManager.getInstance().getConfig();
                
                // 转换为JSON
                String jsonContent = JsonUtil.toJson(config);
                
                // 写入文件
                Files.write(selectedFile.toPath(), jsonContent.getBytes(StandardCharsets.UTF_8));
                
                updateConfigStatus("配置导出成功: " + selectedFile.getName(), Color.GREEN);
                logInfo("配置文件导出成功: " + selectedFile.getAbsolutePath());
                
                JOptionPane.showMessageDialog(
                    this,
                    "配置导出成功！\n文件位置: " + selectedFile.getAbsolutePath(),
                    "导出成功",
                    JOptionPane.INFORMATION_MESSAGE
                );
                
            } catch (Exception ex) {
                updateConfigStatus("配置导出失败: " + ex.getMessage(), Color.RED);
                logError("配置文件导出失败: " + ex.getMessage());
                
                JOptionPane.showMessageDialog(
                    this,
                    "配置导出失败！\n错误: " + ex.getMessage(),
                    "导出失败",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    /**
     * 重置配置文件
     */
    private void resetConfiguration() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "确定要重置配置文件吗？\n这将恢复所有默认设置，当前配置将丢失！",
            "确认重置",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                // 备份当前配置
                backupCurrentConfig();
                
                // 创建默认配置
                Config defaultConfig = createDefaultConfig();
                
                // 写入默认配置
                String jsonContent = JsonUtil.toJson(defaultConfig);
                Path configPath = Paths.get("src/main/resources/config.json");
                Files.write(configPath, jsonContent.getBytes(StandardCharsets.UTF_8));
                
                // 重新加载配置
                ConfigManager.getInstance().reloadConfig();
                
                updateConfigStatus("配置已重置为默认设置", Color.BLUE);
                logInfo("配置文件已重置为默认设置");
                
                JOptionPane.showMessageDialog(
                    this,
                    "配置重置成功！\n已恢复默认设置。",
                    "重置成功",
                    JOptionPane.INFORMATION_MESSAGE
                );
                
            } catch (Exception ex) {
                updateConfigStatus("配置重置失败: " + ex.getMessage(), Color.RED);
                logError("配置重置失败: " + ex.getMessage());
                
                JOptionPane.showMessageDialog(
                    this,
                    "配置重置失败！\n错误: " + ex.getMessage(),
                    "重置失败",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    /**
     * 浏览工具目录
     */
    private void browseToolDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("选择工具根目录");
        
        // 设置当前目录
        String currentDirectory = toolDirectoryField.getText().trim();
        if (!currentDirectory.isEmpty()) {
            File currentDir = new File(currentDirectory);
            if (currentDir.exists() && currentDir.isDirectory()) {
                fileChooser.setCurrentDirectory(currentDir);
            }
        }
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            toolDirectoryField.setText(selectedDirectory.getAbsolutePath());
        }
    }
    
    /**
     * 应用工具目录设置
     */
    private void applyToolDirectory() {
        String directoryPath = toolDirectoryField.getText().trim();
        
        if (directoryPath.isEmpty()) {
            updateDirectoryStatus("工具目录不能为空", Color.RED);
            return;
        }
        
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            int result = JOptionPane.showConfirmDialog(
                this,
                "指定的目录不存在，是否创建？\n路径: " + directoryPath,
                "目录不存在",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (result == JOptionPane.YES_OPTION) {
                try {
                    Files.createDirectories(directory.toPath());
                } catch (Exception ex) {
                    updateDirectoryStatus("创建目录失败: " + ex.getMessage(), Color.RED);
                    return;
                }
            } else {
                return;
            }
        }
        
        if (!directory.isDirectory()) {
            updateDirectoryStatus("指定路径不是有效目录", Color.RED);
            return;
        }
        
        try {
            // 保存工具目录设置
            toolSettings.setProperty("tool.directory", directoryPath);
            saveToolSettings();
            
            updateDirectoryStatus("工具目录设置成功: " + directoryPath, Color.GREEN);
            logInfo("工具目录设置成功: " + directoryPath);
            
            JOptionPane.showMessageDialog(
                this,
                "工具目录设置成功！\n路径: " + directoryPath,
                "设置成功",
                JOptionPane.INFORMATION_MESSAGE
            );
            
        } catch (Exception ex) {
            updateDirectoryStatus("保存设置失败: " + ex.getMessage(), Color.RED);
            logError("保存工具目录设置失败: " + ex.getMessage());
        }
    }
    
    /**
     * 应用命令前缀设置
     */
    private void applyCommandPrefix() {
        String prefix = commandPrefixField.getText().trim();
        
        try {
            // 保存命令前缀设置
            if (prefix.isEmpty()) {
                toolSettings.remove("command.prefix");
                updatePrefixStatus("已重置为系统默认前缀", Color.BLUE);
            } else {
                toolSettings.setProperty("command.prefix", prefix);
                updatePrefixStatus("自定义前缀设置成功: " + prefix, Color.GREEN);
            }
            
            saveToolSettings();
            logInfo("命令前缀设置成功: " + (prefix.isEmpty() ? "系统默认" : prefix));
            
            JOptionPane.showMessageDialog(
                this,
                "命令前缀设置成功！\n前缀: " + (prefix.isEmpty() ? "系统默认" : prefix),
                "设置成功",
                JOptionPane.INFORMATION_MESSAGE
            );
            
        } catch (Exception ex) {
            updatePrefixStatus("保存设置失败: " + ex.getMessage(), Color.RED);
            logError("保存命令前缀设置失败: " + ex.getMessage());
        }
    }
    
    /**
     * 重置命令前缀
     */
    private void resetCommandPrefix() {
        String[] defaultPrefix = OsUtils.getDefaultCommandPrefix();
        String defaultPrefixString = String.join(" ", defaultPrefix);
        commandPrefixField.setText(defaultPrefixString);
        updatePrefixStatus("已重置为系统默认: " + defaultPrefixString, Color.BLUE);
    }
    
    /**
     * 加载当前设置
     */
    private void loadCurrentSettings() {
        // 加载工具目录设置
        String toolDirectory = toolSettings.getProperty("tool.directory", "");
        toolDirectoryField.setText(toolDirectory);
        
        if (!toolDirectory.isEmpty()) {
            File directory = new File(toolDirectory);
            if (directory.exists() && directory.isDirectory()) {
                updateDirectoryStatus("工具目录: " + toolDirectory, Color.GREEN);
            } else {
                updateDirectoryStatus("工具目录无效: " + toolDirectory, Color.RED);
            }
        } else {
            updateDirectoryStatus("工具目录未设置", Color.GRAY);
        }
        
        // 加载命令前缀设置
        String commandPrefix = toolSettings.getProperty("command.prefix", "");
        if (!commandPrefix.isEmpty()) {
            commandPrefixField.setText(commandPrefix);
            updatePrefixStatus("自定义前缀: " + commandPrefix, Color.GREEN);
        } else {
            String[] defaultPrefix = OsUtils.getDefaultCommandPrefix();
            String defaultPrefixString = String.join(" ", defaultPrefix);
            commandPrefixField.setText(defaultPrefixString);
            updatePrefixStatus("使用系统默认: " + defaultPrefixString, Color.BLUE);
        }
        
        // 更新配置状态
        updateConfigStatus("配置状态: 已加载", Color.GREEN);
    }
    
    /**
     * 加载工具设置
     */
    private void loadToolSettings() {
        toolSettings = new Properties();
        File settingsFile = new File(TOOL_CONFIG_FILE);
        
        if (settingsFile.exists()) {
            try (FileInputStream fis = new FileInputStream(settingsFile)) {
                toolSettings.load(fis);
            } catch (IOException e) {
                logError("加载工具设置失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 保存工具设置
     */
    private void saveToolSettings() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(TOOL_CONFIG_FILE)) {
            toolSettings.store(fos, "BpArsenal Tool Settings");
        }
    }
    
    /**
     * 备份当前配置文件
     */
    private void backupCurrentConfig() {
        try {
            Path configPath = Paths.get("src/main/resources/config.json");
            if (Files.exists(configPath)) {
                Path backupPath = Paths.get("src/main/resources/config_backup_" + System.currentTimeMillis() + ".json");
                Files.copy(configPath, backupPath);
                logInfo("配置文件已备份: " + backupPath.toString());
            }
        } catch (Exception e) {
            logError("备份配置文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建默认配置
     * @return 默认配置对象
     */
    private Config createDefaultConfig() {
        Config config = new Config();
        config.setHttpTool(new java.util.ArrayList<>());
        config.setThirtyPart(new java.util.ArrayList<>());
        config.setWebSite(new java.util.ArrayList<>());
        return config;
    }
    
    /**
     * 更新配置状态显示
     * @param message 状态消息
     * @param color 状态颜色
     */
    private void updateConfigStatus(String message, Color color) {
        configStatusLabel.setText("配置状态: " + message);
        configStatusLabel.setForeground(color);
    }
    
    /**
     * 更新目录状态显示
     * @param message 状态消息
     * @param color 状态颜色
     */
    private void updateDirectoryStatus(String message, Color color) {
        directoryStatusLabel.setText("目录状态: " + message);
        directoryStatusLabel.setForeground(color);
    }
    
    /**
     * 更新前缀状态显示
     * @param message 状态消息
     * @param color 状态颜色
     */
    private void updatePrefixStatus(String message, Color color) {
        prefixStatusLabel.setText("前缀状态: " + message);
        prefixStatusLabel.setForeground(color);
    }
    
    /**
     * 记录信息日志
     * @param message 日志消息
     */
    private void logInfo(String message) {
        try {
            if (ApiManager.getInstance().isInitialized()) {
                ApiManager.getInstance().getApi().logging().logToOutput("SettingPanel: " + message);
            }
        } catch (Exception e) {
            System.out.println("SettingPanel: " + message);
        }
    }
    
    /**
     * 记录错误日志
     * @param message 错误消息
     */
    private void logError(String message) {
        try {
            if (ApiManager.getInstance().isInitialized()) {
                ApiManager.getInstance().getApi().logging().logToError("SettingPanel: " + message);
            }
        } catch (Exception e) {
            System.err.println("SettingPanel: " + message);
        }
    }
    
    /**
     * 获取工具目录设置
     * @return 工具目录路径
     */
    public String getToolDirectory() {
        return toolSettings.getProperty("tool.directory", "");
    }
    
    /**
     * 获取命令前缀设置
     * @return 命令前缀
     */
    public String getCommandPrefix() {
        return toolSettings.getProperty("command.prefix", "");
    }
    
    /**
     * 获取格式化后的命令前缀数组
     * @return 命令前缀数组
     */
    public String[] getCommandPrefixArray() {
        String prefix = getCommandPrefix();
        if (prefix.isEmpty()) {
            return OsUtils.getDefaultCommandPrefix();
        } else {
            return prefix.split("\\s+");
        }
    }
    
    /**
     * 刷新设置面板
     */
    public void refreshPanel() {
        loadCurrentSettings();
    }
} 