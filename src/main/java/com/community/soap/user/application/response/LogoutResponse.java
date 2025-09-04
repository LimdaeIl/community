package com.community.soap.user.application.response;

public record LogoutResponse(
        boolean success
) {

    public static LogoutResponse ok() {
        return new LogoutResponse(true);
    }

}
