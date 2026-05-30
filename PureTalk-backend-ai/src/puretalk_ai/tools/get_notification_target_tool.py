import asyncio
from typing import Type

import httpx
from langchain_core.tools import BaseTool
from pydantic import Field,BaseModel

from puretalk_ai.config.settings import JAVA_BACKEND_URL, SERVICE_API_KEY
from puretalk_ai.core.contextvars import current_user_id

class GetNotificationTargetModel(BaseModel):
    notification_id: int = Field(...,description="通知ID")
    type: int = Field(3,description="通知类型：1=举报结果，2=反馈回复，3=系统通知")

class GetNotificationTargetTool(BaseTool):
    name : str = "get_notification_target_tool"
    description : str = "获得通知的具体信息"
    args_schema : Type[BaseModel] = GetNotificationTargetModel

    def _run(self, notification_id: int, type: int) -> str:
        #设置请求头
        headers = {
            "X-Service-Token": SERVICE_API_KEY,
            "X-User-Id": str(current_user_id.get())
        }

        # 发送HTTP请求，并解析响应
        response = httpx.get(
            f"{JAVA_BACKEND_URL}/api/notification/{notification_id}/target",
            headers=headers,
        )

        if response.status_code != 200:
            return f"获取通知具体信息接口异常，HTTP状态码：{response.status_code}"

        result = response.json()

        if result.get("code") != 200:
            return f"查询通知具体信息失败：{result.get('message', '未知错误')}"

        data = result.get("data", {})
        if data is None:
            return "通知详情数据为空，可能已被删除。"

        # 格式化结果
        if type == 1:
            # 举报通知
            report_type_map = {1: "帖子举报", 2: "评论举报"}
            report_type_name = report_type_map.get(data.get("reportType", 0), "未知类型")
            return (
                f"通知类型：举报结果（{report_type_name}）\n"
                f"举报原因：{data.get('reason', '未知')}\n"
                f"处理结果：{data.get('handleResult', '未处理')}\n"
                f"处理状态：{data.get('status', 0)}"
            )

        elif type == 2:
            # 反馈通知
            return (
                f"通知类型：反馈回复\n"
                f"反馈标题：{data.get('title', '无标题')}\n"
                f"反馈内容：{data.get('content', '无内容')}\n"
                f"管理员回复：{data.get('reply', '暂无回复')}\n"
                f"处理状态：{data.get('status', 0)}"
            )

        elif type == 3:
            # 系统通知（预留）
            return f"通知类型：系统通知\n内容：{data}"

        else:
            return f"未知的通知类型：{type}"

    async def _arun(self, notification_id: int, type: int) -> str:
        return await asyncio.to_thread(self._run, notification_id, type)