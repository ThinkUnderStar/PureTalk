package thinkunderstar.puretalk.puretalkbackend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import thinkunderstar.puretalk.puretalkbackend.common.DoSendPost;
import thinkunderstar.puretalk.puretalkbackend.common.Result;
import thinkunderstar.puretalk.puretalkbackend.service.SysPostService;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/post")
public class PostController {
    private final SysPostService sysPostService;

    public PostController( SysPostService sysPostService) {
        this.sysPostService = sysPostService;
    }

    /**
     * 获取评论
     *
     * @param categoryId 板块
     * @param page 帖子页数
     * @param size 一次返回的帖子
     * @return 获取结果
     */
    @GetMapping
    public Result getPosts(@RequestParam(defaultValue = "0") long categoryId,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "20") int size ){
        return sysPostService.getPosts(categoryId,page,size);
    }

    /**
     * 发送一条帖子
     *
     * @param doSendPost 帖子
     * @return 发送结果
     */
    @PostMapping("/send")
    @SaCheckLogin
    @SaCheckPermission(value = "send:add" , orRole = {"admin","root"})
    public Result sendPost(@RequestBody DoSendPost doSendPost){
        return sysPostService.sendPost(doSendPost);
    }

    /**
     * 用户删除自己的帖子
     *
     * @param postId 帖子的id
     * @return 删除结果
     */
    @DeleteMapping("/user/{postId}/delete")
    @SaCheckLogin
    public Result deleteMyPost(@PathVariable long postId){
        return sysPostService.deleteMyPost(postId);
    }

    /**
     * 管理员删除帖子
     *
     * @param postId 帖子id
     * @return 删除结果
     */
    @DeleteMapping("/admin/{postId}/delete")
    @SaCheckLogin
    @SaCheckRole(value = {"admin","root"},mode = SaMode.OR)
    public Result deletePost(@PathVariable long postId){
        return sysPostService.deletePost(postId);
    }

    /**
     * 修改点赞数
     *
     * @param postId 帖子id
     * @return 修改结果
     */
    @PutMapping("/{postId}/like")
    @SaCheckLogin
    public Result updateLikeCount(@PathVariable long postId){
        return sysPostService.updateLikeCount(postId);
    }

    /**
     * 修改浏览量
     *
     * @param viewMap 浏览过的帖子
     * @return 修改结果
     */
    @PutMapping("/view")
    public Result updateViewCount(@RequestBody Map<Long,Integer> viewMap){
        return sysPostService.updateViewCount(viewMap);
    }

    /**
     * 搜索关键词
     *
     * @param str 关键词
     * @return 搜索结果
     */
    @GetMapping("/search")
    public Result searchPosts(@RequestParam String str,
                              @RequestParam(defaultValue = "0") long categoryId,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "20") int size) {
        return sysPostService.searchPosts(str,categoryId,page,size);
    }

    /**
     * 整点刷新前5000名的查询数据
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Async
    public void refreshSearchScore(){
        sysPostService.refreshSearchScore();
    }
}
