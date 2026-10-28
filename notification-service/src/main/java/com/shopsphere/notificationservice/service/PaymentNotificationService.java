package com.shopsphere.notificationservice.service;

import com.shopsphere.notificationservice.dto.event.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@Slf4j
public class PaymentNotificationService {
    public void notifyPaymentStatus(PaymentEvent event) {

        String status = event.paymentStatus() == null
                ? "UNKNOWN"
                : event.paymentStatus().toUpperCase(Locale.ROOT);

        switch (status) {
            case "SUCCESS" ->
                    log.info(
                            "Payment successful notification: orderId={}, paymentId={}, "
                                    + "userId={}, amount={}, transactionId={}",
                            event.orderId(),
                            event.paymentId(),
                            event.userId(),
                            event.amount(),
                            event.transactionId()
                    );

            case "FAILED" ->
                    log.warn(
                            "Payment failed notification: orderId={}, paymentId={}, "
                                    + "userId={}, amount={}",
                            event.orderId(),
                            event.paymentId(),
                            event.userId(),
                            event.amount()
                    );

            case "REFUNDED" ->
                    log.info(
                            "Payment refunded notification: orderId={}, paymentId={}, "
                                    + "userId={}, amount={}",
                            event.orderId(),
                            event.paymentId(),
                            event.userId(),
                            event.amount()
                    );

            default ->
                    log.info(
                            "Payment status notification: orderId={}, paymentId={}, "
                                    + "status={}",
                            event.orderId(),
                            event.paymentId(),
                            status
                    );
        }
    }
}
