package com.taskmanager.security;

import com.taskmanager.config.PasswordResetEmailProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.util.UriComponentsBuilder;

public class SmtpPasswordResetTokenDelivery implements PasswordResetTokenDelivery {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(SmtpPasswordResetTokenDelivery.class);

    private final JavaMailSender mailSender;
    private final PasswordResetEmailProperties properties;

    public SmtpPasswordResetTokenDelivery(
        JavaMailSender mailSender,
        PasswordResetEmailProperties properties
    ) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public void deliver(String email, String rawResetToken) {
        String resetLink = UriComponentsBuilder.fromUriString(properties.resetUrl())
            .queryParam("token", rawResetToken)
            .build()
            .encode()
            .toUriString();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.from());
        message.setTo(email);
        message.setSubject("Reset your Task Management API password");
        message.setText("Use this secure link to reset your password. If you did not request "
            + "a reset, you can ignore this message.\n\n" + resetLink);

        try {
            mailSender.send(message);
        } catch (MailException exception) {
            LOGGER.error(
                "Password-reset email delivery failed ({}).",
                exception.getClass().getSimpleName()
            );
        }
    }
}