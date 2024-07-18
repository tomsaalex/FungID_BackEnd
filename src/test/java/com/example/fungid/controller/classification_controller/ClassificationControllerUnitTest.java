package com.example.fungid.controller.classification_controller;

import com.example.fungid.configuration.SecurityConfig;
import com.example.fungid.controller.ClassificationController;
import com.example.fungid.domain.MushroomInstance;
import com.example.fungid.domain.User;
import com.example.fungid.dto.MushroomClassificationDTO;
import com.example.fungid.exceptions.mushroom_id.*;
import com.example.fungid.service.ClassificationService;
import com.example.fungid.service.JwtService;
import com.example.fungid.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(ClassificationController.class)
@Import(SecurityConfig.class)
class ClassificationControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private ClassificationService classificationService;

    User expectedUser;

    @BeforeEach
    void setUp() {
        expectedUser = new User();
        expectedUser.setId(1L);
        expectedUser.setUsername("testUser");
        expectedUser.setPassword("testPassword");
        expectedUser.setEmail("testEmail");
    }

    @Test
    void test_identifyMushroom_classificationSuccessful() throws Exception {
        //Arrange

        MushroomClassificationDTO expectedMushroom = new MushroomClassificationDTO(1L, "valid_result", "2024-06-06-00-00-00-000");

        MockMultipartFile mushroomImage = new MockMultipartFile("mushroomImage", "mushroomImage.jpg", "image/jpeg", "mushroomImage".getBytes());

        Mockito.when(userService.getUser(1L)).thenReturn(expectedUser);
        Mockito.when(classificationService.classifyMushroom(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(expectedMushroom);

        Mockito.when(jwtService.extractUsername(Mockito.any())).thenReturn(expectedUser.getUsername());
        Mockito.when(userService.getUserByUsername(Mockito.any())).thenReturn(expectedUser);

        //Act

        //String mushroomReqBody =
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/classifications/identify")
                        .file(mushroomImage)
                        .param("mushroomDate", expectedMushroom.sampleTakenAt)
                        .header("Authorization", "Bearer token MOCKED")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.mushroomInstanceId").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.classificationResult").value("valid_result"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sampleTakenAt").value("2024-06-06-00-00-00-000"));

        //Assert
        Mockito.verify(userService, Mockito.times(1)).getUser(1L);
        Mockito.verify(classificationService, Mockito.times(1)).classifyMushroom(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void test_identifyMushroom_invalidDate() throws Exception {
        //Arrange

        MockMultipartFile mushroomImage = new MockMultipartFile("mushroomImage", "mushroomImage.jpg", "image/jpeg", "mushroomImage".getBytes());

        Mockito.when(jwtService.extractUsername(Mockito.any())).thenReturn(expectedUser.getUsername());
        Mockito.when(userService.getUserByUsername(Mockito.any())).thenReturn(expectedUser);

        //Act

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/classifications/identify")
                        .file(mushroomImage)
                        .param("mushroomDate", "invalid_date")
                        .header("Authorization", "Bearer token MOCKED")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                        .andExpect(MockMvcResultMatchers.content().string("The date provided is not in the correct format. Please provide a date in the format yyyy-MM-dd-HH-mm-ss-SSS."));
    }

    @Test
    void test_identifyMushroom_emptyImage() throws Exception {
        // Arrange

        MockMultipartFile mushroomImage = new MockMultipartFile("mushroomImage", "mushroomImage.jpg", "image/jpeg", new byte[0]);
        String mushroomDate = "2024-06-06-00-00-00-000";

        Mockito.when(jwtService.extractUsername(Mockito.any())).thenReturn(expectedUser.getUsername());
        Mockito.when(userService.getUserByUsername(expectedUser.getUsername())).thenReturn(expectedUser);
        Mockito.when(classificationService.classifyMushroom(Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(new MushroomImageMissingException("You must provide an image of the mushroom."));

        // Act
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/classifications/identify")
                        .file(mushroomImage)
                        .param("mushroomDate", mushroomDate)
                        .header("Authorization", "Bearer token MOCKED")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("You must provide an image of the mushroom."));

    }

    @Test
    void test_identifyMushroom_unsupportedContentType() throws Exception {
        // Arrange

        MockMultipartFile mushroomImageTypeInvalid = new MockMultipartFile("mushroomImage", "mushroomImage.jpg", "image/gif", "mushroomImage".getBytes());
        MockMultipartFile mushroomImageTypeNull = new MockMultipartFile("mushroomImage", "mushroomImage.jpg", null, "mushroomImage".getBytes());

        Mockito.when(jwtService.extractUsername(Mockito.any())).thenReturn(expectedUser.getUsername());
        Mockito.when(userService.getUserByUsername(expectedUser.getUsername())).thenReturn(expectedUser);
        Mockito.when(classificationService.classifyMushroom(Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(new ImageTypeNotSupportedException("Only PNG and JPG images are supported."));

        String mushroomDate = "2024-06-06-00-00-00-000";

        // Act
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/classifications/identify")
                        .file(mushroomImageTypeInvalid)
                        .param("mushroomDate", mushroomDate)
                        .header("Authorization", "Bearer token MOCKED")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Only PNG and JPG images are supported."));

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/classifications/identify")
                        .file(mushroomImageTypeNull)
                        .param("mushroomDate", mushroomDate)
                        .header("Authorization", "Bearer token MOCKED")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Only PNG and JPG images are supported."));
    }

    @Test
    void test_identifyMushroom_classificationError() throws Exception {
        // Arrange

        MushroomClassificationDTO expectedMushroom = new MushroomClassificationDTO(1L, "valid_result", "2024-06-06-00-00-00-000");

        MockMultipartFile mushroomImage = new MockMultipartFile("mushroomImage", "mushroomImage.jpg", "image/jpeg", "mushroomImage".getBytes());

        Mockito.when(jwtService.extractUsername(Mockito.any())).thenReturn(expectedUser.getUsername());
        Mockito.when(userService.getUserByUsername(expectedUser.getUsername())).thenReturn(expectedUser);
        Mockito.when(classificationService.classifyMushroom(Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(new MushroomImageProcessingException("Error processing mushroom image."));

        // Act

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/classifications/identify")
                        .file(mushroomImage)
                        .param("mushroomDate", expectedMushroom.sampleTakenAt)
                        .header("Authorization", "Bearer token MOCKED")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.content().string("There was a problem processing the image of the mushroom. Please try again later."));
    }

    @Test
    void test_getAllClassifiedMushroomsForUser() throws Exception {
        // Arrange

        Mockito.when(jwtService.extractUsername(Mockito.any())).thenReturn(expectedUser.getUsername());
        Mockito.when(userService.getUserByUsername(expectedUser.getUsername())).thenReturn(expectedUser);
        Mockito.when(classificationService.getAllMushroomInstancesForUser(Mockito.any())).thenReturn(
                List.of(
                        new MushroomClassificationDTO(1L, "valid_result", "2024-06-06-00-00-00-000"),
                        new MushroomClassificationDTO(2L, "valid_result", "2024-06-06-00-00-00-000")
                )
        );

        // Act

        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/classifications/mushroom-instances")
                        .header("Authorization", "Bearer token MOCKED")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].mushroomInstanceId").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].classificationResult").value("valid_result"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].mushroomInstanceId").value(2L))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].classificationResult").value("valid_result"));
    }

    @Test
    void test_getMushroomClassificationImage_imageFound() throws Exception {
        // Arrange

        MushroomInstance expectedMushroom = new MushroomInstance(expectedUser, "valid_result", "mushroomImage.jpg", LocalDateTime.now());
        byte[] expectedImage = "mushroomImage".getBytes();

        Mockito.when(jwtService.extractUsername(Mockito.any())).thenReturn(expectedUser.getUsername());
        Mockito.when(userService.getUserByUsername(expectedUser.getUsername())).thenReturn(expectedUser);
        Mockito.when(classificationService.getMushroomInstanceForUser(Mockito.any(), Mockito.any())).thenReturn(expectedMushroom);
        Mockito.when(classificationService.getImage(Mockito.any(), Mockito.any())).thenReturn(expectedImage);

        // Act
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/classifications/images/1")
                        .header("Authorization", "Bearer token MOCKED")
                        .accept(MediaType.IMAGE_JPEG)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().bytes(expectedImage));
    }

    @Test
    void test_getMushroomClassificationImage_mushroomNotFound() throws Exception {
        // Arrange
        Mockito.when(jwtService.extractUsername(Mockito.any())).thenReturn(expectedUser.getUsername());
        Mockito.when(userService.getUserByUsername(expectedUser.getUsername())).thenReturn(expectedUser);

        Mockito.when(classificationService.getMushroomInstanceForUser(Mockito.any(), Mockito.any())).thenThrow(new MushroomNotFoundException("Your account does not appear to have a mushroom classification job with the given ID."));

        // Act
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/classifications/images/1")
                        .header("Authorization", "Bearer token MOCKED")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string("Your account does not appear to have a mushroom classification job with the given ID."));
    }

    @Test
    void test_getMushroomClassificationImage_mushroomImageRetrievalError() throws Exception {
        // Arrange
        Mockito.when(jwtService.extractUsername(Mockito.any())).thenReturn(expectedUser.getUsername());
        Mockito.when(userService.getUserByUsername(expectedUser.getUsername())).thenReturn(expectedUser);

        Mockito.when(classificationService.getMushroomInstanceForUser(Mockito.any(), Mockito.any())).thenThrow(new MushroomImageRetrievalException("Error retrieving mushroom image"));

        // Act
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/classifications/images/1")
                        .header("Authorization", "Bearer token MOCKED")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.content().string("There was a problem retrieving the image of the mushroom. Please try again later."));

    }
}