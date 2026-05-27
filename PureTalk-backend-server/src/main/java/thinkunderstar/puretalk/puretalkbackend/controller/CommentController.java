package thinkunderstar.puretalk.puretalkbackend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import org.springframework.web.bind.annotation.*;
import thinkunderstar.puretalk.puretalkbackend.common.DoSendComment;
import thinkunderstar.puretalk.puretalkbackend.common.Result;
import thinkunderstar.puretalk.puretalkbackend.service.SysCommentService;

@RestController
@RequestMapping("/comment")
public class CommentController {

    private final SysCommentService sysCommentService;

    public CommentController(SysCommentService sysCommentService) {
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
    @SaCheckLogin
    @SaCheckPermission(value = "send:add",orRole = {"admin","root"})
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
    @SaCheckLogin
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
    @SaCheckLogin
    @SaCheckRole(value = {"admin","root"},mode = SaMode.OR)
    public Result deleteComment(@PathVariable long commentId){
        return sysCommentService.deleteComment(commentId);
    }

    /**
     * 修改点赞数
     *
     * @param commentId 评论id
     * @return 修改结果
     */
    @PutMapping("/{commentId}/like")
    @SaCheckLogin
    public Result updateLikeCount(@PathVariable long commentId){
        return sysCommentService.updateLikeCount(commentId);
    }
}
