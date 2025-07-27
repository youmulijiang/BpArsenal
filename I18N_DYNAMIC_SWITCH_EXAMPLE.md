# BpArsenal 动态语言切换功能示例

## 功能特点

✅ **动态语言切换** - 无需重启插件，语言立即生效  
✅ **默认中文界面** - 插件启动时默认显示中文界面  
✅ **完整国际化支持** - 所有UI组件都支持多语言  
✅ **实时更新** - 所有面板和对话框同步更新语言  

## 使用步骤

### 1. 启动插件
插件启动时默认显示中文界面，所有文本都是中文。

### 2. 切换语言
1. 打开 **设置** 选项卡
2. 找到 **语言设置** 面板
3. 在下拉框中选择 **English (US)**
4. 点击 **应用语言设置** 按钮
5. 界面立即切换为英文，无需重启

### 3. 验证切换效果
- 主面板选项卡标题变为英文
- 所有按钮文本变为英文
- 对话框标题和内容变为英文
- 状态信息变为英文

## 支持的组件

### 主界面组件
- [x] 主面板标题和选项卡
- [x] 设置面板所有控件
- [x] 按钮和标签文本
- [x] 状态和提示信息

### 对话框组件
- [x] Arsenal工具选择对话框
- [x] 命令执行对话框
- [x] 工具编辑对话框
- [x] 网站编辑对话框
- [x] 第三方工具编辑对话框
- [x] 占位符帮助对话框

### 其他组件
- [x] 文件选择器
- [x] 输入验证消息
- [x] 错误和成功提示
- [x] 工具提示文本

## 技术实现

### 核心机制
```java
// 语言变更监听器接口
public interface LanguageChangeListener {
    void onLanguageChanged(Locale newLocale);
}

// 组件实现监听器
public class SettingPanel extends JPanel implements I18nManager.LanguageChangeListener {
    @Override
    public void onLanguageChanged(Locale newLocale) {
        SwingUtilities.invokeLater(() -> {
            updateUITexts();
            revalidate();
            repaint();
        });
    }
}
```

### 动态更新流程
1. 用户选择新语言
2. `I18nManager.setLocale()` 被调用
3. 所有注册的监听器收到通知
4. 各组件在EDT线程中更新UI文本
5. 界面重新绘制，显示新语言

### 资源管理
- 资源包使用 `ConcurrentHashMap` 缓存
- 支持延迟加载和动态切换
- 自动fallback到默认语言

## 使用截图描述

### 中文界面（默认）
```
┌─────────────────────────────────────┐
│ BpArsenal 武器库                v1.0.0│
├─────────────────────────────────────┤
│ HTTP工具 | 第三方工具 | 网站导航 | 设置 │
├─────────────────────────────────────┤
│ 语言设置                             │
│ 界面语言: [中文 (简体) ▼] [应用语言设置] │
│ 语言状态: 当前语言: 中文 (简体)          │
└─────────────────────────────────────┘
```

### 英文界面（切换后）
```
┌─────────────────────────────────────┐
│ BpArsenal Weapon Arsenal        v1.0.0│
├─────────────────────────────────────┤
│ HTTP Tools | Third-party | Websites | Settings │
├─────────────────────────────────────┤
│ Language Settings                    │
│ Interface Language: [English (US) ▼] [Apply Language Settings] │
│ Language Status: Settings applied successfully │
└─────────────────────────────────────┘
```

## 扩展新语言

### 1. 添加资源文件
```
src/main/resources/
├── messages_ja_JP.properties  # 日语
├── messages_ko_KR.properties  # 韩语
├── messages_fr_FR.properties  # 法语
```

### 2. 更新I18nManager
```java
public static final Locale JAPANESE = new Locale("ja", "JP");
public static final Locale KOREAN = new Locale("ko", "KR");
public static final Locale FRENCH = new Locale("fr", "FR");

public Locale[] getSupportedLocales() {
    return new Locale[]{CHINESE, ENGLISH, JAPANESE, KOREAN, FRENCH};
}
```

### 3. 添加语言检查
```java
public boolean isSupportedLocale(Locale locale) {
    return Arrays.asList(getSupportedLocales()).contains(locale);
}
```

## 性能优化

### 缓存机制
- 资源包只在首次使用时加载
- 使用线程安全的缓存容器
- 支持运行时清理缓存

### 更新策略
- 只更新可见的UI组件
- 使用EDT线程确保线程安全
- 批量更新减少重绘次数

### 内存管理
- 监听器使用弱引用避免内存泄漏
- 及时移除不需要的监听器
- 资源包按需加载和卸载

## 故障排除

### 常见问题

**Q: 某些文本没有切换语言**  
A: 检查该文本是否在资源文件中定义，确保使用了 `I18nManager.getText()` 方法。

**Q: 切换语言后界面布局混乱**  
A: 在 `onLanguageChanged` 方法中添加 `revalidate()` 和 `repaint()` 调用。

**Q: 新添加的组件不支持动态切换**  
A: 确保组件实现了 `LanguageChangeListener` 接口并注册了监听器。

### 调试技巧

1. **检查日志输出**
   ```java
   I18nManager.getInstance().hasKey("your.key");
   ```

2. **验证监听器注册**
   ```java
   // 查看当前注册的监听器数量
   // 在日志中会显示："已通知 X 个监听器语言变更"
   ```

3. **测试资源文件**
   ```java
   // 手动测试特定键值
   String text = I18nManager.getInstance().getText("test.key");
   ```

## 最佳实践

1. **统一键值命名** - 使用有意义的层级结构
2. **完整覆盖** - 确保所有用户可见文本都国际化
3. **测试验证** - 在所有支持的语言下测试界面
4. **性能考虑** - 避免在循环中频繁调用getText()
5. **错误处理** - 为缺失的键值提供合理的fallback

## 总结

BpArsenal的动态语言切换功能提供了流畅的多语言用户体验，支持实时切换，无需重启插件。通过完善的监听器机制和资源管理，确保了功能的稳定性和性能。 