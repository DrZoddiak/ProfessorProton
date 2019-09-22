package io.github.nucleuspowered.proton.config;

import io.github.nucleuspowered.proton.ProfessorProton;
import net.dv8tion.jda.api.entities.Role;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Optional;

@ConfigSerializable
public class BotConfig {

    @Setting
    private DiscordConfig discord = new DiscordConfig();
    @Setting
    private CacheConfig cache = new CacheConfig();
    @Setting
    private CommandConfig command = new CommandConfig();
    @Setting
    private MentionConfig mention = new MentionConfig();
    @Setting
    private SupportConfig support = new SupportConfig();
    @Setting
    private DuplicateMessageConfig duplicateMessage = new DuplicateMessageConfig();
    @Setting
    private JustAskConfig justAsk = new JustAskConfig();
    @Setting
    private String ownerId = "";
    @Setting
    private String quarantineRoleId = "";
    @Setting
    private int warningCooldown = 10;

    public DiscordConfig getDiscord() {
        return discord;
    }

    public CacheConfig getCache() {
        return cache;
    }

    public CommandConfig getCommand() {
        return command;
    }

    public MentionConfig getMention() {
        return mention;
    }

    public SupportConfig getSupport() {
        return support;
    }

    public DuplicateMessageConfig getDuplicateMessage() {
        return duplicateMessage;
    }

    public JustAskConfig getJustAsk() {
        return justAsk;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public Optional<Role> getQuarantineRole() {
        return Optional.ofNullable(ProfessorProton.getInstance().getBot().getRoleById(quarantineRoleId));
    }

    public int getWarningCooldown() {
        return warningCooldown;
    }
}
