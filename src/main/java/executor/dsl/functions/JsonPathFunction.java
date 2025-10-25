package executor.dsl.functions;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import executor.dsl.DslException;
import executor.dsl.FunctionHandler;
import executor.dsl.HttpContext;

import java.util.List;

/**
 * JSON路径提取函数
 * 用法: json(jsonString, path)
 * 示例: json(http.request.body, "$.user.id")
 * 
 * 支持简单的JSON Path语法（不依赖外部库）
 */
public class JsonPathFunction implements FunctionHandler {
    
    private final Gson gson = new Gson();
    
    @Override
    public Object execute(List<Object> args, HttpContext context) throws DslException {
        if (args.size() != 2) {
            throw new DslException("json() requires 2 arguments: json(jsonString, path)");
        }
        
        String jsonString = args.get(0) != null ? args.get(0).toString() : "";
        String path = args.get(1).toString();
        
        try {
            JsonElement root = gson.fromJson(jsonString, JsonElement.class);
            return extractValue(root, path);
        } catch (Exception e) {
            throw new DslException("JSON path extraction failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * 提取JSON值
     * 支持简单路径: $.user.id, $.users[0].name
     */
    private Object extractValue(JsonElement element, String path) {
        // 移除开头的$. 或 $
        if (path.startsWith("$.")) {
            path = path.substring(2);
        } else if (path.startsWith("$")) {
            path = path.substring(1);
        }
        
        if (path.isEmpty()) {
            return jsonElementToObject(element);
        }
        
        String[] parts = path.split("\\.");
        JsonElement current = element;
        
        for (String part : parts) {
            if (current == null || current.isJsonNull()) {
                return null;
            }
            
            // 处理数组索引: users[0]
            if (part.contains("[")) {
                int bracketIndex = part.indexOf('[');
                String fieldName = part.substring(0, bracketIndex);
                String indexStr = part.substring(bracketIndex + 1, part.indexOf(']'));
                int index = Integer.parseInt(indexStr);
                
                // 先获取字段
                if (!fieldName.isEmpty() && current.isJsonObject()) {
                    current = current.getAsJsonObject().get(fieldName);
                }
                
                // 再获取数组元素
                if (current != null && current.isJsonArray()) {
                    JsonArray array = current.getAsJsonArray();
                    if (index >= 0 && index < array.size()) {
                        current = array.get(index);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
            // 普通字段访问
            else {
                if (current.isJsonObject()) {
                    current = current.getAsJsonObject().get(part);
                } else {
                    return null;
                }
            }
        }
        
        return jsonElementToObject(current);
    }
    
    /**
     * 将JsonElement转换为Java对象
     */
    private Object jsonElementToObject(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        } else if (element.isJsonPrimitive()) {
            if (element.getAsJsonPrimitive().isString()) {
                return element.getAsString();
            } else if (element.getAsJsonPrimitive().isNumber()) {
                return element.getAsNumber();
            } else if (element.getAsJsonPrimitive().isBoolean()) {
                return element.getAsBoolean();
            }
        } else if (element.isJsonObject() || element.isJsonArray()) {
            return element.toString();
        }
        return element.toString();
    }
    
    @Override
    public String getName() {
        return "json";
    }
    
    @Override
    public String getDescription() {
        return "Extract value from JSON using path expression";
    }
    
    @Override
    public String getUsage() {
        return "json(jsonString, path) - Example: json(http.request.body, \"$.user.id\")";
    }
}

