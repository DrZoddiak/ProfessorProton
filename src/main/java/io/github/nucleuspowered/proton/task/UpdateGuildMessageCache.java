package io.github.nucleuspowered.proton.task;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.nucleuspowered.proton.ProfessorProton;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.internal.utils.PermissionUtil;

public class UpdateGuildMessageCache implements Runnable {

    private final Guild guild;
    private final Cache<String, Message> cache;

    public UpdateGuildMessageCache(Guild guild, Cache<String, Message> cache) {
        this.guild = guild;
        this.cache = cache;
    }

    @Override public void run() {
        ProfessorProton.LOGGER.info("Begun updating {} message cache.", guild.getName());
        final Member member = this.guild.getMember(this.guild.getJDA().getSelfUser());
        guild.getTextChannels().stream().filter(x -> PermissionUtil.checkPermission(x, member, Permission.MESSAGE_HISTORY)).forEach(channel -> {
            for (Message message : channel.getHistory().retrievePast(100).complete()) {
                cache.put(message.getId(), message);
            }
        });
        ProfessorProton.LOGGER.info("Finished updating the `{}` message cache.", guild.getName());
    }
}
