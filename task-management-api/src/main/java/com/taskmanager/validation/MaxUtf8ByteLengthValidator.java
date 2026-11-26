package com.taskmanager.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;

public class MaxUtf8ByteLengthValidator
    implements ConstraintValidator<MaxUtf8ByteLength, String> {

    private int maxBytes;

    @Override
    public void initialize(MaxUtf8ByteLength constraintAnnotation) {
        maxBytes = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null
            || value.getBytes(StandardCharsets.UTF_8).length <= maxBytes;
    }
}
