package com.frotty27.hrtk.api.assert_;

import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Assertions for codec serialization round-trips and decode validation.
 *
 * <p>Uses {@code Object} types to avoid coupling to HytaleServer.jar codec classes.
 * At runtime, {@code codec} is a {@code Codec<T>} and {@code bsonValue} is a
 * {@code BsonValue}. Codec methods are invoked reflectively.</p>
 *
 * <pre>{@code
 * CodecAssert.assertRoundTrip(myCodec, new MyData("hello", 42));
 * CodecAssert.assertDecodeEquals(myCodec, bsonDoc, expectedObject);
 * CodecAssert.assertDecodeThrows(myCodec, malformedBson);
 * }</pre>
 *
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class CodecAssert {

    private CodecAssert() {}

    /**
     * Encodes a value and decodes it back, asserting the result equals the original
     * using {@link Objects#equals(Object, Object)}.
     *
     * <p>Failure message: {@code "Round-trip failed: original <value> != decoded <decoded>"}</p>
     *
     * @param codec the codec object (runtime type: {@code Codec<T>})
     * @param value the value to encode and then decode
     * @param <T>   the value type
     * @throws AssertionFailedException if the round-trip result does not equal the original
     */
    public static <T> void assertRoundTrip(Object codec, T value) {
        assertRoundTrip(codec, value, Objects::equals);
    }

    /**
     * Encodes a value and decodes it back, asserting equality via a custom predicate.
     *
     * <p>Useful when the value type does not implement {@code equals} or requires
     * custom comparison logic (e.g., floating-point tolerance).</p>
     *
     * <p>Failure message: {@code "Round-trip failed: original <value> != decoded <decoded>"}</p>
     *
     * @param codec         the codec object (runtime type: {@code Codec<T>})
     * @param value         the value to encode and then decode
     * @param equalityCheck a predicate that returns {@code true} if the two values are considered equal
     * @param <T>           the value type
     * @throws AssertionFailedException if the round-trip result fails the equality check
     */
    @SuppressWarnings("unchecked")
    public static <T> void assertRoundTrip(Object codec, T value, BiPredicate<T, T> equalityCheck) {
        try {
            var encodeMethod = findMethod(codec, "encode");
            Object encoded;
            if (encodeMethod.getParameterCount() == 2) {
                encoded = encodeMethod.invoke(codec, value, null);
            } else {
                encoded = encodeMethod.invoke(codec, value);
            }

            var decodeMethod = findMethod(codec, "decode");
            T decoded;
            if (decodeMethod.getParameterCount() == 2) {
                decoded = (T) decodeMethod.invoke(codec, encoded, null);
            } else {
                decoded = (T) decodeMethod.invoke(codec, encoded);
            }

            if (!equalityCheck.test(value, decoded)) {
                HytaleAssert.fail("Round-trip failed: original <%s> != decoded <%s>", value, decoded);
            }
        } catch (AssertionFailedException e) {
            throw e;
        } catch (Exception e) {
            HytaleAssert.fail("Round-trip encode/decode threw: %s", e.getMessage());
        }
    }

    /**
     * Decodes a BSON value and asserts the result equals the expected value using
     * {@link HytaleAssert#assertEquals(Object, Object)}.
     *
     * <p>Failure message: {@code "Expected <expected> but was <decoded>"}</p>
     *
     * @param codec     the codec object (runtime type: {@code Codec<T>})
     * @param bsonValue the BSON value to decode (runtime type: {@code BsonValue})
     * @param expected  the expected decoded result
     * @param <T>       the value type
     * @throws AssertionFailedException if the decoded result does not equal the expected value
     */
    @SuppressWarnings("unchecked")
    public static <T> void assertDecodeEquals(Object codec, Object bsonValue, T expected) {
        try {
            var decodeMethod = findMethod(codec, "decode");
            T decoded;
            if (decodeMethod.getParameterCount() == 2) {
                decoded = (T) decodeMethod.invoke(codec, bsonValue, null);
            } else {
                decoded = (T) decodeMethod.invoke(codec, bsonValue);
            }
            HytaleAssert.assertEquals(expected, decoded);
        } catch (AssertionFailedException e) {
            throw e;
        } catch (Exception e) {
            HytaleAssert.fail("Decode threw: %s", e.getMessage());
        }
    }

    /**
     * Asserts that decoding malformed data throws an exception.
     *
     * <p>The assertion passes if any exception is thrown during decoding. It fails
     * only if decoding completes without error.</p>
     *
     * <p>Failure message: {@code "Expected decode to throw but it succeeded"}</p>
     *
     * @param codec        the codec object (runtime type: {@code Codec<T>})
     * @param malformedBson the malformed BSON data to decode (runtime type: {@code BsonValue})
     * @param <T>          the value type
     * @throws AssertionFailedException if decoding succeeds without throwing
     */
    public static <T> void assertDecodeThrows(Object codec, Object malformedBson) {
        try {
            var decodeMethod = findMethod(codec, "decode");
            if (decodeMethod.getParameterCount() == 2) {
                decodeMethod.invoke(codec, malformedBson, null);
            } else {
                decodeMethod.invoke(codec, malformedBson);
            }
            HytaleAssert.fail("Expected decode to throw but it succeeded");
        } catch (AssertionFailedException e) {
            throw e;
        } catch (Exception _) {
        }
    }

    private static java.lang.reflect.Method findMethod(Object obj, String name) {
        for (var method : obj.getClass().getMethods()) {
            if (name.equals(method.getName())) {
                return method;
            }
        }
        throw new AssertionFailedException("No method '" + name + "' found on " + obj.getClass().getSimpleName());
    }
}
