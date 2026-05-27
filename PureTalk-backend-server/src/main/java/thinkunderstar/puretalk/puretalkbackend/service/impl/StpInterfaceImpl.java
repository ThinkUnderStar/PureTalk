package thinkunderstar.puretalk.puretalkbackend.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;
import thinkunderstar.puretalk.puretalkbackend.entity.User;
import thinkunderstar.puretalk.puretalkbackend.exception.BusinessException;
import thinkunderstar.puretalk.puretalkbackend.service.UserService;
import java.util.ArrayList;
import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {
    private final UserService userService;


    public StpInterfaceImpl(UserService userService ) {
        this.userService = userService;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());  // 安全转换
        User user = userService.getById(userId);
        List<String> permissionList = new ArrayList<>();
        if (user != null && user.getStatus() == 1) {
            permissionList.add("send:add");
        }
        return permissionList;
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());  // 安全转换
        User user = userService.getById(userId);
        List<String> roleList = new ArrayList<>();
        if (user == null) {
            return roleList;
        }
        if (user.getRole() == 1) {
            roleList.add("user");
        } else if (user.getRole() == 2) {
            roleList.add("admin");
        } else if (user.getRole() == 3) {
            roleList.add("root");
        } else {
            throw new BusinessException("非法身份");
        }
        return roleList;
    }
}
