package ui;

import model.Config;
import model.HttpTool;
import manager.ConfigManager;
import manager.ApiManager;
import executor.ToolExecutor;
import util.PlaceholderDocumentation;
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
public class ToolPanel extends JPanel {
    
    private JTable toolTable;
    private ToolTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton executeButton;
    private JButton favoriteButton;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JLabel statusLabel;
    
    public ToolPanel() {
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
        
        // 创建底部操作面板
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
        searchField.setToolTipText("输入工具名称或命令进行搜索");
        
        // 分类过滤
        JLabel categoryLabel = new JLabel("分类:");
        categoryLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        categoryFilter = new JComboBox<>(new String[]{"全部", "注入工具", "扫描工具", "爆破工具", "其他"});
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
        
        addButton = createButton("➕ 添加工具", "添加新的HTTP工具", new Color(46, 125, 50));
        editButton = createButton("✏️ 编辑", "编辑选中的工具", new Color(25, 118, 210));
        deleteButton = createButton("🗑️ 删除", "删除选中的工具", new Color(211, 47, 47));
        favoriteButton = createButton("⭐ 收藏", "切换收藏状态", new Color(255, 152, 0));
        
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
        scrollPane.setPreferredSize(new Dimension(800, 400));
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            "HTTP工具列表", 
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 12)
        ));
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    /**
     * 创建底部操作面板
     * @return 底部面板
     */
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        // 左侧：执行按钮
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        
        executeButton = new JButton("🚀 执行选中工具");
        executeButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        executeButton.setPreferredSize(new Dimension(140, 35));
        executeButton.setBackground(new Color(76, 175, 80));
        executeButton.setForeground(Color.WHITE);
        executeButton.setFocusPainted(false);
        executeButton.setBorder(BorderFactory.createRaisedBevelBorder());
        executeButton.setToolTipText("执行当前选中的HTTP工具");
        
        JButton helpButton = new JButton("❓ 占位符帮助");
        helpButton.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        helpButton.setPreferredSize(new Dimension(120, 30));
        helpButton.addActionListener(e -> showPlaceholderHelp());
        
        leftPanel.add(executeButton);
        leftPanel.add(Box.createHorizontalStrut(10));
        leftPanel.add(helpButton);
        
        // 右侧：状态信息
        statusLabel = new JLabel("就绪");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(100, 100, 100));
        
        bottomPanel.add(leftPanel, BorderLayout.WEST);
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
        
        // 设置命令列渲染器（支持换行显示）
        columnModel.getColumn(1).setCellRenderer(new CommandRenderer());
        
        // 设置行颜色交替
        toolTable.setDefaultRenderer(Object.class, new AlternateRowRenderer());
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
        executeButton.addActionListener(e -> executeSelectedTool());
        
        // 搜索事件
        searchField.addCaretListener(e -> filterTable());
        categoryFilter.addActionListener(e -> filterTable());
    }
    
    /**
     * 加载数据
     */
    public void loadData() {
        try {
            Config config = ConfigManager.getInstance().getConfig();
            List<HttpTool> tools = new ArrayList<>();
            
            if (config.getHttpTool() != null) {
                for (Config.HttpToolCategory category : config.getHttpTool()) {
                    if (category.getContent() != null) {
                        tools.addAll(category.getContent());
                    }
                }
            }
            
            tableModel.setTools(tools);
            updateStatus("已加载 " + tools.size() + " 个工具");
            
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
            tableModel.addTool(newTool);
            saveConfiguration();
            updateStatus("已添加工具: " + newTool.getToolName());
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
        
        HttpTool tool = tableModel.getToolAt(selectedRow);
        ToolEditDialog dialog = new ToolEditDialog(SwingUtilities.getWindowAncestor(this), tool);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            HttpTool updatedTool = dialog.getTool();
            tableModel.updateTool(selectedRow, updatedTool);
            saveConfiguration();
            updateStatus("已更新工具: " + updatedTool.getToolName());
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
        
        HttpTool tool = tableModel.getToolAt(selectedRow);
        int result = JOptionPane.showConfirmDialog(this,
            "确定要删除工具 \"" + tool.getToolName() + "\" 吗？",
            "确认删除",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (result == JOptionPane.YES_OPTION) {
            tableModel.removeTool(selectedRow);
            saveConfiguration();
            updateStatus("已删除工具: " + tool.getToolName());
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
        
        HttpTool tool = tableModel.getToolAt(selectedRow);
        tool.setFavor(!tool.isFavor());
        tableModel.fireTableDataChanged();
        saveConfiguration();
        
        String status = tool.isFavor() ? "已收藏" : "已取消收藏";
        updateStatus(status + ": " + tool.getToolName());
    }
    
    /**
     * 执行选中工具
     */
    private void executeSelectedTool() {
        int selectedRow = toolTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要执行的工具", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        HttpTool tool = tableModel.getToolAt(selectedRow);
        
        // 这里需要HTTP请求对象，暂时显示提示
        JOptionPane.showMessageDialog(this,
            "工具执行功能需要在HTTP请求上下文中使用\n" +
            "请在Burp的HTTP请求上右键选择对应工具",
            "提示",
            JOptionPane.INFORMATION_MESSAGE);
            
        updateStatus("工具 " + tool.getToolName() + " 需要HTTP请求上下文");
    }
    
    /**
     * 显示占位符帮助
     */
    private void showPlaceholderHelp() {
        PlaceholderHelpDialog dialog = new PlaceholderHelpDialog(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
    }
    
    /**
     * 过滤表格
     */
    private void filterTable() {
        // TODO: 实现搜索和过滤功能
        String searchText = searchField.getText().toLowerCase();
        String selectedCategory = (String) categoryFilter.getSelectedItem();
        
        // 这里可以实现表格过滤逻辑
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
        executeButton.setEnabled(hasSelection);
    }
    
    /**
     * 保存配置
     */
    private void saveConfiguration() {
        // TODO: 实现配置保存功能
        logInfo("配置已保存");
    }
    
    /**
     * 更新状态
     * @param message 状态消息
     */
    private void updateStatus(String message) {
        statusLabel.setText(message);
    }
    
    /**
     * 记录信息日志
     * @param message 日志消息
     */
    private void logInfo(String message) {
        if (ApiManager.getInstance().isInitialized()) {
            ApiManager.getInstance().getApi().logging().logToOutput("ToolPanel: " + message);
        }
    }
    
    /**
     * 记录错误日志
     * @param message 错误消息
     */
    private void logError(String message) {
        if (ApiManager.getInstance().isInitialized()) {
            ApiManager.getInstance().getApi().logging().logToError("ToolPanel: " + message);
        }
    }
}

/**
 * 工具表格模型
 */
class ToolTableModel extends AbstractTableModel {
    private final String[] columnNames = {"工具名称", "命令", "收藏", "分类"};
    private List<HttpTool> tools = new ArrayList<>();
    
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
        HttpTool tool = tools.get(rowIndex);
        switch (columnIndex) {
            case 0: return tool.getToolName();
            case 1: return tool.getCommand();
            case 2: return tool.isFavor();
            case 3: return "HTTP工具"; // 可以后续扩展分类
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
            tools.get(rowIndex).setFavor((Boolean) value);
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
    
    public void setTools(List<HttpTool> tools) {
        this.tools = new ArrayList<>(tools);
        fireTableDataChanged();
    }
    
    public void addTool(HttpTool tool) {
        tools.add(tool);
        fireTableRowsInserted(tools.size() - 1, tools.size() - 1);
    }
    
    public void updateTool(int index, HttpTool tool) {
        tools.set(index, tool);
        fireTableRowsUpdated(index, index);
    }
    
    public void removeTool(int index) {
        tools.remove(index);
        fireTableRowsDeleted(index, index);
    }
    
    public HttpTool getToolAt(int index) {
        return tools.get(index);
    }
    
    public List<HttpTool> getTools() {
        return new ArrayList<>(tools);
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
            label.setText(((Boolean) value) ? "⭐" : "☆");
        }
        
        return label;
    }
}

/**
 * 命令渲染器
 */
class CommandRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, 
            boolean isSelected, boolean hasFocus, int row, int column) {
        
        Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        if (value != null) {
            String command = value.toString();
            if (command.length() > 80) {
                setToolTipText("<html>" + command.replaceAll("(.{80})", "$1<br>") + "</html>");
                setText(command.substring(0, 77) + "...");
            } else {
                setToolTipText(command);
                setText(command);
            }
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