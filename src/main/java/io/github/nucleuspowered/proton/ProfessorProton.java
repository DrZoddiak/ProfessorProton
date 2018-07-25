package io.github.nucleuspowered.proton;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.api.client.util.Maps;
import io.github.nucleuspowered.proton.config.BotConfig;
import io.github.nucleuspowered.proton.config.ConfigManager;
import io.github.nucleuspowered.proton.listener.MentionListener;
import io.github.nucleuspowered.proton.listener.MessageListener;
import io.github.nucleuspowered.proton.listener.PrivateMessageListener;
import io.github.nucleuspowered.proton.task.UpdateGuildMessageCache;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;
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

    public static void main(String[] args) {
        instance = new ProfessorProton();
    }

    private ProfessorProton() {
        LOGGER.info("Professor Proton is now loading.");
        initConfig();
        try {
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
        jda = new JDABuilder(AccountType.BOT)
                .setToken(config.getDiscord().getToken())
                .setGame(Game.playing(config.getDiscord().getGame()))
                .addEventListener(new MessageListener())
                .addEventListener(new PrivateMessageListener())
                .addEventListener(new MentionListener())
                .buildBlocking();

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
            t.run();
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

    public static ProfessorProton getInstance() {
        return instance;
    }

    public JDA getBot() {
        return jda;
    }

    public Cache<String, Message> getGuildMessageCache(Guild guild) {
        return guildCacheMap.get(guild);
    }

    public Map<User, Instant> getLastWarning() {
        return lastWarning;
    }

    public Optional<Instant> getLastWarning(User user) {
        return Optional.ofNullable(lastWarning.get(user));
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
