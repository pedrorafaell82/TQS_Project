package tqs.soundshop.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tqs.soundshop.dto.RegisterUserRequest;
import tqs.soundshop.dto.UserDto;
import tqs.soundshop.entities.User;
import tqs.soundshop.entities.UserRepository;
import tqs.soundshop.exception.BadRequestException;
import tqs.soundshop.exception.NotFoundException;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /* ================= REGISTER ================= */

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

        return toDto(userRepository.save(user));
    }

    /* ================= LOGIN SUPPORT ================= */

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(
                        user.getRoles().stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                                .collect(Collectors.toSet())
                )
                .build();
    }

    /* ================= QUERIES ================= */

    public java.util.List<UserDto> getAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    public UserDto getById(Long id) {
        return toDto(
                userRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("User " + id + " not found"))
        );
    }

    public UserDto getByEmail(String email) {
        return toDto(
                userRepository.findByEmail(email)
                        .orElseThrow(() -> new NotFoundException("User with email " + email + " not found"))
        );
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