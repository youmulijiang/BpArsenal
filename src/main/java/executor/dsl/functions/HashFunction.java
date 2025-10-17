package executor.dsl.functions;

import executor.dsl.DslException;
import executor.dsl.FunctionHandler;
import executor.dsl.HttpContext;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * 哈希函数
 * 用法: hash(data, algorithm)
 * 示例: hash(http.request.body, "sha256")
 * 
 * 支持的算法: MD5, SHA-1, SHA-256, SHA-512
 */
public class HashFunction implements FunctionHandler {
    
    @Override
    public Object execute(List<Object> args, HttpContext context) throws DslException {
        if (args.size() != 2) {
            throw new DslException("hash() requires 2 arguments: hash(data, algorithm)");
        }
        
        String data = args.get(0) != null ? args.get(0).toString() : "";
        String algorithm = args.get(1).toString();
        
        try {
            // 标准化算法名称
            String normalizedAlgorithm = normalizeAlgorithm(algorithm);
            
            MessageDigest digest = MessageDigest.getInstance(normalizedAlgorithm);
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new DslException("Hash calculation failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * 标准化算法名称
     */
    private String normalizeAlgorithm(String algorithm) {
        switch (algorithm.toLowerCase().replace("-", "")) {
            case "md5":
                return "MD5";
            case "sha1":
            case "sha":
                return "SHA-1";
            case "sha256":
            case "sha2":
                return "SHA-256";
            case "sha512":
                return "SHA-512";
            default:
                return algorithm;
        }
    }
    
    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    @Override
    public String getName() {
        return "hash";
    }
    
    @Override
    public String getDescription() {
        return "Calculate hash digest of data";
    }
    
    @Override
    public String getUsage() {
        return "hash(data, algorithm) - Supported algorithms: md5, sha1, sha256, sha512";
    }
}

