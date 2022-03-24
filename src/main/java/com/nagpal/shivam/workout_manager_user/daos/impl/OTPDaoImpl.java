package com.nagpal.shivam.workout_manager_user.daos.impl;

import com.nagpal.shivam.workout_manager_user.daos.OTPDao;
import com.nagpal.shivam.workout_manager_user.enums.OTPPurpose;
import com.nagpal.shivam.workout_manager_user.enums.OTPStatus;
import com.nagpal.shivam.workout_manager_user.models.OTP;
import com.nagpal.shivam.workout_manager_user.utils.Constants;
import com.nagpal.shivam.workout_manager_user.utils.DbUtils;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

import java.time.OffsetDateTime;
import java.util.Optional;

public class OTPDaoImpl implements OTPDao {

    public static final String SELECT_TRIGGERED_OTP =
            "SELECT * FROM otp where user_id=$1 and email=$2 and valid_after > $3 and purpose = $4 and " +
                    "otp_status != $5";
    public static final String SELECT_ACTIVE_OTP =
            "SELECT * FROM otp where user_id=$1 and email=$2 and valid_after > $3 and valid_after <= $4 and " +
                    "purpose = $5 and otp_status = $6";
    public static final String INSERT_OTP = "INSERT INTO otp (user_id, email, otp_hash, count, valid_after, " +
            "purpose, deleted, time_created, time_last_modified, otp_status) VALUES($1, $2, $3, $4, $5, $6, $7, $8, " +
            "$9, $10) RETURNING id";
    public static final String UPDATE_OTP =
            "UPDATE otp SET user_id=$1, email=$2, otp_hash=$3, count=$4, valid_after=$5, purpose=$6, " +
                    "deleted=$7, time_created=$8, time_last_modified=$9, otp_status=$10 WHERE id=$11";
    public static final String UPDATE_OTP_STATUS = "UPDATE otp SET otp_status=$1, time_last_modified=$2 WHERE id=$3";

    private final JsonObject config;

    public OTPDaoImpl(JsonObject config) {
        this.config = config;
    }

    @Override
    public Future<Optional<OTP>> fetchAlreadyTriggeredOTP(SqlClient sqlClient, Long userId, String email,
                                                          OTPPurpose otpPurpose) {
        OffsetDateTime lastActiveTime = OffsetDateTime.now().minusSeconds(config.getInteger(Constants.OTP_EXPIRY_TIME));
        Tuple values = Tuple.of(userId, email, lastActiveTime, otpPurpose, OTPStatus.USED);
        return DbUtils.executeQueryAndReturnOne(sqlClient, SELECT_TRIGGERED_OTP, values, OTP::fromRow);
    }

    @Override
    public Future<Optional<OTP>> fetchActiveOTP(SqlClient sqlClient, Long userId, String email,
                                                OTPPurpose otpPurpose) {
        OffsetDateTime currentTime = OffsetDateTime.now();
        OffsetDateTime lastActiveTime = currentTime.minusSeconds(config.getInteger(Constants.OTP_EXPIRY_TIME));
        Tuple values = Tuple.of(userId, email, lastActiveTime, currentTime, otpPurpose, OTPStatus.ACTIVE);
        return DbUtils.executeQueryAndReturnOne(sqlClient, SELECT_ACTIVE_OTP, values, OTP::fromRow);
    }

    @Override
    public Future<Void> update(SqlClient sqlClient, OTP otp) {
        otp.updateLastModifiedTime();
        Tuple tuple = Tuple.of(
                otp.getUserId(),
                otp.getEmail(),
                otp.getOtpHash(),
                otp.getCount(),
                otp.getValidAfter(),
                otp.getPurpose(),
                otp.getDeleted(),
                otp.getTimeCreated(),
                otp.getTimeLastModified(),
                otp.getOtpStatus(),
                otp.getId()
        );
        return DbUtils.executeQuery(sqlClient, UPDATE_OTP, tuple);
    }

    @Override
    public Future<Long> insert(SqlClient sqlClient, OTP otp) {
        otp.updateLastModifiedTime();
        Tuple tuple = Tuple.of(
                otp.getUserId(),
                otp.getEmail(),
                otp.getOtpHash(),
                otp.getCount(),
                otp.getValidAfter(),
                otp.getPurpose(),
                otp.getDeleted(),
                otp.getTimeCreated(),
                otp.getTimeLastModified(),
                otp.getOtpStatus()
        );
        return DbUtils.executeQueryAndReturnOne(sqlClient, INSERT_OTP, tuple, DbUtils::mapRowToId).map(Optional::get);
    }

    @Override
    public Future<Void> updateOTPStatus(SqlClient sqlClient, Long otpId, OTPStatus otpStatus) {
        Tuple values = Tuple.of(
                otpStatus,
                OffsetDateTime.now(),
                otpId
        );
        return DbUtils.executeQuery(sqlClient, UPDATE_OTP_STATUS, values);
    }
}
