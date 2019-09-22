package io.github.nucleuspowered.proton;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.api.client.util.Maps;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import io.github.nucleuspowered.proton.command.QuarantineCommand;
import io.github.nucleuspowered.proton.command.WarningCommand;
import io.github.nucleuspowered.proton.config.BotConfig;
import io.github.nucleuspowered.proton.config.ConfigManager;
import io.github.nucleuspowered.proton.data.Database;
import io.github.nucleuspowered.proton.data.H2Database;
import io.github.nucleuspowered.proton.listener.CommandListener;
import io.github.nucleuspowered.proton.listener.MentionListener;
import io.github.nucleuspowered.proton.listener.MessageListener;
import io.github.nucleuspowered.proton.listener.PrivateMessageListener;
import io.github.nucleuspowered.proton.task.UpdateGuildMessageCache;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import javax.security.auth.login.LoginException;

public class ProfessorProton {

    private static ProfessorProton instance;
    public static final Logger LOGGER = LogManager.getLogger(ProfessorProton.class);

    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private ConfigManager configManager;
    private BotConfig config;

    private JDA jda;

    private TextChannel console;

    private Map<Guild, Cache<String, Message>> guildCacheMap = Maps.newHashMap();

    private Map<User, Instant> lastWarning = Maps.newHashMap();

    private Database database;

    public static void main(String[] args) {
        instance = new ProfessorProton();
    }

    private ProfessorProton() {
        LOGGER.info("Professor Proton is now loading.");
        initConfig();
        try {
            database = new H2Database();
            initDiscordBot();
        } catch (Exception e) {
            LOGGER.error("Error initializing Discord bot instance.", e);
        }
    }

    private void initConfig() {
        config = new BotConfig();
        loader = HoconConfigurationLoader.builder()
                .setPath(Paths.get("./ProfessorProton.conf"))
                .build();
        configManager = new ConfigManager(config, loader);
        configManager.save();
    }

    private void initDiscordBot() throws LoginException, InterruptedException {
        CommandClient commandClient = new CommandClientBuilder()
                .setOwnerId(getConfig().getOwnerId())
                .setPrefix(getConfig().getCommand().getPrefix())
                .addCommand(new WarningCommand())
                .addCommand(new QuarantineCommand())
                .build();
        jda = new JDABuilder(AccountType.BOT)
                .setToken(config.getDiscord().getToken())
                .setActivity(Activity.playing(config.getDiscord().getGame()))
                .addEventListeners(
                        commandClient,
                        new CommandListener(),
                        new MessageListener(),
                        new PrivateMessageListener(),
                        new MentionListener()
                )
                .build()
                .awaitReady();

        // Generate a message cache for each guild
        for (Guild guild : jda.getGuilds()) {
            guildCacheMap.put(
                    guild,
                    Caffeine.newBuilder()
                            .expireAfterWrite(config.getCache().getExpiration(), config.getCache().getUnit())
                            .maximumSize(config.getCache().getMaxSize())
                            .build()
            );
            Thread t = new Thread(new UpdateGuildMessageCache(guild, guildCacheMap.get(guild)), "UpdateGuildMessageCache");
            t.start();
        }

        // Setup console channel
        if (StringUtils.isNotBlank(config.getDiscord().getConsoleChannelID())) {
            console = jda.getTextChannelById(config.getDiscord().getConsoleChannelID());
        }

        LOGGER.info("Bot initialization complete.");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ProfessorProton.LOGGER.info("Bot shutting down.");
            ProfessorProton.getInstance().getBot().shutdown();
        }));
    }

    public boolean shouldSuppressWarnings(Member member) {
        Optional<Instant> lastWarning = Optional.ofNullable(this.lastWarning.get(member.getUser()));
        boolean suppressWarnings = false;
        if (lastWarning.isPresent()) {
            final long secondsSinceLastWarning = Duration.between(lastWarning.get(), Instant.now()).getSeconds();
            if (secondsSinceLastWarning <= this.getConfig().getWarningCooldown()) {
                // Ignore the user's message
                LOGGER.debug("{}'s last warning: {} sec(s). Warnings will be suppressed.",
                        member.getEffectiveName(),
                        secondsSinceLastWarning
                );
                suppressWarnings = true;
            }
        }
        return suppressWarnings;
    }

    public static ProfessorProton getInstance() {
        return instance;
    }

    public JDA getBot() {
        return jda;
    }

    public Cache<String, Message> getGuildMessageCache(Guild guild) {
        return guildCacheMap.get(guild);
    }

    public Database getDatabase() {
        return database;
    }

    public Map<User, Instant> getLastWarning() {
        return lastWarning;
    }

    public BotConfig getConfig() {
        return config;
    }

    public Optional<TextChannel> getConsole() {
        return Optional.ofNullable(console);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
