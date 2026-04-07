package com.frotty27.hrtk.api.assert_;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PermissionsAssertTest {

    @Nested
    class AssertHasPermission {

        @Test
        void assertHasPermission_withNullHolder_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> PermissionsAssert.assertHasPermission(null, "hytale.admin.kick"));
        }

        @Test
        void assertHasPermission_withNullPermission_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> PermissionsAssert.assertHasPermission(new Object(), null));
        }
    }

    @Nested
    class AssertNoPermission {

        @Test
        void assertNoPermission_withNullHolder_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> PermissionsAssert.assertNoPermission(null, "hytale.admin.ban"));
        }

        @Test
        void assertNoPermission_withNullPermission_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> PermissionsAssert.assertNoPermission(new Object(), null));
        }
    }
}
