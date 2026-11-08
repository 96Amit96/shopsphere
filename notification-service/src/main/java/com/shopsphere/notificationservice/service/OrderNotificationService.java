package com.shopsphere.notificationservice.service;

import com.shopsphere.notificationservice.dto.event.OrderCreatedEvent;
import com.shopsphere.notificationservice.dto.event.OrderItemEvent;
import com.shopsphere.notificationservice.entity.UserNotificationProfile;
import com.shopsphere.notificationservice.repository.UserNotificationProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderNotificationService {

    private final UserNotificationProfileRepository profileRepository;
    private final EmailService emailService;

    public void notifyOrderCreated(OrderCreatedEvent event) {

        if (event == null || event.userId() == null) {
            throw new IllegalArgumentException(
                    "Order event and user ID are required"
            );
        }

        if (event.orderId() == null || event.totalAmount() == null) {
            throw new IllegalArgumentException(
                    "Order ID and total amount are required"
            );
        }

        // Retrieve customer profile from Notification Service database.
        UserNotificationProfile profile = profileRepository
                .findById(event.userId())
                .orElseThrow(() -> new IllegalStateException(
                        "User notification profile not found. userId="
                                + event.userId()
                ));

        String firstName = StringUtils.hasText(profile.getFirstName())
                ? profile.getFirstName().trim()
                : "there";

        String htmlBody = buildOrderReceivedEmail(
                firstName,
                event
        );

        // Send the order confirmation email.
        boolean sent = emailService.sendHtmlEmail(
                profile.getEmail(),
                "Your ShopSphere Order #" + event.orderId() + " Received!",
                htmlBody
        );

        if (!sent) {
            throw new IllegalStateException(
                    "Order confirmation email was not sent. orderId="
                            + event.orderId()
            );
        }

        log.info(
                "Order received email delivered successfully. " +
                        "orderId={}, userId={}, eventId={}",
                event.orderId(),
                event.userId(),
                event.eventId()
        );
    }

    private String buildOrderReceivedEmail(
            String firstName,
            OrderCreatedEvent event
    ) {

        StringBuilder itemRows = new StringBuilder();

        List<OrderItemEvent> items = event.items();

        if (items != null && !items.isEmpty()) {

            for (OrderItemEvent item : items) {

                if (item == null) {
                    continue;
                }

                String productName = HtmlUtils.htmlEscape(
                        Objects.toString(item.productName(), "Product")
                );

                String unitPrice = HtmlUtils.htmlEscape(
                        Objects.toString(item.unitPrice(), "0.00")
                );

                itemRows.append("""
                        <tr>
                            <td style="padding:12px;border-bottom:1px solid #eee;">
                                %s
                            </td>
                            <td style="padding:12px;text-align:center;border-bottom:1px solid #eee;">
                                %s
                            </td>
                            <td style="padding:12px;text-align:right;border-bottom:1px solid #eee;">
                                ₹ %s
                            </td>
                        </tr>
                        """.formatted(
                        productName,
                        item.quantity(),
                        unitPrice
                ));
            }

        } else {

            itemRows.append("""
                    <tr>
                        <td colspan="3" style="padding:12px;text-align:center;">
                            No item details available
                        </td>
                    </tr>
                    """);
        }

        String safeFirstName = HtmlUtils.htmlEscape(firstName);

        String formattedTotal = NumberFormat
                .getCurrencyInstance(Locale.forLanguageTag("en-IN"))
                .format(event.totalAmount());

        return """
                <!DOCTYPE html>
                <html>
                <body style="margin:0;padding:20px;background-color:#f4f4f4;
                             font-family:Arial,sans-serif;color:#333;">

                    <div style="max-width:650px;margin:auto;background:#fff;
                                padding:30px;border-radius:8px;">

                        <h2 style="color:#2563eb;">
                            Order Received!
                        </h2>

                        <p>Hi %s,</p>

                        <p>
                            Thank you for shopping with ShopSphere.
                            We have received your order and it is currently being processed.
                            We will send you another email once your order is confirmed.
                        </p>

                        <div style="background:#f0f7ff;padding:15px;
                                    border-radius:5px;margin:20px 0;">

                            <strong>Order ID:</strong> #%s

                        </div>

                        <h3>Order Summary</h3>

                        <table style="width:100%%;border-collapse:collapse;">

                            <thead>
                                <tr style="background:#f4f4f4;">
                                    <th style="padding:12px;text-align:left;">
                                        Product
                                    </th>
                                    <th style="padding:12px;">
                                        Quantity
                                    </th>
                                    <th style="padding:12px;text-align:right;">
                                        Unit Price
                                    </th>
                                </tr>
                            </thead>

                            <tbody>
                                %s
                            </tbody>

                        </table>

                        <div style="text-align:right;margin-top:20px;">
                            <h3>Total Amount: %s</h3>
                        </div>

                        <p>
                            Thank you for choosing ShopSphere.
                            We appreciate your business!
                        </p>

                        <p>
                            Regards,<br>
                            <strong>ShopSphere Team</strong>
                        </p>

                    </div>

                </body>
                </html>
                """.formatted(
                safeFirstName,
                event.orderId(),
                itemRows,
                HtmlUtils.htmlEscape(formattedTotal)
        );
    }
}
