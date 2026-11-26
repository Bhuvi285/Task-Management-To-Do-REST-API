package com.taskmanager.service;

import com.taskmanager.enums.OAuthProvider;

public record SocialIdentity(
    OAuthProvider provider,
    String providerUserId,
    String email,
    boolean emailVerified,
    String name
) {
}
