package com.taskmanager.repository;

import com.taskmanager.entity.OAuthAccount;
import com.taskmanager.entity.User;
import com.taskmanager.enums.OAuthProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class OAuthAccountRepositoryTest {

    @Autowired
    private OAuthAccountRepository oauthAccountRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void persistsProviderIdentityAndFindsAccountsByProviderIdAndLocalUser() {
        User user = userRepository.saveAndFlush(
            new User("Test User", "oauth-repository@example.com", "bcrypt-hash")
        );
        OAuthAccount account = oauthAccountRepository.saveAndFlush(
            new OAuthAccount(user, OAuthProvider.GOOGLE, "stable-subject")
        );

        assertThat(oauthAccountRepository.findByProviderAndProviderUserId(
            OAuthProvider.GOOGLE,
            "stable-subject"
        )).get()
            .extracting(OAuthAccount::getId)
            .isEqualTo(account.getId());
        assertThat(oauthAccountRepository.existsByProviderAndProviderUserId(
            OAuthProvider.GOOGLE,
            "stable-subject"
        )).isTrue();
        assertThat(oauthAccountRepository.findAllByUser_Id(user.getId()))
            .containsExactly(account);
        assertThat(account.getCreatedAt()).isNotNull();
        assertThat(account.getUser()).isSameAs(user);
    }

    @Test
    void providerIdentityIsUniqueButSameIdMayExistWithDifferentProviders() {
        User firstUser = userRepository.saveAndFlush(
            new User("First", "oauth-provider-one@example.com", "bcrypt-hash")
        );
        User secondUser = userRepository.saveAndFlush(
            new User("Second", "oauth-provider-two@example.com", "bcrypt-hash")
        );
        oauthAccountRepository.saveAndFlush(
            new OAuthAccount(firstUser, OAuthProvider.GOOGLE, "provider-id")
        );
        oauthAccountRepository.saveAndFlush(
            new OAuthAccount(secondUser, OAuthProvider.GITHUB, "provider-id")
        );

        assertThatThrownBy(() -> oauthAccountRepository.saveAndFlush(
            new OAuthAccount(secondUser, OAuthProvider.GOOGLE, "provider-id")
        )).isInstanceOf(RuntimeException.class);
    }
}
