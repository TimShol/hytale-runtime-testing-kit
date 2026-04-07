package com.frotty27.hrtk.server.surface;

import com.frotty27.hrtk.server.context.MockCommandSenderImpl;

public final class CommandTestAdapter {

    public MockCommandSenderImpl createSender(String... permissions) {
        MockCommandSenderImpl sender = new MockCommandSenderImpl("TestSender");
        for (String perm : permissions) {
            sender.addPermission(perm);
        }
        return sender;
    }

    public void executeCommand(String commandLine) {
        try {
            var commandManagerClass = Class.forName("com.hypixel.hytale.server.core.command.system.CommandManager");
            var getInstance = commandManagerClass.getMethod("get");
            var manager = getInstance.invoke(null);

            String cmd = commandLine.startsWith("/") ? commandLine.substring(1) : commandLine;
            for (var method : manager.getClass().getMethods()) {
                if ("execute".equals(method.getName()) || "dispatch".equals(method.getName())) {
                    method.invoke(manager, cmd);
                    return;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute command: " + commandLine, e);
        }
    }
}
