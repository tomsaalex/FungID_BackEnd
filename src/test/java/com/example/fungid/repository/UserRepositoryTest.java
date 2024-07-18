package com.example.fungid.repository;

import com.example.fungid.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource("classpath:application-test.properties")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@gmail.com");
        user1.setPassword("password1");

        entityManager.persist(user1);
    }

    @AfterEach
    void tearDown() {
        entityManager.clear();
    }

    @Test
    @Tag("Integration_Testing")
    void test_findByUsername_userFound() {
        // Arrange
        String usernameToFind = "user1";

        // Act
        User foundUser = userRepository.findByUsername(usernameToFind);

        // Assert
        assertNotNull(foundUser);
        assertEquals(usernameToFind, foundUser.getUsername());
        assertNotNull(foundUser.getId());
    }

    @Test
    @Tag("Integration_Testing")
    void test_findByUsername_userNotFound() {
        // Arrange
        String usernameToFind = "userX";

        // Act
        User foundUser = userRepository.findByUsername(usernameToFind);

        // Assert
        assertNull(foundUser);
    }

    @Test
    @Tag("Integration_Testing")
    void test_findByEmail_userFound() {
        // Arrange
        String emailToFind = "user1@gmail.com";

        // Act
        User foundUser = userRepository.findByEmail(emailToFind);

        // Assert
        assertNotNull(foundUser);
        assertEquals(emailToFind, foundUser.getEmail());
        assertNotNull(foundUser.getId());
    }

    @Test
    @Tag("Integration_Testing")
    void test_findByEmail_userNotFound() {
        // Arrange
        String emailToFind = "userX@gmail.com";

        // Act
        User foundUser = userRepository.findByEmail(emailToFind);

        // Assert
        assertNull(foundUser);
    }
}