package com.example.fungid.controller;

import com.example.fungid.domain.User;
import com.example.fungid.dto.LoginDTO;
import com.example.fungid.dto.UserDTO;
import com.example.fungid.service.JwtService;
import com.example.fungid.service.UserService;
import io.jsonwebtoken.Jwt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private final UserService userService;

    @Autowired
    private final JwtService jwtService;

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
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
        UserDTO foundUser = userService.loadUserByUsername(userToLoginDTO.getUsername());

        if (foundUser == null || !Objects.equals(foundUser.getPassword(), userToLoginDTO.getPassword()))
            return new ResponseEntity<>("There is no user with the given login information", HttpStatus.NOT_FOUND);

        String token = jwtService.generateToken(userToLoginDTO.getUsername());
        return new ResponseEntity<>(new LoginDTO(foundUser.getId(), token), HttpStatus.ACCEPTED);
    }
}
