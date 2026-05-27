package thinkunderstar.puretalk.puretalkbackend.service;

import thinkunderstar.puretalk.puretalkbackend.common.BanUser;
import thinkunderstar.puretalk.puretalkbackend.common.DoLogin;
import thinkunderstar.puretalk.puretalkbackend.common.Result;

/**
 * 系统管理员服务接口，提供对系统管理相关功能的支持。
 * 该接口定义了系统管理员可以执行的操作方法签名。
 * 实现类需要提供具体的业务逻辑实现。
 */
public interface SysAdminService {
    /**
     * 执行管理员登录操作。
     *
     * @param doLogin 包含登录所需信息（手机号、密码及是否记住登录状态）的对象
     * @return 返回一个Result对象，包含状态码、提示信息及数据。成功时返回200状态码，失败则返回500状态码，并附带相应的错误信息
     */
    Result adminLogin(DoLogin doLogin);

    /**
     * 执行删除操作。
     *
     * @return 返回一个Result对象，包含状态码、提示信息及数据。成功时返回200状态码，失败则返回500状态码，并附带相应的错误信息
     */
    Result doDelete();

    /**
     * 封禁指定用户。
     *
     * @return 返回一个Result对象，包含状态码、提示信息及数据。成功时返回200状态码，失败则返回500状态码，并附带相应的错误信息
     */
    Result banUser(BanUser banUser);

    /**
     * 解禁指定用户。
     *
     * @param userId 解封用户id
     * @return 解封结果
     */
    Result unBanUser(long userId);
}
