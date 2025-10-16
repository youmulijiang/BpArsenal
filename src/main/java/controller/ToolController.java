package controller;

import model.Config;
import model.HttpTool;
import model.HttpToolCommand;
import model.ThirdPartyTool;
import model.WebSite;
import manager.ConfigManager;
import manager.ApiManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 工具控制器 (Controller层)
 * 处理HTTP工具、第三方工具和网站的业务逻辑和数据操作
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
            
            return tools;
            
        } catch (Exception e) {
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
                            // 优先处理新的commandList格式
                            List<HttpTool.HttpToolCommandData> commandDataList = tool.getCommandDataList();
                            if (!commandDataList.isEmpty()) {
                                // 使用新格式的数据
                                for (int i = 0; i < commandDataList.size(); i++) {
                                    HttpTool.HttpToolCommandData cmdData = commandDataList.get(i);
                                    HttpToolCommand toolCommand = new HttpToolCommand(
                                        tool.getToolName(),
                                        cmdData.getCommand(),
                                        category.getType(),
                                        cmdData.isFavor(),
                                        cmdData.getNote(),
                                        cmdData.getWorkDir(),
                                        i,
                                        tool
                                    );
                                    toolCommands.add(toolCommand);
                                }
                            } else {
                                // 回退到旧格式
                                List<String> commands = tool.getCommands();
                                for (int i = 0; i < commands.size(); i++) {
                                    HttpToolCommand toolCommand = new HttpToolCommand(
                                        tool.getToolName(),
                                        commands.get(i),
                                        category.getType(),
                                        tool.isFavor(),
                                        "",  // 默认note为空
                                        "",  // 默认workDir为空
                                        i,
                                        tool
                                    );
                                    toolCommands.add(toolCommand);
                                }
                            }
                        }
                    }
                }
            }
            
            return toolCommands;
            
        } catch (Exception e) {
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
            
            return true;
            
        } catch (Exception e) {
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
            }
            
            return result;
            
        } catch (Exception e) {
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
            }
            
            return removed;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 更新工具命令的详细信息（包括note和workDir）
     * @param toolCommand 要更新的工具命令
     * @param newCommand 新的命令字符串
     * @param newNote 新的备注
     * @param newWorkDir 新的工作目录
     * @param newFavor 新的收藏状态
     * @param newCategory 新的分类
     * @return 是否成功
     */
    public boolean updateToolCommand(HttpToolCommand toolCommand, String newCommand, String newNote, 
                                   String newWorkDir, boolean newFavor, String newCategory) {
        try {
            HttpTool parentTool = toolCommand.getParentTool();
            if (parentTool == null) {
                return false;
            }
            
            // 查找工具在配置中的位置
            Config config = ConfigManager.getInstance().getConfig();
            if (config.getHttpTool() == null) return false;
            
            HttpTool configTool = null;
            Config.HttpToolCategory currentCategory = null;
            
            // 找到工具所在的分类和工具对象
            for (Config.HttpToolCategory category : config.getHttpTool()) {
                if (category.getContent() != null) {
                    for (HttpTool tool : category.getContent()) {
                        if (tool.getToolName().equals(parentTool.getToolName())) {
                            configTool = tool;
                            currentCategory = category;
                            break;
                        }
                    }
                    if (configTool != null) break;
                }
            }
            
            if (configTool == null) {
                return false;
            }
            
            // 更新命令数据
            List<HttpTool.HttpToolCommandData> commandDataList = configTool.getCommandDataList();
            int commandIndex = toolCommand.getCommandIndex();
            
            if (commandIndex >= 0 && commandIndex < commandDataList.size()) {
                HttpTool.HttpToolCommandData cmdData = commandDataList.get(commandIndex);
                cmdData.setCommand(newCommand);
                cmdData.setNote(newNote);
                cmdData.setWorkDir(newWorkDir);
                cmdData.setFavor(newFavor);
                
                // 同时更新工具的基本信息
                configTool.setToolName(toolCommand.getToolName());
                configTool.setCommand(newCommand); // 保持向后兼容
                configTool.setFavor(newFavor);
                
                // 如果分类发生变化，需要移动工具
                if (!currentCategory.getType().equals(newCategory)) {
                    // 从当前分类中移除
                    currentCategory.getContent().remove(configTool);
                    
                    // 添加到新分类
                    Config.HttpToolCategory targetCategory = findOrCreateCategory(config, newCategory);
                    if (targetCategory.getContent() == null) {
                        targetCategory.setContent(new ArrayList<>());
                    }
                    targetCategory.getContent().add(configTool);
                }
                
                // 保存配置
                saveConfiguration();
                
                return true;
            } else {
                return false;
            }
            
        } catch (Exception e) {
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
            }
            
            return updated;
            
        } catch (Exception e) {
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
     * 获取所有HTTP工具分类列表
     * @return 分类名称列表
     */
    public List<String> getAllHttpToolCategories() {
        try {
            Config config = ConfigManager.getInstance().getConfig();
            List<String> categories = new ArrayList<>();
            
            // 添加"全部"选项 - 使用国际化文本
            categories.add(util.I18nManager.getInstance().getText("filter.all"));
            
            if (config.getHttpTool() != null) {
                for (Config.HttpToolCategory category : config.getHttpTool()) {
                    String categoryType = category.getType();
                    if (categoryType != null && !categoryType.trim().isEmpty()) {
                        // 使用原始的type值，不进行转换
                        if (!categories.contains(categoryType)) {
                            categories.add(categoryType);
                        }
                    }
                }
            }
            
            return categories;
            
        } catch (Exception e) {
            // 返回默认分类
            List<String> defaultCategories = new ArrayList<>();
            defaultCategories.add(util.I18nManager.getInstance().getText("filter.all"));
            defaultCategories.add("SQL注入");
            defaultCategories.add("XSS检测");
            defaultCategories.add("目录扫描");
            defaultCategories.add("漏洞扫描");
            defaultCategories.add("爆破工具");
            defaultCategories.add("编码分析");
            defaultCategories.add("认证测试");
            return defaultCategories;
        }
    }

    /**
     * 获取所有网站分类列表
     * @return 分类名称列表
     */
    public List<String> getAllWebSiteCategories() {
        try {
            Config config = ConfigManager.getInstance().getConfig();
            List<String> categories = new ArrayList<>();
            
            // 添加"全部"选项 - 使用国际化文本
            categories.add(util.I18nManager.getInstance().getText("filter.all"));
            
            if (config.getWebSite() != null) {
                for (Config.WebSiteCategory category : config.getWebSite()) {
                    String categoryType = category.getType();
                    if (categoryType != null && !categoryType.trim().isEmpty()) {
                        // 使用原始的type值
                        if (!categories.contains(categoryType)) {
                            categories.add(categoryType);
                        }
                    }
                }
            }
            
            return categories;
            
        } catch (Exception e) {
            // 返回默认分类
            List<String> defaultCategories = new ArrayList<>();
            defaultCategories.add(util.I18nManager.getInstance().getText("filter.all"));
            defaultCategories.add("OSINT");
            defaultCategories.add("Recon");
            defaultCategories.add(util.I18nManager.getInstance().getText("websites.category.vulnerability.db"));
            return defaultCategories;
        }
    }

    /**
     * 获取所有第三方工具分类列表
     * @return 分类名称列表
     */
    public List<String> getAllThirdPartyToolCategories() {
        try {
            Config config = ConfigManager.getInstance().getConfig();
            List<String> categories = new ArrayList<>();
            
            // 添加"全部"选项 - 使用国际化文本
            categories.add(util.I18nManager.getInstance().getText("filter.all"));
            
            if (config.getThirtyPart() != null) {
                for (Config.ThirdPartyToolCategory category : config.getThirtyPart()) {
                    String categoryType = category.getType();
                    if (categoryType != null && !categoryType.trim().isEmpty()) {
                        // 使用原始的type值
                        if (!categories.contains(categoryType)) {
                            categories.add(categoryType);
                        }
                    }
                }
            }
            
            return categories;
            
        } catch (Exception e) {
            // 返回默认分类
            List<String> defaultCategories = new ArrayList<>();
            defaultCategories.add(util.I18nManager.getInstance().getText("filter.all"));
            defaultCategories.add("exploit");
            defaultCategories.add(util.I18nManager.getInstance().getText("thirdparty.category.editor"));
            return defaultCategories;
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
            
            return tools;
            
        } catch (Exception e) {
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
            
            return true;
            
        } catch (Exception e) {
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
            }
            
            return result;
            
        } catch (Exception e) {
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
            }
            
            return removed;
            
        } catch (Exception e) {
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
            }
            
            return updated;
            
        } catch (Exception e) {
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
     * 获取所有网站
     * @return 网站列表
     */
    public List<WebSite> getAllWebSites() {
        try {
            Config config = ConfigManager.getInstance().getConfig();
            List<WebSite> websites = new ArrayList<>();
            
            if (config.getWebSite() != null) {
                for (Config.WebSiteCategory category : config.getWebSite()) {
                    if (category.getContent() != null) {
                        websites.addAll(category.getContent());
                    }
                }
            }
            
            return websites;
            
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * 添加网站
     * @param website 网站对象
     * @param categoryType 分类类型
     * @return 是否成功
     */
    public boolean addWebSite(WebSite website, String categoryType) {
        try {
            Config config = ConfigManager.getInstance().getConfig();
            if (config.getWebSite() == null) {
                config.setWebSite(new ArrayList<>());
            }
            
            // 查找或创建对应分类
            Config.WebSiteCategory targetCategory = findOrCreateWebSiteCategory(config, categoryType);
            
            if (targetCategory.getContent() == null) {
                targetCategory.setContent(new ArrayList<>());
            }
            
            targetCategory.getContent().add(website);
            
            // 保存配置
            saveConfiguration();
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 更新网站
     * @param oldWebsite 原网站
     * @param newWebsite 新网站
     * @param newCategoryType 新分类类型
     * @return 是否成功
     */
    public boolean updateWebSite(WebSite oldWebsite, WebSite newWebsite, String newCategoryType) {
        try {
            // 先删除原网站
            removeWebSite(oldWebsite);
            
            // 再添加新网站
            boolean result = addWebSite(newWebsite, newCategoryType);
            
            if (result) {
            }
            
            return result;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 删除网站
     * @param website 要删除的网站
     * @return 是否成功
     */
    public boolean removeWebSite(WebSite website) {
        try {
            Config config = ConfigManager.getInstance().getConfig();
            if (config.getWebSite() == null) return false;
            
            boolean removed = false;
            for (Config.WebSiteCategory category : config.getWebSite()) {
                if (category.getContent() != null) {
                    removed = category.getContent().removeIf(w -> 
                        w.getUrl().equals(website.getUrl()) && 
                        w.getDesc().equals(website.getDesc())) || removed;
                }
            }
            
            if (removed) {
                saveConfiguration();
            }
            
            return removed;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 更新网站收藏状态
     * @param website 网站对象
     * @param favorite 收藏状态
     * @return 是否成功
     */
    public boolean updateWebSiteFavorite(WebSite website, boolean favorite) {
        try {
            Config config = ConfigManager.getInstance().getConfig();
            if (config.getWebSite() == null) return false;
            
            boolean updated = false;
            for (Config.WebSiteCategory category : config.getWebSite()) {
                if (category.getContent() != null) {
                    for (WebSite w : category.getContent()) {
                        if (w.getUrl().equals(website.getUrl()) && 
                            w.getDesc().equals(website.getDesc())) {
                            w.setFavor(favorite);
                            updated = true;
                            break;
                        }
                    }
                }
            }
            
            if (updated) {
                saveConfiguration();
            }
            
            return updated;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 根据网站描述获取分类
     * @param websiteDesc 网站描述
     * @return 分类显示名称
     */
    public String getWebSiteCategory(String websiteDesc) {
        try {
            Config config = ConfigManager.getInstance().getConfig();
            if (config.getWebSite() != null) {
                for (Config.WebSiteCategory category : config.getWebSite()) {
                    if (category.getContent() != null) {
                        for (WebSite website : category.getContent()) {
                            if (website.getDesc().equals(websiteDesc)) {
                                return getWebSiteCategoryDisplayName(category.getType());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return "未分类";
    }
    
    /**
     * 查找或创建网站分类
     * @param config 配置对象
     * @param categoryType 分类类型
     * @return 分类对象
     */
    private Config.WebSiteCategory findOrCreateWebSiteCategory(Config config, String categoryType) {
        // 查找现有分类
        for (Config.WebSiteCategory category : config.getWebSite()) {
            if (categoryType.equals(category.getType())) {
                return category;
            }
        }
        
        // 创建新分类
        Config.WebSiteCategory newCategory = new Config.WebSiteCategory();
        newCategory.setType(categoryType);
        newCategory.setContent(new ArrayList<>());
        config.getWebSite().add(newCategory);
        
        return newCategory;
    }
    
    /**
     * 获取网站分类显示名称
     * @param type 分类类型
     * @return 显示名称
     */
    public String getWebSiteCategoryDisplayName(String type) {
        if (type == null) return "未分类";
        
        switch (type.toLowerCase()) {
            case "osint":
                return "OSINT";
            case "recon":
                return "信息收集";
            case "漏洞库":
                return "漏洞库";
            case "tools":
                return "在线工具";
            case "learning":
                return "学习资源";
            default:
                return type;
        }
    }
    
    /**
     * 保存配置到用户目录
     */
    private void saveConfiguration() {
        try {
            ConfigManager.getInstance().saveConfig();
        } catch (Exception e) {
            throw new RuntimeException("配置保存失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 记录信息日志
     * @param message 日志消息
     */
    private void logInfo(String message) {
    }
    
    /**
     * 记录错误日志
     * @param message 错误消息
     */
    private void logError(String message) {
    }
} 