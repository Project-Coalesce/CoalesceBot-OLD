package com.coalesce.commands.executors;

import com.coalesce.commands.Command;
import com.coalesce.commands.CommandError;
import com.coalesce.commands.CommandExecutor;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Command(name = "Whois", aliases = {"tellmeabout", "whos", "who's"}, usage = "[user]", description = "Tells you about the user specified or yourself if none.")
public class Whois extends CommandExecutor {
    @Override
    protected void execute(MessageChannel channel, Message message, String[] args) throws Exception {
        if (args.length > 1) {
            throw new CommandError("Please follow the correct syntax: %s", getAnnotation().usage());
        }
        Member member = null;
        if (args.length == 0) {
            member = message.getGuild().getMember(message.getAuthor());
        } else {
            String user[] = args[0].split("#");
            if (user.length > 2) {
                throw new CommandError("Please specify a valid username.");
            }
            for (Member it : message.getGuild().getMembers()) {
                if (it.getUser().getName().equalsIgnoreCase(user[0])) {
                    if (user.length == 2) {
                        if (it.getUser().getDiscriminator().equalsIgnoreCase(user[1])) {
                            member = it;
                            break;
                        }
                        continue;
                    }
                    member = it;
                    break;
                }
            }
            if (member == null) {
                member = message.getGuild().getMember(jda.getUsersByName(args[0], true).stream().findFirst().orElseThrow(() -> new CommandError("Please specify a valid username.")));
            }
        }
        
        message.getChannel().sendMessage(new EmbedBuilder()
                .setColor(new Color(0.0f, 0.5f, 0.0f))
                .setAuthor(member.getUser().getName(), null, member.getUser().getAvatarUrl())
                .addField("Nickname", member.getNickname() != null ? member.getNickname() : "None", true)
                .addField("Discriminator", member.getUser().getDiscriminator(), true)
                .addField("User ID", member.getUser().getId(), true)
                .addField("Playing Currently", member.getGame().getName(), true)
                .addField("Roles", String.join("\n", member.getRoles().stream().map(role -> "\u2666 " + role.getName()).collect(Collectors.toList())), true)
                .addField("Type", member.getUser().isBot() ? "Bot" : (member.isOwner() ? "Owner" : "User"), true)
                .addField("Creation Time", member.getUser().getCreationTime().format(DateTimeFormatter.ofPattern("d MMM uuuu")), true)
                .build()).queue();
    }
}