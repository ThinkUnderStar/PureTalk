import asyncio
from typing import Type

import httpx
from langchain_core.tools import BaseTool
from pydantic import Field,BaseModel

from puretalk_ai.config.settings import JAVA_BACKEND_URL, SERVICE_API_KEY
from puretalk_ai.core.contextvars import current_user_id

class GetPostCommentsModel(BaseModel):
    post_id: int = Field(...,description="帖子ID")
    page: int = Field(1,description="页码")
    size: int = Field(10,description="每页数量")

class GetPostCommentsTool(BaseTool):
    name : str = "get_post_comments_tool"
    description : str = "从新到老的获取指定帖子的所有评论"
    args_schema : Type[BaseModel] = GetPostCommentsModel

    def _run(self, post_id: int, page: int, size: int) -> str:
        #设置请求头
        headers = {
            "X-Service-Token": SERVICE_API_KEY,
            "X-User-Id": str(current_user_id.get())
        }

        # 发送HTTP请求，并解析响应
        response = httpx.get(
            f"{JAVA_BACKEND_URL}/api/comment",
            params={
                "postId": post_id,
                "page": page,
                "size": size,
            },
            headers=headers,
        )

        if response.status_code != 200:
            return f"获取帖子评论列表异常，HTTP状态码：{response.status_code}"

        result = response.json()

        if result.get("code") != 200:
            return f"获取帖子评论列表失败：{result.get('message', '未知错误')}"

        data = result.get("data", {})
        if data is None:
            return "获取结果为空"

        # 格式化结果
        total = data.get("total", 0)
        current = data.get("current", page)
        pages = data.get("pages", 1)

        header = f"共{total}条结果，第{current}/{pages}页，显示{len(data['records'])}条。\n\n"

        line = ""
        for comment in data["records"]:
            content = ""
            if comment.get("content") is None:
                content = "(无内容)"
            else:
                if len(comment["content"]) > 150:
                    content = comment["content"][:150] + "..."
                else:
                    content = comment["content"]

            if comment.get("parentId") == 0:
                reply_info = "评论了该帖子"
            else:
                reply_info = f"回复了 {comment.get('replyUserName', '未知用户')}"

            line += (f"【{comment['id']}】{comment.get('userName', '未知用户')} {reply_info}\n"
                     f"内容: {content}\n"
                     f"赞: {comment.get('likeCount', 0)} | {comment.get('createTime', '未知')}\n\n")

        return header + line

    async def _arun(self, post_id: int, page: int, size: int) -> str:
        return await asyncio.to_thread(self._run, post_id, page, size)