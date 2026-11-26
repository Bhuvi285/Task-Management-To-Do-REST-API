package com.taskmanager.repository;

import com.taskmanager.entity.OAuthAccount;
import com.taskmanager.enums.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {

    Optional<OAuthAccount> findByProviderAndProviderUserId(
        OAuthProvider provider,
        String providerUserId
    );

    boolean existsByProviderAndProviderUserId(
        OAuthProvider provider,
        String providerUserId
    );

    List<OAuthAccount> findAllByUser_Id(Long userId);
}
