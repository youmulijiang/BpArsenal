package util;

import java.util.Arrays;
import java.util.List;

/**
 * HTTP Tool Placeholder Documentation
 * Provides descriptions and examples for all available variables (standardized naming system)
 */
public class PlaceholderDocumentation {
    
    /**
     * Placeholder Variable Class
     */
    public static class PlaceholderVariable {
        private final String name;
        private final String description;
        private final String example;
        private final String category;
        
        public PlaceholderVariable(String name, String description, String example, String category) {
            this.name = name;
            this.description = description;
            this.example = example;
            this.category = category;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getExample() { return example; }
        public String getCategory() { return category; }
        
        @Override
        public String toString() {
            return String.format("%s - %s\n  Example: %s", name, description, example);
        }
    }
    
    /**
     * Get HTTP request basic variables
     * @return Request basic variables list
     */
    public static List<PlaceholderVariable> getRequestBasicVariables() {
        return Arrays.asList(
            new PlaceholderVariable("%http.request.url%", "Complete request URL", "https://example.com/api/users?id=123", "Request Basic"),
            new PlaceholderVariable("%http.request.method%", "HTTP request method", "GET, POST, PUT, DELETE", "Request Basic"),
            new PlaceholderVariable("%http.request.path%", "Request path", "/api/users", "Request Basic"),
            new PlaceholderVariable("%http.request.query%", "Query string", "id=123&name=test", "Request Basic"),
            new PlaceholderVariable("%http.request.host%", "Target hostname", "example.com", "Request Basic"),
            new PlaceholderVariable("%http.request.port%", "Target port", "80, 443, 8080", "Request Basic"),
            new PlaceholderVariable("%http.request.secure%", "Whether using HTTPS", "true, false", "Request Basic"),
            new PlaceholderVariable("%http.request.protocol%", "Protocol type", "http, https", "Request Basic")
        );
    }
    
    /**
     * Get HTTP request header variables
     * @return Request header variables list
     */
    public static List<PlaceholderVariable> getRequestHeaderVariables() {
        return Arrays.asList(
            new PlaceholderVariable("%http.request.headers.user.agent%", "User agent string", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)", "Request Headers"),
            new PlaceholderVariable("%http.request.headers.content.type%", "请求内容类型", "application/json", "请求头部"),
            new PlaceholderVariable("%http.request.headers.content.length%", "请求内容长度", "1024", "请求头部"),
            new PlaceholderVariable("%http.request.headers.authorization%", "认证头信息", "Bearer eyJhbGciOiJIUzI1NiIs...", "请求头部"),
            new PlaceholderVariable("%http.request.headers.referer%", "来源页面", "https://example.com/login", "请求头部"),
            new PlaceholderVariable("%http.request.headers.accept%", "接受的内容类型", "application/json, text/html", "请求头部"),
            new PlaceholderVariable("%http.request.headers.accept.language%", "接受的语言", "zh-CN,zh;q=0.9,en;q=0.8", "请求头部"),
            new PlaceholderVariable("%http.request.headers.accept.encoding%", "接受的编码", "gzip, deflate, br", "请求头部"),
            new PlaceholderVariable("%http.request.headers.cookies%", "完整Cookie字符串", "sessionId=abc123; token=xyz789", "请求头部"),
            new PlaceholderVariable("%http.request.headers.x.forwarded.for%", "转发IP", "192.168.1.100", "请求头部"),
            new PlaceholderVariable("%http.request.headers.host%", "Host头部", "example.com:8080", "请求头部"),
            new PlaceholderVariable("%http.request.headers.origin%", "请求来源", "https://example.com", "请求头部")
        );
    }
    
    /**
     * 获取HTTP请求参数变量
     * @return 请求参数变量列表
     */
    public static List<PlaceholderVariable> getRequestParameterVariables() {
        return Arrays.asList(
            new PlaceholderVariable("%http.request.params.url.{name}%", "URL参数值", "%http.request.params.url.page%", "请求参数"),
            new PlaceholderVariable("%http.request.params.body.{name}%", "POST参数值", "%http.request.params.body.username%", "请求参数"),
            new PlaceholderVariable("%http.request.params.cookie.{name}%", "Cookie参数值", "%http.request.params.cookie.sessionid%", "请求参数"),
            new PlaceholderVariable("%http.request.params.url.count%", "URL参数数量", "5", "请求参数"),
            new PlaceholderVariable("%http.request.params.body.count%", "POST参数数量", "3", "请求参数"),
            new PlaceholderVariable("%http.request.params.cookie.count%", "Cookie参数数量", "2", "请求参数"),
            new PlaceholderVariable("%http.request.params.total.count%", "总参数数量", "10", "请求参数")
        );
    }
    
    /**
     * 获取HTTP请求Cookie变量
     * @return 请求Cookie变量列表
     */
    public static List<PlaceholderVariable> getRequestCookieVariables() {
        return Arrays.asList(
            new PlaceholderVariable("%http.request.cookies.{name}%", "Cookie值", "%http.request.cookies.jsessionid%", "请求Cookie"),
            new PlaceholderVariable("%http.request.cookies.sessionid%", "会话ID", "ABC123XYZ789", "请求Cookie"),
            new PlaceholderVariable("%http.request.cookies.token%", "令牌Cookie", "jwt_token_value", "请求Cookie"),
            new PlaceholderVariable("%http.request.cookies.csrf%", "CSRF令牌", "csrf_token_value", "请求Cookie")
        );
    }
    
    /**
     * 获取HTTP请求体变量
     * @return 请求体变量列表
     */
    public static List<PlaceholderVariable> getRequestBodyVariables() {
        return Arrays.asList(
            new PlaceholderVariable("%http.request.body%", "完整请求体内容", "{'username':'admin','password':'123456'}", "请求体"),
            new PlaceholderVariable("%http.request.body.len%", "请求体长度", "256", "请求体"),
            new PlaceholderVariable("%http.request.body.empty%", "请求体是否为空", "true, false", "请求体"),
            new PlaceholderVariable("%http.request.body.type%", "请求体类型", "json, xml, form, text", "请求体"),
            new PlaceholderVariable("%http.request.body.format.json%", "是否为JSON格式", "true, false", "请求体"),
            new PlaceholderVariable("%http.request.body.format.xml%", "是否为XML格式", "true, false", "请求体"),
            new PlaceholderVariable("%http.request.body.format.form%", "是否为表单格式", "true, false", "请求体"),
            new PlaceholderVariable("%http.request.body.format.text%", "是否为纯文本", "true, false", "请求体"),
            new PlaceholderVariable("%http.request.body.encoding.base64%", "是否Base64编码", "true, false", "请求体"),
            new PlaceholderVariable("%http.request.body.encoding.url%", "是否URL编码", "true, false", "请求体"),
            new PlaceholderVariable("%http.request.body.base64.decoded%", "Base64解码内容", "原始文本内容", "请求体"),
            new PlaceholderVariable("%http.request.body.base64.decoded.len%", "Base64解码后长度", "128", "请求体"),
            new PlaceholderVariable("%http.request.body.url.decoded%", "URL解码内容", "username=admin&password=123", "请求体"),
            new PlaceholderVariable("%http.request.body.url.decoded.len%", "URL解码后长度", "64", "请求体")
        );
    }
    
    /**
     * 获取HTTP响应状态变量
     * @return 响应状态变量列表
     */
    public static List<PlaceholderVariable> getResponseStatusVariables() {
        return Arrays.asList(
            new PlaceholderVariable("%http.response.status%", "HTTP响应状态码", "200, 404, 500", "响应状态"),
            new PlaceholderVariable("%http.response.reason%", "响应状态描述", "OK, Not Found, Internal Server Error", "响应状态")
        );
    }
    
    /**
     * 获取HTTP响应头部变量
     * @return 响应头部变量列表
     */
    public static List<PlaceholderVariable> getResponseHeaderVariables() {
        return Arrays.asList(
            new PlaceholderVariable("%http.response.headers.content.type%", "响应内容类型", "application/json; charset=utf-8", "响应头部"),
            new PlaceholderVariable("%http.response.headers.content.length%", "响应内容长度", "2048", "响应头部"),
            new PlaceholderVariable("%http.response.headers.server%", "服务器信息", "Apache/2.4.41", "响应头部"),
            new PlaceholderVariable("%http.response.headers.cache.control%", "缓存控制", "no-cache, must-revalidate", "响应头部"),
            new PlaceholderVariable("%http.response.headers.location%", "重定向位置", "https://example.com/redirect", "响应头部"),
            new PlaceholderVariable("%http.response.headers.set.cookie%", "设置Cookie", "sessionId=new_value; Path=/", "响应头部"),
            new PlaceholderVariable("%http.response.headers.x.frame.options%", "X-Frame-Options", "DENY, SAMEORIGIN", "响应头部"),
            new PlaceholderVariable("%http.response.headers.x.xss.protection%", "XSS保护", "1; mode=block", "响应头部"),
            new PlaceholderVariable("%http.response.headers.strict.transport.security%", "HSTS", "max-age=31536000", "响应头部"),
            new PlaceholderVariable("%http.response.headers.content.security.policy%", "CSP策略", "default-src 'self'", "响应头部")
        );
    }
    
    /**
     * 获取HTTP响应Cookie变量
     * @return 响应Cookie变量列表
     */
    public static List<PlaceholderVariable> getResponseCookieVariables() {
        return Arrays.asList(
            new PlaceholderVariable("%http.response.cookies.{name}%", "设置的Cookie值", "%http.response.cookies.jsessionid%", "响应Cookie"),
            new PlaceholderVariable("%http.response.cookies.sessionid%", "新会话ID", "Set-Cookie: sessionId=NEW123; Path=/", "响应Cookie"),
            new PlaceholderVariable("%http.response.cookies.token%", "新令牌Cookie", "Set-Cookie: token=new_token; Secure", "响应Cookie")
        );
    }
    
    /**
     * 获取HTTP响应体变量
     * @return 响应体变量列表
     */
    public static List<PlaceholderVariable> getResponseBodyVariables() {
        return Arrays.asList(
            new PlaceholderVariable("%http.response.body%", "完整响应体内容", "{'result':'success','data':[...]}", "响应体"),
            new PlaceholderVariable("%http.response.body.len%", "响应体长度", "2048", "响应体"),
            new PlaceholderVariable("%http.response.body.empty%", "响应体是否为空", "true, false", "响应体"),
            new PlaceholderVariable("%http.response.body.type%", "响应体类型", "json, xml, html, text", "响应体"),
            new PlaceholderVariable("%http.response.body.format.json%", "是否为JSON格式", "true, false", "响应体"),
            new PlaceholderVariable("%http.response.body.format.xml%", "是否为XML格式", "true, false", "响应体"),
            new PlaceholderVariable("%http.response.body.format.html%", "是否为HTML格式", "true, false", "响应体"),
            new PlaceholderVariable("%http.response.body.format.text%", "是否为纯文本", "true, false", "响应体"),
            new PlaceholderVariable("%http.response.body.encoding.base64%", "是否Base64编码", "true, false", "响应体"),
            new PlaceholderVariable("%http.response.body.base64.decoded%", "Base64解码内容", "原始响应内容", "响应体"),
            new PlaceholderVariable("%http.response.body.base64.decoded.len%", "Base64解码后长度", "1024", "响应体")
        );
    }
    
    /**
     * 获取所有变量
     * @return 所有变量列表
     */
    public static List<PlaceholderVariable> getAllVariables() {
        List<PlaceholderVariable> allVariables = new java.util.ArrayList<>();
        allVariables.addAll(getRequestBasicVariables());
        allVariables.addAll(getRequestHeaderVariables());
        allVariables.addAll(getRequestParameterVariables());
        allVariables.addAll(getRequestCookieVariables());
        allVariables.addAll(getRequestBodyVariables());
        allVariables.addAll(getResponseStatusVariables());
        allVariables.addAll(getResponseHeaderVariables());
        allVariables.addAll(getResponseCookieVariables());
        allVariables.addAll(getResponseBodyVariables());
        return allVariables;
    }
    
    /**
     * 按分类获取变量
     * @param category 分类名称
     * @return 指定分类的变量列表
     */
    public static List<PlaceholderVariable> getVariablesByCategory(String category) {
        return getAllVariables().stream()
                .filter(var -> var.getCategory().equalsIgnoreCase(category))
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Generate placeholder usage examples
     * @return Usage examples string
     */
    public static String generateUsageExamples() {
        StringBuilder examples = new StringBuilder();
        examples.append("=== HTTP Tool Placeholder Usage Examples (Standardized Naming System) ===\n\n");
        
        examples.append("1. SQLMap注入测试:\n");
        examples.append("   python sqlmap.py -u %http.request.url% --user-agent=\"%http.request.headers.user.agent%\" --cookie=\"%http.request.headers.cookies%\" --dbs\n\n");
        
        examples.append("2. Dirb目录扫描:\n");
        examples.append("   dirb %http.request.protocol%://%http.request.host%:%http.request.port% /usr/share/dirb/wordlists/common.txt\n\n");
        
        examples.append("3. Nmap端口扫描:\n");
        examples.append("   nmap -sS -sV %http.request.host% -p 1-10000\n\n");
        
        examples.append("4. Curl请求重放:\n");
        examples.append("   curl -X %http.request.method% -H \"User-Agent: %http.request.headers.user.agent%\" ");
        examples.append("-H \"Cookie: %http.request.headers.cookies%\" -H \"Authorization: %http.request.headers.authorization%\" ");
        examples.append("--data \"%http.request.body%\" \"%http.request.url%\"\n\n");
        
        examples.append("5. Gobuster目录爆破:\n");
        examples.append("   gobuster dir -u %http.request.protocol%://%http.request.host%:%http.request.port% -w /usr/share/wordlists/dirb/common.txt\n\n");
        
        examples.append("6. 条件执行（根据响应状态和内容）:\n");
        examples.append("   if [ \"%http.response.status%\" = \"200\" ] && [ \"%http.response.body.format.json%\" = \"true\" ]; then\n");
        examples.append("       echo \"API返回成功的JSON响应，长度: %http.response.body.len%\"\n");
        examples.append("   fi\n\n");
        
        examples.append("7. 基于请求体类型的处理:\n");
        examples.append("   if [ \"%http.request.body.format.json%\" = \"true\" ]; then\n");
        examples.append("       echo \"处理JSON请求体，长度: %http.request.body.len%\"\n");
        examples.append("   elif [ \"%http.request.body.format.form%\" = \"true\" ]; then\n");
        examples.append("       echo \"处理表单请求体\"\n");
        examples.append("   fi\n\n");
        
        examples.append("8. 提取特定参数值:\n");
        examples.append("   echo \"用户ID: %http.request.params.url.userid%\"\n");
        examples.append("   echo \"用户名: %http.request.params.body.username%\"\n");
        examples.append("   echo \"会话ID: %http.request.cookies.jsessionid%\"\n\n");
        
        return examples.toString();
    }
    
    /**
     * 生成完整的占位符文档
     * @return 完整文档字符串
     */
    public static String generateFullDocumentation() {
        StringBuilder doc = new StringBuilder();
        doc.append("=== BpArsenal HTTP工具占位符完整文档（规范化命名体系）===\n\n");
        
        doc.append("命名规范说明:\n");
        doc.append("- 所有占位符采用层次化命名: http.{request|response}.{category}.{subcategory}.{name}\n");
        doc.append("- 请求相关: http.request.*\n");
        doc.append("- 响应相关: http.response.*\n");
        doc.append("- 头部信息: headers.{header_name}（连字符替换为点号）\n");
        doc.append("- 参数信息: params.{type}.{param_name}\n");
        doc.append("- Cookie信息: cookies.{cookie_name}\n");
        doc.append("- 请求体/响应体: body.{attribute}\n\n");
        
        String[] categories = {
            "请求基础", "请求头部", "请求参数", "请求Cookie", "请求体",
            "响应状态", "响应头部", "响应Cookie", "响应体"
        };
        
        for (String category : categories) {
            doc.append(String.format("=== %s ===\n", category));
            List<PlaceholderVariable> vars = getVariablesByCategory(category);
            for (PlaceholderVariable var : vars) {
                doc.append(String.format("  %-40s : %s\n", var.getName(), var.getDescription()));
                doc.append(String.format("  %-40s   示例: %s\n\n", "", var.getExample()));
            }
            doc.append("\n");
        }
        
        doc.append(generateUsageExamples());
        
        return doc.toString();
    }

    /**
     * 获取HTTP List变量文档
     * @return HTTP List变量文档
     */
    private static String getHttpListVariablesDoc() {
        StringBuilder doc = new StringBuilder();
        
        doc.append("📋 HTTP List变量（多选数据包支持）\n");
        doc.append("当选中多个HTTP数据包时，可使用以下变量：\n\n");
        
        doc.append("🔢 统计信息：\n");
        doc.append("  %httpList.count%                     - 选中的HTTP请求总数\n");
        doc.append("  %httpList.summary%                   - 统计摘要信息\n\n");
        
        doc.append("🌐 URL相关：\n");
        doc.append("  %httpList.requests.urls%             - 包含所有URL的临时文件路径\n");
        doc.append("  %httpList.requests.urls.count%       - 唯一URL数量\n");
        doc.append("  %httpList.requests.urls.list%        - 所有URL（换行分隔）\n");
        doc.append("  %httpList.requests.urls.comma%       - 所有URL（逗号分隔）\n");
        doc.append("  %httpList.requests.urls.space%       - 所有URL（空格分隔）\n\n");
        
        doc.append("🖥️ 主机相关：\n");
        doc.append("  %httpList.requests.hosts%            - 包含所有主机名的临时文件路径\n");
        doc.append("  %httpList.requests.hosts.count%      - 唯一主机数量\n");
        doc.append("  %httpList.requests.hosts.list%       - 所有主机名（换行分隔）\n");
        doc.append("  %httpList.requests.hosts.comma%      - 所有主机名（逗号分隔）\n\n");
        
        doc.append("📂 路径相关：\n");
        doc.append("  %httpList.requests.paths%            - 包含所有路径的临时文件路径\n");
        doc.append("  %httpList.requests.paths.count%      - 唯一路径数量\n");
        doc.append("  %httpList.requests.paths.list%       - 所有路径（换行分隔）\n\n");
        
        doc.append("🔌 端口和协议：\n");
        doc.append("  %httpList.requests.ports.list%       - 所有端口（逗号分隔）\n");
        doc.append("  %httpList.requests.ports.count%      - 唯一端口数量\n");
        doc.append("  %httpList.requests.protocols.list%   - 所有协议（逗号分隔）\n\n");
        
        doc.append("⚠️ 注意事项：\n");
        doc.append("  • 临时文件会在程序退出时自动删除\n");
        doc.append("  • URL和主机名会自动去重\n");
        doc.append("  • 当只选中一个请求时，http.request.url等传统变量仍然可用\n");
        doc.append("  • 多选时，传统变量使用第一个选中的请求数据\n\n");
        
        return doc.toString();
    }

    /**
     * 获取使用示例文档
     * @return 使用示例文档
     */
    private static String getUsageExamplesDoc() {
        StringBuilder doc = new StringBuilder();
        
        doc.append("📝 使用示例\n\n");
        
        doc.append("1. 单个URL扫描（传统方式）：\n");
        doc.append("   dirsearch -u \"%http.request.url%\"\n\n");
        
        doc.append("2. 多个URL批量扫描（使用httpList）：\n");
        doc.append("   dirsearch -l %httpList.requests.urls%\n\n");
        
        doc.append("3. Nuclei批量扫描：\n");
        doc.append("   nuclei -l %httpList.requests.urls% -t vulnerabilities/\n\n");
        
        doc.append("4. Ffuf批量模糊测试：\n");
        doc.append("   ffuf -u FUZZ -w %httpList.requests.urls%\n\n");
        
        doc.append("5. 自定义批量处理脚本：\n");
        doc.append("   python batch_scan.py --urls %httpList.requests.urls% --output results.txt\n\n");
        
        doc.append("6. 主机名批量解析：\n");
        doc.append("   nslookup -batch %httpList.requests.hosts%\n\n");
        
        doc.append("7. 混合使用传统变量和httpList：\n");
        doc.append("   sqlmap -u \"%http.request.url%\" --batch --threads 5 && \\\n");
        doc.append("   nuclei -l %httpList.requests.urls% -t cves/\n\n");
        
        doc.append("8. 条件命令示例：\n");
        doc.append("   if [ %httpList.count% -gt 1 ]; then\n");
        doc.append("     echo \"批量扫描 %httpList.count% 个目标\"\n");
        doc.append("     masscan -l %httpList.requests.hosts% -p 80,443,8080\n");
        doc.append("   else\n");
        doc.append("     echo \"单目标扫描\"\n");
        doc.append("     nmap -sV \"%http.request.host%\"\n");
        doc.append("   fi\n\n");
        
        return doc.toString();
    }

    /**
     * 获取基础HTTP变量文档
     * @return 基础HTTP变量文档
     */
    private static String getBasicHttpVariablesDoc() {
        StringBuilder doc = new StringBuilder();
        
        doc.append("🌐 基础HTTP变量\n");
        doc.append("标准的HTTP请求和响应信息变量：\n\n");
        
        // 请求基础信息
        doc.append("📋 请求基础信息：\n");
        for (PlaceholderVariable var : getRequestBasicVariables()) {
            doc.append(String.format("  %-35s - %s\n", var.getName(), var.getDescription()));
        }
        doc.append("\n");
        
        // 请求头部信息
        doc.append("📋 请求头部信息：\n");
        for (PlaceholderVariable var : getRequestHeaderVariables()) {
            doc.append(String.format("  %-35s - %s\n", var.getName(), var.getDescription()));
        }
        doc.append("\n");
        
        return doc.toString();
    }
    
    /**
     * 获取高级变量文档
     * @return 高级变量文档
     */
    private static String getAdvancedVariablesDoc() {
        StringBuilder doc = new StringBuilder();
        
        doc.append("⚡ 高级变量\n");
        doc.append("扩展的HTTP分析和处理变量：\n\n");
        
        // 请求参数
        doc.append("📋 请求参数：\n");
        for (PlaceholderVariable var : getRequestParameterVariables()) {
            doc.append(String.format("  %-35s - %s\n", var.getName(), var.getDescription()));
        }
        doc.append("\n");
        
        // 请求体和响应体
        doc.append("📋 请求体变量：\n");
        for (PlaceholderVariable var : getRequestBodyVariables()) {
            doc.append(String.format("  %-35s - %s\n", var.getName(), var.getDescription()));
        }
        doc.append("\n");
        
        doc.append("📋 响应状态：\n");
        for (PlaceholderVariable var : getResponseStatusVariables()) {
            doc.append(String.format("  %-35s - %s\n", var.getName(), var.getDescription()));
        }
        doc.append("\n");
        
        return doc.toString();
    }
    
    /**
     * 获取所有占位符的帮助文档
     * @return 格式化的帮助文档字符串
     */
    public static String getAllPlaceholderDocumentation() {
        StringBuilder doc = new StringBuilder();
        
        doc.append("=== BpArsenal 占位符变量文档 ===\n\n");
        
        // 基础HTTP变量
        doc.append(getBasicHttpVariablesDoc());
        
        // HTTP List变量（新增）
        doc.append(getHttpListVariablesDoc());
        
        // 高级变量
        doc.append(getAdvancedVariablesDoc());
        
        // 使用示例
        doc.append(getUsageExamplesDoc());
        
        return doc.toString();
    }
} 