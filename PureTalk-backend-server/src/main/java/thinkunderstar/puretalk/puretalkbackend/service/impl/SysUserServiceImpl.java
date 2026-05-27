package thinkunderstar.puretalk.puretalkbackend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import thinkunderstar.puretalk.puretalkbackend.common.*;
import thinkunderstar.puretalk.puretalkbackend.entity.User;
import thinkunderstar.puretalk.puretalkbackend.exception.AlreadyExistsException;
import thinkunderstar.puretalk.puretalkbackend.exception.AuthException;
import thinkunderstar.puretalk.puretalkbackend.exception.BusinessException;
import thinkunderstar.puretalk.puretalkbackend.service.SysAdminService;
import thinkunderstar.puretalk.puretalkbackend.service.SysUserService;
import thinkunderstar.puretalk.puretalkbackend.service.UserService;
import thinkunderstar.puretalk.puretalkbackend.util.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 系统用户服务实现类，提供发送和验证邮箱及手机验证码的具体实现。
 * 通过与Redis的交互，确保验证码的安全性和时效性。
 */
@Slf4j
@Service
public class SysUserServiceImpl implements SysUserService {
    private final UserService userService;
    private final RedisUtils redisUtils;
    private final SysAdminService sysAdminService;
    private final RedisTokenBucketLimiter redisTokenBucketLimiter;

    public SysUserServiceImpl(UserService userService, RedisUtils redisUtils, SysAdminService sysAdminService, RedisTokenBucketLimiter redisTokenBucketLimiter) {
        this.userService = userService;
        this.redisUtils = redisUtils;
        this.sysAdminService = sysAdminService;
        this.redisTokenBucketLimiter = redisTokenBucketLimiter;
    }

    @Autowired
    private HttpServletRequest request;  // Spring 自动注入当前请求的代理对象

    /**
     * 发送邮箱验证码
     *
     * @param email 用户的邮箱地址
     * @return 操作结果，成功返回Result.success()，失败抛出异常
     * @throws AuthException 如果邮箱地址格式不正确
     * @throws BusinessException 如果验证码发送过于频繁或发送过程中出现错误
     */
    @Override
    public Result sendEmailCode(String email) {
        if (ValidateUtils.emailValidate(email)){
            boolean success = redisTokenBucketLimiter.tryAcquireByIp(IpUtils.getClientIp(request), 10, 1);

            if (!success){
                throw new BusinessException("发送验证码过于频繁");
            }

            String code = CodeUtils.getSixDigitCode();
            String codeKey = "MAIL:LOCK:" + email;
            String sendLock = "MAIL:SEND:LOCK:"+email;

            //间隔60秒，防止频繁申请验证码
            if (!redisUtils.hasKey(sendLock)){
                redisUtils.set(sendLock,"1",60, TimeUnit.SECONDS);
                redisUtils.set(codeKey,code,5,TimeUnit.MINUTES);

                //发送邮箱验证码
                try {
                    MailUtils.sendCode(email, code);
                } catch (Exception e) {
                    throw new BusinessException("验证码发送失败");
                }

                return Result.success();
            }else {
                throw new BusinessException("验证码发送过于频繁，请60秒后重试");
            }
        }else {
            throw new AuthException("邮箱地址格式不对");
        }
    }

    /**
     * Validates the email code provided by the user.
     *
     * @param email the ValidateCode object containing the email and the email code to be validated
     * @return a Result object indicating success if the validation is successful, otherwise throws an AuthException
     */
    @Override
    public Result validateEmailCode(ValidateCode email) {
        String codeKey = "MAIL:LOCK:" + email.getEmail();
        String rightCode = redisUtils.get(codeKey);

        if (rightCode == null){
            throw new AuthException("验证码错误");
        }

        if (rightCode.equals(email.getEmailCode())){
            redisUtils.delete(codeKey);
            return Result.success();
        }else {
            throw new AuthException("验证码错误");
        }
    }

    /**
     * 发送手机验证码
     *
     * @param phone 手机号码
     * @return Result 操作结果，成功返回success，失败抛出异常
     */
    @Override
    public Result sendPhoneCode(String phone) {
        if (ValidateUtils.phoneValidate(phone)){
            boolean success = redisTokenBucketLimiter.tryAcquireByIp(IpUtils.getClientIp(request), 10, 1);

            if (!success){
                throw new BusinessException("发送验证码过于频繁");
            }

            String code = CodeUtils.getSixDigitCode();
            String codeKey = "SMS:LOCK:" + phone;
            String sendLock = "SMS:SEND:LOCK:"+phone;

            //间隔60秒，防止频繁申请验证码
            if (!redisUtils.hasKey(sendLock)){
                redisUtils.set(sendLock,"1",60, TimeUnit.SECONDS);
                redisUtils.set(codeKey,code,5,TimeUnit.MINUTES);

                //发送手机验证码
                try {
                    SmsUtils.sendCode(phone, code);
                } catch (Exception e) {
                    throw new BusinessException("验证码发送失败");
                }

                return Result.success();
            }else {
                throw new BusinessException("验证码发送过于频繁，请60秒后重试");
            }
        }else {
            throw new AuthException("手机号格式不对");
        }
    }

    /**
     * Validates the phone code against the stored code in the system.
     *
     * @param phone the ValidateCode object containing the phone number and the code to be validated
     * @return true if the provided phone code matches the stored code, otherwise false
     */
    @Override
    public boolean validatePhoneCode(ValidateCode phone) {
        String codeKey = "SMS:LOCK:" + phone.getPhone();
        String rightCode = redisUtils.get(codeKey);

        if (rightCode == null){
            return false;
        }

        if (rightCode.equals(phone.getPhoneCode())){
            redisUtils.delete(codeKey);
            return true;
        }else {
            return false;
        }
    }

    /**
     * Authenticates a user and logs them in.
     *
     * @param doLogin the object containing the phone number and password for the login attempt
     * @return a Result object indicating the success of the operation or an error message
     */
    @Override
    public Result doLogin(DoLogin doLogin) {
        //不计入限速
        if (doLogin.getPhone() == null || doLogin.getPhone().isEmpty()) {
            throw new BusinessException("手机号不能为空");
        }
        if (doLogin.getPassword() == null || doLogin.getPassword().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }

        //给ip地址限速
        boolean success = redisTokenBucketLimiter.tryAcquireByIp(IpUtils.getClientIp(request),5,2);

        if (!success){
            throw new BusinessException("登陆过于频繁");
        }

        User user = userService.getOne(new QueryWrapper<User>().eq("phone", doLogin.getPhone()));

        if (user == null || user.getDeleted() == 1)
        {
            throw new AuthException("手机号或密码错误");
        } else if (user.getRole() != 1 ) {
            throw new AuthException("此入口使用户登录入口");
        } else if (!BCrypt.checkpw(doLogin.getPassword(),user.getPassword())) {
            throw new AuthException("手机号或密码错误");
        }else {

            if (user.getStatus() == 0){
                if (LocalDateTime.now().isAfter(user.getBannedUntil())){
                    sysAdminService.unBanUser(user.getId());
                }
            }

            StpUtil.login(user.getId(),doLogin.isRemember());

            return Result.success(new LoginReturnData(user.getId()
                    , user.getUsername()
                    ,DesensitizeUtils.desensitizePhone(user.getPhone())
                    ,DesensitizeUtils.desensitizeEmail(user.getEmail())
                    ,user.getAvatar()
                    , user.getRole()
                    , user.getStatus()
                    , user.getCreateTime()
                    ,user.getBanReason()
                    ,user.getBanOn()
                    ,user.getBannedUntil()
                    ,StpUtil.getTokenValue()));
        }
    }

    @Override
    public Result uploadAvatar(MultipartFile file){
        //判断文件非空
         if (file.isEmpty()){
            throw new BusinessException("头像文件为空");
         }

        //判断文件格式是否正确
        String originalFilename = file.getOriginalFilename();
         if (originalFilename == null || originalFilename.isEmpty()){
             throw new BusinessException("只接受jpg,jpeg,png,gif文件格式");
         }
        String suffix  = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!List.of(".jpg", ".jpeg", ".png", ".gif").contains(suffix)){
            throw new BusinessException("只接受jpg,jpeg,png,gif文件格式");
        }

        User user = userService.getById(StpUtil.getLoginIdAsLong());

        //判断是否有旧头像文件，如果求则删除，没有则跳过
        if (!(user.getAvatar() == null || user.getAvatar().isEmpty())) {
            try {
                Files.deleteIfExists(Path.of("."+user.getAvatar()));
            } catch (IOException e) {
                log.warn("删除旧头像失败，文件可能被占用：{}", user.getAvatar(), e);
            }
        }

        //保存新的头像文件
        String filePath = "/uploads/" + user.getId() + "-" +System.currentTimeMillis() + suffix;
        File newFile = new File("." + filePath);

        if (!newFile.getParentFile().exists()){
            newFile.getParentFile().mkdirs();
        }

        try {
            file.transferTo(Path.of("." + filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        user.setAvatar(filePath);
        userService.updateById(user);

        return Result.success(filePath);
    }

    /**
     * Registers a new user with the provided information.
     * Validates the username, password, phone, and email. If the user already exists but is marked as deleted,
     * it will be re-registered. Throws an AuthException if any validation fails or if the user already exists and is not deleted.
     *
     * @param doRegister an object containing the registration information including username, password, phone, and email
     * @return a Result object indicating the success of the operation
     */
    @Override
    public Result doRegister(DoRegister doRegister) {
        User user = userService.getOne(new QueryWrapper<User>().eq("phone", doRegister.getPhone()));
        //判断格式是否真确
        if (ValidateUtils.usernameValidate(doRegister.getUsername())
                && ValidateUtils.passwordValidate(doRegister.getPassword())) {
            //认证是否为有效手机号与邮箱
            if (doRegister.isRightPhone() && doRegister.isRightEmail()) {
                //ip限流
                boolean success = redisTokenBucketLimiter.tryAcquireByIp(IpUtils.getClientIp(request), 3, 1);

                if (!success){
                    throw new BusinessException("注册过于频繁");
                }

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
                            , doRegister.getEmail()));
                    return Result.success();
                }
            } else {
                throw new AuthException("手机或邮箱验证码错误");
            }
        }else {
            throw new AuthException("用户名或密码格式错误");
        }
    }

    /**
     * Deletes the currently logged-in user and logs them out.
     * The user is marked as deleted in the system, and their session is terminated.
     *
     * @return A Result object indicating the success of the operation.
     */
    @Override
    public Result doDelete() {
        User user = userService.getById(StpUtil.getLoginIdAsLong());
        user.setDeleted(1);
        user.setDeleteTime(LocalDateTime.now());
        user.setWhoDelete(StpUtil.getLoginIdAsLong());
        userService.updateById(user);
        StpUtil.logout();
        return Result.success();
    }

    /**
     * 更新用户信息。
     * 该方法允许更新用户的用户名、密码、手机号和邮箱地址。在更新过程中，会进行相应的格式验证以及唯一性检查（针对手机号和邮箱）。
     * 如果提供的数据格式不正确或违反了业务规则（如手机号/邮箱已被其他未删除账户使用），则抛出异常。
     *
     * @param doUpdate 包含待更新用户信息的对象
     * @return 操作结果，成功时返回成功信息
     */
    @Override
    public Result doUpdate(DoUpdate doUpdate) {
        long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);

        //改用户名
        if (doUpdate.getUsername()!=null){
            if (!ValidateUtils.usernameValidate(doUpdate.getUsername())){
                throw new BusinessException("用户名格式有误");
            }
        }

        //改用户密码
        if (doUpdate.getPassword()!=null) {
            if (!ValidateUtils.passwordValidate(doUpdate.getPassword())){
                throw new BusinessException("用户密码格式有误");
            }

            //加密密码
            doUpdate.setPassword(BCrypt.hashpw(doUpdate.getPassword(), BCrypt.gensalt(12)));
        }

        //改用户手机号
        if (doUpdate.getPhone()!=null) {
            if (!ValidateUtils.phoneValidate(doUpdate.getPhone())){
                throw new BusinessException("手机号格式有误");
            }

            User duplicatePhone = userService.getOne(new QueryWrapper<User>().eq("phone",doUpdate.getPhone()));

            if (duplicatePhone != null && duplicatePhone.getDeleted() == 0){
                throw new AlreadyExistsException("该手机号已被占用");
            }

            if (!doUpdate.isRight()){
                throw new BusinessException("验证码验证未通过");
            }
        }

        //改用户邮箱地址
        if (doUpdate.getEmail()!=null) {
            if (!ValidateUtils.emailValidate(doUpdate.getEmail())){
                throw new BusinessException("邮箱地址格式有误");
            }

            User duplicateEmail = userService.getOne(new QueryWrapper<User>().eq("email",doUpdate.getEmail()));

            if (duplicateEmail != null && duplicateEmail.getDeleted() == 0){
                throw new AlreadyExistsException("该邮箱地址已被占用");
            }

            if (!doUpdate.isRight()){
                throw new BusinessException("验证码验证未通过");
            }
        }

        BeanUtil.copyProperties(doUpdate, user, CopyOptions.create().setIgnoreNullValue(true));
        userService.updateById(user);

        return Result.success();
    }
}
