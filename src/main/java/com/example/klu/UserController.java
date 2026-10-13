
package com.example.klu;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository repository;
    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();

    public UserController(UserRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {

        if (user.getUsername() == null
                || user.getUsername().isBlank()
                || user.getEmail() == null
                || user.getEmail().isBlank()
                || user.getPassword() == null
                || user.getPassword().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message", "All fields are required"
                    ));
        }

        if (repository.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message", "Email already registered"
                    ));
        }

        user.setPassword(encoder.encode(user.getPassword()));

        User saved = repository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "id", saved.getId(),
                        "username", saved.getUsername(),
                        "email", saved.getEmail(),
                        "message", "Registration successful"
                )
        );
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(
                repository.findAll().stream()
                        .map(user -> Map.of(
                                "id", user.getId(),
                                "username", user.getUsername(),
                                "email", user.getEmail()
                        ))
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(
            @PathVariable Long id) {

        return repository.findById(id)
                .<ResponseEntity<?>>map(user ->
                        ResponseEntity.ok(
                                Map.of(
                                        "id", user.getId(),
                                        "username", user.getUsername(),
                                        "email", user.getEmail()
                                )
                        ))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of(
                                        "message", "User not found"
                                )));
    }
}
