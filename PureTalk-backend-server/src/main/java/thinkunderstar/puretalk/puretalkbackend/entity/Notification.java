package thinkunderstar.puretalk.puretalkbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notifications")
public class Notification {

    public Notification() {}

    public Notification(Long userId, Integer type, Long sourceId) {
        this.userId = userId;
        this.type = type;
        this.sourceId = sourceId;
    }

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收者用户ID */
    private Long userId;

    /** 通知类型: 1-举报结果 2-反馈回复 3-系统通知 */
    private Integer type;

    /** 来源ID（举报ID/反馈ID/系统通知ID） */
    private Long sourceId;

    /** 是否已读: 0-未读 1-已读 */
    private Integer isRead;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}