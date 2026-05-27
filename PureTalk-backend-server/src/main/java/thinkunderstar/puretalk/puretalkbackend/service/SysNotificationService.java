package thinkunderstar.puretalk.puretalkbackend.service;

import thinkunderstar.puretalk.puretalkbackend.common.Result;

public interface SysNotificationService {
    /**
     * 用户获取通知
     *
     * @param userId 用户id
     * @param page 页数
     * @param size 一次性返回的数量
     * @return 获取结果
     */
    Result getNotifications(long userId, int page, int size);

    /**
     * 获取通知的具体信息
     *
     * @param notificationId 通知id
     * @return 获取结果
     */
    Result getNotificationTarget( long notificationId);
}
