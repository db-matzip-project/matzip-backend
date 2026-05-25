package com.example.dbmatzip.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private static final String RESTAURANT_FROM_PLACE_PATH = "/api/v1/restaurants/from-place";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getServletPath();
        boolean isFromPlaceRequest =
                RESTAURANT_FROM_PLACE_PATH.equals(path) || (RESTAURANT_FROM_PLACE_PATH + "/").equals(path);
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = resolveToken(header);

        if (isFromPlaceRequest) {
            log.info(
                    "[JWT] from-place request received: method={}, hasAuthorizationHeader={}, tokenResolved={}",
                    request.getMethod(),
                    header != null && !header.isBlank(),
                    token != null);
        }

        if (header != null && token == null) {
            log.warn("[JWT] Authorization header exists but token resolution failed: path={}", path);
        }

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
                if (isFromPlaceRequest) {
                    log.info("[JWT] from-place authentication success: userId={}, loginId={}", userId, loginId);
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                log.warn(
                        "[JWT] token parse failed: path={}, reason={} ({})",
                        path,
                        e.getMessage(),
                        e.getClass().getSimpleName());
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
        if (trimmed.length() >= 7 && trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String bearerToken = trimmed.substring(7).trim();
            return bearerToken.isEmpty() ? null : bearerToken;
        }
        // Backward compatibility: allow raw JWT string in Authorization header.
        return trimmed.chars().filter(ch -> ch == '.').count() == 2 ? trimmed : null;
    }
}
