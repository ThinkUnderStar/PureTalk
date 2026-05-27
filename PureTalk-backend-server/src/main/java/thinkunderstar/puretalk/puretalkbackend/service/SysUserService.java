package thinkunderstar.puretalk.puretalkbackend.service;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import thinkunderstar.puretalk.puretalkbackend.common.*;

/**
 * 系统用户服务接口，提供与用户相关的系统级操作。
 * 该接口定义了发送和验证邮箱及手机验证码的方法签名。
 */
public interface SysUserService {
    /**
     * 发送邮箱验证码。
     *
     * @param email 接收验证码的邮箱地址
     * @return 包含状态码、提示信息及数据的Result对象。成功时返回200状态码，失败则返回500状态码，并附带相应的错误信息
     */
    Result sendEmailCode(String email);

    /**
     * 验证邮箱验证码的有效性。
     *
     * @param email 包含待验证的邮箱及验证码信息的对象
     * @return 返回一个Result对象，包含状态码、提示信息及数据。成功时返回200状态码，失败则返回500状态码，并附带相应的错误信息
     */
    Result validateEmailCode(ValidateCode email);

    /**
     * 发送手机验证码。
     *
     * @param phone 接收验证码的手机号
     * @return 包含状态码、提示信息及数据的Result对象。成功时返回200状态码，失败则返回500状态码，并附带相应的错误信息
     */
    Result sendPhoneCode(String phone);

    /**
     * 验证手机验证码的有效性。
     *
     * @param phone 包含待验证的手机号及验证码信息的对象
     * @return 返回一个Result对象，包含状态码、提示信息及数据。成功时返回200状态码，失败则返回500状态码，并附带相应的错误信息
     */
    boolean validatePhoneCode(ValidateCode phone);

    /**
     * 执行用户登录操作。
     *
     * @param doLogin 包含登录所需信息（手机号、密码及是否记住登录状态）的对象
     * @return 返回一个Result对象，包含状态码、提示信息及数据。成功时返回200状态码，失败则返回500状态码，并附带相应的错误信息
     */
    Result doLogin(DoLogin doLogin);

    /**
     * 用户上传自己的头像
     *
     * @param file 头像文件
     * @return 存储结果与存储的头像文件
     */
    Result uploadAvatar(MultipartFile file);

    /**
     * 执行用户注册操作。
     *
     * @param doRegister 包含注册所需信息（用户名、密码、手机号、邮箱及验证状态）的对象
     * @return 返回一个Result对象，包含状态码、提示信息及数据。成功时返回200状态码，失败则返回500状态码，并附带相应的错误信息
     */
    Result doRegister(DoRegister doRegister);

    /**
     * 执行删除操作。
     *
     * @return 返回一个Result对象，包含状态码、提示信息及数据。成功时返回200状态码，失败则返回500状态码，并附带相应的错误信息
     */
    Result doDelete();

    /**
     * 更新用户信息。
     *
     * @param doUpdate 包含需要更新的用户信息（用户名、密码、手机号、邮箱及头像）的对象
     * @return 返回一个Result对象，包含状态码、提示信息及数据。成功时返回200状态码，失败则返回500状态码，并附带相应的错误信息
     */
    Result doUpdate(@RequestBody DoUpdate doUpdate);
}
