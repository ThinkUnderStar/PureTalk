package thinkunderstar.puretalk.puretalkbackend.common;

import lombok.Data;
import thinkunderstar.puretalk.puretalkbackend.entity.Post;

@Data
public class ReturnReportTarget {
    public ReturnReportTarget(int type, Post post) {
        this.type = type;
        this.post = post;
    }

    public ReturnReportTarget(int type, CommentTarget commentTarget) {
        this.type = type;
        this.commentTarget = commentTarget;
    }

    public ReturnReportTarget() {
    }

    //1-帖子 2-评论
    private int type;
    private Post post;
    private CommentTarget commentTarget;
}
