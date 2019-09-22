package io.github.nucleuspowered.proton.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import io.github.nucleuspowered.proton.ProfessorProton;
import io.github.nucleuspowered.proton.data.Warning;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.lang3.text.StrBuilder;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class WarningCommand extends Command {

    public WarningCommand() {
        this.name = "warning";
        this.help = "lists the warnings for a member.";
        this.arguments = "<member>";
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
        final List<Warning> warnings = ProfessorProton.getInstance().getDatabase().getWarnings(member.getIdLong());
        if (warnings.isEmpty()) {
            event.reply(member.getEffectiveName() + " has no warnings.");
        } else {
            StrBuilder reply = new StrBuilder(member.getEffectiveName())
                    .append("' Warnings")
                    .appendNewLine()
                    .append("```")
                    .appendNewLine();
            warnings.forEach(warning -> reply
                    .append(warning.getDateTime().format(DateTimeFormatter.ofPattern("d/MM/uu hh:mm a")))
                    .append(" - ")
                    .append(warning.getReason())
                    .appendNewLine()
            );
            reply.append("```");
            event.reply(reply.toString());
        }
    }
}
