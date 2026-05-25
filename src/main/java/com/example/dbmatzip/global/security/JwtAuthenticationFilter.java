package com.example.dbmatzip.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = resolveToken(header);
        if (token != null) {
            try {
                var claims = jwtTokenProvider.parseClaims(token);
                Long userId = Long.parseLong(claims.getSubject());
                String loginId = claims.get("loginId", String.class);
                var principal = new MemberPrincipal(userId, loginId);
                var auth =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/api/v1/auth")
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private static String resolveToken(String header) {
        if (header == null) {
            return null;
        }
        String trimmed = header.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.startsWith("Bearer ")) {
            String bearerToken = trimmed.substring(7).trim();
            return bearerToken.isEmpty() ? null : bearerToken;
        }
        // Backward compatibility: allow raw JWT string in Authorization header.
        return trimmed.chars().filter(ch -> ch == '.').count() == 2 ? trimmed : null;
    }
}
