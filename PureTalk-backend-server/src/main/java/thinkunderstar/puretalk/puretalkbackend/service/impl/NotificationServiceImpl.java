package thinkunderstar.puretalk.puretalkbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import thinkunderstar.puretalk.puretalkbackend.entity.Notification;
import thinkunderstar.puretalk.puretalkbackend.mapper.NotificationMapper;
import thinkunderstar.puretalk.puretalkbackend.service.NotificationService;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

}
