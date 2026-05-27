package thinkunderstar.puretalk.puretalkbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import thinkunderstar.puretalk.puretalkbackend.common.Result;
import thinkunderstar.puretalk.puretalkbackend.entity.Feedback;
import thinkunderstar.puretalk.puretalkbackend.entity.Notification;
import thinkunderstar.puretalk.puretalkbackend.entity.Report;
import thinkunderstar.puretalk.puretalkbackend.exception.BusinessException;
import thinkunderstar.puretalk.puretalkbackend.mapper.NotificationMapper;
import thinkunderstar.puretalk.puretalkbackend.service.FeedbackService;
import thinkunderstar.puretalk.puretalkbackend.service.NotificationService;
import thinkunderstar.puretalk.puretalkbackend.service.ReportService;
import thinkunderstar.puretalk.puretalkbackend.service.SysNotificationService;

@Service
public class SysNotificationServiceImpl implements SysNotificationService {
    private final NotificationMapper notificationMapper;
    private final NotificationService notificationService;
    private final ReportService reportService;
    private final FeedbackService feedbackService;

    public SysNotificationServiceImpl(NotificationMapper notificationMapper, NotificationService notificationService, ReportService reportService, FeedbackService feedbackService) {
        this.notificationMapper = notificationMapper;
        this.notificationService = notificationService;
        this.reportService = reportService;
        this.feedbackService = feedbackService;
    }

    @Override
    public Result getNotifications(long userId, int page, int size) {
        Page<Notification> notificationPage = new Page<>(page,size);

        Page<Notification> pageResult = notificationMapper.selectPage(notificationPage,
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .orderByDesc(Notification::getCreateTime));

        return Result.success(pageResult);
    }

    @Override
    public Result getNotificationTarget(long notificationId) {
        Notification notification = notificationService.getById(notificationId);

        if (notification == null){
            throw new BusinessException("该通知不存在");
        }

        Object o;
        if (notification.getType() == 1){
            o = reportService.getById(notification.getSourceId());
        } else if (notification.getType() == 2) {
            o = feedbackService.getById(notification.getSourceId());
        }
//        else if (notification.getType() == 3) {
//            //系统通知预留位
//        }
        else{
            throw new BusinessException("通知业务类型异常");
        }

        notification.setIsRead(1);
        notificationService.updateById(notification);

        return Result.success(o);
    }
}
