package model;

/**
 * 第三方工具数据模型
 */
public class ThirdPartyTool {
    private String toolName;
    private String startCommand;
    private boolean favor;
    private boolean autoStart;
    
    public ThirdPartyTool() {}
    
    public ThirdPartyTool(String toolName, String startCommand, boolean favor, boolean autoStart) {
        this.toolName = toolName;
        this.startCommand = startCommand;
        this.favor = favor;
        this.autoStart = autoStart;
    }
    
    public String getToolName() {
        return toolName;
    }
    
    public void setToolName(String toolName) {
        this.toolName = toolName;
    }
    
    public String getStartCommand() {
        return startCommand;
    }
    
    public void setStartCommand(String startCommand) {
        this.startCommand = startCommand;
    }
    
    public boolean isFavor() {
        return favor;
    }
    
    public void setFavor(boolean favor) {
        this.favor = favor;
    }
    
    public boolean isAutoStart() {
        return autoStart;
    }
    
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }
    
    @Override
    public String toString() {
        return "ThirdPartyTool{" +
                "toolName='" + toolName + '\'' +
                ", startCommand='" + startCommand + '\'' +
                ", favor=" + favor +
                ", autoStart=" + autoStart +
                '}';
    }
} 