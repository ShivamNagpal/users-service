package dev.shivamnagpal.users.core;

import io.vertx.ext.web.Router;

public abstract class Controller {
    protected final Router router;

    protected final RequestPath requestPath;

    protected Controller(Router router, RequestPath requestPath) {
        this.router = router;
        this.requestPath = requestPath;
        registerRoutes();
    }

    public abstract void registerRoutes();
}
