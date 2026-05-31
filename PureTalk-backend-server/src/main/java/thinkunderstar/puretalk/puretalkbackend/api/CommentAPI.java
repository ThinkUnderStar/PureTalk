package thinkunderstar.puretalk.puretalkbackend.api;

import org.springframework.web.bind.annotation.*;
import thinkunderstar.puretalk.puretalkbackend.common.DoSendComment;
import thinkunderstar.puretalk.puretalkbackend.common.Result;
import thinkunderstar.puretalk.puretalkbackend.service.SysCommentService;

@RestController
@RequestMapping("/api/comment")
public class CommentAPI {

    private final SysCommentService sysCommentService;

    public CommentAPI(SysCommentService sysCommentService) {
        this.sysCommentService = sysCommentService;
    }

    /**
     * 获取评论
     *
     * @param postId 帖子id
     * @param page 评论页数
     * @param size 一次返回的评论
     * @return 获取结果
     */
    @GetMapping
    public Result getComments(@RequestParam long postId,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "20") int size){
        return sysCommentService.getComments(postId,page,size);
    }

    /**
     * 发送一条评论
     *
     * @param doSendComment 评论信息
     * @return 发送结果
     */
    @PostMapping("/send")
    public Result sendComment(@RequestBody DoSendComment doSendComment){
        return sysCommentService.sendComment(doSendComment);
    }

    /**
     * 用户删除自己的评论
     *
     * @param commentId 评论的id
     * @return 删除结果
     */
    @DeleteMapping("/user/{commentId}/delete")
    public Result deleteMyComment(@PathVariable long commentId){
        return sysCommentService.deleteMyComment(commentId);
    }

    /**
     * 管理员删除评论
     *
     * @param commentId 评论的id
     * @return 删除结果
     */
    @DeleteMapping("/admin/{commentId}/delete")
    public Result deleteComment(@PathVariable long commentId){
        return sysCommentService.deleteComment(commentId);
    }
}
