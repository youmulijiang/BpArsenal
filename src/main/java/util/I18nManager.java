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
 * 使用枚举单例模式确保线程安全
 * 负责加载和管理多语言资源
 */
public enum I18nManager {
    
    INSTANCE;
    
    // 资源包缓存
    private final ConcurrentMap<SupportedLanguage, ResourceBundle> bundleCache = new ConcurrentHashMap<>();
    
    // 当前语言
    private volatile SupportedLanguage currentLanguage;
    
    // 当前资源包
    private volatile ResourceBundle currentBundle;
    
    // 语言变更监听器列表
    private final java.util.List<LanguageChangeListener> listeners = new java.util.concurrent.CopyOnWriteArrayList<>();
    
    /**
     * 语言变更监听器接口
     */
    public interface LanguageChangeListener {
        void onLanguageChanged(SupportedLanguage newLanguage);
    }
    
    /**
     * 支持的语言枚举
     */
    public enum SupportedLanguage {
        CHINESE("zh", "CN", "中文 (简体)", "settings.language.chinese"),
        ENGLISH("en", "US", "English (US)", "settings.language.english");
        
        private final String language;
        private final String country;
        private final String displayName;
        private final String i18nKey;
        private final Locale locale;
        
        SupportedLanguage(String language, String country, String displayName, String i18nKey) {
            this.language = language;
            this.country = country;
            this.displayName = displayName;
            this.i18nKey = i18nKey;
            this.locale = new Locale(language, country);
        }
        
        public String getLanguage() {
            return language;
        }
        
        public String getCountry() {
            return country;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getI18nKey() {
            return i18nKey;
        }
        
        public Locale getLocale() {
            return locale;
        }
        
        public String getLanguageTag() {
            return locale.toLanguageTag();
        }
        
        /**
         * 根据Locale获取对应的SupportedLanguage
         */
        public static SupportedLanguage fromLocale(Locale locale) {
            if (locale == null) {
                return CHINESE; // 默认返回中文
            }
            
            for (SupportedLanguage lang : values()) {
                if (lang.locale.equals(locale)) {
                    return lang;
                }
            }
            return CHINESE; // 不支持的语言默认返回中文
        }
        
        /**
         * 根据语言标签获取对应的SupportedLanguage
         */
        public static SupportedLanguage fromLanguageTag(String languageTag) {
            if (languageTag == null || languageTag.trim().isEmpty()) {
                return CHINESE;
            }
            
            for (SupportedLanguage lang : values()) {
                if (lang.getLanguageTag().equals(languageTag)) {
                    return lang;
                }
            }
            return CHINESE;
        }
    }
    
    // 语言设置文件
    private static final String LANGUAGE_CONFIG_FILE = "language_settings.properties";
    private static final String LANGUAGE_SETTING_KEY = "current.language";
    
    // 资源包基础名称
    private static final String BUNDLE_BASE_NAME = "messages";
    
    /**
     * 枚举构造函数，在类加载时自动调用
     */
    I18nManager() {
        initializeLanguage();
    }
    
    /**
     * 获取单例实例
     * @return I18nManager实例
     */
    public static I18nManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * 初始化语言设置
     */
    private void initializeLanguage() {
        // 从配置文件加载语言设置
        SupportedLanguage savedLanguage = loadSavedLanguage();
        if (savedLanguage != null) {
            setCurrentLanguage(savedLanguage);
        } else {
            // 默认使用中文，不管系统语言是什么
            setCurrentLanguage(SupportedLanguage.CHINESE);
        }
    }
    
    /**
     * 设置当前语言
     * @param language 语言
     */
    public synchronized void setCurrentLanguage(SupportedLanguage language) {
        if (language == null) {
            logError("语言参数不能为空");
            return;
        }
        
        if (this.currentLanguage == language) {
            return; // 相同语言，无需更新
        }
        
        this.currentLanguage = language;
        this.currentBundle = getResourceBundle(language);
        
        // 保存语言设置
        saveLanguageSetting(language);
        
        // 通知所有监听器
        notifyLanguageChanged(language);
        
        logInfo("语言环境已设置为: " + language.getDisplayName());
    }
    
    /**
     * 设置当前语言环境（兼容旧接口）
     * @param locale 语言环境
     */
    public void setLocale(Locale locale) {
        SupportedLanguage language = SupportedLanguage.fromLocale(locale);
        setCurrentLanguage(language);
    }
    
    /**
     * 获取当前语言
     * @return 当前语言
     */
    public SupportedLanguage getCurrentLanguage() {
        return currentLanguage;
    }
    
    /**
     * 获取当前语言环境（兼容旧接口）
     * @return 当前语言环境
     */
    public Locale getCurrentLocale() {
        return currentLanguage != null ? currentLanguage.getLocale() : SupportedLanguage.CHINESE.getLocale();
    }
    
    /**
     * 获取资源包
     * @param language 语言
     * @return 资源包
     */
    private ResourceBundle getResourceBundle(SupportedLanguage language) {
        return bundleCache.computeIfAbsent(language, lang -> {
            try {
                return ResourceBundle.getBundle(BUNDLE_BASE_NAME, lang.getLocale());
            } catch (Exception e) {
                logError("加载资源包失败: " + lang.getDisplayName() + ", 错误: " + e.getMessage());
                // 如果加载失败，尝试加载默认的中文资源包
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
        
        for (SupportedLanguage lang : SupportedLanguage.values()) {
            if (lang.getLocale().equals(locale)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取支持的语言列表
     * @return 支持的语言数组
     */
    public SupportedLanguage[] getSupportedLanguages() {
        return SupportedLanguage.values();
    }
    
    /**
     * 获取支持的语言环境列表（兼容旧接口）
     * @return 支持的语言环境数组
     */
    public Locale[] getSupportedLocales() {
        SupportedLanguage[] languages = SupportedLanguage.values();
        Locale[] locales = new Locale[languages.length];
        for (int i = 0; i < languages.length; i++) {
            locales[i] = languages[i].getLocale();
        }
        return locales;
    }
    
    /**
     * 获取语言显示名称
     * @param language 语言
     * @return 显示名称
     */
    public String getLanguageDisplayName(SupportedLanguage language) {
        if (language == null) {
            return SupportedLanguage.CHINESE.getDisplayName();
        }
        return language.getDisplayName();
    }
    
    /**
     * 获取语言显示名称（兼容旧接口）
     * @param locale 语言环境
     * @return 显示名称
     */
    public String getLanguageDisplayName(Locale locale) {
        SupportedLanguage language = SupportedLanguage.fromLocale(locale);
        return getLanguageDisplayName(language);
    }
    
    /**
     * 从配置文件加载保存的语言设置
     * @return 保存的语言，如果没有则返回null
     */
    private SupportedLanguage loadSavedLanguage() {
        try {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(LANGUAGE_CONFIG_FILE)) {
                props.load(fis);
                String languageTag = props.getProperty(LANGUAGE_SETTING_KEY);
                if (languageTag != null && !languageTag.trim().isEmpty()) {
                    return SupportedLanguage.fromLanguageTag(languageTag);
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
     * @param language 语言
     */
    private void saveLanguageSetting(SupportedLanguage language) {
        try {
            Properties props = new Properties();
            props.setProperty(LANGUAGE_SETTING_KEY, language.getLanguageTag());
            
            try (FileOutputStream fos = new FileOutputStream(LANGUAGE_CONFIG_FILE)) {
                props.store(fos, "BpArsenal Language Settings");
            }
            
            logInfo("语言设置已保存: " + language.getLanguageTag());
        } catch (IOException e) {
            logError("保存语言设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 重新加载资源包（用于语言切换后的更新）
     */
    public void reloadBundle() {
        if (currentLanguage != null) {
            bundleCache.remove(currentLanguage);
            currentBundle = getResourceBundle(currentLanguage);
            logInfo("资源包已重新加载: " + currentLanguage.getDisplayName());
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
        return currentLanguage != null ? currentLanguage.getLanguageTag() : SupportedLanguage.CHINESE.getLanguageTag();
    }
    
    /**
     * 切换到中文
     */
    public void switchToChinese() {
        setCurrentLanguage(SupportedLanguage.CHINESE);
    }
    
    /**
     * 切换到英文
     */
    public void switchToEnglish() {
        setCurrentLanguage(SupportedLanguage.ENGLISH);
    }
    
    /**
     * 根据语言索引设置语言（便于下拉框使用）
     * @param index 语言索引
     */
    public void setLanguageByIndex(int index) {
        SupportedLanguage[] languages = SupportedLanguage.values();
        if (index >= 0 && index < languages.length) {
            setCurrentLanguage(languages[index]);
        }
    }
    
    /**
     * 获取当前语言的索引（便于下拉框使用）
     * @return 语言索引
     */
    public int getCurrentLanguageIndex() {
        if (currentLanguage == null) {
            return 0; // 默认中文
        }
        
        SupportedLanguage[] languages = SupportedLanguage.values();
        for (int i = 0; i < languages.length; i++) {
            if (languages[i] == currentLanguage) {
                return i;
            }
        }
        return 0; // 默认中文
    }
    
    /**
     * 判断当前是否为中文
     * @return true if current language is Chinese
     */
    public boolean isChinese() {
        return currentLanguage == SupportedLanguage.CHINESE;
    }
    
    /**
     * 判断当前是否为英文
     * @return true if current language is English
     */
    public boolean isEnglish() {
        return currentLanguage == SupportedLanguage.ENGLISH;
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
     * @param newLanguage 新的语言
     */
    private void notifyLanguageChanged(SupportedLanguage newLanguage) {
        for (LanguageChangeListener listener : listeners) {
            try {
                listener.onLanguageChanged(newLanguage);
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