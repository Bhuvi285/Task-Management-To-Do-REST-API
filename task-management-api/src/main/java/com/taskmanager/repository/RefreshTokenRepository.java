package com.taskmanager.repository;

import com.taskmanager.entity.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from RefreshToken token where token.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    List<RefreshToken> findByTokenFamilyId(String tokenFamilyId);

    @Query("""
        select count(token) from RefreshToken token
        where token.tokenFamilyId = :tokenFamilyId and token.revokedAt is null
        """)
    long countActiveByTokenFamilyId(@Param("tokenFamilyId") String tokenFamilyId);

    @Modifying
    @Query("""
        update RefreshToken token
        set token.revokedAt = :revokedAt, token.version = token.version + 1
        where token.tokenFamilyId = :tokenFamilyId and token.revokedAt is null
        """)
    int revokeActiveByTokenFamilyId(
        @Param("tokenFamilyId") String tokenFamilyId,
        @Param("revokedAt") LocalDateTime revokedAt
    );

    @Modifying
    @Query("""
        update RefreshToken token
        set token.revokedAt = :revokedAt, token.version = token.version + 1
        where token.user.id = :userId and token.revokedAt is null
        """)
    int revokeActiveByUserId(
        @Param("userId") Long userId,
        @Param("revokedAt") LocalDateTime revokedAt
    );
}
