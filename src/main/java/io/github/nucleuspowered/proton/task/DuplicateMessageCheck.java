package io.github.nucleuspowered.proton.task;

import io.github.nucleuspowered.proton.ProfessorProton;
import io.github.nucleuspowered.proton.config.DuplicateMessageConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DuplicateMessageCheck extends BaseTask {

    private GuildMessageReceivedEvent event;

    public DuplicateMessageCheck(GuildMessageReceivedEvent event, boolean suppressWarnings) {
        super(suppressWarnings);
        this.event = event;
    }

    @Override
    public void run() {
        StopWatch sw = new StopWatch();
        sw.start();

        DuplicateMessageConfig config = ProfessorProton.getInstance().getConfig().getDuplicateMessage();
        ProfessorProton.LOGGER.debug("Checking {}'s message for duplicates.", event.getMember().getEffectiveName());
        List<Message> messages = ProfessorProton.getInstance().getGuildMessageCache(event.getGuild()).asMap().values().stream()
                .filter(m -> m.getAuthor().getId().equals(event.getAuthor().getId()))
                .collect(Collectors.toList());
        ProfessorProton.LOGGER.debug("Found {} recent messages from {}.", messages.size(), event.getMember().getEffectiveName());

        // Count messages that exceed the min length and match requirement
        int count = (int) messages.stream()
                .filter(m -> !event.getMessageId().equals(m.getId()))
                .filter(m -> event.getMessage().getContentStripped().length() >= config.getMinLength() &&
                        StringUtils.getJaroWinklerDistance(m.getContentStripped().toLowerCase(),
                                event.getMessage().getContentStripped().toLowerCase()) >= config.getMatch())
                .count() + 1;

        ProfessorProton.LOGGER.debug("Found {} similar messages.", count);

        if (count >= config.getWarnThreshold()) {
            if (!suppressWarnings) {
                event.getChannel().sendMessage(config.getMessage().replace("{{user}}", event.getMember().getAsMention())).queue();
                ProfessorProton.getInstance().getLastWarning().put(event.getAuthor(), event.getMessage().getTimeCreated().toInstant());
            }
            ProfessorProton.getInstance().getConsole().ifPresent(c -> c.sendMessage(new EmbedBuilder()
                    .setTitle("Detected duplicate messages")
                    .setThumbnail(event.getAuthor().getAvatarUrl())
                    .setDescription(count + " recent occurrences.")
                    .setTimestamp(event.getMessage().getTimeCreated())
                    .addField("Author", event.getAuthor().getAsMention(), false)
                    .addField("Channel", event.getChannel().getAsMention(), true)
                    .addField("Message", event.getMessage().getContentRaw(), true)
                    .build()
            ).queue());
            ProfessorProton.LOGGER.debug("Detected {} duplicate messages from {}.", count, event.getMember().getEffectiveName());
        }

        sw.stop();
        ProfessorProton.LOGGER.debug("Checking for duplicate messages completed in {}ms.", sw.getTime(TimeUnit.MILLISECONDS));
    }
}
