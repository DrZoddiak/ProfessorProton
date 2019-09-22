package io.github.nucleuspowered.proton.listener;

import io.github.nucleuspowered.proton.ProfessorProton;
import io.github.nucleuspowered.proton.config.MentionConfig;
import io.github.nucleuspowered.proton.data.Warning;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.regex.Pattern;

public class MentionListener extends ListenerAdapter {

    private static final Pattern EVERYONE = Pattern.compile("@(here|everyone)", Pattern.CASE_INSENSITIVE);

    @Override public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getMember() == null || event.getAuthor().isBot()) {
            return;
        }

        // Check mentions sent by members with no roles
        if (event.getMember().getRoles().isEmpty()) {
            MentionConfig config = ProfessorProton.getInstance().getConfig().getMention();
            if (config.isWarnOnMention()) {
                boolean suppressWarnings = ProfessorProton.getInstance().shouldSuppressWarnings(event.getMember());
                if (isSpammingMentions(event, config)) {
                    ProfessorProton.getInstance().getConfig().getQuarantineRole()
                            .ifPresent(quarantine -> {
                                event.getGuild().addRoleToMember(event.getMember(), quarantine).queue();
                                ProfessorProton.getInstance().getConsole().ifPresent(c -> c.sendMessage(
                                        event.getMember().getAsMention() + " has been added to " + quarantine.getAsMention()
                                ).queue());
                            });
                    ProfessorProton.getInstance().getDatabase().logWarning(new Warning(
                            event.getAuthor().getIdLong(),
                            event.getMessage().getTimeCreated(),
                            "Mention Spam",
                            event.getMessage().getContentRaw()
                    ));
                    ProfessorProton.getInstance().getConsole().ifPresent(c -> c.sendMessage(new EmbedBuilder()
                            .setTitle("Detected user spamming mentions")
                            .setThumbnail(event.getAuthor().getAvatarUrl())
                            .setTimestamp(event.getMessage().getTimeCreated())
                            .addField("Author", event.getAuthor().getAsMention(), false)
                            .addField("Channel", event.getChannel().getAsMention(), true)
                            .addField("Message", event.getMessage().getContentRaw(), true)
                            .build()
                    ).queue());
                    ProfessorProton.LOGGER.debug("{} detected spamming mentions.", event.getMember().getEffectiveName());
                    suppressWarnings = true;
                }
                if (isMentioningStaff(event, config)) {
                    if (!suppressWarnings) {
                        event.getChannel().sendMessage(
                                config.getMessage().replace("{{user}}", event.getMember().getAsMention())
                        ).queue();
                    }
                    ProfessorProton.getInstance().getDatabase().logWarning(new Warning(
                            event.getAuthor().getIdLong(),
                            event.getMessage().getTimeCreated(),
                            "Mention Staff",
                            event.getMessage().getContentRaw()
                    ));
                    ProfessorProton.getInstance().getConsole().ifPresent(c -> c.sendMessage(new EmbedBuilder()
                            .setTitle("Detected user mentioning staff")
                            .setThumbnail(event.getAuthor().getAvatarUrl())
                            .setTimestamp(event.getMessage().getTimeCreated())
                            .addField("Author", event.getAuthor().getAsMention(), false)
                            .addField("Channel", event.getChannel().getAsMention(), true)
                            .addField("Message", event.getMessage().getContentRaw(), true)
                            .build()
                    ).queue());
                    ProfessorProton.LOGGER.debug("{} detected mentioning staff.", event.getMember().getEffectiveName());
                    suppressWarnings = true;
                }
                if (isMentioningEveryone(event)) {
                    if (!suppressWarnings) {
                        event.getChannel().sendMessage(config.getEveryoneMessage()
                                .replace("{{user}}", event.getMember().getAsMention())
                                .replace("{{members}}", Integer.toString(event.getGuild().getMembers().size()))
                        ).queue();
                    }
                    ProfessorProton.getInstance().getDatabase().logWarning(new Warning(
                            event.getAuthor().getIdLong(),
                            event.getMessage().getTimeCreated(),
                            "Mention Everyone",
                            event.getMessage().getContentRaw()
                    ));
                    ProfessorProton.getInstance().getConsole().ifPresent(c -> c.sendMessage(new EmbedBuilder()
                            .setTitle("Detected user mentioning everyone")
                            .setThumbnail(event.getAuthor().getAvatarUrl())
                            .setTimestamp(event.getMessage().getTimeCreated())
                            .addField("Author", event.getAuthor().getAsMention(), false)
                            .addField("Channel", event.getChannel().getAsMention(), true)
                            .addField("Message", event.getMessage().getContentRaw(), true)
                            .build()
                    ).queue());
                    ProfessorProton.LOGGER.debug("{} detected mentioning everyone.", event.getMember().getEffectiveName());
                    suppressWarnings = true;
                }
            }
        }
    }

    private boolean isSpammingMentions(GuildMessageReceivedEvent event, MentionConfig config) {
        return event.getMessage().getMentions(MentionType.EVERYONE, MentionType.HERE, MentionType.USER).size() >= config.getSpamThreshold();
    }

    private boolean isMentioningStaff(GuildMessageReceivedEvent event, MentionConfig config) {
        for (Member mention : event.getMessage().getMentionedMembers()) {
            // Mention member's with roles
            if (!mention.getUser().isBot() && !mention.getRoles().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean isMentioningEveryone(GuildMessageReceivedEvent event) {
        return EVERYONE.matcher(event.getMessage().getContentStripped()).find();
    }
}
