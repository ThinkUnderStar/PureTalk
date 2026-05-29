package thinkunderstar.puretalk.puretalkbackend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import thinkunderstar.puretalk.puretalkbackend.common.DoSendPost;
import thinkunderstar.puretalk.puretalkbackend.common.PostVO;
import thinkunderstar.puretalk.puretalkbackend.common.Result;
import thinkunderstar.puretalk.puretalkbackend.entity.Comment;
import thinkunderstar.puretalk.puretalkbackend.entity.Post;
import thinkunderstar.puretalk.puretalkbackend.entity.User;
import thinkunderstar.puretalk.puretalkbackend.exception.BusinessException;
import thinkunderstar.puretalk.puretalkbackend.mapper.PostMapper;
import thinkunderstar.puretalk.puretalkbackend.service.CommentService;
import thinkunderstar.puretalk.puretalkbackend.service.PostService;
import thinkunderstar.puretalk.puretalkbackend.service.SysPostService;
import thinkunderstar.puretalk.puretalkbackend.service.UserService;
import thinkunderstar.puretalk.puretalkbackend.util.RedisTokenBucketLimiter;
import thinkunderstar.puretalk.puretalkbackend.util.RedisUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysPostServiceImpl implements SysPostService {
    private final long COMPREHENSIVE_SECTOR = 0;
    private final long HOTTEST_SECTOR = 1;
    private final long LATEST_SECTOR = 2;
    private static final BigDecimal WEIGHT_VIEW = BigDecimal.valueOf(1);
    private static final BigDecimal WEIGHT_LIKE = BigDecimal.valueOf(5);
    private static final BigDecimal WEIGHT_COMMENT = BigDecimal.valueOf(10);

    private final PostService postService;
    private final SensitiveWordManager sensitiveWordManager;
    private final CommentService commentService;
    private final RedisUtils redisUtils;
    private final PostMapper postMapper;
    private final RedisTokenBucketLimiter redisTokenBucketLimiter;
    private final UserService userService;

    public SysPostServiceImpl(PostService postService, SensitiveWordManager sensitiveWordManager, CommentService commentService, RedisUtils redisUtils, PostMapper postMapper, RedisTokenBucketLimiter redisTokenBucketLimiter, UserService userService) {
        this.postService = postService;
        this.sensitiveWordManager = sensitiveWordManager;
        this.commentService = commentService;
        this.redisUtils = redisUtils;
        this.postMapper = postMapper;
        this.redisTokenBucketLimiter = redisTokenBucketLimiter;
        this.userService = userService;
    }

    @Override
    public Result sendPost(DoSendPost doSendPost) {

        if (doSendPost.getContent() == null || doSendPost.getContent().isEmpty()){
            throw new BusinessException("不能发送空文本");
        }

        //用户限流
        boolean success = redisTokenBucketLimiter.tryAcquireByUser(StpUtil.getLoginIdAsString(), 5, 1);

        if(!success){
            throw new BusinessException("发送过于频繁");
        }

        Post post = new Post();

        //敏感词审核
        if (sensitiveWordManager.checkSensitiveWord(doSendPost.getTitle())){
            throw new BusinessException("标题中含有敏感词");
        }

        if (sensitiveWordManager.checkSensitiveWord(doSendPost.getContent())){
            throw new BusinessException("文本中有敏感词汇");
        }

        post.setUserId(StpUtil.getLoginIdAsLong());
        post.setTitle(doSendPost.getTitle());
        post.setContent(doSendPost.getContent());

        postService.save(post);

        return Result.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result deleteMyPost(long postId) {
        Post post = postService.getById(postId);

        if (post == null){
            throw new BusinessException("未查询到该帖子");
        }

        if (post.getUserId() != StpUtil.getLoginIdAsLong()){
            throw new BusinessException("不能删除他人的帖子");
        }

        //删除用户自己发出的帖子的业务代码
        commentService.remove(new LambdaQueryWrapper<Comment>().eq(Comment::getPostId, postId));
        postService.removeById(postId);

        return Result.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result deletePost(long postId) {
        Post post = postService.getById(postId);

        if (post == null){
            throw new BusinessException("未查询到该帖子");
        }

        //删除帖子的业务代码
        commentService.remove(new QueryWrapper<Comment>().eq("postId",postId));
        postService.removeById(postId);

        return Result.success();
    }

    @Override
    public Result updateLikeCount(long postId) {
        //用户限流
        boolean success = redisTokenBucketLimiter.tryAcquireByUser(StpUtil.getLoginIdAsString(), 10, 1);

        if (!success) {
            throw new BusinessException("点赞过于频繁");
        }

        Post post = postService.getById(postId);

        if (post == null){
            throw new BusinessException("未查询到该帖子");
        }

        String key = StpUtil.getLoginIdAsString() + ":isLikePost:" + postId;

        if (redisUtils.get(key) == null) {
            postService.update(new LambdaUpdateWrapper<Post>()
                    .eq(Post::getId,postId)
                    .setSql("like_count = like_count + 1"));

            redisUtils.set(key,"1",1, TimeUnit.DAYS);
        } else {
            postService.update(new LambdaUpdateWrapper<Post>()
                    .eq(Post::getId,postId)
                    .setSql("like_count = like_count - 1"));

            redisUtils.delete(key);
        }

        return Result.success();
    }

    @Override
    public Result updateViewCount(Map<Long, Integer> viewMap) {
        if (viewMap == null || viewMap.isEmpty()) {
            return Result.success();
        }

        LambdaUpdateWrapper<Post> updateWrapper = new LambdaUpdateWrapper<>();

        StringBuilder stringBuilder = new StringBuilder("view_count = CASE id ");
        List<Long> ids = new ArrayList<>();
        viewMap.forEach((k,v) -> {
            stringBuilder.append("WHEN ").append(k).append(" THEN view_count + ").append(v).append(" ");
            ids.add(k);
        });

        stringBuilder.append("END");

        updateWrapper.setSql(stringBuilder.toString())
                .in(Post::getId, ids);

        postService.update(updateWrapper);

        return Result.success();
    }

    @Override
    public void refreshSearchScore() {

        LambdaQueryWrapper<Post> queryWrapperWrapper = new LambdaQueryWrapper<>();
        queryWrapperWrapper.orderByDesc(Post::getUpdateTime);
        queryWrapperWrapper.last("LIMIT 5000");
        List<Post> list = postService.list(queryWrapperWrapper);

        if (list == null || list.isEmpty()){
            log.info("还没有可刷新查询系数的数据");

            return;
        }

        log.info("开始刷新最近活跃前5000的查询系数");

        list.forEach(post -> {
            BigDecimal viewCount = BigDecimal.valueOf(post.getViewCount());
            BigDecimal commentCount = BigDecimal.valueOf(post.getCommentCount());
            BigDecimal likeCount = BigDecimal.valueOf(post.getLikeCount());
            BigDecimal betweenHours = BigDecimal.valueOf(ChronoUnit.HOURS.between(post.getCreateTime(), LocalDateTime.now()));

            //计算综合查询系数
            post.setCompositeScore(viewCount
                    .add(commentCount.multiply(WEIGHT_COMMENT))
                    .add(likeCount.multiply(WEIGHT_LIKE))
                    .divide(betweenHours.add(BigDecimal.valueOf(1))
                            ,4
                            , RoundingMode.HALF_UP));

            //计算热度查询系数
            post.setHotScore(viewCount
                    .add(likeCount.multiply(WEIGHT_LIKE))
                    .add(commentCount.multiply(WEIGHT_COMMENT)));
        });

        postService.updateBatchById(list);

        log.info("刷新成功");
    }

    @Override
    public Result getPosts(long categoryId, int page, int size) {
        if (!(categoryId == 0||categoryId == 1||categoryId == 2)){
            throw new BusinessException("板块异常");
        }

        Page<Post> postPage = new Page<>(page,size);
        Page<Post> pageResult;

        if (categoryId == COMPREHENSIVE_SECTOR){
            //综合系数分页查询
            LambdaQueryWrapper<Post> lambdaQueryWrapper = (new LambdaQueryWrapper<Post>())
                    .orderByDesc(Post::getCompositeScore);
            pageResult = postMapper.selectPage(postPage, lambdaQueryWrapper);
        } else if (categoryId == HOTTEST_SECTOR) {
            //热度系数分页查询
            LambdaQueryWrapper<Post> lambdaQueryWrapper = (new LambdaQueryWrapper<Post>())
                    .orderByDesc(Post::getHotScore);
            pageResult = postMapper.selectPage(postPage, lambdaQueryWrapper);
        } else {
            //时间倒叙分页查询
            LambdaQueryWrapper<Post> lambdaQueryWrapper = (new LambdaQueryWrapper<Post>())
                    .orderByDesc(Post::getCreateTime);
            pageResult = postMapper.selectPage(postPage, lambdaQueryWrapper);
        }

        //封装VO类
        Page<PostVO> pageVOResult = getPostVOPage(page, size, pageResult);

        return Result.success(pageVOResult);
    }

    //内部分装postVO方法
    private @NonNull Page<PostVO> getPostVOPage(int page, int size, Page<Post> pageResult) {
        Set<Long> userIds = pageResult.getRecords().stream().map(Post::getUserId).collect(Collectors.toSet());

        Map<Long,User> map = new HashMap<>();

        List<User> users = List.of();
        if (!userIds.isEmpty()){
             users = userService.listByIds(userIds);
        }

        users.forEach(user -> {
            map.put(user.getId(),user);
        });

        List<PostVO> voList = pageResult.getRecords().stream().map(post -> {
            PostVO postVO = new PostVO();
            BeanUtils.copyProperties(post, postVO);
            //新增字段
            if (map.get(post.getUserId()) == null){
                postVO.setUserName("该用户已注销");
                postVO.setAvatar(null);
            }else {
                postVO.setUserName(map.get(post.getUserId()).getUsername());
                postVO.setAvatar(map.get(post.getUserId()).getAvatar());
            }

            return postVO;
        }).toList();

        Page<PostVO> pageVOResult = new Page<>(page, size, pageResult.getTotal());
        pageVOResult.setRecords(voList);
        return pageVOResult;
    }

    @Override
    public Result searchPosts(String str, long categoryId, int page, int size) {
        LambdaQueryWrapper<Post> lambdaQuery = new LambdaQueryWrapper<Post>().like(Post::getTitle,  str);
        LambdaQueryWrapper<Post> query;

        if(categoryId == HOTTEST_SECTOR){
            query = lambdaQuery.orderByDesc(Post::getHotScore);
        } else if (categoryId == COMPREHENSIVE_SECTOR) {
            query = lambdaQuery.orderByDesc(Post::getCompositeScore);
        } else if (categoryId == LATEST_SECTOR) {
            query = lambdaQuery.orderByDesc(Post::getCreateTime);
        }else {
            log.error("搜索模块板块参数不符和规定");
            throw new BusinessException("板块参数不符");
        }

        Page<Post> postPage = new Page<>(page,size);
        Page<Post> postResultPage = postMapper.selectPage(postPage, query);

        //封装postVO
        Page<PostVO> pageVOResult = getPostVOPage(page, size, postResultPage);

        return Result.success(pageVOResult);
    }

    @Override
    public Result getUserPosts(long userId, int page, int size) {
        User user = userService.getById(userId);

        if (user == null){
            log.error("查询用户所有帖子:该用户不存在");
            throw new BusinessException("该用户不存在");
        }

        Page<Post> postPage = new Page<>(page,size);
        LambdaQueryWrapper<Post> postLambdaQueryWrapper =
                new LambdaQueryWrapper<Post>().eq(Post::getUserId, userId).orderByDesc(Post::getCreateTime);

        Page<Post> pageResult = postMapper.selectPage(postPage, postLambdaQueryWrapper);

    }
}
