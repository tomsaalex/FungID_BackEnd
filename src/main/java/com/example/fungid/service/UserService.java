package com.example.fungid.service;

import com.example.fungid.domain.User;
import com.example.fungid.dto.UserDTO;
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

    public boolean userExists(String username) {
        User user = userRepository.findByUsername(username);
        return user != null;
    }

    public boolean emailInUse(String email) {
        User user = userRepository.findByEmail(email);
        return user != null;
    }

    public UserDTO saveUser(UserDTO userToSaveDTO) {
        User userToSave = mapToEntity(userToSaveDTO);
        User savedUser = userRepository.saveAndFlush(userToSave);
        return mapToDTO(savedUser);
    }

    public UserDTO loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            return null;
        }
        return mapToDTO(user);
    }

    public User getUser(Long id) {
        Optional<User> foundUser = userRepository.findById(id);
        return foundUser.orElse(null);
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
