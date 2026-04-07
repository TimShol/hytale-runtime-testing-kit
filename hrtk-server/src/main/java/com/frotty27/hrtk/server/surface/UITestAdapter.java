package com.frotty27.hrtk.server.surface;

import com.frotty27.hrtk.api.mock.UICommandCapture;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class UITestAdapter {

    public UICommandCapture buildPage(CustomUIPage page, Ref<EntityStore> playerRef,
                                       Store<EntityStore> store) {
        CapturingUICommandBuilder builder = new CapturingUICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();
        page.build(playerRef, builder, eventBuilder, store);
        return builder.toCapture();
    }

    private static final class CapturingUICommandBuilder extends UICommandBuilder {

        private final List<CapturedCommand> captured = new ArrayList<>();

        @Override
        public UICommandBuilder set(String path, String value) {
            captured.add(new CapturedCommand(path, "SET", value));
            return super.set(path, value);
        }

        @Override
        public UICommandBuilder set(String path, boolean value) {
            captured.add(new CapturedCommand(path, "SET", value));
            return super.set(path, value);
        }

        @Override
        public UICommandBuilder append(String path, String content) {
            captured.add(new CapturedCommand(path, "APPEND", content));
            return super.append(path, content);
        }

        @Override
        public UICommandBuilder clear(String path) {
            captured.add(new CapturedCommand(path, "CLEAR", null));
            return super.clear(path);
        }

        @Override
        public UICommandBuilder remove(String path) {
            captured.add(new CapturedCommand(path, "REMOVE", null));
            return super.remove(path);
        }

        @Override
        public UICommandBuilder setNull(String path) {
            captured.add(new CapturedCommand(path, "SET", null));
            return super.setNull(path);
        }

        UICommandCapture toCapture() {
            return new CapturedUICommands(List.copyOf(captured));
        }
    }

    private record CapturedCommand(String path, String operation, Object value) {}

    private static final class CapturedUICommands implements UICommandCapture {

        private final List<CapturedCommand> commands;

        CapturedUICommands(List<CapturedCommand> commands) {
            this.commands = commands;
        }

        @Override
        public List<UICommandRecord> getCommands() {
            return commands.stream()
                    .<UICommandRecord>map(c -> new UICommandRecord() {
                        @Override public String getPath() { return c.path(); }
                        @Override public String getOperation() { return c.operation(); }
                        @Override public Object getValue() { return c.value(); }
                    })
                    .toList();
        }

        @Override
        public boolean hasCommand(String path, String operation) {
            return commands.stream().anyMatch(c ->
                    c.path().equals(path) && c.operation().equalsIgnoreCase(operation));
        }

        @Override
        public boolean hasSet(String path, Object value) {
            return commands.stream().anyMatch(c ->
                    c.path().equals(path) && "SET".equals(c.operation())
                            && java.util.Objects.equals(c.value(), value));
        }

        @Override
        public int getCount() {
            return commands.size();
        }
    }
}
