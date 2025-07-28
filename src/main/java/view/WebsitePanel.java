package view;

import model.WebSite;
import model.WebSiteTableModel;
import controller.WebSitePanelController;
import controller.ToolController;
import view.component.WebSiteEditDialog;
import util.I18nManager;
import util.WebSiteRendererFactory;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * 网站导航面板 (View层)
 * 重构为MVC架构，仅负责UI展示和用户交互
 */
public class WebsitePanel extends JPanel implements I18nManager.LanguageChangeListener,
        WebSitePanelController.WebSitePanelView, WebSitePanelController.WebSitePanelListener {
    
    private JTable websiteTable;
    private WebSiteTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton favoriteButton;
    private JButton openButton;
    private JTextField searchField;
    private JComboBox<String> searchColumnFilter;
    private JComboBox<String> categoryFilter;
    private JLabel statusLabel;
    
    // 控制器
    private WebSitePanelController controller;
    
    // 列索引常量
    private static final int FAVORITE_COLUMN_INDEX = 2;
    private static final int URL_COLUMN_INDEX = 1;
    
    public WebsitePanel() {
        // 初始化控制器
        controller = WebSitePanelController.getInstance();
        controller.setView(this);
        controller.setListener(this);
        
        // 获取表格模型
        tableModel = controller.getTableModel();
        
        initializeUI();
        setupEventHandlers();
        
        // 注册语言变更监听器
        I18nManager.getInstance().addLanguageChangeListener(this);
        
        // 加载数据
        loadData();
    }
    
    /**
     * 初始化UI组件
     */
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // 创建顶部工具栏
        JPanel toolbarPanel = createToolbarPanel();
        add(toolbarPanel, BorderLayout.NORTH);
        
        // 创建表格面板
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // 创建底部状态面板
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 创建顶部工具栏
     * @return 工具栏面板
     */
    private JPanel createToolbarPanel() {
        JPanel toolbarPanel = new JPanel(new BorderLayout(10, 0));
        toolbarPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // 左侧：搜索和过滤
        JPanel leftPanel = createSearchPanel();
        
        // 右侧：操作按钮
        JPanel rightPanel = createButtonPanel();
        
        toolbarPanel.add(leftPanel, BorderLayout.WEST);
        toolbarPanel.add(rightPanel, BorderLayout.EAST);
        
        return toolbarPanel;
    }
    
    /**
     * 创建搜索面板
     */
    private JPanel createSearchPanel() {
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        
        I18nManager i18n = I18nManager.getInstance();
        
        // 搜索框
        JLabel searchLabel = new JLabel(i18n.getText("label.search"));
        searchLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        searchField = new JTextField(15);
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        searchField.setToolTipText(i18n.getText("tooltip.search.input"));
        
        // 搜索列筛选
        JLabel searchColumnLabel = new JLabel(i18n.getText("label.search.scope"));
        searchColumnLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        searchColumnFilter = new JComboBox<>();
        searchColumnFilter.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        searchColumnFilter.setToolTipText(i18n.getText("tooltip.search.column"));
        initializeSearchColumnFilter();
        
        // 分类过滤
        JLabel categoryLabel = new JLabel(i18n.getText("label.category"));
        categoryLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        categoryFilter = new JComboBox<>();
        categoryFilter.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        loadCategoryOptions();
        
        leftPanel.add(searchLabel);
        leftPanel.add(Box.createHorizontalStrut(5));
        leftPanel.add(searchField);
        leftPanel.add(Box.createHorizontalStrut(10));
        leftPanel.add(searchColumnLabel);
        leftPanel.add(Box.createHorizontalStrut(5));
        leftPanel.add(searchColumnFilter);
        leftPanel.add(Box.createHorizontalStrut(15));
        leftPanel.add(categoryLabel);
        leftPanel.add(Box.createHorizontalStrut(5));
        leftPanel.add(categoryFilter);
        
        return leftPanel;
    }
    
    /**
     * 创建按钮面板
     */
    private JPanel createButtonPanel() {
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        
        I18nManager i18n = I18nManager.getInstance();
        
        addButton = createButton(i18n.getText("websites.button.add"), i18n.getText("websites.tooltip.add"), new Color(46, 125, 50));
        editButton = createButton(i18n.getText("button.edit"), i18n.getText("websites.tooltip.edit"), new Color(25, 118, 210));
        deleteButton = createButton(i18n.getText("button.delete"), i18n.getText("websites.tooltip.delete"), new Color(211, 47, 47));
        favoriteButton = createButton(i18n.getText("button.favorite"), i18n.getText("websites.tooltip.favorite"), new Color(255, 152, 0));
        openButton = createButton(i18n.getText("websites.button.open"), i18n.getText("websites.tooltip.open"), new Color(76, 175, 80));
        
        rightPanel.add(addButton);
        rightPanel.add(editButton);
        rightPanel.add(deleteButton);
        rightPanel.add(favoriteButton);
        rightPanel.add(openButton);
        
        return rightPanel;
    }
    
    /**
     * 创建表格面板
     * @return 表格面板
     */
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        
        // 创建表格
        websiteTable = new JTable(tableModel);
        setupTable();
        
        // 创建滚动面板
        JScrollPane scrollPane = new JScrollPane(websiteTable);
        scrollPane.setPreferredSize(new Dimension(800, 450));
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            I18nManager.getInstance().getText("websites.table.title"), 
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 12)
        ));
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    /**
     * 创建底部状态面板
     * @return 底部面板
     */
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        // 状态信息
        statusLabel = new JLabel(I18nManager.getInstance().getText("status.ready"));
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(100, 100, 100));
        
        bottomPanel.add(statusLabel, BorderLayout.EAST);
        
        return bottomPanel;
    }
    
    /**
     * 创建按钮
     */
    private JButton createButton(String text, String tooltip, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        button.setPreferredSize(new Dimension(90, 28));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setToolTipText(tooltip);
        return button;
    }
    
    /**
     * 设置表格属性
     */
    private void setupTable() {
        // 应用基本表格样式
        WebSiteRendererFactory.applyTableStyle(websiteTable);
        
        // 设置列宽
        TableColumnModel columnModel = websiteTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(150);  // 网站名称
        columnModel.getColumn(1).setPreferredWidth(400);  // 网站地址
        columnModel.getColumn(2).setPreferredWidth(60);   // 收藏
        columnModel.getColumn(3).setPreferredWidth(80);   // 分类
        
        // 设置渲染器
        configureTableRenderers();
    }
    
    /**
     * 配置表格渲染器
     */
    private void configureTableRenderers() {
        WebSiteRendererFactory.configureTableRenderers(websiteTable, FAVORITE_COLUMN_INDEX, URL_COLUMN_INDEX);
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 表格事件
        websiteTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedWebsite();
                } else if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
                    showContextMenu(e);
                }
            }
        });
        
        // 按钮事件
        addButton.addActionListener(e -> addNewWebsite());
        editButton.addActionListener(e -> editSelectedWebsite());
        deleteButton.addActionListener(e -> deleteSelectedWebsite());
        favoriteButton.addActionListener(e -> toggleFavorite());
        openButton.addActionListener(e -> openSelectedWebsite());
        
        // 搜索事件
        searchField.addCaretListener(e -> filterTable());
        searchColumnFilter.addActionListener(e -> filterTable());
        categoryFilter.addActionListener(e -> filterTable());
    }
    
    /**
     * 加载数据
     */
    public void loadData() {
        controller.loadData();
    }
    
    /**
     * 添加新网站
     */
    private void addNewWebsite() {
        controller.addNewWebSite(this);
    }
    
    /**
     * 编辑选中网站
     */
    private void editSelectedWebsite() {
        int selectedRow = websiteTable.getSelectedRow();
        controller.editSelectedWebSite(selectedRow, this);
    }
    
    /**
     * 删除选中网站
     */
    private void deleteSelectedWebsite() {
        int selectedRow = websiteTable.getSelectedRow();
        controller.deleteSelectedWebSite(selectedRow, this);
    }
    
    /**
     * 切换收藏状态
     */
    private void toggleFavorite() {
        int selectedRow = websiteTable.getSelectedRow();
        controller.toggleFavorite(selectedRow, this);
    }
    
    /**
     * 打开选中网站
     */
    private void openSelectedWebsite() {
        int selectedRow = websiteTable.getSelectedRow();
        controller.openSelectedWebSite(selectedRow, this);
    }
    
    /**
     * 过滤表格
     */
    private void filterTable() {
        String searchText = searchField.getText();
        int searchColumnIndex = searchColumnFilter.getSelectedIndex();
        String selectedCategory = (String) categoryFilter.getSelectedItem();
        
        controller.filterTable(searchText, searchColumnIndex, selectedCategory);
    }
    
    /**
     * 显示右键菜单
     */
    private void showContextMenu(MouseEvent e) {
        int row = websiteTable.rowAtPoint(e.getPoint());
        if (row >= 0) {
            websiteTable.setRowSelectionInterval(row, row);
            
            JPopupMenu contextMenu = new JPopupMenu();
            
            JMenuItem openItem = new JMenuItem("打开网站");
            openItem.addActionListener(event -> openSelectedWebsite());
            contextMenu.add(openItem);
            
            JMenuItem editItem = new JMenuItem("编辑");
            editItem.addActionListener(event -> editSelectedWebsite());
            contextMenu.add(editItem);
            
            contextMenu.addSeparator();
            
            JMenuItem favoriteItem = new JMenuItem("切换收藏");
            favoriteItem.addActionListener(event -> toggleFavorite());
            contextMenu.add(favoriteItem);
            
            // 复制URL
            JMenuItem copyItem = new JMenuItem("复制网站地址");
            copyItem.addActionListener(event -> {
                controller.copyUrlToClipboard(websiteTable.getSelectedRow());
            });
            contextMenu.add(copyItem);
            
            JMenuItem deleteItem = new JMenuItem("删除");
            deleteItem.addActionListener(event -> deleteSelectedWebsite());
            contextMenu.add(deleteItem);
            
            contextMenu.show(websiteTable, e.getX(), e.getY());
        }
    }
    
    /**
     * 初始化搜索范围下拉框选项
     */
    private void initializeSearchColumnFilter() {
        if (searchColumnFilter != null) {
            I18nManager i18n = I18nManager.getInstance();
            
            searchColumnFilter.addItem(i18n.getText("filter.all"));
            searchColumnFilter.addItem(i18n.getText("websites.name"));
            searchColumnFilter.addItem(i18n.getText("websites.url"));
            searchColumnFilter.addItem(i18n.getText("label.category"));
            
            searchColumnFilter.setSelectedIndex(0);
        }
    }
    
    /**
     * 更新搜索范围下拉框选项
     */
    private void updateSearchColumnFilter() {
        if (searchColumnFilter != null) {
            I18nManager i18n = I18nManager.getInstance();
            
            int selectedIndex = searchColumnFilter.getSelectedIndex();
            
            searchColumnFilter.removeAllItems();
            
            searchColumnFilter.addItem(i18n.getText("filter.all"));
            searchColumnFilter.addItem(i18n.getText("websites.name"));
            searchColumnFilter.addItem(i18n.getText("websites.url"));
            searchColumnFilter.addItem(i18n.getText("label.category"));
            
            if (selectedIndex >= 0 && selectedIndex < searchColumnFilter.getItemCount()) {
                searchColumnFilter.setSelectedIndex(selectedIndex);
            }
        }
    }

    /**
     * 动态加载分类选项
     */
    private void loadCategoryOptions() {
        try {
            List<String> categories = ToolController.getInstance().getAllWebSiteCategories();
            categoryFilter.removeAllItems();
            for (String category : categories) {
                categoryFilter.addItem(category);
            }
            categoryFilter.setSelectedIndex(0);
        } catch (Exception e) {
            I18nManager i18n = I18nManager.getInstance();
            categoryFilter.removeAllItems();
            categoryFilter.addItem(i18n.getText("filter.all"));
            categoryFilter.addItem("OSINT");
            categoryFilter.addItem("Recon");
            categoryFilter.addItem(i18n.getText("websites.category.vulnerability.db"));
            logError("加载分类选项失败: " + e.getMessage());
        }
    }
    
    /**
     * 记录错误日志
     */
    private void logError(String message) {
        System.err.println("WebsitePanel: " + message);
    }
    
    // =========================== 实现WebSitePanelView接口 ===========================
    
    @Override
    public WebSite showAddWebSiteDialog(Component parentComponent) {
        WebSiteEditDialog dialog = new WebSiteEditDialog(SwingUtilities.getWindowAncestor(parentComponent), null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            return dialog.getWebSite();
        }
        return null;
    }
    
    @Override
    public WebSite showEditWebSiteDialog(Component parentComponent, WebSite website) {
        WebSiteEditDialog dialog = new WebSiteEditDialog(SwingUtilities.getWindowAncestor(parentComponent), website);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            return dialog.getWebSite();
        }
        return null;
    }
    
    @Override
    public String getSelectedCategory() {
        // 这里需要从对话框中获取选中的分类，暂时返回默认值
        return "OSINT";
    }
    
    @Override
    public void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }
    
    @Override
    public void showMessage(Component parent, String message, String title, int messageType) {
        JOptionPane.showMessageDialog(parent, message, title, messageType);
    }
    
    // =========================== 实现WebSitePanelListener接口 ===========================
    
    @Override
    public void onDataLoaded(int count) {
        // 数据加载完成后的处理
    }
    
    @Override
    public void onDataFiltered(int filteredCount, int totalCount) {
        // 数据过滤完成后的处理
    }
    
    @Override
    public void onWebSiteAdded(WebSite website) {
        // 网站添加完成后的处理
    }
    
    @Override
    public void onWebSiteUpdated(WebSite website) {
        // 网站更新完成后的处理
    }
    
    @Override
    public void onWebSiteDeleted(WebSite website) {
        // 网站删除完成后的处理
    }
    
    @Override
    public void onFavoriteToggled(WebSite website, boolean newState) {
        // 收藏状态切换完成后的处理
    }
    
    @Override
    public void onWebSiteOpened(WebSite website) {
        // 网站打开完成后的处理
    }
    
    @Override
    public void onUrlCopied(WebSite website) {
        // URL复制完成后的处理
    }
    
    @Override
    public void onError(String operation, String errorMessage) {
        // 错误处理
        logError(operation + "失败: " + errorMessage);
    }
    
    // =========================== 实现LanguageChangeListener接口 ===========================
    
    @Override
    public void onLanguageChanged(I18nManager.SupportedLanguage newLanguage) {
        SwingUtilities.invokeLater(() -> {
            updateUITexts();
            revalidate();
            repaint();
        });
    }
    
    /**
     * 更新UI文本
     */
    private void updateUITexts() {
        I18nManager i18n = I18nManager.getInstance();
        
        // 更新按钮文本
        if (addButton != null) {
            addButton.setText(i18n.getText("websites.button.add"));
            addButton.setToolTipText(i18n.getText("websites.tooltip.add"));
        }
        if (editButton != null) {
            editButton.setText(i18n.getText("button.edit"));
            editButton.setToolTipText(i18n.getText("websites.tooltip.edit"));
        }
        if (deleteButton != null) {
            deleteButton.setText(i18n.getText("button.delete"));
            deleteButton.setToolTipText(i18n.getText("websites.tooltip.delete"));
        }
        if (favoriteButton != null) {
            favoriteButton.setText(i18n.getText("button.favorite"));
            favoriteButton.setToolTipText(i18n.getText("websites.tooltip.favorite"));
        }
        if (openButton != null) {
            openButton.setText(i18n.getText("websites.button.open"));
            openButton.setToolTipText(i18n.getText("websites.tooltip.open"));
        }
        
        // 更新状态标签
        if (statusLabel != null) {
            statusLabel.setText(i18n.getText("status.ready"));
        }
        
        // 更新表格列名
        if (tableModel != null) {
            tableModel.updateColumnNames();
            tableModel.fireTableStructureChanged();
            
            // 重新设置渲染器（因为fireTableStructureChanged会重置所有列）
            configureTableRenderers();
        }
        
        // 更新搜索范围下拉框选项
        updateSearchColumnFilter();
        
        // 重新加载分类选项
        loadCategoryOptions();
    }
}

 