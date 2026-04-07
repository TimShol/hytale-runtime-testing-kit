package com.frotty27.hrtk.api.assert_;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ChunkAssertTest {

    @Nested
    class AssertChunkLoaded {

        @Test
        void assertChunkLoaded_withNullWorld_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> ChunkAssert.assertChunkLoaded(null, 0, 0));
        }
    }

    @Nested
    class AssertChunkNotLoaded {

        @Test
        void assertChunkNotLoaded_withNullWorld_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> ChunkAssert.assertChunkNotLoaded(null, 100, 100));
        }
    }
}
