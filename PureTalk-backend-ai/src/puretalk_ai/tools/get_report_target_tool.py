import asyncio
from typing import Type

import httpx
from langchain_core.tools import BaseTool
from pydantic import Field,BaseModel

from puretalk_ai.config.settings import JAVA_BACKEND_URL, SERVICE_API_KEY
from puretalk_ai.core.contextvars import current_user_id

class GetReportTargetModel(BaseModel):
    report_id: int = Field(...,description="举报ID")

class GetReportTargetTool(BaseTool):
    name : str = "get_report_target_tool"
    description : str = "获得举报的目标用户的的具体信息"
    args_schema : Type[BaseModel] = GetReportTargetModel

    def _run(self, report_id: int) -> str:
        #设置请求头
        headers = {
            "X-Service-Token": SERVICE_API_KEY,
            "X-User-Id": str(current_user_id.get())
        }

        # 发送HTTP请求，并解析响应
        response = httpx.get(
            f"{JAVA_BACKEND_URL}/api/report/{report_id}/target",
            headers=headers,
        )

        if response.status_code != 200:
            return f"获得举报的目标用户的的具体信息接口异常，HTTP状态码：{response.status_code}"

        result = response.json()

        if result.get("code") != 200:
            return f"获得举报的目标用户的的具体信息失败：{result.get('message', '未知错误')}"

        data = result.get("data", {})
        if data is None:
            return "举报的目标用户的的具体信息数据为空，可能已被删除。"

        # 格式化结果
        target_type = data.get("type")

        if target_type == 1:
            post = data.get("post")
            if post is None:
                return "举报目标数据为空"
            content = post.get("content", "")
            if content and len(content) > 200:
                content = content[:200] + "..."
            return (
                f"举报类型：帖子举报\n"
                f"帖子ID：{post.get('id')}\n"
                f"标题：{post.get('title', '无标题')}\n"
                f"内容：{content or '(无内容)'}\n"
                f"作者ID：{post.get('userId', '未知')}\n"
                f"发布时间：{post.get('createTime', '未知')}"
            )

        elif target_type == 2:
            comment_target = data.get("commentTarget")
            if comment_target is None:
                return "举报目标数据为空"
            post = comment_target.get("post")
            comment = comment_target.get("comment")
            info = "举报类型：评论举报\n"

            if post:
                info += f"所属帖子ID：{post.get('id')}\n标题：{post.get('title', '无标题')}\n"

            if comment:
                c_content = comment.get("content", "")

                if c_content and len(c_content) > 200:
                    c_content = c_content[:200] + "..."
                info += (
                    f"评论ID：{comment.get('id')}\n"
                    f"评论内容：{c_content or '(无内容)'}\n"
                    f"评论者ID：{comment.get('userId', '未知')}\n"
                    f"评论时间：{comment.get('createTime', '未知')}"
                )

            return info

        return f"未知举报类型：{target_type}"


    async def _arun(self, report_id: int) -> str:
        return await asyncio.to_thread(self._run, report_id)