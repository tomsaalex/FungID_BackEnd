package com.example.fungid.service;

import com.example.fungid.domain.User;
import com.example.fungid.dto.UserDTO;
import com.example.fungid.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Value("${jwt.secretKey}")
    private String secretKeyText;
    private SecretKey secretKey;

    private static final long expirationTime = 864_000_000;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    void convertKey() {
        secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKeyText));
    }

    public boolean userExists(String username) {
        User user = userRepository.findByUsername(username);
        return user != null;
    }

    public UserDTO saveUser(UserDTO userToSaveDTO) {
        User userToSave = mapToEntity(userToSaveDTO);
        User savedUser = userRepository.saveAndFlush(userToSave);
        return mapToDTO(savedUser);
    }

    public UserDTO loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            return null;
        }
        return mapToDTO(user);
    }

    public User getUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return user;
    }

    public List<UserDTO> findAll() {
        return userRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    public UserDTO mapToDTO(User user) {
        UserDTO userDTO = new UserDTO();

        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setPassword(user.getPassword());

        return userDTO;
    }

    public User mapToEntity(UserDTO userDTO) {
        User user = new User();

        user.setId(userDTO.getId());
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());

        return user;
    }

    public String createToken(String identifier) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder().subject(identifier)
                .issuedAt(now).expiration(expirationDate)
                .signWith(secretKey).compact();
    }

    public User tokenIsValid(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(secretKey)
                    .build().parseSignedClaims(token);

            Claims claims = claimsJws.getPayload();
            return getUserByUsername(claims.getSubject());
        } catch (Exception e) {
            return null;
        }
    }

}
