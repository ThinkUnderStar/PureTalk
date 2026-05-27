package thinkunderstar.puretalk.puretalkbackend.common;

import lombok.Data;
import thinkunderstar.puretalk.puretalkbackend.entity.Comment;

@Data
public class CommentVO extends Comment {
    //新增返回值
    private String userName;
    private String avatar;
    private String replyUserName;
}
