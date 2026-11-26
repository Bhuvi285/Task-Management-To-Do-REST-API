package com.taskmanager.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;

class GithubEmailClientImplTest {

    @Test
    void requestsGithubEmailListWithBearerToken() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.github.com/user/emails"))
            .andExpect(method(GET))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer github-test-token"))
            .andRespond(withSuccess("""
                [
                  {"email":"private@example.com","primary":true,"verified":true},
                  {"email":"other@example.com","primary":false,"verified":false}
                ]
                """, MediaType.APPLICATION_JSON));

        GithubEmailClient client = new GithubEmailClientImpl(builder);
        List<GithubEmailClient.GithubEmail> emails =
            client.findEmails("github-test-token");

        assertThat(emails)
            .containsExactly(
                new GithubEmailClient.GithubEmail(
                    "private@example.com",
                    true,
                    true
                ),
                new GithubEmailClient.GithubEmail(
                    "other@example.com",
                    false,
                    false
                )
            );
        server.verify();
    }
}
