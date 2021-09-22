package com.nagpal.shivam.workout_manager_user.daos;

import com.nagpal.shivam.workout_manager_user.enums.AccountStatus;
import com.nagpal.shivam.workout_manager_user.models.User;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;

import java.util.Optional;

public interface UserDao {
    Future<Long> signUp(SqlClient sqlClient, User user);

    Future<Void> activateUser(SqlClient sqlClient, Long userId);

    Future<Optional<User>> getUserByEmail(SqlClient sqlClient, String email);

    Future<Optional<User>> getById(SqlClient sqlClient, Long id);

    Future<Void> update(SqlClient sqlClient, User user);

    Future<Void> updateEmail(SqlClient sqlClient, Long userId, String email);

    Future<Void> updatePassword(SqlClient sqlClient, Long userId, String password);

    Future<Void> updateStatus(SqlClient sqlClient, Long userId, AccountStatus status);
}
