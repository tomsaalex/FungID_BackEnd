package com.example.fungid.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class UserControllerTest {

    @Autowired
    private UserController userController;

    /*@BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }
*/
    @Test
    void test_getAllUsers() {
        assertThat(userController).isNotNull();
    }

    /*@Test
    void registerUser() {
    }

    @Test
    void loginUser() {

    }*/
}