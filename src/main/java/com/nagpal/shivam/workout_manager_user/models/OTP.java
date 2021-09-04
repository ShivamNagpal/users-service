package com.nagpal.shivam.workout_manager_user.models;

import com.nagpal.shivam.workout_manager_user.enums.OTPPurpose;
import com.nagpal.shivam.workout_manager_user.utils.ModelConstants;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OTP extends BaseModel {
    private Long userId;
    private String email;
    private String otpHash;
    private Integer count;
    private OffsetDateTime lastAccessTime;
    private OTPPurpose purpose;

    public static OTP fromRow(Row row) {
        OTP otp = new OTP();
        BaseModel.populateMappingFromRow(otp, row);
        otp.setUserId(row.getLong(ModelConstants.USER_ID));
        otp.setEmail(row.getString(ModelConstants.EMAIL));
        otp.setOtpHash(row.getString(ModelConstants.OTP_HASH));
        otp.setCount(row.getInteger(ModelConstants.COUNT));
        otp.setLastAccessTime(row.getOffsetDateTime(ModelConstants.LAST_ACCESS_TIME));
        otp.setPurpose(OTPPurpose.valueOf(row.getString(ModelConstants.PURPOSE)));
        return otp;
    }

    public static List<OTP> fromRows(RowSet<Row> rowSet) {
        RowIterator<Row> iterator = rowSet.iterator();
        ArrayList<OTP> otpList = new ArrayList<>();
        while (iterator.hasNext()) {
            otpList.add(fromRow(iterator.next()));
        }
        return otpList;
    }
}
