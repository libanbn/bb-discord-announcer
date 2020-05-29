package app;

import app.command.CommandManager;
import app.entity.Announcement;
import app.listener.AnnouncementListener;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import java.awt.*;
import java.time.Instant;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.LoggerFactory;

/**
 * Posts announcements to a discord channel as a bot.
 */
public class DiscordBot implements AnnouncementListener {

    private JDA dbot;
    private TextChannel txtChannel;

    /**
     * Creates the Discord bot.
     *
     * @param token         the Discord bots API token from Discord Developer Portal
     * @param channelId     channel id of the channel where the bot posts announcements
     * @throws Exception    when the discord bot couldn't be created
     */
    public DiscordBot(String token, String channelId) throws Exception {
        Logger jdaLogger = (Logger) LoggerFactory.getLogger("net.dv8tion.jda");
        jdaLogger.setLevel(Level.INFO);

        dbot = new JDABuilder(AccountType.BOT)
                .setToken(token)
                .setActivity(Activity.watching("Blackboard Garbage"))
                .build()
                .awaitReady();

        // Add command event lister
        dbot.addEventListener(new CommandManager());

        // Set the channel where announcements will be submitted
        txtChannel = dbot.getTextChannelById(channelId);

        // Only show errors after the bot is logged in
        jdaLogger.setLevel(Level.ERROR);
    }

    /**
     * Sends all announcements provided to the discord channel in separate discord embeds.
     * @param announcements     announcements to publish to the discord text channel
     */
    private void publishAnnouncements(Announcement[] announcements) {
        for (Announcement a : announcements) {
            String body;

            // Discord limits message bodies up to 2048 characters and it must be chopped down
            if (a.getBody().length() > 2048) {
                body = a.getBody().substring(0, 2041).concat("\n\n...");
            } else {
                body = a.getBody();
            }

            // Create an embed for announcement
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(a.getTitle())
                    .setDescription(body)
                    .setFooter(a.getAuthor().concat(" - ").concat(a.getSubject()), dbot.getSelfUser().getAvatarUrl())
                    .setTimestamp(Instant.ofEpochMilli(a.getTimestamp()))
                    .setColor(Color.GREEN);

            // Send the embed as message to the channel
            txtChannel.sendMessage(embed.build()).queue();
        }
    }

    /**
     * Sums up all the provided announcements to their titles and a unique identifier.
     * @param announcements     a set of announcement
     */
    private void publishAnnouncementTitles(Announcement[] announcements) {
        EmbedBuilder embed = new EmbedBuilder();

        for (Announcement a : announcements) {
            embed.addField(a.getTitle(),
                a.getSubject(),
                false);
        }

        txtChannel.sendMessage(embed.build()).queue();
    }

    /**
     * Processes the data received from event and sends it to the appropriate Discord channel.
     * @param newAnnouncements      a set of unread announcements sent along the event
     */
    @Override
    public void update(Announcement[] newAnnouncements) {
        publishAnnouncements(newAnnouncements);

        // TODO This will be used to get the overview of the announcement using Discord commands
        //publishAnnouncementTitles(newAnnouncements);
    }
}
