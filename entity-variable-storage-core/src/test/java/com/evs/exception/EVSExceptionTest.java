package com.evs.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EVSExceptionTest {

    @Test
    void shouldCreateWithMessage() {
        EVSException ex = new EVSException("Test error");
        assertEquals("Test error", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void shouldCreateWithMessageAndCause() {
        RuntimeException cause = new RuntimeException("cause");
        EVSException ex = new EVSException("Wrapper", cause);
        assertEquals("Wrapper", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void shouldCreateWithCauseOnly() {
        RuntimeException cause = new RuntimeException("cause");
        EVSException ex = new EVSException(cause);
        assertSame(cause, ex.getCause());
    }
}
