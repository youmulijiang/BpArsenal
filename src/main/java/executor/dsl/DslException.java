package executor.dsl;

/**
 * DSL异常
 * 用于表示DSL表达式解析和执行过程中的错误
 */
public class DslException extends RuntimeException {
    
    public DslException(String message) {
        super(message);
    }
    
    public DslException(String message, Throwable cause) {
        super(message, cause);
    }
}

