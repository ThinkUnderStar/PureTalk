import asyncio
from typing import Type

import httpx
from langchain_core.tools import BaseTool
from pydantic import Field,BaseModel

from puretalk_ai.config.settings import JAVA_BACKEND_URL, SERVICE_API_KEY
from puretalk_ai.core.contextvars import current_user_id

class ExecuteSqlModel(BaseModel):
    sql: str = Field(description="查询功能的 SQL 语句")

class ExecuteSqlTool(BaseTool):
    name : str = "execute_sql_tool"
    description : str = "执行root用户要求执行的查询sql"
    args_schema : Type[BaseModel] = ExecuteSqlModel

    def _run(self, sql: str) -> str:
        # 清洗 Markdown 包裹
        sql = sql.strip()
        if sql.startswith("```sql"):
            sql = sql[6:]
        elif sql.startswith("```"):
            sql = sql[3:]
        if sql.endswith("```"):
            sql = sql[:-3]

        if not sql:
            return "错误：SQL 语句为空，请重新生成。"

        if current_user_id.get() != 1:
            return "错误：仅允许root用户执行查询功能，请重新生成。"

        if not sql.strip().upper().startswith("SELECT"):
            return "错误：仅允许执行 SELECT 查询语句，请重新生成。"

        #设置请求头
        headers = {
            "X-Service-Token": SERVICE_API_KEY,
            "X-User-Id": str(current_user_id.get())
        }

        # 发送HTTP请求，并解析响应
        response = httpx.post(
            f"{JAVA_BACKEND_URL}/api/root/execute-sql",
            json=sql,
            headers=headers,
        )

        if response.status_code != 200:
            return f"获取sql执行结果异常，HTTP状态码：{response.status_code}"

        result = response.json()

        if result.get("code") != 200:
            return f"获取sql执行结果失败：{result.get('message', '未知错误')}"

        data = result.get("data", {})
        if data is None:
            return "获取sql执行结果为空"

        # 格式化结果
        if isinstance(data, list):
            if not data:
                return "查询结果为空。"
            lines = ["查询结果如下："]
            for row in data:
                line = ", ".join(f"{k}: {v}" for k, v in row.items())
                lines.append(line)
            return "\n".join(lines)

        return str(data)

    async def _arun(self, sql: str) -> str:
        return await asyncio.to_thread(self._run,  sql)