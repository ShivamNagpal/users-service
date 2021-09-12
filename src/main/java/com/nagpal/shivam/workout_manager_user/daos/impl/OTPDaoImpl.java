package com.nagpal.shivam.workout_manager_user.daos.impl;

import com.nagpal.shivam.workout_manager_user.daos.OTPDao;
import com.nagpal.shivam.workout_manager_user.models.OTP;
import com.nagpal.shivam.workout_manager_user.utils.Constants;
import com.nagpal.shivam.workout_manager_user.utils.DbUtils;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

import java.time.OffsetDateTime;
import java.util.Optional;

public class OTPDaoImpl implements OTPDao {

    public static final String SELECT_TRIGGERED_OTP =
            "SELECT * FROM otp where user_id=$1 and email=$2 and last_access_time > $3";
    public static final String SELECT_ACTIVE_OTP =
            "SELECT * FROM otp where user_id=$1 and email=$2 and last_access_time > $3 and last_access_time <= $4";
    public static final String INSERT_OTP = "INSERT INTO otp (user_id, email, otp_hash, count, last_access_time, " +
            "purpose, deleted, time_created, time_last_modified) VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9) RETURNING" +
            " id";
    public static final String UPDATE_OTP =
            "UPDATE otp SET user_id=$1, email=$2, otp_hash=$3, count=$4, last_access_time=$5, purpose=$6, " +
                    "deleted=$7, time_created=$8, time_last_modified=$9 WHERE id=$10";

    @Override
    public Future<Optional<OTP>> fetchAlreadyTriggeredOTP(SqlClient sqlClient, Long userId, String email) {
        OffsetDateTime lastActiveTime = OffsetDateTime.now().minusMinutes(Constants.OTP_EXPIRY_TIME);
        Tuple values = Tuple.of(userId, email, lastActiveTime);
        return DbUtils.executeQueryAndReturnOne(sqlClient, SELECT_TRIGGERED_OTP, values, OTP::fromRow);
    }

    @Override
    public Future<Optional<OTP>> fetchActiveOTP(SqlClient sqlClient, Long userId, String email) {
        OffsetDateTime currentTime = OffsetDateTime.now();
        OffsetDateTime lastActiveTime = currentTime.minusMinutes(Constants.OTP_EXPIRY_TIME);
        Tuple values = Tuple.of(userId, email, lastActiveTime, currentTime);
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
                otp.getLastAccessTime(),
                otp.getPurpose(),
                otp.getDeleted(),
                otp.getTimeCreated(),
                otp.getTimeLastModified(),
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
                otp.getLastAccessTime(),
                otp.getPurpose(),
                otp.getDeleted(),
                otp.getTimeCreated(),
                otp.getTimeLastModified()
        );
        return DbUtils.executeQueryAndReturnOne(sqlClient, INSERT_OTP, tuple, DbUtils::mapRowToId).map(Optional::get);
    }
}
