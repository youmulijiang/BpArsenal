# BpArsenal Config Generator 使用说明

智能配置生成工具，用于快速生成 BpArsenal 插件的 `config.yaml` 配置文件。

## 🚀 功能特性

- ✅ **目录树扫描**: 自动扫描工具目录并生成树形结构
- ✅ **智能提示词**: 生成专业的AI提示词，包含完整DSL语法说明
- ✅ **AI直接生成**: 调用OpenAI API自动生成配置文件
- ✅ **多类型支持**: 支持生成 httpTool、thirtyPart 或完整配置
- ✅ **命令行友好**: 完善的命令行参数解析

## 📦 安装依赖

```bash
# 基础功能（tree和prompt模式）
# 无需额外依赖，使用Python 3.6+即可

# AI生成模式（generate命令）
pip install openai
```

## 💡 使用方法

### 1️⃣ 查看工具目录结构

```bash
python generator_config.py tree --dir /path/to/tools

# 指定扫描深度
python generator_config.py tree --dir /path/to/tools --depth 2
```

**示例输出：**
```
📁 扫描工具目录: /usr/share/tools

/usr/share/tools/
├── sqlmap
│   ├── sqlmap.py
│   └── README.md
├── nmap
│   └── nmap.exe
└── metasploit
    └── msfconsole

📊 工具信息:
  - 根目录: /usr/share/tools
  - 发现工具数: 3
```

---

### 2️⃣ 生成AI提示词（手动模式）

适合没有API key或想要手动调整的场景。

```bash
# 生成httpTool配置提示词
python generator_config.py prompt --dir /path/to/tools --type httpTool

# 生成thirtyPart配置提示词
python generator_config.py prompt --dir /path/to/tools --type thirtyPart

# 生成完整配置提示词
python generator_config.py prompt --dir /path/to/tools --type all

# 保存提示词到文件
python generator_config.py prompt --dir /path/to/tools --type all --output prompt.txt
```

**使用流程：**
1. 运行命令生成提示词
2. 复制提示词内容
3. 发送给AI（ChatGPT、Claude等）
4. AI返回YAML配置
5. 保存到 `src/main/resources/config.yaml`

---

### 3️⃣ AI直接生成配置（自动模式）

需要OpenAI API key。

```bash
# 生成httpTool配置
python generator_config.py generate \
  --dir /path/to/tools \
  --api-key sk-your-api-key \
  --type httpTool \
  --output config_http.yaml

# 生成thirtyPart配置
python generator_config.py generate \
  --dir /path/to/tools \
  --api-key sk-your-api-key \
  --type thirtyPart \
  --output config_thirty.yaml

# 生成完整配置
python generator_config.py generate \
  --dir /path/to/tools \
  --api-key sk-your-api-key \
  --model gpt-4 \
  --type all \
  --output config.yaml
```

**参数说明：**
- `--dir, -d`: 工具目录路径（必需）
- `--api-key, -k`: OpenAI API密钥（必需）
- `--model, -m`: AI模型名称（默认gpt-4）
- `--type, -t`: 配置类型（httpTool/thirtyPart/all，默认all）
- `--output, -o`: 输出文件路径（默认generated_config.yaml）
- `--depth`: 目录扫描深度（默认3）

---

## 📋 配置类型说明

### httpTool - HTTP工具配置
生成用于HTTP请求转换的工具配置，支持DSL变量替换。

**适用工具：**
- sqlmap（SQL注入）
- ffuf、dirsearch（目录扫描）
- nuclei（漏洞扫描）
- nmap（端口扫描）
- curl、httpx（HTTP探测）

### thirtyPart - 第三方工具配置
生成可直接启动的第三方工具配置。

**适用工具：**
- Metasploit、Cobalt Strike（渗透框架）
- Hashcat、John（密码破解）
- Burp Suite、ZAP（代理工具）
- VSCode、Sublime（编辑器）

### all - 完整配置
同时生成 httpTool、thirtyPart 和 webSite 三部分配置。

---

## 🎯 实战示例

### 示例1：扫描Kali Linux工具目录

```bash
# 1. 查看目录结构
python generator_config.py tree --dir /usr/share/tools

# 2. 生成提示词
python generator_config.py prompt \
  --dir /usr/share/tools \
  --type all \
  --output kali_prompt.txt

# 3. 或直接生成配置
python generator_config.py generate \
  --dir /usr/share/tools \
  --api-key $OPENAI_API_KEY \
  --model gpt-4 \
  --type all \
  --output kali_config.yaml
```

### 示例2：Windows环境扫描

```bash
# Windows工具目录
python generator_config.py generate ^
  --dir "D:\Security\Tools" ^
  --api-key sk-xxx ^
  --model gpt-4 ^
  --type all ^
  --output windows_config.yaml
```

### 示例3：只生成HTTP工具配置

```bash
python generator_config.py generate \
  --dir ~/security-tools \
  --api-key sk-xxx \
  --type httpTool \
  --output http_only.yaml
```

---

## 📝 提示词模板

脚本内置三种专业提示词模板：

### 1. httpTool提示词
包含：
- 完整DSL变量列表（18个基础变量）
- DSL函数说明（12个函数）
- 配置规则和示例
- 生成要求（分类、命令模板、favor标记等）

### 2. thirtyPart提示词
包含：
- 配置格式说明
- 工具分类建议
- 启动命令规范
- autoStart使用建议

### 3. all提示词
包含：
- 完整配置结构
- httpTool + thirtyPart + webSite
- 综合配置要求

---

## 🔧 高级用法

### 环境变量配置

```bash
# 设置API Key环境变量
export OPENAI_API_KEY=sk-your-api-key

# 使用环境变量
python generator_config.py generate \
  --dir /path/to/tools \
  --api-key $OPENAI_API_KEY \
  --type all
```

### 批量生成

```bash
#!/bin/bash
# 批量生成不同类型配置

TOOLS_DIR="/usr/share/tools"
API_KEY="sk-xxx"

# 生成HTTP工具配置
python generator_config.py generate \
  --dir "$TOOLS_DIR" \
  --api-key "$API_KEY" \
  --type httpTool \
  --output config_http.yaml

# 生成第三方工具配置
python generator_config.py generate \
  --dir "$TOOLS_DIR" \
  --api-key "$API_KEY" \
  --type thirtyPart \
  --output config_thirty.yaml

echo "✅ 配置生成完成！"
```

### 目录深度控制

```bash
# 浅层扫描（深度1）- 适合工具很多的目录
python generator_config.py prompt --dir /usr/bin --depth 1 --type all

# 深层扫描（深度5）- 适合结构复杂的目录
python generator_config.py prompt --dir ~/tools --depth 5 --type all
```

---

## ⚠️ 注意事项

1. **API Key安全**：
   - 不要在命令行中直接输入API key
   - 使用环境变量或配置文件
   - 不要提交包含API key的脚本到版本控制

2. **目录权限**：
   - 确保有读取目标目录的权限
   - Linux/Mac可能需要sudo权限扫描系统目录

3. **生成配置审查**：
   - AI生成的配置需要人工审查
   - 检查命令路径是否正确
   - 验证DSL变量使用是否合理

4. **API调用成本**：
   - GPT-4模型调用有费用
   - 可以先用prompt模式生成提示词
   - 或使用更便宜的gpt-3.5-turbo模型

---

## 🐛 故障排除

### 问题1：找不到openai模块

```bash
# 安装openai库
pip install openai

# 或使用国内镜像
pip install openai -i https://pypi.tuna.tsinghua.edu.cn/simple
```

### 问题2：API调用失败

```bash
# 检查API key是否正确
echo $OPENAI_API_KEY

# 测试API连接
python -c "import openai; openai.api_key='sk-xxx'; print(openai.Model.list())"
```

### 问题3：目录扫描权限错误

```bash
# Linux/Mac使用sudo
sudo python generator_config.py tree --dir /usr/share/tools

# Windows以管理员身份运行PowerShell
```

---

## 📚 相关文档

- [config.yaml配置文档](../doc/DSL_SYNTAX.md)
- [BpArsenal主README](../README.md)
- [DSL语法完整文档](../doc/DSL_SYNTAX.md)

---

## 🤝 贡献

欢迎提交Issue和PR改进此工具！

---

**作者**: youmulijiang  
**版本**: v1.0.0  
**许可**: MIT

