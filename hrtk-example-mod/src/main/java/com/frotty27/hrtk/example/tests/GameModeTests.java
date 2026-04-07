package com.frotty27.hrtk.example.tests;

import com.frotty27.hrtk.api.annotation.DisplayName;
import com.frotty27.hrtk.api.annotation.HytaleSuite;
import com.frotty27.hrtk.api.annotation.HytaleTest;
import com.frotty27.hrtk.api.annotation.Order;
import com.frotty27.hrtk.api.annotation.Tag;
import com.frotty27.hrtk.api.assert_.HytaleAssert;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.asset.type.gamemode.GameModeType;

@HytaleSuite(value = "Game Mode Tests", isolation = IsolationStrategy.NONE)
@Tag("world")
public class GameModeTests {

    @HytaleTest
    @DisplayName("GameMode enum has Adventure and Creative")
    @Order(1)
    void gameModeValues() {
        HytaleAssert.assertNotNull(GameMode.Adventure);
        HytaleAssert.assertNotNull(GameMode.Creative);
        HytaleAssert.assertEquals(2, GameMode.values().length);
    }

    @HytaleTest
    @DisplayName("GameModeType asset is resolvable from GameMode enum")
    @Order(2)
    void gameModeTypeResolvable() {
        var adventureType = GameModeType.fromGameMode(GameMode.Adventure);
        HytaleAssert.assertNotNull("Adventure GameModeType should resolve", adventureType);

        var creativeType = GameModeType.fromGameMode(GameMode.Creative);
        HytaleAssert.assertNotNull("Creative GameModeType should resolve", creativeType);
    }
}
