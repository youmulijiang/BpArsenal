package executor.dsl.functions;

import executor.dsl.DslException;
import executor.dsl.FunctionHandler;
import executor.dsl.HttpContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式提取函数
 * 用法: regex(text, pattern[, group])
 * 示例: regex(http.response.body, "token=([^&]+)", 1)
 */
public class RegexFunction implements FunctionHandler {
    
    @Override
    public Object execute(List<Object> args, HttpContext context) throws DslException {
        if (args.size() < 2 || args.size() > 3) {
            throw new DslException("regex() requires 2-3 arguments: regex(text, pattern[, group])");
        }
        
        String text = args.get(0) != null ? args.get(0).toString() : "";
        String patternStr = args.get(1).toString();
        int group = args.size() > 2 ? ((Number) args.get(2)).intValue() : 1;
        
        try {
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(text);
            
            List<String> matches = new ArrayList<>();
            while (matcher.find()) {
                if (group == 0) {
                    matches.add(matcher.group(0));
                } else if (group <= matcher.groupCount()) {
                    matches.add(matcher.group(group));
                }
            }
            
            // 如果只有一个匹配，返回字符串；否则返回列表
            if (matches.isEmpty()) {
                return null;
            } else if (matches.size() == 1) {
                return matches.get(0);
            } else {
                return matches;
            }
        } catch (Exception e) {
            throw new DslException("Regex extraction failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getName() {
        return "regex";
    }
    
    @Override
    public String getDescription() {
        return "Extract text using regular expression";
    }
    
    @Override
    public String getUsage() {
        return "regex(text, pattern[, group]) - Extract matching group (default: 1)";
    }
}

