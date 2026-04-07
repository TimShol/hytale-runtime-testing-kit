package com.frotty27.hrtk.api.assert_;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatAssertTest {

    @Nested
    class AssertMessageContains {

        @Test
        void assertMessageContains_withSubstringPresent_passes() {
            assertDoesNotThrow(
                    () -> ChatAssert.assertMessageContains("Welcome to Hytale!", "Welcome"));
        }

        @Test
        void assertMessageContains_withExactMatch_passes() {
            assertDoesNotThrow(
                    () -> ChatAssert.assertMessageContains("hello", "hello"));
        }

        @Test
        void assertMessageContains_withSubstringAbsent_throwsWithUsefulMessage() {
            var exception = assertThrows(AssertionFailedException.class,
                    () -> ChatAssert.assertMessageContains("Welcome to Hytale!", "Goodbye"));
            assertTrue(exception.getMessage().contains("Goodbye"),
                    "Error should mention the expected substring");
            assertTrue(exception.getMessage().contains("Welcome to Hytale!"),
                    "Error should mention the actual message");
        }

        @Test
        void assertMessageContains_withNullMessage_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> ChatAssert.assertMessageContains(null, "test"));
        }

        @Test
        void assertMessageContains_withNullExpected_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> ChatAssert.assertMessageContains("hello", null));
        }

        @Test
        void assertMessageContains_withEmptySubstring_passes() {
            assertDoesNotThrow(
                    () -> ChatAssert.assertMessageContains("any message", ""));
        }

        @Test
        void assertMessageContains_withCaseMismatch_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> ChatAssert.assertMessageContains("Hello", "hello"));
        }
    }

    @Nested
    class AssertMessageNotEmpty {

        @Test
        void assertMessageNotEmpty_withNonEmptyString_passes() {
            assertDoesNotThrow(
                    () -> ChatAssert.assertMessageNotEmpty("Hello!"));
        }

        @Test
        void assertMessageNotEmpty_withSingleCharacter_passes() {
            assertDoesNotThrow(
                    () -> ChatAssert.assertMessageNotEmpty("x"));
        }

        @Test
        void assertMessageNotEmpty_withEmptyString_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> ChatAssert.assertMessageNotEmpty(""));
        }

        @Test
        void assertMessageNotEmpty_withNullMessage_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> ChatAssert.assertMessageNotEmpty(null));
        }

        @Test
        void assertMessageNotEmpty_withWhitespaceOnly_passes() {
            assertDoesNotThrow(
                    () -> ChatAssert.assertMessageNotEmpty("   "));
        }
    }
}
