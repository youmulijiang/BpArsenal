package controller;

import burp.api.montoya.MontoyaApi;
import model.HttpTool;
import model.HttpToolCommand;
import util.I18nManager;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * HTTP工具面板控制器
 * 处理工具面板的业务逻辑和数据操作
 */
public class ToolPanelController {
    
    private static ToolPanelController instance;
    private ToolPanelView view;
    private List<ToolPanelListener> listeners = new ArrayList<>();
    
    private ToolPanelController() {}
    
    public static ToolPanelController getInstance() {
        if (instance == null) {
            instance = new ToolPanelController();
        }
        return instance;
    }
    
    /**
     * 设置关联的视图
     */
    public void setView(ToolPanelView view) {
        this.view = view;
    }
    
    /**
     * 添加监听器
     */
    public void addListener(ToolPanelListener listener) {
        listeners.add(listener);
    }
    
    /**
     * 移除监听器
     */
    public void removeListener(ToolPanelListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * 加载数据
     */
    public void loadData() {
        try {
            List<HttpToolCommand> toolCommands = ToolController.getInstance().getAllToolCommands();
            notifyDataLoaded(toolCommands);
            notifyStatusUpdate(I18nManager.getInstance().getText("status.ready"));
        } catch (Exception e) {
            String errorMsg = "加载失败: " + e.getMessage();
            notifyStatusUpdate(errorMsg);
            logError("加载工具数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加新工具
     */
    public void addNewTool() {
        // 通知视图显示添加对话框
        notifyShowAddDialog();
    }
    
    /**
     * 处理添加工具结果
     */
    public void handleToolAdded(HttpTool newTool, String category) {
        if (ToolController.getInstance().addTool(newTool, category)) {
            loadData(); // 重新加载数据
            String statusMsg = "已添加工具: " + newTool.getToolName() + " (分类: " + category + ")";
            notifyStatusUpdate(statusMsg);
        } else {
            notifyStatusUpdate("添加工具失败");
        }
    }
    
    /**
     * 编辑选中工具
     */
    public void editSelectedTool(int selectedRow, List<HttpToolCommand> currentData) {
        if (selectedRow == -1) {
            notifyShowMessage("请先选择要编辑的工具", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        HttpToolCommand toolCommand = currentData.get(selectedRow);
        HttpTool tool = toolCommand.getParentTool();
        
        // 通知视图显示编辑对话框，传递具体的命令对象
        notifyShowEditDialogWithCommand(tool, toolCommand);
    }
    
    /**
     * 处理编辑工具结果
     */
    public void handleToolEdited(HttpTool originalTool, HttpTool updatedTool, String newCategory) {
        if (ToolController.getInstance().updateTool(originalTool, updatedTool, newCategory)) {
            loadData(); // 重新加载数据
            String statusMsg = "已更新工具: " + updatedTool.getToolName() + " (分类: " + newCategory + ")";
            notifyStatusUpdate(statusMsg);
        } else {
            notifyStatusUpdate("更新工具失败");
        }
    }
    
    /**
     * 处理包含命令详细信息的工具编辑结果
     */
    public void handleToolEditedWithCommand(HttpTool originalTool, HttpTool updatedTool, String newCategory, 
                                          HttpToolCommand originalCommand, String note, String workDir) {
        // 使用新的updateToolCommand方法来处理命令级别的更新
        if (ToolController.getInstance().updateToolCommand(
                originalCommand, 
                updatedTool.getCommand(), 
                note, 
                workDir, 
                updatedTool.isFavor(), 
                newCategory)) {
            loadData(); // 重新加载数据
            String statusMsg = "已更新工具: " + updatedTool.getToolName() + 
                             " (分类: " + newCategory + 
                             (note != null && !note.trim().isEmpty() ? ", 备注: " + note : "") +
                             (workDir != null && !workDir.trim().isEmpty() ? ", 工作目录: " + workDir : "") + ")";
            notifyStatusUpdate(statusMsg);
        } else {
            notifyStatusUpdate("更新工具失败");
        }
    }
    
    /**
     * 删除选中工具
     */
    public void deleteSelectedTool(int selectedRow, List<HttpToolCommand> currentData) {
        if (selectedRow == -1) {
            notifyShowMessage("请先选择要删除的工具", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        HttpToolCommand toolCommand = currentData.get(selectedRow);
        HttpTool tool = toolCommand.getParentTool();
        
        // 通知视图显示确认对话框
        notifyShowDeleteConfirmDialog(tool);
    }
    
    /**
     * 处理删除工具确认
     */
    public void handleToolDeleted(HttpTool tool) {
        if (ToolController.getInstance().removeTool(tool)) {
            loadData(); // 重新加载数据
            notifyStatusUpdate("已删除工具: " + tool.getToolName());
        } else {
            notifyStatusUpdate("删除工具失败");
        }
    }
    
    /**
     * 切换收藏状态
     */
    public void toggleFavorite(int selectedRow, List<HttpToolCommand> currentData) {
        if (selectedRow == -1) {
            notifyShowMessage("请先选择工具", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        HttpToolCommand toolCommand = currentData.get(selectedRow);
        HttpTool tool = toolCommand.getParentTool();
        boolean newFavoriteState = !tool.isFavor();
        
        if (ToolController.getInstance().updateToolFavorite(tool, newFavoriteState)) {
            tool.setFavor(newFavoriteState);
            toolCommand.setFavor(newFavoriteState);
            
            String status = tool.isFavor() ? "已收藏" : "已取消收藏";
            notifyStatusUpdate(status + ": " + tool.getToolName());
            notifyDataChanged();
        } else {
            notifyStatusUpdate("更新收藏状态失败");
        }
    }
    
    /**
     * 过滤表格数据
     */
    public void filterTable(String searchText, int searchColumnIndex, String selectedCategory) {
        List<HttpToolCommand> allToolCommands = ToolController.getInstance().getAllToolCommands();
        List<HttpToolCommand> filteredCommands = new ArrayList<>();
        
        for (HttpToolCommand toolCommand : allToolCommands) {
            boolean matchesSearch = checkSearchMatch(toolCommand, searchText, searchColumnIndex);
            boolean matchesCategory = checkCategoryMatch(toolCommand, selectedCategory);
            
            if (matchesSearch && matchesCategory) {
                filteredCommands.add(toolCommand);
            }
        }
        
        // 通知视图更新过滤后的数据
        notifyDataFiltered(filteredCommands, allToolCommands.size());
        
        // 更新状态信息
        updateFilterStatus(searchText, searchColumnIndex, selectedCategory, 
                         filteredCommands.size(), allToolCommands.size());
    }
    
    /**
     * 检查搜索匹配
     */
    private boolean checkSearchMatch(HttpToolCommand toolCommand, String searchText, int searchColumnIndex) {
        if (searchText.isEmpty()) {
            return true;
        }
        
        String lowerSearchText = searchText.toLowerCase();
        switch (searchColumnIndex) {
            case 0: // 全部
                return toolCommand.getDisplayName().toLowerCase().contains(lowerSearchText) ||
                       toolCommand.getCommand().toLowerCase().contains(lowerSearchText) ||
                       (toolCommand.getNote() != null && toolCommand.getNote().toLowerCase().contains(lowerSearchText)) ||
                       (toolCommand.getWorkDir() != null && toolCommand.getWorkDir().toLowerCase().contains(lowerSearchText)) ||
                       toolCommand.getCategory().toLowerCase().contains(lowerSearchText);
            case 1: // 工具名称
                return toolCommand.getDisplayName().toLowerCase().contains(lowerSearchText);
            case 2: // 命令
                return toolCommand.getCommand().toLowerCase().contains(lowerSearchText);
            case 3: // 备注
                return toolCommand.getNote() != null && toolCommand.getNote().toLowerCase().contains(lowerSearchText);
            case 4: // 工作目录
                return toolCommand.getWorkDir() != null && toolCommand.getWorkDir().toLowerCase().contains(lowerSearchText);
            case 5: // 分类
                return toolCommand.getCategory().toLowerCase().contains(lowerSearchText);
            default:
                return true;
        }
    }
    
    /**
     * 检查分类匹配
     */
    private boolean checkCategoryMatch(HttpToolCommand toolCommand, String selectedCategory) {
        I18nManager i18n = I18nManager.getInstance();
        if (selectedCategory.equals(i18n.getText("filter.all"))) {
            return true;
        } else {
            return toolCommand.getCategory().equals(selectedCategory);
        }
    }
    
    /**
     * 更新过滤状态信息
     */
    private void updateFilterStatus(String searchText, int searchColumnIndex, String selectedCategory,
                                  int filteredSize, int totalSize) {
        String statusMsg = String.format("显示 %d/%d 条记录", filteredSize, totalSize);
        
        if (!searchText.isEmpty()) {
            I18nManager i18n = I18nManager.getInstance();
            String[] searchScopes = {
                i18n.getText("filter.all"),
                i18n.getText("tools.tool.name"),
                i18n.getText("tools.command"),
                i18n.getText("tools.note"),
                i18n.getText("tools.work.dir"),
                i18n.getText("label.category")
            };
            String searchScope = searchColumnIndex < searchScopes.length ? 
                searchScopes[searchColumnIndex] : "全部";
            statusMsg += " | 搜索: " + searchText + " (范围: " + searchScope + ")";
        }
        
        I18nManager i18n = I18nManager.getInstance();
        if (!selectedCategory.equals(i18n.getText("filter.all"))) {
            statusMsg += " | 分类: " + selectedCategory;
        }
        
        notifyStatusUpdate(statusMsg);
    }
    
    /**
     * 获取所有分类
     */
    public List<String> getAllCategories() {
        try {
            return ToolController.getInstance().getAllHttpToolCategories();
        } catch (Exception e) {
            logError("加载分类选项失败: " + e.getMessage());
            // 返回默认分类
            I18nManager i18n = I18nManager.getInstance();
            List<String> defaultCategories = new ArrayList<>();
            defaultCategories.add(i18n.getText("filter.all"));
            defaultCategories.add(i18n.getText("tools.category.sql.injection"));
            defaultCategories.add(i18n.getText("tools.category.xss"));
            defaultCategories.add(i18n.getText("tools.category.directory.scan"));
            defaultCategories.add(i18n.getText("tools.category.vulnerability.scan"));
            defaultCategories.add(i18n.getText("tools.category.brute.force"));
            return defaultCategories;
        }
    }
    
    /**
     * 复制命令到剪贴板
     */
    public void copyCommandToClipboard(int selectedRow, List<HttpToolCommand> currentData) {
        if (selectedRow >= 0 && selectedRow < currentData.size()) {
            HttpToolCommand cmd = currentData.get(selectedRow);
            java.awt.datatransfer.StringSelection stringSelection = 
                new java.awt.datatransfer.StringSelection(cmd.getCommand());
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(stringSelection, null);
            notifyStatusUpdate("已复制命令到剪贴板: " + cmd.getDisplayName());
        }
    }
    
    /**
     * 执行工具命令
     */
    public void executeToolCommand(int selectedRow, List<HttpToolCommand> currentData) {
        if (selectedRow >= 0 && selectedRow < currentData.size()) {
            HttpToolCommand toolCommand = currentData.get(selectedRow);
            // TODO: 实现命令执行功能
            notifyStatusUpdate("执行命令: " + toolCommand.getDisplayName());
        }
    }
    
    // 通知方法
    private void notifyDataLoaded(List<HttpToolCommand> data) {
        for (ToolPanelListener listener : listeners) {
            listener.onDataLoaded(data);
        }
    }
    
    private void notifyDataFiltered(List<HttpToolCommand> filteredData, int totalSize) {
        for (ToolPanelListener listener : listeners) {
            listener.onDataFiltered(filteredData, totalSize);
        }
    }
    
    private void notifyDataChanged() {
        for (ToolPanelListener listener : listeners) {
            listener.onDataChanged();
        }
    }
    
    private void notifyStatusUpdate(String message) {
        for (ToolPanelListener listener : listeners) {
            listener.onStatusUpdate(message);
        }
    }
    
    private void notifyShowAddDialog() {
        for (ToolPanelListener listener : listeners) {
            listener.onShowAddDialog();
        }
    }
    
    private void notifyShowEditDialog(HttpTool tool) {
        for (ToolPanelListener listener : listeners) {
            listener.onShowEditDialog(tool);
        }
    }
    
    private void notifyShowEditDialogWithCommand(HttpTool tool, HttpToolCommand toolCommand) {
        for (ToolPanelListener listener : listeners) {
            listener.onShowEditDialogWithCommand(tool, toolCommand);
        }
    }
    
    private void notifyShowDeleteConfirmDialog(HttpTool tool) {
        for (ToolPanelListener listener : listeners) {
            listener.onShowDeleteConfirmDialog(tool);
        }
    }
    
    private void notifyShowMessage(String message, String title, int messageType) {
        for (ToolPanelListener listener : listeners) {
            listener.onShowMessage(message, title, messageType);
        }
    }
    
    /**
     * 记录错误日志
     */
    private void logError(String message) {
        JOptionPane.showMessageDialog(null, message, "<UNK>", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * 视图接口
     */
    public interface ToolPanelView {
        void updateData(List<HttpToolCommand> data);
        void updateStatus(String message);
        void showAddDialog();
        void showEditDialog(HttpTool tool);
        void showEditDialogWithCommand(HttpTool tool, HttpToolCommand toolCommand);
        void showDeleteConfirmDialog(HttpTool tool);
        void showMessage(String message, String title, int messageType);
    }
    
    /**
     * 事件监听器接口
     */
    public interface ToolPanelListener {
        void onDataLoaded(List<HttpToolCommand> data);
        void onDataFiltered(List<HttpToolCommand> filteredData, int totalSize);
        void onDataChanged();
        void onStatusUpdate(String message);
        void onShowAddDialog();
        void onShowEditDialog(HttpTool tool);
        void onShowEditDialogWithCommand(HttpTool tool, HttpToolCommand toolCommand);
        void onShowDeleteConfirmDialog(HttpTool tool);
        void onShowMessage(String message, String title, int messageType);
    }
} 