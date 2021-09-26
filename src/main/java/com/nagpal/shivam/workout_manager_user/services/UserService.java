package com.nagpal.shivam.workout_manager_user.services;

import com.nagpal.shivam.workout_manager_user.dtos.internal.JWTAuthTokenDTO;
import com.nagpal.shivam.workout_manager_user.dtos.internal.UserUpdateRequestDTO;
import com.nagpal.shivam.workout_manager_user.dtos.request.EmailRequestDTO;
import com.nagpal.shivam.workout_manager_user.dtos.request.LoginRequestDTO;
import com.nagpal.shivam.workout_manager_user.dtos.request.PasswordUpdateRequestDTO;
import com.nagpal.shivam.workout_manager_user.dtos.response.LoginResponseDTO;
import com.nagpal.shivam.workout_manager_user.dtos.response.OTPResponseDTO;
import com.nagpal.shivam.workout_manager_user.dtos.response.UserResponseDTO;
import com.nagpal.shivam.workout_manager_user.models.User;
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
