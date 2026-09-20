package com.shopsphere.paymentservice.gateway;

import com.shopsphere.paymentservice.dto.response.PaymentGatewayResponse;
import com.shopsphere.paymentservice.entity.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class MockPaymentGateway implements PaymentGateway{
    @Override
    public PaymentGatewayResponse processPayment(Payment payment) {
        log.info(
                "Processing payment through mock gateway. paymentId :: {}",
                payment.getId()
        );

        boolean successful = payment.getAmount()
                .compareTo(BigDecimal.valueOf(100000)) <= 0;

        if (successful) {

            return new PaymentGatewayResponse(
                    true,
                    payment.getTransactionId(),
                    "Payment processed successfully"
            );
        }

        return new PaymentGatewayResponse(
                false,
                payment.getTransactionId(),
                "Payment declined by payment gateway"
        );
    }
}
