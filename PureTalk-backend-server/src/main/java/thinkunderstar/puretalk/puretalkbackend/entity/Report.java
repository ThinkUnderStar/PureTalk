package thinkunderstar.puretalk.puretalkbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("reports")
public class Report {
    public Report(Long reportUserId, Integer reportType, Long targetId, Long targetUserId, String reason, Integer status) {
        this.reportUserId = reportUserId;
        this.reportType = reportType;
        this.targetId = targetId;
        this.targetUserId = targetUserId;
        this.reason = reason;
        this.status = status;
    }

    public Report() {
    }

    /**
     * 举报ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 举报用户ID
     */
    private Long reportUserId;

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

    /**
     * 处理状态 0-待处理 1-已处理 2-驳回
     */
    private Integer status;

    /**
     * 处理管理员ID
     */
    private Long handlerId;

    /**
     * 处理结果备注
     */
    private String handleResult;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Version
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
