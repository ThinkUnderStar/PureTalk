package thinkunderstar.puretalk.puretalkbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import thinkunderstar.puretalk.puretalkbackend.entity.AiSession;
import thinkunderstar.puretalk.puretalkbackend.mapper.AiSessionMapper;
import thinkunderstar.puretalk.puretalkbackend.service.AiSessionService;

@Service
public class AiSessionServiceImpl extends ServiceImpl<AiSessionMapper, AiSession> implements AiSessionService {
}
