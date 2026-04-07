package com.frotty27.hrtk.server.surface;

import java.util.Objects;

public final class CodecTestAdapter {

    @SuppressWarnings("unchecked")
    public <T> T roundTrip(Object codec, T value) {
        try {
            var encodeMethod = findMethod(codec, "encode");
            Object encoded;
            if (encodeMethod.getParameterCount() == 2) {
                encoded = encodeMethod.invoke(codec, value, null);
            } else {
                encoded = encodeMethod.invoke(codec, value);
            }

            var decodeMethod = findMethod(codec, "decode");
            if (decodeMethod.getParameterCount() == 2) {
                return (T) decodeMethod.invoke(codec, encoded, null);
            } else {
                return (T) decodeMethod.invoke(codec, encoded);
            }
        } catch (Exception e) {
            throw new RuntimeException("Round-trip failed for codec " + codec.getClass().getSimpleName(), e);
        }
    }

    public <T> boolean roundTripEquals(Object codec, T value) {
        T decoded = roundTrip(codec, value);
        return Objects.equals(value, decoded);
    }

    private static java.lang.reflect.Method findMethod(Object obj, String name) {
        for (var method : obj.getClass().getMethods()) {
            if (name.equals(method.getName())) {
                return method;
            }
        }
        throw new RuntimeException("No method '" + name + "' found on " + obj.getClass().getSimpleName());
    }
}
