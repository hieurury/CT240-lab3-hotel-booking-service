package com.ctu.service;

import com.ctu.model.User;
import com.ctu.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Test Suite")
class UserServiceTest {

    @Mock
    private UserRepository userRepository; // Doi tuong gia lap CSDL

    @Mock
    private EmailService emailService; // Doi tuong gia lap Email API

    @InjectMocks
    private UserService userService; // Tiem 2 mock objects vao UserService

    @Test
    @DisplayName("Dang ky nguoi dung moi thanh cong - Gia lap Repository & EmailService")
    void testRegisterUser_Success() {
        // ARRANGE: Gia lap hanh vi (Stubbing)
        User newUser = new User("U001", "An", "an@ctu.edu.vn");
        when(userRepository.existsByEmail("an@ctu.edu.vn")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // ACT
        boolean result = userService.registerUser(newUser);

        // ASSERT
        assertTrue(result);
        verify(userRepository, times(1)).save(newUser);
        verify(emailService, times(1)).sendWelcomeEmail("an@ctu.edu.vn");
    }

    @Test
    @DisplayName("Dang ky nguoi dung loi do Email da ton tai - Ngung gui email")
    void testRegisterUser_DuplicateEmail_ThrowsException() {
        // ARRANGE
        User duplicateUser = new User("U002", "Binh", "binh@ctu.edu.vn");
        when(userRepository.existsByEmail("binh@ctu.edu.vn")).thenReturn(true);

        // ACT & ASSERT
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(duplicateUser);
        });
        assertEquals("Email da ton tai!", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendWelcomeEmail(anyString());
    }

    @Test
    @DisplayName("Dang ky nguoi dung loi khi doi tuong user null")
    void testRegisterUser_NullUser_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(null);
        });
        assertEquals("Thong tin nguoi dung khong hop le!", exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendWelcomeEmail(anyString());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("Dang ky nguoi dung loi khi email null hoac rong/khoang trang")
    void testRegisterUser_InvalidEmail_ThrowsException(String email) {
        User user = new User("U003", "Cuong", email);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(user);
        });
        assertEquals("Thong tin nguoi dung khong hop le!", exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendWelcomeEmail(anyString());
    }

    @Test
    @DisplayName("Kiem tra getter cua User model")
    void testUserGetters() {
        User user = new User("U005", "Em", "em@ctu.edu.vn");
        assertEquals("U005", user.getId());
        assertEquals("Em", user.getName());
        assertEquals("em@ctu.edu.vn", user.getEmail());
    }
}
