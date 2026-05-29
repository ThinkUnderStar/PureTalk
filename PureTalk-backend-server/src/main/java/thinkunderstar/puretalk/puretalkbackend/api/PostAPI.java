package thinkunderstar.puretalk.puretalkbackend.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import thinkunderstar.puretalk.puretalkbackend.common.DoSendPost;
import thinkunderstar.puretalk.puretalkbackend.common.Result;
import thinkunderstar.puretalk.puretalkbackend.service.SysPostService;

@Slf4j
@RestController
@RequestMapping("/api/post")
public class PostAPI {
    private final SysPostService sysPostService;

    public PostAPI(SysPostService sysPostService) {
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
    public Result deletePost(@PathVariable long postId){
        return sysPostService.deletePost(postId);
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
     * 获得传入用户的所有帖子
     *
     * @param userId 传入用户的ID
     * @param page 帖子页数
     * @param size 一次返回的帖子
     * @return 获取结果
     */
    @GetMapping("/{userId}/posts")
    public Result getUserPosts(@PathVariable long userId,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "20") int size) {
        return sysPostService.getUserPosts(userId, page, size);
    }
}
