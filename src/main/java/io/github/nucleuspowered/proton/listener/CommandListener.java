package io.github.nucleuspowered.proton.listener;

import io.github.nucleuspowered.proton.ProfessorProton;
import io.github.nucleuspowered.proton.config.CommandConfig;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import org.apache.commons.lang3.StringUtils;

public class CommandListener extends ListenerAdapter {

    @Override public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        // Bots can't control Professor Proton! That would just be silly...
        if (event.getAuthor().isBot()) {
            return;
        }

        CommandConfig config = ProfessorProton.getInstance().getConfig().getCommand();
        Member bot = event.getGuild().getMember(ProfessorProton.getInstance().getBot().getSelfUser());
        String m = event.getMessage().getContentRaw();

        boolean prefix = StringUtils.isNotBlank(config.getPrefix()) && StringUtils.startsWithIgnoreCase(m, config.getPrefix());
        boolean mention = config.isMention() && event.getMessage().isMentioned(bot);
        boolean processedCommand = false;

        if (prefix || mention) {
            // Remove the prefix, if used
            if (prefix) {
                m = m.substring(1).trim();
            }
            // Remove the mention, if used
            if (mention) {
                m = m.replace(bot.getAsMention(), "").trim();
            }

            ProfessorProton.LOGGER.debug("Detected possible command: {}", m);

            // Process any message commands
            if (config.getMessageCommand().isEnabled() && config.getMessageCommand().getCommands().containsKey(m.toLowerCase())) {
                event.getChannel().sendMessage(config.getMessageCommand().getCommands().get(m)).queue();
                processedCommand = true;
            }

            if (processedCommand && config.isDeleteCommand() && PermissionUtil.checkPermission(event.getChannel(), bot, Permission.MESSAGE_MANAGE)) {
                event.getMessage().delete().queue();
            }
        }
    }
}
