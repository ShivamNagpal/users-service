package dev.shivamnagpal.users.daos;

import dev.shivamnagpal.users.enums.AccountStatus;
import dev.shivamnagpal.users.models.User;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;

import java.util.List;
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

    Future<List<User>> findAccountsScheduledForDeletion(SqlClient sqlClient, int limit);

    Future<Void> updateUserAccountsAsDeleted(SqlClient sqlClient, List<User> users);
}
