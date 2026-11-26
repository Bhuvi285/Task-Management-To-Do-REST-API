package com.taskmanager.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "app.security.oauth2.google.enabled=true",
    "app.security.oauth2.google.client-id=google-test-client",
    "app.security.oauth2.google.client-secret=google-test-secret",
    "app.security.oauth2.google.redirect-uri="
        + "http://localhost:8080/login/oauth2/code/google"
})
@AutoConfigureMockMvc
class OAuth2AuthorizationFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthorizationRequestRepository<OAuth2AuthorizationRequest>
        authorizationRequestRepository;

    @Test
    void authorizationRedirectStoresAndReturnsOAuthState() throws Exception {
        MvcResult result = mockMvc.perform(get("/oauth2/authorization/google"))
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string(
                "Location",
                org.hamcrest.Matchers.startsWith(
                    "https://accounts.google.com/o/oauth2/v2/auth?"
                )
            ))
            .andReturn();

        String location = result.getResponse().getHeader("Location");
        String state = UriComponentsBuilder.fromUriString(location)
            .build()
            .getQueryParams()
            .getFirst("state");
        state = URLDecoder.decode(state, StandardCharsets.UTF_8);
        assertThat(state).isNotBlank();
        var session = result.getRequest().getSession(false);
        assertThat(session).isNotNull();
        Object stored = session.getAttribute(
            HttpSessionOAuth2AuthorizationRequestRepository.class.getName()
                + ".AUTHORIZATION_REQUEST"
        );
        assertThat(stored).isInstanceOf(OAuth2AuthorizationRequest.class);
        assertThat(((OAuth2AuthorizationRequest) stored).getState()).isEqualTo(state);

        MockHttpServletRequest callbackRequest = new MockHttpServletRequest(
            "GET",
            "/login/oauth2/code/google"
        );
        callbackRequest.setSession(session);
        callbackRequest.setParameter("state", state);
        OAuth2AuthorizationRequest savedRequest =
            authorizationRequestRepository.loadAuthorizationRequest(callbackRequest);
        assertThat(savedRequest).isNotNull();
        assertThat(savedRequest.getState()).isEqualTo(state);
        assertThat(savedRequest.getRedirectUri())
            .isEqualTo("http://localhost:8080/login/oauth2/code/google");
        assertThat(location).doesNotContain("access_token", "refresh_token");

        mockMvc.perform(get("/login/oauth2/code/google")
                .session((MockHttpSession) session)
                .param("code", "test-authorization-code")
                .param("state", "mismatched-state"))
            .andExpect(status().isUnauthorized())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                .jsonPath("$.message").value("Social authentication failed."))
            .andExpect(header().string("Cache-Control", "no-store"))
            .andExpect(header().string("Pragma", "no-cache"))
            .andExpect(header().doesNotExist("Authorization"))
            .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.allOf(
                org.hamcrest.Matchers.containsString("JSESSIONID=;"),
                org.hamcrest.Matchers.containsString("Max-Age=0"),
                org.hamcrest.Matchers.containsString("HttpOnly"),
                org.hamcrest.Matchers.containsString("SameSite=lax")
            )));
        assertThatThrownBy(() -> session.getAttribute("invalidated"))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void callbackWithoutStateIsRejectedWithoutIssuingTokens() throws Exception {
        MvcResult authorization = mockMvc.perform(get("/oauth2/authorization/google"))
            .andExpect(status().is3xxRedirection())
            .andReturn();
        MockHttpSession session = (MockHttpSession) authorization.getRequest().getSession(false);

        mockMvc.perform(get("/login/oauth2/code/google")
                .session(session)
                .param("code", "test-authorization-code"))
            .andExpect(status().isUnauthorized())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                .jsonPath("$.message").value("Social authentication failed."))
            .andExpect(header().string("Cache-Control", "no-store"))
            .andExpect(header().string("Pragma", "no-cache"))
            .andExpect(header().doesNotExist("Location"))
            .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString(
                "JSESSIONID=;"
            )));
        assertThatThrownBy(() -> session.getAttribute("invalidated"))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void temporaryOAuthSessionDoesNotAuthenticateProtectedApiRequests() throws Exception {
        MvcResult authorization = mockMvc.perform(get("/oauth2/authorization/google"))
            .andExpect(status().is3xxRedirection())
            .andReturn();
        MockHttpSession session = (MockHttpSession) authorization.getRequest().getSession(false);

        mockMvc.perform(get("/api/v1/protected").session(session))
            .andExpect(status().isUnauthorized());
    }
}
