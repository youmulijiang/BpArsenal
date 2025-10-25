package util;

import burp.api.montoya.logging.Logging;
import manager.ApiManager;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * BpArsenal日志记录器
 * 封装Montoya API的日志功能，支持开发者在代码层控制日志开关
 * 采用单例模式确保全局唯一实例
 * 
 * 使用方式：
 * - 开发时设置 DEBUG_MODE = true 开启所有日志
 * - 上线时设置 DEBUG_MODE = false 关闭所有日志
 * - 可单独控制不同级别的日志开关
 */
public class BpLogger {
    
    // ===== 开发者控制区域 - 修改这些常量来控制日志行为 =====
    
    /**
     * 调试模式开关 - 开发者直接修改此值
     * true: 开启所有日志（开发环境）
     * false: 关闭所有日志（生产环境）
     */
    private static final boolean DEBUG_MODE = true;
    
    /**
     * 各级别日志开关 - 开发者可单独控制
     * 只有在 DEBUG_MODE = true 时才生效
     */
    private static final boolean ENABLE_DEBUG_LOG = true;   // 调试日志
    private static final boolean ENABLE_INFO_LOG = true;    // 信息日志
    private static final boolean ENABLE_WARN_LOG = true;    // 警告日志
    private static final boolean ENABLE_ERROR_LOG = true;   // 错误日志
    
    /**
     * 是否显示详细信息（时间戳、模块名等）
     */
    private static final boolean SHOW_DETAILED_INFO = true;
    
    // ===== 内部实现 - 开发者无需修改 =====
    
    private static BpLogger instance;
    private static final String LOG_PREFIX = "[BpArsenal]";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    
    /**
     * 私有构造函数，防止外部实例化
     */
    private BpLogger() {}
    
    /**
     * 获取BpLogger单例实例
     * @return BpLogger实例
     */
    public static BpLogger getInstance() {
        if (instance == null) {
            synchronized (BpLogger.class) {
                if (instance == null) {
                    instance = new BpLogger();
                }
            }
        }
        return instance;
    }
    
    /**
     * 记录调试日志
     * @param module 模块名称
     * @param message 日志消息
     */
    public void debug(String module, String message) {
        if (DEBUG_MODE && ENABLE_DEBUG_LOG) {
            logToBurp(false, formatMessage("DEBUG", module, message));
        }
    }
    
    /**
     * 记录信息日志
     * @param module 模块名称
     * @param message 日志消息
     */
    public void info(String module, String message) {
        if (DEBUG_MODE && ENABLE_INFO_LOG) {
            logToBurp(false, formatMessage("INFO", module, message));
        }
    }
    
    /**
     * 记录警告日志
     * @param module 模块名称
     * @param message 日志消息
     */
    public void warn(String module, String message) {
        if (DEBUG_MODE && ENABLE_WARN_LOG) {
            logToBurp(false, formatMessage("WARN", module, message));
        }
    }
    
    /**
     * 记录错误日志
     * @param module 模块名称
     * @param message 日志消息
     */
    public void error(String module, String message) {
        if (DEBUG_MODE && ENABLE_ERROR_LOG) {
            logToBurp(true, formatMessage("ERROR", module, message));
        }
    }
    
    /**
     * 记录错误日志（带异常信息）
     * @param module 模块名称
     * @param message 日志消息
     * @param throwable 异常对象
     */
    public void error(String module, String message, Throwable throwable) {
        if (DEBUG_MODE && ENABLE_ERROR_LOG) {
            String stackTrace = getStackTrace(throwable);
            String fullMessage = message + "\n异常详情:\n" + stackTrace;
            logToBurp(true, formatMessage("ERROR", module, fullMessage));
        }
    }
    
    // ===== 便捷方法：自动获取调用者类名 =====
    
    /**
     * 记录调试日志（自动获取类名）
     * @param message 日志消息
     */
    public void debug(String message) {
        debug(getCallerClassName(), message);
    }
    
    /**
     * 记录信息日志（自动获取类名）
     * @param message 日志消息
     */
    public void info(String message) {
        info(getCallerClassName(), message);
    }
    
    /**
     * 记录警告日志（自动获取类名）
     * @param message 日志消息
     */
    public void warn(String message) {
        warn(getCallerClassName(), message);
    }
    
    /**
     * 记录错误日志（自动获取类名）
     * @param message 日志消息
     */
    public void error(String message) {
        error(getCallerClassName(), message);
    }
    
    /**
     * 记录错误日志（自动获取类名，带异常）
     * @param message 日志消息
     * @param throwable 异常对象
     */
    public void error(String message, Throwable throwable) {
        error(getCallerClassName(), message, throwable);
    }
    
    // ===== 特殊方法：强制日志（即使在生产模式也会输出） =====
    
    /**
     * 强制记录重要信息（忽略DEBUG_MODE设置）
     * 用于记录关键的系统信息，如插件启动、严重错误等
     * @param module 模块名称
     * @param message 日志消息
     */
    public void forceInfo(String module, String message) {
        logToBurp(false, formatMessage("INFO", module, message));
    }
    
    /**
     * 强制记录错误信息（忽略DEBUG_MODE设置）
     * 用于记录严重错误，即使在生产环境也需要记录
     * @param module 模块名称
     * @param message 日志消息
     */
    public void forceError(String module, String message) {
        logToBurp(true, formatMessage("ERROR", module, message));
    }
    
    /**
     * 强制记录错误信息（忽略DEBUG_MODE设置，带异常）
     * @param module 模块名称
     * @param message 日志消息
     * @param throwable 异常对象
     */
    public void forceError(String module, String message, Throwable throwable) {
        String stackTrace = getStackTrace(throwable);
        String fullMessage = message + "\n异常详情:\n" + stackTrace;
        logToBurp(true, formatMessage("ERROR", module, fullMessage));
    }
    
    // ===== 内部辅助方法 =====
    
    /**
     * 格式化日志消息
     * @param level 日志级别
     * @param module 模块名称
     * @param message 原始消息
     * @return 格式化后的消息
     */
    private String formatMessage(String level, String module, String message) {
        if (!SHOW_DETAILED_INFO) {
            return LOG_PREFIX + " " + message;
        }
        
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        return String.format("%s [%s] [%s] [%s] %s", 
            LOG_PREFIX, timestamp, level, module, message);
    }
    
    /**
     * 输出日志到Burp Suite
     * @param isError 是否为错误日志
     * @param message 格式化后的消息
     */
    private void logToBurp(boolean isError, String message) {
        try {
            if (ApiManager.getInstance().isInitialized()) {
                Logging logging = ApiManager.getInstance().getApi().logging();
                
                if (isError) {
                    logging.logToError(message);
                } else {
                    logging.logToOutput(message);
                }
            } else {
                // API未初始化时输出到控制台
                if (isError) {
                    System.err.println(message);
                } else {
                    System.out.println(message);
                }
            }
        } catch (Exception e) {
            // 静默处理，避免日志记录本身出错影响主流程
            System.err.println("BpLogger: 记录日志失败 - " + e.getMessage());
        }
    }
    
    /**
     * 获取异常堆栈跟踪信息
     * @param throwable 异常对象
     * @return 堆栈跟踪字符串
     */
    private String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            return "获取堆栈跟踪失败: " + e.getMessage();
        }
    }
    
    /**
     * 获取调用者类名
     * @return 调用者类名
     */
    private String getCallerClassName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // stackTrace[0] = getStackTrace()
        // stackTrace[1] = getCallerClassName()
        // stackTrace[2] = 调用getCallerClassName()的方法（如debug(), info()等）
        // stackTrace[3] = 实际的调用者
        if (stackTrace.length > 3) {
            String fullClassName = stackTrace[3].getClassName();
            return fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
        }
        return "Unknown";
    }
    
    // ===== 开发者辅助方法 =====
    
    /**
     * 检查当前是否为调试模式
     * @return 是否为调试模式
     */
    public boolean isDebugMode() {
        return DEBUG_MODE;
    }
    
    /**
     * 获取当前日志配置信息
     * @return 配置信息字符串
     */
    public String getLogConfig() {
        return String.format(
            "BpLogger配置: 调试模式=%s, DEBUG=%s, INFO=%s, WARN=%s, ERROR=%s, 详细信息=%s",
            DEBUG_MODE, ENABLE_DEBUG_LOG, ENABLE_INFO_LOG, ENABLE_WARN_LOG, ENABLE_ERROR_LOG, SHOW_DETAILED_INFO
        );
    }
    
    /**
     * 输出当前日志配置（用于调试）
     */
    public void printLogConfig() {
        forceInfo("BpLogger", getLogConfig());
    }
}
