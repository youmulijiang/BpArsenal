package view;

import manager.ConfigManager;
import manager.ApiManager;
import util.I18nManager;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * 主面板UI组件 (View层)
 * 提供BpArsenal插件的主要用户界面，包含四个功能选项卡
 */
public class MainPanel extends JPanel implements I18nManager.LanguageChangeListener {
    
    private JTabbedPane tabbedPane;
    private ToolPanel toolPanel;
    private ThirdPartyPanel thirdPartyPanel;
    private WebsitePanel websitePanel;
    private SettingPanel settingPanel;
    private JLabel titleLabel;
    private JButton refreshButton;
    
    public MainPanel() {
        initializeUI();
        loadData();
        
        // 注册语言变更监听器
        I18nManager.getInstance().addLanguageChangeListener(this);
    }
    
    /**
     * 语言变更监听器实现
     */
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
        
        // 更新标题
        if (titleLabel != null) {
            titleLabel.setText(i18n.getText("main.title"));
        }
        
        // 更新刷新按钮
        if (refreshButton != null) {
            refreshButton.setText(i18n.getText("button.refresh"));
        }
        
        // 更新选项卡标题
        if (tabbedPane != null) {
            tabbedPane.setTitleAt(0, i18n.getText("main.tab.tools"));
            tabbedPane.setTitleAt(1, i18n.getText("main.tab.thirdparty"));
            tabbedPane.setTitleAt(2, i18n.getText("main.tab.websites"));
            tabbedPane.setTitleAt(3, i18n.getText("main.tab.settings"));
            
            // 更新选项卡提示文本
            tabbedPane.setToolTipTextAt(0, i18n.getText("tools.title"));
            tabbedPane.setToolTipTextAt(1, i18n.getText("thirdparty.title"));
            tabbedPane.setToolTipTextAt(2, i18n.getText("websites.title"));
            tabbedPane.setToolTipTextAt(3, i18n.getText("settings.title"));
        }
    }
    
    /**
     * 初始化UI组件
     */
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // 创建标题面板
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);
        
        // 创建选项卡面板
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        
        // 初始化各个面板
        toolPanel = new ToolPanel();
        thirdPartyPanel = new ThirdPartyPanel();
        websitePanel = new WebsitePanel();
        settingPanel = new SettingPanel();
        
        // 添加选项卡
        I18nManager i18n = I18nManager.getInstance();
        tabbedPane.addTab(i18n.getText("main.tab.tools"), null, toolPanel, i18n.getText("tools.title"));
        tabbedPane.addTab(i18n.getText("main.tab.thirdparty"), null, thirdPartyPanel, i18n.getText("thirdparty.title"));
        tabbedPane.addTab(i18n.getText("main.tab.websites"), null, websitePanel, i18n.getText("websites.title"));
        tabbedPane.addTab(i18n.getText("main.tab.settings"), null, settingPanel, i18n.getText("settings.title"));
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // 创建状态栏
        JPanel statusPanel = createStatusPanel();
//        add(statusPanel, BorderLayout.SOUTH);
        
        // 设置面板首选大小
        setPreferredSize(new Dimension(900, 700));
    }
    
    /**
     * 创建标题面板
     * @return 标题面板
     */
    private JPanel createTitlePanel() {

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        titlePanel.setBackground(new Color(245, 245, 245));
        
        // 标题标签
        titleLabel = new JLabel(I18nManager.getInstance().getText("main.title"));
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(new Color(51, 51, 51));
        
        // 版本标签
        JLabel versionLabel = new JLabel("v1.0.0");
        versionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        versionLabel.setForeground(new Color(128, 128, 128));
        
        // 刷新按钮
        refreshButton = new JButton(I18nManager.getInstance().getText("button.refresh"));
        refreshButton.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        refreshButton.setPreferredSize(new Dimension(80, 25));
        refreshButton.addActionListener(e -> refreshConfiguration());
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(titleLabel);
        leftPanel.add(Box.createHorizontalStrut(10));
        leftPanel.add(versionLabel);
        
        titlePanel.add(leftPanel, BorderLayout.WEST);
        titlePanel.add(refreshButton, BorderLayout.EAST);
        
        return titlePanel;
    }

    private ImageIcon getImageIcon() {
        ClassLoader classLoader = getClass().getClassLoader();
        URL imageURL;
        imageURL = classLoader.getResource("logo/logo.png");
        ImageIcon originalIcon = new ImageIcon(imageURL);
        Image originalImage = originalIcon.getImage();
        Image scaledImage = originalImage.getScaledInstance(35, 30, Image.SCALE_FAST);
        return new ImageIcon(scaledImage);
    }

    
    /**
     * 创建状态栏
     * @return 状态栏面板
     */
    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        statusPanel.setBackground(new Color(240, 240, 240));
        
        JLabel statusLabel = new JLabel("就绪");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(100, 100, 100));
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
        
        return statusPanel;
    }
    
    /**
     * 加载配置数据
     */
    private void loadData() {
        try {
            // 加载各个面板的数据
            if (toolPanel != null) {
                toolPanel.loadData();
            }
            if (thirdPartyPanel != null) {
                thirdPartyPanel.loadData();
            }
            if (websitePanel != null) {
                websitePanel.loadData();
            }
            
            logInfo("配置数据加载完成");
        } catch (Exception e) {
            logError("加载配置数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 刷新配置
     */
    private void refreshConfiguration() {
        try {
            ConfigManager.getInstance().reloadConfig();
            loadData();
            logInfo("配置已刷新");
            
            // 显示成功提示
            JOptionPane.showMessageDialog(this, 
                "配置刷新成功！", 
                "信息", 
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            logError("刷新配置失败: " + e.getMessage());
            
            // 显示错误提示
            JOptionPane.showMessageDialog(this, 
                "配置刷新失败:\n" + e.getMessage(), 
                "错误", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 记录信息日志
     * @param message 日志消息
     */
    private void logInfo(String message) {
        if (ApiManager.getInstance().isInitialized()) {
            ApiManager.getInstance().getApi().logging().logToOutput("BpArsenal UI: " + message);
        }
    }
    
    /**
     * 记录错误日志
     * @param message 错误消息
     */
    private void logError(String message) {
        if (ApiManager.getInstance().isInitialized()) {
            ApiManager.getInstance().getApi().logging().logToError("BpArsenal UI: " + message);
        }
    }
} 