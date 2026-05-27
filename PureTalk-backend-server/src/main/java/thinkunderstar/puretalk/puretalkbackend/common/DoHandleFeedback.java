package thinkunderstar.puretalk.puretalkbackend.common;

import lombok.Data;

@Data
public class DoHandleFeedback {
    /**
     * 反馈ID
     */
    private Long feedbackId;

    /**
     * 处理管理员ID
     */
    private Long handlerId;

    /**
     * 管理员回复
     */
    private String reply;
}
