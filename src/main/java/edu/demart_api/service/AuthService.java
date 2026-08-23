package edu.demart_api.service;

import edu.demart_api.dto.request.*;
import edu.demart_api.dto.response.AuthResponse;
import edu.demart_api.dto.response.StaffApplicationResponse;

import java.util.List;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    String sendRegistrationOtp(SendRegistrationOtpRequest request);

    AuthResponse verifyRegistrationOtp(VerifyRegistrationOtpRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse createUserWithRole(RegisterRequest request, String role);

    String forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    // ─── Staff / Restaurant Partner Application Flow ───────────────────────────

    StaffApplicationResponse applyForStaff(StaffApplicationRequest request);

    List<StaffApplicationResponse> getStaffApplications(String status);

    AuthResponse approveStaffApplication(Long id, ApproveStaffApplicationRequest request);

    void rejectStaffApplication(Long id, String reason);
}
