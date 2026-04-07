package com.frotty27.hrtk.api.assert_;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerAssertTest {

    @Nested
    class AssertGameMode {

        @Test
        void assertGameMode_withNullPlayer_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> PlayerAssert.assertGameMode(null, "CREATIVE"));
        }

        @Test
        void assertGameMode_withNullExpectedGameMode_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> PlayerAssert.assertGameMode(new Object(), null));
        }
    }

    @Nested
    class AssertPlayerName {

        @Test
        void assertPlayerName_withNullPlayer_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> PlayerAssert.assertPlayerName(null, "Frotty27"));
        }

        @Test
        void assertPlayerName_withNullExpectedName_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> PlayerAssert.assertPlayerName(new Object(), null));
        }
    }

    @Nested
    class AssertPlayerInWorld {

        @Test
        void assertPlayerInWorld_withNullPlayer_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> PlayerAssert.assertPlayerInWorld(null, "overworld"));
        }

        @Test
        void assertPlayerInWorld_withNullExpectedWorldName_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> PlayerAssert.assertPlayerInWorld(new Object(), null));
        }
    }

    @Nested
    class AssertPlayerAlive {

        @Test
        void assertPlayerAlive_withNullStore_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> PlayerAssert.assertPlayerAlive(null, new Object()));
        }

        @Test
        void assertPlayerAlive_withNullRef_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> PlayerAssert.assertPlayerAlive(new Object(), null));
        }
    }
}
