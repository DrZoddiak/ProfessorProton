package io.github.nucleuspowered.proton.listener;

import io.github.nucleuspowered.proton.ProfessorProton;
import io.github.nucleuspowered.proton.config.MentionConfig;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class MentionListener extends ListenerAdapter {

    @Override public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        // Check mentions sent by members with no roles
        if (!event.getAuthor().isBot() && event.getMember().getRoles().isEmpty() && !event.getMessage().getMentionedMembers().isEmpty()) {
            MentionConfig config = ProfessorProton.getInstance().getConfig().getMention();
            if (config.isWarnOnMention()) {
                for (Member mention : event.getMessage().getMentionedMembers()) {
                    if (!mention.getRoles().isEmpty()) {
                        event.getChannel().sendMessage(config.getMessage().replace("{{user}}", event.getMember().getAsMention())).queue();
                        ProfessorProton.LOGGER.info("{} warned for mentioning staff.", event.getMember().getEffectiveName());
                        return;
                    }
                }
            }
        }
    }
}
