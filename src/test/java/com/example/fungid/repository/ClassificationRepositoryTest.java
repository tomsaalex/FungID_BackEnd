package com.example.fungid.repository;

import com.example.fungid.domain.MushroomInstance;
import com.example.fungid.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@TestPropertySource("classpath:application-test.properties")
class ClassificationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClassificationRepository classificationRepository;

    private User user1, user2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@gmail.com");
        user1.setPassword("password1");

        user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@gmail.com");
        user2.setPassword("password2");

        entityManager.persist(user1);
        entityManager.persist(user2);

        MushroomInstance mushroomInstance1 = new MushroomInstance(user1, "mushroom1", "imageName1", LocalDateTime.now());
        MushroomInstance mushroomInstance2 = new MushroomInstance(user1, "mushroom2", "imageName2", LocalDateTime.now());
        MushroomInstance mushroomInstance3 = new MushroomInstance(user1, "mushroom3", "imageName3", LocalDateTime.now());

        entityManager.persist(mushroomInstance1);
        entityManager.persist(mushroomInstance2);
        entityManager.persist(mushroomInstance3);
    }

    @AfterEach
    void tearDown() {
        user1 = null;
        user2 = null;
    }

    @Test
    void test_findAllByUser_mushroomInstancesFound() {
        // Act
        List<MushroomInstance> mushroomInstances = classificationRepository.findAllByUser(user1);

        // Assert
        assertNotNull(mushroomInstances);
        assertEquals(mushroomInstances.size(), 3);
        assertEquals(mushroomInstances.get(0).getClassificationResult(), "mushroom1");
        assertEquals(mushroomInstances.get(1).getClassificationResult(), "mushroom2");
        assertEquals(mushroomInstances.get(2).getClassificationResult(), "mushroom3");
    }

    @Test
    void test_findAllByUser_noMushroomInstances() {
        // Act
        List<MushroomInstance> mushroomInstances = classificationRepository.findAllByUser(user2);

        // Assert
        assertNotNull(mushroomInstances);
        assertEquals(0, mushroomInstances.size());
    }
}