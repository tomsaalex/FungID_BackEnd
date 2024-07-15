package com.example.fungid.service.user_service;

import com.example.fungid.domain.User;
import com.example.fungid.dto.UserDTO;
import com.example.fungid.exceptions.login.InvalidCredentialsException;
import com.example.fungid.exceptions.register.*;
import com.example.fungid.repository.UserRepository;
import com.example.fungid.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceUnitTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @Tag("Unit_Testing")
    void test_saveUser_saveSuccessful() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("usernameToInsert");
        userDTO.setEmail("emailToInsert");
        userDTO.setPassword("password");

        Mockito.when(userRepository.findByUsername(userDTO.getUsername())).thenReturn(null);
        Mockito.when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(null);
        Mockito.when(userRepository.saveAndFlush(Mockito.any(User.class))).thenReturn(new User(userDTO.getUsername(), userDTO.getEmail(), userDTO.getPassword()));

        // Act
        UserDTO addedUser = userService.saveUser(userDTO);

        // Assert
        assertEquals(userDTO.getUsername(), addedUser.getUsername());
        assertEquals(userDTO.getEmail(), addedUser.getEmail());
        assertEquals(userDTO.getPassword(), addedUser.getPassword());
        Mockito.verify(userRepository, Mockito.times(1)).saveAndFlush(Mockito.any(User.class));
    }

    @Test
    @Tag("Unit_Testing")
    void test_saveUser_invalidUsername() {
        // Arrange
        UserDTO userDTO_emptyUsername = new UserDTO();
        userDTO_emptyUsername.setUsername("");
        userDTO_emptyUsername.setEmail("emailToInsert1");
        userDTO_emptyUsername.setPassword("password");

        UserDTO userDTO_blankUsername = new UserDTO();
        userDTO_blankUsername.setUsername("  ");
        userDTO_blankUsername.setEmail("emailToInsert2");
        userDTO_blankUsername.setPassword("password");

        UserDTO userDT_nullUsername = new UserDTO();
        userDT_nullUsername.setUsername(null);
        userDT_nullUsername.setEmail("emailToInsert3");
        userDT_nullUsername.setPassword("password");

        // Act & Assert
        assertThrows(UncompletedFieldsException.class, () -> userService.saveUser(userDTO_emptyUsername));
        assertThrows(UncompletedFieldsException.class, () -> userService.saveUser(userDTO_blankUsername));
        assertThrows(UncompletedFieldsException.class, () -> userService.saveUser(userDT_nullUsername));
    }

    @Test
    @Tag("Unit_Testing")
    void test_saveUser_invalidEmail() {
        // Arrange
        UserDTO userDTO_emptyEmail = new UserDTO();
        userDTO_emptyEmail.setUsername("usernameToInsert1");
        userDTO_emptyEmail.setEmail("");
        userDTO_emptyEmail.setPassword("password");

        UserDTO userDTO_blankEmail = new UserDTO();
        userDTO_blankEmail.setUsername("usernameToInsert2");
        userDTO_blankEmail.setEmail("  ");
        userDTO_blankEmail.setPassword("password");

        UserDTO userDTO_nullEmail = new UserDTO();
        userDTO_nullEmail.setUsername("usernameToInsert3");
        userDTO_nullEmail.setEmail(null);
        userDTO_nullEmail.setPassword("password");

        // Act & Assert
        assertThrows(UncompletedFieldsException.class, () -> userService.saveUser(userDTO_emptyEmail));
        assertThrows(UncompletedFieldsException.class, () -> userService.saveUser(userDTO_blankEmail));
        assertThrows(UncompletedFieldsException.class, () -> userService.saveUser(userDTO_nullEmail));
    }

    @Test
    @Tag("Unit_Testing")
    void test_saveUser_invalidPassword() {
        // Arrange
        UserDTO userDTO_emptyPassword = new UserDTO();
        userDTO_emptyPassword.setUsername("usernameToInsert1");
        userDTO_emptyPassword.setEmail("emailToInsert1");
        userDTO_emptyPassword.setPassword("");

        UserDTO userDTO_blankPassword = new UserDTO();
        userDTO_blankPassword.setUsername("usernameToInsert2");
        userDTO_blankPassword.setEmail("emailToInsert2");
        userDTO_blankPassword.setPassword("  ");

        UserDTO userDTO_nullPassword = new UserDTO();
        userDTO_nullPassword.setUsername("usernameToInsert3");
        userDTO_nullPassword.setEmail("emailToInsert3");
        userDTO_nullPassword.setPassword(null);

        // Act & Assert
        assertThrows(UncompletedFieldsException.class, () -> userService.saveUser(userDTO_emptyPassword));
        assertThrows(UncompletedFieldsException.class, () -> userService.saveUser(userDTO_blankPassword));
        assertThrows(UncompletedFieldsException.class, () -> userService.saveUser(userDTO_nullPassword));
    }

    @Test
    @Tag("Unit_Testing")
    void test_saveUser_UsernameTooLong() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("usernameToInsert".repeat(4));
        userDTO.setEmail("emailToInsert");
        userDTO.setPassword("password");

        // Act & Assert
        assertThrows(UsernameLengthExceededException.class, () -> userService.saveUser(userDTO));
    }

    @Test
    @Tag("Unit_Testing")
    void test_save_User_EmailTooLong() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("usernameToInsert");
        userDTO.setEmail("emailToInsert".repeat(4));
        userDTO.setPassword("password");

        // Act & Assert
        assertThrows(EmailLengthExceededException.class, () -> userService.saveUser(userDTO));
    }

    @Test
    @Tag("Unit_Testing")
    void test_saveUser_PasswordTooLong() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("usernameToInsert");
        userDTO.setEmail("emailToInsert");
        userDTO.setPassword("password".repeat(7));

        // Act & Assert
        assertThrows(PasswordLengthExceededException.class, () -> userService.saveUser(userDTO));
    }

    @Test
    @Tag("Unit_Testing")
    void test_saveUser_usernameTaken() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("usernameToInsert");
        userDTO.setEmail("emailToInsert");
        userDTO.setPassword("password");

        Mockito.when(userRepository.findByUsername(userDTO.getUsername())).thenReturn(new User(userDTO.getUsername(), "random_email", "random_password"));

        // Act & Assert
        assertThrows(UsernameTakenException.class, () -> userService.saveUser(userDTO));
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(userDTO.getUsername());
    }

    @Test
    @Tag("Unit_Testing")
    void test_saveUser_emailTaken() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("usernameToInsert");
        userDTO.setEmail("emailToInsert");
        userDTO.setPassword("password");

        Mockito.when(userRepository.findByUsername(userDTO.getUsername())).thenReturn(null);
        Mockito.when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(new User("random_username", userDTO.getEmail(), "random_password"));

        // Act & Assert
        assertThrows(EmailTakenException.class, () -> userService.saveUser(userDTO));
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(userDTO.getUsername());
        Mockito.verify(userRepository, Mockito.times(1)).findByEmail(userDTO.getEmail());
    }

    @Test
    @Tag("Unit_Testing")
    void test_loadUserByCredentials_loadedSuccessfully() {
        // Arrange
        User user = new User("username", "email", "password");

        Mockito.when(userRepository.findByUsername(user.getUsername())).thenReturn(user);

        // Act
        UserDTO loadedUser = userService.loadUserByCredentials(user.getUsername(), user.getPassword());

        // Assert
        assertEquals(user.getUsername(), loadedUser.getUsername());
        assertEquals(user.getEmail(), loadedUser.getEmail());
        assertEquals(user.getPassword(), loadedUser.getPassword());
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(user.getUsername());
    }

    @Test
    @Tag("Unit_Testing")
    void test_loadUserByCredentials_invalidUsername() {
        // Arrange
        String emptyUsername = "";
        String blankUsername = "  ";
        String nullUsername = null;

        String password = "password";

        // Act & Assert
        assertThrows(UncompletedFieldsException.class, () -> userService.loadUserByCredentials(emptyUsername, password));
        assertThrows(UncompletedFieldsException.class, () -> userService.loadUserByCredentials(blankUsername, password));
        assertThrows(UncompletedFieldsException.class, () -> userService.loadUserByCredentials(nullUsername, password));
    }

    @Test
    @Tag("Unit_Testing")
    void test_loadUserByCredentials_invalidPassword() {
        // Arrange
        String username = "username";

        String emptyPassword = "";
        String blankPassword = "  ";
        String nullPassword = null;

        // Act & Assert
        assertThrows(UncompletedFieldsException.class, () -> userService.loadUserByCredentials(username, emptyPassword));
        assertThrows(UncompletedFieldsException.class, () -> userService.loadUserByCredentials(username, blankPassword));
        assertThrows(UncompletedFieldsException.class, () -> userService.loadUserByCredentials(username, nullPassword));
    }

    @Test
    @Tag("Unit_Testing")
    void test_loadUserByCredentials_invalidCredentials() {
        // Arrange
        String usernameWithNoUser = "username";
        String usernameWithUser = "username2";

        String invalidPassword = "invalid_password";
        String validPassword = "valid_password";

        Mockito.when(userRepository.findByUsername(usernameWithNoUser)).thenReturn(null);
        Mockito.when(userRepository.findByUsername(usernameWithUser)).thenReturn(new User(usernameWithUser, "random_email", "valid_password"));

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> userService.loadUserByCredentials(usernameWithNoUser, invalidPassword));
        assertThrows(InvalidCredentialsException.class, () -> userService.loadUserByCredentials(usernameWithNoUser, validPassword));
        assertThrows(InvalidCredentialsException.class, () -> userService.loadUserByCredentials(usernameWithUser, invalidPassword));

        Mockito.verify(userRepository, Mockito.times(2)).findByUsername(usernameWithNoUser);
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(usernameWithUser);
    }

    @Test
    @Tag("Unit_Testing")
    void test_getUser_userFound() {
        // Arrange
        Long idWithUser = 1L;
        User user = new User("username", "email", "password");

        Mockito.when(userRepository.findById(idWithUser)).thenReturn(Optional.of(user));

        // Act
        User foundUser = userService.getUser(idWithUser);

        // Assert
        assertEquals(user, foundUser);
        Mockito.verify(userRepository, Mockito.times(1)).findById(idWithUser);
    }

    @Test
    @Tag("Unit_Testing")
    void test_getUser_userNotFound() {
        // Arrange
        Long idWithNoUser = 2L;

        Mockito.when(userRepository.findById(idWithNoUser)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> userService.getUser(idWithNoUser));
        Mockito.verify(userRepository, Mockito.times(1)).findById(idWithNoUser);
    }

    @Test
    @Tag("Unit_Testing")
    void test_getUserByUsername_userNotFound() {
        // Arrange
        String usernameWithNoUser = "username";

        Mockito.when(userRepository.findByUsername(usernameWithNoUser)).thenReturn(null);

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> userService.getUserByUsername(usernameWithNoUser));
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(usernameWithNoUser);
    }

    @Test
    @Tag("Unit_Testing")
    void test_getUserByUsername_userFound() {
        // Arrange
        String usernameWithUser = "username2";

        User user = new User(usernameWithUser, "random_email", "valid_password");

        Mockito.when(userRepository.findByUsername(usernameWithUser)).thenReturn(user);

        // Act
        User foundUser = userService.getUserByUsername(usernameWithUser);

        // Assert
        assertEquals(user, foundUser);
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(usernameWithUser);
    }

    @Test
    @Tag("Unit_Testing")
    void test_mapToDTO() {
        // Arrange
        User user = new User("username", "email", "password");
        user.setId(1L);

        // Act
        UserDTO userDTO = userService.mapToDTO(user);

        // Assert
        assertEquals(user.getId(), userDTO.getId());
        assertEquals(user.getUsername(), userDTO.getUsername());
        assertEquals(user.getEmail(), userDTO.getEmail());
        assertEquals(user.getPassword(), userDTO.getPassword());
    }

    @Test
    @Tag("Unit_Testing")
    void test_mapToEntity() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("username");
        userDTO.setEmail("email");
        userDTO.setPassword("password");

        // Act
        User user = userService.mapToEntity(userDTO);

        // Assert
        assertEquals(userDTO.getId(), user.getId());
        assertEquals(userDTO.getUsername(), user.getUsername());
        assertEquals(userDTO.getEmail(), user.getEmail());
        assertEquals(userDTO.getPassword(), user.getPassword());
    }
}