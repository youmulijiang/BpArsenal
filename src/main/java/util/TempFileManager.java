package util;

import manager.ApiManager;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * 临时文件管理工具类
 * 负责创建和管理BpArsenal插件的临时文件
 */
public class TempFileManager {
    
    private static final String TEMP_FILE_PREFIX = "bparsenal_";
    
    /**
     * 创建包含URLs的临时文件
     * @param urls URL列表
     * @return 临时文件路径
     * @throws IOException 文件创建失败
     */
    public static String createUrlsFile(List<String> urls) throws IOException {
        return createListFile(urls, "urls_", ".txt");
    }
    
    /**
     * 创建包含主机名的临时文件
     * @param hosts 主机名列表
     * @return 临时文件路径
     * @throws IOException 文件创建失败
     */
    public static String createHostsFile(List<String> hosts) throws IOException {
        return createListFile(hosts, "hosts_", ".txt");
    }
    
    /**
     * 创建包含路径的临时文件
     * @param paths 路径列表
     * @return 临时文件路径
     * @throws IOException 文件创建失败
     */
    public static String createPathsFile(List<String> paths) throws IOException {
        return createListFile(paths, "paths_", ".txt");
    }
    
    /**
     * 创建包含列表数据的临时文件
     * @param items 数据项列表
     * @param category 文件类别（如urls_, hosts_等）
     * @param suffix 文件后缀
     * @return 临时文件路径
     * @throws IOException 文件创建失败
     */
    public static String createListFile(List<String> items, String category, String suffix) throws IOException {
        if (items == null || items.isEmpty()) {
            throw new IOException("数据列表为空，无法创建临时文件");
        }
        
        try {
            // 获取临时目录
            String tempDir = getTempDirectory();
            
            // 创建临时文件
            String fileName = TEMP_FILE_PREFIX + category + System.currentTimeMillis() + suffix;
            File tempFile = new File(tempDir, fileName);
            
            // 写入数据
            try (FileWriter writer = new FileWriter(tempFile)) {
                for (String item : items) {
                    writer.write(item);
                    writer.write(System.lineSeparator());
                }
            }
            
            // 设置文件在程序退出时删除
            tempFile.deleteOnExit();
            
            // 记录创建的临时文件
            logFileCreation(tempFile.getAbsolutePath(), items.size());
            
            return tempFile.getAbsolutePath();
            
        } catch (Exception e) {
            throw new IOException("创建临时文件失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取临时目录路径
     * @return 临时目录路径
     */
    private static String getTempDirectory() {
        try {
            // 尝试获取插件目录
            if (ApiManager.getInstance().isInitialized()) {
                String extensionPath = ApiManager.getInstance().getApi().extension().filename();
                if (extensionPath != null && !extensionPath.isEmpty()) {
                    File extensionFile = new File(extensionPath);
                    return extensionFile.getParent();
                }
            }
        } catch (Exception e) {
            // 忽略异常，使用系统临时目录
        }
        
        // 使用系统临时目录作为fallback
        return System.getProperty("java.io.tmpdir");
    }
    
    /**
     * 记录文件创建信息
     * @param filePath 文件路径
     * @param itemCount 数据项数量
     */
    private static void logFileCreation(String filePath, int itemCount) {
    }
    
    /**
     * 清理指定前缀的临时文件
     * @param directory 目录路径
     */
    public static void cleanupTempFiles(String directory) {
        try {
            File dir = new File(directory);
            if (!dir.exists() || !dir.isDirectory()) {
                return;
            }
            
            File[] tempFiles = dir.listFiles((dir1, name) -> 
                name.startsWith(TEMP_FILE_PREFIX) && name.endsWith(".txt"));
            
            if (tempFiles != null) {
                int deletedCount = 0;
                for (File file : tempFiles) {
                    if (file.delete()) {
                        deletedCount++;
                    }
                }
                
            }
        } catch (Exception e) {
        }
    }
} 