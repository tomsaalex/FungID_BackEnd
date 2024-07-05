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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
    public ResponseEntity<?> identifyMushroom(@RequestPart("mushroomImage") MultipartFile mushroomImage, @RequestParam("mushroomDate") String mushroomDateString, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS");
        LocalDateTime mushroomDate = LocalDateTime.parse(mushroomDateString, formatter);

        User foundUser = userService.getUser(userId);

        MushroomClassificationDTO mushroomClassificationDTO = classificationService.classifyMushroom(foundUser, mushroomImage, mushroomDate);
        return new ResponseEntity<>(mushroomClassificationDTO, HttpStatus.OK);
    }

    @GetMapping(
            value = "/mushroom-instances"
    )
    public ResponseEntity<?> getAllClassifiedMushroomsForUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User foundUser = userService.getUser(userId);

        List<MushroomClassificationDTO> mushroomClassificationDTOs = classificationService.getAllMushroomInstancesForUser(foundUser);
        return new ResponseEntity<>(mushroomClassificationDTOs, HttpStatus.OK);
    }

    @GetMapping(
            value = "/images/{id}",
            produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<?> getMushroomClassificationImage(@PathVariable("id") Long mushroomInstanceId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        MushroomInstance foundMushroom = classificationService.getMushroomInstanceForUser(mushroomInstanceId, userId);

        byte[] desiredImage = classificationService.getImage(userId, foundMushroom.getMushroomImageName());
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.IMAGE_JPEG).body(desiredImage);
    }
}
