package model;

/**
 * HTTP工具数据模型
 */
public class HttpTool {
    private String toolName;
    private String Command;
    private boolean favor;
    
    public HttpTool() {}
    
    public HttpTool(String toolName, String command, boolean favor) {
        this.toolName = toolName;
        this.Command = command;
        this.favor = favor;
    }
    
    public String getToolName() {
        return toolName;
    }
    
    public void setToolName(String toolName) {
        this.toolName = toolName;
    }
    
    public String getCommand() {
        return Command;
    }
    
    public void setCommand(String command) {
        this.Command = command;
    }
    
    public boolean isFavor() {
        return favor;
    }
    
    public void setFavor(boolean favor) {
        this.favor = favor;
    }
    
    @Override
    public String toString() {
        return "HttpTool{" +
                "toolName='" + toolName + '\'' +
                ", Command='" + Command + '\'' +
                ", favor=" + favor +
                '}';
    }
} 