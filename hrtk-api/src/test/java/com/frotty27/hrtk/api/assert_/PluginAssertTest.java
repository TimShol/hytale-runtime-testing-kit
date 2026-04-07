package com.frotty27.hrtk.api.assert_;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PluginAssertTest {

    @Test
    void assertPluginLoaded_withNullPluginName_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> PluginAssert.assertPluginLoaded(null));
    }
}
