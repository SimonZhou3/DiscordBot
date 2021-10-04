package bot;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


public class FPS extends ListenerAdapter {

    private String userID;
    private MessageChannel channel;

    public FPS(MessageReceivedEvent event) {
        userID = event.getAuthor().getId();
        channel = event.getChannel();
        channel.sendMessage("React to start game!").queue((message) -> {
            message.addReaction("✔").queue();
        });
    }
}

