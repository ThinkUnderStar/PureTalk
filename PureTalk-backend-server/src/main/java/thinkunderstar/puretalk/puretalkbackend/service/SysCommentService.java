package thinkunderstar.puretalk.puretalkbackend.service;

import thinkunderstar.puretalk.puretalkbackend.common.DoSendComment;
import thinkunderstar.puretalk.puretalkbackend.common.Result;

public interface SysCommentService {
    /**
     * 发送一条评论
     *
     * @param doSendComment 评论信息
     * @return 发送结果
     */
    Result sendComment(DoSendComment doSendComment);

    /**
     * 用户删除自己的评论
     *
     * @param commentId 评论的id
     * @return 删除结果
     */
    Result deleteMyComment(long commentId);

    /**
     * 管理员删除评论
     *
     * @param commentId 评论的id
     * @return 删除结果
     */
    Result deleteComment( long commentId);

    /**
     * 修改点赞数
     *
     * @param commentId 评论id
     * @return 修改结果
     */
    Result updateLikeCount(long commentId);

    /**
     * 获取评论
     *
     * @param postId 帖子id
     * @param page 评论页数
     * @param size 一次返回的评论
     * @return 获取结果
     */
    Result getComments( long postId,int page,int size);
}
