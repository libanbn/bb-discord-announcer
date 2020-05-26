package app.command;

import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * This class is responsible for handling command related events.
 */
public class CommandManager extends ListenerAdapter {

    /**
     * This event is fired when indicated that a message is received in a TextChannel. We use it to
     * retrieve the message and check if it's a bot command.
     *
     * @param event event that indicates when a message is received in a TextChannel.
     */
    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {

        // If the bot itself sends a message, just return
        if (event.getAuthor().equals(event.getJDA().getSelfUser())) {
            return;
        }

        // Get message inside event
        String[] message = event.getMessage().getContentRaw().split(" ");

        // Check if the message contains the initial "!bb"
        if (message[0].equalsIgnoreCase("!bb") && message.length > 1) {
            switch (message[1].toLowerCase()) {

                case "hi":
                    event.getChannel()
                        .sendMessage("Hello " + event.getAuthor().getName() + "\uD83D\uDD90")
                        .queue();
                    break;

                case "hug":
                    event.getChannel().sendMessage("Here you go\uD83E\uDD17").queue();
                    break;

                case "slap":
                    event.getChannel().sendMessage("Slap you back✋").queue();
                    break;

                default: // Just a dummy helper, create a nice help menu when implementing real commands
                    event.getChannel().sendMessage(""
                        + "Supported commands are: hi, hug, slap").queue();
                    break;
            }
            // If command format is wrong return a helper message
        } else if (message[0].contains("!") || message[0].toLowerCase().contains("bb") ||
            event.getMessage().getContentRaw().equalsIgnoreCase("!bb")) {
            event.getChannel().sendMessage("Please use this format: !bb COMMAND [options]").queue();
        }
    }
}
