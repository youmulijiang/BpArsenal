package controller;

import model.WebSite;
import model.WebSiteTableModel;
import util.I18nManager;
import view.menu.ArsenalMenuProvider;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * 网站面板控制器 (Controller层)
 * 实现单例模式，负责处理网站面板的业务逻辑
 */
public class WebSitePanelController {
    
    // 单例实例
    private static WebSitePanelController instance;
    
    // 表格模型
    private WebSiteTableModel tableModel;
    
    // 视图接口
    private WebSitePanelView view;
    
    // 监听器接口
    private WebSitePanelListener listener;
    
    /**
     * 私有构造函数
     */
    private WebSitePanelController() {
        this.tableModel = new WebSiteTableModel();
    }
    
    /**
     * 获取单例实例
     * @return 控制器实例
     */
    public static WebSitePanelController getInstance() {
        if (instance == null) {
            instance = new WebSitePanelController();
        }
        return instance;
    }
    
    /**
     * 设置视图接口
     * @param view 视图接口实现
     */
    public void setView(WebSitePanelView view) {
        this.view = view;
    }
    
    /**
     * 设置监听器
     * @param listener 监听器实现
     */
    public void setListener(WebSitePanelListener listener) {
        this.listener = listener;
    }
    
    /**
     * 获取表格模型
     * @return 表格模型
     */
    public WebSiteTableModel getTableModel() {
        return tableModel;
    }
    
    /**
     * 加载数据
     */
    public void loadData() {
        try {
            List<WebSite> websites = ToolController.getInstance().getAllWebSites();
            tableModel.setWebSites(websites);
            
            if (listener != null) {
                listener.onDataLoaded(websites.size());
            }
            
            updateStatus("已加载 " + websites.size() + " 个网站");
            
        } catch (Exception e) {
            String errorMsg = "加载失败: " + e.getMessage();
            updateStatus(errorMsg);
            logError("加载网站数据失败: " + e.getMessage());
            
            if (listener != null) {
                listener.onError("数据加载", errorMsg);
            }
        }
    }
    
    /**
     * 添加新网站
     * @param parentComponent 父组件
     */
    public void addNewWebSite(Component parentComponent) {
        try {
            if (view != null) {
                WebSite newWebSite = view.showAddWebSiteDialog(parentComponent);
                if (newWebSite != null) {
                    String category = view.getSelectedCategory();
                    
                    if (ToolController.getInstance().addWebSite(newWebSite, category)) {
                        loadData(); // 重新加载数据
                        updateStatus("已添加网站: " + newWebSite.getDesc() + " (分类: " + category + ")");
                        
                        // 如果新网站是收藏状态，更新菜单
                        if (newWebSite.isFavor()) {
                            updateWebsiteMenu();
                        }
                        
                        if (listener != null) {
                            listener.onWebSiteAdded(newWebSite);
                        }
                    } else {
                        updateStatus("添加网站失败");
                        if (listener != null) {
                            listener.onError("添加网站", "添加网站到数据库失败");
                        }
                    }
                }
            }
        } catch (Exception e) {
            String errorMsg = "添加网站失败: " + e.getMessage();
            updateStatus(errorMsg);
            logError(errorMsg);
            if (listener != null) {
                listener.onError("添加网站", errorMsg);
            }
        }
    }
    
    /**
     * 编辑选中网站
     * @param selectedRow 选中行索引
     * @param parentComponent 父组件
     */
    public void editSelectedWebSite(int selectedRow, Component parentComponent) {
        if (selectedRow == -1) {
            showMessage(parentComponent, "请先选择要编辑的网站", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            WebSite website = tableModel.getWebSiteAt(selectedRow);
            if (website == null) {
                showMessage(parentComponent, "选中的网站不存在", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            boolean oldFavoriteState = website.isFavor();
            
            if (view != null) {
                WebSite updatedWebSite = view.showEditWebSiteDialog(parentComponent, website);
                if (updatedWebSite != null) {
                    String newCategory = view.getSelectedCategory();
                    
                    if (ToolController.getInstance().updateWebSite(website, updatedWebSite, newCategory)) {
                        loadData(); // 重新加载数据
                        updateStatus("已更新网站: " + updatedWebSite.getDesc() + " (分类: " + newCategory + ")");
                        
                        // 如果收藏状态发生变化，更新菜单
                        if (oldFavoriteState != updatedWebSite.isFavor()) {
                            updateWebsiteMenu();
                        }
                        
                        if (listener != null) {
                            listener.onWebSiteUpdated(updatedWebSite);
                        }
                    } else {
                        updateStatus("更新网站失败");
                        if (listener != null) {
                            listener.onError("更新网站", "更新网站到数据库失败");
                        }
                    }
                }
            }
        } catch (Exception e) {
            String errorMsg = "编辑网站失败: " + e.getMessage();
            updateStatus(errorMsg);
            logError(errorMsg);
            if (listener != null) {
                listener.onError("编辑网站", errorMsg);
            }
        }
    }
    
    /**
     * 删除选中网站
     * @param selectedRow 选中行索引
     * @param parentComponent 父组件
     */
    public void deleteSelectedWebSite(int selectedRow, Component parentComponent) {
        if (selectedRow == -1) {
            showMessage(parentComponent, "请先选择要删除的网站", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            WebSite website = tableModel.getWebSiteAt(selectedRow);
            if (website == null) {
                showMessage(parentComponent, "选中的网站不存在", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            boolean wasFavorite = website.isFavor();
            
            int result = JOptionPane.showConfirmDialog(parentComponent,
                "确定要删除网站 \"" + website.getDesc() + "\" 吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
                
            if (result == JOptionPane.YES_OPTION) {
                if (ToolController.getInstance().removeWebSite(website)) {
                    loadData(); // 重新加载数据
                    updateStatus("已删除网站: " + website.getDesc());
                    
                    // 如果删除的是收藏网站，更新菜单
                    if (wasFavorite) {
                        updateWebsiteMenu();
                    }
                    
                    if (listener != null) {
                        listener.onWebSiteDeleted(website);
                    }
                } else {
                    updateStatus("删除网站失败");
                    if (listener != null) {
                        listener.onError("删除网站", "从数据库删除网站失败");
                    }
                }
            }
        } catch (Exception e) {
            String errorMsg = "删除网站失败: " + e.getMessage();
            updateStatus(errorMsg);
            logError(errorMsg);
            if (listener != null) {
                listener.onError("删除网站", errorMsg);
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
            showMessage(parentComponent, "请先选择网站", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            WebSite website = tableModel.getWebSiteAt(selectedRow);
            if (website == null) {
                showMessage(parentComponent, "选中的网站不存在", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            boolean newFavoriteState = !website.isFavor();
            
            if (ToolController.getInstance().updateWebSiteFavorite(website, newFavoriteState)) {
                website.setFavor(newFavoriteState);
                tableModel.fireTableDataChanged();
                
                String status = website.isFavor() ? "已收藏" : "已取消收藏";
                updateStatus(status + ": " + website.getDesc());
                
                // 动态更新菜单栏
                updateWebsiteMenu();
                
                if (listener != null) {
                    listener.onFavoriteToggled(website, newFavoriteState);
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
     * 打开选中网站
     * @param selectedRow 选中行索引
     * @param parentComponent 父组件
     */
    public void openSelectedWebSite(int selectedRow, Component parentComponent) {
        if (selectedRow == -1) {
            showMessage(parentComponent, "请先选择要打开的网站", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            WebSite website = tableModel.getWebSiteAt(selectedRow);
            if (website == null) {
                showMessage(parentComponent, "选中的网站不存在", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            openWebSite(website, parentComponent);
            
        } catch (Exception e) {
            String errorMsg = "打开网站失败: " + e.getMessage();
            updateStatus(errorMsg);
            logError(errorMsg);
            if (listener != null) {
                listener.onError("打开网站", errorMsg);
            }
        }
    }
    
    /**
     * 打开网站
     * @param website 网站对象
     * @param parentComponent 父组件
     */
    public void openWebSite(WebSite website, Component parentComponent) {
        try {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(new URI(website.getUrl()));
                updateStatus("正在打开: " + website.getDesc() + " - " + website.getUrl());
                
                if (listener != null) {
                    listener.onWebSiteOpened(website);
                }
            } else {
                String errorMsg = "系统不支持打开浏览器";
                updateStatus(errorMsg);
                showMessage(parentComponent,
                    "系统不支持打开浏览器\n网站地址: " + website.getUrl(),
                    "无法打开网站",
                    JOptionPane.WARNING_MESSAGE);
                    
                if (listener != null) {
                    listener.onError("打开网站", errorMsg);
                }
            }
            
        } catch (Exception e) {
            String errorMsg = "打开网站失败: " + e.getMessage();
            updateStatus(errorMsg);
            logError("打开网站失败: " + e.getMessage());
            
            showMessage(parentComponent,
                "打开网站失败！\n错误: " + e.getMessage() + "\n网站地址: " + website.getUrl(),
                "打开失败",
                JOptionPane.ERROR_MESSAGE);
                
            if (listener != null) {
                listener.onError("打开网站", errorMsg);
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
            List<WebSite> allWebSites = ToolController.getInstance().getAllWebSites();
            List<WebSite> filteredSites = new ArrayList<>();
            
            for (WebSite website : allWebSites) {
                boolean matchesSearch = false;
                boolean matchesCategory = false;
                
                // 检查搜索条件
                if (searchText == null || searchText.trim().isEmpty()) {
                    matchesSearch = true;
                } else {
                    String searchLower = searchText.toLowerCase().trim();
                    String websiteCategory = ToolController.getInstance().getWebSiteCategory(website.getDesc());
                    
                    switch (searchColumnIndex) {
                        case 0: // 全部
                            matchesSearch = website.getDesc().toLowerCase().contains(searchLower) ||
                                           website.getUrl().toLowerCase().contains(searchLower) ||
                                           websiteCategory.toLowerCase().contains(searchLower);
                            break;
                        case 1: // 网站名称
                            matchesSearch = website.getDesc().toLowerCase().contains(searchLower);
                            break;
                        case 2: // 网站地址
                            matchesSearch = website.getUrl().toLowerCase().contains(searchLower);
                            break;
                        case 3: // 分类
                            matchesSearch = websiteCategory.toLowerCase().contains(searchLower);
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
                    String websiteCategory = ToolController.getInstance().getWebSiteCategory(website.getDesc());
                    matchesCategory = websiteCategory.equals(selectedCategory);
                }
                
                // 同时满足搜索和分类条件的记录才会被显示
                if (matchesSearch && matchesCategory) {
                    filteredSites.add(website);
                }
            }
            
            // 更新表格数据
            tableModel.setWebSites(filteredSites);
            
            // 更新状态信息
            updateFilterStatus(filteredSites.size(), allWebSites.size(), searchText, searchColumnIndex, selectedCategory);
            
            if (listener != null) {
                listener.onDataFiltered(filteredSites.size(), allWebSites.size());
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
     * 复制网站URL到剪贴板
     * @param selectedRow 选中行索引
     */
    public void copyUrlToClipboard(int selectedRow) {
        try {
            WebSite website = tableModel.getWebSiteAt(selectedRow);
            if (website != null) {
                java.awt.datatransfer.StringSelection stringSelection = 
                    new java.awt.datatransfer.StringSelection(website.getUrl());
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(stringSelection, null);
                updateStatus("已复制网站地址到剪贴板: " + website.getDesc());
                
                if (listener != null) {
                    listener.onUrlCopied(website);
                }
            }
        } catch (Exception e) {
            String errorMsg = "复制网站地址失败: " + e.getMessage();
            updateStatus(errorMsg);
            logError(errorMsg);
            if (listener != null) {
                listener.onError("复制地址", errorMsg);
            }
        }
    }
    
    /**
     * 获取网站统计信息
     * @return 统计信息
     */
    public String getStatistics() {
        return tableModel.getStatistics();
    }
    
    /**
     * 获取收藏网站列表
     * @return 收藏网站列表
     */
    public List<WebSite> getFavoriteWebSites() {
        return tableModel.getFavoriteWebSites();
    }
    
    /**
     * 根据分类获取网站列表
     * @param category 分类名
     * @return 指定分类的网站列表
     */
    public List<WebSite> getWebSitesByCategory(String category) {
        return tableModel.getWebSitesByCategory(category);
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
        String statusMsg = String.format("显示 %d/%d 个网站", filteredSize, totalSize);
        
        if (searchText != null && !searchText.trim().isEmpty()) {
            String[] searchScopes = {"全部", "网站名称", "网站地址", "分类"};
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
     * 更新网站菜单
     */
    private void updateWebsiteMenu() {
        try {
            ArsenalMenuProvider.updateWebsiteMenu();
        } catch (Exception e) {
            logError("更新菜单失败: " + e.getMessage());
        }
    }
    
    /**
     * 记录错误日志
     * @param message 错误消息
     */
    private void logError(String message) {
        System.err.println("WebSitePanelController: " + message);
    }
    
    /**
     * 视图接口
     * 定义控制器与视图的交互方法
     */
    public interface WebSitePanelView {
        /**
         * 显示添加网站对话框
         * @param parentComponent 父组件
         * @return 新网站对象，取消返回null
         */
        WebSite showAddWebSiteDialog(Component parentComponent);
        
        /**
         * 显示编辑网站对话框
         * @param parentComponent 父组件
         * @param website 要编辑的网站
         * @return 编辑后的网站对象，取消返回null
         */
        WebSite showEditWebSiteDialog(Component parentComponent, WebSite website);
        
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
    public interface WebSitePanelListener {
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
         * 网站添加完成
         * @param website 添加的网站
         */
        void onWebSiteAdded(WebSite website);
        
        /**
         * 网站更新完成
         * @param website 更新的网站
         */
        void onWebSiteUpdated(WebSite website);
        
        /**
         * 网站删除完成
         * @param website 删除的网站
         */
        void onWebSiteDeleted(WebSite website);
        
        /**
         * 收藏状态切换完成
         * @param website 网站对象
         * @param newState 新的收藏状态
         */
        void onFavoriteToggled(WebSite website, boolean newState);
        
        /**
         * 网站打开完成
         * @param website 打开的网站
         */
        void onWebSiteOpened(WebSite website);
        
        /**
         * URL复制完成
         * @param website 复制URL的网站
         */
        void onUrlCopied(WebSite website);
        
        /**
         * 发生错误
         * @param operation 操作名称
         * @param errorMessage 错误消息
         */
        void onError(String operation, String errorMessage);
    }
} 