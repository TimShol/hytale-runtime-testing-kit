package com.frotty27.hrtk.server.surface;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class EcsTestAdapter {

    private final Store<EntityStore> store;
    private final CommandBuffer<EntityStore> commandBuffer;

    public EcsTestAdapter(Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer) {
        this.store = store;
        this.commandBuffer = commandBuffer;
    }

    public <T extends Component<EntityStore>> void removeComponent(
            Ref<EntityStore> ref, ComponentType<EntityStore, T> type) {
        commandBuffer.removeComponent(ref, type);
    }

    public void run(java.util.function.Consumer<Store<EntityStore>> operation) {
        commandBuffer.run(operation);
    }

    public Store<EntityStore> getStore() { return store; }
    public CommandBuffer<EntityStore> getCommandBuffer() { return commandBuffer; }
}
