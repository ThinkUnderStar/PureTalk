import asyncio
from typing import Type

import httpx
from langchain_core.tools import BaseTool
from pydantic import Field,BaseModel

from puretalk_ai.config.settings import JAVA_BACKEND_URL, SERVICE_API_KEY
from puretalk_ai.core.contextvars import current_user_id

class GetNotificationsModel(BaseModel):
    page: int = Field(1,description="页码")
    size: int = Field(10,description="每页数量")

class GetNotificationsTool(BaseTool):
    name : str = "get_notifications_tool"
    description : str = "获取该用户的所有通知"
    args_schema : Type[BaseModel] = GetNotificationsModel

    def _run(self, page: int, size: int) -> str:
        #设置请求头
        headers = {
            "X-Service-Token": SERVICE_API_KEY,
            "X-User-Id": str(current_user_id.get())
        }

        # 发送HTTP请求，并解析响应
        response = httpx.get(
            f"{JAVA_BACKEND_URL}/api/notification",
            params={
                "page": page,
                "size": size,
            },
            headers=headers,
        )

        if response.status_code != 200:
            return f"获取用户通知列表异常，HTTP状态码：{response.status_code}"

        result = response.json()

        if result.get("code") != 200:
            return f"获取用户通知列表失败：{result.get('message', '未知错误')}"

        data = result.get("data", {})
        if data is None:
            return "获取结果为空"

        # 格式化结果
        total = data.get("total", 0)
        current = data.get("current", page)
        pages = data.get("pages", 1)

        header = f"共{total}条结果，第{current}/{pages}页，显示{len(data['records'])}条。\n\n"

        line = ""
        # 通知类型映射
        type_map = {1: "举报结果", 2: "反馈回复", 3: "系统通知"}

        for notification in data["records"]:
            n_type = notification.get("type", 0)
            type_name = type_map.get(n_type, "未知类型")
            is_read = "已读" if notification.get("isRead") == 1 else "未读"
            create_time = notification.get("createTime", "未知时间")

            line += (f"【{notification['id']}】{type_name}（{is_read}）\n"
                     f"时间：{create_time}\n")

        return header + line

    async def _arun(self, page: int, size: int) -> str:
        return await asyncio.to_thread(self._run, page, size)