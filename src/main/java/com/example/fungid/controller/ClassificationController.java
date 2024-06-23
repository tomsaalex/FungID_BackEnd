package com.example.fungid.controller;

import com.example.fungid.domain.MushroomInstance;
import com.example.fungid.domain.User;
import com.example.fungid.dto.MushroomClassificationDTO;
import com.example.fungid.service.ClassificationService;
import com.example.fungid.service.ImageService;
import com.example.fungid.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping("/api/classifications")
public class ClassificationController {
    @Autowired
    private final ClassificationService classificationService;

    @Autowired
    private final UserService userService;

    @Autowired
    private final ImageService imageService;

    private final String IMAGE_UPLOAD_DIRECTORY = "src/main/resources/static/images/mushroom_instances";

    private static final Logger LOG = LoggerFactory.getLogger(ClassificationController.class);

    public ClassificationController(ClassificationService classificationService, UserService userService, ImageService imageService) {
        this.classificationService = classificationService;
        this.userService = userService;
        this.imageService = imageService;
    }

    @PostMapping("/identify")
    public ResponseEntity<?> identifyMushroom(@RequestParam("mushroomImage") MultipartFile mushroomImage, HttpServletRequest request) throws IOException {
        Long userId = (Long) request.getAttribute("userId");
        String imageName = imageService.saveImageToStorage(IMAGE_UPLOAD_DIRECTORY + "/" + userId, mushroomImage);
        User foundUser = userService.getUser(userId);

        MushroomClassificationDTO mushroomClassificationDTO = classificationService.classifyMushroom(foundUser, imageName);

        return new ResponseEntity<>(mushroomClassificationDTO, HttpStatus.OK);
    }

    @GetMapping(
            value = "/image/{id}",
            produces = MediaType.IMAGE_JPEG_VALUE
    )
    public ResponseEntity<?> getMushroomClassificationImage(@PathVariable("id") Long mushroomInstanceId, HttpServletRequest request) throws IOException {
        MushroomInstance foundMushroom = classificationService.getMushroomInstance(mushroomInstanceId);
        Long userId = (Long) request.getAttribute("userId");

        if(foundMushroom == null || !Objects.equals(foundMushroom.getUser().getId(), userId)) {
            return new ResponseEntity<>("Your account does not appear to have a mushroom classification job with the given ID.", HttpStatus.NOT_FOUND);
        }

        byte[] desiredImage = imageService.getImage(IMAGE_UPLOAD_DIRECTORY + "/" + userId, foundMushroom.getMushroomImageName());
        return new ResponseEntity<>(desiredImage, HttpStatus.OK);
    }
}
