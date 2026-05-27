from pydantic import Field

from pydantic import BaseModel

class DoChat(BaseModel):
    name: str = Field(default="用户")
    user_id: int = Field(...,description="用户ID")
    role: int = Field(...,description="用户角色,角色: 1-普通用户, 2-管理员, 3-root")
    status: int = Field(...,description="用户状态,状态: 1-正常, 0-禁言")
    context: list[dict[str, str]] = Field(default=[])

    