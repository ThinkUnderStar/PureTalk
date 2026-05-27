package thinkunderstar.puretalk.puretalkbackend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import thinkunderstar.puretalk.puretalkbackend.common.*;
import thinkunderstar.puretalk.puretalkbackend.entity.User;
import thinkunderstar.puretalk.puretalkbackend.exception.AuthException;
import thinkunderstar.puretalk.puretalkbackend.exception.BusinessException;
import thinkunderstar.puretalk.puretalkbackend.service.SysUserService;
import thinkunderstar.puretalk.puretalkbackend.service.UserService;
import thinkunderstar.puretalk.puretalkbackend.util.*;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final SysUserService sysUserService;

    public UserController(UserService userService, SysUserService sysUserService) {
        this.userService = userService;
        this.sysUserService = sysUserService;
    }

    /**
     * 处理用户登录请求，验证用户名和密码，并返回登录结果。
     * @param doLogin 包含手机号和密码的DoLogin对象
     * @return 返回Result对象，如果登录成功则返回成功状态码和消息
     * @throws AuthException 如果用户不存在或密码错误，则抛出AuthException异常
     */
    @PostMapping("/login")
    public Result doLogin(@RequestBody DoLogin doLogin){
        return sysUserService.doLogin(doLogin);
    }

    /**
     * 验证码登录-获取验证码
     * @param phone 用户手机号
     * @return 发送结果
     */
    @GetMapping("/login/code")
    public Result doLoginWithSendCode(@RequestParam String phone){
        return sysUserService.sendPhoneCode(phone);
    }

    /**
     * 验证码登录-审核验证码
     * @param phone 用户手机号+验证码+是否记住登录状态
     * @return 验证而结果
     */
    @PostMapping("/login/code")
    public Result doLoginWithValidateCode(@RequestBody ValidateCode phone){
        if (sysUserService.validatePhoneCode(phone)){
            StpUtil.login(userService.getOne(new QueryWrapper<User>().eq("phone",phone)).getId());
            return Result.success();
        }else{
            throw new BusinessException("验证码有误");
        }
    }

    /**
     * 用户上传自己的头像
     *
     * @param file 头像文件
     * @return 存储结果与存储的头像文件
     */
    @PostMapping("/avatar")
    @SaCheckLogin
    public Result uploadAvatar(@RequestParam("file") MultipartFile file){
        return sysUserService.uploadAvatar(file);
    }

    /**
     * 处理用户注册请求，验证手机号是否已存在，并根据情况保存新用户或更新已删除用户的信息。
     * @param doRegister 包含用户名、密码、手机号和邮箱的DoRegister对象
     * @return 返回Result对象，如果注册成功则返回成功状态码和消息，如果用户已存在则返回错误状态码和消息
     */
    @PostMapping("/register")
    public Result doRegister(@RequestBody DoRegister doRegister){
        return sysUserService.doRegister(doRegister);
    }

    /**
     * 申请手机号的验证码
     * @param phone 用户手机号
     * @return 发送结果
     */
    @GetMapping("/register/phone")
    public Result sendPhoneCode(@RequestParam String phone){
        return sysUserService.sendPhoneCode(phone);
    }

    /**
     * 验证手机验证码
     * @param phone 手机号与验证码
     * @return 验证结果
     */
    @PostMapping("/register/phone")
    public Result validatePhoneCode(@RequestBody ValidateCode phone){
        if (sysUserService.validatePhoneCode(phone)) {
            return Result.success();
        }else {
            throw new BusinessException("验证码有误");
        }
    }

    /**
     * 申请邮箱地址的验证码
     * @param email 用户邮箱地址
     * @return 发送结果
     */
    @GetMapping("/register/email")
    public Result sendEmailCode(@RequestParam String email){
        return sysUserService.sendEmailCode(email);
    }

    /**
     * 验证邮箱验证码
     * @param email 用户邮箱地址与邮箱验证码
     * @return 验证结果
     */
    @PostMapping("/register/email")
    public Result validateEmailCode(@RequestBody ValidateCode email){
        return sysUserService.validateEmailCode(email);
    }

    /**
     * 删除当前登录用户的账户信息。
     * 将用户标记为已删除，并执行登出操作。
     * @return 返回Result对象，包含操作结果的状态码和消息
     */
    @SaCheckLogin
    @DeleteMapping("/delete")
    public Result doDelete(){
        return sysUserService.doDelete();
    }

    /**
     * 处理用户登出请求，使当前登录的用户退出系统。
     * @return 返回Result对象，包含操作结果的状态码和消息。如果登出成功，则返回成功状态码和消息。
     */
    @SaCheckLogin
    @RequestMapping("/logout")
    public Result doLogout(){
        StpUtil.logout();
        return Result.success();
    }

    /**
     * 更新用户信息。
     *
     * @param doUpdate 包含需要更新的用户信息，如用户名、密码、手机号和邮箱等
     * @return 返回一个Result对象，表示操作是否成功
     */
    @SaCheckLogin
    @PutMapping("/update")
    public Result doUpdate(@RequestBody DoUpdate doUpdate){
        return sysUserService.doUpdate(doUpdate);
    }
}
