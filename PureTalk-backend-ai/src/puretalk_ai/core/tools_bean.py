from puretalk_ai.tools.get_feedbacks_tool import GetFeedbacksTool
from puretalk_ai.tools.get_notification_target_tool import GetNotificationTargetTool
from puretalk_ai.tools.get_notifications_tool import GetNotificationsTool
from puretalk_ai.tools.get_post_comments_tool import GetPostCommentsTool
from puretalk_ai.tools.get_posts_tool import GetPostsTool
from puretalk_ai.tools.get_report_target_tool import GetReportTargetTool
from puretalk_ai.tools.get_reports_tool import GetReportsTool
from puretalk_ai.tools.get_user_posts_tool import GetUserPostsTool
from puretalk_ai.tools.search_posts_tool import SearchPostsTool

#创建全局工具对象
search_posts_tool_bean = SearchPostsTool()
get_posts_tool_bean = GetPostsTool()
get_user_posts_tool_bean = GetUserPostsTool()
get_post_comments_tool_bean = GetPostCommentsTool()
get_notifications_tool_bean = GetNotificationsTool()
get_notification_target_tool_bean = GetNotificationTargetTool()
get_feedbacks_tool_bean = GetFeedbacksTool()
get_reports_tool_bean = GetReportsTool()
get_report_target_tool_bean = GetReportTargetTool()
