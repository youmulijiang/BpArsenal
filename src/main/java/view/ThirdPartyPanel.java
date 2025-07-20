package view;

import model.ThirdPartyTool;
import controller.ToolController;
import view.component.ThirdPartyToolEditDialog;
import executor.ToolExecutor;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * 第三方工具面板 (View层)
 * 用于管理和启动外部工具
 */
public class ThirdPartyPanel extends JPanel {
    
    private JTable toolTable;
    private ThirdPartyToolTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton favoriteButton;
    private JButton launchButton;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JLabel statusLabel;
    
    public ThirdPartyPanel() {
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
        searchField.setToolTipText("输入工具名称或启动命令进行搜索");
        
        // 分类过滤
        JLabel categoryLabel = new JLabel("分类:");
        categoryLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        categoryFilter = new JComboBox<>(new String[]{"全部", "渗透工具", "编辑器", "网络工具", "分析工具", "其他"});
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
        
        addButton = createButton("+ 添加工具", "添加新的第三方工具", new Color(46, 125, 50));
        editButton = createButton("编辑", "编辑选中的工具", new Color(25, 118, 210));
        deleteButton = createButton("删除", "删除选中的工具", new Color(211, 47, 47));
        favoriteButton = createButton("收藏", "切换收藏状态", new Color(255, 152, 0));
        launchButton = createButton("启动", "启动选中的工具", new Color(102, 187, 106));
        
        rightPanel.add(addButton);
        rightPanel.add(editButton);
        rightPanel.add(deleteButton);
        rightPanel.add(favoriteButton);
        rightPanel.add(launchButton);
        
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
        tableModel = new ThirdPartyToolTableModel();
        toolTable = new JTable(tableModel);
        
        // 设置表格属性
        setupTable();
        
        // 创建滚动面板
        JScrollPane scrollPane = new JScrollPane(toolTable);
        scrollPane.setPreferredSize(new Dimension(800, 450));
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            "第三方工具列表", 
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
        columnModel.getColumn(1).setPreferredWidth(350);  // 启动命令
        columnModel.getColumn(2).setPreferredWidth(60);   // 收藏
        columnModel.getColumn(3).setPreferredWidth(80);   // 分类
        columnModel.getColumn(4).setPreferredWidth(80);   // 自启动
        
        // 设置收藏列渲染器
        columnModel.getColumn(2).setCellRenderer(new ThirdPartyFavoriteRenderer());
        
        // 设置自启动列渲染器
        columnModel.getColumn(4).setCellRenderer(new ThirdPartyAutoStartRenderer());
        
        // 设置命令列渲染器（纯文本格式）
        columnModel.getColumn(1).setCellRenderer(new ThirdPartyCommandRenderer());
        
        // 设置行颜色交替
        toolTable.setDefaultRenderer(Object.class, new ThirdPartyAlternateRowRenderer());
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 表格双击编辑
        toolTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedTool();
                }
            }
        });
        
        // 选择变化处理
        toolTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
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
        categoryFilter.addActionListener(e -> filterTable());
    }
    
    /**
     * 加载数据
     */
    public void loadData() {
        try {
            List<ThirdPartyTool> tools = ToolController.getInstance().getAllThirdPartyTools();
            tableModel.setTools(tools);
            updateStatus("已加载 " + tools.size() + " 个第三方工具");
            
        } catch (Exception e) {
            updateStatus("加载失败: " + e.getMessage());
            logError("加载第三方工具数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加新工具
     */
    private void addNewTool() {
        ThirdPartyToolEditDialog dialog = new ThirdPartyToolEditDialog(SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            ThirdPartyTool newTool = dialog.getTool();
            String category = dialog.getSelectedCategory();
            
            if (ToolController.getInstance().addThirdPartyTool(newTool, category)) {
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
        
        ThirdPartyTool tool = tableModel.getToolAt(selectedRow);
        ThirdPartyToolEditDialog dialog = new ThirdPartyToolEditDialog(SwingUtilities.getWindowAncestor(this), tool);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            ThirdPartyTool updatedTool = dialog.getTool();
            String newCategory = dialog.getSelectedCategory();
            
            if (ToolController.getInstance().updateThirdPartyTool(tool, updatedTool, newCategory)) {
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
        
        ThirdPartyTool tool = tableModel.getToolAt(selectedRow);
        int result = JOptionPane.showConfirmDialog(this,
            "确定要删除工具 \"" + tool.getToolName() + "\" 吗？",
            "确认删除",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (result == JOptionPane.YES_OPTION) {
            if (ToolController.getInstance().removeThirdPartyTool(tool)) {
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
        
        ThirdPartyTool tool = tableModel.getToolAt(selectedRow);
        boolean newFavoriteState = !tool.isFavor();
        
        if (ToolController.getInstance().updateThirdPartyToolFavorite(tool, newFavoriteState)) {
            tool.setFavor(newFavoriteState);
            tableModel.fireTableDataChanged();
            
            String status = tool.isFavor() ? "已收藏" : "已取消收藏";
            updateStatus(status + ": " + tool.getToolName());
        } else {
            updateStatus("更新收藏状态失败");
        }
    }
    
    /**
     * 启动选中工具
     */
    private void launchSelectedTool() {
        int selectedRow = toolTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要启动的工具", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        ThirdPartyTool tool = tableModel.getToolAt(selectedRow);
        
        try {
            ToolExecutor.getInstance().executeThirdPartyTool(tool);
            updateStatus("正在启动: " + tool.getToolName() + " - " + tool.getStartCommand());
            
        } catch (Exception e) {
            updateStatus("启动失败: " + e.getMessage());
            logError("启动第三方工具失败: " + e.getMessage());
            
            JOptionPane.showMessageDialog(this,
                "启动工具失败！\n错误: " + e.getMessage(),
                "启动失败",
                JOptionPane.ERROR_MESSAGE);
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
        boolean hasSelection = toolTable.getSelectedRow() != -1;
        editButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
        favoriteButton.setEnabled(hasSelection);
        launchButton.setEnabled(hasSelection);
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
        System.err.println("ThirdPartyPanel: " + message);
    }
}

/**
 * 第三方工具表格模型
 */
class ThirdPartyToolTableModel extends AbstractTableModel {
    private final String[] columnNames = {"工具名称", "启动命令", "收藏", "分类", "自启动"};
    private List<ThirdPartyTool> tools = new ArrayList<>();
    
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
            case 3: return ToolController.getInstance().getThirdPartyToolCategory(tool.getToolName());
            case 4: return tool.isAutoStart();
            default: return null;
        }
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 2 || columnIndex == 4) return Boolean.class;
        return String.class;
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 2 || columnIndex == 4; // 收藏和自启动列可编辑
    }
    
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        ThirdPartyTool tool = tools.get(rowIndex);
        if (columnIndex == 2) {
            tool.setFavor((Boolean) value);
        } else if (columnIndex == 4) {
            tool.setAutoStart((Boolean) value);
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }
    
    public void setTools(List<ThirdPartyTool> tools) {
        this.tools = new ArrayList<>(tools);
        fireTableDataChanged();
    }
    
    public ThirdPartyTool getToolAt(int index) {
        return tools.get(index);
    }
    
    public List<ThirdPartyTool> getTools() {
        return new ArrayList<>(tools);
    }
}

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
            
            // 设置为等宽字体，更适合显示命令
            setFont(new Font("Consolas", Font.PLAIN, 10));
            
            // 显示完整命令作为提示
            setToolTipText("<html><pre>" + command + "</pre></html>");
            
            // 如果命令太长，截断显示
            if (command.length() > 50) {
                setText(command.substring(0, 47) + "...");
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
class ThirdPartyAlternateRowRenderer extends DefaultTableCellRenderer {
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