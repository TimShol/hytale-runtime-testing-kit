package com.frotty27.hrtk.server.context;

import com.frotty27.hrtk.api.mock.MockPlayerRef;

import java.util.UUID;

public final class MockPlayerRefImpl implements MockPlayerRef {

    private final UUID uuid;
    private final String displayName;

    public MockPlayerRefImpl(String displayName) {
        this.uuid = UUID.randomUUID();
        this.displayName = displayName;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }
}
