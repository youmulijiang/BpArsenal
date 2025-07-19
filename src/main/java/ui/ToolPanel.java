package ui;

import javax.swing.*;
import java.awt.*;

/**
 * 工具面板UI组件
 * 用于显示和管理各种工具
 */
public class ToolPanel extends JPanel {
    
    public ToolPanel() {
        initializeUI();
    }
    
    /**
     * 初始化UI组件
     */
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // 工具栏
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton("刷新");
        JButton settingsButton = new JButton("设置");
        
        toolbarPanel.add(refreshButton);
        toolbarPanel.add(settingsButton);
        
        add(toolbarPanel, BorderLayout.NORTH);
        
        // 工具列表
        JList<String> toolList = new JList<>();
        toolList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(toolList);
        scrollPane.setPreferredSize(new Dimension(300, 400));
        
        add(scrollPane, BorderLayout.CENTER);
        
        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton executeButton = new JButton("执行");
        JButton favoriteButton = new JButton("收藏");
        
        buttonPanel.add(executeButton);
        buttonPanel.add(favoriteButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
} 