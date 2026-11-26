package com.taskmanager.security;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class GithubEmailClientImpl implements GithubEmailClient {

    private static final ParameterizedTypeReference<List<GithubEmail>> EMAIL_LIST_TYPE =
        new ParameterizedTypeReference<>() {
        };

    private final RestClient restClient;

    public GithubEmailClientImpl(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
            .baseUrl("https://api.github.com")
            .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
            .build();
    }

    @Override
    public List<GithubEmail> findEmails(String accessToken) {
        List<GithubEmail> emails = restClient.get()
            .uri("/user/emails")
            .headers(headers -> headers.setBearerAuth(accessToken))
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(EMAIL_LIST_TYPE);
        return emails == null ? List.of() : emails;
    }
}
