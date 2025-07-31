package model;

/**
 * HTTP工具命令实体
 * 表示工具的一个具体命令实例，用于在表格中独立显示
 */
public class HttpToolCommand {
    private String toolName;
    private String command;
    private String category;
    private boolean favor;
    private String note;        // 备注信息
    private String workDir;     // 工作目录
    private int commandIndex; // 在原工具命令列表中的索引
    private HttpTool parentTool; // 父工具对象
    
    public HttpToolCommand() {}
    
    public HttpToolCommand(String toolName, String command, String category, boolean favor, int commandIndex, HttpTool parentTool) {
        this.toolName = toolName;
        this.command = command;
        this.category = category;
        this.favor = favor;
        this.commandIndex = commandIndex;
        this.parentTool = parentTool;
    }
    
    public HttpToolCommand(String toolName, String command, String category, boolean favor, String note, String workDir, int commandIndex, HttpTool parentTool) {
        this.toolName = toolName;
        this.command = command;
        this.category = category;
        this.favor = favor;
        this.note = note;
        this.workDir = workDir;
        this.commandIndex = commandIndex;
        this.parentTool = parentTool;
    }
    
    public String getToolName() {
        return toolName;
    }
    
    public void setToolName(String toolName) {
        this.toolName = toolName;
    }
    
    public String getCommand() {
        return command;
    }
    
    public void setCommand(String command) {
        this.command = command;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public boolean isFavor() {
        return favor;
    }
    
    public void setFavor(boolean favor) {
        this.favor = favor;
    }
    
    public int getCommandIndex() {
        return commandIndex;
    }
    
    public void setCommandIndex(int commandIndex) {
        this.commandIndex = commandIndex;
    }
    
    public HttpTool getParentTool() {
        return parentTool;
    }
    
    public void setParentTool(HttpTool parentTool) {
        this.parentTool = parentTool;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public String getWorkDir() {
        return workDir;
    }
    
    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }
    
    /**
     * 获取显示用的工具名称（包含命令索引）
     * @return 显示名称
     */
    public String getDisplayName() {
        if (parentTool != null && parentTool.getCommandCount() > 1) {
            return toolName + " (" + (commandIndex + 1) + "/" + parentTool.getCommandCount() + ")";
        }
        return toolName;
    }
    
    /**
     * 获取命令描述（截取前50字符）
     * @return 命令描述
     */
    public String getCommandDescription() {
        if (command == null) {
            return "";
        }
        
        if (command.length() > 50) {
            return command.substring(0, 47) + "...";
        }
        return command;
    }
    
    @Override
    public String toString() {
        return "HttpToolCommand{" +
                "toolName='" + toolName + '\'' +
                ", command='" + command + '\'' +
                ", category='" + category + '\'' +
                ", favor=" + favor +
                ", note='" + note + '\'' +
                ", workDir='" + workDir + '\'' +
                ", commandIndex=" + commandIndex +
                '}';
    }
} 