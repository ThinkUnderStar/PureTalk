package thinkunderstar.puretalk.puretalkbackend.common;

import lombok.Data;

@Data
public class DoSendComment {
    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论ID，0表示直接评论帖子
     */
    private Long parentId;

    /**
     * 被回复的用户ID
     */
    private Long replyUserId;
}
