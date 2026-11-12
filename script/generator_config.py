#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
BpArsenal Config Generator
用于生成config.yaml配置文件的智能工具

功能：
1. 扫描工具目录并生成目录树
2. 生成AI提示词（prompt模式）
3. 直接调用AI生成配置（generate模式）
"""

import os
import sys
import argparse
import json
from pathlib import Path
from typing import List, Dict, Optional


class DirectoryTree:
    """目录树生成器"""
    
    def __init__(self, root_path: str, max_depth: int = 3):
        self.root_path = Path(root_path)
        self.max_depth = max_depth
        self.tree_lines = []
    
    def generate_tree(self) -> str:
        """生成目录树字符串"""
        if not self.root_path.exists():
            return f"错误: 目录不存在 - {self.root_path}"
        
        self.tree_lines = [f"{self.root_path}/"]
        self._walk_directory(self.root_path, "", 0)
        return "\n".join(self.tree_lines)
    
    def _walk_directory(self, path: Path, prefix: str, depth: int):
        """递归遍历目录"""
        if depth >= self.max_depth:
            return
        
        try:
            entries = sorted(path.iterdir(), key=lambda x: (not x.is_dir(), x.name.lower()))
        except PermissionError:
            return
        
        # 过滤隐藏文件和常见忽略目录
        ignore_patterns = {'.git', '__pycache__', 'node_modules', '.venv', 'venv'}
        entries = [e for e in entries if e.name not in ignore_patterns and not e.name.startswith('.')]
        
        for i, entry in enumerate(entries):
            is_last = i == len(entries) - 1
            connector = "└── " if is_last else "├── "
            self.tree_lines.append(f"{prefix}{connector}{entry.name}")
            
            if entry.is_dir():
                extension = "    " if is_last else "│   "
                self._walk_directory(entry, prefix + extension, depth + 1)
    
    def get_tool_info(self) -> Dict:
        """获取工具目录信息"""
        tools = []

        if not self.root_path.exists():
            return {"tools": [], "total": 0, "root": str(self.root_path)}
        
        for item in self.root_path.iterdir():
            if item.is_dir():
                tools.append({
                    "name": item.name,
                    "path": str(item),
                    "type": "directory"
                })
            elif item.is_file() and item.suffix in ['.exe', '.sh', '.py', '.jar']:
                tools.append({
                    "name": item.stem,
                    "path": str(item),
                    "type": "executable"
                })
        
        return {
            "tools": tools,
            "total": len(tools),
            "root": str(self.root_path)
        }


class PromptGenerator:
    """提示词生成器"""
    
    HTTPTOOLS_PROMPT = """# 任务：生成BpArsenal HTTP工具配置

## 背景
你是一个安全测试工具配置专家。我需要你为BpArsenal插件生成httpTool配置。

## 工具目录结构
```
{tree}
```

## 配置规则

### YAML格式
```yaml
httpTool:
- type: 工具分类名称
  content:
  - toolName: 工具名称
    commandList:
    - command: 命令模板（支持DSL变量）
      favor: true/false
      note: 命令说明
      workDir: ''
```

### 支持的DSL变量
**基础变量：**
- %http.request.url% - 完整URL
- %http.request.host% - 主机名
- %http.request.path% - 路径
- %http.request.method% - HTTP方法
- %http.request.body.raw% - 原始请求体
- %http.request.headers.cookie% - Cookie头
- %http.request.headers.authorization% - Authorization头

**批量处理：**
- %httpList.requests.*.request.url% - 所有请求URL
- %httpList.requests.*.request.host% - 所有主机

**DSL函数：**
- %tmpFile(content, extension)% - 创建临时文件
- %file(content, filepath)% - 创建持久文件
- %unique(list)% - 去重
- %join(list, delimiter)% - 连接
- %hash(data, algorithm)% - 哈希计算
- %json(jsonString, path)% - JSON提取
- %base64(data, mode)% - Base64编解码

## 要求
1. 根据上述工具目录，识别常见的安全测试工具
2. 为每个工具生成至少2-3个常用命令模板
3. 合理使用DSL变量和函数
4. 按照功能分类（SQL注入、目录扫描、漏洞扫描、XSS测试等）
5. favor标记最常用的命令为true
6. 提供详细的note说明

## 输出格式
直接输出YAML格式配置，不要有其他解释文字。
"""

    THIRDPARTY_PROMPT = """# 任务：生成BpArsenal 第三方工具配置

## 背景
你是一个安全测试工具配置专家。我需要你为BpArsenal插件生成thirtyPart配置。

## 工具目录结构
```
{tree}
```

## 配置规则

### YAML格式
```yaml
thirtyPart:
- type: 工具分类
  content:
  - toolName: 工具显示名称
    startCommand: 启动命令
    favor: true/false
    note: 工具描述
    workDir: ''
    autoStart: false
```

## 要求
1. 根据上述工具目录，识别可启动的工具
2. 按功能分类（渗透框架、密码破解、代理工具、编辑器等）
3. startCommand使用完整路径或系统命令
4. favor标记常用工具为true
5. autoStart一般设置为false（除非特殊工具）
6. 提供清晰的note说明工具用途

## 输出格式
直接输出YAML格式配置，不要有其他解释文字。
"""

    ALL_PROMPT = """# 任务：生成BpArsenal完整配置

## 背景
你是一个安全测试工具配置专家。我需要你为BpArsenal插件生成完整的config.yaml配置。

## 工具目录结构
```
{tree}
```

## 配置规则

### 完整YAML格式
```yaml
httpTool:
- type: 工具分类名称
  content:
  - toolName: 工具名称
    commandList:
    - command: 命令模板（支持DSL变量）
      favor: true/false
      note: 命令说明
      workDir: ''

thirtyPart:
- type: 工具分类
  content:
  - toolName: 工具显示名称
    startCommand: 启动命令
    favor: true/false
    note: 工具描述
    workDir: ''
    autoStart: false

webSite:
- type: 网站分类
  content:
  - url: 网站URL
    desc: 网站描述
    favor: true/false
```

### HTTP工具DSL变量（部分）
- %http.request.url%, %http.request.host%, %http.request.method%
- %http.request.body.raw%, %http.request.headers.cookie%
- %tmpFile(content, ext)%, %file(content, path)%
- %unique(list)%, %join(list, delimiter)%
- %hash(data, alg)%, %json(str, path)%, %base64(data, mode)%

## 要求
1. 生成httpTool、thirtyPart和webSite三个完整部分
2. httpTool：根据工具目录生成HTTP工具配置
3. thirtyPart：生成可启动的第三方工具配置
4. webSite：生成常用安全网站收藏（OSINT、漏洞库、在线工具等）
5. 每个部分都要有合理的分类和详细说明
6. 优先级：favor合理标记

## 输出格式
直接输出完整YAML格式配置，不要有其他解释文字。
"""
    
    @staticmethod
    def generate_prompt(tree: str, config_type: str = "all") -> str:
        """生成提示词"""
        prompts = {
            "httpTool": PromptGenerator.HTTPTOOLS_PROMPT,
            "thirtyPart": PromptGenerator.THIRDPARTY_PROMPT,
            "all": PromptGenerator.ALL_PROMPT
        }
        
        template = prompts.get(config_type, PromptGenerator.ALL_PROMPT)
        return template.format(tree=tree)


class AIConfigGenerator:
    """AI配置生成器（调用API）"""
    
    def __init__(self, api_key: str, model: str = "gpt-4"):
        self.api_key = api_key
        self.model = model
    
    def generate_config(self, prompt: str) -> str:
        """调用AI生成配置（兼容 openai >= 1.0 与旧版本SDK）"""
        try:
            import openai
        except ImportError:
            print("错误: 需要安装openai库。运行: pip install openai", file=sys.stderr)
            sys.exit(1)

        messages = [
            {"role": "system", "content": "你是一个安全测试工具配置专家，精通各种渗透测试工具的使用。"},
            {"role": "user", "content": prompt}
        ]

        print(f"🤖 正在调用 {self.model} 生成配置...")

        try:
            # openai >= 1.0：使用Client实例
            client = openai.OpenAI(api_key=self.api_key)
            response = client.chat.completions.create(
                model=self.model,
                messages=messages,
                temperature=0.7,
                max_tokens=4000
            )
            config = response.choices[0].message.content.strip()
        except AttributeError:
            # openai < 1.0：旧版全局API
            openai.api_key = self.api_key
            response = openai.ChatCompletion.create(
                model=self.model,
                messages=messages,
                temperature=0.7,
                max_tokens=4000
            )
            config = response.choices[0].message.content.strip()

        print("✅ 配置生成成功！")
        return config


def main():
    """主函数"""
    parser = argparse.ArgumentParser(
        description="BpArsenal Config Generator - 智能生成配置文件",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
示例用法:
  # 生成提示词（手动发送给AI）
  python generator_config.py prompt --dir /path/to/tools --type httpTool
  
  # 直接调用AI生成配置
  python generator_config.py generate --dir /path/to/tools --api-key sk-xxx --model gpt-4 --type all
  
  # 查看工具目录结构
  python generator_config.py tree --dir /path/to/tools
        """
    )
    
    subparsers = parser.add_subparsers(dest='command', help='子命令')
    
    # 子命令1: tree - 仅显示目录树
    tree_parser = subparsers.add_parser('tree', help='显示工具目录树结构')
    tree_parser.add_argument(
        '--dir', '-d',
        type=str,
        required=True,
        help='工具目录路径'
    )
    tree_parser.add_argument(
        '--depth',
        type=int,
        default=3,
        help='目录遍历深度（默认3）'
    )
    
    # 子命令2: prompt - 生成提示词
    prompt_parser = subparsers.add_parser('prompt', help='生成AI提示词')
    prompt_parser.add_argument(
        '--dir', '-d',
        type=str,
        required=True,
        help='工具目录路径'
    )
    prompt_parser.add_argument(
        '--type', '-t',
        choices=['httpTool', 'thirtyPart', 'all'],
        default='all',
        help='配置类型（默认all）'
    )
    prompt_parser.add_argument(
        '--output', '-o',
        type=str,
        help='输出提示词到文件'
    )
    prompt_parser.add_argument(
        '--depth',
        type=int,
        default=3,
        help='目录遍历深度（默认3）'
    )
    
    # 子命令3: generate - 直接生成配置
    generate_parser = subparsers.add_parser('generate', help='调用AI生成配置')
    generate_parser.add_argument(
        '--dir', '-d',
        type=str,
        required=True,
        help='工具目录路径'
    )
    generate_parser.add_argument(
        '--api-key', '-k',
        type=str,
        required=True,
        help='OpenAI API密钥'
    )
    generate_parser.add_argument(
        '--model', '-m',
        type=str,
        default='gpt-4',
        help='AI模型名称（默认gpt-4）'
    )
    generate_parser.add_argument(
        '--type', '-t',
        choices=['httpTool', 'thirtyPart', 'all'],
        default='all',
        help='配置类型（默认all）'
    )
    generate_parser.add_argument(
        '--output', '-o',
        type=str,
        default='generated_config.yaml',
        help='输出配置文件路径（默认generated_config.yaml）'
    )
    generate_parser.add_argument(
        '--depth',
        type=int,
        default=3,
        help='目录遍历深度（默认3）'
    )
    
    args = parser.parse_args()
    
    if not args.command:
        parser.print_help()
        sys.exit(1)
    
    # 执行对应命令
    if args.command == 'tree':
        handle_tree_command(args)
    elif args.command == 'prompt':
        handle_prompt_command(args)
    elif args.command == 'generate':
        handle_generate_command(args)


def handle_tree_command(args):
    """处理tree命令"""
    print(f"📁 扫描工具目录: {args.dir}\n")
    
    tree_gen = DirectoryTree(args.dir, args.depth)
    tree = tree_gen.generate_tree()
    
    print(tree)
    print(f"\n📊 工具信息:")
    
    tool_info = tree_gen.get_tool_info()
    print(f"  - 根目录: {tool_info['root']}")
    print(f"  - 发现工具数: {tool_info['total']}")
    
    if tool_info['tools']:
        print("\n  发现的工具:")
        for tool in tool_info['tools'][:10]:  # 只显示前10个
            print(f"    • {tool['name']} ({tool['type']})")
        if len(tool_info['tools']) > 10:
            print(f"    ... 还有 {len(tool_info['tools']) - 10} 个工具")


def handle_prompt_command(args):
    """处理prompt命令"""
    print(f"📝 生成 {args.type} 配置提示词\n")
    
    # 生成目录树
    tree_gen = DirectoryTree(args.dir, args.depth)
    tree = tree_gen.generate_tree()
    
    # 生成提示词
    prompt = PromptGenerator.generate_prompt(tree, args.type)
    
    # 输出
    if args.output:
        with open(args.output, 'w', encoding='utf-8') as f:
            f.write(prompt)
        print(f"✅ 提示词已保存到: {args.output}")
        print(f"\n💡 使用方法:")
        print(f"  1. 复制提示词内容发送给AI（如ChatGPT、Claude等）")
        print(f"  2. AI将返回YAML格式的配置")
        print(f"  3. 将配置保存到 src/main/resources/config.yaml")
    else:
        print("=" * 80)
        print(prompt)
        print("=" * 80)
        print(f"\n💡 提示: 使用 --output prompt.txt 保存到文件")


def handle_generate_command(args):
    """处理generate命令"""
    print(f"🚀 AI配置生成模式")
    print(f"  - 目录: {args.dir}")
    print(f"  - 模型: {args.model}")
    print(f"  - 类型: {args.type}")
    print(f"  - 输出: {args.output}\n")
    
    # 生成目录树
    tree_gen = DirectoryTree(args.dir, args.depth)
    tree = tree_gen.generate_tree()
    
    print(f"📁 目录树:\n{tree}\n")
    
    # 生成提示词
    prompt = PromptGenerator.generate_prompt(tree, args.type)
    
    # 调用AI
    ai_gen = AIConfigGenerator(args.api_key, args.model)
    config = ai_gen.generate_config(prompt)
    
    # 保存配置
    with open(args.output, 'w', encoding='utf-8') as f:
        f.write(config)
    
    print(f"\n✅ 配置已保存到: {args.output}")
    print(f"\n💡 下一步:")
    print(f"  1. 检查生成的配置文件")
    print(f"  2. 根据需要手动调整")
    print(f"  3. 复制到 src/main/resources/config.yaml")


if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        print("\n\n⚠️  操作已取消")
        sys.exit(0)
    except Exception as e:
        print(f"\n❌ 错误: {str(e)}", file=sys.stderr)
        sys.exit(1)
