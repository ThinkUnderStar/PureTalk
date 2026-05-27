package thinkunderstar.puretalk.puretalkbackend.controller;

import org.springframework.web.bind.annotation.*;
import thinkunderstar.puretalk.puretalkbackend.common.Result;
import thinkunderstar.puretalk.puretalkbackend.service.SysNotificationService;

@RestController
@RequestMapping("/notification")
public class NotificationController {
    private final SysNotificationService sysNotificationService;

    public NotificationController(SysNotificationService sysNotificationService) {
        this.sysNotificationService = sysNotificationService;
    }

    /**
     * 用户获取通知
     *
     * @param userId 用户id
     * @param page 页数
     * @param size 一次性返回的数量
     * @return 获取结果
     */
    @GetMapping
    public Result getNotifications(@RequestParam long userId,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "20") int size){
        return sysNotificationService.getNotifications(userId,page,size);
    }

    /**
     * 获取通知的具体信息
     *
     * @param notificationId 通知id
     * @return 获取结果
     */
    @GetMapping("/{notificationId}/target")
    public Result getNotificationTarget(@PathVariable long notificationId){
        return sysNotificationService.getNotificationTarget(notificationId);
    }
}
