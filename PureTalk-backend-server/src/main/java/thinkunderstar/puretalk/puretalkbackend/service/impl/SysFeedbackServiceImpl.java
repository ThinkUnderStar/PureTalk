package thinkunderstar.puretalk.puretalkbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import thinkunderstar.puretalk.puretalkbackend.common.DoFeedback;
import thinkunderstar.puretalk.puretalkbackend.common.DoHandleFeedback;
import thinkunderstar.puretalk.puretalkbackend.common.Result;
import thinkunderstar.puretalk.puretalkbackend.entity.Feedback;
import thinkunderstar.puretalk.puretalkbackend.entity.Notification;
import thinkunderstar.puretalk.puretalkbackend.exception.BusinessException;
import thinkunderstar.puretalk.puretalkbackend.mapper.FeedbackMapper;
import thinkunderstar.puretalk.puretalkbackend.service.FeedbackService;
import thinkunderstar.puretalk.puretalkbackend.service.NotificationService;
import thinkunderstar.puretalk.puretalkbackend.service.SysFeedbackService;
import thinkunderstar.puretalk.puretalkbackend.util.RedisTokenBucketLimiter;

@Slf4j
@Service
public class SysFeedbackServiceImpl implements SysFeedbackService {
    private final FeedbackService feedbackService;
    private final FeedbackMapper feedbackMapper;
    private final NotificationService notificationService;
    private final RedisTokenBucketLimiter redisTokenBucketLimiter;

    public SysFeedbackServiceImpl(FeedbackService feedbackService, FeedbackMapper feedbackMapper, NotificationService notificationService, RedisTokenBucketLimiter redisTokenBucketLimiter) {
        this.feedbackService = feedbackService;
        this.feedbackMapper = feedbackMapper;
        this.notificationService = notificationService;
        this.redisTokenBucketLimiter = redisTokenBucketLimiter;
    }

    @Override
    public Result sendFeedback(DoFeedback doFeedback) {
        if (doFeedback.getUserId() == null) {
            throw new BusinessException("用户信息不存在");
        }

        if (doFeedback.getTitle() == null
                || doFeedback.getContent() == null
                || doFeedback.getTitle().isEmpty()
                || doFeedback.getContent().isEmpty()){

            throw new BusinessException("标题或反馈内容不能为空");
        }

        //用户限流
        boolean success = redisTokenBucketLimiter.tryAcquireByUser(String.valueOf(doFeedback.getUserId()), 3, 1);

        if (!success){
            throw new BusinessException("反馈过于频繁");
        }

        feedbackService.save(new Feedback(doFeedback.getUserId(), doFeedback.getTitle(), doFeedback.getContent(), doFeedback.getContact()));

        return Result.success();
    }

    @Override
    public Result getFeedbacks(int page, int size) {
        Page<Feedback> feedbackPage = new Page<>(page,size);

        Page<Feedback> pageResult = feedbackMapper
                .selectPage(feedbackPage
                        , new LambdaUpdateWrapper<Feedback>()
                                .eq(Feedback::getStatus,0)
                                .orderByAsc(Feedback::getCreateTime));

        return Result.success(pageResult);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result handleFeedback(DoHandleFeedback doHandleFeedback) {
        Feedback feedback = feedbackService.getById(doHandleFeedback.getFeedbackId());

        if (feedback == null){
            throw new BusinessException("该反馈不存在");
        }

        if (feedback.getStatus() == 1){
            throw new BusinessException("该反馈已被回应");
        }

        feedback.setStatus(1);
        feedback.setHandlerId(doHandleFeedback.getHandlerId());
        feedback.setReply(doHandleFeedback.getReply());

        feedbackService.updateById(feedback);

        //添加到用户通知中
        notificationService.save(new Notification(feedback.getUserId(), 2, feedback.getId()));

        return Result.success();
    }

    @Override
    public void clearHandledFeedback() {
        log.info("开始清除上一年已回应的反馈");
        feedbackService.remove(new LambdaUpdateWrapper<Feedback>()
                .eq(Feedback::getStatus,1));
        log.info("清理完成");
    }
}
