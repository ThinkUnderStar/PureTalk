package thinkunderstar.puretalk.puretalkbackend.common;

import lombok.Data;

@Data
public class DoReport {
    /**
     * 举报类型 1-帖子 2-评论
     */
    private Integer reportType;

    /**
     * 被举报目标ID（帖子id/评论id）
     */
    private Long targetId;

    /**
     * 被举报内容的所有者ID（帖子作者ID/评论作者ID）
     */
    private Long targetUserId;

    /**
     * 举报原因
     */
    private String reason;
}
