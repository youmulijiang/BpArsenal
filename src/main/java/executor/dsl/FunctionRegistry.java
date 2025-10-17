package executor.dsl;

import executor.dsl.functions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 函数注册中心
 * 采用单例模式，管理所有DSL函数的注册和查找
 */
public class FunctionRegistry {
    
    private static final Map<String, FunctionHandler> handlers = new HashMap<>();
    
    static {
        // 注册内置函数
        registerBuiltinFunctions();
    }
    
    /**
     * 注册内置函数
     */
    private static void registerBuiltinFunctions() {
        register(new HashFunction());
        register(new Base64Function());
        register(new JsonPathFunction());
        register(new RegexFunction());
        register(new UrlEncodeFunction());
        register(new UrlDecodeFunction());
        register(new JoinFunction());
        register(new UniqueFunction());
        register(new FilterFunction());
        register(new MapFunction());
        register(new CountFunction());
        register(new TmpFileFunction());
        register(new FileFunction());
    }
    
    /**
     * 注册函数
     * @param handler 函数处理器
     */
    public static void register(FunctionHandler handler) {
        if (handler != null) {
            handlers.put(handler.getName().toLowerCase(), handler);
        }
    }
    
    /**
     * 注册函数（指定名称）
     * @param name 函数名称
     * @param handler 函数处理器
     */
    public static void register(String name, FunctionHandler handler) {
        if (name != null && handler != null) {
            handlers.put(name.toLowerCase(), handler);
        }
    }
    
    /**
     * 获取函数处理器
     * @param name 函数名称
     * @return 函数处理器，不存在返回null
     */
    public static FunctionHandler getHandler(String name) {
        if (name == null) {
            return null;
        }
        return handlers.get(name.toLowerCase());
    }
    
    /**
     * 检查函数是否存在
     * @param name 函数名称
     * @return 是否存在
     */
    public static boolean hasFunction(String name) {
        return name != null && handlers.containsKey(name.toLowerCase());
    }
    
    /**
     * 获取所有已注册的函数名称
     * @return 函数名称集合
     */
    public static Set<String> getAllFunctionNames() {
        return handlers.keySet();
    }
    
    /**
     * 获取所有已注册的函数处理器
     * @return 函数处理器映射
     */
    public static Map<String, FunctionHandler> getAllHandlers() {
        return new HashMap<>(handlers);
    }
    
    /**
     * 注销函数
     * @param name 函数名称
     */
    public static void unregister(String name) {
        if (name != null) {
            handlers.remove(name.toLowerCase());
        }
    }
    
    /**
     * 清空所有注册的函数
     */
    public static void clear() {
        handlers.clear();
    }
}

