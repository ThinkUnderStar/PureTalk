package thinkunderstar.puretalk.puretalkbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import thinkunderstar.puretalk.puretalkbackend.entity.Post;
import thinkunderstar.puretalk.puretalkbackend.mapper.PostMapper;
import thinkunderstar.puretalk.puretalkbackend.service.PostService;

/**
 * 帖子服务实现类，继承自ServiceImpl并实现了PostService接口。
 * 该类提供了对帖子数据进行增删改查等基本操作的具体实现。
 * 使用MyBatis-Plus框架提供的通用CRUD功能，并通过PostMapper与数据库交互。
 */
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {
}
