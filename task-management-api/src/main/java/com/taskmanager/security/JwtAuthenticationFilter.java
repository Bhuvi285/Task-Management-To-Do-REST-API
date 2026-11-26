package com.taskmanager.security;

import com.taskmanager.service.JwtService;
import com.taskmanager.exception.RestAuthenticationEntryPoint;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String REFRESH_PATH = "/api/v1/auth/refresh";
    private static final String LOGOUT_PATH = "/api/v1/auth/logout";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(
        JwtService jwtService,
        UserDetailsService userDetailsService,
        RestAuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return HttpMethod.POST.matches(request.getMethod())
            && (REFRESH_PATH.equals(request.getServletPath())
                || LOGOUT_PATH.equals(request.getServletPath()));
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication currentAuthentication =
            SecurityContextHolder.getContext().getAuthentication();
        if (currentAuthentication != null
            && currentAuthentication.isAuthenticated()
            && !(currentAuthentication instanceof AnonymousAuthenticationToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authorizationHeader == null || !hasBearerScheme(authorizationHeader)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            reject(request, response);
            return;
        }

        try {
            String subject = jwtService.extractSubject(token);
            if (subject == null || subject.isBlank()) {
                reject(request, response);
                return;
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(subject);
            if (!userDetails.isEnabled()) {
                SecurityContextHolder.clearContext();
                reject(request, response);
                return;
            }
            var authentication = new org.springframework.security.authentication
                .UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
            );
            authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException exception) {
            SecurityContextHolder.clearContext();
            reject(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean hasBearerScheme(String authorizationHeader) {
        return authorizationHeader.regionMatches(
            true,
            0,
            BEARER_PREFIX,
            0,
            BEARER_PREFIX.length()
        );
    }

    private void reject(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        authenticationEntryPoint.commence(
            request,
            response,
            new BadCredentialsException("Invalid bearer token.")
        );
    }
}
