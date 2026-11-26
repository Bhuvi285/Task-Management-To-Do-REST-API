package com.taskmanager.security;

import com.taskmanager.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public final class LocalOAuth2User implements OAuth2User, LocalUserOAuth2Principal {

    private final OAuth2User delegate;
    private final User localUser;

    public LocalOAuth2User(OAuth2User delegate, User localUser) {
        this.delegate = delegate;
        this.localUser = localUser;
    }

    @Override
    public User getLocalUser() {
        return localUser;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return delegate.getAuthorities();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }
}
