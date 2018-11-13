package io.github.nucleuspowered.proton.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class CommandConfig {

    @Setting
    private String prefix = "~";
    @Setting(value = "delete-command", comment = "Whether the bot should attempt to delete a verified command.")
    private boolean deleteCommand = true;
    @Setting(value = "mention-commands", comment = "Whether or not the bot can be mentioned to issue a command instead of using a prefix.")
    private boolean mention = true;
    @Setting(value = "message-commands", comment = "Use to create simple command -> message mappings.")
    private MessageCommandConfig messageCommand = new MessageCommandConfig();

    public String getPrefix() {
        return prefix;
    }

    public boolean isDeleteCommand() {
        return deleteCommand;
    }

    public boolean isMention() {
        return mention;
    }

    public MessageCommandConfig getMessageCommand() {
        return messageCommand;
    }
}
