package thinkunderstar.puretalk.puretalkbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import thinkunderstar.puretalk.puretalkbackend.entity.SensitiveWord;
import thinkunderstar.puretalk.puretalkbackend.mapper.SensitiveWordMapper;
import thinkunderstar.puretalk.puretalkbackend.service.SensitiveWordService;

/**
 * 敏感词服务实现类，继承自ServiceImpl并实现了SensitiveWordService接口。
 * 该类提供了对敏感词数据进行增删改查等基本操作的具体实现。
 * 使用MyBatis-Plus框架提供的通用CRUD功能，并通过SensitiveWordMapper与数据库交互。
 */
@Service
public class SensitiveWordServiceImpl extends ServiceImpl<SensitiveWordMapper, SensitiveWord> implements SensitiveWordService {
}
