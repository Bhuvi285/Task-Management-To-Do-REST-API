package com.taskmanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "refresh_tokens",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_refresh_tokens_token_hash",
        columnNames = "token_hash"
    ),
    indexes = {
        @Index(name = "idx_refresh_tokens_hash", columnList = "token_hash"),
        @Index(name = "idx_refresh_tokens_family", columnList = "token_family_id"),
        @Index(name = "idx_refresh_tokens_user", columnList = "user_id")
    }
)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, length = 43)
    private String tokenHash;

    @Column(name = "token_family_id", nullable = false, length = 36)
    private String tokenFamilyId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "replaced_by_token_id")
    private Long replacedByTokenId;

    @Version
    @Column(nullable = false)
    private Long version;

    protected RefreshToken() {
    }

    public RefreshToken(
        User user,
        String tokenHash,
        String tokenFamilyId,
        LocalDateTime expiresAt
    ) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.tokenFamilyId = tokenFamilyId;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    protected void populateCreatedAt() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void revoke(LocalDateTime revokedAt, Long replacedByTokenId) {
        this.revokedAt = revokedAt;
        this.replacedByTokenId = replacedByTokenId;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public String getTokenFamilyId() {
        return tokenFamilyId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public Long getReplacedByTokenId() {
        return replacedByTokenId;
    }

    public Long getVersion() {
        return version;
    }
}
