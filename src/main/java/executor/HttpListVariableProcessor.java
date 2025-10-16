package executor;

import burp.api.montoya.http.message.requests.HttpRequest;
import manager.ApiManager;
import util.TempFileManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * HttpList变量处理器
 * 实现策略模式，专门处理多选HTTP请求的变量替换
 */
public class HttpListVariableProcessor {
    
    /**
     * 处理httpList相关变量
     * @param variables 变量映射
     * @param allRequests 所有选中的HTTP请求
     */
    public static void processHttpListVariables(Map<String, String> variables, List<HttpRequest> allRequests) {
        if (allRequests == null || allRequests.isEmpty()) {
            return;
        }
        
        try {
            // 添加请求数量统计
            addCountVariables(variables, allRequests);
            
            // 处理URL相关变量
            processUrlVariables(variables, allRequests);
            
            // 处理主机相关变量
            processHostVariables(variables, allRequests);
            
            // 处理路径相关变量
            processPathVariables(variables, allRequests);
            
            // 处理端口和协议变量
            processPortAndProtocolVariables(variables, allRequests);
            
            // 添加统计摘要
            addSummaryVariables(variables, allRequests);
            
        } catch (Exception e) {
            handleError("添加httpList变量失败", e, variables);
        }
    }
    
    /**
     * 添加数量统计变量
     */
    private static void addCountVariables(Map<String, String> variables, List<HttpRequest> allRequests) {
        variables.put("httpList.count", String.valueOf(allRequests.size()));
    }
    
    /**
     * 处理URL相关变量
     */
    private static void processUrlVariables(Map<String, String> variables, List<HttpRequest> allRequests) {
        List<String> urls = extractUrls(allRequests);
        
        if (!urls.isEmpty()) {
            try {
                String urlsFilePath = TempFileManager.createUrlsFile(urls);
                variables.put("httpList.requests.urls", urlsFilePath);
                variables.put("httpList.requests.urls.count", String.valueOf(urls.size()));
                variables.put("httpList.requests.urls.list", String.join("\n", urls));
                variables.put("httpList.requests.urls.comma", String.join(",", urls));
                variables.put("httpList.requests.urls.space", String.join(" ", urls));
            } catch (Exception e) {
                handleError("创建URLs临时文件失败", e, variables);
            }
        }
    }
    
    /**
     * 处理主机相关变量
     */
    private static void processHostVariables(Map<String, String> variables, List<HttpRequest> allRequests) {
        List<String> hosts = extractHosts(allRequests);
        
        if (!hosts.isEmpty()) {
            try {
                String hostsFilePath = TempFileManager.createHostsFile(hosts);
                variables.put("httpList.requests.hosts", hostsFilePath);
                variables.put("httpList.requests.hosts.count", String.valueOf(hosts.size()));
                variables.put("httpList.requests.hosts.list", String.join("\n", hosts));
                variables.put("httpList.requests.hosts.comma", String.join(",", hosts));
            } catch (Exception e) {
                handleError("创建主机临时文件失败", e, variables);
            }
        }
    }
    
    /**
     * 处理路径相关变量
     */
    private static void processPathVariables(Map<String, String> variables, List<HttpRequest> allRequests) {
        List<String> paths = extractPaths(allRequests);
        
        if (!paths.isEmpty()) {
            try {
                String pathsFilePath = TempFileManager.createPathsFile(paths);
                variables.put("httpList.requests.paths", pathsFilePath);
                variables.put("httpList.requests.paths.count", String.valueOf(paths.size()));
                variables.put("httpList.requests.paths.list", String.join("\n", paths));
            } catch (Exception e) {
                handleError("创建路径临时文件失败", e, variables);
            }
        }
    }
    
    /**
     * 处理端口和协议变量
     */
    private static void processPortAndProtocolVariables(Map<String, String> variables, List<HttpRequest> allRequests) {
        // 处理端口
        List<String> ports = extractPorts(allRequests);
        if (!ports.isEmpty()) {
            variables.put("httpList.requests.ports.list", String.join(",", ports));
            variables.put("httpList.requests.ports.count", String.valueOf(ports.size()));
        }
        
        // 处理协议
        List<String> protocols = extractProtocols(allRequests);
        if (!protocols.isEmpty()) {
            variables.put("httpList.requests.protocols.list", String.join(",", protocols));
        }
    }
    
    /**
     * 添加统计摘要变量
     */
    private static void addSummaryVariables(Map<String, String> variables, List<HttpRequest> allRequests) {
        int urlCount = getIntVariable(variables, "httpList.requests.urls.count");
        int hostCount = getIntVariable(variables, "httpList.requests.hosts.count");
        
        String summary = String.format(
            "总请求数: %d, 唯一URL: %d, 唯一主机: %d", 
            allRequests.size(), urlCount, hostCount
        );
        variables.put("httpList.summary", summary);
    }
    
    /**
     * 提取所有URL
     */
    private static List<String> extractUrls(List<HttpRequest> allRequests) {
        return allRequests.stream()
            .map(request -> {
                try {
                    return request.url();
                } catch (Exception e) {
                    return null;
                }
            })
            .filter(url -> url != null && !url.isEmpty())
            .distinct()
            .collect(Collectors.toList());
    }
    
    /**
     * 提取所有主机名
     */
    private static List<String> extractHosts(List<HttpRequest> allRequests) {
        return allRequests.stream()
            .map(request -> {
                try {
                    return request.httpService().host();
                } catch (Exception e) {
                    return null;
                }
            })
            .filter(host -> host != null && !host.isEmpty())
            .distinct()
            .collect(Collectors.toList());
    }
    
    /**
     * 提取所有路径
     */
    private static List<String> extractPaths(List<HttpRequest> allRequests) {
        return allRequests.stream()
            .map(request -> {
                try {
                    return request.path();
                } catch (Exception e) {
                    return null;
                }
            })
            .filter(path -> path != null && !path.isEmpty())
            .distinct()
            .collect(Collectors.toList());
    }
    
    /**
     * 提取所有端口
     */
    private static List<String> extractPorts(List<HttpRequest> allRequests) {
        return allRequests.stream()
            .map(request -> {
                try {
                    return String.valueOf(request.httpService().port());
                } catch (Exception e) {
                    return null;
                }
            })
            .filter(port -> port != null)
            .distinct()
            .collect(Collectors.toList());
    }
    
    /**
     * 提取所有协议
     */
    private static List<String> extractProtocols(List<HttpRequest> allRequests) {
        return allRequests.stream()
            .map(request -> {
                try {
                    return request.httpService().secure() ? "https" : "http";
                } catch (Exception e) {
                    return null;
                }
            })
            .filter(protocol -> protocol != null)
            .distinct()
            .collect(Collectors.toList());
    }
    
    /**
     * 获取整数变量值
     */
    private static int getIntVariable(Map<String, String> variables, String key) {
        try {
            String value = variables.get(key);
            return value != null ? Integer.parseInt(value) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * 处理错误
     */
    private static void handleError(String message, Exception e, Map<String, String> variables) {
        String errorMsg = message + ": " + e.getMessage();
        variables.put("httpList.error", errorMsg);
    }
    
    /**
     * 记录错误日志
     */
    private static void logError(String message, Exception e) {
        // 日志记录已移除
    }
} 