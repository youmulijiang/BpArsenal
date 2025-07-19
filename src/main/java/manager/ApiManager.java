package manager;

import burp.api.montoya.MontoyaApi;

/**
 * API管理器，负责MontoyaApi实例的全局管理
 * 采用单例模式确保全局唯一实例
 */
public class ApiManager {
    private static ApiManager instance;
    private MontoyaApi api;
    
    /**
     * 私有构造函数，防止外部实例化
     */
    private ApiManager() {}
    
    /**
     * 获取ApiManager单例实例
     * @return ApiManager实例
     */
    public static ApiManager getInstance() {
        if (instance == null) {
            instance = new ApiManager();
        }
        return instance;
    }
    
    /**
     * 设置MontoyaApi实例
     * @param api Montoya API实例
     */
    public void setApi(MontoyaApi api) {
        this.api = api;
    }
    
    /**
     * 获取MontoyaApi实例
     * @return MontoyaApi实例
     * @throws IllegalStateException 如果API未初始化
     */
    public MontoyaApi getApi() {
        if (api == null) {
            throw new IllegalStateException("MontoyaApi未初始化，请先调用setApi()方法");
        }
        return this.api;
    }
    
    /**
     * 检查API是否已初始化
     * @return 如果API已初始化返回true，否则返回false
     */
    public boolean isInitialized() {
        return api != null;
    }
} 