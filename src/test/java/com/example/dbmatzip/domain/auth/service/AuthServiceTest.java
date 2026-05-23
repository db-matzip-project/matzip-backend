package com.example.dbmatzip.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.dbmatzip.domain.auth.dto.SignupRequest;
import com.example.dbmatzip.domain.member.entity.User;
import com.example.dbmatzip.domain.member.repository.UserRepository;
import com.example.dbmatzip.domain.preference.entity.Preference;
import com.example.dbmatzip.domain.preference.entity.UserPreference;
import com.example.dbmatzip.domain.preference.repository.PreferenceRepository;
import com.example.dbmatzip.domain.preference.repository.UserPreferenceRepository;
import com.example.dbmatzip.global.security.JwtProperties;
import com.example.dbmatzip.global.security.JwtTokenProvider;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PreferenceRepository preferenceRepository;

    @Mock
    private UserPreferenceRepository userPreferenceRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private AuthService authService;

    @Test
    void signup_withPreferenceIds_savesUserPreferences() {
        SignupRequest request = new SignupRequest(
                "member3",
                "Password123!",
                "홍길동",
                "01012345678",
                "길동이",
                24,
                List.of(1L, 3L));

        User savedUser = new User("member3", "encoded", "홍길동", "01012345678", "길동이", 24);
        ReflectionTestUtils.setField(savedUser, "id", 101L);
        Preference p1 = Preference.builder().id(1L).code("SPICY_HIGH").displayName("아주 매운 편").build();
        Preference p3 = Preference.builder().id(3L).code("SWEET").displayName("단 맛 선호").build();

        when(userRepository.existsByLoginId("member3")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(preferenceRepository.findAllById(anyCollection())).thenReturn(List.of(p1, p3));
        when(jwtTokenProvider.createAccessToken(savedUser)).thenReturn("token");
        when(jwtProperties.getExpirationMs()).thenReturn(86_400_000L);

        var result = authService.signup(request);

        assertThat(result.accessToken()).isEqualTo("token");
        ArgumentCaptor<List<UserPreference>> captor = ArgumentCaptor.forClass(List.class);
        verify(userPreferenceRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
        assertThat(captor.getValue().stream().map(up -> up.getPreference().getId())).containsExactly(1L, 3L);
    }
}
