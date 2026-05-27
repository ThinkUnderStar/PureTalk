package thinkunderstar.puretalk.puretalkbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import thinkunderstar.puretalk.puretalkbackend.entity.Comment;

/**
 * 评论服务接口，继承自IService，提供对Comment实体的CRUD操作。
 * 该接口定义了对评论数据进行增删改查等基本操作的方法签名。
 * 实现类需要提供具体的业务逻辑实现。
 */
public interface CommentService extends IService<Comment> {
}
