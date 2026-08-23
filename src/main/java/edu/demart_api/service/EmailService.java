package edu.demart_api.service;

public interface EmailService {

    void sendPasswordResetOtp(String toEmail, String recipientName, String otp);

    void sendRegistrationOtp(String toEmail, String recipientName, String otp);

    void sendStaffApprovalEmail(String toEmail, String recipientName, String password, String storeName);
}
