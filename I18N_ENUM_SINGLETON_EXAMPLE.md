# I18nManager 枚举单例模式使用示例

## 新架构特点

✅ **线程安全的枚举单例** - 使用enum确保线程安全和唯一性  
✅ **强类型语言支持** - 通过SupportedLanguage枚举避免错误  
✅ **简化的API接口** - 提供便捷方法进行语言控制  
✅ **向后兼容性** - 保留旧接口支持现有代码  

## 核心架构

### 枚举单例模式
```java
public enum I18nManager {
    INSTANCE;
    
    // 获取单例实例
    public static I18nManager getInstance() {
        return INSTANCE;
    }
}
```

### 支持的语言枚举
```java
public enum SupportedLanguage {
    CHINESE("zh", "CN", "中文 (简体)", "settings.language.chinese"),
    ENGLISH("en", "US", "English (US)", "settings.language.english");
    
    // 便捷方法
    public static SupportedLanguage fromLocale(Locale locale);
    public static SupportedLanguage fromLanguageTag(String languageTag);
}
```

## 在SettingPanel中的使用示例

### 1. 基本语言切换
```java
// 在SettingPanel中切换语言
I18nManager i18n = I18nManager.getInstance();

// 方法1：直接切换
i18n.switchToChinese();
i18n.switchToEnglish();

// 方法2：使用枚举
i18n.setCurrentLanguage(I18nManager.SupportedLanguage.CHINESE);
i18n.setCurrentLanguage(I18nManager.SupportedLanguage.ENGLISH);

// 方法3：使用索引（适合下拉框）
i18n.setLanguageByIndex(0); // 中文
i18n.setLanguageByIndex(1); // 英文
```

### 2. 下拉框集成示例
```java
public class SettingPanel extends JPanel implements I18nManager.LanguageChangeListener {
    private JComboBox<I18nManager.SupportedLanguage> languageComboBox;
    
    private void initializeLanguageComboBox() {
        languageComboBox = new JComboBox<>();
        
        // 填充支持的语言
        for (I18nManager.SupportedLanguage lang : I18nManager.getInstance().getSupportedLanguages()) {
            languageComboBox.addItem(lang);
        }
        
        // 设置当前选中的语言
        languageComboBox.setSelectedItem(I18nManager.getInstance().getCurrentLanguage());
        
        // 添加选择事件监听器
        languageComboBox.addActionListener(e -> {
            I18nManager.SupportedLanguage selected = 
                (I18nManager.SupportedLanguage) languageComboBox.getSelectedItem();
            if (selected != null) {
                I18nManager.getInstance().setCurrentLanguage(selected);
            }
        });
    }
    
    @Override
    public void onLanguageChanged(I18nManager.SupportedLanguage newLanguage) {
        SwingUtilities.invokeLater(() -> {
            // 更新下拉框选择（避免循环触发事件）
            languageComboBox.removeActionListener(this);
            languageComboBox.setSelectedItem(newLanguage);
            languageComboBox.addActionListener(this);
            
            // 更新UI文本
            updateUITexts();
        });
    }
}
```

### 3. 语言状态检查
```java
I18nManager i18n = I18nManager.getInstance();

// 检查当前语言
if (i18n.isChinese()) {
    // 当前是中文
    System.out.println("当前语言：中文");
} else if (i18n.isEnglish()) {
    // 当前是英文
    System.out.println("Current Language: English");
}

// 获取当前语言信息
I18nManager.SupportedLanguage current = i18n.getCurrentLanguage();
System.out.println("语言：" + current.getDisplayName());
System.out.println("标签：" + current.getLanguageTag());
System.out.println("索引：" + i18n.getCurrentLanguageIndex());
```

## 监听器实现

### 新的监听器接口
```java
public interface LanguageChangeListener {
    void onLanguageChanged(SupportedLanguage newLanguage);
}
```

### 实现示例
```java
public class MyComponent implements I18nManager.LanguageChangeListener {
    
    public MyComponent() {
        // 注册监听器
        I18nManager.getInstance().addLanguageChangeListener(this);
    }
    
    @Override
    public void onLanguageChanged(I18nManager.SupportedLanguage newLanguage) {
        SwingUtilities.invokeLater(() -> {
            updateTexts(newLanguage);
            revalidate();
            repaint();
        });
    }
    
    private void updateTexts(I18nManager.SupportedLanguage language) {
        I18nManager i18n = I18nManager.getInstance();
        
        // 根据语言更新UI文本
        switch (language) {
            case CHINESE:
                titleLabel.setText(i18n.getText("title.chinese"));
                break;
            case ENGLISH:
                titleLabel.setText(i18n.getText("title.english"));
                break;
        }
    }
}
```

## 向后兼容支持

### 旧接口仍然可用
```java
I18nManager i18n = I18nManager.getInstance();

// 旧接口（仍然支持）
Locale[] locales = i18n.getSupportedLocales();
i18n.setLocale(new Locale("zh", "CN"));
Locale current = i18n.getCurrentLocale();

// 新接口（推荐使用）
I18nManager.SupportedLanguage[] languages = i18n.getSupportedLanguages();
i18n.setCurrentLanguage(I18nManager.SupportedLanguage.CHINESE);
I18nManager.SupportedLanguage currentLang = i18n.getCurrentLanguage();
```

## 线程安全特性

### 枚举单例的优势
1. **懒加载**：类加载时自动初始化，无需担心多线程问题
2. **序列化安全**：枚举天然支持序列化，不会创建新实例
3. **反射安全**：无法通过反射创建新实例
4. **线程安全**：JVM保证枚举的线程安全性

### 并发控制
```java
// setCurrentLanguage方法使用synchronized确保原子性
public synchronized void setCurrentLanguage(SupportedLanguage language) {
    // 线程安全的语言切换
}

// 使用volatile确保可见性
private volatile SupportedLanguage currentLanguage;
private volatile ResourceBundle currentBundle;
```

## 性能优化

### 缓存策略
```java
// 使用ConcurrentHashMap缓存资源包
private final ConcurrentMap<SupportedLanguage, ResourceBundle> bundleCache = new ConcurrentHashMap<>();

// 按需加载资源包
private ResourceBundle getResourceBundle(SupportedLanguage language) {
    return bundleCache.computeIfAbsent(language, lang -> {
        return ResourceBundle.getBundle(BUNDLE_BASE_NAME, lang.getLocale());
    });
}
```

### 监听器管理
```java
// 使用CopyOnWriteArrayList确保线程安全
private final List<LanguageChangeListener> listeners = new CopyOnWriteArrayList<>();
```

## 错误处理和回退

### 资源包加载失败处理
```java
private ResourceBundle getResourceBundle(SupportedLanguage language) {
    return bundleCache.computeIfAbsent(language, lang -> {
        try {
            return ResourceBundle.getBundle(BUNDLE_BASE_NAME, lang.getLocale());
        } catch (Exception e) {
            // 加载失败时回退到中文
            if (lang != SupportedLanguage.CHINESE) {
                try {
                    return ResourceBundle.getBundle(BUNDLE_BASE_NAME, SupportedLanguage.CHINESE.getLocale());
                } catch (Exception ex) {
                    throw new RuntimeException("无法加载任何资源包", ex);
                }
            }
            throw new RuntimeException("无法加载任何资源包", e);
        }
    });
}
```

### 文本获取失败处理
```java
public String getText(String key) {
    try {
        return currentBundle.getString(key);
    } catch (Exception e) {
        logError("获取国际化文本失败: " + key);
        return key; // 返回键值作为fallback
    }
}
```

## 扩展新语言

### 添加新语言支持
```java
public enum SupportedLanguage {
    CHINESE("zh", "CN", "中文 (简体)", "settings.language.chinese"),
    ENGLISH("en", "US", "English (US)", "settings.language.english"),
    JAPANESE("ja", "JP", "日本語", "settings.language.japanese"),     // 新增
    KOREAN("ko", "KR", "한국어", "settings.language.korean");        // 新增
    
    // 其余代码保持不变
}
```

### 对应的资源文件
```
src/main/resources/
├── messages_zh_CN.properties
├── messages_en_US.properties
├── messages_ja_JP.properties  # 新增
└── messages_ko_KR.properties  # 新增
```

## 总结

新的I18nManager设计提供了：

1. **线程安全性** - 枚举单例模式确保并发安全
2. **类型安全性** - 强类型枚举避免运行时错误
3. **易用性** - 简化的API便于使用
4. **可扩展性** - 易于添加新语言支持
5. **向后兼容** - 保留旧接口支持现有代码
6. **性能优化** - 智能缓存和懒加载机制

这个设计特别适合在SettingPanel中进行动态语言切换，提供了完整的类型安全和线程安全保障。 