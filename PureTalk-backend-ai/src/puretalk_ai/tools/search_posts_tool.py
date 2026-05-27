import httpx
from langchain_core.tools import BaseTool
from pydantic import Field,BaseModel

from puretalk_ai.config.settings import JAVA_BACKEND_URL


#  search_posts —— 搜索帖子
# 调用 Java 接口：GET {JAVA_BACKEND_URL}/post/search
#
# 参数：keyword（AI 传入）, page, size（AI 可选传入，有默认值）
#
# 返回：格式化后的帖子列表文本
#String str, long categoryId, int page, int size
# 安全：无需身份验证，公开接口
# {
#   "code": 200,
#   "message": "success",
#   "data": {
#     "records": [
#       {"id": 1, "title": "帖子1", "userName": "张三", "createTime": "..."},
#       {"id": 2, "title": "帖子2", "userName": "李四", "createTime": "..."}
#     ],
#     "total": 50,
#     "size": 20,
#     "current": 1,
#     "pages": 3
#   }
# }

class SearchPostsModel(BaseModel):
    keyword: str = Field(...,description="要搜索的关键词")
    category_id: int = Field(0,description="排序方式：0=综合排序，1=最热排序，2=最新排序。默认综合排序。")
    page: int = Field(1,description="页码")
    size: int = Field(10,description="每页数量")

class SearchPostsTool(BaseTool):
    name = "search_posts_tool"
    description = "搜索PureTalk论坛网站上公开的帖子"
    args_schema = SearchPostsModel

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

       if result["code"] != 200:
           return "查询失败"

       line = ""
       for post in result["data"]["records"]:
           content = ""

           if post["content"] is None:
               content = "(无内容)"

           else:
               if len(post["content"]) > 150:
                    content = (post["content"][:150] + "...")

               else:
                   content = post["content"]

           line += (f"【{post['id']}】{post['title']}\n作者: {post['username']} "
                    f"| 发布于 {post['createTime']}\n浏览: {post['viewCount']} "
                    f"| 赞: {post['likeCount']} "
                    f"| 评论: {post['commentCount']}\n摘要: {content}\n\n")

       return line
