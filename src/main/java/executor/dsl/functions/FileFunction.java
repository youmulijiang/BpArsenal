package executor.dsl.functions;

import executor.dsl.FunctionHandler;
import executor.dsl.HttpContext;
import executor.dsl.DslException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * file函数实现 - 生成持久化文件
 * 
 * 支持的格式：
 * 1. %file(content, filepath)% - 创建包含单个内容的文件
 * 2. %file(listContent, filepath)% - 创建包含列表内容的文件（每行一项）
 * 
 * 示例：
 * - %file(http.request.body.raw, 'request.txt')% - 将请求体保存到request.txt
 * - %file(httpList.requests.*.request.url, 'urls.txt')% - 将URL列表保存到urls.txt
 * - %file(httpList.requests.*.request.host, 'hosts.txt')% - 将主机列表保存到hosts.txt
 * 
 * 文件路径支持：
 * - 相对路径：保存到当前工作目录（'urls.txt', './output/urls.txt'）
 * - 绝对路径：保存到指定位置（'/tmp/urls.txt', 'C:/temp/urls.txt'）
 */
public class FileFunction implements FunctionHandler {
    
    @Override
    public String getName() {
        return "file";
    }
    
    @Override
    public String getDescription() {
        return "Create persistent file with content";
    }
    
    @Override
    public Object execute(List<Object> args, HttpContext context) throws DslException {
        if (args.size() < 2) {
            throw new DslException("file requires 2 arguments: file(content, filepath)");
        }
        
        Object input = args.get(0);
        String filepath = args.get(1).toString();
        
        try {
            // 处理输入内容
            List<String> lines = convertToLines(input);
            
            if (lines.isEmpty()) {
                throw new DslException("file: content is empty");
            }
            
            // 创建文件
            File outputFile = createFile(filepath);
            
            // 写入内容
            writeLinesToFile(outputFile, lines);
            
            return outputFile.getAbsolutePath();
            
        } catch (IOException e) {
            throw new DslException("Failed to create file: " + e.getMessage(), e);
        }
    }
    
    /**
     * 将输入转换为行列表
     */
    private List<String> convertToLines(Object input) {
        List<String> lines = new ArrayList<>();
        
        if (input == null) {
            return lines;
        }
        
        if (input instanceof List) {
            // 如果是列表，逐项添加
            for (Object item : (List<?>) input) {
                if (item != null) {
                    lines.add(item.toString());
                }
            }
        } else if (input instanceof String) {
            // 如果是字符串，直接添加
            String content = (String) input;
            if (!content.isEmpty()) {
                lines.add(content);
            }
        } else {
            // 其他类型转为字符串
            lines.add(input.toString());
        }
        
        return lines;
    }
    
    /**
     * 创建文件（如果目录不存在则创建）
     */
    private File createFile(String filepath) throws IOException {
        File file = new File(filepath);
        
        // 如果是相对路径，使用当前工作目录
        if (!file.isAbsolute()) {
            String workDir = System.getProperty("user.dir");
            file = new File(workDir, filepath);
        }
        
        // 创建父目录（如果不存在）
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + parentDir.getAbsolutePath());
            }
        }
        
        // 如果文件已存在，先删除
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("Failed to delete existing file: " + file.getAbsolutePath());
            }
        }
        
        return file;
    }
    
    /**
     * 写入行到文件
     */
    private void writeLinesToFile(File file, List<String> lines) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            for (String line : lines) {
                writer.write(line);
                writer.write(System.lineSeparator());
            }
        }
    }
}

