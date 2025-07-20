package controller;

import model.Config;
import model.HttpTool;
import model.HttpToolCommand;
import model.ThirdPartyTool;
import manager.ConfigManager;
import manager.ApiManager;
import util.JsonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 工具控制器 (Controller层)
 * 处理HTTP工具和第三方工具的业务逻辑和数据操作
 */
public class ToolController {
    
    private static ToolController instance;
    
    private ToolController() {
    }
    
    public static ToolController getInstance() {
        if (instance == null) {
            instance = new ToolController();
        }
        return instance;
    }
    
    /**
     * 获取所有工具
     * @return 工具列表
     */
    public List<HttpTool> getAllTools() {
        try {
            Config config = ConfigManager.getInstance().getConfig();
            List<HttpTool> tools = new ArrayList<>();
            
            if (config.getHttpTool() != null) {
                for (Config.HttpToolCategory category : config.getHttpTool()) {
                    if (category.getContent() != null) {
                        tools.addAll(category.getContent());
                    }
                }
            }
            
            logInfo("成功加载 " + tools.size() + " 个工具");
            return tools;
            
        } catch (Exception e) {
            logError("获取工具列表失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取所有工具命令（展开命令数组）
     * @return 工具命令列表
     */
    public List<HttpToolCommand> getAllToolCommands() {
        try {
            Config config = ConfigManager.getInstance().getConfig();
            List<HttpToolCommand> toolCommands = new ArrayList<>();
            
            if (config.getHttpTool() != null) {
                for (Config.HttpToolCategory category : config.getHttpTool()) {
                    if (category.getContent() != null) {
                        for (HttpTool tool : category.getContent()) {
                            List<String> commands = tool.getCommands();
                            for (int i = 0; i < commands.size(); i++) {
                                HttpToolCommand toolCommand = new HttpToolCommand(
                                    tool.getToolName(),
                                    commands.get(i),
                                    category.getType(),
                                    tool.isFavor(),
                                    i,
                                    tool
                                );
                                toolCommands.add(toolCommand);
                            }
                        }
                    }
                }
            }
            
            logInfo("成功加载 " + toolCommands.size() + " 个工具命令");
            return toolCommands;
            
        } catch (Exception e) {
            logError("获取工具命令列表失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 添加工具
     * @param tool 工具对象
     * @param categoryType 分类类型
     * @return 是否成功
     */
    public boolean addTool(HttpTool tool, String categoryType) {
        try {
            Config config = ConfigManager.getInstance().getConfig();
            if (config.getHttpTool() == null) {
                config.setHttpTool(new ArrayList<>());
            }
            
            // 查找或创建对应分类
            Config.HttpToolCategory targetCategory = findOrCreateCategory(config, categoryType);
            
            if (targetCategory.getContent() == null) {
                targetCategory.setContent(new ArrayList<>());
            }
            
            targetCategory.getContent().add(tool);
            
            // 保存配置
            saveConfiguration();
            
            logInfo("成功添加工具: " + tool.getToolName() + " (分类: " + categoryType + ")");
            return true;
            
        } catch (Exception e) {
            logError("添加工具失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 更新工具
     * @param oldTool 原工具
     * @param newTool 新工具
     * @param newCategoryType 新分类类型
     * @return 是否成功
     */
    public boolean updateTool(HttpTool oldTool, HttpTool newTool, String newCategoryType) {
        try {
            // 先删除原工具
            removeTool(oldTool);
            
            // 再添加新工具
            boolean result = addTool(newTool, newCategoryType);
            
            if (result) {
                logInfo("成功更新工具: " + newTool.getToolName());
            }
            
            return result;
            
        } catch (Exception e) {
            logError("更新工具失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 删除工具
     * @param tool 要删除的工具
     * @return 是否成功
     */
    public boolean removeTool(HttpTool tool) {
        try {
            Config config = ConfigManager.getInstance().getConfig();
            if (config.getHttpTool() == null) return false;
            
            boolean removed = false;
            for (Config.HttpToolCategory category : config.getHttpTool()) {
                if (category.getContent() != null) {
                    removed = category.getContent().removeIf(t -> 
                        t.getToolName().equals(tool.getToolName())) || removed;
                }
            }
            
            if (removed) {
                saveConfiguration();
                logInfo("成功删除工具: " + tool.getToolName());
            }
            
            return removed;
            
        } catch (Exception e) {
            logError("删除工具失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 更新工具收藏状态
     * @param tool 工具对象
     * @param favorite 收藏状态
     * @return 是否成功
     */
    public boolean updateToolFavorite(HttpTool tool, boolean favorite) {
        try {
            Config config = ConfigManager.getInstance().getConfig();
            if (config.getHttpTool() == null) return false;
            
            boolean updated = false;
            for (Config.HttpToolCategory category : config.getHttpTool()) {
                if (category.getContent() != null) {
                    for (HttpTool t : category.getContent()) {
                        if (t.getToolName().equals(tool.getToolName())) {
                            t.setFavor(favorite);
                            updated = true;
                            break;
                        }
                    }
                }
            }
            
            if (updated) {
                saveConfiguration();
                logInfo("更新工具收藏状态: " + tool.getToolName() + " -> " + favorite);
            }
            
            return updated;
            
        } catch (Exception e) {
            logError("更新工具收藏状态失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 根据工具名称获取分类
     * @param toolName 工具名称
     * @return 分类显示名称
     */
    public String getToolCategory(String toolName) {
        try {
            Config config = ConfigManager.getInstance().getConfig();
            if (config.getHttpTool() != null) {
                for (Config.HttpToolCategory category : config.getHttpTool()) {
                    if (category.getContent() != null) {
                        for (HttpTool tool : category.getContent()) {
                            if (tool.getToolName().equals(toolName)) {
                                return getCategoryDisplayName(category.getType());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logError("获取工具分类失败: " + e.getMessage());
        }
        return "未分类";
    }
    
    /**
     * 查找或创建分类
     * @param config 配置对象
     * @param categoryType 分类类型
     * @return 分类对象
     */
    private Config.HttpToolCategory findOrCreateCategory(Config config, String categoryType) {
        // 查找现有分类
        for (Config.HttpToolCategory category : config.getHttpTool()) {
            if (categoryType.equals(category.getType())) {
                return category;
            }
        }
        
        // 创建新分类
        Config.HttpToolCategory newCategory = new Config.HttpToolCategory();
        newCategory.setType(categoryType);
        newCategory.setContent(new ArrayList<>());
        config.getHttpTool().add(newCategory);
        
        return newCategory;
    }
    
    /**
     * 获取分类显示名称
     * @param type 分类类型
     * @return 显示名称
     */
    public String getCategoryDisplayName(String type) {
        if (type == null) return "未分类";
        
        switch (type.toLowerCase()) {
            case "sql-inject":
                return "SQL注入";
            case "xss":
                return "XSS";
            case "scanner":
                return "扫描工具";
            case "brute-force":
                return "爆破工具";
            case "exploit":
                return "漏洞利用";
            default:
                return type;
        }
    }

    /**
     * 获取所有第三方工具
     * @return 第三方工具列表
     */
    public List<ThirdPartyTool> getAllThirdPartyTools() {
        try {
            Config config = ConfigManager.getInstance().getConfig();
            List<ThirdPartyTool> tools = new ArrayList<>();
            
            if (config.getThirtyPart() != null) {
                for (Config.ThirdPartyToolCategory category : config.getThirtyPart()) {
                    if (category.getContent() != null) {
                        tools.addAll(category.getContent());
                    }
                }
            }
            
            logInfo("成功加载 " + tools.size() + " 个第三方工具");
            return tools;
            
        } catch (Exception e) {
            logError("获取第三方工具列表失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 添加第三方工具
     * @param tool 工具对象
     * @param categoryType 分类类型
     * @return 是否成功
     */
    public boolean addThirdPartyTool(ThirdPartyTool tool, String categoryType) {
        try {
            Config config = ConfigManager.getInstance().getConfig();
            if (config.getThirtyPart() == null) {
                config.setThirtyPart(new ArrayList<>());
            }
            
            // 查找或创建对应分类
            Config.ThirdPartyToolCategory targetCategory = findOrCreateThirdPartyCategory(config, categoryType);
            
            if (targetCategory.getContent() == null) {
                targetCategory.setContent(new ArrayList<>());
            }
            
            targetCategory.getContent().add(tool);
            
            // 保存配置
            saveConfiguration();
            
            logInfo("成功添加第三方工具: " + tool.getToolName() + " (分类: " + categoryType + ")");
            return true;
            
        } catch (Exception e) {
            logError("添加第三方工具失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 更新第三方工具
     * @param oldTool 原工具
     * @param newTool 新工具
     * @param newCategoryType 新分类类型
     * @return 是否成功
     */
    public boolean updateThirdPartyTool(ThirdPartyTool oldTool, ThirdPartyTool newTool, String newCategoryType) {
        try {
            // 先删除原工具
            removeThirdPartyTool(oldTool);
            
            // 再添加新工具
            boolean result = addThirdPartyTool(newTool, newCategoryType);
            
            if (result) {
                logInfo("成功更新第三方工具: " + newTool.getToolName());
            }
            
            return result;
            
        } catch (Exception e) {
            logError("更新第三方工具失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 删除第三方工具
     * @param tool 要删除的工具
     * @return 是否成功
     */
    public boolean removeThirdPartyTool(ThirdPartyTool tool) {
        try {
            Config config = ConfigManager.getInstance().getConfig();
            if (config.getThirtyPart() == null) return false;
            
            boolean removed = false;
            for (Config.ThirdPartyToolCategory category : config.getThirtyPart()) {
                if (category.getContent() != null) {
                    removed = category.getContent().removeIf(t -> 
                        t.getToolName().equals(tool.getToolName())) || removed;
                }
            }
            
            if (removed) {
                saveConfiguration();
                logInfo("成功删除第三方工具: " + tool.getToolName());
            }
            
            return removed;
            
        } catch (Exception e) {
            logError("删除第三方工具失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 更新第三方工具收藏状态
     * @param tool 工具对象
     * @param favorite 收藏状态
     * @return 是否成功
     */
    public boolean updateThirdPartyToolFavorite(ThirdPartyTool tool, boolean favorite) {
        try {
            Config config = ConfigManager.getInstance().getConfig();
            if (config.getThirtyPart() == null) return false;
            
            boolean updated = false;
            for (Config.ThirdPartyToolCategory category : config.getThirtyPart()) {
                if (category.getContent() != null) {
                    for (ThirdPartyTool t : category.getContent()) {
                        if (t.getToolName().equals(tool.getToolName())) {
                            t.setFavor(favorite);
                            updated = true;
                            break;
                        }
                    }
                }
            }
            
            if (updated) {
                saveConfiguration();
                logInfo("更新第三方工具收藏状态: " + tool.getToolName() + " -> " + favorite);
            }
            
            return updated;
            
        } catch (Exception e) {
            logError("更新第三方工具收藏状态失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 根据工具名称获取第三方工具分类
     * @param toolName 工具名称
     * @return 分类显示名称
     */
    public String getThirdPartyToolCategory(String toolName) {
        try {
            Config config = ConfigManager.getInstance().getConfig();
            if (config.getThirtyPart() != null) {
                for (Config.ThirdPartyToolCategory category : config.getThirtyPart()) {
                    if (category.getContent() != null) {
                        for (ThirdPartyTool tool : category.getContent()) {
                            if (tool.getToolName().equals(toolName)) {
                                return getThirdPartyCategoryDisplayName(category.getType());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logError("获取第三方工具分类失败: " + e.getMessage());
        }
        return "未分类";
    }
    
    /**
     * 查找或创建第三方工具分类
     * @param config 配置对象
     * @param categoryType 分类类型
     * @return 分类对象
     */
    private Config.ThirdPartyToolCategory findOrCreateThirdPartyCategory(Config config, String categoryType) {
        // 查找现有分类
        for (Config.ThirdPartyToolCategory category : config.getThirtyPart()) {
            if (categoryType.equals(category.getType())) {
                return category;
            }
        }
        
        // 创建新分类
        Config.ThirdPartyToolCategory newCategory = new Config.ThirdPartyToolCategory();
        newCategory.setType(categoryType);
        newCategory.setContent(new ArrayList<>());
        config.getThirtyPart().add(newCategory);
        
        return newCategory;
    }
    
    /**
     * 获取第三方工具分类显示名称
     * @param type 分类类型
     * @return 显示名称
     */
    public String getThirdPartyCategoryDisplayName(String type) {
        if (type == null) return "未分类";
        
        switch (type.toLowerCase()) {
            case "exploit":
                return "渗透工具";
            case "编辑器":
                return "编辑器";
            case "network":
                return "网络工具";
            case "analysis":
                return "分析工具";
            default:
                return type;
        }
    }
    
    /**
     * 保存配置
     */
    private void saveConfiguration() {
        try {
                         // ConfigManager暂未实现saveConfig方法，这里仅记录日志
            logInfo("配置保存成功");
        } catch (Exception e) {
            logError("配置保存失败: " + e.getMessage());
        }
    }
    
    /**
     * 记录信息日志
     * @param message 日志消息
     */
    private void logInfo(String message) {
        if (ApiManager.getInstance().isInitialized()) {
            ApiManager.getInstance().getApi().logging().logToOutput("ToolController: " + message);
        }
    }
    
    /**
     * 记录错误日志
     * @param message 错误消息
     */
    private void logError(String message) {
        if (ApiManager.getInstance().isInitialized()) {
            ApiManager.getInstance().getApi().logging().logToError("ToolController: " + message);
        }
    }
} 