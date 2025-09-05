package com.community.soap.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthKeys {

    public static final String ATTR_USER_ID  = "X_USER_ID";
    public static final String ATTR_USER_ROLE = "X_USER_ROLE";

    public static final String HDR_USER_ID   = "X-USER-ID";
    public static final String HDR_USER_ROLE = "X-USER-ROLE";
}