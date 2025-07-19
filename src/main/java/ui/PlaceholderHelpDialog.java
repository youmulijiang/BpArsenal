package ui;

import util.PlaceholderDocumentation;
import javax.swing.*;
import java.awt.*;

/**
 * 占位符帮助对话框
 * 显示所有可用的HTTP占位符变量
 */
public class PlaceholderHelpDialog extends JDialog {
    
    public PlaceholderHelpDialog(Window parent) {
        super(parent, "HTTP占位符帮助", ModalityType.APPLICATION_MODAL);
        initializeUI();
        setLocationRelativeTo(parent);
    }
    
    /**
     * 初始化UI组件
     */
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(800, 600);
        setResizable(true);
        
        // 创建选项卡面板
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // 完整文档
        JTextArea fullDocArea = new JTextArea();
        fullDocArea.setEditable(false);
        fullDocArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        fullDocArea.setText(PlaceholderDocumentation.generateFullDocumentation());
        fullDocArea.setCaretPosition(0);
        
        JScrollPane fullDocScroll = new JScrollPane(fullDocArea);
        tabbedPane.addTab("📋 完整文档", fullDocScroll);
        
        // 使用示例
        JTextArea exampleArea = new JTextArea();
        exampleArea.setEditable(false);
        exampleArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        exampleArea.setText(PlaceholderDocumentation.generateUsageExamples());
        exampleArea.setCaretPosition(0);
        
        JScrollPane exampleScroll = new JScrollPane(exampleArea);
        tabbedPane.addTab("💡 使用示例", exampleScroll);
        
        // 分类浏览
        JPanel categoryPanel = createCategoryPanel();
        tabbedPane.addTab("📂 分类浏览", categoryPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("关闭");
        closeButton.setPreferredSize(new Dimension(80, 30));
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 创建分类面板
     * @return 分类面板
     */
    private JPanel createCategoryPanel() {
        JPanel categoryPanel = new JPanel(new BorderLayout());
        
        // 分类列表
        String[] categories = {
            "请求基础", "请求头部", "请求参数", "请求Cookie", "请求体",
            "响应状态", "响应头部", "响应Cookie", "响应体"
        };
        
        JList<String> categoryList = new JList<>(categories);
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        
        // 详细内容
        JTextArea detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setFont(new Font("Consolas", Font.PLAIN, 10));
        
        // 选择监听器
        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = categoryList.getSelectedValue();
                if (selected != null) {
                    StringBuilder content = new StringBuilder();
                    content.append("=== ").append(selected).append(" ===\n\n");
                    
                    PlaceholderDocumentation.getVariablesByCategory(selected).forEach(var -> {
                        content.append(String.format("%-40s : %s\n", var.getName(), var.getDescription()));
                        content.append(String.format("%-40s   示例: %s\n\n", "", var.getExample()));
                    });
                    
                    detailArea.setText(content.toString());
                    detailArea.setCaretPosition(0);
                }
            }
        });
        
        // 默认选择第一项
        categoryList.setSelectedIndex(0);
        
        // 布局
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(new JScrollPane(categoryList));
        splitPane.setRightComponent(new JScrollPane(detailArea));
        splitPane.setDividerLocation(150);
        
        categoryPanel.add(splitPane, BorderLayout.CENTER);
        
        return categoryPanel;
    }
} 