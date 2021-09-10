package com.nagpal.shivam.workout_manager_user.dtos.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SignUpResponseDto {
    private Long userId;
    private String otpToken;
}
