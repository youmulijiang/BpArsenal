package util;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.HashMap;

/**
 * 菜单工具类
 * 负责UI菜单的创建和样式处理
 */
public class MenuUtils {
    
    // 分类颜色映射
    private static final Map<String, Color> CATEGORY_COLORS = new HashMap<>();
    
    static {
        CATEGORY_COLORS.put("sql注入", new Color(220, 53, 69));      // 红色
        CATEGORY_COLORS.put("sql", new Color(220, 53, 69));
        CATEGORY_COLORS.put("xss", new Color(255, 193, 7));         // 黄色
        CATEGORY_COLORS.put("跨站脚本", new Color(255, 193, 7));
        CATEGORY_COLORS.put("扫描工具", new Color(40, 167, 69));      // 绿色
        CATEGORY_COLORS.put("扫描", new Color(40, 167, 69));
        CATEGORY_COLORS.put("爆破工具", new Color(255, 87, 34));      // 橙色
        CATEGORY_COLORS.put("爆破", new Color(255, 87, 34));
        CATEGORY_COLORS.put("漏洞利用", new Color(156, 39, 176));     // 紫色
        CATEGORY_COLORS.put("exploit", new Color(156, 39, 176));
        CATEGORY_COLORS.put("信息收集", new Color(3, 169, 244));       // 蓝色
        CATEGORY_COLORS.put("reconnaissance", new Color(3, 169, 244));
        CATEGORY_COLORS.put("批量扫描", new Color(0, 123, 255));       // 深蓝色
    }
    
    /**
     * 创建分类图标
     * @param category 分类名称
     * @return 图标对象
     */
    public static Icon createCategoryIcon(String category) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Color iconColor = getCategoryColor(category);
                g.setColor(iconColor);
                g.fillOval(x + 2, y + 2, 8, 8);
                g.setColor(iconColor.darker());
                g.drawOval(x + 2, y + 2, 8, 8);
            }
            
            @Override
            public int getIconWidth() { return 12; }
            
            @Override
            public int getIconHeight() { return 12; }
        };
    }
    
    /**
     * 根据分类获取对应颜色
     * @param category 分类名称
     * @return 颜色对象
     */
    public static Color getCategoryColor(String category) {
        if (category == null) {
            return new Color(108, 117, 125); // 默认灰色
        }
        
        return CATEGORY_COLORS.getOrDefault(
            category.toLowerCase(), 
            new Color(108, 117, 125) // 默认灰色
        );
    }
    
    /**
     * 创建工具菜单项
     * @param displayText 显示文本
     * @param toolTip 工具提示
     * @param actionListener 事件监听器
     * @return 菜单项
     */
    public static JMenuItem createToolMenuItem(String displayText, String toolTip, 
                                             java.awt.event.ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem(displayText);
        menuItem.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        menuItem.setToolTipText(toolTip);
        menuItem.addActionListener(actionListener);
        return menuItem;
    }
    
    /**
     * 创建分类子菜单
     * @param category 分类名称
     * @param commandCount 命令数量
     * @return 子菜单
     */
    public static JMenu createCategoryMenu(String category, int commandCount) {
        JMenu categoryMenu = new JMenu(category + " (" + commandCount + ")");
        categoryMenu.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        categoryMenu.setIcon(createCategoryIcon(category));
        return categoryMenu;
    }
    
    /**
     * 创建主菜单
     * @param title 菜单标题
     * @return 主菜单
     */
    public static JMenu createMainMenu(String title) {
        JMenu menu = new JMenu(title);
        menu.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        return menu;
    }
    
    /**
     * 截断文本以适应菜单显示
     * @param text 原始文本
     * @param maxLength 最大长度
     * @return 截断后的文本
     */
    public static String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
    
    /**
     * 创建工具提示HTML
     * @param toolName 工具名称
     * @param command 命令
     * @param category 分类
     * @return HTML格式的工具提示
     */
    public static String createToolTipHtml(String toolName, String command, String category) {
        return String.format(
            "<html><b>%s</b><br/>%s<br/><i>分类: %s</i></html>", 
            escapeHtml(toolName), 
            escapeHtml(truncateText(command, 100)),
            escapeHtml(category)
        );
    }
    
    /**
     * 转义HTML特殊字符
     * @param text 原始文本
     * @return 转义后的文本
     */
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    }
} 