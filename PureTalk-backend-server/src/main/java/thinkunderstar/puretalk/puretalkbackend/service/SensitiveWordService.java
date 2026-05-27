package thinkunderstar.puretalk.puretalkbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import thinkunderstar.puretalk.puretalkbackend.entity.SensitiveWord;

/**
 * 敏感词服务接口，继承自IService，提供对SensitiveWord实体的CRUD操作。
 * 该接口定义了对敏感词数据进行增删改查等基本操作的方法签名。
 * 实现类需要提供具体的业务逻辑实现。
 */
public interface SensitiveWordService extends IService<SensitiveWord> {
}
