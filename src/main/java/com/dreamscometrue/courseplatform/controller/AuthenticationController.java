package com.dreamscometrue.courseplatform.controller;

import com.dreamscometrue.courseplatform.DTO.LoginRequest;
import com.dreamscometrue.courseplatform.security.JwtUtility;
import com.dreamscometrue.courseplatform.service.PlatformUserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.management.remote.JMXAuthenticator;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class AuthenticationController {
    @Autowired
    private PlatformUserService platformUserService;
    @Autowired
    private JwtUtility jwtUtility;
    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest loginRequest) {
        platformUserService.register(loginRequest.getUsername(), loginRequest.getPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        final UserDetails userDetails = platformUserService
                .loadUserByUsername(loginRequest.getUsername());
        final String accessToken = jwtUtility.generateAccessToken(userDetails.getUsername());
        final String refreshToken = jwtUtility.generateRefreshToken(userDetails.getUsername());

        Cookie accessCookie = new Cookie("accessToken", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60 * 15); // 15 minutes

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(60 * 60 * 24 * 7); // 7 days

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        // Return a success message or user info instead of the response object
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Login successful");
        responseBody.put("username", userDetails.getUsername());

        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerSession(@RequestBody LoginRequest loginRequest,
                                             HttpServletRequest request) {
        try {
            // Перевірка, чи користувач вже існує
            try {
                platformUserService.loadUserByUsername(loginRequest.getUsername());
                // Якщо користувач знайдений, значить він вже існує
                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("success", false);
                responseBody.put("message", "User already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(responseBody);
            } catch (UsernameNotFoundException e) {
                // Це нормально - користувач не існує, можемо реєструвати
            }

            // Реєстрація нового користувача
            platformUserService.register(loginRequest.getUsername(), loginRequest.getPassword());

            // Автоматичний вхід після реєстрації
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Створення сесії
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());
            session.setAttribute("username", loginRequest.getUsername());
            session.setAttribute("loginTime", System.currentTimeMillis());

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("message", "Registration successful");
            responseBody.put("username", loginRequest.getUsername());
            responseBody.put("redirectUrl", "/dashboard");

            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", false);
            responseBody.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        }
    }

    // Logout endpoint для сесій
    @PostMapping("/logout")
    public ResponseEntity<?> logoutSession(HttpServletRequest request,
                                           HttpServletResponse response) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            SecurityContextHolder.clearContext();

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("message", "Logout successful");
            responseBody.put("redirectUrl", "/login");

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", false);
            responseBody.put("message", "Logout failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        }
    }
}
