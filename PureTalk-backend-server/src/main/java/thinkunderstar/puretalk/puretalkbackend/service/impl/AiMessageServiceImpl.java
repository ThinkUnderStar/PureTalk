package thinkunderstar.puretalk.puretalkbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import thinkunderstar.puretalk.puretalkbackend.entity.AiMessage;
import thinkunderstar.puretalk.puretalkbackend.mapper.AiMessageMapper;
import thinkunderstar.puretalk.puretalkbackend.service.AiMessageService;

@Service
public class AiMessageServiceImpl extends ServiceImpl<AiMessageMapper, AiMessage> implements AiMessageService {
}
