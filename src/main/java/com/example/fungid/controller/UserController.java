package com.example.fungid.controller;

import com.example.fungid.dto.LoginDTO;
import com.example.fungid.dto.UserDTO;
import com.example.fungid.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private final UserService userService;


    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDTO userToRegister) {
        if (userService.userExists(userToRegister.getUsername()))
            return new ResponseEntity<>("Username already taken", HttpStatus.CONFLICT);

        UserDTO savedUser = userService.saveUser(userToRegister);

        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserDTO userToLoginDTO) {
        if (!userService.userExists(userToLoginDTO.getUsername()))
            return new ResponseEntity<>("There is no user with the given username", HttpStatus.NOT_FOUND);

        String token = userService.createToken(userToLoginDTO.getUsername());
        return new ResponseEntity<>(new LoginDTO(userToLoginDTO.getId(), token), HttpStatus.ACCEPTED);
    }
}
