package thinkunderstar.puretalk.puretalkbackend.api;

import org.springframework.web.bind.annotation.*;
import thinkunderstar.puretalk.puretalkbackend.common.DoFeedback;
import thinkunderstar.puretalk.puretalkbackend.common.DoHandleFeedback;
import thinkunderstar.puretalk.puretalkbackend.common.Result;
import thinkunderstar.puretalk.puretalkbackend.service.SysFeedbackService;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackAPI {
    private final SysFeedbackService sysFeedbackService;

    public FeedbackAPI(SysFeedbackService sysFeedbackService) {
        this.sysFeedbackService = sysFeedbackService;
    }

    /**
     * 获取反馈集合
     *
     * @param page 页数
     * @param size 一次获取的数量
     * @return 获取结果
     */
    @GetMapping
    public Result getFeedbacks(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "20") int size){
        return sysFeedbackService.getFeedbacks(page,size);
    }

    /**
     * 提交反馈
     *
     * @param doFeedback 反馈体
     * @return 提交结果
     */
    @PostMapping("/send")
    public Result sendFeedback(@RequestBody DoFeedback doFeedback){
        return sysFeedbackService.sendFeedback(doFeedback);
    }

    /**
     * 管理员回应反馈
     *
     * @param doHandleFeedback 回应体
     * @return 回应结果
     */
    @PutMapping("/handle")
    public Result handleFeedback(@RequestBody DoHandleFeedback doHandleFeedback){
        return sysFeedbackService.handleFeedback(doHandleFeedback);
    }
}
