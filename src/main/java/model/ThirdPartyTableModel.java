package model;

import controller.ToolController;
import util.I18nManager;
import view.menu.ArsenalMenuProvider;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * 第三方工具表格模型 (Model层)
 * 负责管理第三方工具的表格数据展示
 */
public class ThirdPartyTableModel extends AbstractTableModel {
    
    // 表格列名
    private String[] columnNames;
    
    // 第三方工具数据列表
    private List<ThirdPartyTool> tools = new ArrayList<>();
    
    /**
     * 构造函数
     */
    public ThirdPartyTableModel() {
        updateColumnNames();
    }
    
    /**
     * 更新列名（支持国际化）
     */
    public void updateColumnNames() {
        I18nManager i18n = I18nManager.getInstance();
        columnNames = new String[]{
            i18n.getText("thirdparty.tool.name"),      // 0: 工具名称
            i18n.getText("thirdparty.start.command"),  // 1: 启动命令
            i18n.getText("column.favorite"),           // 2: 收藏
            i18n.getText("column.note"),               // 3: 备注
            i18n.getText("column.work.dir"),           // 4: 工作目录
            i18n.getText("label.category"),            // 5: 分类
            i18n.getText("thirdparty.auto.start")      // 6: 自启动
        };
    }
    
    @Override
    public int getRowCount() {
        return tools.size();
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
        ThirdPartyTool tool = tools.get(rowIndex);
        switch (columnIndex) {
            case 0: return tool.getToolName();
            case 1: return tool.getStartCommand();
            case 2: return tool.isFavor();
            case 3: return tool.getNote() != null ? tool.getNote() : "";
            case 4: return tool.getWorkDir() != null ? tool.getWorkDir() : "";
            case 5: return ToolController.getInstance().getThirdPartyToolCategory(tool.getToolName());
            case 6: return tool.isAutoStart();
            default: return null;
        }
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 2 || columnIndex == 6) return Boolean.class;  // 收藏列和自启动列
        return String.class;
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 2 || columnIndex == 6; // 收藏和自启动列可编辑
    }
    
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        ThirdPartyTool tool = tools.get(rowIndex);
        boolean favoriteChanged = false;
        
        if (columnIndex == 2) {  // 收藏列
            boolean oldFavoriteState = tool.isFavor();
            tool.setFavor((Boolean) value);
            favoriteChanged = (oldFavoriteState != tool.isFavor());
            
            // 更新数据库中的收藏状态
            if (favoriteChanged) {
                ToolController.getInstance().updateThirdPartyToolFavorite(tool, tool.isFavor());
            }
        } else if (columnIndex == 6) {  // 自启动列
            tool.setAutoStart((Boolean) value);
            // 更新数据库中的自启动状态
            ToolController.getInstance().updateThirdPartyToolAutoStart(tool, tool.isAutoStart());
        }
        
        fireTableCellUpdated(rowIndex, columnIndex);
        
        // 如果收藏状态发生变化，更新菜单
        if (favoriteChanged) {
            try {
                ArsenalMenuProvider.updateToolsMenu();
            } catch (Exception e) {
            }
        }
    }
    
    /**
     * 设置工具列表数据
     * @param tools 工具列表
     */
    public void setTools(List<ThirdPartyTool> tools) {
        this.tools = new ArrayList<>(tools);
        fireTableDataChanged();
    }
    
    /**
     * 添加工具
     * @param tool 工具对象
     */
    public void addTool(ThirdPartyTool tool) {
        tools.add(tool);
        int row = tools.size() - 1;
        fireTableRowsInserted(row, row);
    }
    
    /**
     * 更新工具
     * @param index 工具索引
     * @param tool 更新后的工具对象
     */
    public void updateTool(int index, ThirdPartyTool tool) {
        if (index >= 0 && index < tools.size()) {
            tools.set(index, tool);
            fireTableRowsUpdated(index, index);
        }
    }
    
    /**
     * 移除工具
     * @param index 工具索引
     */
    public void removeTool(int index) {
        if (index >= 0 && index < tools.size()) {
            tools.remove(index);
            fireTableRowsDeleted(index, index);
        }
    }
    
    /**
     * 获取指定索引的工具
     * @param index 工具索引
     * @return 工具对象
     */
    public ThirdPartyTool getToolAt(int index) {
        if (index >= 0 && index < tools.size()) {
            return tools.get(index);
        }
        return null;
    }
    
    /**
     * 获取所有工具列表
     * @return 工具列表副本
     */
    public List<ThirdPartyTool> getTools() {
        return new ArrayList<>(tools);
    }
    
    /**
     * 清空所有数据
     */
    public void clear() {
        int oldSize = tools.size();
        if (oldSize > 0) {
            tools.clear();
            fireTableRowsDeleted(0, oldSize - 1);
        }
    }
    
    /**
     * 获取数据大小
     * @return 数据条数
     */
    public int getDataSize() {
        return tools.size();
    }
    
    /**
     * 判断是否为空
     * @return 是否为空
     */
    public boolean isEmpty() {
        return tools.isEmpty();
    }
    
    /**
     * 查找工具索引
     * @param tool 工具对象
     * @return 索引位置，未找到返回-1
     */
    public int findToolIndex(ThirdPartyTool tool) {
        return tools.indexOf(tool);
    }
    
    /**
     * 根据工具名查找工具索引
     * @param toolName 工具名
     * @return 索引位置，未找到返回-1
     */
    public int findToolIndexByName(String toolName) {
        for (int i = 0; i < tools.size(); i++) {
            if (tools.get(i).getToolName().equals(toolName)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 获取收藏工具列表
     * @return 收藏工具列表
     */
    public List<ThirdPartyTool> getFavoriteTools() {
        List<ThirdPartyTool> favorites = new ArrayList<>();
        for (ThirdPartyTool tool : tools) {
            if (tool.isFavor()) {
                favorites.add(tool);
            }
        }
        return favorites;
    }
    
    /**
     * 获取自启动工具列表
     * @return 自启动工具列表
     */
    public List<ThirdPartyTool> getAutoStartTools() {
        List<ThirdPartyTool> autoStartTools = new ArrayList<>();
        for (ThirdPartyTool tool : tools) {
            if (tool.isAutoStart()) {
                autoStartTools.add(tool);
            }
        }
        return autoStartTools;
    }
    
    /**
     * 根据分类获取工具列表
     * @param category 分类名
     * @return 指定分类的工具列表
     */
    public List<ThirdPartyTool> getToolsByCategory(String category) {
        List<ThirdPartyTool> categoryTools = new ArrayList<>();
        for (ThirdPartyTool tool : tools) {
            String toolCategory = ToolController.getInstance().getThirdPartyToolCategory(tool.getToolName());
            if (toolCategory.equals(category)) {
                categoryTools.add(tool);
            }
        }
        return categoryTools;
    }
} 