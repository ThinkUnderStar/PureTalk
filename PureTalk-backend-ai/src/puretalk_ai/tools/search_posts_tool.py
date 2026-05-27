from typing import Type

import httpx
from langchain_core.tools import BaseTool
from pydantic import Field,BaseModel

from puretalk_ai.config.settings import JAVA_BACKEND_URL

class SearchPostsModel(BaseModel):
    keyword: str = Field(...,description="要搜索的关键词")
    category_id: int = Field(0,description="排序方式：0=综合排序，1=最热排序，2=最新排序。默认综合排序。")
    page: int = Field(1,description="页码")
    size: int = Field(10,description="每页数量")

class SearchPostsTool(BaseTool):
    name : str = "search_posts_tool"
    description : str = "搜索PureTalk论坛网站上公开的帖子"
    args_schema : Type[BaseModel] = SearchPostsModel

    def _run(self, keyword: str, category_id: int, page: int, size: int) -> str:
       response = httpx.get(
           f"{JAVA_BACKEND_URL}/post/search",
           params={
               "str": keyword,
               "categoryId": category_id,
               "page": page,
               "size": size
           }
       )

       if response.status_code != 200:
           return f"搜索接口异常，HTTP状态码：{response.status_code}"

       result = response.json()

       if result.get("code") != 200:
           return f"搜索失败：{result.get('message', '未知错误')}"

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
