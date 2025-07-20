package view;

import javax.swing.*;
import java.awt.*;

/**
 * 第三方工具面板 (View层)
 * 用于管理和启动外部工具
 */
public class ThirdPartyPanel extends JPanel {
    
    public ThirdPartyPanel() {
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        JLabel label = new JLabel("第三方工具面板 - 开发中...", JLabel.CENTER);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        label.setForeground(Color.GRAY);
        
        add(label, BorderLayout.CENTER);
    }
    
    public void loadData() {
        // TODO: 加载第三方工具数据
    }
} 