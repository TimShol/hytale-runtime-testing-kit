package com.frotty27.hrtk.api.assert_;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HytaleAssertTest {

    @Nested
    class AssertEquals {

        @Test
        void withEqualValues_passes() {
            assertDoesNotThrow(() -> HytaleAssert.assertEquals("hello", "hello"));
        }

        @Test
        void withDifferentValues_throwsWithBothValues() {
            var exception = assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertEquals("foo", "bar"));
            assertTrue(exception.getMessage().contains("foo"));
            assertTrue(exception.getMessage().contains("bar"));
        }

        @Test
        void withNullExpectedAndNullActual_passes() {
            assertDoesNotThrow(() -> HytaleAssert.assertEquals(null, null));
        }

        @Test
        void withNullExpectedAndNonNullActual_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertEquals(null, "value"));
        }

        @Test
        void withNonNullExpectedAndNullActual_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertEquals("value", null));
        }

        @Test
        void withCustomMessage_includesMessageInError() {
            var exception = assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertEquals("my context", "a", "b"));
            assertTrue(exception.getMessage().contains("my context"));
        }

        @Test
        void withEqualIntegers_passes() {
            assertDoesNotThrow(() -> HytaleAssert.assertEquals(42, 42));
        }

        @Test
        void withEqualLists_passes() {
            assertDoesNotThrow(() -> HytaleAssert.assertEquals(
                    List.of(1, 2, 3), List.of(1, 2, 3)));
        }
    }

    @Nested
    class AssertNotEquals {

        @Test
        void withDifferentValues_passes() {
            assertDoesNotThrow(() -> HytaleAssert.assertNotEquals("foo", "bar"));
        }

        @Test
        void withEqualValues_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertNotEquals("same", "same"));
        }

        @Test
        void withBothNull_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertNotEquals(null, null));
        }
    }

    @Nested
    class AssertTrue {

        @Test
        void withTrue_passes() {
            assertDoesNotThrow(() -> HytaleAssert.assertTrue(true));
        }

        @Test
        void withFalse_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertTrue(false));
        }

        @Test
        void withCustomMessage_includesMessage() {
            var exception = assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertTrue("check failed", false));
            assertTrue(exception.getMessage().contains("check failed"));
        }
    }

    @Nested
    class AssertFalse {

        @Test
        void withFalse_passes() {
            assertDoesNotThrow(() -> HytaleAssert.assertFalse(false));
        }

        @Test
        void withTrue_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertFalse(true));
        }
    }

    @Nested
    class AssertNull {

        @Test
        void withNull_passes() {
            assertDoesNotThrow(() -> HytaleAssert.assertNull(null));
        }

        @Test
        void withNonNull_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertNull("not null"));
        }
    }

    @Nested
    class AssertNotNull {

        @Test
        void withNonNull_passes() {
            assertDoesNotThrow(() -> HytaleAssert.assertNotNull("value"));
        }

        @Test
        void withNull_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertNotNull(null));
        }

        @Test
        void withCustomMessage_includesMessage() {
            var exception = assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertNotNull("entity ref", null));
            assertTrue(exception.getMessage().contains("entity ref"));
        }
    }

    @Nested
    class AssertThrowsTest {

        @Test
        void withExpectedException_passes() {
            var caught = HytaleAssert.assertThrows(IllegalArgumentException.class,
                    () -> { throw new IllegalArgumentException("bad"); });
            assertNotNull(caught);
            assertEquals("bad", caught.getMessage());
        }

        @Test
        void withNoException_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertThrows(RuntimeException.class, () -> {}));
        }

        @Test
        void withWrongExceptionType_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertThrows(IllegalStateException.class,
                            () -> { throw new IllegalArgumentException("wrong type"); }));
        }
    }

    @Nested
    class AssertDoesNotThrow {

        @Test
        void withNoException_passes() {
            assertDoesNotThrow(() -> HytaleAssert.assertDoesNotThrow(() -> {}));
        }

        @Test
        void withException_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertDoesNotThrow(() -> { throw new RuntimeException("boom"); }));
        }
    }

    @Nested
    class AssertTimeout {

        @Test
        void withFastExecution_passes() {
            assertDoesNotThrow(() -> HytaleAssert.assertTimeout(Duration.ofSeconds(1), () -> {}));
        }

        @Test
        void withSlowExecution_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertTimeout(Duration.ofMillis(10),
                            () -> { try { Thread.sleep(5000); } catch (InterruptedException _) {} }));
        }
    }

    @Nested
    class AssertArrayEquals {

        @Test
        void withEqualArrays_passes() {
            assertDoesNotThrow(() -> HytaleAssert.assertArrayEquals(
                    new Integer[]{1, 2, 3}, new Integer[]{1, 2, 3}));
        }

        @Test
        void withDifferentArrays_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertArrayEquals(
                            new Integer[]{1, 2}, new Integer[]{1, 3}));
        }

        @Test
        void withBothEmpty_passes() {
            assertDoesNotThrow(() -> HytaleAssert.assertArrayEquals(
                    new Object[]{}, new Object[]{}));
        }
    }

    @Nested
    class CollectionAssertions {

        @Test
        void assertContains_withPresent_passes() {
            assertDoesNotThrow(() -> HytaleAssert.assertContains(
                    List.of("a", "b", "c"), "b"));
        }

        @Test
        void assertContains_withAbsent_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertContains(List.of("a", "b"), "z"));
        }

        @Test
        void assertContains_withNullCollection_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertContains(null, "x"));
        }

        @Test
        void assertNotContains_withAbsent_passes() {
            assertDoesNotThrow(() -> HytaleAssert.assertNotContains(
                    List.of("a", "b"), "z"));
        }

        @Test
        void assertNotContains_withPresent_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertNotContains(List.of("a", "b"), "a"));
        }

        @Test
        void assertEmpty_withEmpty_passes() {
            assertDoesNotThrow(() -> HytaleAssert.assertEmpty(Collections.emptyList()));
        }

        @Test
        void assertEmpty_withNonEmpty_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertEmpty(List.of("item")));
        }

        @Test
        void assertNotEmpty_withNonEmpty_passes() {
            assertDoesNotThrow(() -> HytaleAssert.assertNotEmpty(List.of("item")));
        }

        @Test
        void assertNotEmpty_withEmpty_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertNotEmpty(Collections.emptyList()));
        }
    }

    @Nested
    class StringAssertions {

        @Test
        void assertMatches_withMatch_passes() {
            assertDoesNotThrow(() -> HytaleAssert.assertMatches("\\d+", "42"));
        }

        @Test
        void assertMatches_withNoMatch_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertMatches("\\d+", "abc"));
        }

        @Test
        void assertContainsString_withSubstring_passes() {
            assertDoesNotThrow(() -> HytaleAssert.assertContainsString("world", "hello world"));
        }

        @Test
        void assertContainsString_withoutSubstring_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertContainsString("xyz", "hello"));
        }
    }

    @Nested
    class NumericAssertions {

        @Test
        void assertEqualsDouble_withinDelta_passes() {
            assertDoesNotThrow(() -> HytaleAssert.assertEquals(3.14, 3.15, 0.02));
        }

        @Test
        void assertEqualsDouble_outsideDelta_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertEquals(3.14, 4.0, 0.01));
        }

        @Test
        void assertGreaterThan_withGreaterValue_passes() {
            assertDoesNotThrow(() -> HytaleAssert.assertGreaterThan(5, 10));
        }

        @Test
        void assertGreaterThan_withEqualValue_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertGreaterThan(5, 5));
        }

        @Test
        void assertGreaterThan_withLesserValue_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertGreaterThan(5, 3));
        }

        @Test
        void assertLessThan_withLesserValue_passes() {
            assertDoesNotThrow(() -> HytaleAssert.assertLessThan(10, 5));
        }

        @Test
        void assertLessThan_withEqualValue_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertLessThan(5, 5));
        }
    }

    @Nested
    class ReferenceAssertions {

        @Test
        void assertSame_withSameInstance_passes() {
            Object obj = new Object();
            assertDoesNotThrow(() -> HytaleAssert.assertSame(obj, obj));
        }

        @Test
        void assertSame_withDifferentInstances_throws() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertSame(new Object(), new Object()));
        }

        @Test
        void assertNotSame_withDifferentInstances_passes() {
            assertDoesNotThrow(() -> HytaleAssert.assertNotSame(new Object(), new Object()));
        }

        @Test
        void assertNotSame_withSameInstance_throws() {
            Object obj = new Object();
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.assertNotSame(obj, obj));
        }
    }

    @Nested
    class Fail {

        @Test
        void fail_alwaysThrows() {
            assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.fail("forced"));
        }

        @Test
        void fail_withFormat_includesArgs() {
            var exception = assertThrows(AssertionFailedException.class,
                    () -> HytaleAssert.fail("value is %d", 42));
            assertTrue(exception.getMessage().contains("42"));
        }
    }
}
