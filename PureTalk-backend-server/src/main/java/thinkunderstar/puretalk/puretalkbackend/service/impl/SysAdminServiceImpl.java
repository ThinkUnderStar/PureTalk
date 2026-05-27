package thinkunderstar.puretalk.puretalkbackend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import thinkunderstar.puretalk.puretalkbackend.common.BanUser;
import thinkunderstar.puretalk.puretalkbackend.common.DoLogin;
import thinkunderstar.puretalk.puretalkbackend.common.LoginReturnData;
import thinkunderstar.puretalk.puretalkbackend.common.Result;
import thinkunderstar.puretalk.puretalkbackend.entity.User;
import thinkunderstar.puretalk.puretalkbackend.exception.AuthException;
import thinkunderstar.puretalk.puretalkbackend.exception.BusinessException;
import thinkunderstar.puretalk.puretalkbackend.service.SysAdminService;
import thinkunderstar.puretalk.puretalkbackend.service.UserService;
import thinkunderstar.puretalk.puretalkbackend.util.DesensitizeUtils;
import thinkunderstar.puretalk.puretalkbackend.util.IpUtils;
import thinkunderstar.puretalk.puretalkbackend.util.RedisTokenBucketLimiter;
import thinkunderstar.puretalk.puretalkbackend.util.ValidateUtils;

import java.time.LocalDateTime;

@Service
public class SysAdminServiceImpl implements SysAdminService {
    private final UserService userService;
    private final RedisTokenBucketLimiter redisTokenBucketLimiter;

    public SysAdminServiceImpl(UserService userService, RedisTokenBucketLimiter redisTokenBucketLimiter) {
        this.userService = userService;
        this.redisTokenBucketLimiter = redisTokenBucketLimiter;
    }

    @Autowired
    private HttpServletRequest request;

    @Override
    public Result adminLogin(DoLogin doLogin) {

        //不计入限速
        if (doLogin.getPhone() == null || doLogin.getPhone().isEmpty()) {
            throw new BusinessException("手机号不能为空");
        }

        if (doLogin.getPassword() == null || doLogin.getPassword().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }

        if (!ValidateUtils.phoneValidate(doLogin.getPhone())){
            throw new BusinessException("手机号格式有误");
        }

        //给ip地址限速
        boolean success = redisTokenBucketLimiter.tryAcquireByIp(IpUtils.getClientIp(request),5,2);

        if (!success){
            throw new BusinessException("登陆过于频繁");
        }

        User user = userService.getOne(new QueryWrapper<User>().eq("phone", doLogin.getPhone()));

        if (user == null || user.getDeleted() == 1){
            throw new BusinessException("手机号或密码错误");
        } else if (user.getRole() == 1) {
            throw new BusinessException("此入口是管理员登录入口");
        }else if (!BCrypt.checkpw(doLogin.getPassword(),user.getPassword())) {
            throw new AuthException("手机号或密码错误");
        }

        StpUtil.login(user.getId(),doLogin.isRemember());

        return  Result.success(new LoginReturnData(
                user.getId()
                , user.getUsername()
                , DesensitizeUtils.desensitizePhone(user.getPhone())
                ,DesensitizeUtils.desensitizeEmail(user.getEmail())
                ,user.getAvatar()
                , user.getRole()
                , user.getStatus()
                , user.getCreateTime()
                , user.getBanReason()
                ,user.getBanOn()
                ,user.getBannedUntil()
                ,StpUtil.getTokenValue()));
    }

    @Override
    public Result doDelete() {
        User user = userService.getById(StpUtil.getLoginIdAsLong());
        user.setDeleted(1);
        userService.updateById(user);
        StpUtil.logout();
        return Result.success();
    }

    @Override
    public Result banUser(BanUser banUser) {
        //0代表永久禁言
        if (banUser.getBanTime()<0){
            throw new BusinessException("禁言时间异常");
        }

        User user = userService.getById(banUser.getUserId());

        if (user == null){
            throw new BusinessException("该用户不存在");
        }

        if (user.getStatus() == 0){
            throw new BusinessException("用户已被禁言");
        }

        user.setStatus(0);
        user.setBanOn(LocalDateTime.now());

        if (banUser.getBanTime() != 0) {
            user.setBannedUntil(LocalDateTime.now().plusSeconds(banUser.getBanTime()));
        }else {
            user.setBannedUntil(LocalDateTime.of(2999, 12, 31, 23, 59, 59));
        }

        user.setWhoBan(StpUtil.getLoginIdAsLong());
        user.setBanReason(banUser.getBanReason());

        userService.updateById(user);
        return Result.success();
    }

    @Override
    public Result unBanUser(long userId) {
        User user = userService.getById(userId);

        if (user.getStatus() == 1){
            throw new BusinessException("该用户未禁言");
        }

        user.setStatus(1);
        user.setBanReason(null);
        user.setWhoBan(0);
        user.setBanOn(null);
        user.setBannedUntil(null);

        userService.updateById(user);

        return Result.success();
    }
}
