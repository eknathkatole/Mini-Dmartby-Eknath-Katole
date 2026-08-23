package edu.demart_api.service.impl;

import edu.demart_api.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String senderEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public void sendPasswordResetOtp(String toEmail, String recipientName, String otp) {
        log.info("Preparing Password Reset OTP Email for: {} [OTP: {}]", toEmail, otp);

        if (senderEmail == null || senderEmail.trim().isEmpty()) {
            log.warn("⚠️ SPRING_MAIL_USERNAME is not configured. Email not sent via SMTP. OTP code is: {}", otp);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, "Mini D-Mart Express");
            helper.setTo(toEmail);
            helper.setSubject("Mini D-Mart: Your Password Reset Verification Code");

            String htmlBody = buildOtpEmailTemplate(recipientName, otp, "Password Reset Verification Code",
                    "We received a request to reset the password for your Mini D-Mart account. Please use the following 6-digit verification code to complete your password reset:");
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("✅ Password reset OTP email successfully sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("❌ MessagingException sending email to {}: {}", toEmail, e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error sending email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendRegistrationOtp(String toEmail, String recipientName, String otp) {
        log.info("Preparing Account Registration OTP Email for: {} [OTP: {}]", toEmail, otp);

        if (senderEmail == null || senderEmail.trim().isEmpty()) {
            log.warn("⚠️ SPRING_MAIL_USERNAME is not configured. Email not sent via SMTP. Registration OTP code is: {}", otp);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, "Mini D-Mart Express");
            helper.setTo(toEmail);
            helper.setSubject("Welcome to Mini D-Mart! Your Registration Verification Code");

            String htmlBody = buildOtpEmailTemplate(recipientName, otp, "Account Registration Code",
                    "Thank you for joining Mini D-Mart Express! Please verify your email address with the following 6-digit code to activate your account and start ordering fresh groceries:");
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("✅ Registration OTP email successfully sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("❌ MessagingException sending email to {}: {}", toEmail, e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error sending email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendStaffApprovalEmail(String toEmail, String recipientName, String password, String storeName) {
        log.info("Preparing Staff Approval Credentials Email for: {}", toEmail);

        if (senderEmail == null || senderEmail.trim().isEmpty()) {
            log.warn("⚠️ SPRING_MAIL_USERNAME is not configured. Staff credentials: Email={}, Password={}", toEmail, password);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, "Mini D-Mart Admin");
            helper.setTo(toEmail);
            helper.setSubject("🎉 Approved: Your Mini D-Mart Staff & Restaurant Operator Credentials");

            String htmlBody = buildStaffCredentialsEmailTemplate(recipientName, toEmail, password, storeName);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("✅ Staff credentials email successfully sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("❌ Error sending staff credentials email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildStaffCredentialsEmailTemplate(String name, String email, String password, String storeName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <style>
                body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8fafc; margin: 0; padding: 20px; color: #1e293b; }
                .container { max-width: 540px; margin: 0 auto; background: #ffffff; border-radius: 20px; overflow: hidden; border: 1px solid #e2e8f0; box-shadow: 0 10px 25px -5px rgba(0,0,0,0.05); }
                .header { background: linear-gradient(135deg, #38bdf8 0%%, #f97316 100%%); padding: 32px 24px; text-align: center; color: #ffffff; }
                .logo { font-size: 26px; font-weight: 900; margin: 0; }
                .content { padding: 32px 24px; }
                .greeting { font-size: 16px; font-weight: 700; color: #0f172a; margin-bottom: 12px; }
                .message { font-size: 14px; line-height: 1.6; color: #475569; margin-bottom: 24px; }
                .cred-box { background: #f0f9ff; border: 1px solid #bae6fd; border-radius: 16px; padding: 20px; margin-bottom: 24px; }
                .cred-row { display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 13px; }
                .cred-label { font-weight: bold; color: #0369a1; }
                .cred-value { font-weight: 900; color: #0f172a; font-family: monospace; }
                .footer { background: #f1f5f9; padding: 20px; text-align: center; font-size: 11px; color: #94a3b8; border-top: 1px solid #e2e8f0; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1 class="logo">Mini D-Mart Partner Portal</h1>
                  <p style="margin: 4px 0 0 0; font-size: 13px; opacity: 0.95;">Staff & Restaurant Operations</p>
                </div>
                <div class="content">
                  <div class="greeting">Congratulations, %s! 🎉</div>
                  <div class="message">
                    Your application to register as a <strong>Staff / Restaurant Partner</strong>%s has been reviewed and approved by the Super Admin. Your official management login credentials are generated below:
                  </div>
                  <div class="cred-box">
                    <div class="cred-row">
                      <span class="cred-label">Login Email ID:</span>
                      <span class="cred-value">%s</span>
                    </div>
                    <div class="cred-row" style="margin-bottom: 0;">
                      <span class="cred-label">Generated Password:</span>
                      <span class="cred-value" style="color: #ea580c; font-size: 15px;">%s</span>
                    </div>
                  </div>
                  <div class="message" style="font-size: 13px; color: #64748b;">
                    You can now sign in at the Mini D-Mart web app to access the <strong>Staff Ops</strong> dashboard to manage store inventory, incoming order preparation, and fulfillment.
                  </div>
                </div>
                <div class="footer">
                  © 2026 Mini D-Mart Express · Partner Operations
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                name != null && !name.isBlank() ? name : "Partner",
                storeName != null && !storeName.isBlank() ? " for " + storeName : "",
                email,
                password
        );
    }

    private String buildOtpEmailTemplate(String name, String otp, String title, String introMessage) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <style>
                body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8fafc; margin: 0; padding: 20px; color: #1e293b; }
                .container { max-width: 520px; margin: 0 auto; background: #ffffff; border-radius: 20px; overflow: hidden; border: 1px solid #e2e8f0; box-shadow: 0 10px 25px -5px rgba(0,0,0,0.05); }
                .header { background: linear-gradient(135deg, #f97316 0%%, #38bdf8 100%%); padding: 32px 24px; text-align: center; color: #ffffff; }
                .logo { font-size: 28px; font-weight: 900; letter-spacing: -0.5px; margin: 0; }
                .content { padding: 32px 24px; }
                .greeting { font-size: 16px; font-weight: 700; color: #0f172a; margin-bottom: 12px; }
                .message { font-size: 14px; line-height: 1.6; color: #475569; margin-bottom: 24px; }
                .otp-box { background: #fff7ed; border: 2px dashed #f97316; border-radius: 16px; padding: 20px; text-align: center; margin-bottom: 24px; }
                .otp-label { font-size: 12px; font-weight: 800; text-transform: uppercase; color: #ea580c; letter-spacing: 1px; margin-bottom: 6px; }
                .otp-code { font-size: 34px; font-weight: 900; color: #f97316; letter-spacing: 8px; font-family: monospace; }
                .validity { font-size: 12px; color: #64748b; margin-top: 6px; }
                .footer { background: #f1f5f9; padding: 20px; text-align: center; font-size: 11px; color: #94a3b8; border-top: 1px solid #e2e8f0; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1 class="logo">Mini D-Mart Express</h1>
                  <p style="margin: 4px 0 0 0; font-size: 13px; opacity: 0.95;">Supermarket & Grocery Delivery</p>
                </div>
                <div class="content">
                  <div class="greeting">Hello %s,</div>
                  <div class="message">
                    %s
                  </div>
                  <div class="otp-box">
                    <div class="otp-label">%s</div>
                    <div class="otp-code">%s</div>
                    <div class="validity">⏱️ Valid for 15 minutes only</div>
                  </div>
                  <div class="message" style="font-size: 12px; color: #94a3b8;">
                    If you did not make this request, you can safely ignore this email.
                  </div>
                </div>
                <div class="footer">
                  © 2026 Mini D-Mart Express · All rights reserved.<br/>
                  Automated security notification — please do not reply.
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                name != null && !name.isBlank() ? name : "Valued Customer",
                introMessage,
                title,
                otp
        );
    }
}
