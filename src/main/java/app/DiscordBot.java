package app;

import app.controller.AnnouncementController;
import app.entity.Announcement;
import app.listener.AnnouncementListener;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.eclipse.jetty.util.security.Credential;

import java.awt.*;
import java.time.Instant;

public class DiscordBot implements AnnouncementListener {
    private JDA dbot;
    private TextChannel txtChannel;

    public DiscordBot(String token, String channelId, AnnouncementController ac) throws Exception {
        dbot = new JDABuilder(AccountType.BOT)
                .setToken(token)
                .setGame(Game.of(Game.GameType.LISTENING, "Blackboard", "https://ntnu.blackboard.com"))
                .build()
                .awaitReady();

        txtChannel = dbot.getTextChannelById(channelId);

        // Adds itself as event handler before releasing reference for the object triggering them
        ac.addListener(this);
        ac.startIntervalPulling();
    }

    private void publishAnnouncements(Announcement[] announcements) {
        for (Announcement a : announcements) {
            String body;

            // Discord limits message bodies up to 2048 characters and it must be chopped down
            body = (a.getBody().length() > 2048) ? a.getBody().substring(0, 2041).concat("\n\n...") : a.getBody();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(a.getTitle())
                    .setDescription(body)
                    .setFooter("From BlackBoard", dbot.getSelfUser().getAvatarUrl())
                    .setTimestamp(Instant.ofEpochMilli(a.getTimestamp()))
                    .setColor(Color.GREEN);
            txtChannel.sendMessage(embed.build()).queue();
        }
    }

    private void publishAnnouncementTitles(Announcement[] announcements) {
        EmbedBuilder embed = new EmbedBuilder();

        for (Announcement a : announcements) {
            embed.setTimestamp(Instant.now());
            embed.addField(a.getTitle(), Credential.MD5.digest(a.getTitle()).substring(4, 11), false);
        }

        embed.setFooter("From BlackBoard", dbot.getSelfUser().getAvatarUrl());
        txtChannel.sendMessage(embed.build()).queue();
    }

    @Override
    public void update(Announcement[] newAnnouncements) {
        publishAnnouncements(newAnnouncements);
        publishAnnouncementTitles(newAnnouncements);
    }

    public void disconnect() {
        dbot.shutdownNow();
    }
}
