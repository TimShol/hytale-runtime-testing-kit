package com.frotty27.hrtk.server.context;

import com.frotty27.hrtk.api.mock.MockCommandSender;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class MockCommandSenderImpl implements MockCommandSender, CommandSender {

    private final String name;
    private final UUID uuid;
    private final List<String> messages = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final Set<String> permissions = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public MockCommandSenderImpl(String name) {
        this.name = name;
        this.uuid = UUID.randomUUID();
    }


    @Override
    public List<String> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    @Override
    public String getLastMessage() {
        return messages.isEmpty() ? null : messages.getLast();
    }

    @Override
    public boolean hasReceivedMessage(String substring) {
        for (String msg : messages) {
            if (msg.contains(substring)) return true;
        }
        return false;
    }

    @Override
    public void clearMessages() {
        messages.clear();
    }

    @Override
    public Set<String> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    @Override
    public boolean hasPermission(String permission) {
        return permissions.contains(permission) || permissions.contains("*");
    }

    @Override
    public void addPermission(String permission) {
        permissions.add(permission);
    }

    @Override
    public void removePermission(String permission) {
        permissions.remove(permission);
    }

    @Override
    public String getName() {
        return name;
    }


    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }


    @Override
    public void sendMessage(Message message) {
        messages.add(message.toString());
    }


    @Override
    public boolean hasPermission(String permission, boolean defaultValue) {
        if (permissions.contains(permission) || permissions.contains("*")) {
            return true;
        }
        return defaultValue;
    }
}
