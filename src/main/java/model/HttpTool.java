package model;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * HTTP工具数据模型
 * 支持单个命令和命令数组两种格式，以及新的commandList格式
 */
public class HttpTool {
    private String toolName;
    private Object command; // 可以是String或List<String>
    private Object commandList; // 新格式：List<HttpToolCommandData>
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
        // 优先使用新的commandList格式
        if (commandList != null) {
            return getCommandsFromCommandList();
        }
        
        // 回退到旧格式
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
     * 从commandList中提取命令字符串
     * @return 命令字符串列表
     */
    private List<String> getCommandsFromCommandList() {
        List<String> commands = new ArrayList<>();
        if (commandList instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> commandDataList = (List<Object>) commandList;
            for (Object cmdData : commandDataList) {
                if (cmdData instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> cmdMap = (Map<String, Object>) cmdData;
                    Object cmdValue = cmdMap.get("command");
                    if (cmdValue instanceof String) {
                        commands.add((String) cmdValue);
                    }
                } else if (cmdData instanceof HttpToolCommandData) {
                    HttpToolCommandData data = (HttpToolCommandData) cmdData;
                    commands.add(data.getCommand());
                }
            }
        }
        return commands;
    }
    
    /**
     * 获取完整的命令数据列表
     * @return 命令数据列表
     */
    public List<HttpToolCommandData> getCommandDataList() {
        List<HttpToolCommandData> commandDataList = new ArrayList<>();
        
        if (commandList != null && commandList instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> rawList = (List<Object>) commandList;
            for (Object cmdData : rawList) {
                if (cmdData instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> cmdMap = (Map<String, Object>) cmdData;
                    
                    String cmd = (String) cmdMap.get("command");
                    Boolean favor = (Boolean) cmdMap.get("favor");
                    String note = (String) cmdMap.get("note");
                    String workDir = (String) cmdMap.get("workDir");
                    
                    HttpToolCommandData data = new HttpToolCommandData(
                        cmd != null ? cmd : "",
                        favor != null ? favor : false,
                        note != null ? note : "",
                        workDir != null ? workDir : ""
                    );
                    commandDataList.add(data);
                } else if (cmdData instanceof HttpToolCommandData) {
                    commandDataList.add((HttpToolCommandData) cmdData);
                }
            }
        }
        
        return commandDataList;
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
                ", commandList=" + commandList +
                ", favor=" + favor +
                '}';
    }
    
    // getters and setters for commandList
    public Object getCommandList() {
        return commandList;
    }
    
    public void setCommandList(Object commandList) {
        this.commandList = commandList;
    }
    
    /**
     * 命令数据结构
     * 用于新的配置文件格式
     */
    public static class HttpToolCommandData {
        private String command;
        private boolean favor;
        private String note;
        private String workDir;
        
        public HttpToolCommandData() {}
        
        public HttpToolCommandData(String command, boolean favor, String note, String workDir) {
            this.command = command;
            this.favor = favor;
            this.note = note;
            this.workDir = workDir;
        }
        
        public String getCommand() {
            return command;
        }
        
        public void setCommand(String command) {
            this.command = command;
        }
        
        public boolean isFavor() {
            return favor;
        }
        
        public void setFavor(boolean favor) {
            this.favor = favor;
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
        
        @Override
        public String toString() {
            return "HttpToolCommandData{" +
                    "command='" + command + '\'' +
                    ", favor=" + favor +
                    ", note='" + note + '\'' +
                    ", workDir='" + workDir + '\'' +
                    '}';
        }
    }
} 