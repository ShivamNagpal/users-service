package com.nagpal.shivam.workout_manager_user.models;

import com.nagpal.shivam.workout_manager_user.utils.ModelConstants;
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
public class Role extends BaseModel {
    private Long userId;
    private String roleName;

    public static Role fromRow(Row row) {
        Role role = new Role();
        BaseModel.populateMappingFromRow(role, row);
        role.setUserId(row.getLong(ModelConstants.USER_ID));
        role.setRoleName(row.getString(ModelConstants.ROLE));
        return role;
    }

    public static List<Role> fromRows(RowSet<Row> rowSet) {
        RowIterator<Row> iterator = rowSet.iterator();
        ArrayList<Role> roleList = new ArrayList<>();
        while (iterator.hasNext()) {
            roleList.add(fromRow(iterator.next()));
        }
        return roleList;
    }
}
