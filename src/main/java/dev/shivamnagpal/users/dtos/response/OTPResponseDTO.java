package dev.shivamnagpal.users.dtos.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OTPResponseDTO {
    private String otpToken;
}
