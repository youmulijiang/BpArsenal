package view;

import javax.swing.*;
import java.awt.*;

/**
 * 网站导航面板 (View层)
 * 用于快速访问常用安全网站
 */
public class WebsitePanel extends JPanel {
    
    public WebsitePanel() {
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        JLabel label = new JLabel("网站导航面板 - 开发中...", JLabel.CENTER);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        label.setForeground(Color.GRAY);
        
        add(label, BorderLayout.CENTER);
    }
    
    public void loadData() {
        // TODO: 加载网站数据
    }
} 