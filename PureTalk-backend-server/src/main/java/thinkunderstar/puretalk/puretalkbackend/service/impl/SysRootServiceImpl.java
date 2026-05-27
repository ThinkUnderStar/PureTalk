package thinkunderstar.puretalk.puretalkbackend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import thinkunderstar.puretalk.puretalkbackend.common.DeleteData;
import thinkunderstar.puretalk.puretalkbackend.common.DoRegister;
import thinkunderstar.puretalk.puretalkbackend.common.Result;
import thinkunderstar.puretalk.puretalkbackend.entity.User;
import thinkunderstar.puretalk.puretalkbackend.exception.AuthException;
import thinkunderstar.puretalk.puretalkbackend.exception.BusinessException;
import thinkunderstar.puretalk.puretalkbackend.service.SysRootService;
import thinkunderstar.puretalk.puretalkbackend.service.UserService;
import thinkunderstar.puretalk.puretalkbackend.util.DesensitizeUtils;
import thinkunderstar.puretalk.puretalkbackend.util.ValidateUtils;

import java.time.LocalDateTime;

@Service
public class SysRootServiceImpl implements SysRootService {
    private final UserService userService;

    public SysRootServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Result doAdminRegister(DoRegister doRegister) {
        User user = userService.getOne(new QueryWrapper<User>().eq("phone", doRegister.getPhone()));
        //判断格式是否真确
        if (ValidateUtils.usernameValidate(doRegister.getUsername())
                && ValidateUtils.passwordValidate(doRegister.getPassword())) {
            //认证是否为有效手机号与邮箱
            if (doRegister.isRightPhone() && doRegister.isRightEmail()) {
                //判断用户是否已经存在
                if (user != null) {
                    if (user.getDeleted() == 0) {
                        throw new AuthException("用户已存在");
                    } else {
                        userService.remove(new QueryWrapper<User>().eq("phone", doRegister.getPhone()));

                        userService.save(new User(doRegister.getUsername()
                                , BCrypt.hashpw(doRegister.getPassword(), BCrypt.gensalt(12))
                                , doRegister.getPhone()
                                , doRegister.getEmail()));
                        return Result.success();
                    }
                } else {
                    userService.save(new User(doRegister.getUsername()
                            , BCrypt.hashpw(doRegister.getPassword(), BCrypt.gensalt(12))
                            , doRegister.getPhone()
                            , doRegister.getEmail()
                            ,2));
                    return Result.success();
                }
            } else {
                throw new AuthException("手机或邮箱验证码错误");
            }
        }else {
            throw new AuthException("用户名或密码格式错误");
        }
    }

    @Override
    public Result getDeleteAdminData(String phone) {
        String role;

        if (!ValidateUtils.phoneValidate(phone)) {
            throw new BusinessException("手机号格式有误");
        }

        User user = userService.getOne(new QueryWrapper<User>().eq("phone", phone));

        if (user == null) {
            throw new BusinessException("该用户不存在");
        } else {
            if (user.getRole() == 1) {
                role = "用户";
            } else if (user.getRole() == 2) {
                role = "管理员";
            } else if (user.getRole() == 3) {
                throw new BusinessException("root账户不可删除!");
            } else {
                throw new BusinessException("非法账户身份信息");
            }
        }

        return Result.success(new DeleteData(user.getUsername()
                , DesensitizeUtils.desensitizePhone(user.getPhone())
                , DesensitizeUtils.desensitizeEmail(user.getEmail())
                , role
                , user.getCreateTime()));
    }

    @Override
    public Result deleteAdmin(String phone) {
        if (!ValidateUtils.phoneValidate(phone)) {
            throw new BusinessException("手机号格式有误");
        }

        User user = userService.getOne(new QueryWrapper<User>().eq("phone", phone));

        if (user == null) {
            throw new BusinessException("该用户不存在");
        } else if (user.getRole() == 3) {
            throw new BusinessException("root账户不可删除!");
        }

        user.setDeleted(1);
        user.setDeleteTime(LocalDateTime.now());
        user.setWhoDelete(StpUtil.getLoginIdAsLong());
        userService.updateById(user);

        return Result.success();
    }
}
