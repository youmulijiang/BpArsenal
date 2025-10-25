package util;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * 表格渲染器工厂类
 * 提供各种类型的表格单元格渲染器
 */
public class TableRendererFactory {
    
    /**
     * 创建收藏状态渲染器
     * 显示星号图标表示收藏状态
     */
    public static TableCellRenderer createFavoriteRenderer() {
        return new FavoriteRenderer();
    }
    
    /**
     * 创建纯文本渲染器
     * 用于命令列的文本显示，支持长文本截断和工具提示
     */
    public static TableCellRenderer createPlainTextRenderer() {
        return new PlainTextRenderer();
    }
    
    /**
     * 创建交替行颜色渲染器
     * 为表格行提供交替的背景色
     */
    public static TableCellRenderer createAlternateRowRenderer() {
        return new AlternateRowRenderer();
    }
    
    /**
     * 收藏状态渲染器内部类
     */
    private static class FavoriteRenderer extends DefaultTableCellRenderer {
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            JLabel label = new JLabel();
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setOpaque(true);
            
            // 设置选中状态的颜色
            if (isSelected) {
                label.setBackground(table.getSelectionBackground());
                label.setForeground(table.getSelectionForeground());
            } else {
                label.setBackground(table.getBackground());
                label.setForeground(table.getForeground());
            }
            
            // 显示收藏状态
            if (value instanceof Boolean) {
                boolean isFavorite = (Boolean) value;
                label.setText(isFavorite ? "★" : "☆");
                label.setForeground(isFavorite ? 
                    new Color(255, 152, 0) : // 橙色表示收藏
                    new Color(158, 158, 158)   // 灰色表示未收藏
                );
            }
            
            return label;
        }
    }
    
    /**
     * 纯文本渲染器内部类
     */
    private static class PlainTextRenderer extends DefaultTableCellRenderer {
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value != null) {
                String text = value.toString();
                
                // 设置工具提示显示完整文本
                setToolTipText(text);
                
                // 如果文本太长，进行截断显示
                if (text.length() > 130) {
                    setText(text.substring(0, 87) + "...");
                } else {
                    setText(text);
                }
                
                // 设置文本左对齐
                setHorizontalAlignment(JLabel.LEFT);
            }
            
            return comp;
        }
    }
    
    /**
     * 交替行颜色渲染器内部类
     */
    private static class AlternateRowRenderer extends DefaultTableCellRenderer {
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // 只在非选中状态时设置交替行颜色
            if (!isSelected) {
                if (row % 2 == 0) {
                    comp.setBackground(Color.WHITE);
                } else {
                    comp.setBackground(new Color(248, 248, 248)); // 浅灰色
                }
            }
            
            return comp;
        }
    }
    
    /**
     * 配置表格渲染器
     * 为指定的表格设置合适的渲染器
     * 
     * @param table 目标表格
     * @param favoriteColumnIndex 收藏列索引
     * @param textColumnIndex 文本列索引（如命令列）
     */
    public static void configureTableRenderers(JTable table, int favoriteColumnIndex, int textColumnIndex) {
        if (table == null || table.getColumnModel() == null) {
            return;
        }
        
        try {
            // 设置收藏列渲染器
            if (favoriteColumnIndex >= 0 && favoriteColumnIndex < table.getColumnCount()) {
                table.getColumnModel().getColumn(favoriteColumnIndex)
                     .setCellRenderer(createFavoriteRenderer());
            }
            
            // 设置文本列渲染器
            if (textColumnIndex >= 0 && textColumnIndex < table.getColumnCount()) {
                table.getColumnModel().getColumn(textColumnIndex)
                     .setCellRenderer(createPlainTextRenderer());
            }
            
        } catch (Exception e) {
        }
    }
    
    /**
     * 重置表格渲染器
     * 移除所有自定义渲染器，恢复默认样式
     * 
     * @param table 目标表格
     */
    public static void resetTableRenderers(JTable table) {
        if (table == null || table.getColumnModel() == null) {
            return;
        }
        
        try {
            // 重置所有列的渲染器为默认
            for (int i = 0; i < table.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setCellRenderer(null);
            }
        } catch (Exception e) {
        }
    }
    
    /**
     * 应用表格样式
     * 设置表格的基本样式属性
     * 
     * @param table 目标表格
     */
    public static void applyTableStyle(JTable table) {
        if (table == null) {
            return;
        }
        
        try {
            // 基本字体和行高
            table.setFont(new Font("微软雅黑", Font.PLAIN, 11));
            table.setRowHeight(25);
            
            // 选择模式
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            // 网格线设置（可选）
            table.setGridColor(new Color(230, 230, 230));
            table.setShowGrid(true);
            
            // 表头样式
            if (table.getTableHeader() != null) {
                table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
                table.getTableHeader().setBackground(new Color(245, 245, 245));
                table.getTableHeader().setReorderingAllowed(false);
            }
            
        } catch (Exception e) {
        }
    }
} 