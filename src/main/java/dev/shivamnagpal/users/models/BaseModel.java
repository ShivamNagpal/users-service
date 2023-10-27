package dev.shivamnagpal.users.models;

import dev.shivamnagpal.users.utils.ModelConstants;
import io.vertx.sqlclient.Row;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class BaseModel {
    private Long id;
    private Boolean deleted;
    private OffsetDateTime timeCreated;
    private OffsetDateTime timeLastModified;

    protected static void populateMappingFromRow(BaseModel baseModel, Row row) {
        baseModel.setId(row.getLong(ModelConstants.ID));
        baseModel.setDeleted(row.getBoolean(ModelConstants.DELETED));
        baseModel.setTimeCreated(row.getOffsetDateTime(ModelConstants.TIME_CREATED));
        baseModel.setTimeLastModified(row.getOffsetDateTime(ModelConstants.TIME_LAST_MODIFIED));
    }

    public void updateLastModifiedTime() {
        setTimeLastModified(OffsetDateTime.now());
    }
}
