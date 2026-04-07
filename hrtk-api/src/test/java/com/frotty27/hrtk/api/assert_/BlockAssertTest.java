package com.frotty27.hrtk.api.assert_;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class BlockAssertTest {

    @Nested
    class AssertBlockMaterial {

        @Test
        void assertBlockMaterial_withNullBlockType_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> BlockAssert.assertBlockMaterial(null, "stone"));
        }

        @Test
        void assertBlockMaterial_withNullExpectedMaterial_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> BlockAssert.assertBlockMaterial(new Object(), null));
        }
    }

    @Nested
    class AssertBlockIsTrigger {

        @Test
        void assertBlockIsTrigger_withNullBlockType_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> BlockAssert.assertBlockIsTrigger(null));
        }
    }

    @Nested
    class AssertBlockNotTrigger {

        @Test
        void assertBlockNotTrigger_withNullBlockType_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> BlockAssert.assertBlockNotTrigger(null));
        }
    }

    @Nested
    class AssertBlockState {

        @Test
        void assertBlockState_withNullBlockType_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> BlockAssert.assertBlockState(null, "default"));
        }

        @Test
        void assertBlockState_withNullExpectedState_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> BlockAssert.assertBlockState(new Object(), null));
        }
    }

    @Nested
    class AssertBlockGroup {

        @Test
        void assertBlockGroup_withNullBlockType_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> BlockAssert.assertBlockGroup(null, "natural"));
        }

        @Test
        void assertBlockGroup_withNullExpectedGroup_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> BlockAssert.assertBlockGroup(new Object(), null));
        }
    }
}
