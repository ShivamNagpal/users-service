package dev.shivamnagpal.users.core;

import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

public record RequestPath(@NonNull String path) {

    private static final String STRIP_CHARS = " /";

    private static final String HTTP_PATH_SEPARATOR = "/";

    public RequestPath(String path) {
        this.path = stripPathAndPrefixWithSeparator(path);
    }

    public RequestPath next(@NonNull String nextPath) {
        String newPath = this.path + stripPathAndPrefixWithSeparator(nextPath);
        return new RequestPath(newPath);
    }

    private String stripPathAndPrefixWithSeparator(String path) {
        String newPath = StringUtils.strip(path, STRIP_CHARS);
        return newPath.isEmpty() ? "" : HTTP_PATH_SEPARATOR + newPath;
    }
}
