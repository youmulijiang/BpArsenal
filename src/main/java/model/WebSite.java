package model;

/**
 * 网站数据模型
 */
public class WebSite {
    private String url;
    private String desc;
    private boolean favor;
    
    public WebSite() {}
    
    public WebSite(String url, String desc, boolean favor) {
        this.url = url;
        this.desc = desc;
        this.favor = favor;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public void setDesc(String desc) {
        this.desc = desc;
    }
    
    public boolean isFavor() {
        return favor;
    }
    
    public void setFavor(boolean favor) {
        this.favor = favor;
    }
    
    @Override
    public String toString() {
        return "WebSite{" +
                "url='" + url + '\'' +
                ", desc='" + desc + '\'' +
                ", favor=" + favor +
                '}';
    }
} 