package edu.demart_api.service.impl;

import edu.demart_api.dto.request.LoginRequest;
import edu.demart_api.dto.request.RegisterRequest;
import edu.demart_api.dto.response.AuthResponse;
import edu.demart_api.entity.Role;
import edu.demart_api.entity.User;
import edu.demart_api.exception.BusinessException;
import edu.demart_api.exception.ResourceNotFoundException;
import edu.demart_api.repository.UserRepository;
import edu.demart_api.security.JwtService;
import edu.demart_api.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // ─── Public Registration (always CUSTOMER) ───────────────────────────────

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        return createAccount(request, Role.CUSTOMER);
    }

    // ─── Login ───────────────────────────────────────────────────────────────

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Incorrect password");
        }

        if (!user.isActive()) {
            throw new BusinessException("Your account has been deactivated");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name(), user.getId());
        return toAuthResponse(user, token);
    }

    // ─── Admin: Create STAFF or ADMIN account ────────────────────────────────

    @Override
    @Transactional
    public AuthResponse createUserWithRole(RegisterRequest request, String roleName) {
        Role role;
        try {
            role = Role.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid role: " + roleName + ". Allowed: CUSTOMER, STAFF, ADMIN");
        }
        return createAccount(request, role);
    }

    // ─── Shared Internal Helper ───────────────────────────────────────────────

    private AuthResponse createAccount(RegisterRequest request, Role role) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email is already registered");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("Phone number is already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(role);
        user.setActive(true);

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name(), user.getId());
        return toAuthResponse(user, token);
    }

    private AuthResponse toAuthResponse(User user, String token) {
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
