package view.component;

import util.PlaceholderDocumentation;
import util.I18nManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 占位符帮助对话框
 * 显示所有可用的HTTP占位符变量
 */
public class PlaceholderHelpDialog extends JDialog implements I18nManager.LanguageChangeListener {
    
    private JTextArea documentationArea;
    private JButton closeButton;
    
    public PlaceholderHelpDialog(Window parent) {
        super(parent);
        
        // 注册语言变更监听器
        I18nManager.getInstance().addLanguageChangeListener(this);
        
        initializeUI();
        setupEventHandlers();
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    /**
     * 初始化UI组件
     */
    private void initializeUI() {
        I18nManager i18n = I18nManager.getInstance();
        
        // 设置对话框标题和属性
        setTitle(i18n.getText("placeholder.help.dialog.title"));
        setModalityType(ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(10, 10));
        setSize(900, 700);
        setResizable(true);
        
        // 创建主面板
        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);
        
        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 设置默认按钮
        getRootPane().setDefaultButton(closeButton);
    }
    
    /**
     * 创建主面板
     */
    private JPanel createMainPanel() {
        I18nManager mainI18n = I18nManager.getInstance();
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        
        // 创建标题标签
        JLabel titleLabel = new JLabel(mainI18n.getText("placeholder.help.dialog.description"));
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // 创建文档显示区域
        documentationArea = new JTextArea();
        documentationArea.setEditable(false);
//        documentationArea.setFont(getUnicodeFont(11));
        documentationArea.setBackground(new Color(248, 248, 248));
        documentationArea.setLineWrap(false);
        documentationArea.setWrapStyleWord(false);
        
        // 确保正确显示UTF-8编码的中文
        setupTextAreaForUTF8(documentationArea);
        
        // 设置文档内容
        documentationArea.setText(PlaceholderDocumentation.getAllPlaceholderDocumentation());
        
        // 添加到滚动面板
        JScrollPane scrollPane = new JScrollPane(documentationArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createTitledBorder(mainI18n.getText("placeholder.help.dialog.border.documentation")));
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    /**
     * 创建按钮面板
     */
    private JPanel createButtonPanel() {
        I18nManager buttonI18n = I18nManager.getInstance();
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        
        closeButton = new JButton(buttonI18n.getText("placeholder.help.dialog.button.close"));
        closeButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        closeButton.setPreferredSize(new Dimension(80, 30));
        closeButton.setBackground(new Color(108, 117, 125));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        
        buttonPanel.add(closeButton);
        
        return buttonPanel;
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        closeButton.addActionListener(e -> dispose());
        
        // ESC键关闭对话框
        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }
    
    /**
     * 获取支持Unicode的字体
     * @param size 字体大小
     * @return 字体对象
     */
    private Font getUnicodeFont(int size) {
        // 优先尝试等宽字体，支持中文
        String[] fontNames = {
            "JetBrains Mono",     // 现代等宽字体
            "Consolas",           // Windows等宽字体
            "Monaco",             // macOS等宽字体
            "DejaVu Sans Mono",   // Linux等宽字体
            "Courier New",        // 通用等宽字体
            "Microsoft YaHei",    // 中文支持字体
            "SimSun",             // 宋体
            "Dialog"              // Java默认字体
        };
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] availableFonts = ge.getAvailableFontFamilyNames();
        
        for (String fontName : fontNames) {
            for (String availableFont : availableFonts) {
                if (availableFont.equals(fontName)) {
                    return new Font(fontName, Font.PLAIN, size);
                }
            }
        }
        
        // 如果都没找到，使用系统默认等宽字体
        return new Font(Font.MONOSPACED, Font.PLAIN, size);
    }
    
    /**
     * 设置JTextArea以正确显示UTF-8编码的文本
     * @param textArea 文本区域组件
     */
    private void setupTextAreaForUTF8(JTextArea textArea) {
        // 确保使用UTF-8字符集
        textArea.putClientProperty("charset", "UTF-8");
        
        // 设置字符编码相关属性
        textArea.setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
        
        // 如果有必要，可以设置字符输入方法
        try {
            textArea.getInputContext().selectInputMethod(java.util.Locale.getDefault());
        } catch (Exception e) {
            // 忽略输入法设置失败的情况
        }
    }
    
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
        
        // 更新对话框标题
        setTitle(i18n.getText("placeholder.help.dialog.title"));
        
        // 更新按钮文本
        if (closeButton != null) {
            closeButton.setText(i18n.getText("placeholder.help.dialog.button.close"));
        }
        
        // 重新生成文档内容（如果有国际化需要）
        if (documentationArea != null) {
            documentationArea.setText(PlaceholderDocumentation.getAllPlaceholderDocumentation());
        }
    }
} 