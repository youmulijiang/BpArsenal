package util;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;

/**
 * 网站渲染器工厂 (Factory Pattern)
 * 统一管理和创建网站面板的表格渲染器
 */
public class WebSiteRendererFactory {
    
    /**
     * 创建收藏状态渲染器
     * @return 收藏状态渲染器
     */
    public static TableCellRenderer createFavoriteRenderer() {
        return new WebSiteFavoriteRenderer();
    }
    
    /**
     * 创建URL渲染器
     * @return URL渲染器
     */
    public static TableCellRenderer createUrlRenderer() {
        return new WebSiteUrlRenderer();
    }
    
    /**
     * 创建纯文本渲染器
     * @return 纯文本渲染器
     */
    public static TableCellRenderer createPlainTextRenderer() {
        return new WebSitePlainTextRenderer();
    }
    
    /**
     * 创建交替行颜色渲染器
     * @return 交替行颜色渲染器
     */
    public static TableCellRenderer createAlternateRowRenderer() {
        return new WebSiteAlternateRowRenderer();
    }
    
    /**
     * 配置表格渲染器
     * @param table 表格对象
     * @param favoriteColumnIndex 收藏列索引
     * @param urlColumnIndex URL列索引
     */
    public static void configureTableRenderers(JTable table, int favoriteColumnIndex, int urlColumnIndex) {
        if (table != null) {
            TableColumnModel columnModel = table.getColumnModel();
            
            // 设置收藏列渲染器
            if (favoriteColumnIndex >= 0 && favoriteColumnIndex < columnModel.getColumnCount()) {
                columnModel.getColumn(favoriteColumnIndex).setCellRenderer(createFavoriteRenderer());
            }
            
            // 设置URL列渲染器
            if (urlColumnIndex >= 0 && urlColumnIndex < columnModel.getColumnCount()) {
                columnModel.getColumn(urlColumnIndex).setCellRenderer(createUrlRenderer());
            }
        }
    }
    
    /**
     * 重置表格渲染器为默认
     * @param table 表格对象
     */
    public static void resetTableRenderers(JTable table) {
        if (table != null) {
            TableColumnModel columnModel = table.getColumnModel();
            for (int i = 0; i < columnModel.getColumnCount(); i++) {
                columnModel.getColumn(i).setCellRenderer(null);
            }
        }
    }
    
    /**
     * 应用基本表格样式
     * @param table 表格对象
     */
    public static void applyTableStyle(JTable table) {
        if (table != null) {
            // 基本样式
            table.setFont(new Font("微软雅黑", Font.PLAIN, 11));
            table.setRowHeight(25);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            // 表头样式
            if (table.getTableHeader() != null) {
                table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
                table.getTableHeader().setReorderingAllowed(false);
            }
        }
    }
    
    // =========================== 内部渲染器类 ===========================
    
    /**
     * 收藏状态渲染器
     */
    private static class WebSiteFavoriteRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel label = new JLabel();
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setOpaque(true);

            if (isSelected) {
                label.setBackground(table.getSelectionBackground());
                label.setForeground(table.getSelectionForeground());
            } else {
                label.setBackground(table.getBackground());
                label.setForeground(table.getForeground());
            }

            if (value instanceof Boolean) {
                label.setText(((Boolean) value) ? "★" : "☆");
                label.setForeground(((Boolean) value) ? new Color(255, 152, 0) : new Color(158, 158, 158));
            }

            return label;
        }
    }
    
    /**
     * URL渲染器 - 链接样式
     */
    private static class WebSiteUrlRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value != null) {
                String url = value.toString();
                
                // 设置为等宽字体
                setFont(new Font("Consolas", Font.PLAIN, 10));
                
                // 设置链接样式
                if (!isSelected) {
                    setForeground(new Color(0, 0, 238)); // 蓝色链接
                }
                
                // 显示完整URL作为提示
                setToolTipText(url);
                
                // 如果URL太长，截断显示
                if (url.length() > 100) {
                    setText(url.substring(0, 57) + "...");
                } else {
                    setText(url);
                }
                
                // 设置文本对齐
                setHorizontalAlignment(JLabel.LEFT);
            }
            
            return comp;
        }
    }
    
    /**
     * 纯文本渲染器
     */
    private static class WebSitePlainTextRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // 设置标准字体
            setFont(new Font("微软雅黑", Font.PLAIN, 11));
            setHorizontalAlignment(JLabel.LEFT);
            
            if (value != null) {
                setToolTipText(value.toString());
            }
            
            return comp;
        }
    }
    
    /**
     * 交替行颜色渲染器
     */
    private static class WebSiteAlternateRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                if (row % 2 == 0) {
                    comp.setBackground(Color.WHITE);
                } else {
                    comp.setBackground(new Color(248, 248, 248));
                }
            }
            
            return comp;
        }
    }
} 