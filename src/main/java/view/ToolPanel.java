package view;

import model.HttpTool;
import model.HttpToolCommand;
import controller.ToolController;
import view.component.ToolEditDialog;
import util.I18nManager;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * HTTP工具面板
 * 提供HTTP渗透测试工具的管理、配置和执行功能
 */
public class ToolPanel extends JPanel implements I18nManager.LanguageChangeListener {
    
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
    
    public ToolPanel() {
        initializeUI();
        setupEventHandlers();
        
        // 注册语言变更监听器
        I18nManager.getInstance().addLanguageChangeListener(this);
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
        I18nManager i18n = I18nManager.getInstance();
        JLabel searchLabel = new JLabel(i18n.getText("label.search"));
        searchLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        searchField = new JTextField(15);
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        searchField.setToolTipText(i18n.getText("tooltip.search.input"));
        
        // 搜索列筛选
        JLabel searchColumnLabel = new JLabel(i18n.getText("label.search.scope"));
        searchColumnLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        searchColumnFilter = new JComboBox<>(new String[]{
            i18n.getText("filter.all"), 
            i18n.getText("tools.tool.name"), 
            i18n.getText("tools.command"), 
            i18n.getText("label.category")
        });
        searchColumnFilter.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        searchColumnFilter.setToolTipText(i18n.getText("tooltip.search.column"));
        
        // 分类过滤
        JLabel categoryLabel = new JLabel(i18n.getText("label.category"));
        categoryLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        categoryFilter = new JComboBox<>();
        categoryFilter.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        // 动态加载分类选项
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
        
        // 右侧：操作按钮
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        
        addButton = createButton(i18n.getText("tools.button.add"), i18n.getText("tools.tooltip.add"), new Color(46, 125, 50));
        editButton = createButton(i18n.getText("button.edit"), i18n.getText("tools.tooltip.edit"), new Color(25, 118, 210));
        deleteButton = createButton(i18n.getText("button.delete"), i18n.getText("tools.tooltip.delete"), new Color(211, 47, 47));
        favoriteButton = createButton(i18n.getText("button.favorite"), i18n.getText("tools.tooltip.favorite"), new Color(255, 152, 0));
        
        rightPanel.add(addButton);
        rightPanel.add(editButton);
        rightPanel.add(deleteButton);
        rightPanel.add(favoriteButton);
        
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
        tableModel = new ToolTableModel();
        toolTable = new JTable(tableModel);
        
        // 设置表格属性
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
        toolTable.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        toolTable.setRowHeight(25);
        toolTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        toolTable.setGridColor(new Color(230, 230, 230));
        toolTable.setShowGrid(true);
        
        // 设置表头
        JTableHeader header = toolTable.getTableHeader();
        header.setFont(new Font("微软雅黑", Font.BOLD, 12));
        header.setBackground(new Color(245, 245, 245));
        header.setReorderingAllowed(false);
        
        // 设置列宽
        TableColumnModel columnModel = toolTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(120);  // 工具名称
        columnModel.getColumn(1).setPreferredWidth(400);  // 命令
        columnModel.getColumn(2).setPreferredWidth(60);   // 收藏
        columnModel.getColumn(3).setPreferredWidth(80);   // 分类
        
        // 设置收藏列渲染器
        columnModel.getColumn(2).setCellRenderer(new FavoriteRenderer());
        
        // 设置命令列渲染器（纯文本格式）
        columnModel.getColumn(1).setCellRenderer(new PlainTextRenderer());
        
        // 设置行颜色交替
//        toolTable.setDefaultRenderer(Object.class, new AlternateRowRenderer());
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 表格双击编辑和右键菜单
        toolTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedTool();
                } else if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
                    // 右键菜单
                    showContextMenu(e);
                }
            }
        });
        
        // 选择变化处理
//        toolTable.getSelectionModel().addListSelectionListener(e -> {
//            if (!e.getValueIsAdjusting()) {
//                updateButtonStates();
//            }
//        });
//
        // 按钮事件
        addButton.addActionListener(e -> addNewTool());
        editButton.addActionListener(e -> editSelectedTool());
        deleteButton.addActionListener(e -> deleteSelectedTool());
        favoriteButton.addActionListener(e -> toggleFavorite());
        
        // 搜索事件
        searchField.addCaretListener(e -> filterTable());
        searchColumnFilter.addActionListener(e -> filterTable());
        categoryFilter.addActionListener(e -> filterTable());
    }
    
    /**
     * 加载数据
     */
    public void loadData() {
        try {
            // 先加载原始数据到表格
            List<HttpToolCommand> toolCommands = ToolController.getInstance().getAllToolCommands();
            tableModel.setToolCommands(toolCommands);
            
            // 然后应用当前的筛选条件
            filterTable();
            
        } catch (Exception e) {
            updateStatus("加载失败: " + e.getMessage());
            logError("加载工具数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加新工具
     */
    private void addNewTool() {
        ToolEditDialog dialog = new ToolEditDialog(SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            HttpTool newTool = dialog.getTool();
            String category = dialog.getSelectedCategory();
            
            if (ToolController.getInstance().addTool(newTool, category)) {
                // 重新加载数据以显示新添加的工具命令
                loadData();
                updateStatus("已添加工具: " + newTool.getToolName() + " (分类: " + category + ")");
            } else {
                updateStatus("添加工具失败");
            }
        }
    }
    
    /**
     * 编辑选中工具
     */
    private void editSelectedTool() {
        int selectedRow = toolTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的工具", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        HttpToolCommand toolCommand = tableModel.getToolCommandAt(selectedRow);
        HttpTool tool = toolCommand.getParentTool();
        ToolEditDialog dialog = new ToolEditDialog(SwingUtilities.getWindowAncestor(this), tool);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            HttpTool updatedTool = dialog.getTool();
            String newCategory = dialog.getSelectedCategory();
            
            if (ToolController.getInstance().updateTool(tool, updatedTool, newCategory)) {
                // 重新加载数据以显示更新后的工具命令
                loadData();
                updateStatus("已更新工具: " + updatedTool.getToolName() + " (分类: " + newCategory + ")");
            } else {
                updateStatus("更新工具失败");
            }
        }
    }
    
    /**
     * 删除选中工具
     */
    private void deleteSelectedTool() {
        int selectedRow = toolTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的工具", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        HttpToolCommand toolCommand = tableModel.getToolCommandAt(selectedRow);
        HttpTool tool = toolCommand.getParentTool();
        int result = JOptionPane.showConfirmDialog(this,
            "确定要删除工具 \"" + tool.getToolName() + "\" 吗？\n注意：这将删除该工具的所有命令。",
            "确认删除",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (result == JOptionPane.YES_OPTION) {
            if (ToolController.getInstance().removeTool(tool)) {
                // 重新加载数据以反映删除操作
                loadData();
                updateStatus("已删除工具: " + tool.getToolName());
            } else {
                updateStatus("删除工具失败");
            }
        }
    }
    
    /**
     * 切换收藏状态
     */
    private void toggleFavorite() {
        int selectedRow = toolTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择工具", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        HttpToolCommand toolCommand = tableModel.getToolCommandAt(selectedRow);
        HttpTool tool = toolCommand.getParentTool();
        boolean newFavoriteState = !tool.isFavor();
        
        if (ToolController.getInstance().updateToolFavorite(tool, newFavoriteState)) {
            tool.setFavor(newFavoriteState);
            toolCommand.setFavor(newFavoriteState);
            tableModel.fireTableDataChanged();
            
            String status = tool.isFavor() ? "已收藏" : "已取消收藏";
            updateStatus(status + ": " + tool.getToolName());
        } else {
            updateStatus("更新收藏状态失败");
        }
    }
    
    /**
     * 过滤表格
     */
    private void filterTable() {
        String searchText = searchField.getText().toLowerCase().trim();
        String selectedSearchColumn = (String) searchColumnFilter.getSelectedItem();
        String selectedCategory = (String) categoryFilter.getSelectedItem();
        
        // 获取原始数据
        List<HttpToolCommand> allToolCommands = ToolController.getInstance().getAllToolCommands();
        List<HttpToolCommand> filteredCommands = new ArrayList<>();
        
        for (HttpToolCommand toolCommand : allToolCommands) {
            boolean matchesSearch = false;
            boolean matchesCategory = false;
            
            // 检查搜索条件
            if (searchText.isEmpty()) {
                matchesSearch = true;
            } else {
                switch (selectedSearchColumn) {
                    case "全部":
                        matchesSearch = toolCommand.getDisplayName().toLowerCase().contains(searchText) ||
                                       toolCommand.getCommand().toLowerCase().contains(searchText) ||
                                       toolCommand.getCategory().toLowerCase().contains(searchText);
                        break;
                    case "工具名称":
                        matchesSearch = toolCommand.getDisplayName().toLowerCase().contains(searchText);
                        break;
                    case "命令":
                        matchesSearch = toolCommand.getCommand().toLowerCase().contains(searchText);
                        break;
                    case "分类":
                        matchesSearch = toolCommand.getCategory().toLowerCase().contains(searchText);
                        break;
                    default:
                        matchesSearch = true;
                        break;
                }
            }
            
            // 检查分类过滤条件
            if ("全部".equals(selectedCategory)) {
                matchesCategory = true;
            } else {
                matchesCategory = toolCommand.getCategory().equals(selectedCategory);
            }
            
            // 同时满足搜索和分类条件的记录才会被显示
            if (matchesSearch && matchesCategory) {
                filteredCommands.add(toolCommand);
            }
        }
        
        // 更新表格数据
        tableModel.setToolCommands(filteredCommands);
        
        // 更新状态信息
        String statusMsg = String.format("显示 %d/%d 条记录", 
                                        filteredCommands.size(), 
                                        allToolCommands.size());
        if (!searchText.isEmpty()) {
            statusMsg += " | 搜索: " + searchText + " (范围: " + selectedSearchColumn + ")";
        }
        if (!"全部".equals(selectedCategory)) {
            statusMsg += " | 分类: " + selectedCategory;
        }
        updateStatus(statusMsg);
    }
    
    /**
     * 更新按钮状态
     */
    private void updateButtonStates() {
        boolean hasSelection = toolTable.getSelectedRow() != -1;
        editButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
        favoriteButton.setEnabled(hasSelection);
    }
    
    /**
     * 显示右键菜单
     * @param e 鼠标事件
     */
    private void showContextMenu(MouseEvent e) {
        int row = toolTable.rowAtPoint(e.getPoint());
        if (row >= 0) {
            toolTable.setRowSelectionInterval(row, row);
            
            JPopupMenu contextMenu = new JPopupMenu();
            
            // 执行工具命令
            JMenuItem executeItem = new JMenuItem("执行命令");
            executeItem.addActionListener(event -> {
                HttpToolCommand toolCommand = tableModel.getToolCommandAt(toolTable.getSelectedRow());
                // TODO: 实现命令执行功能
                updateStatus("执行命令: " + toolCommand.getDisplayName());
            });
            contextMenu.add(executeItem);
            
            contextMenu.addSeparator();
            
            // 编辑
            JMenuItem editItem = new JMenuItem("编辑工具");
            editItem.addActionListener(event -> editSelectedTool());
            contextMenu.add(editItem);
            
            // 收藏/取消收藏
            HttpToolCommand toolCommand = tableModel.getToolCommandAt(row);
            String favoriteText = toolCommand.isFavor() ? "取消收藏" : "收藏工具";
            JMenuItem favoriteItem = new JMenuItem(favoriteText);
            favoriteItem.addActionListener(event -> toggleFavorite());
            contextMenu.add(favoriteItem);
            
            contextMenu.addSeparator();
            
            // 复制命令
            JMenuItem copyItem = new JMenuItem("复制命令");
            copyItem.addActionListener(event -> {
                HttpToolCommand cmd = tableModel.getToolCommandAt(toolTable.getSelectedRow());
                java.awt.datatransfer.StringSelection stringSelection = 
                    new java.awt.datatransfer.StringSelection(cmd.getCommand());
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(stringSelection, null);
                updateStatus("已复制命令到剪贴板: " + cmd.getDisplayName());
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
     * 动态加载分类选项
     */
    private void loadCategoryOptions() {
        try {
            List<String> categories = ToolController.getInstance().getAllHttpToolCategories();
            categoryFilter.removeAllItems();
            for (String category : categories) {
                categoryFilter.addItem(category);
            }
            categoryFilter.setSelectedIndex(0); // 默认选中"全部"
        } catch (Exception e) {
            // 如果加载失败，使用默认选项
            I18nManager i18n = I18nManager.getInstance();
            categoryFilter.removeAllItems();
            categoryFilter.addItem(i18n.getText("filter.all"));
            categoryFilter.addItem(i18n.getText("tools.category.sql.injection"));
            categoryFilter.addItem(i18n.getText("tools.category.xss"));
            categoryFilter.addItem(i18n.getText("tools.category.directory.scan"));
            categoryFilter.addItem(i18n.getText("tools.category.vulnerability.scan"));
            categoryFilter.addItem(i18n.getText("tools.category.brute.force"));
            logError("加载分类选项失败: " + e.getMessage());
        }
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
        // 委托给控制器处理日志
        System.err.println("ToolPanel: " + message);
    }
    
    /**
     * 语言变更监听器实现
     * @param newLanguage 新的语言
     */
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
        }
        
        // 更新搜索范围下拉框选项
        updateSearchColumnFilter();
        
        // 重新加载分类选项（可能包含国际化的默认分类）
        loadCategoryOptions();
    }
    
    /**
     * 更新搜索范围下拉框选项
     */
    private void updateSearchColumnFilter() {
        if (searchColumnFilter != null) {
            I18nManager i18n = I18nManager.getInstance();
            
            // 保存当前选中的索引
            int selectedIndex = searchColumnFilter.getSelectedIndex();
            
            // 移除所有项目
            searchColumnFilter.removeAllItems();
            
            // 添加新的国际化项目
            searchColumnFilter.addItem(i18n.getText("filter.all"));
            searchColumnFilter.addItem(i18n.getText("tools.tool.name"));
            searchColumnFilter.addItem(i18n.getText("tools.command"));
            searchColumnFilter.addItem(i18n.getText("label.category"));
            
            // 恢复选中状态
            if (selectedIndex >= 0 && selectedIndex < searchColumnFilter.getItemCount()) {
                searchColumnFilter.setSelectedIndex(selectedIndex);
            }
        }
    }

}

/**
 * 工具表格模型
 */
class ToolTableModel extends AbstractTableModel {
    private String[] columnNames;
    private List<HttpToolCommand> toolCommands = new ArrayList<>();
    
    public ToolTableModel() {
        updateColumnNames();
    }
    
    public void updateColumnNames() {
        I18nManager i18n = I18nManager.getInstance();
        columnNames = new String[]{
            i18n.getText("tools.tool.name"), 
            i18n.getText("tools.command"), 
            i18n.getText("column.favorite"), 
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
        HttpToolCommand toolCommand = toolCommands.get(rowIndex);
        switch (columnIndex) {
            case 0: return toolCommand.getDisplayName();
            case 1: return toolCommand.getCommand();
            case 2: return toolCommand.isFavor();
            case 3: return toolCommand.getCategory();
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
        return columnIndex == 2; // 只有收藏列可以直接编辑
    }
    
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (columnIndex == 2) {
            toolCommands.get(rowIndex).setFavor((Boolean) value);
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
    
    public void setToolCommands(List<HttpToolCommand> toolCommands) {
        this.toolCommands = new ArrayList<>(toolCommands);
        fireTableDataChanged();
    }
    
    public void addToolCommand(HttpToolCommand toolCommand) {
        toolCommands.add(toolCommand);
        fireTableRowsInserted(toolCommands.size() - 1, toolCommands.size() - 1);
    }
    
    public void updateToolCommand(int index, HttpToolCommand toolCommand) {
        toolCommands.set(index, toolCommand);
        fireTableRowsUpdated(index, index);
    }
    
    public void removeToolCommand(int index) {
        toolCommands.remove(index);
        fireTableRowsDeleted(index, index);
    }
    
    public HttpToolCommand getToolCommandAt(int index) {
        return toolCommands.get(index);
    }
    
    public List<HttpToolCommand> getToolCommands() {
        return new ArrayList<>(toolCommands);
    }

}

/**
 * 收藏状态渲染器
 */
class FavoriteRenderer extends DefaultTableCellRenderer {
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
        }
        
        return label;
    }
}

/**
 * 纯文本渲染器 - 命令列专用
 */
class PlainTextRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, 
            boolean isSelected, boolean hasFocus, int row, int column) {
        
        Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        if (value != null) {
            String command = value.toString();
            
            // 设置为等宽字体，更适合显示命令
            setFont(new Font("Consolas", Font.PLAIN, 10));
            
            // 显示完整命令作为提示
            setToolTipText(command);
            
            // 如果命令太长，截断显示
            if (command.length() > 130) {
                setText(command.substring(0, 57) + "...");
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
 * 交替行颜色渲染器
 */
class AlternateRowRenderer extends DefaultTableCellRenderer {
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