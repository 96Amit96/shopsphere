package com.shopsphere.paymentservice.kafka;

import com.shopsphere.paymentservice.dto.event.PaymentRequestedEvent;
import com.shopsphere.paymentservice.dto.request.PaymentRequest;
import com.shopsphere.paymentservice.enums.PaymentMethod;
import com.shopsphere.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentRequestedEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(
            topics = "payment-requests",
            containerFactory = "paymentRequestedKafkaListenerContainerFactory"
    )
    public void consumePaymentRequestedEvent(PaymentRequestedEvent event) {
        log.info(
                "Received PaymentRequestedEvent. eventId :: {}, orderId :: {}, userId :: {}, amount :: {}, paymentMethod :: {}",
                event.eventId(),
                event.orderId(),
                event.userId(),
                event.amount(),
                event.paymentMethod()
        );

        /*PaymentMethod paymentMethod = PaymentMethod.valueOf(event.paymentMethod());

        PaymentRequest request = new PaymentRequest(
                event.orderId(),
                paymentMethod
        );

        paymentService.createPayment(request);*/

        paymentService.createPaymentFromEvent(event);

        log.info(
                "Payment request processed. orderId :: {}",
                event.orderId()
        );
    }
}
