package com.taskmanager.repository;

import com.taskmanager.entity.PasswordResetToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from PasswordResetToken token where token.tokenHash = :tokenHash")
    Optional<PasswordResetToken> findByTokenHashForUpdate(
        @Param("tokenHash") String tokenHash
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        update PasswordResetToken token
        set token.usedAt = :usedAt
        where token.user.id = :userId and token.usedAt is null
        """)
    int invalidateUnusedByUserId(
        @Param("userId") Long userId,
        @Param("usedAt") LocalDateTime usedAt
    );
}
