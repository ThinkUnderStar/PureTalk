package thinkunderstar.puretalk.puretalkbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户信息表
 */
@Data
@TableName("users")
public class User {
    /**
     * 空参构造
     */
    public User() {
    }

    /**
     * 构造一个新的User实例。
     * @param username 用户名
     * @param password 加密后的密码
     * @param email 邮箱地址
     */
    public User(String username, String password,String phone,String email) {
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.email = email;
    }

    /**
     * 创建管理员对象
     * @param username 用户名
     * @param password 加密后的密码
     * @param phone 管理员手机号
     * @param email 管理员邮箱地址
     * @param role 管理员身份
     */
    public User(String username, String password,String phone,String email,Integer role) {
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.email = email;
        this.role = role;
    }

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 加密后的密码
     */
    private String password;

    /**
     * 用户的联系电话
     */
    private String phone;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 角色: 1-普通用户, 2-管理员, 3-root
     */
    private Integer role;

    /**
     * 状态: 1-正常, 0-禁言
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Version
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记 0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 记录用户被逻辑删除的时间。当用户被标记为已删除时，此字段将存储删除操作发生的具体时间。
     */
    private LocalDateTime deleteTime;

    /**
     * 用户被禁言的时间。当用户被禁言时，此字段将存储禁言操作发生的具体时间。
     */
    private LocalDateTime banOn;

    /**
     * 用户被禁言的原因。当用户被设置为禁言状态时，此字段记录具体的禁言原因。
     */
    private String banReason;

    /**
     * 表示执行禁言操作的用户的ID。该字段用于记录对当前用户进行禁言操作的管理员或系统的用户ID。
     */
    private long whoBan;

    /**
     * 用户被禁言的结束时间。当用户被设置为禁言状态时，此字段将存储禁言解除的具体时间。
     * 如果此字段为空或者其值小于当前时间，则表示用户当前未处于禁言状态。
     */
    private LocalDateTime bannedUntil;

    /**
     * 表示执行删除操作的用户的ID。该字段用于记录对当前用户进行逻辑删除操作的管理员或系统的用户ID。
     */
    private long whoDelete;
}