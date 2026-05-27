package thinkunderstar.puretalk.puretalkbackend.common;

import lombok.Data;

@Data
public class ValidateCode {
    private String phone;
    private String email;
    private String phoneCode;
    private String emailCode;
}
