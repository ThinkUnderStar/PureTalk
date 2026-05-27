package thinkunderstar.puretalk.puretalkbackend.service;

import org.springframework.web.bind.annotation.RequestBody;
import thinkunderstar.puretalk.puretalkbackend.common.DoRegister;
import thinkunderstar.puretalk.puretalkbackend.common.Result;

/**
 * 系统根服务接口，提供系统级别操作的支持。
 * 该接口定义了系统核心功能可以执行的操作方法签名。
 * 实现类需要提供具体的业务逻辑实现。
 */
public interface SysRootService {
    /**
     * 执行管理员注册操作。
     *
     * @param doRegister 包含注册所需信息（用户名、密码、手机号、邮箱及验证状态）的对象
     * @return 返回一个Result对象，包含状态码、提示信息及数据。成功时返回200状态码，失败则返回500状态码，并附带相应的错误信息
     */
    Result doAdminRegister( DoRegister doRegister);

    /**
     * 获取删除管理员数据的结果。
     *
     * @param phone 管理员的手机号
     * @return 返回一个Result对象，包含状态码、提示信息及数据。成功时返回200状态码，失败则返回500状态码，并附带相应的错误信息
     */
    Result getDeleteAdminData(String phone);

    /**
     * 执行删除管理员操作。
     *
     * @param phone 管理员的手机号
     * @return 返回一个Result对象，包含状态码、提示信息及数据。成功时返回200状态码，失败则返回500状态码，并附带相应的错误信息
     */
    Result deleteAdmin(String phone);
}
