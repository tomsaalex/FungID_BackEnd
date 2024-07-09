package com.example.fungid.controller;

import com.example.fungid.dto.AuthDTO;
import com.example.fungid.dto.UserDTO;
import com.example.fungid.service.JwtService;
import com.example.fungid.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private final UserService userService;

    @Autowired
    private final JwtService jwtService;

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
        UserDTO savedUser = userService.saveUser(userToRegister);
        String token = jwtService.generateToken(savedUser.getUsername());
        return new ResponseEntity<>(new AuthDTO(savedUser.getId(), token), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserDTO userToLoginDTO) {
        UserDTO foundUser = userService.loadUserByCredentials(userToLoginDTO.getUsername(), userToLoginDTO.getPassword());

        String token = jwtService.generateToken(userToLoginDTO.getUsername());
        return new ResponseEntity<>(new AuthDTO(foundUser.getId(), token), HttpStatus.OK);
    }
}
