package io.github.nucleuspowered.proton.task;

import io.github.nucleuspowered.proton.ProfessorProton;
import io.github.nucleuspowered.proton.config.JustAskConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.util.concurrent.TimeUnit;

public class JustAskCheck extends BaseTask {

    private GuildMessageReceivedEvent event;

    public JustAskCheck(GuildMessageReceivedEvent event, boolean suppressWarnings) {
        super(suppressWarnings);
        this.event = event;
    }

    @Override public void run() {
        StopWatch sw = new StopWatch();
        sw.start();

        JustAskConfig config = ProfessorProton.getInstance().getConfig().getJustAsk();
        String content = event.getMessage().getContentStripped().toLowerCase();

        // Check if the message matches any of the predefined phrases
        config.getPhases().stream()
                .filter(s -> StringUtils.getJaroWinklerDistance(s.toLowerCase(), content) >= config.getMatch())
                .findAny()
                .ifPresent(s -> {
                    if (!suppressWarnings) {
                        event.getChannel().sendMessage(config.getMessage().replace("{{user}}", event.getMember().getAsMention())).queue();
                        ProfessorProton.getInstance().getLastWarning().put(event.getAuthor(), event.getMessage().getTimeCreated().toInstant());
                    }
                    ProfessorProton.getInstance().getConsole().ifPresent(c -> c.sendMessage(new EmbedBuilder()
                            .setTitle("Detected \"asking to ask\"")
                            .setThumbnail(event.getAuthor().getAvatarUrl())
                            .setDescription(Math.round(StringUtils.getJaroWinklerDistance(s.toLowerCase(), content) * 100) + "% similar")
                            .setTimestamp(event.getMessage().getTimeCreated())
                            .addField("Author", event.getAuthor().getAsMention(), false)
                            .addField("Channel", event.getChannel().getAsMention(), true)
                            .addField("Message", event.getMessage().getContentRaw(), true)
                            .addField("Match", s, false)
                            .build()
                    ).queue());
                    ProfessorProton.LOGGER.debug("Detected {} \"asking to ask\"", event.getMember().getEffectiveName());
                    ProfessorProton.LOGGER.debug("{}% similar:\nUser:\t{}\nMatch:\t{}",
                            Math.round(StringUtils.getJaroWinklerDistance(s.toLowerCase(), content) * 100),
                            content,
                            s
                    );
                });

        sw.stop();
        ProfessorProton.LOGGER.debug("Testing for asking to ask completed in {}ms", sw.getTime(TimeUnit.MILLISECONDS));
    }
}
