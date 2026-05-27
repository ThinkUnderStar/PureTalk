package thinkunderstar.puretalk.puretalkbackend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.web.bind.annotation.*;
import thinkunderstar.puretalk.puretalkbackend.common.*;
import thinkunderstar.puretalk.puretalkbackend.exception.AuthException;
import thinkunderstar.puretalk.puretalkbackend.exception.BusinessException;
import thinkunderstar.puretalk.puretalkbackend.service.SysAdminService;
import thinkunderstar.puretalk.puretalkbackend.service.SysUserService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final SysUserService sysUserService;
    private final SysAdminService sysAdminService;

    public AdminController(SysUserService sysUserService, SysAdminService sysAdminService) {
        this.sysUserService = sysUserService;
        this.sysAdminService = sysAdminService;
    }

    /**
     * 管理员登录接口
     *
     * @param doLogin 包含手机号、密码和记住我选项的登录信息对象
     * @return 登录成功返回Result.success()，失败抛出异常
     * @throws BusinessException 手机号格式有误、账号不存在或此入口仅限管理员登录时抛出
     * @throws AuthException 密码错误时抛出
     */
    @PostMapping("/login")
    public Result adminLogin(@RequestBody DoLogin doLogin){
        return sysAdminService.adminLogin(doLogin);
    }

    /**
     * 管理员自己注销自己的账号
     * @return 注销结果
     */
    @SaCheckRole("admin")
    @SaCheckLogin
    @DeleteMapping("/delete")
    public Result doDelete(){
        return sysAdminService.doDelete();
    }

    /**
     * 处理管理员登出请求，使当前登录的用户退出系统。
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
     * @param doUpdate 包含需要更新的用户信息，如用户名、密码、手机号或邮箱地址等
     * @return 返回一个Result对象，表示操作的结果。如果更新成功，则返回成功信息；如果发生错误，则抛出相应的异常。
     */
    @SaCheckLogin
    @PutMapping("/update")
    public Result doUpdate(@RequestBody DoUpdate doUpdate){
        return sysUserService.doUpdate(doUpdate);
    }

    /**
     * Bans a user from the system.
     *
     * @return A Result object indicating the success or failure of the operation.
     */
    @PostMapping("/ban")
    @SaCheckRole(value = {"admin","root"}, mode = SaMode.OR)
    public Result banUser(@RequestBody BanUser banUser){
        return sysAdminService.banUser(banUser);
    }

    /**
     * 解禁用户
     *
     * @param userId 解禁用户id
     * @return 解禁结果
     */
    @PostMapping("/unban")
    @SaCheckRole(value = {"admin","root"}, mode = SaMode.OR)
    public Result unBanUser(@RequestParam long userId){
        return sysAdminService.unBanUser(userId);
    }
}
