package thinkunderstar.puretalk.puretalkbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import thinkunderstar.puretalk.puretalkbackend.entity.Feedback;
import thinkunderstar.puretalk.puretalkbackend.mapper.FeedbackMapper;
import thinkunderstar.puretalk.puretalkbackend.service.FeedbackService;

@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {
}
