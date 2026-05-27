package thinkunderstar.puretalk.puretalkbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评论表
 */
@Data
@TableName("comments")
public class Comment {
    public Comment() {
    }

    public Comment(Long postId, Long userId, String content, Long parentId, Long replyUserId) {
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.parentId = parentId;
        this.replyUserId = replyUserId;
    }

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 评论用户ID
     */
    private Long userId;

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

    /**
     * 状态: 1-正常, 0-隐藏
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 更新时间
     */
    @Version
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 评论路径
     */
    private String path;
}