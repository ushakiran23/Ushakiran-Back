package com.travelu.backend.security;

import com.travelu.backend.entity.User;
import com.travelu.backend.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserService userService;

    public JwtAuthFilter(JwtUtils jwtUtils, UserService userService) {
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Expect header:  Authorization: Bearer <token>
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token -> just continue, request remains unauthenticated
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // Optional: validate token signature & expiry first
            if (!jwtUtils.validateToken(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            String email = jwtUtils.extractEmail(token);

            // Set authentication only if not already set
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                User user = userService.findByEmail(email); // must return null if not found

                if (user != null) {
                    // Principal ga user object / email ivvachu
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    user,                // principal
                                    null,                // no credentials
                                    Collections.emptyList() // authorities (add roles later if needed)
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

        } catch (Exception ex) {
            // Invalid / expired token -> ignore and continue without auth
            // (no need to throw, just don't set security context)
        }

        filterChain.doFilter(request, response);
    }
}
