package app.command;

import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandManager extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {

        // If the bot itself sends a message, just return
        if (event.getAuthor().equals(event.getJDA().getSelfUser())) return;

        // Get message inside event
        String[] message = event.getMessage().getContentRaw().split(" ");

        // Check if the message contains the initial "!bb"
        if (message[0].equalsIgnoreCase("!bb")) {
            switch (message[1].toLowerCase()) {

                case "hi":
                    event.getChannel().sendMessage("Hello " + event.getAuthor().getName() + "\uD83D\uDD90").queue();
                    break;

                case "hug":
                    event.getChannel().sendMessage("Here you go\uD83E\uDD17").queue();
                    break;

                case "slap":
                    event.getChannel().sendMessage("Slap you back✋").queue();
                    break;

                default:
                    event.getChannel().sendMessage("Invalid command!❌").queue();
                    break;
            }
        } else if (message[0].contains("!") || message[0].toLowerCase().contains("bb")) {
            event.getChannel().sendMessage("Please use this format: !bb").queue();
        }




    }
}
