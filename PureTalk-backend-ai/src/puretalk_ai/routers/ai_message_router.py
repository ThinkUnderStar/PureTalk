from fastapi import APIRouter
from starlette.responses import StreamingResponse

from puretalk_ai.agents.admin_agent import admin_agent
from puretalk_ai.agents.root_agent import root_agent
from puretalk_ai.agents.user_agent import user_agent
from puretalk_ai.schemas.do_chat import DoChat
from puretalk_ai.core.contextvars import current_user_id, current_user_role, current_user_status, current_user_name

#创建路由
ai_message_api_router = APIRouter(prefix="/ai", tags=["AI"])

@ai_message_api_router.post("/chat/stream")
async def chat_stream(do_chat: DoChat):
    """
    根据用户不同的身份分配不同的agent进行交互

    :param do_chat: 用户信息,与对话记录
    :return: 返回流式输出连接
    """

    #存入该请求的用户信息
    current_user_id.set(do_chat.user_id)
    current_user_role.set(do_chat.role)
    current_user_status.set(do_chat.status)
    current_user_name.set(do_chat.name)

    #分配 agent 与用户进行交互
    if do_chat.role == 1:
        #普通用户
        agent = user_agent
    elif do_chat.role == 2:
        #管理员
        agent = admin_agent
    elif do_chat.role == 3:
        #root
        agent = root_agent
    else:
        raise Exception("用户角色错误")

    # 异步生成器：提取每个 chunk 的文本内容
    async def event_stream():
        async for chunk in agent.astream(
                {"messages": do_chat.context},
                stream_mode="messages"
        ):
            message = chunk[0]

            #判断是否有content
            if hasattr(message, "content") and message.content:
                #返回内容
                yield message.content

    #StreamingResponse内部的__call__方法会循环调用event_stream
    return StreamingResponse(event_stream(), media_type="text/event-stream")




