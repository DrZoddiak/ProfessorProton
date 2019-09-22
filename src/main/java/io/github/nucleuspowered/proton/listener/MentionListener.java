package io.github.nucleuspowered.proton.listener;

import io.github.nucleuspowered.proton.ProfessorProton;
import io.github.nucleuspowered.proton.config.MentionConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.regex.Pattern;

public class MentionListener extends ListenerAdapter {

    private static final Pattern EVERYONE = Pattern.compile("@(here|everyone)", Pattern.CASE_INSENSITIVE);

    @Override public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        // Check mentions sent by members with no roles
        if (!event.getAuthor().isBot() && event.getMember().getRoles().isEmpty()) {
            MentionConfig config = ProfessorProton.getInstance().getConfig().getMention();
            if (config.isWarnOnMention()) {
                for (Member mention : event.getMessage().getMentionedMembers()) {
                    // Mention member's with roles
                    if (!mention.getUser().isBot() && !mention.getRoles().isEmpty()) {
                        event.getChannel().sendMessage(config.getMessage().replace("{{user}}", event.getMember().getAsMention())).queue();
                        ProfessorProton.getInstance().getConsole().ifPresent(c -> c.sendMessage(new EmbedBuilder()
                                .setTitle("Detected user mentioning staff")
                                .setThumbnail(event.getAuthor().getAvatarUrl())
                                .setTimestamp(event.getMessage().getTimeCreated())
                                .addField("Author", event.getAuthor().getAsMention(), false)
                                .addField("Channel", event.getChannel().getAsMention(), true)
                                .addField("Message", event.getMessage().getContentRaw(), true)
                                .build()
                        ).queue());
                        ProfessorProton.LOGGER.debug("{} warned for mentioning staff.", event.getMember().getEffectiveName());
                        return;
                    }
                }
                // Mentions everyone
                if (EVERYONE.matcher(event.getMessage().getContentStripped()).find()) {
                    event.getChannel().sendMessage(config.getEveryoneMessage()
                            .replace("{{user}}", event.getMember().getAsMention())
                            .replace("{{members}}", Integer.toString(event.getGuild().getMembers().size()))
                    ).queue();
                    ProfessorProton.getInstance().getConsole().ifPresent(c -> c.sendMessage(new EmbedBuilder()
                            .setTitle("Detected user mentioning everyone")
                            .setThumbnail(event.getAuthor().getAvatarUrl())
                            .setTimestamp(event.getMessage().getTimeCreated())
                            .addField("Author", event.getAuthor().getAsMention(), false)
                            .addField("Channel", event.getChannel().getAsMention(), true)
                            .addField("Message", event.getMessage().getContentRaw(), true)
                            .build()
                    ).queue());
                    ProfessorProton.LOGGER.debug("{} warned for mentioning everyone.", event.getMember().getEffectiveName());
                }
            }
        }
    }
}
