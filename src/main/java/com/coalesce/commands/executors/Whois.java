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

@Command(name = "Whois", aliases = {"tellmeabout", "whos", "who's"}, usage = "[user]", description = "Tells you about the user specified or yourself if none.", permission = "commands.whois")
public class Whois extends CommandExecutor {
    @Override
    protected void execute(MessageChannel channel, Message message, String[] args) throws Exception {
        if (args.length > 1) {
            throw new CommandError("Please follow the correct syntax: %s", getAnnotation().usage());
        }
        Member member;
        if (args.length == 0) {
            member = message.getGuild().getMember(message.getAuthor());
        } else {
            member = message.getGuild().getMember(message.getMentionedUsers().stream().findFirst().orElseThrow(() -> new CommandError("Please specify a valid user.")));
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