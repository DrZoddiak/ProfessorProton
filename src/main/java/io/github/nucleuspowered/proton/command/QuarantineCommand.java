package io.github.nucleuspowered.proton.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import io.github.nucleuspowered.proton.ProfessorProton;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;
import java.util.Optional;

public class QuarantineCommand extends Command {

    public QuarantineCommand() {
        this.name = "quarantine";
        this.help = "Adds or removes a member from quarantine.";
        this.arguments = "<member>";
        this.botPermissions = new Permission[] {Permission.MANAGE_ROLES};
        this.requiredRole = "Channel Support";
    }

    @Override
    protected void execute(CommandEvent event) {
        List<Member> members = FinderUtil.findMembers(event.getArgs(), event.getGuild());
        if (event.getArgs().isEmpty() || members.isEmpty()) {
            event.replyError("A member must be provided.");
            return;
        }

        Member member = members.get(0);
        final Optional<Role> quarantineRole = ProfessorProton.getInstance().getConfig().getQuarantineRole();
        if (quarantineRole.isPresent()) {
            final Role quarantine = quarantineRole.get();
            if (member.getRoles().contains(quarantine)) {
                event.getGuild().removeRoleFromMember(member, quarantine).queue();
                event.reply(member.getAsMention() + " was removed from " + quarantine.getAsMention() + ".");
            } else {
                event.getGuild().addRoleToMember(member, quarantine).queue();
                event.reply(member.getAsMention() + " was added to " + quarantine.getAsMention() + ".");
            }
        } else {
            event.replyError("A quarantine role is not configured.");
        }
    }
}
