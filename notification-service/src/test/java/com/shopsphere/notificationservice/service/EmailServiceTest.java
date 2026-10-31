package com.shopsphere.notificationservice.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @Test
    void shouldSendTestEmailToMailtrap() {
        boolean result = emailService.sendEmail(
                "amit.test@example.com",
                "ShopSphere - SMTP Integration Test",
                """
                Hello!

                This is a test email from ShopSphere Notification Service.

                SMTP integration with Mailtrap is working successfully.

                Regards,
                ShopSphere Team
                """
        );

        assertTrue(result);
    }
}
