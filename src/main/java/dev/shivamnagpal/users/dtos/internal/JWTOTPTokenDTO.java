package dev.shivamnagpal.users.dtos.internal;

import dev.shivamnagpal.users.enums.OTPPurpose;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JWTOTPTokenDTO {
    private Long userId;
    private String email;
    private OTPPurpose otpPurpose;
}
