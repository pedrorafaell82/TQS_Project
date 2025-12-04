package tqs.soundshop.service;

import org.springframework.stereotype.Service;
import tqs.soundshop.dto.RegisterUserRequest;
import tqs.soundshop.dto.UserDto;
import tqs.soundshop.entities.User;
import tqs.soundshop.entities.UserRepository;
import tqs.soundshop.exception.BadRequestException;
import tqs.soundshop.exception.NotFoundException;

import java.time.Instant;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto register(RegisterUserRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(u -> {
            throw new BadRequestException("Email already in use");
        });

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(request.password()); // TODO: replace with real hashing later
        user.setName(request.name());
        user.setRole(request.role());             
        user.setCreatedAt(Instant.now());

        User saved = userRepository.save(user);
        return toDto(saved);
    }

    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User " + id + " not found"));
        return toDto(user);
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole()
        );
    }
}
