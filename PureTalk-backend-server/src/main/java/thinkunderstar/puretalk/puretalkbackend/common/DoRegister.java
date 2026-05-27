package thinkunderstar.puretalk.puretalkbackend.common;

import lombok.Data;

@Data
public class DoRegister {
    private String username;
    private String password;
    private String phone;
    private String email;
    private boolean rightPhone = false;
    private boolean rightEmail = false;

    public DoRegister(String username, String password, String phone ,String email,boolean rightPhone,boolean rightEmail) {
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.email = email;
        this.rightPhone = rightPhone;
        this.rightEmail = rightEmail;
    }

    public DoRegister() {
    }
}
