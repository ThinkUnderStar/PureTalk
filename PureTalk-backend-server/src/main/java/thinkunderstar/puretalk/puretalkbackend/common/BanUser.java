package thinkunderstar.puretalk.puretalkbackend.common;

import lombok.Data;

@Data
public class BanUser {
    public BanUser() {
    }

    public BanUser(long userId, String banReason, long banTime) {
        this.userId = userId;
        this.banReason = banReason;
        this.banTime = banTime;
    }

    private long userId;

    /**
     * 禁言原因
     */
    private String banReason;

    /**
     * 0代表永久禁言
     */
    private long banTime;

}
