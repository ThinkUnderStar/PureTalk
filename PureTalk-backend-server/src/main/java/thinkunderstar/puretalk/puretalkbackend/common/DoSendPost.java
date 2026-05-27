package thinkunderstar.puretalk.puretalkbackend.common;

import lombok.Data;

@Data
public class DoSendPost {
    public DoSendPost(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public DoSendPost() {
    }

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子内容
     */
    private String content;
}
