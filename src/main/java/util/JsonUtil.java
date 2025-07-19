package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;

/**
 * JSON工具类，提供JSON序列化和反序列化功能
 */
public class JsonUtil {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    
    /**
     * 将对象转换为JSON字符串
     * @param object 要转换的对象
     * @return JSON字符串
     * @throws JsonSyntaxException JSON转换异常
     */
    public static String toJson(Object object) throws JsonSyntaxException {
        if (object == null) {
            return "null";
        }
        return gson.toJson(object);
    }
    
    /**
     * 将JSON字符串转换为指定类型的对象
     * @param json JSON字符串
     * @param classOfT 目标类型
     * @param <T> 泛型类型
     * @return 转换后的对象
     * @throws JsonSyntaxException JSON转换异常
     */
    public static <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        return gson.fromJson(json, classOfT);
    }
    
    /**
     * 将JSON字符串转换为指定类型的对象（支持泛型）
     * @param json JSON字符串
     * @param typeOfT 目标类型
     * @param <T> 泛型类型
     * @return 转换后的对象
     * @throws JsonSyntaxException JSON转换异常
     */
    public static <T> T fromJson(String json, Type typeOfT) throws JsonSyntaxException {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        return gson.fromJson(json, typeOfT);
    }
    
    /**
     * 检查JSON字符串格式是否有效
     * @param json JSON字符串
     * @return 如果格式有效返回true，否则返回false
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        
        try {
            gson.fromJson(json, Object.class);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }
    
    /**
     * 格式化JSON字符串（美化输出）
     * @param json 原始JSON字符串
     * @return 格式化后的JSON字符串
     * @throws JsonSyntaxException JSON格式异常
     */
    public static String formatJson(String json) throws JsonSyntaxException {
        if (json == null || json.trim().isEmpty()) {
            return json;
        }
        
        Object jsonObject = gson.fromJson(json, Object.class);
        return gson.toJson(jsonObject);
    }
} 