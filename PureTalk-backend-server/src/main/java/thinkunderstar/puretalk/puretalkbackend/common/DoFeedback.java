package thinkunderstar.puretalk.puretalkbackend.common;

import lombok.Data;

@Data
public class DoFeedback {
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
}
