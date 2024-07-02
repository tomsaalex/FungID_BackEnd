package com.example.fungid.controller;

import com.example.fungid.dto.LoginDTO;
import com.example.fungid.dto.UserDTO;
import com.example.fungid.service.JwtService;
import com.example.fungid.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        if (userToRegister.getUsername().isEmpty() || userToRegister.getPassword().isEmpty() || userToRegister.getEmail().isEmpty() ||
                userToRegister.getUsername().isBlank() || userToRegister.getPassword().isBlank() || userToRegister.getEmail().isBlank())
            return new ResponseEntity<>("Username, Password and Email are all neccessary fields", HttpStatus.BAD_REQUEST);

        if (userService.userExists(userToRegister.getUsername()))
            return new ResponseEntity<>("Username already taken", HttpStatus.CONFLICT);
        if (userService.emailInUse(userToRegister.getEmail()))
            return new ResponseEntity<>("Email already taken", HttpStatus.CONFLICT);

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
