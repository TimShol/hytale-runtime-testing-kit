package com.frotty27.hrtk.api.assert_;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PhysicsAssertTest {

    @Nested
    class AssertVelocity {

        @Test
        void assertVelocity_withNullStore_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> PhysicsAssert.assertVelocity(null, new Object(), 0.0, -9.8, 0.0, 0.5));
        }

        @Test
        void assertVelocity_withNullRef_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> PhysicsAssert.assertVelocity(new Object(), null, 0.0, -9.8, 0.0, 0.5));
        }
    }

    @Nested
    class AssertSpeed {

        @Test
        void assertSpeed_withNullStore_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> PhysicsAssert.assertSpeed(null, new Object(), 0.0, 5.0));
        }

        @Test
        void assertSpeed_withNullRef_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> PhysicsAssert.assertSpeed(new Object(), null, 0.0, 5.0));
        }
    }

    @Nested
    class AssertOnGround {

        @Test
        void assertOnGround_withNullStore_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> PhysicsAssert.assertOnGround(null, new Object()));
        }

        @Test
        void assertOnGround_withNullRef_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> PhysicsAssert.assertOnGround(new Object(), null));
        }
    }

    @Nested
    class AssertInAir {

        @Test
        void assertInAir_withNullStore_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> PhysicsAssert.assertInAir(null, new Object()));
        }

        @Test
        void assertInAir_withNullRef_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> PhysicsAssert.assertInAir(new Object(), null));
        }
    }

    @Nested
    class AssertStationary {

        @Test
        void assertStationary_withNullStore_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> PhysicsAssert.assertStationary(null, new Object(), 0.01));
        }

        @Test
        void assertStationary_withNullRef_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> PhysicsAssert.assertStationary(new Object(), null, 0.01));
        }
    }
}
