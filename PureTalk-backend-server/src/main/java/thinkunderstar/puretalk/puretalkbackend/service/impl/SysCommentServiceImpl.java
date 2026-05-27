package thinkunderstar.puretalk.puretalkbackend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import thinkunderstar.puretalk.puretalkbackend.common.CommentVO;
import thinkunderstar.puretalk.puretalkbackend.common.DoSendComment;
import thinkunderstar.puretalk.puretalkbackend.common.Result;
import thinkunderstar.puretalk.puretalkbackend.entity.Comment;
import thinkunderstar.puretalk.puretalkbackend.entity.Post;
import thinkunderstar.puretalk.puretalkbackend.entity.User;
import thinkunderstar.puretalk.puretalkbackend.exception.BusinessException;
import thinkunderstar.puretalk.puretalkbackend.mapper.CommentMapper;
import thinkunderstar.puretalk.puretalkbackend.service.CommentService;
import thinkunderstar.puretalk.puretalkbackend.service.PostService;
import thinkunderstar.puretalk.puretalkbackend.service.SysCommentService;
import thinkunderstar.puretalk.puretalkbackend.service.UserService;
import thinkunderstar.puretalk.puretalkbackend.util.RedisTokenBucketLimiter;
import thinkunderstar.puretalk.puretalkbackend.util.RedisUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SysCommentServiceImpl implements SysCommentService {
    private final SensitiveWordManager sensitiveWordManager;
    private final CommentService commentService;
    private final RedisUtils redisUtils;
    private final CommentMapper commentMapper;
    private final PostService postService;
    private final RedisTokenBucketLimiter redisTokenBucketLimiter;
    private final UserService userService;

    public SysCommentServiceImpl(SensitiveWordManager sensitiveWordManager, CommentService commentService, RedisUtils redisUtils, CommentMapper commentMapper, PostService postService, RedisTokenBucketLimiter redisTokenBucketLimiter, UserService userService) {
        this.sensitiveWordManager = sensitiveWordManager;
        this.commentService = commentService;
        this.redisUtils = redisUtils;
        this.commentMapper = commentMapper;
        this.postService = postService;
        this.redisTokenBucketLimiter = redisTokenBucketLimiter;
        this.userService = userService;
    }

    @Override
    public Result sendComment(DoSendComment doSendComment) {
        if (doSendComment.getContent() == null || doSendComment.getContent().isEmpty()){
            throw new BusinessException("评论内容不能为空");
        }

        //用户限流
        boolean success = redisTokenBucketLimiter.tryAcquireByUser(StpUtil.getLoginIdAsString(), 5, 1);

        if (!success){
            throw new BusinessException("发送过于频繁");
        }

        if (sensitiveWordManager.checkSensitiveWord(doSendComment.getContent())){
            throw new BusinessException("文本中有敏感词汇");
        }

        Comment comment = new Comment(doSendComment.getPostId()
                , StpUtil.getLoginIdAsLong()
                , doSendComment.getContent()
                , doSendComment.getParentId()
                , doSendComment.getReplyUserId());

        if (doSendComment.getParentId() == 0){
            comment.setPath(doSendComment.getPostId()+"/");
        }else {
            String parentPath = commentService.getById(doSendComment.getParentId()).getPath();
            comment.setPath(parentPath + doSendComment.getParentId() + "/");
        }

        commentService.save(comment);

        //评论数+1
        Post post = postService.getById(doSendComment.getPostId());
        post.setCommentCount(post.getCommentCount()+1);
        postService.updateById(post);

        return Result.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result deleteMyComment(long commentId) {
        Comment comment = commentService.getById(commentId);

        if (comment == null){
            throw new BusinessException("未查询到该评论");
        }

        if (comment.getUserId() != StpUtil.getLoginIdAsLong()){
            throw new BusinessException("不能删除他人的评论");
        }

        List<Comment> list = commentService.list(new LambdaQueryWrapper<Comment>().likeRight(Comment::getPath, comment.getPath() + comment.getId() + "/"));
        list.add(comment);

        //删除用户自己发出的评论的业务代码
        commentService.removeByIds(list);

        Post post = postService.getById(comment.getPostId());

        if (post == null){
            throw new BusinessException("该帖子不存在");
        }

        post.setCommentCount(post.getCommentCount() - list.size());
        postService.updateById(post);

        return Result.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result deleteComment(long commentId) {
        Comment comment = commentService.getById(commentId);

        if (comment == null){
            throw new BusinessException("未查询到该评论");
        }

        List<Comment> list = commentService.list(new LambdaQueryWrapper<Comment>().likeRight(Comment::getPath, comment.getPath() + comment.getId() + "/"));
        list.add(comment);

        //删除用户自己发出的评论的业务代码
        commentService.removeByIds(list);

        Post post = postService.getById(comment.getPostId());

        if (post == null){
            throw new BusinessException("该帖子不存在");
        }

        post.setCommentCount(post.getCommentCount() - list.size());
        postService.updateById(post);

        return Result.success();
    }

    @Override
    public Result updateLikeCount(long commentId) {
        //用户限流
        boolean success = redisTokenBucketLimiter.tryAcquireByUser(StpUtil.getLoginIdAsString(), 10, 1);

        if (!success) {
            throw new BusinessException("点赞过于频繁");
        }

        Comment comment = commentService.getById(commentId);

        if (comment == null){
            throw new BusinessException("未查询到该评论");
        }

        String key = StpUtil.getLoginIdAsString() + ":isLikeComment:" + commentId;

        if (redisUtils.get(key) == null) {
            commentService.update(new LambdaUpdateWrapper<Comment>()
                    .eq(Comment::getId,commentId)
                    .setSql("like_count = like_count + 1"));

            redisUtils.set(key,"1",1, TimeUnit.DAYS);
        } else {
            commentService.update(new LambdaUpdateWrapper<Comment>()
                    .eq(Comment::getId,commentId)
                    .setSql("like_count = like_count - 1"));

            redisUtils.delete(key);
        }

        return Result.success();
    }

    @Override
    public Result getComments(long postId, int page, int size) {
        Page<Comment> commentPage = new Page<>(page,size);
        LambdaQueryWrapper<Comment> lambdaQueryWrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPostId,postId)
                .orderByDesc(Comment::getCreateTime);

        Page<Comment> pageResult = commentMapper.selectPage(commentPage,lambdaQueryWrapper);

        //封装VO类
        Set<Long> userIds = pageResult.getRecords().stream().map(Comment::getUserId).collect(Collectors.toSet());

        Map<Long, User> map = new HashMap<>();

        List<User> users = List.of();
        if (!userIds.isEmpty()){
            users = userService.listByIds(userIds);
        }

        users.forEach(user -> {
            map.put(user.getId(),user);
        });

        List<CommentVO> voList = pageResult.getRecords().stream().map(comment -> {
            CommentVO commentVO = new CommentVO();
            BeanUtils.copyProperties(comment, commentVO);
            //新增字段
            if (map.get(comment.getUserId()) == null){
                commentVO.setUserName("该账号已注销");
                commentVO.setAvatar(null);
            }else {
                commentVO.setUserName(map.get(comment.getUserId()).getUsername());
                commentVO.setAvatar(map.get(comment.getUserId()).getAvatar());
            }

            if (comment.getParentId() == 0){
                commentVO.setReplyUserName("该帖子");
            } else if (map.get(comment.getReplyUserId()) == null) {
                commentVO.setReplyUserName("该账号已注销");
            } else {
                commentVO.setReplyUserName(map.get(comment.getReplyUserId()).getUsername());
            }

            return commentVO;
        }).toList();

        Page<CommentVO> pageVOResult = new Page<>(page,size,pageResult.getTotal());
        pageVOResult.setRecords(voList);

        return Result.success(pageVOResult);
    }
}
