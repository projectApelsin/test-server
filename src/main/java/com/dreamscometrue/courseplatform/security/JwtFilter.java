package com.dreamscometrue.courseplatform.security;

import com.dreamscometrue.courseplatform.service.PlatformUserService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private PlatformUserService platformUserService;

    @Autowired
    private JwtUtility jwtUtility;

    // Define public endpoints that don't require JWT
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/login",
            "/register",
            "/public",
            "/error"  // Добавили /error чтобы избежать редиректов
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Skip JWT processing for public endpoints
        if (isPublicEndpoint(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip if already authenticated
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 1. Get tokens from cookies
            String accessToken = getTokenFromCookie(request, "accessToken");
            String refreshToken = getTokenFromCookie(request, "refreshToken");

            // 2. Try to authenticate with access token first
            if (accessToken != null) {
                String username = jwtUtility.extractUsername(accessToken);
                if (username != null) {
                    UserDetails userDetails = platformUserService.loadUserByUsername(username);

                    if (jwtUtility.validateToken(accessToken, userDetails)) {
                        // Valid access token - authenticate user
                        setAuthentication(userDetails, request);
                        filterChain.doFilter(request, response);
                        return;
                    }
                }
            }

            // 3. If access token invalid but refresh token exists, try to refresh
            if (refreshToken != null) {
                String username = jwtUtility.extractUsername(refreshToken);
                if (username != null) {
                    UserDetails userDetails = platformUserService.loadUserByUsername(username);

                    if (jwtUtility.validateToken(refreshToken, userDetails)) {
                        // Valid refresh token - generate new access token
                        String newAccessToken = jwtUtility.generateAccessToken(username);

                        // ВАЖНО: Создаем wrapper для response, чтобы не было конфликта с выводом
                        HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(response);

                        Cookie newAccessCookie = new Cookie("accessToken", newAccessToken);
                        newAccessCookie.setHttpOnly(true);
                        newAccessCookie.setPath("/");
                        newAccessCookie.setMaxAge(60 * 15); // 15 minutes
                        responseWrapper.addCookie(newAccessCookie);

                        // Authenticate user
                        setAuthentication(userDetails, request);

                        filterChain.doFilter(request, responseWrapper);
                        return;
                    }
                }
            }
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token expired: " + e.getMessage());
        } catch (Exception e) {
            logger.warn("JWT processing failed: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String requestPath) {
        return PUBLIC_ENDPOINTS.stream()
                .anyMatch(endpoint -> requestPath.equals(endpoint) || requestPath.startsWith(endpoint + "/"));
    }

    private void setAuthentication(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private String getTokenFromCookie(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}