package util;

import model.Config;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * YAML工具类，提供YAML序列化和反序列化功能
 * 使用SnakeYAML库实现配置文件的读写操作
 */
public class YamlUtil {
    
    // 静态初始化Yaml实例，参考JsonUtil的Gson初始化方式
    private static final Yaml yaml = createYaml();
    
    /**
     * 创建配置好的Yaml实例
     */
    private static Yaml createYaml() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        
        Representer representer = new Representer(options);
        representer.getPropertyUtils().setSkipMissingProperties(true);
        // 配置Config类不输出类型标签，避免反序列化时的"Global tag is not allowed"错误
        representer.addClassTag(Config.class, Tag.MAP);
        
        return new Yaml(representer, options);
    }
    
    /**
     * 将对象转换为YAML字符串
     * @param object 要转换的对象
     * @return YAML字符串
     */
    public static String toYaml(Object object) {
        if (object == null) {
            return "null";
        }
        return yaml.dump(object);
    }
    
    /**
     * 将YAML字符串转换为指定类型的对象
     * @param yamlString YAML字符串
     * @param classOfT 目标类型
     * @param <T> 泛型类型
     * @return 转换后的对象
     */
    public static <T> T fromYaml(String yamlString, Class<T> classOfT) {
        if (yamlString == null || yamlString.trim().isEmpty()) {
            return null;
        }
        
        LoaderOptions loaderOptions = new LoaderOptions();
        Constructor constructor = new Constructor(classOfT, loaderOptions);
        // 设置属性工具，允许跳过缺失的属性（向后兼容）
        constructor.getPropertyUtils().setSkipMissingProperties(true);
        Yaml yamlParser = new Yaml(constructor);
        return yamlParser.load(yamlString);
    }
    
    /**
     * 从文件读取YAML
     * @param file 文件对象
     * @param classOfT 目标类型
     * @param <T> 泛型类型
     * @return 转换后的对象
     * @throws IOException 读取失败
     */
    public static <T> T fromYamlFile(File file, Class<T> classOfT) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader reader = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
            LoaderOptions loaderOptions = new LoaderOptions();
            Constructor constructor = new Constructor(classOfT, loaderOptions);
            // 设置属性工具，允许跳过缺失的属性（向后兼容）
            constructor.getPropertyUtils().setSkipMissingProperties(true);
            Yaml yamlParser = new Yaml(constructor);
            return yamlParser.load(reader);
        }
    }
    
    /**
     * 从InputStream读取YAML
     * @param inputStream 输入流
     * @param classOfT 目标类型
     * @param <T> 泛型类型
     * @return 转换后的对象
     */
    public static <T> T fromYamlStream(InputStream inputStream, Class<T> classOfT) {
        if (inputStream == null) {
            return null;
        }
        
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            LoaderOptions loaderOptions = new LoaderOptions();
            Constructor constructor = new Constructor(classOfT, loaderOptions);
            // 设置属性工具，允许跳过缺失的属性（向后兼容）
            constructor.getPropertyUtils().setSkipMissingProperties(true);
            Yaml yamlParser = new Yaml(constructor);
            return yamlParser.load(reader);
        } catch (IOException e) {
            throw new RuntimeException("读取YAML流失败", e);
        }
    }
    
    /**
     * 将对象写入YAML文件
     * @param object 要写入的对象
     * @param file 目标文件
     * @throws IOException 写入失败
     */
    public static void toYamlFile(Object object, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            yaml.dump(object, writer);
        }
    }
    
    /**
     * 检查YAML字符串格式是否有效
     * @param yamlString YAML字符串
     * @return 如果格式有效返回true，否则返回false
     */
    public static boolean isValidYaml(String yamlString) {
        if (yamlString == null || yamlString.trim().isEmpty()) {
            return false;
        }
        
        try {
            yaml.load(yamlString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 格式化YAML字符串（美化输出）
     * @param yamlString 原始YAML字符串
     * @return 格式化后的YAML字符串
     */
    public static String formatYaml(String yamlString) {
        if (yamlString == null || yamlString.trim().isEmpty()) {
            return yamlString;
        }
        
        Object yamlObject = yaml.load(yamlString);
        return yaml.dump(yamlObject);
    }
    
    /**
     * 将JSON字符串转换为YAML字符串
     * @param jsonString JSON字符串
     * @return YAML字符串
     */
    public static String jsonToYaml(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return "";
        }
        
        // 使用JsonUtil解析JSON，然后转换为YAML
        Object object = JsonUtil.fromJson(jsonString, Object.class);
        return toYaml(object);
    }
    
    /**
     * 将YAML字符串转换为JSON字符串
     * @param yamlString YAML字符串
     * @return JSON字符串
     */
    public static String yamlToJson(String yamlString) {
        if (yamlString == null || yamlString.trim().isEmpty()) {
            return "";
        }
        
        // 解析YAML，然后转换为JSON
        Object object = yaml.load(yamlString);
        return JsonUtil.toJson(object);
    }
}

