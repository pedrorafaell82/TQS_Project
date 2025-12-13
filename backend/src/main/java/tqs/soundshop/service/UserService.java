package tqs.soundshop.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tqs.soundshop.dto.RegisterUserRequest;
import tqs.soundshop.dto.UserDto;
import tqs.soundshop.entities.User;
import tqs.soundshop.entities.UserRepository;
import tqs.soundshop.exception.BadRequestException;
import tqs.soundshop.exception.NotFoundException;

import java.time.Instant;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDto register(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already in use");
        }

        User user = new User();
        user.setEmail(request.email().toLowerCase());

        user.setPasswordHash(passwordEncoder.encode(request.password()));

        user.setName(request.name());
        user.setCreatedAt(Instant.now());

        Set<User.Role> roles = request.roles();
        if (roles == null || roles.isEmpty()) {
            roles = Set.of(User.Role.RENTER);
        }
        user.setRoles(roles);

        User saved = userRepository.save(user);
        return toDto(saved);
    }

    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User " + id + " not found"));
        return toDto(user);
    }

    public UserDto getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User with email " + email + " not found"));
        return toDto(user);
    }
    
    public UserDetails loadUserByName(String username) {
        User user = userRepository.findByName(username);

        return org.springframework.security.core.userdetails.User
        .withUsername(user.getName())
        .password(user.getPasswordHash())
        .authorities(user.getRole())
        .build();
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRoles()
        );
    }
}
