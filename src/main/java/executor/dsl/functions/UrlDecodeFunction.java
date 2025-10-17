package executor.dsl.functions;

import executor.dsl.DslException;
import executor.dsl.FunctionHandler;
import executor.dsl.HttpContext;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * URL解码函数
 * 用法: urldecode(data)
 * 示例: urldecode(http.request.query)
 */
public class UrlDecodeFunction implements FunctionHandler {
    
    @Override
    public Object execute(List<Object> args, HttpContext context) throws DslException {
        if (args.size() != 1) {
            throw new DslException("urldecode() requires 1 argument: urldecode(data)");
        }
        
        String data = args.get(0) != null ? args.get(0).toString() : "";
        
        try {
            return URLDecoder.decode(data, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            throw new DslException("URL decode failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getName() {
        return "urldecode";
    }
    
    @Override
    public String getDescription() {
        return "URL decode data";
    }
    
    @Override
    public String getUsage() {
        return "urldecode(data)";
    }
}

