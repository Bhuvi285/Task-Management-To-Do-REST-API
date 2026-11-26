package com.taskmanager.security;

import com.taskmanager.enums.OAuthProvider;
import com.taskmanager.entity.User;
import com.taskmanager.service.SocialAccountService;
import com.taskmanager.service.SocialIdentity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GoogleOidcUserService
    implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OAuth2UserService<OidcUserRequest, OidcUser> delegate;
    private final SocialAccountService socialAccountService;

    @Autowired
    public GoogleOidcUserService(SocialAccountService socialAccountService) {
        this(new OidcUserService(), socialAccountService);
    }

    GoogleOidcUserService(
        OAuth2UserService<OidcUserRequest, OidcUser> delegate,
        SocialAccountService socialAccountService
    ) {
        this.delegate = delegate;
        this.socialAccountService = socialAccountService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest)
        throws OAuth2AuthenticationException {
        if (!"google".equals(userRequest.getClientRegistration().getRegistrationId())) {
            throw SocialAccountService.authenticationFailure();
        }
        OidcUser providerUser;
        try {
            providerUser = delegate.loadUser(userRequest);
        } catch (OAuth2AuthenticationException exception) {
            throw SocialAccountService.authenticationFailure();
        }
        Map<String, Object> claims = providerUser.getClaims();
        Object emailVerifiedClaim = claims.get("email_verified");
        if (!Boolean.TRUE.equals(emailVerifiedClaim)) {
            throw SocialAccountService.authenticationFailure();
        }

        String subject = providerUser.getSubject();
        String email = stringClaim(claims, "email");
        if (subject == null || subject.isBlank() || email == null) {
            throw SocialAccountService.authenticationFailure();
        }
        String name = stringClaim(claims, "name");
        if (name == null) {
            String givenName = stringClaim(claims, "given_name");
            String familyName = stringClaim(claims, "family_name");
            name = joinName(givenName, familyName);
        }

        final User localUser;
        try {
            localUser = socialAccountService.resolveOrProvision(new SocialIdentity(
                OAuthProvider.GOOGLE,
                subject,
                email,
                true,
                name
            ));
        } catch (DataAccessException exception) {
            throw SocialAccountService.authenticationFailure();
        }
        return new LocalOidcUser(providerUser, localUser);
    }

    private String stringClaim(Map<String, Object> claims, String name) {
        Object value = claims.get(name);
        if (value instanceof String text && !text.isBlank()) {
            return text.trim();
        }
        return null;
    }

    private String joinName(String givenName, String familyName) {
        if (givenName == null) {
            return familyName;
        }
        if (familyName == null) {
            return givenName;
        }
        return givenName + " " + familyName;
    }
}
