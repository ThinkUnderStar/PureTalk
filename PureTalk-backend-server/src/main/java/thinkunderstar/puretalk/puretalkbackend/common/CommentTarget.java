package thinkunderstar.puretalk.puretalkbackend.common;

import lombok.Data;
import thinkunderstar.puretalk.puretalkbackend.entity.Comment;
import thinkunderstar.puretalk.puretalkbackend.entity.Post;

@Data
public class CommentTarget {
    public CommentTarget() {
    }

    public CommentTarget(Post post, Comment comment) {
        this.post = post;
        this.comment = comment;
    }

    private Post post;
    private Comment comment;
}
