package executor.dsl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * Body上下文
 * 封装HTTP请求/响应体，支持多种格式解析
 */
public class BodyContext {
    private String raw;
    private int length;
    private String type;
    private JsonElement json;
    
    private BodyContext() {
    }
    
    /**
     * 从字符串构建Body上下文
     */
    public static BodyContext from(String bodyString) {
        BodyContext context = new BodyContext();
        context.raw = bodyString != null ? bodyString : "";
        context.length = context.raw.length();
        
        // 检测内容类型
        context.type = detectType(context.raw);
        
        // 尝试解析JSON
        if ("json".equals(context.type) || "json_array".equals(context.type)) {
            try {
                Gson gson = new Gson();
                context.json = gson.fromJson(context.raw, JsonElement.class);
            } catch (JsonSyntaxException e) {
                // 不是有效JSON
                context.json = null;
            }
        }
        
        return context;
    }
    
    /**
     * 检测Body类型
     */
    private static String detectType(String body) {
        if (body == null || body.isEmpty()) {
            return "empty";
        }
        
        String trimmed = body.trim();
        
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return "json";
        } else if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            return "json_array";
        } else if (trimmed.startsWith("<") && trimmed.endsWith(">")) {
            if (trimmed.toLowerCase().contains("<html")) {
                return "html";
            }
            return "xml";
        } else if (body.contains("=") && body.contains("&")) {
            return "form";
        } else {
            return "text";
        }
    }
    
    // Getters
    public String getRaw() { return raw; }
    public int getLength() { return length; }
    public String getType() { return type; }
    public JsonElement getJson() { return json; }
    
    /**
     * 获取属性（用于反射和动态访问）
     */
    public Object getProperty(String propertyName) {
        switch (propertyName.toLowerCase()) {
            case "raw": return raw;
            case "length": return length;
            case "len": return length;  // 别名
            case "type": return type;
            case "json": return json;
            default: return null;
        }
    }
}

