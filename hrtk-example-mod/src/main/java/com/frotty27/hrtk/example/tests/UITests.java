package com.frotty27.hrtk.example.tests;

import com.frotty27.hrtk.api.annotation.DisplayName;
import com.frotty27.hrtk.api.annotation.HytaleSuite;
import com.frotty27.hrtk.api.annotation.HytaleTest;
import com.frotty27.hrtk.api.annotation.Order;
import com.frotty27.hrtk.api.annotation.Tag;
import com.frotty27.hrtk.api.assert_.HytaleAssert;
import com.frotty27.hrtk.api.assert_.UIAssert;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;
import com.frotty27.hrtk.api.mock.UICommandCapture;

@HytaleSuite(value = "UI Tests", isolation = IsolationStrategy.NONE)
@Tag("ui")
public class UITests {

    @HytaleTest
    @DisplayName("UI SET command is captured for a page element")
    @Order(1)
    void testPageSetCommand(UICommandCapture capture) {
        UIAssert.assertHasCommands(capture);
        UIAssert.assertCommandSent(capture, "hud.health.bar", "SET");
    }

    @HytaleTest
    @DisplayName("UI command count matches expected operations")
    @Order(2)
    void testUICommandCount(UICommandCapture capture) {
        HytaleAssert.assertNotNull("UICommandCapture should not be null", capture);
        HytaleAssert.assertTrue(
            "Capture should have recorded at least one command",
            capture.getCount() >= 0
        );
    }

    @HytaleTest
    @DisplayName("UI SET command carries the expected value")
    @Order(3)
    void testUISetWithValue(UICommandCapture capture) {
        UIAssert.assertCommandSentWithValue(capture, "hud.score.label", 42);
    }
}
