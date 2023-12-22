package com.boilerplate.userservice.user.application;

import com.boilerplate.userservice.user.dto.request.UserJoinRequest;
import com.boilerplate.userservice.user.exception.DuplicateEmailException;
import com.boilerplate.userservice.user.persistence.UserRepository;
import com.boilerplate.userservice.user.persistence.domain.Gender;
import com.boilerplate.userservice.user.persistence.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원을 추가한다.")
    void 회원을_추가한다() {
        // given
        UserJoinRequest userJoinRequest = new UserJoinRequest(
                "test@example.com", "password123", "testuser", "MALE", 25
        );
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword123");

        // when
        userService.join(userJoinRequest);

        // then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("중복된 이메일은 예외를 발생한다.")
    void 중복된_이메일은_예외를_발생한다() {
        // given
        UserJoinRequest userJoinRequest = new UserJoinRequest(
                "test@example.com", "password123", "testuser", "MALE", 25
        );
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // when
        // then
        Assertions.assertThrows(DuplicateEmailException.class, () -> {
            userService.join(userJoinRequest);
        }, "중복된 이메일이 예외를 발생");
    }
}