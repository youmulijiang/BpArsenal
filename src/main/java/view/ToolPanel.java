package view;

import model.HttpTool;
import model.HttpToolCommand;
import model.ToolTableModel;
import controller.ToolPanelController;
import view.component.ToolEditDialog;
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
 * HTTP工具面板 (View层)
 * 提供HTTP渗透测试工具的管理、配置和执行功能
 * 重构后的纯视图层，业务逻辑由ToolPanelController处理
 */
public class ToolPanel extends JPanel implements 
        I18nManager.LanguageChangeListener, 
        ToolPanelController.ToolPanelListener,
        ToolPanelController.ToolPanelView {
    
    // UI组件
    private JTable toolTable;
    private ToolTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton favoriteButton;
    private JTextField searchField;
    private JComboBox<String> searchColumnFilter;
    private JComboBox<String> categoryFilter;
    private JLabel statusLabel;
    
    // Controller
    private ToolPanelController controller;
    
    // 常量定义
    private static final int FAVORITE_COLUMN_INDEX = 2;
    private static final int COMMAND_COLUMN_INDEX = 1;
    private static final int NOTE_COLUMN_INDEX = 3;
    private static final int WORK_DIR_COLUMN_INDEX = 4;
    
    public ToolPanel() {
        // 初始化Controller
        controller = ToolPanelController.getInstance();
        controller.setView(this);
        controller.addListener(this);
        
        // 初始化UI
        initializeUI();
        setupEventHandlers();
        
        // 注册语言变更监听器
        I18nManager.getInstance().addLanguageChangeListener(this);
        
        // 加载初始数据
        controller.loadData();
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
        
        // 组装面板
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
        
        addButton = createButton(i18n.getText("tools.button.add"), i18n.getText("tools.tooltip.add"), new Color(46, 125, 50));
        editButton = createButton(i18n.getText("button.edit"), i18n.getText("tools.tooltip.edit"), new Color(25, 118, 210));
        deleteButton = createButton(i18n.getText("button.delete"), i18n.getText("tools.tooltip.delete"), new Color(211, 47, 47));
        favoriteButton = createButton(i18n.getText("button.favorite"), i18n.getText("tools.tooltip.favorite"), new Color(255, 152, 0));
        
        rightPanel.add(addButton);
        rightPanel.add(editButton);
        rightPanel.add(deleteButton);
        rightPanel.add(favoriteButton);
        
        return rightPanel;
    }
    
    /**
     * 创建表格面板
     */
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        
        // 创建表格模型和表格
        tableModel = new ToolTableModel();
        toolTable = new JTable(tableModel);
        
        // 设置表格样式和渲染器
        setupTable();
        
        // 创建滚动面板
        JScrollPane scrollPane = new JScrollPane(toolTable);
        scrollPane.setPreferredSize(new Dimension(800, 450));
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            I18nManager.getInstance().getText("tools.table.title"), 
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 12)
        ));
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    /**
     * 创建底部状态面板
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
        // 应用基本样式
        TableRendererFactory.applyTableStyle(toolTable);
        
        // 设置列宽
        setupColumnWidths();
        
        // 配置渲染器
        TableRendererFactory.configureTableRenderers(toolTable, FAVORITE_COLUMN_INDEX, COMMAND_COLUMN_INDEX);
    }
    
    /**
     * 设置列宽
     */
    private void setupColumnWidths() {
        TableColumnModel columnModel = toolTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(120);  // 工具名称
        columnModel.getColumn(1).setPreferredWidth(300);  // 命令
        columnModel.getColumn(2).setPreferredWidth(60);   // 收藏
        columnModel.getColumn(3).setPreferredWidth(150);  // 备注
        columnModel.getColumn(4).setPreferredWidth(120);  // 工作目录
        columnModel.getColumn(5).setPreferredWidth(80);   // 分类
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 表格双击编辑和右键菜单
        toolTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleTableMouseClick(e);
            }
        });
        
        // 按钮事件
        addButton.addActionListener(e -> controller.addNewTool());
        editButton.addActionListener(e -> controller.editSelectedTool(toolTable.getSelectedRow(), tableModel.getToolCommands()));
        deleteButton.addActionListener(e -> controller.deleteSelectedTool(toolTable.getSelectedRow(), tableModel.getToolCommands()));
        favoriteButton.addActionListener(e -> controller.toggleFavorite(toolTable.getSelectedRow(), tableModel.getToolCommands()));
        
        // 搜索事件
        searchField.addCaretListener(e -> performFilter());
        searchColumnFilter.addActionListener(e -> performFilter());
        categoryFilter.addActionListener(e -> performFilter());
    }
    
    /**
     * 处理表格鼠标点击事件
     */
    private void handleTableMouseClick(MouseEvent e) {
        if (e.getClickCount() == 2) {
            controller.editSelectedTool(toolTable.getSelectedRow(), tableModel.getToolCommands());
        } else if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
            showContextMenu(e);
        }
    }
    
    /**
     * 执行过滤操作
     */
    private void performFilter() {
        String searchText = searchField.getText().toLowerCase().trim();
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
            
            // 执行工具命令
//            JMenuItem executeItem = new JMenuItem("执行命令");
//            executeItem.addActionListener(event ->
//                controller.executeToolCommand(toolTable.getSelectedRow(), tableModel.getToolCommands()));
//            contextMenu.add(executeItem);
//
//            contextMenu.addSeparator();
            
            // 编辑
            JMenuItem editItem = new JMenuItem("编辑工具");
            editItem.addActionListener(event -> 
                controller.editSelectedTool(toolTable.getSelectedRow(), tableModel.getToolCommands()));
            contextMenu.add(editItem);
            
            // 收藏/取消收藏
            HttpToolCommand toolCommand = tableModel.getToolCommandAt(row);
            if (toolCommand != null) {
                String favoriteText = toolCommand.isFavor() ? "取消收藏" : "收藏工具";
                JMenuItem favoriteItem = new JMenuItem(favoriteText);
                favoriteItem.addActionListener(event -> 
                    controller.toggleFavorite(toolTable.getSelectedRow(), tableModel.getToolCommands()));
                contextMenu.add(favoriteItem);
            }
            
            contextMenu.addSeparator();
            
            // 复制命令
//            JMenuItem copyItem = new JMenuItem("复制命令");
//            copyItem.addActionListener(event ->
//                controller.copyCommandToClipboard(toolTable.getSelectedRow(), tableModel.getToolCommands()));
//            contextMenu.add(copyItem);
            
            // 删除
            JMenuItem deleteItem = new JMenuItem("删除工具");
            deleteItem.addActionListener(event -> 
                controller.deleteSelectedTool(toolTable.getSelectedRow(), tableModel.getToolCommands()));
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
            searchColumnFilter.addItem(i18n.getText("tools.tool.name"));
            searchColumnFilter.addItem(i18n.getText("tools.command"));
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
            searchColumnFilter.addItem(i18n.getText("tools.tool.name"));
            searchColumnFilter.addItem(i18n.getText("tools.command"));
            searchColumnFilter.addItem(i18n.getText("tools.note"));
            searchColumnFilter.addItem(i18n.getText("tools.work.dir"));
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
            List<String> categories = controller.getAllCategories();
            categoryFilter.removeAllItems();
            for (String category : categories) {
                categoryFilter.addItem(category);
            }
            categoryFilter.setSelectedIndex(0);
        } catch (Exception e) {
            System.err.println("ToolPanel: 加载分类选项失败: " + e.getMessage());
        }
    }
    
    // ================== ToolPanelView接口实现 ==================
    
    @Override
    public void updateData(List<HttpToolCommand> data) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setToolCommands(data);
        });
    }
    
    @Override
    public void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);
        });
    }
    
    @Override
    public void showAddDialog() {
        SwingUtilities.invokeLater(() -> {
            ToolEditDialog dialog = new ToolEditDialog(SwingUtilities.getWindowAncestor(this), null);
            dialog.setVisible(true);
            
            if (dialog.isConfirmed()) {
                HttpTool newTool = dialog.getTool();
                String category = dialog.getSelectedCategory();
                controller.handleToolAdded(newTool, category);
            }
        });
    }
    
    @Override
    public void showEditDialog(HttpTool tool) {
        SwingUtilities.invokeLater(() -> {
            ToolEditDialog dialog = new ToolEditDialog(SwingUtilities.getWindowAncestor(this), tool);
            dialog.setVisible(true);
            
            if (dialog.isConfirmed()) {
                HttpTool updatedTool = dialog.getTool();
                String newCategory = dialog.getSelectedCategory();
                controller.handleToolEdited(tool, updatedTool, newCategory);
            }
        });
    }
    
    @Override
    public void showEditDialogWithCommand(HttpTool tool, HttpToolCommand toolCommand) {
        SwingUtilities.invokeLater(() -> {
            ToolEditDialog dialog = new ToolEditDialog(SwingUtilities.getWindowAncestor(this), tool, toolCommand);
            dialog.setVisible(true);
            
            if (dialog.isConfirmed()) {
                HttpTool updatedTool = dialog.getTool();
                String newCategory = dialog.getSelectedCategory();
                String note = dialog.getNote();
                String workDir = dialog.getWorkDir();
                
                // 处理包含note和workDir的工具编辑结果
                controller.handleToolEditedWithCommand(tool, updatedTool, newCategory, toolCommand, note, workDir);
            }
        });
    }
    
    @Override
    public void showDeleteConfirmDialog(HttpTool tool) {
        SwingUtilities.invokeLater(() -> {
            int result = JOptionPane.showConfirmDialog(this,
                "确定要删除工具 \"" + tool.getToolName() + "\" 吗？\n注意：这将删除该工具的所有命令。",
                "确认删除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
                
            if (result == JOptionPane.YES_OPTION) {
                controller.handleToolDeleted(tool);
            }
        });
    }
    
    @Override
    public void showMessage(String message, String title, int messageType) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, title, messageType);
        });
    }
    
    // ================== ToolPanelListener接口实现 ==================
    
    @Override
    public void onDataLoaded(List<HttpToolCommand> data) {
        updateData(data);
    }
    
    @Override
    public void onDataFiltered(List<HttpToolCommand> filteredData, int totalSize) {
        updateData(filteredData);
    }
    
    @Override
    public void onDataChanged() {
        SwingUtilities.invokeLater(() -> {
            tableModel.fireTableDataChanged();
        });
    }
    
    @Override
    public void onStatusUpdate(String message) {
        updateStatus(message);
    }
    
    @Override
    public void onShowAddDialog() {
        showAddDialog();
    }
    
    @Override
    public void onShowEditDialog(HttpTool tool) {
        showEditDialog(tool);
    }
    
    @Override
    public void onShowEditDialogWithCommand(HttpTool tool, HttpToolCommand toolCommand) {
        showEditDialogWithCommand(tool, toolCommand);
    }
    
    @Override
    public void onShowDeleteConfirmDialog(HttpTool tool) {
        showDeleteConfirmDialog(tool);
    }
    
    @Override
    public void onShowMessage(String message, String title, int messageType) {
        showMessage(message, title, messageType);
    }
    
    // ================== 语言变更监听器实现 ==================
    
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
            addButton.setText(i18n.getText("tools.button.add"));
            addButton.setToolTipText(i18n.getText("tools.tooltip.add"));
        }
        if (editButton != null) {
            editButton.setText(i18n.getText("button.edit"));
            editButton.setToolTipText(i18n.getText("tools.tooltip.edit"));
        }
        if (deleteButton != null) {
            deleteButton.setText(i18n.getText("button.delete"));
            deleteButton.setToolTipText(i18n.getText("tools.tooltip.delete"));
        }
        if (favoriteButton != null) {
            favoriteButton.setText(i18n.getText("button.favorite"));
            favoriteButton.setToolTipText(i18n.getText("tools.tooltip.favorite"));
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
            TableRendererFactory.configureTableRenderers(toolTable, FAVORITE_COLUMN_INDEX, COMMAND_COLUMN_INDEX);
        }
        
        // 更新搜索范围下拉框选项
        updateSearchColumnFilter();
        
        // 重新加载分类选项
        loadCategoryOptions();
    }
    
    /**
     * 公共方法：刷新数据
     */
    public void loadData() {
        controller.loadData();
    }
} 