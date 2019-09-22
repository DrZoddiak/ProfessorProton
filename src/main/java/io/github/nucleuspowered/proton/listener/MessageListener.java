package io.github.nucleuspowered.proton.listener;

import io.github.nucleuspowered.proton.ProfessorProton;
import io.github.nucleuspowered.proton.task.DuplicateMessageCheck;
import io.github.nucleuspowered.proton.task.JustAskCheck;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getMember() == null) {
            return;
        }

        ProfessorProton.getInstance().getGuildMessageCache(event.getGuild()).put(event.getMessageId(), event.getMessage());

        // Command Handler
        // if (!)

        // Staff tend to behave themselves...
        if (!event.getMember().getRoles().isEmpty()) {
            return;
        }

        // Check if the user has been recently warned
        boolean suppressWarnings = ProfessorProton.getInstance().shouldSuppressWarnings(event.getMember());

        // Duplicate Message Check
        // Only check members without a role, when enabled
        if (ProfessorProton.getInstance().getConfig().getDuplicateMessage().isEnabled()) {
            new Thread(new DuplicateMessageCheck(event, suppressWarnings), "deduplicate").start();
        }

        // Just Ask Check
        // Only check members without a role, when enabled
        if (ProfessorProton.getInstance().getConfig().getJustAsk().isEnabled()) {
            new Thread(new JustAskCheck(event, suppressWarnings), "just-ask").start();
        }
    }

}
