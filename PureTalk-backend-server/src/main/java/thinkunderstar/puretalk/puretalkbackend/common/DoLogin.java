package thinkunderstar.puretalk.puretalkbackend.common;

import lombok.Data;

@Data
public class DoLogin {
    private String phone;
    private String password;
    private boolean isRemember;

    public DoLogin(String phone, String password, boolean isRemember) {
        this.phone = phone;
        this.password = password;
        this.isRemember = isRemember;
    }

    public DoLogin() {
    }
}
