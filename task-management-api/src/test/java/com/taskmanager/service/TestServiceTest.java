package com.taskmanager.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestServiceTest {

    @Test
    void getMessageReturnsExpectedMessage() {
        TestService testService = new TestService();

        assertEquals(
            "Spring Boot IoC and DI are working successfully!",
            testService.getMessage()
        );
    }
}
