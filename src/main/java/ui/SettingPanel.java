package ui;

import javax.swing.*;
import java.awt.*;

/**
 * 设置面板
 * 用于插件配置和管理
 */
public class SettingPanel extends JPanel {
    
    public SettingPanel() {
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        JLabel label = new JLabel("设置面板 - 开发中...", JLabel.CENTER);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        label.setForeground(Color.GRAY);
        
        add(label, BorderLayout.CENTER);
    }
} 