package com.coalesce.commands.executors;

import com.coalesce.Bot;
import com.coalesce.commands.Command;
import com.coalesce.commands.CommandError;
import com.coalesce.commands.CommandExecutor;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Command(name = "Resolve", aliases = {"resolver", "url"}, usage = "<url>", description = "Resolves URL shortened links.")
public class Resolve extends CommandExecutor {
    @Override
    protected void execute(MessageChannel channel, Message message, String[] args) throws Exception {
        if (args.length < 1) {
            throw new CommandError("Please use the correct syntax: %s", getAnnotation().usage());
        }
        String url = String.join("%20", args);
        Bot.getInstance().executor.execute(() -> {
            try {
                String resolved = getFinalUrl(url);
                message.getChannel().sendMessage(new MessageBuilder().append(message.getAuthor()).append(": The URL was resolved to ").append(resolved).build()).complete();
            } catch (IOException ex) {
                message.getChannel().sendMessage(new MessageBuilder().append(message.getAuthor()).appendFormat(": Couldn't resolve the URL.").build()).complete();
            }
        });
    }

    private String getFinalUrl(String url) throws IOException {
        if (!(url.startsWith("https://") || url.startsWith("http://"))) {
            url = "http://" + url;
        }

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.addRequestProperty("User-Agent", "Mozilla/4.76");
        conn.setInstanceFollowRedirects(false);
        conn.connect();
        conn.getInputStream();

        if (conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP
                || conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM) {
            return getFinalUrl(conn.getHeaderField("Location"));
        }

        return url;
    }
}
