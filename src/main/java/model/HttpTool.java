package model;

import java.util.List;
import java.util.ArrayList;

/**
 * HTTP工具数据模型
 * 支持单个命令和命令数组两种格式
 */
public class HttpTool {
    private String toolName;
    private Object command; // 可以是String或List<String>
    private boolean favor;
    
    public HttpTool() {}
    
    public HttpTool(String toolName, String command, boolean favor) {
        this.toolName = toolName;
        this.command = command;
        this.favor = favor;
    }
    
    public HttpTool(String toolName, List<String> commands, boolean favor) {
        this.toolName = toolName;
        this.command = commands;
        this.favor = favor;
    }
    
    public String getToolName() {
        return toolName;
    }
    
    public void setToolName(String toolName) {
        this.toolName = toolName;
    }
    
    /**
     * 获取单个命令（向后兼容）
     * @return 第一个命令或null
     */
    public String getCommand() {
        if (command instanceof String) {
            return (String) command;
        } else if (command instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> commands = (List<String>) command;
            return commands.isEmpty() ? null : commands.get(0);
        }
        return null;
    }
    
    /**
     * 设置单个命令
     * @param command 命令字符串
     */
    public void setCommand(String command) {
        this.command = command;
    }
    
    /**
     * 获取所有命令
     * @return 命令列表
     */
    public List<String> getCommands() {
        if (command instanceof String) {
            List<String> commands = new ArrayList<>();
            commands.add((String) command);
            return commands;
        } else if (command instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> commands = (List<String>) command;
            return new ArrayList<>(commands);
        }
        return new ArrayList<>();
    }
    
    /**
     * 设置命令列表
     * @param commands 命令列表
     */
    public void setCommands(List<String> commands) {
        this.command = commands;
    }
    
    /**
     * 获取命令数量
     * @return 命令数量
     */
    public int getCommandCount() {
        return getCommands().size();
    }
    
    /**
     * 获取指定索引的命令
     * @param index 命令索引
     * @return 命令字符串
     */
    public String getCommandAt(int index) {
        List<String> commands = getCommands();
        if (index >= 0 && index < commands.size()) {
            return commands.get(index);
        }
        return null;
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
                ", command=" + command +
                ", favor=" + favor +
                '}';
    }
} 