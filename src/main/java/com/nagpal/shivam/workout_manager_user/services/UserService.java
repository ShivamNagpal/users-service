package com.nagpal.shivam.workout_manager_user.services;

import com.nagpal.shivam.workout_manager_user.dtos.internal.JWTAuthTokenDTO;
import com.nagpal.shivam.workout_manager_user.dtos.request.LoginRequestDTO;
import com.nagpal.shivam.workout_manager_user.dtos.response.LoginResponseDTO;
import com.nagpal.shivam.workout_manager_user.dtos.response.OTPResponseDTO;
import com.nagpal.shivam.workout_manager_user.models.User;
import io.vertx.core.Future;

public interface UserService {
    Future<OTPResponseDTO> signUp(User user);

    Future<LoginResponseDTO> login(LoginRequestDTO loginRequestDTO);

    Future<Void> logout(JWTAuthTokenDTO jwtAuthTokenDTO, boolean allSession);
}
