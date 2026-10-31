package com.shopsphere.notificationservice.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${notification.email.from}")
    private String from;

    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    public boolean sendEmail (String to, String subject, String body) {

        if (!emailEnabled) {
            log.warn("Email delivery is disabled. Email not sent.");
            return false;
        }

        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException(
                    "Recipient email address cannot be empty"
            );
        }

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException(
                    "Email subject cannot be empty"
            );
        }

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);

        log.info(
                "Email sent successfully. Recipient={}, Subject={}",
                to,
                subject
        );

        return true;
    }


    public boolean sendHtmlEmail(
            String to,
            String subject,
            String htmlBody
    ) {

        if (!emailEnabled) {
            log.warn("Email delivery is disabled. Email not sent.");
            return false;
        }

        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException(
                    "Recipient email address cannot be empty"
            );
        }

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException(
                    "Email subject cannot be empty"
            );
        }

        if (htmlBody == null || htmlBody.isBlank()) {
            throw new IllegalArgumentException(
                    "Email body cannot be empty"
            );
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    false,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(mimeMessage);

            log.info(
                    "HTML email sent successfully. Recipient={}, Subject={}",
                    to,
                    subject
            );

            return true;

        } catch (MessagingException exception) {
            throw new IllegalStateException(
                    "Failed to create HTML email message",
                    exception
            );
        }
    }

}
