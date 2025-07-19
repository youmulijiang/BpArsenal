package ui;

import javax.swing.*;
import java.awt.*;

/**
 * 主面板UI组件
 * 提供插件的主要用户界面
 */
public class MainPanel extends JPanel {
    
    public MainPanel() {
        initializeUI();
    }
    
    /**
     * 初始化UI组件
     */
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // 创建标题标签
        JLabel titleLabel = new JLabel("BpArsenal - Burp扩展工具集", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        add(titleLabel, BorderLayout.NORTH);
        
        // TODO: 添加选项卡面板
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // TODO: 添加HTTP工具标签页
        // TODO: 添加第三方工具标签页  
        // TODO: 添加网站导航标签页
        // TODO: 添加设置标签页
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // 设置面板首选大小
        setPreferredSize(new Dimension(800, 600));
    }
} 