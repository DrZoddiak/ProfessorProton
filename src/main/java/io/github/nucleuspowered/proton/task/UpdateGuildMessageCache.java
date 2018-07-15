package io.github.nucleuspowered.proton.task;

import com.github.benmanes.caffeine.cache.Cache;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.utils.PermissionUtil;

public class UpdateGuildMessageCache implements Runnable {

    private final Guild guild;
    private final Cache<String, Message> cache;

    public UpdateGuildMessageCache(Guild guild, Cache<String, Message> cache) {
        this.guild = guild;
        this.cache = cache;
    }

    @Override public void run() {
        final Member member = this.guild.getMember(this.guild.getJDA().getSelfUser());
        guild.getTextChannels().stream().filter(x -> PermissionUtil.checkPermission(x, member, Permission.MESSAGE_HISTORY)).forEach(channel -> {
            for (Message message : channel.getHistory().retrievePast(100).complete()) {
                cache.put(message.getId(), message);
            }
        });
    }
}
