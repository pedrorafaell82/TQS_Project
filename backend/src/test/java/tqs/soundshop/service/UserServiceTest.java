package tqs.soundshop.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import tqs.soundshop.dto.RegisterUserRequest;
import tqs.soundshop.dto.UserDto;
import tqs.soundshop.entities.User;
import tqs.soundshop.entities.UserRepository;
import tqs.soundshop.exception.BadRequestException;
import tqs.soundshop.exception.NotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // ---------- register ----------

    @Test
    void register_whenEmailNotInUse_andRolesProvided_createsUserWithEncodedPassword() {
        String name = "Alice";
        String email = "Alice@Example.com";
        String password = "secret123";
        Set<User.Role> roles = Set.of(User.Role.RENTER, User.Role.OWNER);

        RegisterUserRequest request = new RegisterUserRequest(name, email, password, roles);

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(42L);
            return u;
        });

        UserDto dto = userService.register(request);

        // capture saved user and verify fields
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        assertThat(saved.getEmail()).isEqualTo(email.toLowerCase());
        assertThat(saved.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(saved.getName()).isEqualTo(name);
        assertThat(saved.getRoles()).containsExactlyInAnyOrder(User.Role.RENTER, User.Role.OWNER);

        assertThat(dto.id()).isEqualTo(42L);
        assertThat(dto.email()).isEqualTo(email.toLowerCase());
        assertThat(dto.name()).isEqualTo(name);
        assertThat(dto.roles()).containsExactlyInAnyOrder(User.Role.RENTER, User.Role.OWNER);
    }

    @Test
    void register_whenRolesNull_defaultsToRenter() {
        String name = "Bob";
        String email = "bob@example.com";
        String password = "password";

        RegisterUserRequest request = new RegisterUserRequest(name, email, password, null);

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encoded");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });

        UserDto dto = userService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        assertThat(saved.getRoles()).containsExactly(User.Role.RENTER);
        assertThat(dto.roles()).containsExactly(User.Role.RENTER);
    }

    @Test
    void register_whenRolesEmpty_defaultsToRenter() {
        String name = "Charlie";
        String email = "charlie@example.com";
        String password = "password";

        RegisterUserRequest request = new RegisterUserRequest(name, email, password, Set.of());

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encoded");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(11L);
            return u;
        });

        UserDto dto = userService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        assertThat(saved.getRoles()).containsExactly(User.Role.RENTER);
        assertThat(dto.roles()).containsExactly(User.Role.RENTER);
    }

    @Test
    void register_whenEmailAlreadyInUse_throwsBadRequest() {
        String name = "Dave";
        String email = "dave@example.com";
        String password = "password";
        Set<User.Role> roles = Set.of(User.Role.RENTER);

        RegisterUserRequest request = new RegisterUserRequest(name, email, password, roles);

        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email already in use");
    }

    // ---------- getById ----------

    @Test
    void getById_whenUserExists_returnsDto() {
        Long id = 5L;
        User user = new User();
        user.setId(id);
        user.setEmail("user@example.com");
        user.setName("User Name");
        user.setRoles(Set.of(User.Role.RENTER, User.Role.OWNER));

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserDto dto = userService.getById(id);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.email()).isEqualTo(user.getEmail());
        assertThat(dto.name()).isEqualTo(user.getName());
        assertThat(dto.roles()).containsExactlyInAnyOrder(User.Role.RENTER, User.Role.OWNER);
    }

    @Test
    void getById_whenUserNotFound_throwsNotFound() {
        Long id = 999L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User " + id + " not found");
    }

    // ---------- getByEmail ----------

    @Test
    void getByEmail_whenUserExists_returnsDto() {
        String email = "user@example.com";

        User user = new User();
        user.setId(7L);
        user.setEmail(email);
        user.setName("User Name");
        user.setRoles(Set.of(User.Role.ADMIN));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDto dto = userService.getByEmail(email);

        assertThat(dto.id()).isEqualTo(7L);
        assertThat(dto.email()).isEqualTo(email);
        assertThat(dto.name()).isEqualTo("User Name");
        assertThat(dto.roles()).containsExactly(User.Role.ADMIN);
    }

    @Test
    void getByEmail_whenUserNotFound_throwsNotFound() {
        String email = "missing@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByEmail(email))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User with email " + email + " not found");
    }
}
