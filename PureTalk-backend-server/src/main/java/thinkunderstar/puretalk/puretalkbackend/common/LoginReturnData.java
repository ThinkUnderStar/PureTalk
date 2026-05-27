package thinkunderstar.puretalk.puretalkbackend.common;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginReturnData {
    public LoginReturnData(Long id, String username, String phone, String email, String avatar, Integer role, Integer status, LocalDateTime createTime, String banReason, LocalDateTime banOn , LocalDateTime bannedUntil, String token) {
        this.banOn = banOn;
        this.id = id;
        this.username = username;
        this.phone = phone;
        this.email = email;
        this.avatar = avatar;
        this.role = role;
        this.status = status;
        this.createTime = createTime;
        this.banReason = banReason;
        this.bannedUntil = bannedUntil;
        this.token = token;
    }

    public LoginReturnData() {
    }

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

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
     * (前端可根据此字段控制菜单显示)
     */
    private Integer role;

    /**
     * 状态: 1-正常, 0-禁言
     * (前端可根据此字段禁用账号操作)
     */
    private Integer status;

    /**
     * 注册时间
     */
    private LocalDateTime createTime;

    // --- 以下是禁言相关的详细信息，仅当 status=0 时前端可能关注 ---

    /**
     * 禁言原因
     */
    private String banReason;

    /**
     * 表示用户被禁言的时间点。
     * 当用户的 {@code status} 为 0（即被禁言）时，此字段记录了禁言开始的具体时间。
     */
    private LocalDateTime banOn;

    /**
     * 禁言结束时间
     */
    private LocalDateTime bannedUntil;

    /**
     * 返回登录令牌
     */
    private String token;
}
