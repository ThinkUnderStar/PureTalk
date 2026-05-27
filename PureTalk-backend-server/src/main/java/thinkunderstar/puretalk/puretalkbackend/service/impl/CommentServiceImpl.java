package thinkunderstar.puretalk.puretalkbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import thinkunderstar.puretalk.puretalkbackend.entity.Comment;
import thinkunderstar.puretalk.puretalkbackend.mapper.CommentMapper;
import thinkunderstar.puretalk.puretalkbackend.service.CommentService;

/**
 * 评论服务实现类，继承自ServiceImpl并实现了CommentService接口。
 * 该类提供了对评论数据进行增删改查等基本操作的具体实现。
 * 使用MyBatis-Plus框架提供的通用CRUD功能，并通过CommentMapper与数据库交互。
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
}
