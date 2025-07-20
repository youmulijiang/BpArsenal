package util;

/**
 * 操作系统工具类
 * 提供系统平台判断和相关工具方法
 */
public class OsUtils {
    
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    
    /**
     * 判断是否为Windows系统
     * @return 是否为Windows系统
     */
    public static boolean isWindows() {
        return OS_NAME.contains("win");
    }
    
    /**
     * 判断是否为Linux系统
     * @return 是否为Linux系统
     */
    public static boolean isLinux() {
        return OS_NAME.contains("nix") || OS_NAME.contains("nux") || OS_NAME.contains("aix");
    }
    
    /**
     * 判断是否为Mac系统
     * @return 是否为Mac系统
     */
    public static boolean isMac() {
        return OS_NAME.contains("mac");
    }
    
    /**
     * 判断是否为Unix-like系统（Linux, Mac, Unix等）
     * @return 是否为Unix-like系统
     */
    public static boolean isUnixLike() {
        return isLinux() || isMac() || OS_NAME.contains("freebsd") || OS_NAME.contains("openbsd") || OS_NAME.contains("netbsd");
    }
    
    /**
     * 获取系统名称
     * @return 系统名称
     */
    public static String getOsName() {
        return OS_NAME;
    }
    
    /**
     * 获取系统类型字符串
     * @return 系统类型（Windows, Linux, Mac, Unknown）
     */
    public static String getOsType() {
        if (isWindows()) {
            return "Windows";
        } else if (isLinux()) {
            return "Linux";
        } else if (isMac()) {
            return "Mac";
        } else {
            return "Unknown";
        }
    }
    
    /**
     * 获取默认的命令执行前缀
     * @return 命令执行前缀数组
     */
    public static String[] getDefaultCommandPrefix() {
        if (isWindows()) {
            return new String[]{"cmd", "/c"};
        } else {
            return new String[]{"/bin/bash", "-c"};
        }
    }
    
    /**
     * 格式化命令以在当前操作系统上运行
     * @param command 原始命令
     * @return 格式化后的命令数组
     */
    public static String[] formatCommandForRunningOnOperatingSystem(String command) {
        String[] commandToBeExecuted;
        if (isWindows()) {
            commandToBeExecuted = new String[]{"cmd", "/c", command};
        } else {
            commandToBeExecuted = new String[]{"/bin/bash", "-c", command};
        }
        return commandToBeExecuted;
    }
    
    /**
     * 使用自定义前缀格式化命令
     * @param command 原始命令
     * @param prefix 自定义前缀
     * @return 格式化后的命令数组
     */
    public static String[] formatCommandWithPrefix(String command, String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return formatCommandForRunningOnOperatingSystem(command);
        }
        
        String[] prefixParts = prefix.trim().split("\\s+");
        String[] result = new String[prefixParts.length + 1];
        System.arraycopy(prefixParts, 0, result, 0, prefixParts.length);
        result[prefixParts.length] = command;
        return result;
    }
    
    /**
     * 获取系统编码
     * @return 编码字符串
     */
    public static String getSystemEncoding() {
        if (isWindows()) {
            return "GBK";
        } else {
            return "UTF-8";
        }
    }
    
    /**
     * 获取路径分隔符
     * @return 路径分隔符
     */
    public static String getPathSeparator() {
        return System.getProperty("path.separator");
    }
    
    /**
     * 获取文件分隔符
     * @return 文件分隔符
     */
    public static String getFileSeparator() {
        return System.getProperty("file.separator");
    }
    
    /**
     * 获取行分隔符
     * @return 行分隔符
     */
    public static String getLineSeparator() {
        return System.getProperty("line.separator");
    }
} 