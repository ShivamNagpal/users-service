package dev.shivamnagpal.users.services;

import dev.shivamnagpal.users.dtos.internal.JWTAuthTokenDTO;
import dev.shivamnagpal.users.dtos.internal.UserUpdateRequestDTO;
import dev.shivamnagpal.users.dtos.request.EmailRequestDTO;
import dev.shivamnagpal.users.dtos.request.LoginRequestDTO;
import dev.shivamnagpal.users.dtos.request.PasswordUpdateRequestDTO;
import dev.shivamnagpal.users.dtos.response.LoginResponseDTO;
import dev.shivamnagpal.users.dtos.response.OTPResponseDTO;
import dev.shivamnagpal.users.dtos.response.UserResponseDTO;
import dev.shivamnagpal.users.models.User;
import io.vertx.core.Future;

public interface UserService {
    Future<OTPResponseDTO> signUp(User user);

    Future<Object> login(LoginRequestDTO loginRequestDTO);

    Future<Void> logout(JWTAuthTokenDTO jwtAuthTokenDTO, boolean allSession);

    Future<UserResponseDTO> getById(JWTAuthTokenDTO jwtAuthTokenDTO);

    Future<UserResponseDTO> update(JWTAuthTokenDTO jwtAuthTokenDTO, UserUpdateRequestDTO userUpdateRequestDTO);

    Future<OTPResponseDTO> updateEmail(JWTAuthTokenDTO jwtAuthTokenDTO, EmailRequestDTO emailRequestDTO);

    Future<LoginResponseDTO> updatePassword(JWTAuthTokenDTO jwtAuthTokenDTO,
                                            PasswordUpdateRequestDTO passwordUpdateRequestDTO);

    Future<OTPResponseDTO> resetPassword(EmailRequestDTO emailRequestDTO);

    Future<Void> deactivate(JWTAuthTokenDTO jwtAuthTokenDTO);

    Future<Void> reactivate(LoginRequestDTO loginRequestDTO);

    Future<Void> scheduleForDeletion(JWTAuthTokenDTO jwtAuthTokenDTO);
}
