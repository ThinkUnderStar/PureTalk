package thinkunderstar.puretalk.puretalkbackend.common;

import lombok.Data;

@Data
public class DoPostMessage {
    /**
     * 0为新对话
     */
    private long sessionId;

    /**
     * 交互文本
     */
    private String message;
}
