package model;

import controller.ToolController;
import util.I18nManager;
import view.menu.ArsenalMenuProvider;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * 网站表格模型 (Model层)
 * 负责管理网站的表格数据展示
 */
public class WebSiteTableModel extends AbstractTableModel {
    
    // 表格列名
    private String[] columnNames;
    
    // 网站数据列表
    private List<WebSite> websites = new ArrayList<>();
    
    /**
     * 构造函数
     */
    public WebSiteTableModel() {
        updateColumnNames();
    }
    
    /**
     * 更新列名（支持国际化）
     */
    public void updateColumnNames() {
        I18nManager i18n = I18nManager.getInstance();
        columnNames = new String[]{
            i18n.getText("websites.name"), 
            i18n.getText("websites.url"), 
            i18n.getText("column.favorite"), 
            i18n.getText("label.category")
        };
    }
    
    @Override
    public int getRowCount() {
        return websites.size();
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
        WebSite website = websites.get(rowIndex);
        switch (columnIndex) {
            case 0: return website.getDesc();
            case 1: return website.getUrl();
            case 2: return website.isFavor();
            case 3: return ToolController.getInstance().getWebSiteCategory(website.getDesc());
            default: return null;
        }
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 2) return Boolean.class;
        return String.class;
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 2; // 只有收藏列可编辑
    }
    
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (columnIndex == 2) {
            WebSite website = websites.get(rowIndex);
            boolean oldFavoriteState = website.isFavor();
            website.setFavor((Boolean) value);
            
            // 更新数据库中的收藏状态
            boolean favoriteChanged = (oldFavoriteState != website.isFavor());
            if (favoriteChanged) {
                ToolController.getInstance().updateWebSiteFavorite(website, website.isFavor());
                
                // 更新菜单
                try {
                    ArsenalMenuProvider.updateWebsiteMenu();
                } catch (Exception e) {
                }
            }
            
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
    
    /**
     * 设置网站列表数据
     * @param websites 网站列表
     */
    public void setWebSites(List<WebSite> websites) {
        this.websites = new ArrayList<>(websites);
        fireTableDataChanged();
    }
    
    /**
     * 添加网站
     * @param website 网站对象
     */
    public void addWebSite(WebSite website) {
        websites.add(website);
        int row = websites.size() - 1;
        fireTableRowsInserted(row, row);
    }
    
    /**
     * 更新网站
     * @param index 网站索引
     * @param website 更新后的网站对象
     */
    public void updateWebSite(int index, WebSite website) {
        if (index >= 0 && index < websites.size()) {
            websites.set(index, website);
            fireTableRowsUpdated(index, index);
        }
    }
    
    /**
     * 移除网站
     * @param index 网站索引
     */
    public void removeWebSite(int index) {
        if (index >= 0 && index < websites.size()) {
            websites.remove(index);
            fireTableRowsDeleted(index, index);
        }
    }
    
    /**
     * 获取指定索引的网站
     * @param index 网站索引
     * @return 网站对象
     */
    public WebSite getWebSiteAt(int index) {
        if (index >= 0 && index < websites.size()) {
            return websites.get(index);
        }
        return null;
    }
    
    /**
     * 获取所有网站列表
     * @return 网站列表副本
     */
    public List<WebSite> getWebSites() {
        return new ArrayList<>(websites);
    }
    
    /**
     * 清空所有数据
     */
    public void clear() {
        int oldSize = websites.size();
        if (oldSize > 0) {
            websites.clear();
            fireTableRowsDeleted(0, oldSize - 1);
        }
    }
    
    /**
     * 获取数据大小
     * @return 数据条数
     */
    public int getDataSize() {
        return websites.size();
    }
    
    /**
     * 判断是否为空
     * @return 是否为空
     */
    public boolean isEmpty() {
        return websites.isEmpty();
    }
    
    /**
     * 查找网站索引
     * @param website 网站对象
     * @return 索引位置，未找到返回-1
     */
    public int findWebSiteIndex(WebSite website) {
        return websites.indexOf(website);
    }
    
    /**
     * 根据网站名查找网站索引
     * @param siteName 网站名
     * @return 索引位置，未找到返回-1
     */
    public int findWebSiteIndexByName(String siteName) {
        for (int i = 0; i < websites.size(); i++) {
            if (websites.get(i).getDesc().equals(siteName)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 根据URL查找网站索引
     * @param url 网站URL
     * @return 索引位置，未找到返回-1
     */
    public int findWebSiteIndexByUrl(String url) {
        for (int i = 0; i < websites.size(); i++) {
            if (websites.get(i).getUrl().equals(url)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 获取收藏网站列表
     * @return 收藏网站列表
     */
    public List<WebSite> getFavoriteWebSites() {
        List<WebSite> favorites = new ArrayList<>();
        for (WebSite website : websites) {
            if (website.isFavor()) {
                favorites.add(website);
            }
        }
        return favorites;
    }
    
    /**
     * 根据分类获取网站列表
     * @param category 分类名
     * @return 指定分类的网站列表
     */
    public List<WebSite> getWebSitesByCategory(String category) {
        List<WebSite> categoryWebSites = new ArrayList<>();
        for (WebSite website : websites) {
            String websiteCategory = ToolController.getInstance().getWebSiteCategory(website.getDesc());
            if (websiteCategory.equals(category)) {
                categoryWebSites.add(website);
            }
        }
        return categoryWebSites;
    }
    
    /**
     * 根据搜索条件获取网站列表
     * @param searchText 搜索文本
     * @param searchInName 是否在名称中搜索
     * @param searchInUrl 是否在URL中搜索
     * @param searchInCategory 是否在分类中搜索
     * @return 匹配的网站列表
     */
    public List<WebSite> searchWebSites(String searchText, boolean searchInName, 
                                       boolean searchInUrl, boolean searchInCategory) {
        List<WebSite> results = new ArrayList<>();
        String searchLower = searchText.toLowerCase();
        
        for (WebSite website : websites) {
            boolean matches = false;
            
            if (searchInName && website.getDesc().toLowerCase().contains(searchLower)) {
                matches = true;
            }
            if (searchInUrl && website.getUrl().toLowerCase().contains(searchLower)) {
                matches = true;
            }
            if (searchInCategory) {
                String category = ToolController.getInstance().getWebSiteCategory(website.getDesc());
                if (category.toLowerCase().contains(searchLower)) {
                    matches = true;
                }
            }
            
            if (matches) {
                results.add(website);
            }
        }
        
        return results;
    }
    
    /**
     * 获取网站统计信息
     * @return 统计信息字符串
     */
    public String getStatistics() {
        int totalCount = websites.size();
        int favoriteCount = getFavoriteWebSites().size();
        return String.format("总计: %d, 收藏: %d", totalCount, favoriteCount);
    }
} 