package view;

import model.ThirdPartyTool;
import model.ThirdPartyTableModel;
import controller.ThirdPartyPanelController;
import controller.ToolController;
import view.component.ThirdPartyToolEditDialog;
import util.I18nManager;
import util.TableRendererFactory;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * 第三方工具面板 (View层)
 * 重构为MVC架构，仅负责UI展示和用户交互
 */
public class ThirdPartyPanel extends JPanel implements I18nManager.LanguageChangeListener,
        ThirdPartyPanelController.ThirdPartyPanelView, ThirdPartyPanelController.ThirdPartyPanelListener {
    
    private JTable toolTable;
    private ThirdPartyTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton favoriteButton;
    private JButton launchButton;
    private JTextField searchField;
    private JComboBox<String> searchColumnFilter;
    private JComboBox<String> categoryFilter;
    private JLabel statusLabel;
    
    // 控制器
    private ThirdPartyPanelController controller;
    
    // 列索引常量
    private static final int TOOL_NAME_COLUMN_INDEX = 0;
    private static final int COMMAND_COLUMN_INDEX = 1;
    private static final int WORK_DIR_COLUMN_INDEX = 2;
    private static final int FAVORITE_COLUMN_INDEX = 3;
    private static final int CATEGORY_COLUMN_INDEX = 4;
    private static final int AUTO_START_COLUMN_INDEX = 5;
    
    public ThirdPartyPanel() {
        // 初始化控制器
        controller = ThirdPartyPanelController.getInstance();
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
        
        addButton = createButton(i18n.getText("thirdparty.button.add"), i18n.getText("thirdparty.tooltip.add"), new Color(46, 125, 50));
        editButton = createButton(i18n.getText("button.edit"), i18n.getText("thirdparty.tooltip.edit"), new Color(25, 118, 210));
        deleteButton = createButton(i18n.getText("button.delete"), i18n.getText("thirdparty.tooltip.delete"), new Color(211, 47, 47));
        favoriteButton = createButton(i18n.getText("button.favorite"), i18n.getText("thirdparty.tooltip.favorite"), new Color(255, 152, 0));
        launchButton = createButton(i18n.getText("thirdparty.button.launch"), i18n.getText("thirdparty.tooltip.launch"), new Color(102, 187, 106));
        
        rightPanel.add(addButton);
        rightPanel.add(editButton);
        rightPanel.add(deleteButton);
        rightPanel.add(favoriteButton);
        rightPanel.add(launchButton);
        
        return rightPanel;
    }
    
    /**
     * 创建表格面板
     * @return 表格面板
     */
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        
        // 创建表格
        toolTable = new JTable(tableModel);
        setupTable();
        
        // 创建滚动面板
        JScrollPane scrollPane = new JScrollPane(toolTable);
        scrollPane.setPreferredSize(new Dimension(800, 450));
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            I18nManager.getInstance().getText("thirdparty.table.title"), 
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
        TableRendererFactory.applyTableStyle(toolTable);
        
        // 设置列宽
        // setupColumnWidths();
        
        // 设置渲染器
        configureTableRenderers();
    }
    
    /**
     * 设置列宽
     */
    private void setupColumnWidths() {
        TableColumnModel columnModel = toolTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(120);  // 工具名称
        columnModel.getColumn(1).setPreferredWidth(300);  // 启动命令
        columnModel.getColumn(2).setPreferredWidth(150);  // 工作目录
        columnModel.getColumn(3).setPreferredWidth(60);   // 收藏
        columnModel.getColumn(4).setPreferredWidth(80);   // 分类
        columnModel.getColumn(5).setPreferredWidth(80);   // 自启动
    }
    
    /**
     * 配置表格渲染器
     */
    private void configureTableRenderers() {
        TableColumnModel columnModel = toolTable.getColumnModel();
        
        // 设置命令列渲染器
        columnModel.getColumn(COMMAND_COLUMN_INDEX).setCellRenderer(new ThirdPartyCommandRenderer());
        
        // 设置工作目录列渲染器
        columnModel.getColumn(WORK_DIR_COLUMN_INDEX).setCellRenderer(new ThirdPartyWorkDirRenderer());
        
        // 设置收藏列渲染器
        columnModel.getColumn(FAVORITE_COLUMN_INDEX).setCellRenderer(new ThirdPartyFavoriteRenderer());
        
        // 设置自启动列渲染器
        columnModel.getColumn(AUTO_START_COLUMN_INDEX).setCellRenderer(new ThirdPartyAutoStartRenderer());
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 表格事件
        toolTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedTool();
                } else if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
                    showContextMenu(e);
                }
            }
        });
        
        // 按钮事件
        addButton.addActionListener(e -> addNewTool());
        editButton.addActionListener(e -> editSelectedTool());
        deleteButton.addActionListener(e -> deleteSelectedTool());
        favoriteButton.addActionListener(e -> toggleFavorite());
        launchButton.addActionListener(e -> launchSelectedTool());
        
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
     * 添加新工具
     */
    private void addNewTool() {
        controller.addNewTool(this);
    }
    
    /**
     * 编辑选中工具
     */
    private void editSelectedTool() {
        int selectedRow = toolTable.getSelectedRow();
        controller.editSelectedTool(selectedRow, this);
    }
    
    /**
     * 删除选中工具
     */
    private void deleteSelectedTool() {
        int selectedRow = toolTable.getSelectedRow();
        controller.deleteSelectedTool(selectedRow, this);
    }
    
    /**
     * 切换收藏状态
     */
    private void toggleFavorite() {
        int selectedRow = toolTable.getSelectedRow();
        controller.toggleFavorite(selectedRow, this);
    }
    
    /**
     * 启动选中工具
     */
    private void launchSelectedTool() {
        int selectedRow = toolTable.getSelectedRow();
        controller.launchSelectedTool(selectedRow, this);
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
        int row = toolTable.rowAtPoint(e.getPoint());
        if (row >= 0) {
            toolTable.setRowSelectionInterval(row, row);
            
            JPopupMenu contextMenu = new JPopupMenu();
            
            // 启动工具
            JMenuItem launchItem = new JMenuItem("启动工具");
            launchItem.addActionListener(event -> launchSelectedTool());
            contextMenu.add(launchItem);
            
            contextMenu.addSeparator();
            
            // 编辑
            JMenuItem editItem = new JMenuItem("编辑工具");
            editItem.addActionListener(event -> editSelectedTool());
            contextMenu.add(editItem);
            
            // 收藏/取消收藏
            ThirdPartyTool tool = tableModel.getToolAt(row);
            if (tool != null) {
                String favoriteText = tool.isFavor() ? "取消收藏" : "收藏工具";
                JMenuItem favoriteItem = new JMenuItem(favoriteText);
                favoriteItem.addActionListener(event -> toggleFavorite());
                contextMenu.add(favoriteItem);
                
                // 自启动切换
                String autoStartText = tool.isAutoStart() ? "禁用自启动" : "启用自启动";
                JMenuItem autoStartItem = new JMenuItem(autoStartText);
                autoStartItem.addActionListener(event -> {
                    controller.toggleAutoStart(row, !tool.isAutoStart());
                });
                contextMenu.add(autoStartItem);
            }
            
            contextMenu.addSeparator();
            
            // 复制启动命令
            JMenuItem copyItem = new JMenuItem("复制启动命令");
            copyItem.addActionListener(event -> {
                controller.copyCommandToClipboard(toolTable.getSelectedRow());
            });
            contextMenu.add(copyItem);
            
            // 删除
            JMenuItem deleteItem = new JMenuItem("删除工具");
            deleteItem.addActionListener(event -> deleteSelectedTool());
            contextMenu.add(deleteItem);
            
            contextMenu.show(toolTable, e.getX(), e.getY());
        }
    }
    
    /**
     * 初始化搜索范围下拉框选项
     */
    private void initializeSearchColumnFilter() {
        if (searchColumnFilter != null) {
            I18nManager i18n = I18nManager.getInstance();
            
            searchColumnFilter.addItem(i18n.getText("filter.all"));
            searchColumnFilter.addItem(i18n.getText("thirdparty.tool.name"));
            searchColumnFilter.addItem(i18n.getText("thirdparty.start.command"));
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
            searchColumnFilter.addItem(i18n.getText("thirdparty.tool.name"));
            searchColumnFilter.addItem(i18n.getText("thirdparty.start.command"));
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
            List<String> categories = ToolController.getInstance().getAllThirdPartyToolCategories();
            categoryFilter.removeAllItems();
            for (String category : categories) {
                categoryFilter.addItem(category);
            }
            categoryFilter.setSelectedIndex(0);
        } catch (Exception e) {
            I18nManager i18n = I18nManager.getInstance();
            categoryFilter.removeAllItems();
            categoryFilter.addItem(i18n.getText("filter.all"));
            categoryFilter.addItem("exploit");
            categoryFilter.addItem(i18n.getText("thirdparty.category.editor"));
        }
    }
    
    /**
     * 记录错误日志
     */
    
    // =========================== 实现ThirdPartyPanelView接口 ===========================
    
    @Override
    public ThirdPartyTool showAddToolDialog(Component parentComponent) {
        ThirdPartyToolEditDialog dialog = new ThirdPartyToolEditDialog(SwingUtilities.getWindowAncestor(parentComponent), null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            return dialog.getTool();
        }
        return null;
    }
    
    @Override
    public ThirdPartyTool showEditToolDialog(Component parentComponent, ThirdPartyTool tool) {
        ThirdPartyToolEditDialog dialog = new ThirdPartyToolEditDialog(SwingUtilities.getWindowAncestor(parentComponent), tool);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            return dialog.getTool();
        }
        return null;
    }
    
    @Override
    public String getSelectedCategory() {
        // 这里需要从对话框中获取选中的分类，暂时返回默认值
        return "exploit";
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
    
    // =========================== 实现ThirdPartyPanelListener接口 ===========================
    
    @Override
    public void onDataLoaded(int count) {
        // 数据加载完成后的处理
    }
    
    @Override
    public void onDataFiltered(int filteredCount, int totalCount) {
        // 数据过滤完成后的处理
    }
    
    @Override
    public void onToolAdded(ThirdPartyTool tool) {
        // 工具添加完成后的处理
    }
    
    @Override
    public void onToolUpdated(ThirdPartyTool tool) {
        // 工具更新完成后的处理
    }
    
    @Override
    public void onToolDeleted(ThirdPartyTool tool) {
        // 工具删除完成后的处理
    }
    
    @Override
    public void onFavoriteToggled(ThirdPartyTool tool, boolean newState) {
        // 收藏状态切换完成后的处理
    }
    
    @Override
    public void onAutoStartToggled(ThirdPartyTool tool, boolean newState) {
        // 自启动状态切换完成后的处理
    }
    
    @Override
    public void onToolLaunched(ThirdPartyTool tool) {
        // 工具启动完成后的处理
    }
    
    @Override
    public void onCommandCopied(ThirdPartyTool tool) {
        // 命令复制完成后的处理
    }
    
    @Override
    public void onError(String operation, String errorMessage) {
        // 错误处理
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
            addButton.setText(i18n.getText("thirdparty.button.add"));
            addButton.setToolTipText(i18n.getText("thirdparty.tooltip.add"));
        }
        if (editButton != null) {
            editButton.setText(i18n.getText("button.edit"));
            editButton.setToolTipText(i18n.getText("thirdparty.tooltip.edit"));
        }
        if (deleteButton != null) {
            deleteButton.setText(i18n.getText("button.delete"));
            deleteButton.setToolTipText(i18n.getText("thirdparty.tooltip.delete"));
        }
        if (favoriteButton != null) {
            favoriteButton.setText(i18n.getText("button.favorite"));
            favoriteButton.setToolTipText(i18n.getText("thirdparty.tooltip.favorite"));
        }
        if (launchButton != null) {
            launchButton.setText(i18n.getText("thirdparty.button.launch"));
            launchButton.setToolTipText(i18n.getText("thirdparty.tooltip.launch"));
        }
        
        // 更新状态标签
        if (statusLabel != null) {
            statusLabel.setText(i18n.getText("status.ready"));
        }
        
        // 更新表格列名
        if (tableModel != null) {
            tableModel.updateColumnNames();
            tableModel.fireTableStructureChanged();
            
            // 重新设置列宽（因为fireTableStructureChanged会重置列宽）
            setupColumnWidths();
            
            // 重新设置渲染器（因为fireTableStructureChanged会重置所有列）
            configureTableRenderers();
        }
        
        // 更新搜索范围下拉框选项
        updateSearchColumnFilter();
        
        // 延迟重新加载分类选项，确保配置已完全加载
        SwingUtilities.invokeLater(() -> {
            loadCategoryOptions();
        });
    }
}

// =========================== 渲染器类 ===========================

/**
 * 收藏状态渲染器
 */
class ThirdPartyFavoriteRenderer extends DefaultTableCellRenderer {
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
 * 自启动状态渲染器
 */
class ThirdPartyAutoStartRenderer extends DefaultTableCellRenderer {
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
            label.setText(((Boolean) value) ? "●" : "○");
            label.setForeground(((Boolean) value) ? new Color(46, 125, 50) : new Color(158, 158, 158));
        }
        
        return label;
    }
}

/**
 * 纯文本渲染器 - 启动命令列专用
 */
class ThirdPartyCommandRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, 
            boolean isSelected, boolean hasFocus, int row, int column) {
        
        Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        if (value != null) {
            String command = value.toString();
            
            // 显示完整命令作为提示
            setToolTipText(command);
            
            // 如果命令太长，截断显示
            if (command.length() > 80) {
                setText(command.substring(0, 77) + "...");
            } else {
                setText(command);
            }
            
            // 设置文本对齐
            setHorizontalAlignment(JLabel.LEFT);
        }
        
        return comp;
    }
}

/**
 * 工作目录渲染器
 */
class ThirdPartyWorkDirRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, 
            boolean isSelected, boolean hasFocus, int row, int column) {
        
        Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        if (value != null) {
            String workDir = value.toString();
            
            // 显示完整路径作为提示
            setToolTipText(workDir);
            
            // 如果路径太长，截断显示
            if (workDir.length() > 30) {
                setText("..." + workDir.substring(workDir.length() - 27));
            } else {
                setText(workDir);
            }
            
            // 设置文本对齐和样式
            setHorizontalAlignment(JLabel.LEFT);
            setForeground(new Color(100, 100, 100));
        }
        
        return comp;
    }
} 