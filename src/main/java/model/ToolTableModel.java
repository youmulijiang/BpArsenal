package model;

import util.I18nManager;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * HTTP工具表格模型
 * 负责管理表格数据和列定义
 */
public class ToolTableModel extends AbstractTableModel {
    
    private String[] columnNames;
    private List<HttpToolCommand> toolCommands = new ArrayList<>();
    
    public ToolTableModel() {
        updateColumnNames();
    }
    
    /**
     * 更新列名（用于国际化）
     */
    public void updateColumnNames() {
        I18nManager i18n = I18nManager.getInstance();
        columnNames = new String[]{
            i18n.getText("tools.tool.name"), 
            i18n.getText("tools.command"), 
            i18n.getText("column.favorite"), 
            i18n.getText("tools.note"), 
            i18n.getText("tools.work.dir"), 
            i18n.getText("label.category")
        };
    }
    
    @Override
    public int getRowCount() {
        return toolCommands.size();
    }
    
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
    
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= toolCommands.size()) {
            return null;
        }
        
        HttpToolCommand toolCommand = toolCommands.get(rowIndex);
        switch (columnIndex) {
            case 0: return toolCommand.getDisplayName();
            case 1: return toolCommand.getCommand();
            case 2: return toolCommand.isFavor();
            case 3: return toolCommand.getNote() != null ? toolCommand.getNote() : "";
            case 4: return toolCommand.getWorkDir() != null ? toolCommand.getWorkDir() : "";
            case 5: return toolCommand.getCategory();
            default: return null;
        }
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 2) return Boolean.class; // 收藏列
        return String.class;
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 2; // 只有收藏列可以直接编辑
    }
    
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= toolCommands.size()) {
            return;
        }
        
        HttpToolCommand toolCommand = toolCommands.get(rowIndex);
        switch (columnIndex) {
            case 2: // 收藏列
                if (value instanceof Boolean) {
                    toolCommand.setFavor((Boolean) value);
                    fireTableCellUpdated(rowIndex, columnIndex);
                }
                break;
            case 3: // 备注列
                if (value instanceof String) {
                    toolCommand.setNote((String) value);
                    fireTableCellUpdated(rowIndex, columnIndex);
                }
                break;
            case 4: // 工作目录列
                if (value instanceof String) {
                    toolCommand.setWorkDir((String) value);
                    fireTableCellUpdated(rowIndex, columnIndex);
                }
                break;
        }
    }
    
    /**
     * 设置工具命令列表
     */
    public void setToolCommands(List<HttpToolCommand> toolCommands) {
        if (toolCommands == null) {
            this.toolCommands = new ArrayList<>();
        } else {
            this.toolCommands = new ArrayList<>(toolCommands);
        }
        fireTableDataChanged();
    }
    
    /**
     * 添加工具命令
     */
    public void addToolCommand(HttpToolCommand toolCommand) {
        if (toolCommand != null) {
            toolCommands.add(toolCommand);
            fireTableRowsInserted(toolCommands.size() - 1, toolCommands.size() - 1);
        }
    }
    
    /**
     * 更新工具命令
     */
    public void updateToolCommand(int index, HttpToolCommand toolCommand) {
        if (index >= 0 && index < toolCommands.size() && toolCommand != null) {
            toolCommands.set(index, toolCommand);
            fireTableRowsUpdated(index, index);
        }
    }
    
    /**
     * 移除工具命令
     */
    public void removeToolCommand(int index) {
        if (index >= 0 && index < toolCommands.size()) {
            toolCommands.remove(index);
            fireTableRowsDeleted(index, index);
        }
    }
    
    /**
     * 获取指定索引的工具命令
     */
    public HttpToolCommand getToolCommandAt(int index) {
        if (index >= 0 && index < toolCommands.size()) {
            return toolCommands.get(index);
        }
        return null;
    }
    
    /**
     * 获取所有工具命令的副本
     */
    public List<HttpToolCommand> getToolCommands() {
        return new ArrayList<>(toolCommands);
    }
    
    /**
     * 清空所有数据
     */
    public void clear() {
        if (!toolCommands.isEmpty()) {
            int size = toolCommands.size();
            toolCommands.clear();
            fireTableRowsDeleted(0, size - 1);
        }
    }
    
    /**
     * 获取数据行数
     */
    public int getDataSize() {
        return toolCommands.size();
    }
    
    /**
     * 检查是否为空
     */
    public boolean isEmpty() {
        return toolCommands.isEmpty();
    }
    
    /**
     * 查找工具命令索引
     */
    public int findToolCommandIndex(HttpToolCommand targetCommand) {
        if (targetCommand == null) {
            return -1;
        }
        
        for (int i = 0; i < toolCommands.size(); i++) {
            HttpToolCommand command = toolCommands.get(i);
            if (command != null && command.equals(targetCommand)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 获取收藏的工具命令
     */
    public List<HttpToolCommand> getFavoriteToolCommands() {
        List<HttpToolCommand> favorites = new ArrayList<>();
        for (HttpToolCommand command : toolCommands) {
            if (command != null && command.isFavor()) {
                favorites.add(command);
            }
        }
        return favorites;
    }
    
    /**
     * 按分类过滤工具命令
     */
    public List<HttpToolCommand> getToolCommandsByCategory(String category) {
        List<HttpToolCommand> filtered = new ArrayList<>();
        if (category == null) {
            return filtered;
        }
        
        for (HttpToolCommand command : toolCommands) {
            if (command != null && category.equals(command.getCategory())) {
                filtered.add(command);
            }
        }
        return filtered;
    }
} 