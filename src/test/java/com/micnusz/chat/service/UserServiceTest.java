package com.micnusz.chat.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.micnusz.chat.dto.UserRequestDTO;
import com.micnusz.chat.dto.UserResponseDTO;
import com.micnusz.chat.exception.IncorrectPasswordException;
import com.micnusz.chat.exception.UserNotFoundException;
import com.micnusz.chat.exception.UsernameAlreadyExistsException;
import com.micnusz.chat.mapper.UserMapper;
import com.micnusz.chat.model.User;
import com.micnusz.chat.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    //CreateUser
    @Test
    void createUser_shouldEncodePasswordAndSave_whenUsernameDoesNotExist() {
        UserRequestDTO request = new UserRequestDTO();
        request.setUsername("testuser");
        request.setPassword("password123");

        User entity = new User();
        entity.setUsername("testuser");

        User savedEntity = new User();
        savedEntity.setUsername("testuser");
        savedEntity.setPassword("encodedPassword");

        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userMapper.toEntity(request)).thenReturn(entity);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(entity)).thenReturn(savedEntity);
        when(userMapper.toDto(savedEntity)).thenReturn(responseDTO);

        UserResponseDTO result = userService.createUser(request);

        assertEquals("testuser", result.getUsername());

        verify(userRepository).findByUsername("testuser");
        verify(userMapper).toEntity(request);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(entity);
        verify(userMapper).toDto(savedEntity);
        assertEquals("encodedPassword", entity.getPassword());
    }

    @Test
    void createUser_shouldThrow_whenUsernameAlreadyExists() {
        UserRequestDTO request = new UserRequestDTO();
        request.setUsername("existingUser");
        request.setPassword("password");

        User existingUser = new User();
        existingUser.setUsername("existingUser");

        when(userRepository.findByUsername("existingUser")).thenReturn(Optional.of(existingUser));

        UsernameAlreadyExistsException exception = assertThrows(UsernameAlreadyExistsException.class,
                () -> userService.createUser(request));

        assertTrue(exception.getMessage().contains("existingUser"));

        verify(userRepository).findByUsername("existingUser");
        verify(userMapper, never()).toEntity(any());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toDto(any());
    }

    //LoginUser
    @Test
    void loginUser_shouldReturnDto_whenCredentialsAreCorrect() {
        String username = "testuser";
        String rawPassword = "password123";

        User user = new User();
        user.setUsername(username);
        user.setPassword("encodedPassword");

        UserResponseDTO dto = new UserResponseDTO();
        dto.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, user.getPassword())).thenReturn(true);
        when(userMapper.toDto(user)).thenReturn(dto);

        UserResponseDTO result = userService.loginUser(username, rawPassword);

        assertEquals(username, result.getUsername());

        verify(userRepository).findByUsername(username);
        verify(passwordEncoder).matches(rawPassword, user.getPassword());
        verify(userMapper).toDto(user);
    }

    @Test
    void loginUser_shouldThrow_whenUserNotFound() {
        String username = "nonexistent";
        String rawPassword = "password";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.loginUser(username, rawPassword));

        assertTrue(exception.getMessage().contains(username));

        verify(userRepository).findByUsername(username);
        verify(passwordEncoder, never()).matches(any(), any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void loginUser_shouldThrow_whenPasswordIncorrect() {
        String username = "testuser";
        String rawPassword = "wrongPassword";

        User user = new User();
        user.setUsername(username);
        user.setPassword("encodedPassword");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, user.getPassword())).thenReturn(false);

        IncorrectPasswordException exception = assertThrows(IncorrectPasswordException.class,
                () -> userService.loginUser(username, rawPassword));

        assertTrue(exception.getMessage().contains(rawPassword));

        verify(userRepository).findByUsername(username);
        verify(passwordEncoder).matches(rawPassword, user.getPassword());
        verify(userMapper, never()).toDto(any());
    }

    //LoadUserByUsername
    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        String username = "testuser";

        User user = new User();
        user.setUsername(username);
        user.setPassword("encodedPassword");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername(username);

        assertEquals(username, userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("USER")));

        verify(userRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_shouldThrow_whenUserNotFound() {
        String username = "nonexistent";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(username));

        verify(userRepository).findByUsername(username);
    }


    //ReturnCurrentUser
    @Test
    void returnCurrentUser_shouldReturnDto_whenPrincipalIsUserDetails() {
        org.springframework.security.core.userdetails.User userDetails = (org.springframework.security.core.userdetails.User) org.springframework.security.core.userdetails.User
                .withUsername("testuser")
                .password("encodedPassword")
                .authorities("USER")
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);

        // Mockujemy repo i mapper
        User userEntity = new User();
        userEntity.setUsername("testuser");

        UserResponseDTO dto = new UserResponseDTO();
        dto.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));
        when(userMapper.toDto(userEntity)).thenReturn(dto);

        UserResponseDTO result = userService.returnCurrentUser();

        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByUsername("testuser");
        verify(userMapper).toDto(userEntity);
    }

    @Test
    void returnCurrentUser_shouldReturnDto_whenPrincipalIsString() {
        String username = "testuser";

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(username);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);

        User userEntity = new User();
        userEntity.setUsername(username);

        UserResponseDTO dto = new UserResponseDTO();
        dto.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));
        when(userMapper.toDto(userEntity)).thenReturn(dto);

        UserResponseDTO result = userService.returnCurrentUser();

        assertEquals(username, result.getUsername());
        verify(userRepository).findByUsername(username);
        verify(userMapper).toDto(userEntity);
    }

    @Test
    void returnCurrentUser_shouldReturnNull_whenPrincipalIsOtherType() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(12345);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);

        UserResponseDTO result = userService.returnCurrentUser();

        assertNull(result);
    }

    //ReturnUserByUsername
    @Test
    void returnUserByUsername_shouldReturnDto_whenUserExists() {
        String username = "testuser";

        User userEntity = new User();
        userEntity.setUsername(username);

        UserResponseDTO dto = new UserResponseDTO();
        dto.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));
        when(userMapper.toDto(userEntity)).thenReturn(dto);

        UserResponseDTO result = userService.returnUserByUsername(username);

        assertEquals(username, result.getUsername());
        verify(userRepository).findByUsername(username);
        verify(userMapper).toDto(userEntity);
    }

    @Test
    void returnUserByUsername_shouldThrow_whenUserNotFound() {
        String username = "nonexistent";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.returnUserByUsername(username));

        assertTrue(exception.getMessage().contains(username));
        verify(userRepository).findByUsername(username);
        verify(userMapper, never()).toDto(any());
    }
}
