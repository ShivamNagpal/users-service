package com.nagpal.shivam.workout_manager_user.configurations;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;

public class GenericMessageCodec<T> implements MessageCodec<T, T> {
    private final Class<T> cls;

    public GenericMessageCodec(Class<T> cls) {
        this.cls = cls;
    }

    @Override
    public void encodeToWire(Buffer buffer, T user) {
        String encode = Json.encode(user);
        buffer.appendInt(encode.length()).appendString(encode);
    }

    @Override
    public T decodeFromWire(int pos, Buffer buffer) {
        int length = buffer.getInt(pos);
        pos += 4;
        String jsonString = buffer.getString(pos, pos + length);
        return Json.decodeValue(jsonString, cls);
    }

    @Override
    public T transform(T user) {
        return user;
    }

    @Override
    public String name() {
        return cls.getName();
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
