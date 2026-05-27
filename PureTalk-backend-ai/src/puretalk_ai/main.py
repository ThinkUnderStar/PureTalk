from fastapi import FastAPI

from puretalk_ai.routers.ai_message_router import ai_message_api_router

# 创建FastAPI实例
app = FastAPI(
    title="PureTalk AI 服务",
    description="为 PureTalk 提供大模型对话能力",
    version="1.0.0"
)

# 注册路由
app.include_router(ai_message_api_router)

