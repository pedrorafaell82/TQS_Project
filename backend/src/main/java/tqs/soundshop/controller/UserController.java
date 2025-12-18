package tqs.soundshop.controller;

import jakarta.validation.Valid;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tqs.soundshop.dto.RegisterUserRequest;
import tqs.soundshop.dto.UserDto;
import tqs.soundshop.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAll();
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterUserRequest request) {
        UserDto created = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @GetMapping("/me")
    public UserDto me(Authentication authentication) {
        return userService.getByEmail(authentication.getName());
        }
}
