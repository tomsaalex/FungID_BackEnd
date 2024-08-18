package com.example.fungid.controller.classification_controller;

import com.example.fungid.FungidApplication;
import com.example.fungid.dto.ClassificationResultAI;
import com.example.fungid.dto.MushroomClassificationDTO;
import com.example.fungid.service.JwtService;
import com.example.fungid.test_config.TestFileSystemConfig;
import com.example.fungid.test_config.TestRestTemplateConfig;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FungidApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {TestFileSystemConfig.class, TestRestTemplateConfig.class})
@Import({TestFileSystemConfig.class, TestRestTemplateConfig.class})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ClassificationControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;
    @Autowired
    private JwtService jwtService;

    @MockBean
    private RestTemplate aiContactRestTemplate;

    @PostConstruct
    void createBaseUrl() {
        baseUrl = "http://localhost:" + port + "/api/classifications";
    }

    private HttpEntity<LinkedMultiValueMap<String, Object>> buildImageDateMultiPartRequest(String bearerToken, MockMultipartFile imageFile, String dateString) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(bearerToken);

        LinkedMultiValueMap<String, Object> requestParts = new LinkedMultiValueMap<>();
        requestParts.add("mushroomImage", imageFile.getResource());
        requestParts.add("mushroomDate", dateString);

        return new HttpEntity<>(requestParts, headers);
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @Tag("Integration_Testing")
    @Sql({"/classification-controller-test.sql"})
    void test_identifyMushroom_classificationSuccessful() {
        // Arrange
        MockMultipartFile image = new MockMultipartFile("mushroomImage", "mushroom.jpg", "image/jpeg", "image".getBytes());
        String dateString = "2021-09-01-12-00-00-000";
        String bearerToken = jwtService.generateToken("John Doe");
        HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = buildImageDateMultiPartRequest(bearerToken, image, dateString);

        Mockito.when(aiContactRestTemplate.postForEntity(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(
                new ResponseEntity<>(new ClassificationResultAI("random_result"), null, HttpStatus.OK));

        // Act
        ResponseEntity<MushroomClassificationDTO> response = restTemplate.postForEntity(baseUrl + "/identify", httpEntity, MushroomClassificationDTO.class);

        // Assert
        assertTrue(response.getStatusCode().isSameCodeAs(HttpStatus.OK));
        assertFalse(Objects.requireNonNull(response.getBody()).classificationResult.isEmpty());
        assertEquals(response.getBody().sampleTakenAt, dateString);
        assertTrue(response.getBody().mushroomInstanceId > 0);
    }

    @Test
    @Tag("Integration_Testing")
    @Sql({"/classification-controller-test.sql"})
    void test_identifyMushroom_invalidDate() {
        // Arrange

        MockMultipartFile image = new MockMultipartFile("mushroomImage", "mushroom.jpg", "image/jpeg", "image".getBytes());
        String dateString = "invalid_date";
        String bearerToken = jwtService.generateToken("John Doe");

        // Act
        HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = buildImageDateMultiPartRequest(bearerToken, image, dateString);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/identify", httpEntity, String.class);

        // Assert
        assertTrue(response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST));
        assertTrue(Objects.requireNonNull(response.getBody()).contains("The date provided is not in the correct format. Please provide a date in the format yyyy-MM-dd-HH-mm-ss-SSS."));
    }

    @Test
    @Tag("Integration_Testing")
    @Sql({"/classification-controller-test.sql"})
    void test_identifyMushroom_emptyImage() {
        // Arrange
        MockMultipartFile image = new MockMultipartFile("mushroomImage", "mushroom.jpg", "image/jpeg", new byte[0]);
        String dateString = "2021-09-01-12-00-00-000";
        String bearerToken = jwtService.generateToken("John Doe");

        // Act
        HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = buildImageDateMultiPartRequest(bearerToken, image, dateString);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/identify", httpEntity, String.class);

        // Assert
        assertTrue(response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST));
        assertEquals(response.getBody(), "You must provide an image of the mushroom.");
    }

    @Test
    @Tag("Integration_Testing")
    @Sql({"/classification-controller-test.sql"})
    void test_identifyMushroom_unsupportedContentType() {
        // Arrange
        MockMultipartFile invalidContentTypeImage = new MockMultipartFile("mushroomImage", "mushroom.mp4", "video/mp4", "image".getBytes());
        MockMultipartFile nullContentTypeImage = new MockMultipartFile("mushroomImage", "mushroom", null, "image".getBytes());

        String dateString = "2021-09-01-12-00-00-000";
        String bearerToken = jwtService.generateToken("John Doe");

        // Act
        HttpEntity<LinkedMultiValueMap<String, Object>> invalidTypeHttpEntity = buildImageDateMultiPartRequest(bearerToken, invalidContentTypeImage, dateString);
        HttpEntity<LinkedMultiValueMap<String, Object>> nullTypeHttpEntity = buildImageDateMultiPartRequest(bearerToken, nullContentTypeImage, dateString);
        ResponseEntity<String> invalidTypeResponse = restTemplate.postForEntity(baseUrl + "/identify", invalidTypeHttpEntity, String.class);
        ResponseEntity<String> nullTypeResponse = restTemplate.postForEntity(baseUrl + "/identify", nullTypeHttpEntity, String.class);

        // Assert
        assertTrue(invalidTypeResponse.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST));
        assertEquals(invalidTypeResponse.getBody(), "Only PNG and JPG images are supported.");

        assertTrue(nullTypeResponse.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST));
        assertEquals(nullTypeResponse.getBody(), "Only PNG and JPG images are supported.");
    }

    @Test
    @Tag("Integration_Testing")
    @Sql({"/classification-controller-test.sql"})
    void test_getAllClassifiedMushroomsForUser() {
        // Arrange
        MockMultipartFile image = new MockMultipartFile("mushroomImage", "mushroom.jpg", "image/jpeg", "image".getBytes());
        MockMultipartFile image2 = new MockMultipartFile("mushroomImage", "mushroom2.jpg", "image/jpeg", "image".getBytes());
        String dateString = "2021-09-01-12-00-00-000";
        String bearerToken = jwtService.generateToken("John Doe");
        HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = buildImageDateMultiPartRequest(bearerToken, image, dateString);
        HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity2 = buildImageDateMultiPartRequest(bearerToken, image2, dateString);

        Mockito.when(aiContactRestTemplate.postForEntity(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(
                new ResponseEntity<>(new ClassificationResultAI("random_result"), null, HttpStatus.OK));

        HttpHeaders allRequestheaders = new HttpHeaders();
        allRequestheaders.setBearerAuth(bearerToken);
        MultiValueMap<String, String> allRequestBody = new LinkedMultiValueMap<>();
        HttpEntity<?> allRequestEntity = new HttpEntity<>(allRequestBody, allRequestheaders);

        // Act
        ResponseEntity<MushroomClassificationDTO> response = restTemplate.postForEntity(baseUrl + "/identify", httpEntity, MushroomClassificationDTO.class);
        ResponseEntity<MushroomClassificationDTO> response2 = restTemplate.postForEntity(baseUrl + "/identify", httpEntity2, MushroomClassificationDTO.class);

        ResponseEntity<MushroomClassificationDTO[]> responseAll = restTemplate.exchange(baseUrl + "/mushroom-instances", HttpMethod.GET, allRequestEntity, MushroomClassificationDTO[].class);

        // Assert
        assertTrue(responseAll.getStatusCode().isSameCodeAs(HttpStatus.OK));
        assertEquals(2, Objects.requireNonNull(responseAll.getBody()).length);
        assertEquals(Objects.requireNonNull(response.getBody()).mushroomInstanceId, responseAll.getBody()[0].mushroomInstanceId);
        assertEquals(Objects.requireNonNull(response2.getBody()).mushroomInstanceId, responseAll.getBody()[1].mushroomInstanceId);
    }

    @Test
    @Tag("Integration_Testing")
    @Sql({"/classification-controller-test.sql"})
    void test_getAllClassifiedMushroomsForUser_noMushroomsFound() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();

        String bearerToken = jwtService.generateToken("John Doe");
        headers.setBearerAuth(bearerToken);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        HttpEntity<?> requestEntity = new HttpEntity<>(body, headers);

        // Act
        ResponseEntity<MushroomClassificationDTO[]> responseAll = restTemplate.exchange(baseUrl + "/mushroom-instances", HttpMethod.GET, requestEntity, MushroomClassificationDTO[].class);

        // Assert
        assertTrue(responseAll.getStatusCode().isSameCodeAs(HttpStatus.OK));
        assertEquals(0, Objects.requireNonNull(responseAll.getBody()).length);
    }

    @Test
    @Tag("Integration_Testing")
    @Sql({"/classification-controller-test.sql"})
    void test_getMushroomClassificationImage_imageFound() {
        // Arrange
        byte[] imageBytes = "image".getBytes();

        MockMultipartFile image = new MockMultipartFile("mushroomImage", "mushroom.jpg", "image/jpeg", imageBytes);
        String bearerToken = jwtService.generateToken("John Doe");
        String dateString = "2021-09-01-12-00-00-000";
        HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = buildImageDateMultiPartRequest(bearerToken, image, dateString);

        Mockito.when(aiContactRestTemplate.postForEntity(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(
                new ResponseEntity<>(new ClassificationResultAI("random_result"), null, HttpStatus.OK));


        ResponseEntity<MushroomClassificationDTO> response = restTemplate.postForEntity(baseUrl + "/identify", httpEntity, MushroomClassificationDTO.class);

        HttpHeaders imageRequestheaders = new HttpHeaders();
        imageRequestheaders.setBearerAuth(bearerToken);
        MultiValueMap<String, String> imageRequestBody = new LinkedMultiValueMap<>();
        HttpEntity<?> imageRequestEntity = new HttpEntity<>(imageRequestBody, imageRequestheaders);

        // Act
        ResponseEntity<byte[]> imageRequestResponse = restTemplate.exchange(baseUrl + "/images/" + Objects.requireNonNull(response.getBody()).mushroomInstanceId, HttpMethod.GET, imageRequestEntity, byte[].class);

        // Assert
        assertTrue(imageRequestResponse.getStatusCode().isSameCodeAs(HttpStatus.OK));
        assertNotNull(imageRequestResponse.getBody());
        assertTrue(imageRequestResponse.getBody().length > 0);
        assertArrayEquals(imageRequestResponse.getBody(), imageBytes);
    }

    @Test
    @Tag("Integration_Testing")
    @Sql({"/classification-controller-test.sql"})
    void test_getMushroomClassificationImage_imageNotFound() {
        // Arrange
        String johnBearerToken = jwtService.generateToken("John Doe");
        String janeBearerToken = jwtService.generateToken("Jane Smith");

        HttpHeaders johnImageRequestheaders = new HttpHeaders();
        johnImageRequestheaders.setBearerAuth(johnBearerToken);
        MultiValueMap<String, String> johnImageRequestBody = new LinkedMultiValueMap<>();
        HttpEntity<?> johnImageRequestEntity = new HttpEntity<>(johnImageRequestBody, johnImageRequestheaders);

        HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = buildImageDateMultiPartRequest(johnBearerToken, new MockMultipartFile("mushroomImage", "mushroom.jpg", "image/jpeg", "image".getBytes()), "2021-09-01-12-00-00-000");

        HttpHeaders janeImageRequestheaders = new HttpHeaders();
        janeImageRequestheaders.setBearerAuth(janeBearerToken);
        MultiValueMap<String, String> janeImageRequestBody = new LinkedMultiValueMap<>();
        HttpEntity<?> janeImageRequestEntity = new HttpEntity<>(janeImageRequestBody, janeImageRequestheaders);

        Mockito.when(aiContactRestTemplate.postForEntity(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(
                new ResponseEntity<>(new ClassificationResultAI("random_result"), null, HttpStatus.OK));

        // Act
        ResponseEntity<String> johnImageRequestResponse = restTemplate.exchange(baseUrl + "/images/1", HttpMethod.GET, johnImageRequestEntity, String.class);
        ResponseEntity<MushroomClassificationDTO> johnPostResponse = restTemplate.postForEntity(baseUrl + "/identify", httpEntity, MushroomClassificationDTO.class);
        ResponseEntity<String> janeImageRequestResponse = restTemplate.exchange(baseUrl + "/images/" + Objects.requireNonNull(johnPostResponse.getBody()).mushroomInstanceId, HttpMethod.GET, janeImageRequestEntity, String.class);

        // Assert
        assertTrue(johnImageRequestResponse.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND));
        assertNotNull(johnImageRequestResponse.getBody());
        assertEquals(johnImageRequestResponse.getBody(), "Your account does not appear to have a mushroom classification job with the given ID.");

        assertTrue(johnPostResponse.getStatusCode().isSameCodeAs(HttpStatus.OK));

        assertTrue(janeImageRequestResponse.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND));
        assertNotNull(janeImageRequestResponse.getBody());
        assertEquals(janeImageRequestResponse.getBody(), "Your account does not appear to have a mushroom classification job with the given ID.");
    }

    @Test
    @Tag("Integration_Testing")
    void test_accessNonexistentEndpoint() {
        // TODO: Implement this test
    }
}