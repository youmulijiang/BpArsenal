# BpArsenal 国际化使用指南

## 概述

BpArsenal 插件已集成完整的国际化（i18n）支持，目前支持中文（简体）和英文两种语言。用户可以在设置面板中自由切换界面语言。

## 文件结构

### 资源文件
```
src/main/resources/
├── messages_zh_CN.properties  # 中文资源文件
├── messages_en_US.properties  # 英文资源文件
└── config.json                # 配置文件
```

### 核心类
```
src/main/java/util/
└── I18nManager.java           # 国际化管理器
```

## 使用方法

### 1. 基本用法

在任何UI组件中使用国际化文本：

```java
import util.I18nManager;

// 获取简单文本
String title = I18nManager.getInstance().getText("main.title");

// 获取带参数的文本
String message = I18nManager.getInstance().getText("settings.config.status", "已加载");
```

### 2. 在UI组件中使用

#### 按钮文本
```java
JButton button = new JButton(I18nManager.getInstance().getText("button.apply"));
```

#### 标签文本
```java
JLabel label = new JLabel(I18nManager.getInstance().getText("settings.directory.label"));
```

#### 面板标题
```java
panel.setBorder(BorderFactory.createTitledBorder(
    BorderFactory.createEtchedBorder(),
    I18nManager.getInstance().getText("settings.config.title"),
    TitledBorder.LEFT,
    TitledBorder.TOP,
    new Font("微软雅黑", Font.BOLD, 12)
));
```

### 3. 对话框和消息

```java
JOptionPane.showMessageDialog(
    this,
    I18nManager.getInstance().getText("settings.language.restart.required"),
    I18nManager.getInstance().getText("dialog.title.success"),
    JOptionPane.INFORMATION_MESSAGE
);
```

## 支持的语言

### 中文（zh-CN）
- 文件：`messages_zh_CN.properties`
- 编码：UTF-8
- 语言标识：`I18nManager.CHINESE`

### 英文（en-US）
- 文件：`messages_en_US.properties`
- 编码：UTF-8
- 语言标识：`I18nManager.ENGLISH`

## 资源键值规范

### 命名规则
- 使用点号分隔的层级结构
- 全小写，单词间用点号分隔
- 按功能模块分组

### 示例
```properties
# 主要模块
main.title=BpArsenal 武器库
main.tab.tools=HTTP工具

# 按钮操作
button.apply=应用
button.cancel=取消

# 设置面板
settings.config.title=配置文件管理
settings.directory.label=工具根目录:

# 对话框
dialog.title.confirm=确认
dialog.title.error=错误
```

## 添加新的国际化文本

### 1. 在资源文件中添加键值对

**中文资源文件** (`messages_zh_CN.properties`)：
```properties
new.feature.title=新功能标题
new.feature.description=这是新功能的描述
```

**英文资源文件** (`messages_en_US.properties`)：
```properties
new.feature.title=New Feature Title
new.feature.description=This is the description of the new feature
```

### 2. 在代码中使用

```java
String title = I18nManager.getInstance().getText("new.feature.title");
String desc = I18nManager.getInstance().getText("new.feature.description");
```

## 语言切换功能

### 用户界面
1. 打开 BpArsenal 插件
2. 切换到"设置"选项卡
3. 找到"语言设置"面板
4. 在下拉框中选择语言
5. 点击"应用语言设置"按钮
6. 重启插件使设置生效

### 程序化切换
```java
import util.I18nManager;
import java.util.Locale;

// 切换到中文
I18nManager.getInstance().setLocale(I18nManager.CHINESE);

// 切换到英文
I18nManager.getInstance().setLocale(I18nManager.ENGLISH);
```

## 最佳实践

### 1. 文本提取
- 所有用户可见的文本都应该使用国际化
- 避免在代码中硬编码文本字符串
- 日志信息可以考虑保留英文

### 2. 键值命名
- 使用有意义的键名
- 按模块分组组织
- 保持一致的命名规范

### 3. 文本内容
- 保持简洁明了
- 避免过长的文本
- 考虑不同语言的文本长度差异

### 4. 界面适配
- 预留足够的空间适应不同长度的文本
- 测试所有支持的语言
- 确保UI布局在不同语言下都正常

## 错误处理

### 缺失键值
如果请求的键值不存在，`I18nManager` 会：
1. 记录错误日志
2. 返回键名作为显示文本
3. 继续程序执行

### 资源文件问题
- 如果无法加载指定语言的资源包，会自动降级到中文
- 如果中文资源包也无法加载，会抛出运行时异常

## 扩展支持

### 添加新语言

1. **创建资源文件**
   ```
   messages_ja_JP.properties  # 日语
   messages_ko_KR.properties  # 韩语
   ```

2. **更新 I18nManager**
   ```java
   public static final Locale JAPANESE = new Locale("ja", "JP");
   public static final Locale KOREAN = new Locale("ko", "KR");
   
   public boolean isSupportedLocale(Locale locale) {
       // 添加新语言检查
   }
   
   public Locale[] getSupportedLocales() {
       return new Locale[]{CHINESE, ENGLISH, JAPANESE, KOREAN};
   }
   ```

3. **更新UI组件**
   在语言选择下拉框中添加新语言选项。

## 性能考虑

### 资源包缓存
- `I18nManager` 使用 `ConcurrentHashMap` 缓存资源包
- 避免重复加载相同语言的资源
- 支持运行时清理缓存

### 延迟加载
- 资源包只在首次使用时加载
- 支持动态语言切换
- 最小化内存占用

## 调试和维护

### 日志输出
`I18nManager` 会记录以下信息：
- 语言环境初始化
- 语言切换操作
- 资源包加载状态
- 错误信息

### 验证工具
可以使用以下方法验证国际化实现：
```java
// 检查键是否存在
boolean exists = I18nManager.getInstance().hasKey("some.key");

// 获取当前语言
Locale current = I18nManager.getInstance().getCurrentLocale();

// 获取支持的语言列表
Locale[] supported = I18nManager.getInstance().getSupportedLocales();
```

## 总结

BpArsenal 的国际化系统提供了：
- ✅ 完整的中英文支持
- ✅ 用户友好的语言切换界面
- ✅ 灵活的资源管理机制
- ✅ 优雅的错误处理
- ✅ 高性能的缓存机制
- ✅ 易于扩展的架构设计

通过遵循本指南，开发人员可以轻松地维护和扩展 BpArsenal 的国际化功能。 