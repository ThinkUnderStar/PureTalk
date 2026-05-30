import asyncio
from typing import Type

import httpx
from langchain_core.tools import BaseTool
from pydantic import Field,BaseModel

from puretalk_ai.config.settings import JAVA_BACKEND_URL, SERVICE_API_KEY
from puretalk_ai.core.contextvars import current_user_id

class GetFeedbacksModel(BaseModel):
    page: int = Field(1,description="页码")
    size: int = Field(10,description="每页数量")

class GetFeedbacksTool(BaseTool):
    name : str = "get_feedbacks_tool"
    description : str = "按照时间升序的顺序获取未处理的反馈信息列表"
    args_schema : Type[BaseModel] = GetFeedbacksModel

    def _run(self, page: int, size: int) -> str:
        #设置请求头
        headers = {
            "X-Service-Token": SERVICE_API_KEY,
            "X-User-Id": str(current_user_id.get())
        }

        # 发送HTTP请求，并解析响应
        response = httpx.get(
            f"{JAVA_BACKEND_URL}/api/feedback",
            params={
                "page": page,
                "size": size
            },
            headers=headers,
        )

        if response.status_code != 200:
            return f"获取反馈列表异常，HTTP状态码：{response.status_code}"

        result = response.json()

        if result.get("code") != 200:
            return f"获取反馈列表失败：{result.get('message', '未知错误')}"

        data = result.get("data", {})
        if data is None:
            return "获取结果为空"

        # 格式化结果
        total = data.get("total", 0)
        current = data.get("current", page)
        pages = data.get("pages", 1)

        header = f"共{total}条结果，第{current}/{pages}页，显示{len(data['records'])}条。\n\n"

        line = ""
        status_map = {0: "待处理", 1: "已处理"}

        for feedback in data["records"]:
            fb_title = feedback.get("title", "无标题")
            fb_content = feedback.get("content", "")
            if fb_content is None:
                fb_content = "(无内容)"
            elif len(fb_content) > 150:
                fb_content = fb_content[:150] + "..."

            fb_status = status_map.get(feedback.get("status", 0), "未知状态")
            fb_contact = feedback.get("contact", "未填写")
            fb_reply = feedback.get("reply", "暂无回复")
            fb_time = feedback.get("createTime", "未知时间")
            fb_user_id = feedback.get('userId', '未知')

            line += (f"【{feedback['id']}】{fb_title}\n"
                     f"反馈人ID：{fb_user_id}\n"
                     f"状态：{fb_status} | 联系方式：{fb_contact}\n"
                     f"提交时间：{fb_time}\n"
                     f"摘要：{fb_content}\n"
                     f"管理员回复：{fb_reply}\n\n")

        return header + line

    async def _arun(self, page: int, size: int) -> str:
        return await asyncio.to_thread(self._run, page, size)