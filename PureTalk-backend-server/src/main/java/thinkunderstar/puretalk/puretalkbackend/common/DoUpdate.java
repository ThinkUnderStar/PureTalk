package thinkunderstar.puretalk.puretalkbackend.common;

import lombok.Data;

@Data
public class DoUpdate {
    public DoUpdate(String username, String password, String phone, String email, String avatar) {
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.email = email;
        this.avatar = avatar;
    }

    public DoUpdate() {
    }

    private String username;
    private String password;
    private String phone;
    private String email;
    private String avatar;
    private boolean isRight = false;
}
