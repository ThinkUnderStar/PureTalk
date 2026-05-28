from _contextvars import ContextVar

current_user_id: ContextVar[int] = ContextVar("current_user_id",default=0)
current_user_role: ContextVar[int] = ContextVar("current_user_role",default=0)
current_user_status: ContextVar[int] = ContextVar("current_user_status",default=0)
current_user_name: ContextVar[str] = ContextVar("current_user_name",default="未知用户")