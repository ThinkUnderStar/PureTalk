package thinkunderstar.puretalk.puretalkbackend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import org.springframework.web.bind.annotation.*;
import thinkunderstar.puretalk.puretalkbackend.common.DoRegister;
import thinkunderstar.puretalk.puretalkbackend.common.Result;
import thinkunderstar.puretalk.puretalkbackend.common.ValidateCode;
import thinkunderstar.puretalk.puretalkbackend.exception.BusinessException;
import thinkunderstar.puretalk.puretalkbackend.service.SysRootService;
import thinkunderstar.puretalk.puretalkbackend.service.SysUserService;

@RestController
@RequestMapping("/root")
public class RootController {
    private final SysRootService sysRootService;
    private final SysUserService sysUserService;

    public RootController(SysRootService sysRootService, SysUserService sysUserService) {
        this.sysRootService = sysRootService;
        this.sysUserService = sysUserService;
    }

    /**
     * root创建管理员业务
     * @param doRegister 输入的管理员的基本信息
     * @return 创建结果
     */
    @SaCheckRole("root")
    @PostMapping("/register")
    public Result doAdminRegister(@RequestBody DoRegister doRegister){
        return sysRootService.doAdminRegister(doRegister);
    }

    /**
     * 申请手机号的验证码
     * @param phone 管理员手机号
     * @return 发送结果
     */
    @SaCheckRole("root")
    @GetMapping("/register/phone")
    public Result sendPhoneCode(@RequestParam String phone){
        return sysUserService.sendPhoneCode(phone);
    }

    /**
     * 验证手机验证码
     * @param phone 手机号与验证码
     * @return 验证结果
     */
    @SaCheckRole("root")
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
     * @param email 管理员邮箱地址
     * @return 发送结果
     */
    @SaCheckRole("root")
    @GetMapping("/register/email")
    public Result sendEmailCode(@RequestParam String email){
        return sysUserService.sendEmailCode(email);
    }

    /**
     * 验证邮箱验证码
     * @param email 管理员邮箱地址与邮箱验证码
     * @return 验证结果
     */
    @SaCheckRole("root")
    @PostMapping("/register/email")
    public Result validateEmailCode(@RequestBody ValidateCode email){
        return sysUserService.validateEmailCode(email);
    }

    /**
     * 获取待删除管理员的数据
     *
     * @param phone 用户手机号
     * @return 包含用户信息的结果对象，其中用户敏感信息已脱敏处理
     * @throws BusinessException 当手机号格式有误、用户不存在或尝试删除root账户时抛出
     */
    @SaCheckLogin
    @SaCheckRole("root")
    @GetMapping("/delete")
    public Result getDeleteAdminData(@RequestParam String phone) {
        return sysRootService.getDeleteAdminData(phone);
    }

    /**
     * 删除指定手机号对应的管理员账户。
     *
     * @param phone 管理员的手机号
     * @return 操作结果，成功时返回成功信息
     * @throws BusinessException 当手机号格式有误、用户不存在或尝试删除root账户时抛出异常
     */
    @SaCheckLogin
    @SaCheckRole("root")
    @DeleteMapping("/delete")
    public Result deleteAdmin(@RequestParam String phone) {
        return sysRootService.deleteAdmin(phone);
    }

}
