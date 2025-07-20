package view;

import model.WebSite;
import controller.ToolController;
import view.component.WebSiteEditDialog;
import view.menu.ArsenalMenuProvider;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * 网站导航面板 (View层)
 * 用于快速访问常用安全网站
 */
public class WebsitePanel extends JPanel {
    
    private JTable websiteTable;
    private WebSiteTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton favoriteButton;
    private JButton openButton;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JLabel statusLabel;
    
    public WebsitePanel() {
        initializeUI();
        setupEventHandlers();
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
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        
        // 搜索框
        JLabel searchLabel = new JLabel("搜索:");
        searchLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        searchField = new JTextField(15);
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        searchField.setToolTipText("输入网站名称或URL进行搜索");
        
        // 分类过滤
        JLabel categoryLabel = new JLabel("分类:");
        categoryLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        categoryFilter = new JComboBox<>(new String[]{"全部", "OSINT", "信息收集", "漏洞库", "在线工具", "学习资源", "其他"});
        categoryFilter.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        
        leftPanel.add(searchLabel);
        leftPanel.add(Box.createHorizontalStrut(5));
        leftPanel.add(searchField);
        leftPanel.add(Box.createHorizontalStrut(15));
        leftPanel.add(categoryLabel);
        leftPanel.add(Box.createHorizontalStrut(5));
        leftPanel.add(categoryFilter);
        
        // 右侧：操作按钮
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        
        addButton = createButton("+ 添加网站", "添加新的网站", new Color(46, 125, 50));
        editButton = createButton("编辑", "编辑选中的网站", new Color(25, 118, 210));
        deleteButton = createButton("删除", "删除选中的网站", new Color(211, 47, 47));
        favoriteButton = createButton("收藏", "切换收藏状态", new Color(255, 152, 0));
        openButton = createButton("打开", "在浏览器中打开网站", new Color(76, 175, 80));
        
        rightPanel.add(addButton);
        rightPanel.add(editButton);
        rightPanel.add(deleteButton);
        rightPanel.add(favoriteButton);
        rightPanel.add(openButton);
        
        toolbarPanel.add(leftPanel, BorderLayout.WEST);
        toolbarPanel.add(rightPanel, BorderLayout.EAST);
        
        return toolbarPanel;
    }
    
    /**
     * 创建表格面板
     * @return 表格面板
     */
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        
        // 创建表格模型和表格
        tableModel = new WebSiteTableModel();
        websiteTable = new JTable(tableModel);
        
        // 设置表格属性
        setupTable();
        
        // 创建滚动面板
        JScrollPane scrollPane = new JScrollPane(websiteTable);
        scrollPane.setPreferredSize(new Dimension(800, 450));
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            "网站导航列表", 
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
        statusLabel = new JLabel("就绪");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(100, 100, 100));
        
        bottomPanel.add(statusLabel, BorderLayout.EAST);
        
        return bottomPanel;
    }
    
    /**
     * 创建按钮
     * @param text 按钮文本
     * @param tooltip 提示文本
     * @param color 背景颜色
     * @return 按钮
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
        // 基本设置
        websiteTable.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        websiteTable.setRowHeight(25);
        websiteTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        websiteTable.setGridColor(new Color(230, 230, 230));
        websiteTable.setShowGrid(true);
        
        // 设置表头
        JTableHeader header = websiteTable.getTableHeader();
        header.setFont(new Font("微软雅黑", Font.BOLD, 12));
        header.setBackground(new Color(245, 245, 245));
        header.setReorderingAllowed(false);
        
        // 设置列宽
        TableColumnModel columnModel = websiteTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(150);  // 网站名称
        columnModel.getColumn(1).setPreferredWidth(400);  // 网站地址
        columnModel.getColumn(2).setPreferredWidth(60);   // 收藏
        columnModel.getColumn(3).setPreferredWidth(80);   // 分类
        
        // 设置收藏列渲染器
        columnModel.getColumn(2).setCellRenderer(new WebSiteFavoriteRenderer());
        
        // 设置URL列渲染器（可点击链接样式）
        columnModel.getColumn(1).setCellRenderer(new WebSiteUrlRenderer());
        
        // 设置行颜色交替
        websiteTable.setDefaultRenderer(Object.class, new WebSiteAlternateRowRenderer());
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 表格双击打开网站
        websiteTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedWebsite();
                } else if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
                    // 右键菜单（可扩展）
                    showContextMenu(e);
                }
            }
        });
        
        // 选择变化处理
        websiteTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
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
        categoryFilter.addActionListener(e -> filterTable());
    }
    
    /**
     * 加载数据
     */
    public void loadData() {
        try {
            List<WebSite> websites = ToolController.getInstance().getAllWebSites();
            tableModel.setWebSites(websites);
            updateStatus("已加载 " + websites.size() + " 个网站");
            
        } catch (Exception e) {
            updateStatus("加载失败: " + e.getMessage());
            logError("加载网站数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加新网站
     */
    private void addNewWebsite() {
        WebSiteEditDialog dialog = new WebSiteEditDialog(SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            WebSite newWebsite = dialog.getWebSite();
            String category = dialog.getSelectedCategory();
            
            if (ToolController.getInstance().addWebSite(newWebsite, category)) {
                loadData();
                updateStatus("已添加网站: " + newWebsite.getDesc() + " (分类: " + category + ")");
                
                // 如果新网站是收藏状态，更新菜单
                if (newWebsite.isFavor()) {
                    try {
                        ArsenalMenuProvider.updateWebsiteMenu();
                    } catch (Exception e) {
                        logError("更新菜单失败: " + e.getMessage());
                    }
                }
            } else {
                updateStatus("添加网站失败");
            }
        }
    }
    
    /**
     * 编辑选中网站
     */
    private void editSelectedWebsite() {
        int selectedRow = websiteTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的网站", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        WebSite website = tableModel.getWebSiteAt(selectedRow);
        boolean oldFavoriteState = website.isFavor();
        
        WebSiteEditDialog dialog = new WebSiteEditDialog(SwingUtilities.getWindowAncestor(this), website);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            WebSite updatedWebsite = dialog.getWebSite();
            String newCategory = dialog.getSelectedCategory();
            
            if (ToolController.getInstance().updateWebSite(website, updatedWebsite, newCategory)) {
                loadData();
                updateStatus("已更新网站: " + updatedWebsite.getDesc() + " (分类: " + newCategory + ")");
                
                // 如果收藏状态发生变化，更新菜单
                if (oldFavoriteState != updatedWebsite.isFavor()) {
                    try {
                        ArsenalMenuProvider.updateWebsiteMenu();
                    } catch (Exception e) {
                        logError("更新菜单失败: " + e.getMessage());
                    }
                }
            } else {
                updateStatus("更新网站失败");
            }
        }
    }
    
    /**
     * 删除选中网站
     */
    private void deleteSelectedWebsite() {
        int selectedRow = websiteTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的网站", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        WebSite website = tableModel.getWebSiteAt(selectedRow);
        boolean wasFavorite = website.isFavor();
        
        int result = JOptionPane.showConfirmDialog(this,
            "确定要删除网站 \"" + website.getDesc() + "\" 吗？",
            "确认删除",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (result == JOptionPane.YES_OPTION) {
            if (ToolController.getInstance().removeWebSite(website)) {
                loadData();
                updateStatus("已删除网站: " + website.getDesc());
                
                // 如果删除的是收藏网站，更新菜单
                if (wasFavorite) {
                    try {
                        ArsenalMenuProvider.updateWebsiteMenu();
                    } catch (Exception e) {
                        logError("更新菜单失败: " + e.getMessage());
                    }
                }
            } else {
                updateStatus("删除网站失败");
            }
        }
    }
    
    /**
     * 切换收藏状态
     */
    private void toggleFavorite() {
        int selectedRow = websiteTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择网站", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        WebSite website = tableModel.getWebSiteAt(selectedRow);
        boolean newFavoriteState = !website.isFavor();
        
        if (ToolController.getInstance().updateWebSiteFavorite(website, newFavoriteState)) {
            website.setFavor(newFavoriteState);
            tableModel.fireTableDataChanged();
            
            String status = website.isFavor() ? "已收藏" : "已取消收藏";
            updateStatus(status + ": " + website.getDesc());
            
            // 动态更新菜单栏
            try {
                ArsenalMenuProvider.updateWebsiteMenu();
            } catch (Exception e) {
                logError("更新菜单失败: " + e.getMessage());
            }
        } else {
            updateStatus("更新收藏状态失败");
        }
    }
    
    /**
     * 打开选中网站
     */
    private void openSelectedWebsite() {
        int selectedRow = websiteTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要打开的网站", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        WebSite website = tableModel.getWebSiteAt(selectedRow);
        
        try {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(new URI(website.getUrl()));
                updateStatus("正在打开: " + website.getDesc() + " - " + website.getUrl());
            } else {
                updateStatus("系统不支持打开浏览器");
                JOptionPane.showMessageDialog(this,
                    "系统不支持打开浏览器\n网站地址: " + website.getUrl(),
                    "无法打开网站",
                    JOptionPane.WARNING_MESSAGE);
            }
            
        } catch (Exception e) {
            updateStatus("打开网站失败: " + e.getMessage());
            logError("打开网站失败: " + e.getMessage());
            
            JOptionPane.showMessageDialog(this,
                "打开网站失败！\n错误: " + e.getMessage() + "\n网站地址: " + website.getUrl(),
                "打开失败",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 显示右键菜单
     * @param e 鼠标事件
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
            
            JMenuItem deleteItem = new JMenuItem("删除");
            deleteItem.addActionListener(event -> deleteSelectedWebsite());
            contextMenu.add(deleteItem);
            
            contextMenu.show(websiteTable, e.getX(), e.getY());
        }
    }
    
    /**
     * 过滤表格
     */
    private void filterTable() {
        // TODO: 实现搜索和过滤功能
        String searchText = searchField.getText().toLowerCase();
        String selectedCategory = (String) categoryFilter.getSelectedItem();
        
        updateStatus("搜索: " + searchText + ", 分类: " + selectedCategory);
    }
    
    /**
     * 更新按钮状态
     */
    private void updateButtonStates() {
        boolean hasSelection = websiteTable.getSelectedRow() != -1;
        editButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
        favoriteButton.setEnabled(hasSelection);
        openButton.setEnabled(hasSelection);
    }
    
    /**
     * 更新状态
     * @param message 状态消息
     */
    private void updateStatus(String message) {
        statusLabel.setText(message);
    }
    
    /**
     * 记录错误日志
     * @param message 错误消息
     */
    private void logError(String message) {
        System.err.println("WebsitePanel: " + message);
    }
}

/**
 * 网站表格模型
 */
class WebSiteTableModel extends AbstractTableModel {
    private final String[] columnNames = {"网站名称", "网站地址", "收藏", "分类"};
    private List<WebSite> websites = new ArrayList<>();
    
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
                    System.err.println("更新菜单失败: " + e.getMessage());
                }
            }
            
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
    
    public void setWebSites(List<WebSite> websites) {
        this.websites = new ArrayList<>(websites);
        fireTableDataChanged();
    }
    
    public WebSite getWebSiteAt(int index) {
        return websites.get(index);
    }
    
    public List<WebSite> getWebSites() {
        return new ArrayList<>(websites);
    }
}

/**
 * 收藏状态渲染器
 */
class WebSiteFavoriteRenderer extends DefaultTableCellRenderer {
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
class WebSiteUrlRenderer extends DefaultTableCellRenderer {
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
            setToolTipText("<html><a href='" + url + "'>" + url + "</a></html>");
            
            // 如果URL太长，截断显示
            if (url.length() > 60) {
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
 * 交替行颜色渲染器
 */
class WebSiteAlternateRowRenderer extends DefaultTableCellRenderer {
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