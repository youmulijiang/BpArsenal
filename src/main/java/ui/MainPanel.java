package ui;

import manager.ConfigManager;
import manager.ApiManager;
import javax.swing.*;
import java.awt.*;

/**
 * 主面板UI组件
 * 提供BpArsenal插件的主要用户界面，包含四个功能选项卡
 */
public class MainPanel extends JPanel {
    
    private JTabbedPane tabbedPane;
    private ToolPanel toolPanel;
    private ThirdPartyPanel thirdPartyPanel;
    private WebsitePanel websitePanel;
    private SettingPanel settingPanel;
    
    public MainPanel() {
        initializeUI();
        loadData();
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
        tabbedPane.addTab("🔧 HTTP工具", createTabIcon("tool"), toolPanel, "HTTP渗透测试工具");
        tabbedPane.addTab("⚙️ 第三方工具", createTabIcon("third"), thirdPartyPanel, "外部工具启动管理");
        tabbedPane.addTab("🌐 网站导航", createTabIcon("website"), websitePanel, "快速访问常用网站");
        tabbedPane.addTab("⚙️ 设置", createTabIcon("setting"), settingPanel, "插件配置和管理");
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // 创建状态栏
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
        
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
        JLabel titleLabel = new JLabel("BpArsenal - Burp扩展工具集");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(new Color(51, 51, 51));
        
        // 版本标签
        JLabel versionLabel = new JLabel("v1.0.0");
        versionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        versionLabel.setForeground(new Color(128, 128, 128));
        
        // 刷新按钮
        JButton refreshButton = new JButton("🔄 刷新配置");
        refreshButton.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        refreshButton.setPreferredSize(new Dimension(100, 25));
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
     * 创建选项卡图标（简单实现）
     * @param type 图标类型
     * @return 图标
     */
    private Icon createTabIcon(String type) {
        // 这里可以后续添加实际的图标
        return null;
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
    
    /**
     * 获取当前选中的选项卡索引
     * @return 选项卡索引
     */
    public int getSelectedTabIndex() {
        return tabbedPane.getSelectedIndex();
    }
    
    /**
     * 切换到指定选项卡
     * @param index 选项卡索引
     */
    public void setSelectedTab(int index) {
        if (index >= 0 && index < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(index);
        }
    }
} 