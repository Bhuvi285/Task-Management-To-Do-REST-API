package com.taskmanager.security;

public interface PasswordResetTokenDelivery {

    void deliver(String email, String rawResetToken);
}
