package thinkunderstar.puretalk.puretalkbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 帖子表
 */
@Data
@TableName("posts")
public class Post {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子内容
     */
    private String content;

    /**
     * 发布用户ID
     */
    private Long userId;

    /**
     * 分类ID，0表示默认板块
     */
    private Long categoryId;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 热度分数
     */
    private BigDecimal hotScore;

    /**
     * 综合评分，用于综合评估帖子的热度、互动度等多个因素后的得分。
     * 该分数可能基于点赞数、评论数、浏览量等指标计算得出，具体算法根据业务需求而定。
     */
    private BigDecimal compositeScore;

    /**
     * 状态: 1-公开, 0-隐藏/审核中, -1-删除
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 最后活跃的时间
     */
    @Version
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}