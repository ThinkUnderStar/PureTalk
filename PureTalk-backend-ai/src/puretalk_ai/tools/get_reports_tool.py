import asyncio
from typing import Type

import httpx
from langchain_core.tools import BaseTool
from pydantic import Field,BaseModel

from puretalk_ai.config.settings import JAVA_BACKEND_URL, SERVICE_API_KEY
from puretalk_ai.core.contextvars import current_user_id

class GetReportsModel(BaseModel):
    page: int = Field(1,description="页码")
    size: int = Field(10,description="每页数量")

class GetReportsTool(BaseTool):
    name : str = "get_reports_tool"
    description : str = "按照时间升序的顺序获取为未处理的举报信息列表"
    args_schema : Type[BaseModel] = GetReportsModel

    def _run(self, page: int, size: int) -> str:
        #设置请求头
        headers = {
            "X-Service-Token": SERVICE_API_KEY,
            "X-User-Id": str(current_user_id.get())
        }

        # 发送HTTP请求，并解析响应
        response = httpx.get(
            f"{JAVA_BACKEND_URL}/api/report",
            params={
                "page": page,
                "size": size
            },
            headers=headers,
        )

        if response.status_code != 200:
            return f"获取举报列表异常，HTTP状态码：{response.status_code}"

        result = response.json()

        if result.get("code") != 200:
            return f"获取举报列表失败：{result.get('message', '未知错误')}"

        data = result.get("data", {})
        if data is None:
            return "获取结果为空"

        # 格式化结果
        total = data.get("total", 0)
        current = data.get("current", page)
        pages = data.get("pages", 1)
        header = f"共{total}条结果，第{current}/{pages}页，显示{len(data['records'])}条。\n\n"

        line = ""
        report_type_map = {1: "帖子举报", 2: "评论举报"}
        status_map = {0: "待处理", 1: "已处理", 2: "已驳回"}
        for report in data["records"]:
            report_type_name = report_type_map.get(report.get("reportType", 0), "未知类型")
            report_status = status_map.get(report.get("status", 0), "未知状态")
            report_reason = report.get("reason", "未填写")
            report_result = report.get("handleResult", "未处理")
            report_time = report.get("createTime", "未知时间")
            report_user_id = report.get("reportUserId", "未知")
            line += (f"【{report['id']}】{report_type_name}\n"
                     f"举报人ID：{report_user_id}\n"
                     f"被举报目标ID：{report.get('targetId', '未知')}\n"
                     f"原因：{report_reason}\n"
                     f"状态：{report_status}\n"
                     f"处理结果：{report_result}\n"
                     f"提交时间：{report_time}\n\n")
        return header + line

    async def _arun(self, page: int, size: int) -> str:
        return await asyncio.to_thread(self._run, page, size)