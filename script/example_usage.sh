#!/bin/bash
# BpArsenal Config Generator 使用示例

echo "=========================================="
echo "BpArsenal Config Generator 示例"
echo "=========================================="
echo ""

# 配置
TOOLS_DIR="${1:-/usr/share/tools}"
API_KEY="${OPENAI_API_KEY}"

echo "📁 工具目录: $TOOLS_DIR"
echo ""

# 示例1: 查看目录树
echo "🌲 示例1: 查看目录树"
echo "命令: python generator_config.py tree --dir $TOOLS_DIR"
echo ""
python generator_config.py tree --dir "$TOOLS_DIR"
echo ""
echo "----------------------------------------"
echo ""

# 示例2: 生成httpTool提示词
echo "📝 示例2: 生成httpTool提示词"
echo "命令: python generator_config.py prompt --dir $TOOLS_DIR --type httpTool --output prompt_http.txt"
echo ""
python generator_config.py prompt --dir "$TOOLS_DIR" --type httpTool --output prompt_http.txt
echo ""
echo "----------------------------------------"
echo ""

# 示例3: 生成thirtyPart提示词
echo "📝 示例3: 生成thirtyPart提示词"
echo "命令: python generator_config.py prompt --dir $TOOLS_DIR --type thirtyPart --output prompt_thirty.txt"
echo ""
python generator_config.py prompt --dir "$TOOLS_DIR" --type thirtyPart --output prompt_thirty.txt
echo ""
echo "----------------------------------------"
echo ""

# 示例4: 生成完整配置提示词
echo "📝 示例4: 生成完整配置提示词"
echo "命令: python generator_config.py prompt --dir $TOOLS_DIR --type all --output prompt_all.txt"
echo ""
python generator_config.py prompt --dir "$TOOLS_DIR" --type all --output prompt_all.txt
echo ""
echo "----------------------------------------"
echo ""

# 示例5: AI直接生成（需要API key）
if [ -n "$API_KEY" ]; then
    echo "🤖 示例5: AI直接生成配置"
    echo "命令: python generator_config.py generate --dir $TOOLS_DIR --api-key \$OPENAI_API_KEY --model gpt-4 --type all"
    echo ""
    python generator_config.py generate \
        --dir "$TOOLS_DIR" \
        --api-key "$API_KEY" \
        --model gpt-4 \
        --type all \
        --output generated_config.yaml
    echo ""
else
    echo "⚠️  示例5: AI直接生成（跳过 - 未设置OPENAI_API_KEY）"
    echo "提示: 设置环境变量 export OPENAI_API_KEY=sk-your-key 后可使用"
    echo ""
fi

echo "=========================================="
echo "✅ 示例执行完成！"
echo "=========================================="
echo ""
echo "📂 生成的文件:"
ls -lh prompt_*.txt generated_config.yaml 2>/dev/null | awk '{print "  - "$9" ("$5")"}'
echo ""
echo "💡 下一步:"
echo "  1. 查看生成的提示词文件"
echo "  2. 使用提示词发送给AI（ChatGPT/Claude等）"
echo "  3. 或直接使用generate命令自动生成"
echo "  4. 将生成的配置复制到 src/main/resources/config.yaml"
echo ""

