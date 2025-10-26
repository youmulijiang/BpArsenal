@echo off
REM BpArsenal Config Generator 使用示例 (Windows)
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ==========================================
echo BpArsenal Config Generator 示例 (Windows)
echo ==========================================
echo.

REM 配置
if "%1"=="" (
    set TOOLS_DIR=D:\Security\Tools
) else (
    set TOOLS_DIR=%1
)

echo 📁 工具目录: %TOOLS_DIR%
echo.

REM 示例1: 查看目录树
echo 🌲 示例1: 查看目录树
echo 命令: python generator_config.py tree --dir %TOOLS_DIR%
echo.
python generator_config.py tree --dir "%TOOLS_DIR%"
echo.
echo ----------------------------------------
echo.

REM 示例2: 生成httpTool提示词
echo 📝 示例2: 生成httpTool提示词
echo 命令: python generator_config.py prompt --dir %TOOLS_DIR% --type httpTool --output prompt_http.txt
echo.
python generator_config.py prompt --dir "%TOOLS_DIR%" --type httpTool --output prompt_http.txt
echo.
echo ----------------------------------------
echo.

REM 示例3: 生成thirtyPart提示词
echo 📝 示例3: 生成thirtyPart提示词
echo 命令: python generator_config.py prompt --dir %TOOLS_DIR% --type thirtyPart --output prompt_thirty.txt
echo.
python generator_config.py prompt --dir "%TOOLS_DIR%" --type thirtyPart --output prompt_thirty.txt
echo.
echo ----------------------------------------
echo.

REM 示例4: 生成完整配置提示词
echo 📝 示例4: 生成完整配置提示词
echo 命令: python generator_config.py prompt --dir %TOOLS_DIR% --type all --output prompt_all.txt
echo.
python generator_config.py prompt --dir "%TOOLS_DIR%" --type all --output prompt_all.txt
echo.
echo ----------------------------------------
echo.

REM 示例5: AI直接生成（需要API key）
if defined OPENAI_API_KEY (
    echo 🤖 示例5: AI直接生成配置
    echo 命令: python generator_config.py generate --dir %TOOLS_DIR% --api-key %%OPENAI_API_KEY%% --model gpt-4 --type all
    echo.
    python generator_config.py generate ^
        --dir "%TOOLS_DIR%" ^
        --api-key "%OPENAI_API_KEY%" ^
        --model gpt-4 ^
        --type all ^
        --output generated_config.yaml
    echo.
) else (
    echo ⚠️  示例5: AI直接生成（跳过 - 未设置OPENAI_API_KEY）
    echo 提示: 运行 set OPENAI_API_KEY=sk-your-key 后可使用
    echo.
)

echo ==========================================
echo ✅ 示例执行完成！
echo ==========================================
echo.
echo 📂 生成的文件:
dir /b prompt_*.txt generated_config.yaml 2>nul
echo.
echo 💡 下一步:
echo   1. 查看生成的提示词文件
echo   2. 使用提示词发送给AI（ChatGPT/Claude等）
echo   3. 或直接使用generate命令自动生成
echo   4. 将生成的配置复制到 src\main\resources\config.yaml
echo.

pause

