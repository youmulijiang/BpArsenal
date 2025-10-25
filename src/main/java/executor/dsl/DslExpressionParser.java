package executor.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DSL表达式解析器
 * 负责解析和执行DSL表达式，区分函数调用和链式属性访问
 * 
 * 支持的语法：
 * 1. 链式访问: http.request.headers.cookies.token
 * 2. 函数调用: json(http.request.body, "$.user.id")
 * 3. 嵌套表达式: hash(json(http.request.body, "$.data"), "sha256")
 */
public class DslExpressionParser {
    
    // 函数调用模式: function_name(arg1, arg2, ...)
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("^([a-zA-Z_][a-zA-Z0-9_]*)\\((.*)\\)$");
    
    // 链式访问模式: path.to.property[index]
    private static final Pattern CHAIN_PATTERN = Pattern.compile("^[a-zA-Z0-9._\\[\\]\\*]+$");
    
    private final ChainAccessor chainAccessor;
    
    public DslExpressionParser() {
        this.chainAccessor = new ChainAccessor();
    }
    
    /**
     * 解析并执行表达式
     * @param expression 表达式字符串
     * @param context HTTP上下文
     * @return 执行结果
     */
    public Object evaluate(String expression, HttpContext context) {
        if (expression == null || expression.isEmpty()) {
            return "";
        }
        
        expression = expression.trim();
        
        // 1. 尝试匹配函数调用
        Matcher functionMatcher = FUNCTION_PATTERN.matcher(expression);
        if (functionMatcher.matches()) {
            String functionName = functionMatcher.group(1);
            String argsString = functionMatcher.group(2);
            return evaluateFunction(functionName, argsString, context);
        }
        
        // 2. 尝试匹配链式访问
        if (CHAIN_PATTERN.matcher(expression).matches()) {
            return chainAccessor.navigate(expression, context);
        }
        
        // 3. 不匹配任何模式，返回原值
        return expression;
    }
    
    /**
     * 执行函数调用
     */
    private Object evaluateFunction(String functionName, String argsString, HttpContext context) {
        try {
            // 解析参数
            List<Object> args = parseArguments(argsString, context);
            
            // 获取函数处理器
            FunctionHandler handler = FunctionRegistry.getHandler(functionName);
            if (handler == null) {
                throw new DslException("Unknown function: " + functionName);
            }
            
            // 执行函数
            return handler.execute(args, context);
        } catch (Exception e) {
            throw new DslException("Function execution failed: " + functionName, e);
        }
    }
    
    /**
     * 解析函数参数
     * 支持：字符串字面量、数字、嵌套表达式
     */
    private List<Object> parseArguments(String argsString, HttpContext context) {
        List<Object> args = new ArrayList<>();
        
        if (argsString == null || argsString.trim().isEmpty()) {
            return args;
        }
        
        // 智能分割参数（考虑嵌套括号和引号）
        List<String> argStrings = splitArguments(argsString);
        
        for (String argString : argStrings) {
            argString = argString.trim();
            
            // 字符串字面量（单引号或双引号）
            if ((argString.startsWith("\"") && argString.endsWith("\"")) ||
                (argString.startsWith("'") && argString.endsWith("'"))) {
                args.add(argString.substring(1, argString.length() - 1));
            }
            // 数字
            else if (argString.matches("-?\\d+")) {
                args.add(Integer.parseInt(argString));
            }
            // 浮点数
            else if (argString.matches("-?\\d+\\.\\d+")) {
                args.add(Double.parseDouble(argString));
            }
            // 布尔值
            else if (argString.equalsIgnoreCase("true")) {
                args.add(true);
            }
            else if (argString.equalsIgnoreCase("false")) {
                args.add(false);
            }
            // null值
            else if (argString.equalsIgnoreCase("null")) {
                args.add(null);
            }
            // 否则作为表达式递归求值
            else {
                args.add(evaluate(argString, context));
            }
        }
        
        return args;
    }
    
    /**
     * 智能分割参数字符串
     * 考虑嵌套括号、引号和逗号
     */
    private List<String> splitArguments(String argsString) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int parenthesesDepth = 0;
        boolean inDoubleQuotes = false;
        boolean inSingleQuotes = false;
        boolean escapeNext = false;
        
        for (char c : argsString.toCharArray()) {
            if (escapeNext) {
                current.append(c);
                escapeNext = false;
                continue;
            }
            
            if (c == '\\') {
                current.append(c);
                escapeNext = true;
                continue;
            }
            
            if (c == '"' && !inSingleQuotes) {
                inDoubleQuotes = !inDoubleQuotes;
                current.append(c);
            } else if (c == '\'' && !inDoubleQuotes) {
                inSingleQuotes = !inSingleQuotes;
                current.append(c);
            } else if (c == '(' && !inDoubleQuotes && !inSingleQuotes) {
                parenthesesDepth++;
                current.append(c);
            } else if (c == ')' && !inDoubleQuotes && !inSingleQuotes) {
                parenthesesDepth--;
                current.append(c);
            } else if (c == ',' && !inDoubleQuotes && !inSingleQuotes && parenthesesDepth == 0) {
                // 遇到分隔符，保存当前参数
                if (current.length() > 0) {
                    result.add(current.toString().trim());
                    current = new StringBuilder();
                }
            } else {
                current.append(c);
            }
        }
        
        // 添加最后一个参数
        if (current.length() > 0) {
            result.add(current.toString().trim());
        }
        
        return result;
    }
}

