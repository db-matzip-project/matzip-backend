package com.example.dbmatzip.domain.auth.service;

import com.example.dbmatzip.domain.auth.dto.LoginRequest;
import com.example.dbmatzip.domain.auth.dto.SignupRequest;
import com.example.dbmatzip.domain.member.dto.AuthTokenResponse;
import com.example.dbmatzip.domain.member.dto.UserProfileResponse;
import com.example.dbmatzip.domain.member.entity.User;
import com.example.dbmatzip.domain.member.exception.DuplicateLoginIdException;
import com.example.dbmatzip.domain.member.repository.UserRepository;
import com.example.dbmatzip.global.security.JwtProperties;
import com.example.dbmatzip.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    @Transactional
    public AuthTokenResponse signup(SignupRequest request) {
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new DuplicateLoginIdException(request.loginId());
        }
        User user = new User(
                request.loginId(),
                passwordEncoder.encode(request.password()),
                request.name(),
                request.phone(),
                request.nickname(),
                request.age());
        userRepository.save(user);
        return tokenResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthTokenResponse login(LoginRequest request) {
        User user =
                userRepository.findByLoginId(request.loginId()).orElseThrow(() -> new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다."));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        return tokenResponse(user);
    }

    private AuthTokenResponse tokenResponse(User user) {
        String token = jwtTokenProvider.createAccessToken(user);
        return AuthTokenResponse.of(token, jwtProperties.getExpirationMs(), UserProfileResponse.from(user));
    }
}
