package dev.shivamnagpal.users.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.shivamnagpal.users.models.Role;
import dev.shivamnagpal.users.models.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
@NoArgsConstructor
public class UserResponseDTO {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> roles;

    public static UserResponseDTO from(User user, List<Role> roles) {
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setUserId(user.getId());
        userResponseDTO.setFirstName(user.getFirstName());
        userResponseDTO.setLastName(user.getLastName());
        userResponseDTO.setEmail(user.getEmail());
        userResponseDTO.setRoles(
                Stream.ofNullable(roles).flatMap(Collection::stream).map(role -> role.getRoleName().name())
                        .collect(Collectors.toList())
        );
        return userResponseDTO;
    }
}
