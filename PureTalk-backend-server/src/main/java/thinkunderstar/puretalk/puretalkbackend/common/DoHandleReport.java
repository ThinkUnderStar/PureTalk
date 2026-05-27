package thinkunderstar.puretalk.puretalkbackend.common;

import lombok.Data;

@Data
public class DoHandleReport {
    /**
     * 举报id
     */
    private long reportId;

    /**
     * 处理管理员ID
     */
    private Long handlerId;

    /**
     * 处理结果备注
     */
    private String handleResult;

    /**
     * 是否删除帖子/评论
     */
    private boolean deleted;

    /**
     * 是否禁言该用户
     */
    private boolean banned;

    /**
     * 禁言原因
     */
    private String banReason;

    /**
     * 0代表永久禁言
     */
    private long banTime;

    /**
     * 是否驳回举报
     */
    private boolean denied;
}
