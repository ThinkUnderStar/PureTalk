from _contextvars import ContextVar

current_user_id: ContextVar[int] = ContextVar("current_user_id")
current_user_role: ContextVar[int] = ContextVar("current_user_role")
current_user_status: ContextVar[int] = ContextVar("current_user_status")
current_user_name: ContextVar[str] = ContextVar("current_user_name")