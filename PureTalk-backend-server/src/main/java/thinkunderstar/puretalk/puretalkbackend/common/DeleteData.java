package thinkunderstar.puretalk.puretalkbackend.common;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeleteData {
    private String username;
    private String phone;
    private String email;
    private String role;
    private LocalDateTime creatTime;

    public DeleteData(String username, String phone, String email, String role, LocalDateTime creatTime) {
        this.username = username;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.creatTime = creatTime;
    }

    public DeleteData() {
    }
}
