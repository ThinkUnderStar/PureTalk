package thinkunderstar.puretalk.puretalkbackend.service;

import thinkunderstar.puretalk.puretalkbackend.common.DoFeedback;
import thinkunderstar.puretalk.puretalkbackend.common.DoHandleFeedback;
import thinkunderstar.puretalk.puretalkbackend.common.Result;

public interface SysFeedbackService {
    /**
     * 提交反馈
     *
     * @param doFeedback 反馈体
     * @return 提交结果
     */
    Result sendFeedback(DoFeedback doFeedback);

    /**
     * 获取反馈集合
     *
     * @param page 页数
     * @param size 一次获取的数量
     * @return 获取结果
     */
    Result getFeedbacks(int page, int size);

    /**
     * 管理员回应反馈
     *
     * @param doHandleFeedback 回应体
     * @return 回应结果
     */
    Result handleFeedback( DoHandleFeedback doHandleFeedback);

    /**
     * 一年清理一次已处理的反馈
     */
    void clearHandledFeedback();
}
