package dev.shivamnagpal.users;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
class TestMainVerticle {

    @Test
    void verticle_deployed(Vertx vertx, VertxTestContext testContext) {
        Assertions.assertEquals(5, 2 + 3);
        testContext.completeNow();
    }
}
