package model;

import java.util.List;

/**
 * 配置文件数据模型
 */
public class Config {
    private List<HttpToolCategory> httpTool;
    private List<ThirdPartyToolCategory> thirtyPart;
    private List<WebSiteCategory> webSite;
    
    public List<HttpToolCategory> getHttpTool() {
        return httpTool;
    }
    
    public void setHttpTool(List<HttpToolCategory> httpTool) {
        this.httpTool = httpTool;
    }
    
    public List<ThirdPartyToolCategory> getThirtyPart() {
        return thirtyPart;
    }
    
    public void setThirtyPart(List<ThirdPartyToolCategory> thirtyPart) {
        this.thirtyPart = thirtyPart;
    }
    
    public List<WebSiteCategory> getWebSite() {
        return webSite;
    }
    
    public void setWebSite(List<WebSiteCategory> webSite) {
        this.webSite = webSite;
    }
    
    /**
     * HTTP工具分类
     */
    public static class HttpToolCategory {
        private String type;
        private List<HttpTool> content;
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public List<HttpTool> getContent() {
            return content;
        }
        
        public void setContent(List<HttpTool> content) {
            this.content = content;
        }
    }
    
    /**
     * 第三方工具分类
     */
    public static class ThirdPartyToolCategory {
        private String type;
        private List<ThirdPartyTool> content;
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public List<ThirdPartyTool> getContent() {
            return content;
        }
        
        public void setContent(List<ThirdPartyTool> content) {
            this.content = content;
        }
    }
    
    /**
     * 网站分类
     */
    public static class WebSiteCategory {
        private String type;
        private List<WebSite> content;
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public List<WebSite> getContent() {
            return content;
        }
        
        public void setContent(List<WebSite> content) {
            this.content = content;
        }
    }
} 