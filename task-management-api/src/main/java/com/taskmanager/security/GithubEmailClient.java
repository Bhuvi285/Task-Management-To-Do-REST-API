package com.taskmanager.security;

import java.util.List;

public interface GithubEmailClient {

    List<GithubEmail> findEmails(String accessToken);

    record GithubEmail(String email, boolean primary, boolean verified) {
    }
}
