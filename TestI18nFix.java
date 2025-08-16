import util.I18nManager;

/**
 * 测试国际化占位符修复
 */
public class TestI18nFix {
    public static void main(String[] args) {
        I18nManager i18n = I18nManager.getInstance();
        
        // 测试占位符渲染
        System.out.println("测试占位符渲染:");
        
        // 测试 {0}/{1} 格式的占位符
        String titlePattern = i18n.getText("arsenal.dialog.title.pattern", "5", "10");
        System.out.println("标题模式: " + titlePattern);
        
        // 测试 {0}, {1}, {2} 格式的占位符
        String copySuccessPattern = i18n.getText("arsenal.dialog.copy.success.pattern", 
            "渲染命令", "sqlmap", "150");
        System.out.println("复制成功模式: " + copySuccessPattern);
        
        // 测试单个参数
        String exitCodePattern = i18n.getText("arsenal.dialog.exit.code", "0");
        System.out.println("退出码模式: " + exitCodePattern);
        
        // 测试无参数的情况
        String simpleText = i18n.getText("button.confirm");
        System.out.println("简单文本: " + simpleText);
    }
}
