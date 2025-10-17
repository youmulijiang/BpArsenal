package executor.dsl.functions;

import executor.dsl.DslException;
import executor.dsl.FunctionHandler;
import executor.dsl.HttpContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 连接函数
 * 用法: join(list, delimiter)
 * 示例: join(httpList.requests.*.request.url, "\n")
 */
public class JoinFunction implements FunctionHandler {
    
    @Override
    public Object execute(List<Object> args, HttpContext context) throws DslException {
        if (args.size() < 1 || args.size() > 2) {
            throw new DslException("join() requires 1-2 arguments: join(list[, delimiter])");
        }
        
        Object collection = args.get(0);
        String delimiter = args.size() > 1 ? args.get(1).toString() : ",";
        
        if (!(collection instanceof List)) {
            // 如果不是列表，直接返回字符串
            return collection != null ? collection.toString() : "";
        }
        
        List<?> list = (List<?>) collection;
        return list.stream()
            .map(obj -> obj != null ? obj.toString() : "")
            .collect(Collectors.joining(delimiter));
    }
    
    @Override
    public String getName() {
        return "join";
    }
    
    @Override
    public String getDescription() {
        return "Join list elements with delimiter";
    }
    
    @Override
    public String getUsage() {
        return "join(list[, delimiter]) - Default delimiter: comma";
    }
}

