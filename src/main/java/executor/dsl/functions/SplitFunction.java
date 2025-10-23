package executor.dsl.functions;

import executor.dsl.DslException;
import executor.dsl.FunctionHandler;
import executor.dsl.HttpContext;

import java.util.ArrayList;
import java.util.List;

/**
 * 字符串切割函数
 * 用法: split(input, delimiter)
 * 
 * 支持的输入类型：
 * 1. 单个字符串 - 按分隔符切割成列表
 * 2. 字符串列表 - 对每个字符串切割并合并结果
 * 
 * 示例：
 * - split("https://baidu.com/path", "/") → ["https:", "", "baidu.com", "path"]
 * - split(httpList.urls, "/") → 切割所有URL并返回所有片段
 * - split("a,b,c", ",") → ["a", "b", "c"]
 */
public class SplitFunction implements FunctionHandler {
    
    @Override
    public Object execute(List<Object> args, HttpContext context) throws DslException {
        if (args.size() < 1 || args.size() > 2) {
            throw new DslException("split() requires 1-2 arguments: split(input[, delimiter])");
        }
        
        Object input = args.get(0);
        String delimiter = args.size() > 1 ? args.get(1).toString() : ",";
        
        // 处理空输入
        if (input == null) {
            return new ArrayList<>();
        }
        
        // 处理字符串输入
        if (input instanceof String) {
            return splitString((String) input, delimiter);
        }
        
        // 处理列表输入
        if (input instanceof List) {
            return splitList((List<?>) input, delimiter);
        }
        
        // 其他类型转为字符串处理
        return splitString(input.toString(), delimiter);
    }
    
    /**
     * 切割单个字符串
     */
    private List<String> splitString(String text, String delimiter) {
        List<String> result = new ArrayList<>();
        
        if (text == null || text.isEmpty()) {
            return result;
        }
        
        // 使用正则表达式切割（需要转义特殊字符）
        String[] parts = text.split(java.util.regex.Pattern.quote(delimiter));
        
        for (String part : parts) {
            result.add(part);
        }
        
        return result;
    }
    
    /**
     * 切割列表中的每个字符串
     */
    private List<String> splitList(List<?> list, String delimiter) {
        List<String> result = new ArrayList<>();
        
        for (Object item : list) {
            if (item != null) {
                String text = item.toString();
                List<String> parts = splitString(text, delimiter);
                result.addAll(parts);
            }
        }
        
        return result;
    }
    
    @Override
    public String getName() {
        return "split";
    }
    
    @Override
    public String getDescription() {
        return "Split string(s) by delimiter into list";
    }
    
    @Override
    public String getUsage() {
        return "split(input[, delimiter]) - Default delimiter: comma";
    }
}

