package executor.dsl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 链式属性访问器
 * 支持点号导航、数组索引、通配符访问
 * 
 * 支持的语法：
 * - 简单属性: http.request.url
 * - 嵌套属性: http.request.headers.cookies.token
 * - 数组索引: httpList.requests[0].request.url
 * - 语义索引: httpList.requests.first.request.url
 * - 通配符: httpList.requests.*.request.url
 */
public class ChainAccessor {
    
    /**
     * 导航到指定路径
     * @param path 属性路径
     * @param context HTTP上下文
     * @return 属性值
     */
    public Object navigate(String path, HttpContext context) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        
        String[] parts = path.split("\\.");
        Object current = context;
        
        for (int i = 0; i < parts.length; i++) {
            if (current == null) {
                return null;
            }

            // Map键中含'.'的情况（如headers存储键为"user.agent"），
            // 优先用剩余路径拼接成完整键查找（长键优先）
            if (current instanceof Map) {
                Object joinedValue = getJoinedMapValue((Map<?, ?>) current, parts, i);
                if (joinedValue != null) {
                    return joinedValue;
                }
            }

            String part = parts[i];

            // 处理数组索引: requests[0]
            if (part.contains("[")) {
                current = handleIndexAccess(current, part);
            }
            // 处理通配符: requests.*
            else if (part.equals("*")) {
                current = handleWildcard(current, parts, i);
                break;  // 通配符后续处理在handleWildcard中完成
            }
            // 普通属性访问
            else {
                current = getProperty(current, part);
            }
        }

        return current;
    }

    /**
     * 在Map中按剩余路径拼接的完整键查找值
     * 依次尝试 "a.b.c"、"a.b"、"a"（最长优先），支持headers.user.agent这类键
     * @param map 目标Map
     * @param parts 完整路径分段
     * @param startIndex 当前段索引
     * @return 找到的值，未找到返回null
     */
    private Object getJoinedMapValue(Map<?, ?> map, String[] parts, int startIndex) {
        for (int end = parts.length; end > startIndex; end--) {
            String joined = String.join(".", Arrays.copyOfRange(parts, startIndex, end));
            Object value = lookupInMap(map, joined);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * 在Map中查找键（尝试原样、小写、点替换为下划线）
     */
    private Object lookupInMap(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (value != null) {
            return value;
        }
        value = map.get(key.toLowerCase());
        if (value != null) {
            return value;
        }
        return map.get(key.replace(".", "_"));
    }

    /**
     * 处理索引访问
     * 支持: [0], [1], first, last
     */
    private Object handleIndexAccess(Object obj, String expression) {
        int bracketIndex = expression.indexOf('[');
        String propertyName = expression.substring(0, bracketIndex);
        int closeIndex = expression.indexOf(']');
        if (closeIndex < 0) {
            // 缺少右括号，按普通属性处理，避免StringIndexOutOfBoundsException
            return propertyName.isEmpty() ? obj : getProperty(obj, propertyName);
        }
        String indexStr = expression.substring(bracketIndex + 1, closeIndex);
        
        // 先获取集合属性
        Object collection = propertyName.isEmpty() ? obj : getProperty(obj, propertyName);
        
        if (collection == null) {
            return null;
        }
        
        // 数字索引
        if (indexStr.matches("\\d+")) {
            int index = Integer.parseInt(indexStr);
            if (collection instanceof List) {
                List<?> list = (List<?>) collection;
                return index < list.size() ? list.get(index) : null;
            } else if (collection instanceof Object[]) {
                Object[] array = (Object[]) collection;
                return index < array.length ? array[index] : null;
            }
        }
        
        return null;
    }
    
    /**
     * 处理通配符访问
     * 例如: httpList.requests.*.request.url -> 返回所有requests的url列表
     */
    private Object handleWildcard(Object current, String[] parts, int currentIndex) {
        // 当前对象必须是集合
        if (!(current instanceof List)) {
            return null;
        }
        
        List<?> list = (List<?>) current;
        List<Object> results = new ArrayList<>();
        
        // 获取通配符后的路径
        if (currentIndex + 1 < parts.length) {
            String[] remainingParts = Arrays.copyOfRange(parts, currentIndex + 1, parts.length);
            String remainingPath = String.join(".", remainingParts);
            
            // 对每个元素执行剩余路径的访问
            for (Object item : list) {
                ChainAccessor subAccessor = new ChainAccessor();
                // 创建临时上下文，将item作为根对象
                Object value = navigateFromObject(item, remainingPath);
                if (value != null) {
                    results.add(value);
                }
            }
        } else {
            // 通配符是最后一个元素，返回整个列表
            return list;
        }
        
        return results;
    }
    
    /**
     * 从任意对象开始导航（不需要HttpContext）
     */
    private Object navigateFromObject(Object obj, String path) {
        String[] parts = path.split("\\.");
        Object current = obj;

        for (int i = 0; i < parts.length; i++) {
            if (current == null) {
                return null;
            }

            // Map键中含'.'的情况，优先用剩余路径拼接的完整键查找
            if (current instanceof Map) {
                Object joinedValue = getJoinedMapValue((Map<?, ?>) current, parts, i);
                if (joinedValue != null) {
                    return joinedValue;
                }
            }

            String part = parts[i];
            if (part.contains("[")) {
                current = handleIndexAccess(current, part);
            } else {
                current = getProperty(current, part);
            }
        }

        return current;
    }
    
    /**
     * 获取对象属性
     * 支持多种对象类型的属性访问
     */
    private Object getProperty(Object obj, String propertyName) {
        if (obj == null || propertyName == null) {
            return null;
        }
        
        // HttpContext 及其子类的特殊处理
        if (obj instanceof HttpContext ||
            obj instanceof HttpContext.HttpWrapper ||
            obj instanceof HttpRequestContext ||
            obj instanceof HttpResponseContext ||
            obj instanceof HttpListContext ||
            obj instanceof HttpRequestResponsePair ||
            obj instanceof BodyContext ||
            obj instanceof ParameterContext) {
            
            try {
                Method method = obj.getClass().getMethod("getProperty", String.class);
                return method.invoke(obj, propertyName);
            } catch (Exception e) {
                // 降级到通用方法
            }
        }
        
        // Map类型
        if (obj instanceof Map) {
            return lookupInMap((Map<?, ?>) obj, propertyName);
        }
        
        // List类型（处理语义化索引）
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            switch (propertyName.toLowerCase()) {
                case "first":
                    return list.isEmpty() ? null : list.get(0);
                case "last":
                    return list.isEmpty() ? null : list.get(list.size() - 1);
                case "size":
                case "count":
                    return list.size();
            }
        }
        
        // 使用反射获取getter方法
        return getPropertyByReflection(obj, propertyName);
    }
    
    /**
     * 使用反射获取属性
     */
    private Object getPropertyByReflection(Object obj, String propertyName) {
        try {
            // 尝试getXxx()方法
            String methodName = "get" + capitalize(propertyName);
            Method method = obj.getClass().getMethod(methodName);
            return method.invoke(obj);
        } catch (Exception e1) {
            try {
                // 尝试isXxx()方法（布尔值）
                String methodName = "is" + capitalize(propertyName);
                Method method = obj.getClass().getMethod(methodName);
                return method.invoke(obj);
            } catch (Exception e2) {
                // 尝试直接访问public字段
                try {
                    return obj.getClass().getField(propertyName).get(obj);
                } catch (Exception e3) {
                    return null;
                }
            }
        }
    }
    
    /**
     * 首字母大写
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}

