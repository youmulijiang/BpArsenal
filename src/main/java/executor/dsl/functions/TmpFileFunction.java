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
 * tmpFile函数实现 - 生成临时文件
 * 
 * 支持的格式：
 * 1. %tmpFile(content, extension)% - 创建包含单个内容的临时文件
 * 2. %tmpFile(listContent, extension)% - 创建包含列表内容的临时文件（每行一项）
 * 
 * 示例：
 * - %tmpFile(http.request.body.raw, 'txt')% - 将请求体保存到临时.txt文件
 * - %tmpFile(httpList.requests.*.request.url, 'txt')% - 将URL列表保存到临时文件
 * - %tmpFile(http.request.headers.*, 'txt')% - 将所有请求头保存到临时文件
 */
public class TmpFileFunction implements FunctionHandler {
    
    private static final String TEMP_PREFIX = "bparsenal_tmp_";
    
    @Override
    public String getName() {
        return "tmpFile";
    }
    
    @Override
    public String getDescription() {
        return "Create temporary file with content (auto-deleted on exit)";
    }
    
    @Override
    public Object execute(List<Object> args, HttpContext context) throws DslException {
        if (args.size() < 2) {
            throw new DslException("tmpFile requires 2 arguments: tmpFile(content, extension)");
        }
        
        Object input = args.get(0);
        String extension = args.get(1).toString();
        
        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }
        
        try {
            // 处理输入内容
            List<String> lines = convertToLines(input);
            
            if (lines.isEmpty()) {
                throw new DslException("tmpFile: content is empty");
            }
            
            // 创建临时文件
            File tempFile = createTempFile(extension);
            tempFile.deleteOnExit();
            
            // 写入内容
            writeLinesToFile(tempFile, lines);
            
            return tempFile.getAbsolutePath();
            
        } catch (IOException e) {
            throw new DslException("Failed to create temporary file: " + e.getMessage(), e);
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
     * 创建临时文件
     */
    private File createTempFile(String extension) throws IOException {
        // 获取临时目录
        String tempDir = System.getProperty("java.io.tmpdir");
        
        // 创建唯一的临时文件名
        String fileName = TEMP_PREFIX + System.currentTimeMillis() + "_" + 
                         Thread.currentThread().getId() + extension;
        
        File tempFile = new File(tempDir, fileName);
        
        // 确保文件不存在
        if (tempFile.exists()) {
            tempFile.delete();
        }
        
        return tempFile;
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

