package com.nagpal.shivam.workout_manager_user.services;

import com.nagpal.shivam.workout_manager_user.models.User;
import io.vertx.core.Future;

public interface UserService {
    Future<Void> signUp(User user);
}
