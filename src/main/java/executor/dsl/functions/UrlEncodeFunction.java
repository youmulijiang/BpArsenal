package executor.dsl.functions;

import executor.dsl.DslException;
import executor.dsl.FunctionHandler;
import executor.dsl.HttpContext;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * URL编码函数
 * 用法: urlencode(data)
 * 示例: urlencode(http.request.path)
 */
public class UrlEncodeFunction implements FunctionHandler {
    
    @Override
    public Object execute(List<Object> args, HttpContext context) throws DslException {
        if (args.size() != 1) {
            throw new DslException("urlencode() requires 1 argument: urlencode(data)");
        }
        
        String data = args.get(0) != null ? args.get(0).toString() : "";
        
        try {
            return URLEncoder.encode(data, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            throw new DslException("URL encode failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getName() {
        return "urlencode";
    }
    
    @Override
    public String getDescription() {
        return "URL encode data";
    }
    
    @Override
    public String getUsage() {
        return "urlencode(data)";
    }
}

