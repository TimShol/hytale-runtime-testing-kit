package com.frotty27.hrtk.server.context;

import com.frotty27.hrtk.api.context.TestContext;
import com.frotty27.hrtk.api.mock.EventCapture;
import com.frotty27.hrtk.api.mock.MockCommandSender;
import com.frotty27.hrtk.api.mock.MockPlayerRef;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LiveTestContext implements TestContext {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final String pluginName;
    private final List<LiveEventCapture<?>> activeCaptures = new ArrayList<>();

    public LiveTestContext(String pluginName) {
        this.pluginName = pluginName;
    }

    @Override
    public String getPluginName() {
        return pluginName;
    }

    @Override
    public void log(String message) {
        LOGGER.atInfo().log("HRTK [%s]: %s", pluginName, message);
    }

    @Override
    public void log(String format, Object... args) {
        LOGGER.atInfo().log("HRTK [%s]: %s", pluginName, String.format(format, args));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <E> EventCapture<E> captureEvent(Class<E> eventType) {
        LiveEventCapture<E> capture = new LiveEventCapture<>(eventType);

        EventRegistry eventRegistry = findEventRegistry();
        if (eventRegistry != null) {
            try {
                Object registration = eventRegistry.registerGlobal(
                        (Class) eventType,
                        (java.util.function.Consumer) event -> capture.capture((E) event)
                );
                capture.setRegistration(registration);
            } catch (Exception e) {
                try {
                    Object registration = eventRegistry.register(
                            (Class) eventType,
                            (java.util.function.Consumer) event -> capture.capture((E) event)
                    );
                    capture.setRegistration(registration);
                } catch (Exception innerException) {
                    LOGGER.atWarning().log("HRTK: Failed to register event capture for %s: %s",
                            eventType.getSimpleName(), innerException.getMessage());
                }
            }
        } else {
            LOGGER.atWarning().log("HRTK: No EventRegistry found for plugin '%s' - event capture will not work", pluginName);
        }

        activeCaptures.add(capture);
        return capture;
    }

    @Override
    public MockCommandSender createCommandSender() {
        return new MockCommandSenderImpl("TestSender");
    }

    @Override
    public MockCommandSender createCommandSender(String... permissions) {
        MockCommandSenderImpl sender = new MockCommandSenderImpl("TestSender");
        for (String permission : permissions) {
            sender.addPermission(permission);
        }
        return sender;
    }

    @Override
    public MockPlayerRef createMockPlayer() {
        return new MockPlayerRefImpl("TestPlayer");
    }

    @Override
    public MockPlayerRef createMockPlayer(String displayName) {
        return new MockPlayerRefImpl(displayName);
    }

    @Override
    public void executeCommand(String commandLine, MockCommandSender sender) {
        String command = commandLine.startsWith("/") ? commandLine.substring(1) : commandLine;
        CommandManager manager = CommandManager.get();

        if (sender instanceof MockCommandSenderImpl mockSender) {
            CompletableFuture<Void> future = manager.handleCommand(mockSender, command);
            try {
                future.get(5, java.util.concurrent.TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new RuntimeException("Command execution failed: " + command, e);
            }
        } else {
            throw new RuntimeException("MockCommandSender must be created via TestContext.createCommandSender()");
        }
    }

    protected EventRegistry findEventRegistry() {
        for (PluginBase plugin : PluginManager.get().getPlugins()) {
            if (pluginName.equals(plugin.getName())) {
                return plugin.getEventRegistry();
            }
        }
        return null;
    }

    protected void closeCaptures() {
        for (LiveEventCapture<?> capture : activeCaptures) {
            capture.close();
        }
        activeCaptures.clear();
    }

    public void cleanup() {
        closeCaptures();
    }
}
