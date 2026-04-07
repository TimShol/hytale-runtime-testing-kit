package com.frotty27.hrtk.api.assert_;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class NPCAssertTest {

    @Nested
    class AssertRoleName {

        @Test
        void assertRoleName_withNullStore_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> NPCAssert.assertRoleName(null, new Object(), "merchant"));
        }

        @Test
        void assertRoleName_withNullRef_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> NPCAssert.assertRoleName(new Object(), null, "merchant"));
        }

        @Test
        void assertRoleName_withNullExpectedRole_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> NPCAssert.assertRoleName(new Object(), new Object(), null));
        }
    }

    @Nested
    class AssertNotDespawning {

        @Test
        void assertNotDespawning_withNullStore_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> NPCAssert.assertNotDespawning(null, new Object()));
        }

        @Test
        void assertNotDespawning_withNullRef_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> NPCAssert.assertNotDespawning(new Object(), null));
        }
    }

    @Nested
    class AssertRoleExists {

        @Test
        void assertRoleExists_withNullRoleName_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> NPCAssert.assertRoleExists(null));
        }
    }

    @Nested
    class AssertNPCEntity {

        @Test
        void assertNPCEntity_withNullStore_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> NPCAssert.assertNPCEntity(null, new Object()));
        }

        @Test
        void assertNPCEntity_withNullRef_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> NPCAssert.assertNPCEntity(new Object(), null));
        }
    }

    @Nested
    class AssertLeashPoint {

        @Test
        void assertLeashPoint_withNullStore_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> NPCAssert.assertLeashPoint(null, new Object()));
        }

        @Test
        void assertLeashPoint_withNullRef_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> NPCAssert.assertLeashPoint(new Object(), null));
        }
    }
}
