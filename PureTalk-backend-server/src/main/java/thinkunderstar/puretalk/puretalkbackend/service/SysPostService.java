package thinkunderstar.puretalk.puretalkbackend.service;

import thinkunderstar.puretalk.puretalkbackend.common.DoSendPost;
import thinkunderstar.puretalk.puretalkbackend.common.Result;

import java.util.Map;

public interface SysPostService {
    /**
     * 发送POST请求以发布新帖子。
     *
     * @param doSendPost 包含帖子标题、内容及发布用户ID的对象
     * @return 返回一个Result对象，包含状态码、提示信息及数据。成功时返回200状态码，失败则返回500状态码，并附带相应的错误信息
     */
    Result sendPost(DoSendPost doSendPost);

    /**
     * 用户删除自己发出的帖子
     *
     * @param postId 帖子的id
     * @return 删除结果
     */
    Result deleteMyPost(long postId);

    /**
     * 管理员删除帖子
     *
     * @param postId 帖子id
     * @return 删除结果
     */
    Result deletePost(long postId);

    /**
     * 增减点赞数
     *
     * @param postId 点赞帖子id
     * @return 点赞结果
     */
    Result updateLikeCount(long postId);

    /**
     * 浏览量增加
     *
     * @param viewMap 浏览量
     * @return 刷新结果
     */
    Result updateViewCount(Map<Long,Integer> viewMap);

    /**
     * 整点执行一次
     * 刷新该帖子的查询系数
     */
    void refreshSearchScore();

    /**
     * 获取评论
     *
     * @param categoryId 板块
     * @param page 帖子页数
     * @param size 一次返回的帖子
     * @return 获取结果
     */
    Result getPosts(long categoryId, int page, int size);

    /**
     * 搜索关键词
     *
     * @param str 关键词
     * @return 搜索结果
     */
    Result searchPosts(String str, long categoryId, int page, int size);
}
