package edu.demart_api.service.impl;

import edu.demart_api.dto.request.*;
import edu.demart_api.dto.response.AuthResponse;
import edu.demart_api.dto.response.StaffApplicationResponse;
import edu.demart_api.entity.RegistrationOtp;
import edu.demart_api.entity.Role;
import edu.demart_api.entity.StaffApplication;
import edu.demart_api.entity.StaffApplicationStatus;
import edu.demart_api.entity.User;
import edu.demart_api.exception.BusinessException;
import edu.demart_api.exception.ResourceNotFoundException;
import edu.demart_api.repository.RegistrationOtpRepository;
import edu.demart_api.repository.StaffApplicationRepository;
import edu.demart_api.repository.UserRepository;
import edu.demart_api.security.JwtService;
import edu.demart_api.service.AuthService;
import edu.demart_api.service.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RegistrationOtpRepository registrationOtpRepository;
    private final StaffApplicationRepository staffApplicationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthServiceImpl(UserRepository userRepository,
                           RegistrationOtpRepository registrationOtpRepository,
                           StaffApplicationRepository staffApplicationRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.registrationOtpRepository = registrationOtpRepository;
        this.staffApplicationRepository = staffApplicationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    // ─── Staff / Restaurant Partner Application Flow ───────────────────────────

    @Override
    @Transactional
    public StaffApplicationResponse applyForStaff(StaffApplicationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("An active account already exists with this email.");
        }
        if (staffApplicationRepository.existsByEmailAndStatus(request.getEmail(), StaffApplicationStatus.PENDING)) {
            throw new BusinessException("A pending application for this email is already under review.");
        }

        StaffApplication app = new StaffApplication();
        app.setName(request.getName());
        app.setEmail(request.getEmail());
        app.setPhone(request.getPhone());
        app.setStoreName(request.getStoreName());
        app.setReason(request.getReason());
        app.setStatus(StaffApplicationStatus.PENDING);

        staffApplicationRepository.save(app);
        return toStaffApplicationResponse(app);
    }

    @Override
    public List<StaffApplicationResponse> getStaffApplications(String status) {
        List<StaffApplication> list;
        if (status != null && !status.isBlank()) {
            try {
                StaffApplicationStatus appStatus = StaffApplicationStatus.valueOf(status.toUpperCase());
                list = staffApplicationRepository.findByStatusOrderByCreatedAtDesc(appStatus);
            } catch (IllegalArgumentException e) {
                list = staffApplicationRepository.findAllByOrderByCreatedAtDesc();
            }
        } else {
            list = staffApplicationRepository.findAllByOrderByCreatedAtDesc();
        }

        return list.stream()
                .map(this::toStaffApplicationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AuthResponse approveStaffApplication(Long id, ApproveStaffApplicationRequest request) {
        StaffApplication app = staffApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff application not found with ID: " + id));

        if (app.getStatus() == StaffApplicationStatus.APPROVED) {
            throw new BusinessException("This application has already been approved.");
        }

        // Generate password if not supplied by admin
        String rawPassword;
        if (request != null && request.getCustomPassword() != null && !request.getCustomPassword().trim().isEmpty()) {
            rawPassword = request.getCustomPassword().trim();
        } else {
            // Auto-generate strong secure password e.g. Staff#748291
            int randomNum = 100000 + secureRandom.nextInt(900000);
            rawPassword = "Staff#" + randomNum;
        }

        // Create or update User as STAFF
        User user = userRepository.findByEmail(app.getEmail())
                .orElseGet(User::new);

        user.setName(app.getName());
        user.setEmail(app.getEmail());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setPhone(app.getPhone());
        user.setRole(Role.STAFF);
        user.setActive(true);

        userRepository.save(user);

        // Update application
        app.setStatus(StaffApplicationStatus.APPROVED);
        app.setGeneratedPassword(rawPassword);
        if (request != null && request.getAdminNote() != null) {
            app.setAdminNote(request.getAdminNote());
        }
        staffApplicationRepository.save(app);

        // Send official credentials email to applicant
        emailService.sendStaffApprovalEmail(app.getEmail(), app.getName(), rawPassword, app.getStoreName());

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name(), user.getId());
        return toAuthResponse(user, token);
    }

    @Override
    @Transactional
    public void rejectStaffApplication(Long id, String reason) {
        StaffApplication app = staffApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff application not found with ID: " + id));

        app.setStatus(StaffApplicationStatus.REJECTED);
        app.setAdminNote(reason);
        staffApplicationRepository.save(app);
    }

    private StaffApplicationResponse toStaffApplicationResponse(StaffApplication app) {
        return StaffApplicationResponse.builder()
                .id(app.getId())
                .name(app.getName())
                .email(app.getEmail())
                .phone(app.getPhone())
                .storeName(app.getStoreName())
                .reason(app.getReason())
                .status(app.getStatus())
                .adminNote(app.getAdminNote())
                .generatedPassword(app.getGeneratedPassword())
                .createdAt(app.getCreatedAt())
                .build();
    }

    // ─── Send Registration OTP ───────────────────────────────────────────────

    @Override
    @Transactional
    public String sendRegistrationOtp(SendRegistrationOtpRequest request) {
        if (request.getConfirmPassword() != null && !request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email is already registered. Please sign in instead.");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("Phone number is already registered.");
        }

        int otpNumber = 100000 + secureRandom.nextInt(900000);
        String otp = String.valueOf(otpNumber);

        RegistrationOtp regOtp = registrationOtpRepository.findByEmail(request.getEmail())
                .orElseGet(() -> {
                    RegistrationOtp newOtp = new RegistrationOtp();
                    newOtp.setEmail(request.getEmail());
                    return newOtp;
                });

        regOtp.setOtp(otp);
        regOtp.setExpiryTime(LocalDateTime.now().plusMinutes(15));
        registrationOtpRepository.save(regOtp);

        emailService.sendRegistrationOtp(request.getEmail(), request.getName(), otp);

        return otp;
    }

    // ─── Verify Registration OTP & Create User ───────────────────────────────

    @Override
    @Transactional
    public AuthResponse verifyRegistrationOtp(VerifyRegistrationOtpRequest request) {
        if (request.getConfirmPassword() != null && !request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email is already registered.");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("Phone number is already registered.");
        }

        RegistrationOtp regOtp = registrationOtpRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("No pending registration found for this email. Please request a new OTP."));

        if (!regOtp.getOtp().equals(request.getOtp().trim())) {
            throw new BusinessException("Invalid verification OTP code. Please check your code and try again.");
        }

        if (regOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Registration OTP code has expired. Please request a new code.");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(Role.CUSTOMER);
        user.setActive(true);

        userRepository.save(user);
        registrationOtpRepository.delete(regOtp);

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name(), user.getId());
        return toAuthResponse(user, token);
    }

    // ─── Public Registration (Direct Fallback) ───────────────────────────────

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

    // ─── Forgot Password (generates 6-digit OTP, sends Email, valid for 15 mins) ───────────

    @Override
    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email: " + request.getEmail()));

        if (!user.isActive()) {
            throw new BusinessException("Account is deactivated");
        }

        int otpNumber = 100000 + secureRandom.nextInt(900000);
        String otp = String.valueOf(otpNumber);

        user.setResetToken(otp);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        emailService.sendPasswordResetOtp(user.getEmail(), user.getName(), otp);

        return otp;
    }

    // ─── Reset Password ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Passwords do not match");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email: " + request.getEmail()));

        if (user.getResetToken() == null || !user.getResetToken().equals(request.getOtp().trim())) {
            throw new BusinessException("Invalid reset OTP code");
        }

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Reset OTP code has expired. Please request a new code.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    // ─── Shared Internal Helper ───────────────────────────────────────────────

    private AuthResponse createAccount(RegisterRequest request, Role role) {
        if (request.getConfirmPassword() != null && !request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Passwords do not match");
        }

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
