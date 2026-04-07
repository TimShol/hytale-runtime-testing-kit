package com.frotty27.hrtk.example.tests;

import com.frotty27.hrtk.api.annotation.DisplayName;
import com.frotty27.hrtk.api.annotation.HytaleSuite;
import com.frotty27.hrtk.api.annotation.HytaleTest;
import com.frotty27.hrtk.api.annotation.Order;
import com.frotty27.hrtk.api.annotation.Tag;
import com.frotty27.hrtk.api.assert_.CodecAssert;
import com.frotty27.hrtk.api.assert_.HytaleAssert;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;
import com.hypixel.hytale.codec.Codec;

@HytaleSuite(value = "Codec Tests", isolation = IsolationStrategy.NONE)
@Tag("codec")
public class CodecTests {

    @HytaleTest
    @DisplayName("STRING codec round-trips correctly")
    @Order(1)
    void testStringCodecRoundTrip() {
        CodecAssert.assertRoundTrip(Codec.STRING, "Trork_Warrior");
    }

    @HytaleTest
    @DisplayName("INTEGER codec round-trips correctly")
    @Order(2)
    void testIntegerCodecRoundTrip() {
        CodecAssert.assertRoundTrip(Codec.INTEGER, 42);
    }

    @HytaleTest
    @DisplayName("BOOLEAN codec round-trips correctly")
    @Order(3)
    void testBooleanCodecRoundTrip() {
        CodecAssert.assertRoundTrip(Codec.BOOLEAN, true);
        CodecAssert.assertRoundTrip(Codec.BOOLEAN, false);
    }

    @HytaleTest
    @DisplayName("DOUBLE codec round-trips correctly")
    @Order(4)
    void testDoubleCodecRoundTrip() {
        CodecAssert.assertRoundTrip(Codec.DOUBLE, 3.14159);
    }

    @HytaleTest
    @DisplayName("Encode produces correct value that decodes back")
    @Order(5)
    void testEncodeDecodeExplicit() {
        String original = "Weapon_Sword_Iron";
        var encoded = Codec.STRING.encode(original);
        HytaleAssert.assertNotNull("Encoded value should not be null", encoded);

        var decoded = Codec.STRING.decode(encoded);
        HytaleAssert.assertEquals(original, decoded);
    }

    @HytaleTest
    @DisplayName("Codec rejects malformed input")
    @Order(6)
    void testMalformedDataRejection() {
        var badBson = new org.bson.BsonDocument(
            "wrong_field", new org.bson.BsonInt32(-1)
        );
        CodecAssert.assertDecodeThrows(Codec.STRING, badBson);
    }

    @HytaleTest
    @DisplayName("Null encoding is handled gracefully")
    @Order(7)
    void testNullHandling() {
        HytaleAssert.assertThrows(
            Exception.class,
            () -> Codec.STRING.encode(null)
        );
    }
}
