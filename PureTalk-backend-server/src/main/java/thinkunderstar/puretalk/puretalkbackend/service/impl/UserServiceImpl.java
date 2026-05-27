package thinkunderstar.puretalk.puretalkbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import thinkunderstar.puretalk.puretalkbackend.entity.User;
import thinkunderstar.puretalk.puretalkbackend.mapper.UserMapper;
import thinkunderstar.puretalk.puretalkbackend.service.UserService;

/**
 * 用户服务实现类，继承自ServiceImpl并实现了UserService接口。
 * 该类提供了对用户数据进行增删改查等基本操作的具体实现。
 * 使用MyBatis-Plus框架提供的通用CRUD功能，并通过UserMapper与数据库交互。
 *
 * @see UserService
 * @see UserMapper
 * @see User
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
