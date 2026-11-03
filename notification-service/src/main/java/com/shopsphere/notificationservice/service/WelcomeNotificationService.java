package com.shopsphere.notificationservice.service;

import com.shopsphere.notificationservice.dto.event.UserProfileEvent;
import com.shopsphere.notificationservice.entity.UserNotificationProfile;
import com.shopsphere.notificationservice.repository.UserNotificationProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class WelcomeNotificationService {

    private final UserNotificationProfileRepository profileRepository;
    private final EmailService emailService;

    public void sendWelcomeEmail(UserProfileEvent event){

        if (event == null || event.userId() == null){
            throw new IllegalArgumentException("User profile event and user ID are required");
        }

        UserNotificationProfile profile = profileRepository.findById(event.userId())
                .orElseThrow(() -> new IllegalStateException("User notification profile not found. userId="+ event.userId()));

        String firstName = StringUtils.hasText(profile.getFirstName())
                ? profile.getFirstName().trim()
                : "there";

        String safeFirstName = HtmlUtils.htmlEscape(firstName);

        String htmlBody = """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                    <div style="max-width: 600px; margin: auto; padding: 24px;">
                        <h2>Welcome to ShopSphere, %s!</h2>

                        <p>Thank you for registering with ShopSphere.</p>

                        <p>
                            Your account has been successfully created.
                            You can now explore our products, add items to
                            your cart, and place orders.
                        </p>

                        <p>We're glad to have you with us!</p>

                        <p>Regards,<br><strong>ShopSphere Team</strong></p>
                    </div>
                </body>
                </html>
                """.formatted(safeFirstName);

        boolean sent = emailService.sendHtmlEmail(
                profile.getEmail(),
                "Welcome to ShopSphere!",
                htmlBody
        );

        if (!sent) {
            throw new IllegalStateException(
                    "Welcome email was not sent. Check notification.email.enabled"
            );
        }

        log.info(
                "Welcome email delivered successfully. userId={}, recipient={}",
                profile.getUserId(),
                profile.getEmail()
        );

    }
}
