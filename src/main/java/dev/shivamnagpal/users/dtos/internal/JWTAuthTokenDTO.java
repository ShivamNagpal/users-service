package dev.shivamnagpal.users.dtos.internal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JWTAuthTokenDTO {
    private Long userId;
    private String sessionId;
    private String[] roles;
}
