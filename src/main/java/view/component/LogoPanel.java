package view.component;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * Logo面板组件
 * 用于显示BpArsenal插件的Logo图标
 */
public class LogoPanel extends JPanel {
    private Image logoImage;
    private static final int LOGO_SIZE = 24; // Logo大小

    public LogoPanel(String path) {
        try {
            // 加载图片资源
            logoImage = new ImageIcon(path).getImage();
        } catch (Exception e) {
            logoImage = null;
            System.err.println("Logo图片加载失败: " + path + ", 错误: " + e.getMessage());
        }
        
        // 设置面板大小
        setPreferredSize(new Dimension(LOGO_SIZE, LOGO_SIZE));
        setOpaque(false); // 透明背景
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (logoImage != null) {
            // 绘制Logo图片，保持原始比例居中显示
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            int imgWidth = logoImage.getWidth(this);
            int imgHeight = logoImage.getHeight(this);
            
            if (imgWidth > 0 && imgHeight > 0) {
                // 计算缩放比例，保持宽高比
                double scale = Math.min((double) getWidth() / imgWidth, (double) getHeight() / imgHeight);
                int scaledWidth = (int) (imgWidth * scale);
                int scaledHeight = (int) (imgHeight * scale);
                
                // 居中绘制
                int x = (getWidth() - scaledWidth) / 2;
                int y = (getHeight() - scaledHeight) / 2;
                
                g2d.drawImage(logoImage, x, y, scaledWidth, scaledHeight, this);
            }
            
            g2d.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("背景图示例");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ClassLoader classLoader = LogoPanel.class.getClassLoader();
            f.setContentPane(new LogoPanel("D:\\programming\\武器开发\\burpExtend\\BpArsenal\\src\\main\\resources\\logo.ico")); // 换成你自己的路径
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}