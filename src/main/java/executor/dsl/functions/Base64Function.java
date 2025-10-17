package executor.dsl.functions;

import executor.dsl.DslException;
import executor.dsl.FunctionHandler;
import executor.dsl.HttpContext;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * Base64编解码函数
 * 用法: base64(data, "encode"|"decode")
 * 示例: base64(http.request.headers.authorization, "decode")
 */
public class Base64Function implements FunctionHandler {
    
    @Override
    public Object execute(List<Object> args, HttpContext context) throws DslException {
        if (args.size() < 1 || args.size() > 2) {
            throw new DslException("base64() requires 1-2 arguments: base64(data[, mode])");
        }
        
        String data = args.get(0) != null ? args.get(0).toString() : "";
        String mode = args.size() > 1 ? args.get(1).toString().toLowerCase() : "encode";
        
        try {
            switch (mode) {
                case "encode":
                case "enc":
                case "e":
                    return Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
                    
                case "decode":
                case "dec":
                case "d":
                    byte[] decoded = Base64.getDecoder().decode(data);
                    return new String(decoded, StandardCharsets.UTF_8);
                    
                default:
                    throw new DslException("Invalid mode: " + mode + ". Use 'encode' or 'decode'");
            }
        } catch (IllegalArgumentException e) {
            throw new DslException("Base64 operation failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getName() {
        return "base64";
    }
    
    @Override
    public String getDescription() {
        return "Base64 encode or decode data";
    }
    
    @Override
    public String getUsage() {
        return "base64(data[, mode]) - mode: encode(default) or decode";
    }
}

