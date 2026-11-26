package com.taskmanager.service;

import com.taskmanager.entity.OAuthAccount;
import com.taskmanager.entity.User;
import com.taskmanager.enums.OAuthProvider;
import com.taskmanager.repository.OAuthAccountRepository;
import com.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class SocialAccountProvisioningIntegrationTest {

    @Autowired
    private SocialAccountService socialAccountService;

    @Autowired
    private OAuthAccountRepository oauthAccountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void provisionsFirstPartyUsersAndReusesStableProviderIdentity() {
        String suffix = UUID.randomUUID().toString();
        SocialIdentity googleIdentity = new SocialIdentity(
            OAuthProvider.GOOGLE,
            "google-" + suffix,
            " Google.User+" + suffix + "@example.com ",
            true,
            "Google User"
        );
        User googleUser = socialAccountService.resolveOrProvision(googleIdentity);
        User resolvedGoogle = socialAccountService.resolveOrProvision(googleIdentity);
        SocialIdentity githubIdentity = new SocialIdentity(
            OAuthProvider.GITHUB,
            "123456789",
            "github.user+" + suffix + "@example.com",
            true,
            "GitHub User"
        );
        User githubUser = socialAccountService.resolveOrProvision(githubIdentity);

        assertThat(resolvedGoogle.getId()).isEqualTo(googleUser.getId());
        assertThat(googleUser.getEmail()).isEqualTo(
            ("google.user+" + suffix + "@example.com").toLowerCase()
        );
        assertThat(googleUser.getPasswordHash()).startsWith("$2");
        assertThat(githubUser.getPasswordHash()).startsWith("$2");
        assertThat(githubUser.getPasswordHash()).isNotEqualTo(googleUser.getPasswordHash());
        assertThat(oauthAccountRepository.findAllByUser_Id(googleUser.getId()))
            .extracting(OAuthAccount::getProvider)
            .containsExactly(OAuthProvider.GOOGLE);
        assertThat(oauthAccountRepository.findAllByUser_Id(githubUser.getId()))
            .extracting(OAuthAccount::getProvider)
            .containsExactly(OAuthProvider.GITHUB);
    }

    @Test
    void rejectsEmailCollisionAndDisabledExistingIdentity() {
        String suffix = UUID.randomUUID().toString();
        String existingPasswordHash = passwordEncoder.encode("existing-local-password");
        User localUser = userRepository.saveAndFlush(new User(
            "Local User",
            "collision+" + suffix + "@example.com",
            existingPasswordHash
        ));
        assertThatThrownBy(() -> socialAccountService.resolveOrProvision(new SocialIdentity(
            OAuthProvider.GOOGLE,
            "collision-google-" + suffix,
            localUser.getEmail().toUpperCase(),
            true,
            "Provider Name"
        ))).isInstanceOf(OAuth2AuthenticationException.class)
            .hasMessage("Social authentication failed.");
        assertThat(localUser.getPasswordHash())
            .isEqualTo(existingPasswordHash);

        OAuthAccount identity = oauthAccountRepository.saveAndFlush(new OAuthAccount(
            localUser,
            OAuthProvider.GITHUB,
            "disabled-github-" + suffix
        ));
        localUser.setEnabled(false);
        userRepository.saveAndFlush(localUser);

        assertThatThrownBy(() -> socialAccountService.resolveOrProvision(new SocialIdentity(
            OAuthProvider.GITHUB,
            identity.getProviderUserId(),
            "another@example.com",
            true,
            "Different"
        ))).isInstanceOf(OAuth2AuthenticationException.class);
    }

    @Test
    void concurrentFirstLoginLeavesOneUserAndOneProviderIdentity() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String providerUserId = "concurrent-google-" + suffix;
        int requests = 8;
        CountDownLatch start = new CountDownLatch(1);
        List<User> successful = new ArrayList<>();

        try (ExecutorService executor = Executors.newFixedThreadPool(requests)) {
            var futures = java.util.stream.IntStream.range(0, requests)
                .mapToObj(index -> executor.submit(() -> {
                    start.await();
                    return socialAccountService.resolveOrProvision(new SocialIdentity(
                        OAuthProvider.GOOGLE,
                        providerUserId,
                        "concurrent+" + suffix + "-" + index + "@example.com",
                        true,
                        "Concurrent User"
                    ));
                }))
                .toList();
            start.countDown();
            for (var future : futures) {
                try {
                    successful.add(future.get());
                } catch (java.util.concurrent.ExecutionException exception) {
                    assertThat(exception.getCause())
                        .isInstanceOf(OAuth2AuthenticationException.class);
                }
            }
        }

        assertThat(successful).isNotEmpty();
        assertThat(oauthAccountRepository.findByProviderAndProviderUserId(
            OAuthProvider.GOOGLE,
            providerUserId
        )).isPresent();
        User winner = oauthAccountRepository.findByProviderAndProviderUserId(
            OAuthProvider.GOOGLE,
            providerUserId
        ).orElseThrow().getUser();
        long remainingUsers = java.util.stream.IntStream.range(0, requests)
            .mapToLong(index -> userRepository.countByEmailIgnoreCase(
                "concurrent+" + suffix + "-" + index + "@example.com"
            ))
            .sum();
        assertThat(remainingUsers).isEqualTo(1);
        assertThat(oauthAccountRepository.findAllByUser_Id(winner.getId())).hasSize(1);
    }
}
