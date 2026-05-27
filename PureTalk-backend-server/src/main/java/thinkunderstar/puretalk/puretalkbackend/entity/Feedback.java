package thinkunderstar.puretalk.puretalkbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("feedbacks")
public class Feedback {
    public Feedback(Long userId, String title, String content, String contact) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.contact = contact;
    }

    public Feedback() {
    }

    /**
     * 反馈ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 反馈用户ID
     */
    private Long userId;

    /**
     * 反馈标题
     */
    private String title;

    /**
     * 反馈内容
     */
    private String content;

    /**
     * 联系方式（可选）
     */
    private String contact;

    /**
     * 处理状态 0-待处理 1-已处理
     */
    private Integer status;

    /**
     * 处理管理员ID
     */
    private Long handlerId;

    /**
     * 管理员回复
     */
    private String reply;

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
