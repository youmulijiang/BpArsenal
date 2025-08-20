import javax.swing.*;
import java.awt.*;

/**
 * 演示：带有 icon 的 JLabel 示例
 * 运行环境：JDK 8+
 */
public class JLabelWithIconDemo {

    private static final String ICON_PATH = "java-icon.png"; // 换成你自己的图标路径
    private static final String TEXT      = "Hello Swing";

    public static void main(String[] args) {
        /* 所有 Swing 组件必须在 EDT（事件派发线程）中创建 */
        SwingUtilities.invokeLater(JLabelWithIconDemo::createAndShowGui);
    }

    private static void createAndShowGui() {
        /* 1. 创建窗体 */
        JFrame frame = new JFrame("JLabel with Icon 示例");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);
        frame.setLocationRelativeTo(null); // 居中

        /* 2. 创建 JLabel，并设置文本、图标、水平对齐方式 */
        ImageIcon icon = loadIcon(ICON_PATH);
        JLabel label = new JLabel(TEXT, icon, SwingConstants.LEFT); // 图标在左，文本在右
        label.setHorizontalTextPosition(SwingConstants.RIGHT);      // 文本相对图标的位置
        label.setIconTextGap(10);                                   // 图标与文本间距
        label.setFont(new Font("SansSerif", Font.PLAIN, 16));

        /* 3. 把 JLabel 放到内容面板里（默认 BorderLayout） */
        frame.getContentPane().add(label, BorderLayout.CENTER);

        /* 4. 显示窗体 */
        frame.setVisible(true);
    }

    /**
     * 从磁盘加载图标，如果不存在则返回 null
     */
    private static ImageIcon loadIcon(String path) {
        java.net.URL url = JLabelWithIconDemo.class.getResource(path);
        if (url != null) {
            return new ImageIcon(url);
        }
        // 尝试文件系统
        java.io.File file = new java.io.File(path);
        if (file.exists()) {
            return new ImageIcon(file.getAbsolutePath());
        }
        System.err.println("图标文件未找到: " + path);
        return null;
    }
}