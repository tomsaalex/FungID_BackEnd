package com.example.fungid.service;

import com.example.fungid.domain.User;
import com.example.fungid.dto.UserDTO;
import com.example.fungid.exceptions.login.InvalidCredentialsException;
import com.example.fungid.exceptions.register.*;
import com.example.fungid.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;


    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO saveUser(UserDTO userToSaveDTO) {
        if(userToSaveDTO.getUsername() == null || userToSaveDTO.getPassword() == null || userToSaveDTO.getEmail() == null)
            throw new UncompletedFieldsException("Username, Password and Email are all necessary fields for registration.");

        if (userToSaveDTO.getUsername().isEmpty() || userToSaveDTO.getPassword().isEmpty() || userToSaveDTO.getEmail().isEmpty() ||
                userToSaveDTO.getUsername().isBlank() || userToSaveDTO.getPassword().isBlank() || userToSaveDTO.getEmail().isBlank())
            throw new UncompletedFieldsException("Username, Password and Email are all necessary fields for registration.");

        if (userToSaveDTO.getUsername().length() > 50)
            throw new UsernameLengthExceededException("Username must not exceed 50 characters.");

        if (userToSaveDTO.getPassword().length() > 50)
            throw new PasswordLengthExceededException("Password must not exceed 50 characters.");

        if (userToSaveDTO.getEmail().length() > 50)
            throw new EmailLengthExceededException("Email must not exceed 50 characters.");

        if (userRepository.findByUsername(userToSaveDTO.getUsername()) != null)
            throw new UsernameTakenException("Username already taken.");

        if (userRepository.findByEmail(userToSaveDTO.getEmail()) != null)
            throw new EmailTakenException("Email already taken.");

        User userToSave = mapToEntity(userToSaveDTO);
        User savedUser = userRepository.saveAndFlush(userToSave);
        return mapToDTO(savedUser);
    }

    public UserDTO loadUserByCredentials(String username, String password) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty() || username.isBlank() || password.isBlank())
            throw new UncompletedFieldsException("Username and Password are necessary fields for login.");

        User user = userRepository.findByUsername(username);

        if (user == null || !user.getPassword().equals(password)) {
            throw new InvalidCredentialsException("No user found with the given credentials.");
        }
        return mapToDTO(user);
    }

    public User getUser(Long id) {
        Optional<User> foundUserOpt = userRepository.findById(id);
        User foundUser = foundUserOpt.orElse(null);

        if (foundUser == null)
            throw new InvalidCredentialsException("No user found with the given credentials.");

        return foundUser;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<UserDTO> findAll() {
        return userRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    public UserDTO mapToDTO(User user) {
        UserDTO userDTO = new UserDTO();

        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setPassword(user.getPassword());
        userDTO.setEmail(user.getEmail());

        return userDTO;
    }

    public User mapToEntity(UserDTO userDTO) {
        User user = new User();

        user.setId(userDTO.getId());
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());
        user.setEmail(userDTO.getEmail());

        return user;
    }
}
