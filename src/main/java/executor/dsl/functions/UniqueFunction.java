package executor.dsl.functions;

import executor.dsl.DslException;
import executor.dsl.FunctionHandler;
import executor.dsl.HttpContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 去重函数
 * 用法: unique(list)
 * 示例: unique(httpList.requests.hosts)
 */
public class UniqueFunction implements FunctionHandler {
    
    @Override
    public Object execute(List<Object> args, HttpContext context) throws DslException {
        if (args.size() != 1) {
            throw new DslException("unique() requires 1 argument: unique(list)");
        }
        
        Object collection = args.get(0);
        
        if (!(collection instanceof List)) {
            return collection;
        }
        
        List<?> list = (List<?>) collection;
        return list.stream()
            .distinct()
            .collect(Collectors.toList());
    }
    
    @Override
    public String getName() {
        return "unique";
    }
    
    @Override
    public String getDescription() {
        return "Remove duplicate elements from list";
    }
    
    @Override
    public String getUsage() {
        return "unique(list)";
    }
}

