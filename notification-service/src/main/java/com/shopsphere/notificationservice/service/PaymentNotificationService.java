package com.shopsphere.notificationservice.service;
import com.shopsphere.notificationservice.dto.event.PaymentEvent;
import com.shopsphere.notificationservice.entity.UserNotificationProfile;
import com.shopsphere.notificationservice.repository.UserNotificationProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentNotificationService {

    private final UserNotificationProfileRepository profileRepository;
    private final EmailService emailService;

    public void notifyPaymentStatus(PaymentEvent event) {

        String status = event.paymentStatus() == null
                ? "UNKNOWN"
                : event.paymentStatus().toUpperCase(Locale.ROOT);

        UserNotificationProfile profile = profileRepository
                .findById(event.userId())
                .orElseThrow(() -> new IllegalStateException(
                        "Notification profile not found for userId: "
                                + event.userId()
                ));

        String customerName = (
                nullSafe(profile.getFirstName()) + " "
                        + nullSafe(profile.getLastName())
        ).trim();

        String subject;
        String heading;
        String message;
        String statusColor;

        switch (status) {

            case "SUCCESS" -> {
                subject = "Payment Successful - ShopSphere Order #"
                        + event.orderId();

                heading = "Payment Successful!";
                message = "Your payment has been successfully processed.";
                statusColor = "#16a34a";
            }

            case "FAILED" -> {
                subject = "Payment Failed - ShopSphere Order #"
                        + event.orderId();

                heading = "Payment Failed";
                message = "Unfortunately, your payment could not be processed.";
                statusColor = "#dc2626";
            }

            case "REFUNDED" -> {
                subject = "Payment Refunded - ShopSphere Order #"
                        + event.orderId();

                heading = "Payment Refunded";
                message = "Your payment has been refunded successfully.";
                statusColor = "#2563eb";
            }

            default -> throw new IllegalArgumentException(
                    "Unsupported payment status: " + status
            );
        }

        String htmlBody = buildEmail(
                customerName,
                event,
                heading,
                message,
                statusColor
        );

        boolean sent = emailService.sendHtmlEmail(
                profile.getEmail(),
                subject,
                htmlBody
        );

        if (!sent) {
            throw new IllegalStateException(
                    "Payment notification email was not sent. eventId="
                            + event.eventId()
            );
        }

        log.info(
                "Payment notification email delivered successfully. "
                        + "orderId={}, paymentId={}, userId={}, status={}, eventId={}",
                event.orderId(),
                event.paymentId(),
                event.userId(),
                status,
                event.eventId()
        );
    }

    private String buildEmail(
            String customerName,
            PaymentEvent event,
            String heading,
            String message,
            String statusColor
    ) {

        String safeName = HtmlUtils.htmlEscape(customerName);
        String amount = formatAmount(event.amount());

        String transactionId = HtmlUtils.htmlEscape(
                nullSafe(event.transactionId())
        );

        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family:Arial,sans-serif;background:#f4f4f4;padding:20px;">
                    <div style="max-width:600px;margin:auto;background:white;
                                padding:30px;border-radius:8px;">

                        <h2 style="color:#2563eb;">ShopSphere</h2>

                        <h2 style="color:%s;">%s</h2>

                        <p>Hello %s,</p>

                        <p>%s</p>

                        <hr/>

                        <p><strong>Order ID:</strong> #%s</p>
                        <p><strong>Payment ID:</strong> %s</p>
                        <p><strong>Amount:</strong> %s</p>
                        <p><strong>Transaction ID:</strong> %s</p>

                        <hr/>

                        <p>Thank you for shopping with ShopSphere!</p>

                        <p style="color:#777;font-size:12px;">
                            This is an automated notification. Please do not reply.
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(
                statusColor,
                heading,
                safeName,
                message,
                event.orderId(),
                event.paymentId(),
                amount,
                transactionId
        );
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "Not available";
        }

        return NumberFormat.getCurrencyInstance(
                Locale.forLanguageTag("en-IN")
        ).format(amount);
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
