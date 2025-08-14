package controller;

import manager.ConfigManager;
import model.SettingModel;
import util.I18nManager;
import executor.ToolExecutor;

import java.awt.Component;
import java.io.File;

/**
 * 设置面板控制器 (Controller层)
 * 实现单例模式，负责处理设置面板的业务逻辑
 */
public class SettingPanelController {
    
    // 单例实例
    private static SettingPanelController instance;
    
    // 数据模型
    private SettingModel settingModel;
    
    // 视图接口
    private SettingPanelView view;
    
    // 监听器接口
    private SettingPanelListener listener;
    
    /**
     * 私有构造函数
     */
    private SettingPanelController() {
        this.settingModel = new SettingModel();
    }
    
    /**
     * 获取单例实例
     * @return 控制器实例
     */
    public static SettingPanelController getInstance() {
        if (instance == null) {
            instance = new SettingPanelController();
        }
        return instance;
    }
    
    /**
     * 设置视图接口
     * @param view 视图接口实现
     */
    public void setView(SettingPanelView view) {
        this.view = view;
    }
    
    /**
     * 设置监听器
     * @param listener 监听器实现
     */
    public void setListener(SettingPanelListener listener) {
        this.listener = listener;
    }
    
    /**
     * 获取设置模型
     * @return 设置模型
     */
    public SettingModel getSettingModel() {
        return settingModel;
    }
    
    /**
     * 初始化设置
     */
    public void initializeSettings() {
        try {
            // 自动设置语言
            autoSetLanguageByLocale();
            
            // 加载当前设置
            loadCurrentSettings();
            
            if (listener != null) {
                listener.onSettingsInitialized();
            }
            
        } catch (Exception e) {
            logError("初始化设置失败: " + e.getMessage());
            if (listener != null) {
                listener.onError("初始化设置", e.getMessage());
            }
        }
    }
    
    /**
     * 加载当前设置
     */
    public void loadCurrentSettings() {
        try {
            // 加载工具目录设置
            loadToolDirectorySettings();
            
            // 加载命令前缀设置
            loadCommandPrefixSettings();
            
            // 加载语言设置
            loadLanguageSettings();
            
            // 加载配置状态
            loadConfigurationStatus();
            
            if (listener != null) {
                listener.onSettingsLoaded();
            }
            
        } catch (Exception e) {
            logError("加载设置失败: " + e.getMessage());
            if (listener != null) {
                listener.onError("加载设置", e.getMessage());
            }
        }
    }
    
    /**
     * 导入配置文件
     * @param configFilePath 配置文件路径
     * @param parentComponent 父组件
     */
    public void importConfiguration(String configFilePath, Component parentComponent) {
        try {
            // 确认导入
            if (view != null) {
                boolean confirmed = view.confirmImportConfiguration(new File(configFilePath).getName());
                if (!confirmed) {
                    return;
                }
            }
            
            // 导入配置（自动保存到用户目录）
            ConfigManager.getInstance().importConfig(configFilePath);
            
            updateStatus("配置导入成功: " + new File(configFilePath).getName(), StatusType.SUCCESS);
            logInfo("配置文件导入成功: " + configFilePath);
            
            if (view != null) {
                view.showImportSuccessMessage(new File(configFilePath).getName());
            }
            
            if (listener != null) {
                listener.onConfigurationImported(configFilePath);
            }
            
        } catch (Exception e) {
            String errorMsg = "配置导入失败: " + e.getMessage();
            updateStatus(errorMsg, StatusType.ERROR);
            logError(errorMsg);
            
            if (view != null) {
                view.showImportErrorMessage(e.getMessage());
            }
            
            if (listener != null) {
                listener.onError("配置导入", e.getMessage());
            }
        }
    }
    
    /**
     * 导出配置文件
     * @param exportFilePath 导出文件路径
     */
    public void exportConfiguration(String exportFilePath) {
        try {
            // 导出配置
            ConfigManager.getInstance().exportConfig(exportFilePath);
            
            updateStatus("配置导出成功: " + new File(exportFilePath).getName(), StatusType.SUCCESS);
            logInfo("配置文件导出成功: " + exportFilePath);
            
            if (view != null) {
                view.showExportSuccessMessage(exportFilePath);
            }
            
            if (listener != null) {
                listener.onConfigurationExported(exportFilePath);
            }
            
        } catch (Exception e) {
            String errorMsg = "配置导出失败: " + e.getMessage();
            updateStatus(errorMsg, StatusType.ERROR);
            logError(errorMsg);
            
            if (view != null) {
                view.showExportErrorMessage(e.getMessage());
            }
            
            if (listener != null) {
                listener.onError("配置导出", e.getMessage());
            }
        }
    }
    
    /**
     * 重置配置文件
     * @param parentComponent 父组件
     */
    public void resetConfiguration(Component parentComponent) {
        try {
            // 确认重置
            if (view != null) {
                boolean confirmed = view.confirmResetConfiguration();
                if (!confirmed) {
                    return;
                }
            }
            
            // 重置配置为默认配置
            ConfigManager.getInstance().resetToDefaultConfig();
            
            updateStatus("配置已重置为默认设置", StatusType.INFO);
            logInfo("配置文件已重置为默认设置");
            
            if (view != null) {
                view.showResetSuccessMessage();
            }
            
            if (listener != null) {
                listener.onConfigurationReset();
            }
            
        } catch (Exception e) {
            String errorMsg = "配置重置失败: " + e.getMessage();
            updateStatus(errorMsg, StatusType.ERROR);
            logError(errorMsg);
            
            if (view != null) {
                view.showResetErrorMessage(e.getMessage());
            }
            
            if (listener != null) {
                listener.onError("配置重置", e.getMessage());
            }
        }
    }
    
    /**
     * 设置工具目录
     * @param directoryPath 目录路径
     */
    public void setToolDirectory(String directoryPath) {
        try {
            // 验证目录
            SettingModel.DirectoryValidationResult result = settingModel.validateToolDirectory(directoryPath);
            
            if (!result.isValid()) {
                if (result.canCreate()) {
                    // 询问是否创建目录
                    if (view != null) {
                        boolean createDirectory = view.confirmCreateDirectory(directoryPath);
                        if (createDirectory) {
                            settingModel.createDirectory(directoryPath);
                        } else {
                            updateStatus(result.getMessage(), StatusType.WARNING);
                            return;
                        }
                    }
                } else {
                    updateStatus(result.getMessage(), StatusType.ERROR);
                    return;
                }
            }
            
            // 设置目录
            settingModel.setToolDirectory(directoryPath);
            settingModel.saveToolSettings();
            
            // 刷新ToolExecutor中的设置
            refreshToolExecutorSettings();
            
            // 更新目录状态显示
            updateDirectoryStatus("工具目录: " + directoryPath, StatusType.SUCCESS);
            
            updateStatus("工具目录设置成功: " + directoryPath, StatusType.SUCCESS);
            logInfo("工具目录设置成功: " + directoryPath);
            
            if (view != null) {
                view.showDirectorySuccessMessage(directoryPath);
            }
            
            if (listener != null) {
                listener.onToolDirectoryChanged(directoryPath);
            }
            
        } catch (Exception e) {
            String errorMsg = "设置工具目录失败: " + e.getMessage();
            updateStatus(errorMsg, StatusType.ERROR);
            logError(errorMsg);
            
            if (listener != null) {
                listener.onError("设置工具目录", e.getMessage());
            }
        }
    }
    
    /**
     * 设置命令前缀
     * @param prefix 命令前缀
     */
    public void setCommandPrefix(String prefix) {
        try {
            // 设置前缀
            settingModel.setCommandPrefix(prefix);
            settingModel.saveToolSettings();
            
            // 刷新ToolExecutor中的设置
            refreshToolExecutorSettings();
            
            String statusMsg;
            String prefixStatusMsg;
            if (prefix == null || prefix.trim().isEmpty()) {
                statusMsg = "已重置为系统默认前缀";
                prefixStatusMsg = "使用系统默认: " + settingModel.getDefaultCommandPrefix();
            } else {
                statusMsg = "自定义前缀设置成功: " + prefix;
                prefixStatusMsg = "自定义前缀: " + prefix;
            }
            
            // 更新前缀状态显示
            updatePrefixStatus(prefixStatusMsg, StatusType.SUCCESS);
            
            updateStatus(statusMsg, StatusType.SUCCESS);
            logInfo("命令前缀设置成功: " + (prefix == null || prefix.trim().isEmpty() ? "系统默认" : prefix));
            
            if (view != null) {
                view.showPrefixSuccessMessage(prefix);
            }
            
            if (listener != null) {
                listener.onCommandPrefixChanged(prefix);
            }
            
        } catch (Exception e) {
            String errorMsg = "设置命令前缀失败: " + e.getMessage();
            updateStatus(errorMsg, StatusType.ERROR);
            logError(errorMsg);
            
            if (listener != null) {
                listener.onError("设置命令前缀", e.getMessage());
            }
        }
    }
    
    /**
     * 设置语言
     * @param language 语言设置
     */
    public void setLanguage(I18nManager.SupportedLanguage language) {
        try {
            I18nManager.SupportedLanguage currentLanguage = I18nManager.getInstance().getCurrentLanguage();
            if (language != currentLanguage) {
                I18nManager.getInstance().setCurrentLanguage(language);
                updateStatus("语言设置已更改为: " + language.getDisplayName(), StatusType.SUCCESS);
                logInfo("语言设置已更改为: " + language.getDisplayName());
                
                if (listener != null) {
                    listener.onLanguageChanged(language);
                }
            }
        } catch (Exception e) {
            String errorMsg = "设置语言失败: " + e.getMessage();
            updateStatus(errorMsg, StatusType.ERROR);
            logError(errorMsg);
            
            if (listener != null) {
                listener.onError("设置语言", e.getMessage());
            }
        }
    }
    
    /**
     * 自动根据系统地区设置语言
     */
    public void autoSetLanguageByLocale() {
        try {
            I18nManager.SupportedLanguage targetLanguage = settingModel.detectSystemLanguage();
            I18nManager.SupportedLanguage currentLanguage = I18nManager.getInstance().getCurrentLanguage();
            
            logInfo("检测到系统语言: " + targetLanguage.getDisplayName());
            
            if (currentLanguage != targetLanguage) {
                I18nManager.getInstance().setCurrentLanguage(targetLanguage);
                logInfo("已根据系统地区自动设置语言为: " + targetLanguage.getDisplayName());
                
                if (listener != null) {
                    listener.onLanguageAutoSet(targetLanguage);
                }
            } else {
                logInfo("当前语言已经匹配系统地区，无需更改");
            }
            
        } catch (Exception e) {
            logError("自动设置语言失败: " + e.getMessage());
        }
    }
    
    /**
     * 刷新设置
     */
    public void refreshSettings() {
        loadCurrentSettings();
    }
    
    /**
     * 获取设置统计信息
     * @return 统计信息
     */
    public SettingModel.SettingStatistics getStatistics() {
        return settingModel.getStatistics();
    }
    
    /**
     * 加载工具目录设置
     */
    private void loadToolDirectorySettings() {
        String toolDirectory = settingModel.getToolDirectory();
        
        if (view != null) {
            view.updateToolDirectory(toolDirectory);
        }
        
        if (!toolDirectory.isEmpty()) {
            File directory = new File(toolDirectory);
            if (directory.exists() && directory.isDirectory()) {
                updateDirectoryStatus("工具目录: " + toolDirectory, StatusType.SUCCESS);
            } else {
                updateDirectoryStatus("工具目录无效: " + toolDirectory, StatusType.ERROR);
            }
        } else {
            updateDirectoryStatus("工具目录未设置", StatusType.INFO);
        }
    }
    
    /**
     * 加载命令前缀设置
     */
    private void loadCommandPrefixSettings() {
        String commandPrefix = settingModel.getCommandPrefix();
        String defaultPrefix = settingModel.getDefaultCommandPrefix();
        
        if (view != null) {
            if (!commandPrefix.isEmpty()) {
                view.updateCommandPrefix(commandPrefix);
            } else {
                view.updateCommandPrefix(defaultPrefix);
            }
        }
        
        if (!commandPrefix.isEmpty()) {
            updatePrefixStatus("自定义前缀: " + commandPrefix, StatusType.SUCCESS);
        } else {
            updatePrefixStatus("使用系统默认: " + defaultPrefix, StatusType.INFO);
        }
    }
    
    /**
     * 加载语言设置
     */
    private void loadLanguageSettings() {
        I18nManager.SupportedLanguage currentLanguage = I18nManager.getInstance().getCurrentLanguage();
        
        if (view != null) {
            view.updateLanguage(currentLanguage);
        }
        
        updateLanguageStatus("当前语言: " + currentLanguage.getDisplayName() + " (已根据系统地区自动设置)", StatusType.SUCCESS);
    }
    
    /**
     * 加载配置状态
     */
    private void loadConfigurationStatus() {
        updateConfigStatus("配置状态: 已加载", StatusType.SUCCESS);
    }
    
    /**
     * 更新状态信息
     * @param message 状态消息
     * @param type 状态类型
     */
    private void updateStatus(String message, StatusType type) {
        if (view != null) {
            view.updateStatus(message, type);
        }
    }
    
    /**
     * 更新配置状态
     * @param message 状态消息
     * @param type 状态类型
     */
    private void updateConfigStatus(String message, StatusType type) {
        if (view != null) {
            view.updateConfigStatus(message, type);
        }
    }
    
    /**
     * 更新目录状态
     * @param message 状态消息
     * @param type 状态类型
     */
    private void updateDirectoryStatus(String message, StatusType type) {
        if (view != null) {
            view.updateDirectoryStatus(message, type);
        }
    }
    
    /**
     * 更新前缀状态
     * @param message 状态消息
     * @param type 状态类型
     */
    private void updatePrefixStatus(String message, StatusType type) {
        if (view != null) {
            view.updatePrefixStatus(message, type);
        }
    }
    
    /**
     * 更新语言状态
     * @param message 状态消息
     * @param type 状态类型
     */
    private void updateLanguageStatus(String message, StatusType type) {
        if (view != null) {
            view.updateLanguageStatus(message, type);
        }
    }
    
    /**
     * 刷新ToolExecutor中的设置
     * 确保ToolExecutor使用最新的用户配置
     */
    private void refreshToolExecutorSettings() {
        try {
            ToolExecutor.getInstance().refreshSettings();
            logInfo("已刷新ToolExecutor设置");
        } catch (Exception e) {
            logError("刷新ToolExecutor设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 记录信息日志
     * @param message 日志消息
     */
    private void logInfo(String message) {
        System.out.println("SettingPanelController: " + message);
    }
    
    /**
     * 记录错误日志
     * @param message 错误消息
     */
    private void logError(String message) {
        System.err.println("SettingPanelController: " + message);
    }
    
    /**
     * 状态类型枚举
     */
    public enum StatusType {
        SUCCESS,
        ERROR,
        WARNING,
        INFO
    }
    
    /**
     * 视图接口
     * 定义控制器与视图的交互方法
     */
    public interface SettingPanelView {
        /**
         * 确认导入配置
         * @param fileName 文件名
         * @return 是否确认
         */
        boolean confirmImportConfiguration(String fileName);
        
        /**
         * 确认重置配置
         * @return 是否确认
         */
        boolean confirmResetConfiguration();
        
        /**
         * 确认创建目录
         * @param directoryPath 目录路径
         * @return 是否确认
         */
        boolean confirmCreateDirectory(String directoryPath);
        
        /**
         * 显示导入成功消息
         * @param fileName 文件名
         */
        void showImportSuccessMessage(String fileName);
        
        /**
         * 显示导入错误消息
         * @param errorMessage 错误消息
         */
        void showImportErrorMessage(String errorMessage);
        
        /**
         * 显示导出成功消息
         * @param filePath 文件路径
         */
        void showExportSuccessMessage(String filePath);
        
        /**
         * 显示导出错误消息
         * @param errorMessage 错误消息
         */
        void showExportErrorMessage(String errorMessage);
        
        /**
         * 显示重置成功消息
         */
        void showResetSuccessMessage();
        
        /**
         * 显示重置错误消息
         * @param errorMessage 错误消息
         */
        void showResetErrorMessage(String errorMessage);
        
        /**
         * 显示目录设置成功消息
         * @param directoryPath 目录路径
         */
        void showDirectorySuccessMessage(String directoryPath);
        
        /**
         * 显示前缀设置成功消息
         * @param prefix 前缀
         */
        void showPrefixSuccessMessage(String prefix);
        
        /**
         * 更新工具目录
         * @param directory 目录路径
         */
        void updateToolDirectory(String directory);
        
        /**
         * 更新命令前缀
         * @param prefix 前缀
         */
        void updateCommandPrefix(String prefix);
        
        /**
         * 更新语言设置
         * @param language 语言
         */
        void updateLanguage(I18nManager.SupportedLanguage language);
        
        /**
         * 更新状态信息
         * @param message 状态消息
         * @param type 状态类型
         */
        void updateStatus(String message, StatusType type);
        
        /**
         * 更新配置状态
         * @param message 状态消息
         * @param type 状态类型
         */
        void updateConfigStatus(String message, StatusType type);
        
        /**
         * 更新目录状态
         * @param message 状态消息
         * @param type 状态类型
         */
        void updateDirectoryStatus(String message, StatusType type);
        
        /**
         * 更新前缀状态
         * @param message 状态消息
         * @param type 状态类型
         */
        void updatePrefixStatus(String message, StatusType type);
        
        /**
         * 更新语言状态
         * @param message 状态消息
         * @param type 状态类型
         */
        void updateLanguageStatus(String message, StatusType type);
    }
    
    /**
     * 监听器接口
     * 定义控制器事件的回调方法
     */
    public interface SettingPanelListener {
        /**
         * 设置初始化完成
         */
        void onSettingsInitialized();
        
        /**
         * 设置加载完成
         */
        void onSettingsLoaded();
        
        /**
         * 配置导入完成
         * @param filePath 文件路径
         */
        void onConfigurationImported(String filePath);
        
        /**
         * 配置导出完成
         * @param filePath 文件路径
         */
        void onConfigurationExported(String filePath);
        
        /**
         * 配置重置完成
         */
        void onConfigurationReset();
        
        /**
         * 工具目录更改
         * @param directory 新目录
         */
        void onToolDirectoryChanged(String directory);
        
        /**
         * 命令前缀更改
         * @param prefix 新前缀
         */
        void onCommandPrefixChanged(String prefix);
        
        /**
         * 语言更改
         * @param language 新语言
         */
        void onLanguageChanged(I18nManager.SupportedLanguage language);
        
        /**
         * 语言自动设置完成
         * @param language 设置的语言
         */
        void onLanguageAutoSet(I18nManager.SupportedLanguage language);
        
        /**
         * 发生错误
         * @param operation 操作名称
         * @param errorMessage 错误消息
         */
        void onError(String operation, String errorMessage);
    }
} 