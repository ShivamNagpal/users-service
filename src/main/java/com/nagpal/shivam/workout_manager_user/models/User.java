package com.nagpal.shivam.workout_manager_user.models;

import com.nagpal.shivam.workout_manager_user.enums.AccountStatus;
import com.nagpal.shivam.workout_manager_user.utils.ModelConstants;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class User extends BaseModel {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Boolean emailVerified;
    private AccountStatus accountStatus;
    private JsonObject meta;

    public static User fromRow(Row row) {
        User user = new User();
        BaseModel.populateMappingFromRow(user, row);
        user.setFirstName(row.getString(ModelConstants.FIRST_NAME));
        user.setLastName(row.getString(ModelConstants.LAST_NAME));
        user.setEmail(row.getString(ModelConstants.EMAIL));
        user.setPassword(row.getString(ModelConstants.PASSWORD));
        user.setEmailVerified(row.getBoolean(ModelConstants.EMAIL_VERIFIED));
        user.setAccountStatus(AccountStatus.valueOf(row.getString(ModelConstants.ACCOUNT_STATUS)));
        user.setMeta(row.getJsonObject(ModelConstants.META));
        return user;
    }

    public static List<User> fromRows(RowSet<Row> rowSet) {
        RowIterator<Row> iterator = rowSet.iterator();
        ArrayList<User> userList = new ArrayList<>();
        while (iterator.hasNext()) {
            userList.add(fromRow(iterator.next()));
        }
        return userList;
    }
}
