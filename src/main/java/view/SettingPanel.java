package view;

import controller.SettingPanelController;
import manager.ConfigManager;
import model.SettingModel;
import util.I18nManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * 设置面板 (View层)
 * 重构为MVC架构，仅负责UI展示和用户交互
 */
public class SettingPanel extends JPanel implements I18nManager.LanguageChangeListener,
        SettingPanelController.SettingPanelView, SettingPanelController.SettingPanelListener {
    
    // 控制器
    private SettingPanelController controller;
    private SettingModel settingModel;
    
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
    
    // 语言设置相关组件
    private JComboBox<I18nManager.SupportedLanguage> languageComboBox;
    private JLabel languageStatusLabel;
    private ActionListener languageActionListener;
    
    // 插件信息组件
    private JLabel versionLabel;
    private JLabel authorLabel;
    
    public SettingPanel() {
        // 初始化控制器
        controller = SettingPanelController.getInstance();
        controller.setView(this);
        controller.setListener(this);
        
        // 获取设置模型
        settingModel = controller.getSettingModel();
        
        initializeUI();
        
        // 注册语言变更监听器
        I18nManager.getInstance().addLanguageChangeListener(this);
        
        // 初始化设置
        controller.initializeSettings();
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
        
        // 语言设置面板
        mainPanel.add(createLanguagePanel());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // 插件信息面板
        mainPanel.add(createPluginInfoPanel());
        mainPanel.add(Box.createVerticalGlue());
        
        return mainPanel;
    }
    
    /**
     * 创建配置文件管理面板
     */
    private JPanel createConfigPanel() {
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            I18nManager.getInstance().getText("settings.config.title"),
            TitledBorder.LEFT,
            TitledBorder.TOP,
            UIManager.getFont("TitledBorder.font") != null ? UIManager.getFont("TitledBorder.font").deriveFont(Font.BOLD, 12f) : new Font(Font.SANS_SERIF, Font.BOLD, 12)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        I18nManager i18n = I18nManager.getInstance();
        
        // 按钮行
        gbc.gridx = 0; gbc.gridy = 0;
        importConfigButton = createStyledButton(i18n.getText("settings.config.import"), new Color(46, 125, 50));
        configPanel.add(importConfigButton, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        exportConfigButton = createStyledButton(i18n.getText("settings.config.export"), new Color(25, 118, 210));
        configPanel.add(exportConfigButton, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
        resetConfigButton = createStyledButton(i18n.getText("settings.config.reset"), new Color(211, 47, 47));
        configPanel.add(resetConfigButton, gbc);
        
        // 状态标签
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        configStatusLabel = new JLabel(i18n.getText("settings.config.status") + ": " + i18n.getText("settings.config.status.loaded"));
        configStatusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        configStatusLabel.setForeground(new Color(100, 100, 100));
        configPanel.add(configStatusLabel, gbc);
        
        // 说明文本
        gbc.gridy = 2;
        JTextArea configDescArea = new JTextArea(3, 50);
        configDescArea.setText(i18n.getText("desc.config.management"));
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
     */
    private JPanel createToolDirectoryPanel() {
        JPanel directoryPanel = new JPanel(new GridBagLayout());
        directoryPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            I18nManager.getInstance().getText("settings.directory.title"),
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 12)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 目录输入行
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel directoryLabel = new JLabel(I18nManager.getInstance().getText("settings.directory.label"));
        directoryLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        directoryPanel.add(directoryLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        toolDirectoryField = new JTextField(30);
        toolDirectoryField.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        toolDirectoryField.setToolTipText(I18nManager.getInstance().getText("settings.directory.tooltip"));
        directoryPanel.add(toolDirectoryField, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        browseDirectoryButton = createStyledButton(I18nManager.getInstance().getText("button.browse"), new Color(255, 152, 0));
        browseDirectoryButton.setPreferredSize(new Dimension(70, 25));
        directoryPanel.add(browseDirectoryButton, gbc);
        
        gbc.gridx = 3; gbc.gridy = 0;
        applyDirectoryButton = createStyledButton(I18nManager.getInstance().getText("button.apply"), new Color(46, 125, 50));
        applyDirectoryButton.setPreferredSize(new Dimension(70, 25));
        directoryPanel.add(applyDirectoryButton, gbc);
        
        // 状态行
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        directoryStatusLabel = new JLabel(I18nManager.getInstance().getText("settings.directory.status") + ": " + I18nManager.getInstance().getText("settings.directory.status.notset"));
        directoryStatusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        directoryStatusLabel.setForeground(new Color(100, 100, 100));
        directoryPanel.add(directoryStatusLabel, gbc);
        
        // 说明文本
        gbc.gridy = 2;
        JTextArea directoryDescArea = new JTextArea(3, 50);
        directoryDescArea.setText(I18nManager.getInstance().getText("desc.tool.directory"));
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
     */
    private JPanel createSystemPrefixPanel() {
        JPanel prefixPanel = new JPanel(new GridBagLayout());
        prefixPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            I18nManager.getInstance().getText("settings.prefix.title"),
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 12)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 系统信息
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        systemInfoLabel = new JLabel(I18nManager.getInstance().getText("settings.prefix.current.system") + ": " + settingModel.getSystemInfo());
        systemInfoLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        systemInfoLabel.setForeground(new Color(46, 125, 50));
        prefixPanel.add(systemInfoLabel, gbc);
        
        // 前缀输入行
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        JLabel prefixLabel = new JLabel(I18nManager.getInstance().getText("settings.prefix.label"));
        prefixLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        prefixPanel.add(prefixLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        commandPrefixField = new JTextField(20);
        commandPrefixField.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        commandPrefixField.setToolTipText(I18nManager.getInstance().getText("settings.prefix.tooltip"));
        prefixPanel.add(commandPrefixField, gbc);
        
        gbc.gridx = 2; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        resetPrefixButton = createStyledButton(I18nManager.getInstance().getText("button.reset"), new Color(255, 152, 0));
        resetPrefixButton.setPreferredSize(new Dimension(70, 25));
        prefixPanel.add(resetPrefixButton, gbc);
        
        gbc.gridx = 3; gbc.gridy = 1;
        applyPrefixButton = createStyledButton(I18nManager.getInstance().getText("button.apply"), new Color(46, 125, 50));
        applyPrefixButton.setPreferredSize(new Dimension(70, 25));
        prefixPanel.add(applyPrefixButton, gbc);
        
        // 状态行
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        prefixStatusLabel = new JLabel(I18nManager.getInstance().getText("settings.prefix.status") + ": " + I18nManager.getInstance().getText("settings.prefix.status.default"));
        prefixStatusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        prefixStatusLabel.setForeground(new Color(100, 100, 100));
        prefixPanel.add(prefixStatusLabel, gbc);
        
        // 说明文本
        gbc.gridy = 3;
        JTextArea prefixDescArea = new JTextArea(4, 50);
        prefixDescArea.setText(I18nManager.getInstance().getText("desc.command.prefix"));
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
     * 创建语言设置面板
     */
    private JPanel createLanguagePanel() {
        JPanel languagePanel = new JPanel(new GridBagLayout());
        languagePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            I18nManager.getInstance().getText("settings.language.title"),
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 12)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 语言选择行
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel languageLabel = new JLabel(I18nManager.getInstance().getText("settings.language.label"));
        languageLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        languagePanel.add(languageLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        languageComboBox = new JComboBox<>();
        languageComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        
        // 添加支持的语言
        I18nManager i18n = I18nManager.getInstance();
        for (I18nManager.SupportedLanguage language : i18n.getSupportedLanguages()) {
            languageComboBox.addItem(language);
        }
        
        // 创建语言切换监听器
        languageActionListener = e -> {
            I18nManager.SupportedLanguage selected = (I18nManager.SupportedLanguage) languageComboBox.getSelectedItem();
            if (selected != null) {
                controller.setLanguage(selected);
                // 延迟刷新设置，确保语言变更通知完成后再刷新
                SwingUtilities.invokeLater(() -> {
                    refreshAllSettings();
                });
            }
        };
        
        languageComboBox.addActionListener(languageActionListener);
        languagePanel.add(languageComboBox, gbc);
        
        // 状态行
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        languageStatusLabel = new JLabel(I18nManager.getInstance().getText("settings.language.restart.required"));
        languageStatusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        languageStatusLabel.setForeground(new Color(100, 100, 100));
        languagePanel.add(languageStatusLabel, gbc);
        
        // 说明文本
        gbc.gridy = 2;
        JTextArea languageDescArea = new JTextArea(2, 50);
        languageDescArea.setText(I18nManager.getInstance().getText("desc.language.setting"));
        languageDescArea.setEditable(false);
        languageDescArea.setBackground(getBackground());
        languageDescArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        languageDescArea.setForeground(new Color(100, 100, 100));
        languageDescArea.setLineWrap(true);
        languageDescArea.setWrapStyleWord(true);
        languagePanel.add(languageDescArea, gbc);
        
        return languagePanel;
    }
    
    /**
     * 创建插件信息面板
     */
    private JPanel createPluginInfoPanel() {
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            I18nManager.getInstance().getText("settings.plugin.title"),
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 12)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 插件名称
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameLabel = new JLabel(I18nManager.getInstance().getText("settings.plugin.name") + ":");
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        infoPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        JLabel nameValueLabel = new JLabel(settingModel.getPluginName());
        nameValueLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        infoPanel.add(nameValueLabel, gbc);
        
        // 版本信息
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel versionLabelTitle = new JLabel(I18nManager.getInstance().getText("settings.plugin.version") + ":");
        versionLabelTitle.setFont(new Font("微软雅黑", Font.BOLD, 12));
        infoPanel.add(versionLabelTitle, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        versionLabel = new JLabel(settingModel.getPluginVersion());
        versionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        infoPanel.add(versionLabel, gbc);
        
        // 作者信息
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel authorLabelTitle = new JLabel(I18nManager.getInstance().getText("settings.plugin.author") + ":");
        authorLabelTitle.setFont(new Font("微软雅黑", Font.BOLD, 12));
        infoPanel.add(authorLabelTitle, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        authorLabel = new JLabel(settingModel.getPluginAuthor());
        authorLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        infoPanel.add(authorLabel, gbc);
        
        return infoPanel;
    }
    
    /**
     * 创建样式化按钮
     */
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        button.setPreferredSize(new Dimension(90, 30));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        
        // 添加事件监听器
        button.addActionListener(createButtonActionListener(text));
        
        return button;
    }
    
    /**
     * 创建按钮事件监听器
     */
    private ActionListener createButtonActionListener(String buttonText) {
        I18nManager i18n = I18nManager.getInstance();
        return e -> {
            if (buttonText.equals(i18n.getText("settings.config.import"))) {
                importConfiguration();
            } else if (buttonText.equals(i18n.getText("settings.config.export"))) {
                exportConfiguration();
            } else if (buttonText.equals(i18n.getText("settings.config.reset"))) {
                resetConfiguration();
            } else if (buttonText.equals(i18n.getText("button.browse"))) {
                browseToolDirectory();
            } else if (buttonText.equals(i18n.getText("button.apply"))) {
                if (e.getSource() == applyDirectoryButton) {
                    applyToolDirectory();
                } else if (e.getSource() == applyPrefixButton) {
                    applyCommandPrefix();
                }
            } else if (buttonText.equals(i18n.getText("button.reset"))) {
                resetCommandPrefix();
            }
        };
    }
    
    /**
     * 导入配置文件
     */
    private void importConfiguration() {
        JFileChooser fileChooser = new JFileChooser();
        I18nManager i18n = I18nManager.getInstance();
        fileChooser.setFileFilter(new FileNameExtensionFilter(i18n.getText("dialog.config.file.filter"), "json"));
        fileChooser.setDialogTitle(i18n.getText("dialog.import.config.title"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            controller.importConfiguration(selectedFile.getAbsolutePath(), this);
        }
    }
    
    /**
     * 导出配置文件
     */
    private void exportConfiguration() {
        JFileChooser fileChooser = new JFileChooser();
        I18nManager i18n = I18nManager.getInstance();
        fileChooser.setFileFilter(new FileNameExtensionFilter(i18n.getText("dialog.config.file.filter"), "json"));
        fileChooser.setDialogTitle(i18n.getText("dialog.export.config.title"));
        fileChooser.setSelectedFile(new File("bparsenal_config_" + System.currentTimeMillis() + ".json"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // 确保文件有.json扩展名
            if (!selectedFile.getName().toLowerCase().endsWith(".json")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".json");
            }
            
            controller.exportConfiguration(selectedFile.getAbsolutePath());
        }
    }
    
    /**
     * 重置配置文件
     */
    private void resetConfiguration() {
        controller.resetConfiguration(this);
    }
    
    /**
     * 浏览工具目录
     */
    private void browseToolDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        I18nManager i18n = I18nManager.getInstance();
        fileChooser.setDialogTitle(i18n.getText("dialog.choose.tool.directory.title"));
        
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
        controller.setToolDirectory(directoryPath);
    }
    
    /**
     * 应用命令前缀设置
     */
    private void applyCommandPrefix() {
        String prefix = commandPrefixField.getText().trim();
        controller.setCommandPrefix(prefix);
    }
    
    /**
     * 重置命令前缀
     */
    private void resetCommandPrefix() {
        String defaultPrefix = settingModel.getDefaultCommandPrefix();
        commandPrefixField.setText(defaultPrefix);
            I18nManager i18n = I18nManager.getInstance();
            updatePrefixStatus(i18n.getText("status.prefix.reset.default") + ": " + defaultPrefix, SettingPanelController.StatusType.INFO);
    }
    
    /**
     * 刷新设置面板
     */
    public void refreshPanel() {
        controller.refreshSettings();
    }
    
    /**
     * 刷新所有设置组件显示
     * 参考MainPanel的刷新逻辑
     */
    private void refreshAllSettings() {
        try {
            // 重新加载配置数据（类似MainPanel的refreshConfiguration）
            ConfigManager.getInstance().reloadConfig();
            
            // 刷新控制器设置数据
            controller.refreshSettings();
            
            // 重新加载SettingModel数据
            settingModel = controller.getSettingModel();
            
            // 更新工具目录显示
            String currentToolDirectory = settingModel.getToolDirectory();
            if (toolDirectoryField != null) {
                toolDirectoryField.setText(currentToolDirectory != null ? currentToolDirectory : "");
            }
            
            // 更新命令前缀显示
            String currentCommandPrefix = settingModel.getCommandPrefix();
            if (commandPrefixField != null) {
                commandPrefixField.setText(currentCommandPrefix != null ? currentCommandPrefix : "");
            }
            
            // 更新语言设置显示
            I18nManager i18n = I18nManager.getInstance();
            I18nManager.SupportedLanguage currentLanguage = i18n.getCurrentLanguage();
            if (languageComboBox != null && languageActionListener != null && currentLanguage != null) {
                languageComboBox.removeActionListener(languageActionListener);
                try {
                    languageComboBox.setSelectedItem(currentLanguage);
                } finally {
                    languageComboBox.addActionListener(languageActionListener);
                }
            }
            
            // 更新所有状态标签
            updateAllStatusLabels();
            
            // 重绘面板（类似MainPanel的处理）
            revalidate();
            repaint();
            
            // 记录成功日志（类似MainPanel的logInfo）
            
            // 可以在这里通知其他需要刷新的组件
            notifyOtherPanelsToRefresh();
            
        } catch (Exception e) {
            // 记录错误日志（类似MainPanel的logError）
        }
    }
    
    /**
     * 更新所有状态标签
     */
    private void updateAllStatusLabels() {
        I18nManager i18n = I18nManager.getInstance();
        
        // 更新配置状态
        if (configStatusLabel != null) {
            configStatusLabel.setText(i18n.getText("settings.config.status") + ": " + 
                i18n.getText("settings.config.status.loaded"));
            configStatusLabel.setForeground(getStatusColor(SettingPanelController.StatusType.SUCCESS));
        }
        
        // 更新目录状态
        if (directoryStatusLabel != null) {
            String currentDirectory = settingModel.getToolDirectory();
            if (currentDirectory != null && !currentDirectory.trim().isEmpty()) {
                directoryStatusLabel.setText(i18n.getText("settings.directory.status") + ": " + 
                    i18n.getText("settings.directory.status.set"));
                directoryStatusLabel.setForeground(getStatusColor(SettingPanelController.StatusType.SUCCESS));
            } else {
                directoryStatusLabel.setText(i18n.getText("settings.directory.status") + ": " + 
                    i18n.getText("settings.directory.status.notset"));
                directoryStatusLabel.setForeground(getStatusColor(SettingPanelController.StatusType.INFO));
            }
        }
        
        // 更新前缀状态
        if (prefixStatusLabel != null) {
            String currentPrefix = settingModel.getCommandPrefix();
            if (currentPrefix != null && !currentPrefix.trim().isEmpty()) {
                prefixStatusLabel.setText(i18n.getText("settings.prefix.status") + ": " + 
                    i18n.getText("settings.prefix.status.custom"));
                prefixStatusLabel.setForeground(getStatusColor(SettingPanelController.StatusType.SUCCESS));
            } else {
                prefixStatusLabel.setText(i18n.getText("settings.prefix.status") + ": " + 
                    i18n.getText("settings.prefix.status.default"));
                prefixStatusLabel.setForeground(getStatusColor(SettingPanelController.StatusType.INFO));
            }
        }
        
        // 更新语言状态
        if (languageStatusLabel != null) {
            languageStatusLabel.setText(i18n.getText("settings.language.restart.required"));
            languageStatusLabel.setForeground(getStatusColor(SettingPanelController.StatusType.INFO));
        }
    }
    
    /**
     * 记录信息日志（参考MainPanel的logInfo方法）
     * @param message 日志消息
     */
    private void logInfo(String message) {
        if (manager.ApiManager.getInstance().isInitialized()) {
        }
    }
    
    /**
     * 记录错误日志（参考MainPanel的logError方法）
     * @param message 错误消息
     */
    private void logError(String message) {
        if (manager.ApiManager.getInstance().isInitialized()) {
        }
    }
    
    /**
     * 通知其他面板刷新
     * 当配置重置后，可能需要通知其他面板重新加载数据
     */
    private void notifyOtherPanelsToRefresh() {
        try {
            // 获取父容器（MainPanel），通知其刷新其他面板
            Container parent = getParent();
            while (parent != null && !(parent instanceof JTabbedPane)) {
                parent = parent.getParent();
            }
            
            if (parent instanceof JTabbedPane) {
                JTabbedPane tabbedPane = (JTabbedPane) parent;
                
                // 遍历所有选项卡，刷新其他面板的数据
                for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                    Component component = tabbedPane.getComponentAt(i);
                    
                    // 如果是ToolPanel，调用其loadData方法
                    if (component instanceof ToolPanel && component != this) {
                        ((ToolPanel) component).loadData();
                    }
                    // 如果是ThirdPartyPanel，调用其loadData方法
                    else if (component instanceof ThirdPartyPanel && component != this) {
                        ((ThirdPartyPanel) component).loadData();
                    }
                    // 如果是WebsitePanel，调用其loadData方法
                    else if (component instanceof WebsitePanel && component != this) {
                        ((WebsitePanel) component).loadData();
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * 获取工具目录设置
     * @return 工具目录路径
     */
    public String getToolDirectory() {
        return settingModel.getToolDirectory();
    }
    
    /**
     * 获取命令前缀设置
     * @return 命令前缀
     */
    public String getCommandPrefix() {
        return settingModel.getCommandPrefix();
    }
    
    /**
     * 获取格式化后的命令前缀数组
     * @return 命令前缀数组
     */
    public String[] getCommandPrefixArray() {
        return settingModel.getCommandPrefixArray();
    }
    
    // =========================== 实现SettingPanelView接口 ===========================
    
    @Override
    public boolean confirmImportConfiguration(String fileName) {
        int result = JOptionPane.showConfirmDialog(
            this,
            I18nManager i18n = I18nManager.getInstance();
            i18n.getText("confirm.import.config.message", fileName),
            i18n.getText("confirm.import.config.title"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }
    
    @Override
    public boolean confirmResetConfiguration() {
        int result = JOptionPane.showConfirmDialog(
            this,
            I18nManager i18n = I18nManager.getInstance();
            i18n.getText("confirm.reset.config.message"),
            i18n.getText("confirm.reset.config.title"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }
    
    @Override
    public boolean confirmCreateDirectory(String directoryPath) {
        int result = JOptionPane.showConfirmDialog(
            this,
            I18nManager i18n = I18nManager.getInstance();
            i18n.getText("confirm.create.directory.message", directoryPath),
            i18n.getText("confirm.create.directory.title"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }
    
    @Override
    public void showImportSuccessMessage(String fileName) {
        JOptionPane.showMessageDialog(
            this,
            I18nManager i18n = I18nManager.getInstance();
            i18n.getText("success.import.config.message"),
            i18n.getText("success.import.config.title"),
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    @Override
    public void showImportErrorMessage(String errorMessage) {
        JOptionPane.showMessageDialog(
            this,
            I18nManager i18n = I18nManager.getInstance();
            i18n.getText("failed.import.config.message", errorMessage),
            i18n.getText("failed.import.config.title"),
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    @Override
    public void showExportSuccessMessage(String filePath) {
        JOptionPane.showMessageDialog(
            this,
            I18nManager i18n = I18nManager.getInstance();
            i18n.getText("success.export.config.message", filePath),
            i18n.getText("success.export.config.title"),
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    @Override
    public void showExportErrorMessage(String errorMessage) {
        JOptionPane.showMessageDialog(
            this,
            I18nManager i18n = I18nManager.getInstance();
            i18n.getText("failed.export.config.message", errorMessage),
            i18n.getText("failed.export.config.title"),
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    @Override
    public void showResetSuccessMessage() {
        JOptionPane.showMessageDialog(
            this,
            I18nManager i18n = I18nManager.getInstance();
            i18n.getText("success.reset.config.message"),
            i18n.getText("success.reset.config.title"),
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    @Override
    public void showResetErrorMessage(String errorMessage) {
        JOptionPane.showMessageDialog(
            this,
            I18nManager i18n = I18nManager.getInstance();
            i18n.getText("failed.reset.config.message", errorMessage),
            i18n.getText("failed.reset.config.title"),
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    @Override
    public void showDirectorySuccessMessage(String directoryPath) {
        JOptionPane.showMessageDialog(
            this,
            I18nManager i18n = I18nManager.getInstance();
            i18n.getText("success.set.directory.message", directoryPath),
            i18n.getText("success.set.directory.title"),
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    @Override
    public void showPrefixSuccessMessage(String prefix) {
        JOptionPane.showMessageDialog(
            this,
            I18nManager i18n = I18nManager.getInstance();
            String prefixDisplay = (prefix == null || prefix.trim().isEmpty()) ? i18n.getText("success.set.prefix.default") : prefix;
            i18n.getText("success.set.prefix.message", prefixDisplay),
            i18n.getText("success.set.prefix.title"),
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    @Override
    public void updateToolDirectory(String directory) {
        if (toolDirectoryField != null) {
            toolDirectoryField.setText(directory);
        }
    }
    
    @Override
    public void updateCommandPrefix(String prefix) {
        if (commandPrefixField != null) {
            commandPrefixField.setText(prefix);
        }
    }
    
    @Override
    public void updateLanguage(I18nManager.SupportedLanguage language) {
        if (languageComboBox != null && languageActionListener != null) {
            // 临时移除监听器避免循环触发
            languageComboBox.removeActionListener(languageActionListener);
            try {
                languageComboBox.setSelectedItem(language);
            } finally {
                languageComboBox.addActionListener(languageActionListener);
            }
        }
    }
    
    @Override
    public void updateStatus(String message, SettingPanelController.StatusType type) {
        // 可以根据需要添加通用状态显示
    }
    
    @Override
    public void updateConfigStatus(String message, SettingPanelController.StatusType type) {
        if (configStatusLabel != null) {
            I18nManager i18n = I18nManager.getInstance();
            configStatusLabel.setText(i18n.getText("status.config.status", message));
            configStatusLabel.setForeground(getStatusColor(type));
        }
    }
    
    @Override
    public void updateDirectoryStatus(String message, SettingPanelController.StatusType type) {
        if (directoryStatusLabel != null) {
            I18nManager i18n = I18nManager.getInstance();
            directoryStatusLabel.setText(i18n.getText("status.directory.status", message));
            directoryStatusLabel.setForeground(getStatusColor(type));
        }
    }
    
    @Override
    public void updatePrefixStatus(String message, SettingPanelController.StatusType type) {
        if (prefixStatusLabel != null) {
            I18nManager i18n = I18nManager.getInstance();
            prefixStatusLabel.setText(i18n.getText("status.prefix.status", message));
            prefixStatusLabel.setForeground(getStatusColor(type));
        }
    }
    
    @Override
    public void updateLanguageStatus(String message, SettingPanelController.StatusType type) {
        if (languageStatusLabel != null) {
            I18nManager i18n = I18nManager.getInstance();
            languageStatusLabel.setText(i18n.getText("status.language.status", message));
            languageStatusLabel.setForeground(getStatusColor(type));
        }
    }
    
    /**
     * 获取状态颜色
     */
    private Color getStatusColor(SettingPanelController.StatusType type) {
        switch (type) {
            case SUCCESS: return Color.GREEN;
            case ERROR: return Color.RED;
            case WARNING: return Color.ORANGE;
            case INFO: 
            default: return new Color(100, 100, 100);
        }
    }
    
    // =========================== 实现SettingPanelListener接口 ===========================
    
    @Override
    public void onSettingsInitialized() {
        // 设置初始化完成后的处理
    }
    
    @Override
    public void onSettingsLoaded() {
        // 设置加载完成后的处理
    }
    
    @Override
    public void onConfigurationImported(String filePath) {
        // 配置导入完成后刷新页面
        SwingUtilities.invokeLater(() -> {
            refreshAllSettings();
        });
    }
    
    @Override
    public void onConfigurationExported(String filePath) {
        // 配置导出完成后的处理
    }
    
    @Override
    public void onConfigurationReset() {
        // 配置重置完成后刷新页面
        SwingUtilities.invokeLater(() -> {
            refreshAllSettings();
        });
    }
    
    @Override
    public void onToolDirectoryChanged(String directory) {
        // 工具目录更改完成后的处理
    }
    
    @Override
    public void onCommandPrefixChanged(String prefix) {
        // 命令前缀更改完成后的处理
    }
    

    
    @Override
    public void onLanguageAutoSet(I18nManager.SupportedLanguage language) {
        // 语言自动设置完成后的处理
        updateLanguage(language);
    }
    
    @Override
    public void onError(String operation, String errorMessage) {
        // 错误处理
    }
    
    // =========================== 实现LanguageChangeListener接口 ===========================
    
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
        
        // 更新按钮文本
        if (importConfigButton != null) {
            importConfigButton.setText(i18n.getText("settings.config.import"));
        }
        if (exportConfigButton != null) {
            exportConfigButton.setText(i18n.getText("settings.config.export"));
        }
        if (resetConfigButton != null) {
            resetConfigButton.setText(i18n.getText("settings.config.reset"));
        }
        if (applyDirectoryButton != null) {
            applyDirectoryButton.setText(i18n.getText("button.apply"));
        }
        if (browseDirectoryButton != null) {
            browseDirectoryButton.setText(i18n.getText("button.browse"));
        }
        if (resetPrefixButton != null) {
            resetPrefixButton.setText(i18n.getText("button.reset"));
        }
        if (applyPrefixButton != null) {
            applyPrefixButton.setText(i18n.getText("button.apply"));
        }
        
        // 更新状态标签
        if (configStatusLabel != null) {
            configStatusLabel.setText(i18n.getText("settings.config.status") + ": " + i18n.getText("settings.config.status.loaded"));
        }
        
        // 更新语言下拉框选项
        updateLanguageComboBox();
    }
    
    /**
     * 更新语言下拉框选项
     */
    private void updateLanguageComboBox() {
        if (languageComboBox != null && languageActionListener != null) {
            languageComboBox.removeActionListener(languageActionListener);
            
            try {
                languageComboBox.removeAllItems();
                I18nManager i18n = I18nManager.getInstance();
                for (I18nManager.SupportedLanguage language : i18n.getSupportedLanguages()) {
                    languageComboBox.addItem(language);
                }
                
                // 恢复选择
                I18nManager.SupportedLanguage actualCurrent = i18n.getCurrentLanguage();
                if (actualCurrent != null) {
                    languageComboBox.setSelectedItem(actualCurrent);
                }
            } finally {
                languageComboBox.addActionListener(languageActionListener);
            }
        }
    }
} 