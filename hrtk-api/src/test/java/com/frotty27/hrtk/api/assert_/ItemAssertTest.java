package com.frotty27.hrtk.api.assert_;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemAssertTest {

    @Nested
    class AssertItemId {

        @Test
        void assertItemId_withNullStack_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> ItemAssert.assertItemId(null, "stone"));
        }

        @Test
        void assertItemId_withNullExpectedId_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> ItemAssert.assertItemId(new Object(), null));
        }
    }

    @Nested
    class AssertItemQuantity {

        @Test
        void assertItemQuantity_withNullStack_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> ItemAssert.assertItemQuantity(null, 5));
        }
    }

    @Nested
    class AssertItemNotEmpty {

        @Test
        void assertItemNotEmpty_withNullStack_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> ItemAssert.assertItemNotEmpty(null));
        }
    }

    @Nested
    class AssertItemEmpty {

        @Test
        void assertItemEmpty_withNullStack_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> ItemAssert.assertItemEmpty(null));
        }
    }

    @Nested
    class AssertItemBroken {

        @Test
        void assertItemBroken_withNullStack_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> ItemAssert.assertItemBroken(null));
        }
    }

    @Nested
    class AssertItemNotBroken {

        @Test
        void assertItemNotBroken_withNullStack_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> ItemAssert.assertItemNotBroken(null));
        }
    }

    @Nested
    class AssertItemDurability {

        @Test
        void assertItemDurability_withNullStack_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> ItemAssert.assertItemDurability(null, 80.0, 1.0));
        }
    }

    @Nested
    class AssertItemStackable {

        @Test
        void assertItemStackable_withNullStackA_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> ItemAssert.assertItemStackable(null, new Object()));
        }

        @Test
        void assertItemStackable_withNullStackB_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> ItemAssert.assertItemStackable(new Object(), null));
        }
    }

    @Nested
    class AssertItemMetadata {

        @Test
        void assertItemMetadata_withNullStack_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> ItemAssert.assertItemMetadata(null, "key", "value"));
        }

        @Test
        void assertItemMetadata_withNullKey_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> ItemAssert.assertItemMetadata(new Object(), null, "value"));
        }

        @Test
        void assertItemMetadata_withNullExpectedValue_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> ItemAssert.assertItemMetadata(new Object(), "key", null));
        }
    }
}
