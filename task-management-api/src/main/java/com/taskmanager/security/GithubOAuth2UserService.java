package com.taskmanager.security;

import com.taskmanager.entity.User;
import com.taskmanager.enums.OAuthProvider;
import com.taskmanager.service.SocialAccountService;
import com.taskmanager.service.SocialIdentity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class GithubOAuth2UserService
    implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;
    private final GithubEmailClient githubEmailClient;
    private final SocialAccountService socialAccountService;

    @Autowired
    public GithubOAuth2UserService(
        GithubEmailClient githubEmailClient,
        SocialAccountService socialAccountService
    ) {
        this(new DefaultOAuth2UserService(), githubEmailClient, socialAccountService);
    }

    GithubOAuth2UserService(
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate,
        GithubEmailClient githubEmailClient,
        SocialAccountService socialAccountService
    ) {
        this.delegate = delegate;
        this.githubEmailClient = githubEmailClient;
        this.socialAccountService = socialAccountService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
        throws OAuth2AuthenticationException {
        if (!"github".equals(userRequest.getClientRegistration().getRegistrationId())) {
            throw SocialAccountService.authenticationFailure();
        }

        OAuth2User providerUser;
        try {
            providerUser = delegate.loadUser(userRequest);
        } catch (OAuth2AuthenticationException exception) {
            throw SocialAccountService.authenticationFailure();
        }
        Map<String, Object> attributes = providerUser.getAttributes();
        String providerUserId = stableGithubId(attributes.get("id"));
        String accessToken = userRequest.getAccessToken().getTokenValue();
        if (providerUserId == null || accessToken == null || accessToken.isBlank()) {
            throw SocialAccountService.authenticationFailure();
        }

        final List<GithubEmailClient.GithubEmail> emails;
        try {
            emails = githubEmailClient.findEmails(accessToken);
        } catch (RestClientException exception) {
            throw SocialAccountService.authenticationFailure();
        }
        String verifiedEmail = emails.stream()
            .filter(GithubEmailClient.GithubEmail::verified)
            .filter(email -> email.email() != null && !email.email().isBlank())
            .sorted(Comparator.comparing(
                GithubEmailClient.GithubEmail::primary,
                Comparator.reverseOrder()
            ))
            .map(GithubEmailClient.GithubEmail::email)
            .findFirst()
            .orElseThrow(SocialAccountService::authenticationFailure);

        String name = stringAttribute(attributes, "name");
        if (name == null) {
            name = stringAttribute(attributes, "login");
        }
        final User localUser;
        try {
            localUser = socialAccountService.resolveOrProvision(new SocialIdentity(
                OAuthProvider.GITHUB,
                providerUserId,
                verifiedEmail,
                true,
                name
            ));
        } catch (DataAccessException exception) {
            throw SocialAccountService.authenticationFailure();
        }
        return new LocalOAuth2User(providerUser, localUser);
    }

    private String stableGithubId(Object rawId) {
        if (rawId instanceof Number number) {
            String value = number.toString();
            return value.matches("[1-9][0-9]*") ? value : null;
        }
        if (rawId instanceof String value && value.matches("[1-9][0-9]*")) {
            return value;
        }
        return null;
    }

    private String stringAttribute(Map<String, Object> attributes, String name) {
        Object value = attributes.get(name);
        if (value instanceof String text && !text.isBlank()) {
            return text.trim();
        }
        return null;
    }
}
