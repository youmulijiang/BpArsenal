package executor.dsl;

import java.util.List;

/**
 * 函数处理器接口
 * 所有DSL函数都必须实现此接口
 */
public interface FunctionHandler {
    
    /**
     * 执行函数
     * @param args 函数参数列表
     * @param context HTTP上下文
     * @return 函数执行结果
     * @throws DslException 函数执行异常
     */
    Object execute(List<Object> args, HttpContext context) throws DslException;
    
    /**
     * 获取函数名称
     * @return 函数名称
     */
    default String getName() {
        return this.getClass().getSimpleName().replace("Function", "").toLowerCase();
    }
    
    /**
     * 获取函数描述
     * @return 函数描述
     */
    default String getDescription() {
        return "No description available";
    }
    
    /**
     * 获取函数使用示例
     * @return 使用示例
     */
    default String getUsage() {
        return getName() + "(...)";
    }
}

