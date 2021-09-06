package com.nagpal.shivam.workout_manager_user.daos;

import com.nagpal.shivam.workout_manager_user.models.User;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;

public interface UserDao {
    Future<Long> signUp(SqlClient sqlClient, User user);
}
