package com.taskmanager.config;

import com.taskmanager.security.PasswordResetTokenDelivery;
import com.taskmanager.security.SmtpPasswordResetTokenDelivery;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration(proxyBeanMethods = false)
@Profile("prod")
@ConditionalOnProperty(
    prefix = "app.security.password-reset-email",
    name = "enabled",
    havingValue = "true"
)
@EnableConfigurationProperties(PasswordResetEmailProperties.class)
public class PasswordResetEmailConfiguration {

    @Bean
    public PasswordResetTokenDelivery passwordResetTokenDelivery(
        JavaMailSender mailSender,
        PasswordResetEmailProperties properties
    ) {
        return new SmtpPasswordResetTokenDelivery(mailSender, properties);
    }
}