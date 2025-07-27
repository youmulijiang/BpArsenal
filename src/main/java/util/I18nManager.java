package util;

import manager.ApiManager;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 国际化管理器
 * 负责加载和管理多语言资源
 */
public class I18nManager {
    
    private static I18nManager instance;
    
    // 资源包缓存
    private final ConcurrentMap<Locale, ResourceBundle> bundleCache = new ConcurrentHashMap<>();
    
    // 当前语言环境
    private Locale currentLocale;
    
    // 当前资源包
    private ResourceBundle currentBundle;
    
    // 语言变更监听器列表
    private final java.util.List<LanguageChangeListener> listeners = new java.util.concurrent.CopyOnWriteArrayList<>();
    
    /**
     * 语言变更监听器接口
     */
    public interface LanguageChangeListener {
        void onLanguageChanged(Locale newLocale);
    }
    
    // 支持的语言列表
    public static final Locale CHINESE = new Locale("zh", "CN");
    public static final Locale ENGLISH = new Locale("en", "US");
    
    // 语言设置文件
    private static final String LANGUAGE_CONFIG_FILE = "language_settings.properties";
    private static final String LANGUAGE_SETTING_KEY = "current.language";
    
    // 资源包基础名称
    private static final String BUNDLE_BASE_NAME = "messages";
    
    /**
     * 私有构造函数
     */
    private I18nManager() {
        initializeLanguage();
    }
    
    /**
     * 获取单例实例
     * @return I18nManager实例
     */
    public static I18nManager getInstance() {
        if (instance == null) {
            synchronized (I18nManager.class) {
                if (instance == null) {
                    instance = new I18nManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 初始化语言设置
     */
    private void initializeLanguage() {
        // 从配置文件加载语言设置
        Locale savedLocale = loadSavedLanguage();
        if (savedLocale != null) {
            setLocale(savedLocale);
        } else {
            // 默认使用中文，不管系统语言是什么
            setLocale(CHINESE);
        }
    }
    
    /**
     * 设置当前语言环境
     * @param locale 语言环境
     */
    public void setLocale(Locale locale) {
        if (locale == null || !isSupportedLocale(locale)) {
            logError("不支持的语言环境: " + (locale != null ? locale.toString() : "null"));
            return;
        }
        
        this.currentLocale = locale;
        this.currentBundle = getResourceBundle(locale);
        
        // 保存语言设置
        saveLanguageSetting(locale);
        
        // 通知所有监听器
        notifyLanguageChanged(locale);
        
        logInfo("语言环境已设置为: " + locale.toString());
    }
    
    /**
     * 获取当前语言环境
     * @return 当前语言环境
     */
    public Locale getCurrentLocale() {
        return currentLocale;
    }
    
    /**
     * 获取资源包
     * @param locale 语言环境
     * @return 资源包
     */
    private ResourceBundle getResourceBundle(Locale locale) {
        return bundleCache.computeIfAbsent(locale, l -> {
            try {
                return ResourceBundle.getBundle(BUNDLE_BASE_NAME, l);
            } catch (Exception e) {
                logError("加载资源包失败: " + l.toString() + ", 错误: " + e.getMessage());
                // 如果加载失败，尝试加载默认的中文资源包
                if (!l.equals(CHINESE)) {
                    return ResourceBundle.getBundle(BUNDLE_BASE_NAME, CHINESE);
                }
                throw new RuntimeException("无法加载任何资源包", e);
            }
        });
    }
    
    /**
     * 获取国际化文本
     * @param key 键值
     * @return 国际化文本
     */
    public String getText(String key) {
        if (currentBundle == null) {
            logError("资源包未初始化");
            return key;
        }
        
        try {
            return currentBundle.getString(key);
        } catch (Exception e) {
            logError("获取国际化文本失败: " + key + ", 错误: " + e.getMessage());
            return key; // 返回键值作为fallback
        }
    }
    
    /**
     * 获取国际化文本（带参数替换）
     * @param key 键值
     * @param params 参数
     * @return 格式化后的国际化文本
     */
    public String getText(String key, Object... params) {
        String text = getText(key);
        if (params == null || params.length == 0) {
            return text;
        }
        
        try {
            return String.format(text, params);
        } catch (Exception e) {
            logError("格式化国际化文本失败: " + key + ", 错误: " + e.getMessage());
            return text;
        }
    }
    
    /**
     * 检查是否支持指定的语言环境
     * @param locale 语言环境
     * @return true如果支持
     */
    public boolean isSupportedLocale(Locale locale) {
        if (locale == null) {
            return false;
        }
        
        return (locale.getLanguage().equals("zh") && locale.getCountry().equals("CN")) ||
               (locale.getLanguage().equals("en") && locale.getCountry().equals("US"));
    }
    
    /**
     * 获取支持的语言列表
     * @return 支持的语言数组
     */
    public Locale[] getSupportedLocales() {
        return new Locale[]{CHINESE, ENGLISH};
    }
    
    /**
     * 获取语言显示名称
     * @param locale 语言环境
     * @return 显示名称
     */
    public String getLanguageDisplayName(Locale locale) {
        if (locale.equals(CHINESE)) {
            return getText("settings.language.chinese");
        } else if (locale.equals(ENGLISH)) {
            return getText("settings.language.english");
        } else {
            return locale.getDisplayName();
        }
    }
    
    /**
     * 从配置文件加载保存的语言设置
     * @return 保存的语言环境，如果没有则返回null
     */
    private Locale loadSavedLanguage() {
        try {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(LANGUAGE_CONFIG_FILE)) {
                props.load(fis);
                String languageTag = props.getProperty(LANGUAGE_SETTING_KEY);
                if (languageTag != null && !languageTag.trim().isEmpty()) {
                    return Locale.forLanguageTag(languageTag);
                }
            }
        } catch (IOException e) {
            // 文件不存在或读取失败，这是正常的
            logInfo("语言配置文件不存在或读取失败，将使用默认语言");
        }
        return null;
    }
    
    /**
     * 保存语言设置到配置文件
     * @param locale 语言环境
     */
    private void saveLanguageSetting(Locale locale) {
        try {
            Properties props = new Properties();
            props.setProperty(LANGUAGE_SETTING_KEY, locale.toLanguageTag());
            
            try (FileOutputStream fos = new FileOutputStream(LANGUAGE_CONFIG_FILE)) {
                props.store(fos, "BpArsenal Language Settings");
            }
            
            logInfo("语言设置已保存: " + locale.toLanguageTag());
        } catch (IOException e) {
            logError("保存语言设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 重新加载资源包（用于语言切换后的更新）
     */
    public void reloadBundle() {
        if (currentLocale != null) {
            bundleCache.remove(currentLocale);
            currentBundle = getResourceBundle(currentLocale);
            logInfo("资源包已重新加载: " + currentLocale.toString());
        }
    }
    
    /**
     * 清理缓存
     */
    public void clearCache() {
        bundleCache.clear();
        logInfo("国际化资源包缓存已清理");
    }
    
    /**
     * 检查资源键是否存在
     * @param key 键值
     * @return true如果键存在
     */
    public boolean hasKey(String key) {
        if (currentBundle == null) {
            return false;
        }
        
        try {
            currentBundle.getString(key);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取当前语言的语言标签
     * @return 语言标签（如：zh-CN, en-US）
     */
    public String getCurrentLanguageTag() {
        return currentLocale != null ? currentLocale.toLanguageTag() : "zh-CN";
    }
    
    /**
     * 添加语言变更监听器
     * @param listener 监听器
     */
    public void addLanguageChangeListener(LanguageChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            logInfo("添加语言变更监听器: " + listener.getClass().getSimpleName());
        }
    }
    
    /**
     * 移除语言变更监听器
     * @param listener 监听器
     */
    public void removeLanguageChangeListener(LanguageChangeListener listener) {
        if (listener != null) {
            listeners.remove(listener);
            logInfo("移除语言变更监听器: " + listener.getClass().getSimpleName());
        }
    }
    
    /**
     * 通知所有监听器语言已变更
     * @param newLocale 新的语言环境
     */
    private void notifyLanguageChanged(Locale newLocale) {
        for (LanguageChangeListener listener : listeners) {
            try {
                listener.onLanguageChanged(newLocale);
            } catch (Exception e) {
                logError("通知语言变更监听器失败: " + e.getMessage());
            }
        }
        logInfo("已通知 " + listeners.size() + " 个监听器语言变更");
    }
    
    /**
     * 记录信息日志
     * @param message 日志消息
     */
    private void logInfo(String message) {
        try {
            if (ApiManager.getInstance().isInitialized()) {
                ApiManager.getInstance().getApi().logging().logToOutput("I18nManager: " + message);
            }
        } catch (Exception e) {
            System.out.println("I18nManager: " + message);
        }
    }
    
    /**
     * 记录错误日志
     * @param message 错误消息
     */
    private void logError(String message) {
        try {
            if (ApiManager.getInstance().isInitialized()) {
                ApiManager.getInstance().getApi().logging().logToError("I18nManager: " + message);
            }
        } catch (Exception e) {
            System.err.println("I18nManager: " + message);
        }
    }
} 