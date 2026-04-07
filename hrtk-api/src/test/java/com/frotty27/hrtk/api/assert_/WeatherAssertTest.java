package com.frotty27.hrtk.api.assert_;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class WeatherAssertTest {

    @Nested
    class AssertWeather {

        @Test
        void assertWeather_withNullWorld_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> WeatherAssert.assertWeather(null, "Rain"));
        }

        @Test
        void assertWeather_withNullExpectedWeatherId_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> WeatherAssert.assertWeather(new Object(), null));
        }
    }

    @Nested
    class AssertNotWeather {

        @Test
        void assertNotWeather_withNullWorld_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> WeatherAssert.assertNotWeather(null, "Clear"));
        }

        @Test
        void assertNotWeather_withNullUnexpectedWeatherId_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> WeatherAssert.assertNotWeather(new Object(), null));
        }
    }
}
