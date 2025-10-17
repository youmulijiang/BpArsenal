package executor;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import executor.dsl.DslVariableReplacer;
import model.HttpToolCommand;

import java.util.List;

/**
 * 命令渲染策略处理器
 * 实现策略模式，处理不同类型的命令渲染逻辑
 * 
 * 版本2.0: 集成DSL表达式系统，支持函数调用和链式访问
 */
public class CommandRenderingStrategy {
    
    // DSL变量替换器实例
    private static final DslVariableReplacer dslReplacer = new DslVariableReplacer();
    
    /**
     * 渲染命令（DSL版本）
     * @param toolCommand 工具命令
     * @param primaryRequest 主要HTTP请求
     * @param httpResponse HTTP响应（可选）
     * @param allSelectedRequests 所有选中的HTTP请求
     * @return 渲染后的命令
     */
    public static String renderCommand(HttpToolCommand toolCommand, 
                                     HttpRequest primaryRequest, 
                                     HttpResponse httpResponse,
                                     List<HttpRequest> allSelectedRequests) {
        try {
            String command = toolCommand.getCommand();
            if (command == null || command.isEmpty()) {
                return "";
            }
            
            if (primaryRequest == null) {
                return command;
            }
            
            // 使用DSL替换器处理变量
            if (allSelectedRequests != null && allSelectedRequests.size() > 1) {
                // 批量请求模式
                return dslReplacer.replaceWithList(command, allSelectedRequests, null);
            } else {
                // 单个请求模式
                return dslReplacer.replace(command, primaryRequest, httpResponse);
            }
            
        } catch (Exception e) {
            return toolCommand.getCommand();
        }
    }
}
