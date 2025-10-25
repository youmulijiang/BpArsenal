package executor.dsl.functions;

import executor.dsl.DslException;
import executor.dsl.FunctionHandler;
import executor.dsl.HttpContext;

import java.util.List;

/**
 * 映射函数（简化版）
 * 用法: map(list, propertyPath)
 * 示例: map(httpList.requests, "request.url")
 * 
 * 注意：这是简化实现，推荐使用通配符语法作为替代
 */
public class MapFunction implements FunctionHandler {
    
    @Override
    public Object execute(List<Object> args, HttpContext context) throws DslException {
        throw new DslException("map() function is not yet fully implemented. Use chain accessor with wildcards instead: httpList.requests.*.request.url");
    }
    
    @Override
    public String getName() {
        return "map";
    }
    
    @Override
    public String getDescription() {
        return "Map list elements to property values (not yet implemented)";
    }
    
    @Override
    public String getUsage() {
        return "map(list, propertyPath) - Use wildcard syntax as alternative: list.*.property";
    }
}

