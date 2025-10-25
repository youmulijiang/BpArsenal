package executor.dsl.functions;

import executor.dsl.DslException;
import executor.dsl.FunctionHandler;
import executor.dsl.HttpContext;

import java.util.List;

/**
 * 计数函数
 * 用法: count(list)
 * 示例: count(httpList.requests)
 */
public class CountFunction implements FunctionHandler {
    
    @Override
    public Object execute(List<Object> args, HttpContext context) throws DslException {
        if (args.size() != 1) {
            throw new DslException("count() requires 1 argument: count(list)");
        }
        
        Object collection = args.get(0);
        
        if (collection instanceof List) {
            return ((List<?>) collection).size();
        } else if (collection instanceof String) {
            return ((String) collection).length();
        } else if (collection != null) {
            return 1;
        }
        
        return 0;
    }
    
    @Override
    public String getName() {
        return "count";
    }
    
    @Override
    public String getDescription() {
        return "Count elements in list or characters in string";
    }
    
    @Override
    public String getUsage() {
        return "count(list)";
    }
}

