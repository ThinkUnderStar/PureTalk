from typing import Type

import httpx
from langchain_core.tools import BaseTool
from pydantic import Field,BaseModel

from puretalk_ai.config.settings import JAVA_BACKEND_URL, SERVICE_API_KEY
from puretalk_ai.core.contextvars import current_user_id


class GetPostsModel(BaseModel):
    category_id: int = Field(0,description="排序方式：0=综合排序，1=最热排序，2=最新排序。默认综合排序。")
    page: int = Field(1,description="页码")
    size: int = Field(10,description="每页数量")

class GetPostsTool(BaseTool):
    name : str = "get_posts_tool"
    description : str = "按照规定的排序获取帖子"
    args_schema : Type[BaseModel] = GetPostsModel

    def _run(self,  category_id: int, page: int, size: int) -> str:
        #设置请求头
        headers = {
            "X-Service-Token": SERVICE_API_KEY,
            "X-User-Id": str(current_user_id.get())
        }

        # 发送HTTP请求，并解析响应
        response = httpx.get(
            f"{JAVA_BACKEND_URL}/api/post",
            params={
                "categoryId": category_id,
                "page": page,
                "size": size
            },
            headers=headers,
        )

        if response.status_code != 200:
            return f"获取帖子列表异常，HTTP状态码：{response.status_code}"

        result = response.json()

        if result.get("code") != 200:
            return f"获取失败：{result.get('message', '未知错误')}"

        data = result.get("data", {})

        # 格式化结果
        total = data.get("total", 0)
        current = data.get("current", page)
        pages = data.get("pages", 1)

        header = f"共{total}条结果，第{current}/{pages}页，显示{len(data['records'])}条。\n\n"

        line = ""
        for post in data["records"]:
            content = ""

            if post["content"] is None:
                content = "(无内容)"

            else:
                if len(post["content"]) > 150:
                    content = (post["content"][:150] + "...")

                else:
                    content = post["content"]

            line += (f"【{post['id']}】{post['title']}\n作者: {post['userName']} "
                     f"| 发布于 {post['createTime']}\n浏览: {post['viewCount']} "
                     f"| 赞: {post['likeCount']} "
                     f"| 评论: {post['commentCount']}\n摘要: {content}\n\n")

        return header + line
