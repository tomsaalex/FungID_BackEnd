package com.example.fungid.controller;

import com.example.fungid.domain.MushroomInstance;
import com.example.fungid.domain.User;
import com.example.fungid.dto.MushroomClassificationDTO;
import com.example.fungid.service.ClassificationService;
import com.example.fungid.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/classifications")
public class ClassificationController {
    @Autowired
    private final ClassificationService classificationService;

    @Autowired
    private final UserService userService;

    public ClassificationController(ClassificationService classificationService, UserService userService) {
        this.classificationService = classificationService;
        this.userService = userService;
    }

    @PostMapping("/identify")
    public ResponseEntity<?> identifyMushroom(@RequestParam("mushroomImage") MultipartFile mushroomImage, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User foundUser = userService.getUser(userId);

        if (foundUser == null) {
            return new ResponseEntity<>("No user found with the given credentials", HttpStatus.NOT_FOUND);
        }

        try {
            MushroomClassificationDTO mushroomClassificationDTO = classificationService.classifyMushroom(foundUser, mushroomImage);
            return new ResponseEntity<>(mushroomClassificationDTO, HttpStatus.OK);
        } catch (IOException ex) {
            return new ResponseEntity<>("Error handling mushroom image", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(
            value = "/mushroom-instances"
    )
    public ResponseEntity<?> getAllClassifiedMushroomsForUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User foundUser = userService.getUser(userId);

        if (foundUser == null) {
            return new ResponseEntity<>("No user found with the given credentials", HttpStatus.NOT_FOUND);
        }

        List<MushroomClassificationDTO> mushroomClassificationDTOs = classificationService.getAllMushroomInstancesForUser(foundUser);
        return new ResponseEntity<>(mushroomClassificationDTOs, HttpStatus.OK);
    }

    @GetMapping(
            value = "/images/{id}",
            produces = MediaType.IMAGE_JPEG_VALUE
    )
    public ResponseEntity<?> getMushroomClassificationImage(@PathVariable("id") Long mushroomInstanceId, HttpServletRequest request) {
        MushroomInstance foundMushroom = classificationService.getMushroomInstance(mushroomInstanceId);
        Long userId = (Long) request.getAttribute("userId");

        if (foundMushroom == null || !Objects.equals(foundMushroom.getUser().getId(), userId)) {
            return new ResponseEntity<>("Your account does not appear to have a mushroom classification job with the given ID.", HttpStatus.NOT_FOUND);
        }

        try {
            byte[] desiredImage = classificationService.getImage(userId, foundMushroom.getMushroomImageName());
            return new ResponseEntity<>(desiredImage, HttpStatus.OK);
        } catch (IOException ex) {
            return new ResponseEntity<>("Error retrieving mushroom image", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
