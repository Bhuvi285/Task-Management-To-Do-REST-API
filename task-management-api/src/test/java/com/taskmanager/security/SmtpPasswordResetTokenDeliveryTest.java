package com.taskmanager.security;

import com.taskmanager.config.PasswordResetEmailProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SmtpPasswordResetTokenDeliveryTest {

    @Test
    void sendsResetLinkToTheRequestedAddress() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        SmtpPasswordResetTokenDelivery delivery = new SmtpPasswordResetTokenDelivery(
            mailSender,
            new PasswordResetEmailProperties(
                true,
                "no-reply@example.test",
                "https://app.example.test/reset-password"
            )
        );

        delivery.deliver("user@example.test", "opaque-token");

        var messageCaptor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage message = messageCaptor.getValue();
        assertThat(message.getFrom()).isEqualTo("no-reply@example.test");
        assertThat(message.getTo()).containsExactly("user@example.test");
        assertThat(message.getText())
            .contains("https://app.example.test/reset-password?token=opaque-token");
    }

    @Test
    void doesNotPropagateSensitiveMailTransportErrorsToTheCaller() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        org.springframework.mail.MailSendException failure =
            new org.springframework.mail.MailSendException("transport failure");
        org.mockito.Mockito.doThrow(failure).when(mailSender).send(any(SimpleMailMessage.class));
        SmtpPasswordResetTokenDelivery delivery = new SmtpPasswordResetTokenDelivery(
            mailSender,
            new PasswordResetEmailProperties(
                true,
                "no-reply@example.test",
                "https://app.example.test/reset-password"
            )
        );

        delivery.deliver("user@example.test", "opaque-token");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}