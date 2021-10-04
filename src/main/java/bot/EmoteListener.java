package bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.OffsetDateTime;

public class EmoteListener extends ListenerAdapter {
    Message message;
    int track = 0;
    String[] page;



    public void onMessageReactionAdd (@Nonnull MessageReactionAddEvent event) {
       handleHelper (event);
        }


    private MessageEmbed getEditedEmbed() {
        MessageEmbed messageEmbed = new EmbedBuilder()
                .setTitle("  ``` List of Songs  ```")
                .setDescription("```" + page[track] + "```")
                .setColor(new Color(4818551))
                .setTimestamp(OffsetDateTime.now())
                .setFooter("Youtube Analytics", "https://gamepress.gg/grandorder/sites/grandorder/files/2018-08/196_Ereshkigal_4.png")
                .build();
        return messageEmbed;
    }

    public void handleHelper(MessageReactionAddEvent event) {
        if ( event.getReactionEmote().getName() == "➡")
            track++;
        else if (event.getReactionEmote().getName() == "⬅")
            track--;


        if (track == 0) {
            message.removeReaction("➡").queue();
            message.removeReaction("⬅").queue();
            message.addReaction("➡").queue();
        }
        else if (track > 0 && track < page.length) {
            message.removeReaction("➡").queue();
            message.removeReaction("⬅").queue();
            message.addReaction("⬅").queue();
            message.addReaction("➡").queue();
        } else {
            message.removeReaction("➡").queue();
            message.removeReaction("⬅").queue();
            message.addReaction("⬅").queue();
        }
    }


    public int getTrack() {
        return track;
    }

}
