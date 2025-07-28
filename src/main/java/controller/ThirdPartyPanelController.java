package controller;

import model.ThirdPartyTool;
import model.ThirdPartyTableModel;
import executor.ToolExecutor;
import util.I18nManager;
import view.menu.ArsenalMenuProvider;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * 第三方工具面板控制器 (Controller层)
 * 实现单例模式，负责处理第三方工具面板的业务逻辑
 */
public class ThirdPartyPanelController {
    
    // 单例实例
    private static ThirdPartyPanelController instance;
    
    // 表格模型
    private ThirdPartyTableModel tableModel;
    
    // 视图接口
    private ThirdPartyPanelView view;
    
    // 监听器接口
    private ThirdPartyPanelListener listener;
    
    /**
     * 私有构造函数
     */
    private ThirdPartyPanelController() {
        this.tableModel = new ThirdPartyTableModel();
    }
    
    /**
     * 获取单例实例
     * @return 控制器实例
     */
    public static ThirdPartyPanelController getInstance() {
        if (instance == null) {
            instance = new ThirdPartyPanelController();
        }
        return instance;
    }
    
    /**
     * 设置视图接口
     * @param view 视图接口实现
     */
    public void setView(ThirdPartyPanelView view) {
        this.view = view;
    }
    
    /**
     * 设置监听器
     * @param listener 监听器实现
     */
    public void setListener(ThirdPartyPanelListener listener) {
        this.listener = listener;
    }
    
    /**
     * 获取表格模型
     * @return 表格模型
     */
    public ThirdPartyTableModel getTableModel() {
        return tableModel;
    }
    
    /**
     * 加载数据
     */
    public void loadData() {
        try {
            List<ThirdPartyTool> tools = ToolController.getInstance().getAllThirdPartyTools();
            tableModel.setTools(tools);
            
            if (listener != null) {
                listener.onDataLoaded(tools.size());
            }
            
            updateStatus("已加载 " + tools.size() + " 个第三方工具");
            
        } catch (Exception e) {
            String errorMsg = "加载失败: " + e.getMessage();
            updateStatus(errorMsg);
            logError("加载第三方工具数据失败: " + e.getMessage());
            
            if (listener != null) {
                listener.onError("数据加载", errorMsg);
            }
        }
    }
    
    /**
     * 添加新工具
     * @param parentComponent 父组件
     */
    public void addNewTool(Component parentComponent) {
        try {
            if (view != null) {
                ThirdPartyTool newTool = view.showAddToolDialog(parentComponent);
                if (newTool != null) {
                    String category = view.getSelectedCategory();
                    
                    if (ToolController.getInstance().addThirdPartyTool(newTool, category)) {
                        loadData(); // 重新加载数据
                        updateStatus("已添加工具: " + newTool.getToolName() + " (分类: " + category + ")");
                        
                        // 如果新工具是收藏状态，更新菜单
                        if (newTool.isFavor()) {
                            updateToolsMenu();
                        }
                        
                        if (listener != null) {
                            listener.onToolAdded(newTool);
                        }
                    } else {
                        updateStatus("添加工具失败");
                        if (listener != null) {
                            listener.onError("添加工具", "添加工具到数据库失败");
                        }
                    }
                }
            }
        } catch (Exception e) {
            String errorMsg = "添加工具失败: " + e.getMessage();
            updateStatus(errorMsg);
            logError(errorMsg);
            if (listener != null) {
                listener.onError("添加工具", errorMsg);
            }
        }
    }
    
    /**
     * 编辑选中工具
     * @param selectedRow 选中行索引
     * @param parentComponent 父组件
     */
    public void editSelectedTool(int selectedRow, Component parentComponent) {
        if (selectedRow == -1) {
            showMessage(parentComponent, "请先选择要编辑的工具", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            ThirdPartyTool tool = tableModel.getToolAt(selectedRow);
            if (tool == null) {
                showMessage(parentComponent, "选中的工具不存在", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            boolean oldFavoriteState = tool.isFavor();
            
            if (view != null) {
                ThirdPartyTool updatedTool = view.showEditToolDialog(parentComponent, tool);
                if (updatedTool != null) {
                    String newCategory = view.getSelectedCategory();
                    
                    if (ToolController.getInstance().updateThirdPartyTool(tool, updatedTool, newCategory)) {
                        loadData(); // 重新加载数据
                        updateStatus("已更新工具: " + updatedTool.getToolName() + " (分类: " + newCategory + ")");
                        
                        // 如果收藏状态发生变化，更新菜单
                        if (oldFavoriteState != updatedTool.isFavor()) {
                            updateToolsMenu();
                        }
                        
                        if (listener != null) {
                            listener.onToolUpdated(updatedTool);
                        }
                    } else {
                        updateStatus("更新工具失败");
                        if (listener != null) {
                            listener.onError("更新工具", "更新工具到数据库失败");
                        }
                    }
                }
            }
        } catch (Exception e) {
            String errorMsg = "编辑工具失败: " + e.getMessage();
            updateStatus(errorMsg);
            logError(errorMsg);
            if (listener != null) {
                listener.onError("编辑工具", errorMsg);
            }
        }
    }
    
    /**
     * 删除选中工具
     * @param selectedRow 选中行索引
     * @param parentComponent 父组件
     */
    public void deleteSelectedTool(int selectedRow, Component parentComponent) {
        if (selectedRow == -1) {
            showMessage(parentComponent, "请先选择要删除的工具", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            ThirdPartyTool tool = tableModel.getToolAt(selectedRow);
            if (tool == null) {
                showMessage(parentComponent, "选中的工具不存在", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            boolean wasFavorite = tool.isFavor();
            
            int result = JOptionPane.showConfirmDialog(parentComponent,
                "确定要删除工具 \"" + tool.getToolName() + "\" 吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
                
            if (result == JOptionPane.YES_OPTION) {
                if (ToolController.getInstance().removeThirdPartyTool(tool)) {
                    loadData(); // 重新加载数据
                    updateStatus("已删除工具: " + tool.getToolName());
                    
                    // 如果删除的是收藏工具，更新菜单
                    if (wasFavorite) {
                        updateToolsMenu();
                    }
                    
                    if (listener != null) {
                        listener.onToolDeleted(tool);
                    }
                } else {
                    updateStatus("删除工具失败");
                    if (listener != null) {
                        listener.onError("删除工具", "从数据库删除工具失败");
                    }
                }
            }
        } catch (Exception e) {
            String errorMsg = "删除工具失败: " + e.getMessage();
            updateStatus(errorMsg);
            logError(errorMsg);
            if (listener != null) {
                listener.onError("删除工具", errorMsg);
            }
        }
    }
    
    /**
     * 切换收藏状态
     * @param selectedRow 选中行索引
     * @param parentComponent 父组件
     */
    public void toggleFavorite(int selectedRow, Component parentComponent) {
        if (selectedRow == -1) {
            showMessage(parentComponent, "请先选择工具", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            ThirdPartyTool tool = tableModel.getToolAt(selectedRow);
            if (tool == null) {
                showMessage(parentComponent, "选中的工具不存在", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            boolean newFavoriteState = !tool.isFavor();
            
            if (ToolController.getInstance().updateThirdPartyToolFavorite(tool, newFavoriteState)) {
                tool.setFavor(newFavoriteState);
                tableModel.fireTableDataChanged();
                
                String status = tool.isFavor() ? "已收藏" : "已取消收藏";
                updateStatus(status + ": " + tool.getToolName());
                
                // 动态更新菜单栏
                updateToolsMenu();
                
                if (listener != null) {
                    listener.onFavoriteToggled(tool, newFavoriteState);
                }
            } else {
                updateStatus("更新收藏状态失败");
                if (listener != null) {
                    listener.onError("更新收藏", "更新收藏状态到数据库失败");
                }
            }
        } catch (Exception e) {
            String errorMsg = "切换收藏状态失败: " + e.getMessage();
            updateStatus(errorMsg);
            logError(errorMsg);
            if (listener != null) {
                listener.onError("切换收藏", errorMsg);
            }
        }
    }
    
    /**
     * 启动选中工具
     * @param selectedRow 选中行索引
     * @param parentComponent 父组件
     */
    public void launchSelectedTool(int selectedRow, Component parentComponent) {
        if (selectedRow == -1) {
            showMessage(parentComponent, "请先选择要启动的工具", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            ThirdPartyTool tool = tableModel.getToolAt(selectedRow);
            if (tool == null) {
                showMessage(parentComponent, "选中的工具不存在", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            ToolExecutor.getInstance().executeThirdPartyTool(tool);
            updateStatus("正在启动: " + tool.getToolName() + " - " + tool.getStartCommand());
            
            if (listener != null) {
                listener.onToolLaunched(tool);
            }
            
        } catch (Exception e) {
            String errorMsg = "启动失败: " + e.getMessage();
            updateStatus(errorMsg);
            logError("启动第三方工具失败: " + e.getMessage());
            
            showMessage(parentComponent,
                "启动工具失败！\n错误: " + e.getMessage(),
                "启动失败",
                JOptionPane.ERROR_MESSAGE);
                
            if (listener != null) {
                listener.onError("启动工具", errorMsg);
            }
        }
    }
    
    /**
     * 过滤表格数据
     * @param searchText 搜索文本
     * @param searchColumnIndex 搜索列索引
     * @param selectedCategory 选中分类
     */
    public void filterTable(String searchText, int searchColumnIndex, String selectedCategory) {
        try {
            // 获取原始数据
            List<ThirdPartyTool> allTools = ToolController.getInstance().getAllThirdPartyTools();
            List<ThirdPartyTool> filteredTools = new ArrayList<>();
            
            for (ThirdPartyTool tool : allTools) {
                boolean matchesSearch = false;
                boolean matchesCategory = false;
                
                // 检查搜索条件
                if (searchText == null || searchText.trim().isEmpty()) {
                    matchesSearch = true;
                } else {
                    String searchLower = searchText.toLowerCase().trim();
                    String toolCategory = ToolController.getInstance().getThirdPartyToolCategory(tool.getToolName());
                    
                    switch (searchColumnIndex) {
                        case 0: // 全部
                            matchesSearch = tool.getToolName().toLowerCase().contains(searchLower) ||
                                           tool.getStartCommand().toLowerCase().contains(searchLower) ||
                                           toolCategory.toLowerCase().contains(searchLower);
                            break;
                        case 1: // 工具名称
                            matchesSearch = tool.getToolName().toLowerCase().contains(searchLower);
                            break;
                        case 2: // 启动命令
                            matchesSearch = tool.getStartCommand().toLowerCase().contains(searchLower);
                            break;
                        case 3: // 分类
                            matchesSearch = toolCategory.toLowerCase().contains(searchLower);
                            break;
                        default:
                            matchesSearch = true;
                            break;
                    }
                }
                
                // 检查分类过滤条件
                I18nManager i18n = I18nManager.getInstance();
                if (selectedCategory == null || selectedCategory.equals(i18n.getText("filter.all"))) {
                    matchesCategory = true;
                } else {
                    String toolCategory = ToolController.getInstance().getThirdPartyToolCategory(tool.getToolName());
                    matchesCategory = toolCategory.equals(selectedCategory);
                }
                
                // 同时满足搜索和分类条件的记录才会被显示
                if (matchesSearch && matchesCategory) {
                    filteredTools.add(tool);
                }
            }
            
            // 更新表格数据
            tableModel.setTools(filteredTools);
            
            // 更新状态信息
            updateFilterStatus(filteredTools.size(), allTools.size(), searchText, searchColumnIndex, selectedCategory);
            
            if (listener != null) {
                listener.onDataFiltered(filteredTools.size(), allTools.size());
            }
            
        } catch (Exception e) {
            String errorMsg = "过滤数据失败: " + e.getMessage();
            updateStatus(errorMsg);
            logError(errorMsg);
            if (listener != null) {
                listener.onError("数据过滤", errorMsg);
            }
        }
    }
    
    /**
     * 复制启动命令到剪贴板
     * @param selectedRow 选中行索引
     */
    public void copyCommandToClipboard(int selectedRow) {
        try {
            ThirdPartyTool tool = tableModel.getToolAt(selectedRow);
            if (tool != null) {
                java.awt.datatransfer.StringSelection stringSelection = 
                    new java.awt.datatransfer.StringSelection(tool.getStartCommand());
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(stringSelection, null);
                updateStatus("已复制启动命令到剪贴板: " + tool.getToolName());
                
                if (listener != null) {
                    listener.onCommandCopied(tool);
                }
            }
        } catch (Exception e) {
            String errorMsg = "复制命令失败: " + e.getMessage();
            updateStatus(errorMsg);
            logError(errorMsg);
            if (listener != null) {
                listener.onError("复制命令", errorMsg);
            }
        }
    }
    
    /**
     * 切换自启动状态
     * @param selectedRow 选中行索引
     * @param newAutoStartState 新的自启动状态
     */
    public void toggleAutoStart(int selectedRow, boolean newAutoStartState) {
        try {
            ThirdPartyTool tool = tableModel.getToolAt(selectedRow);
            if (tool != null) {
                tool.setAutoStart(newAutoStartState);
                tableModel.fireTableDataChanged();
                
                String status = tool.isAutoStart() ? "已启用自启动" : "已禁用自启动";
                updateStatus(status + ": " + tool.getToolName());
                
                if (listener != null) {
                    listener.onAutoStartToggled(tool, newAutoStartState);
                }
            }
        } catch (Exception e) {
            String errorMsg = "切换自启动状态失败: " + e.getMessage();
            updateStatus(errorMsg);
            logError(errorMsg);
            if (listener != null) {
                listener.onError("切换自启动", errorMsg);
            }
        }
    }
    
    /**
     * 更新状态信息
     * @param message 状态消息
     */
    private void updateStatus(String message) {
        if (view != null) {
            view.updateStatus(message);
        }
    }
    
    /**
     * 更新过滤状态信息
     */
    private void updateFilterStatus(int filteredSize, int totalSize, String searchText, 
                                  int searchColumnIndex, String selectedCategory) {
        String statusMsg = String.format("显示 %d/%d 个工具", filteredSize, totalSize);
        
        if (searchText != null && !searchText.trim().isEmpty()) {
            String[] searchScopes = {"全部", "工具名称", "启动命令", "分类"};
            String searchScope = (searchColumnIndex >= 0 && searchColumnIndex < searchScopes.length) ? 
                searchScopes[searchColumnIndex] : "全部";
            statusMsg += " | 搜索: " + searchText + " (范围: " + searchScope + ")";
        }
        
        I18nManager i18n = I18nManager.getInstance();
        if (selectedCategory != null && !selectedCategory.equals(i18n.getText("filter.all"))) {
            statusMsg += " | 分类: " + selectedCategory;
        }
        
        updateStatus(statusMsg);
    }
    
    /**
     * 显示消息
     */
    private void showMessage(Component parent, String message, String title, int messageType) {
        if (view != null) {
            view.showMessage(parent, message, title, messageType);
        } else {
            JOptionPane.showMessageDialog(parent, message, title, messageType);
        }
    }
    
    /**
     * 更新工具菜单
     */
    private void updateToolsMenu() {
        try {
            ArsenalMenuProvider.updateToolsMenu();
        } catch (Exception e) {
            logError("更新菜单失败: " + e.getMessage());
        }
    }
    
    /**
     * 记录错误日志
     * @param message 错误消息
     */
    private void logError(String message) {
        System.err.println("ThirdPartyPanelController: " + message);
    }
    
    /**
     * 视图接口
     * 定义控制器与视图的交互方法
     */
    public interface ThirdPartyPanelView {
        /**
         * 显示添加工具对话框
         * @param parentComponent 父组件
         * @return 新工具对象，取消返回null
         */
        ThirdPartyTool showAddToolDialog(Component parentComponent);
        
        /**
         * 显示编辑工具对话框
         * @param parentComponent 父组件
         * @param tool 要编辑的工具
         * @return 编辑后的工具对象，取消返回null
         */
        ThirdPartyTool showEditToolDialog(Component parentComponent, ThirdPartyTool tool);
        
        /**
         * 获取选中的分类
         * @return 分类名称
         */
        String getSelectedCategory();
        
        /**
         * 更新状态信息
         * @param message 状态消息
         */
        void updateStatus(String message);
        
        /**
         * 显示消息
         * @param parent 父组件
         * @param message 消息内容
         * @param title 标题
         * @param messageType 消息类型
         */
        void showMessage(Component parent, String message, String title, int messageType);
    }
    
    /**
     * 监听器接口
     * 定义控制器事件的回调方法
     */
    public interface ThirdPartyPanelListener {
        /**
         * 数据加载完成
         * @param count 加载的数据条数
         */
        void onDataLoaded(int count);
        
        /**
         * 数据过滤完成
         * @param filteredCount 过滤后的数据条数
         * @param totalCount 总数据条数
         */
        void onDataFiltered(int filteredCount, int totalCount);
        
        /**
         * 工具添加完成
         * @param tool 添加的工具
         */
        void onToolAdded(ThirdPartyTool tool);
        
        /**
         * 工具更新完成
         * @param tool 更新的工具
         */
        void onToolUpdated(ThirdPartyTool tool);
        
        /**
         * 工具删除完成
         * @param tool 删除的工具
         */
        void onToolDeleted(ThirdPartyTool tool);
        
        /**
         * 收藏状态切换完成
         * @param tool 工具对象
         * @param newState 新的收藏状态
         */
        void onFavoriteToggled(ThirdPartyTool tool, boolean newState);
        
        /**
         * 自启动状态切换完成
         * @param tool 工具对象
         * @param newState 新的自启动状态
         */
        void onAutoStartToggled(ThirdPartyTool tool, boolean newState);
        
        /**
         * 工具启动完成
         * @param tool 启动的工具
         */
        void onToolLaunched(ThirdPartyTool tool);
        
        /**
         * 命令复制完成
         * @param tool 复制命令的工具
         */
        void onCommandCopied(ThirdPartyTool tool);
        
        /**
         * 发生错误
         * @param operation 操作名称
         * @param errorMessage 错误消息
         */
        void onError(String operation, String errorMessage);
    }
} 