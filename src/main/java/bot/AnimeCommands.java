package bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.sandrohc.jikan.Jikan;
import net.sandrohc.jikan.model.anime.Anime;
import net.sandrohc.jikan.model.common.Studio;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Objects;

public class AnimeCommands {
    public Jikan jikan;

    public AnimeCommands() {
        this.jikan = new Jikan();
    }

    public void searchAnimeByName(String[] message, MessageReceivedEvent event) {
        String name = message[1];
        for (int i = 2; i < message.length; i++) { //concatenates the name of the anime
            name += "%" + message[i];
        }
        try {
            Anime anime = Objects.requireNonNull(jikan.query().anime().search()
                    .query(name)
                    .limit(5)
                    .execute()
                    .collectList()
                    .block()).get(0);
            Object[] listOfProducers = anime.getStudios().toArray();
            Studio studio = (Studio) listOfProducers[0];
            String isAiring = anime.airing ? "Currently Airing" : "Finished Airing";
            String episodes = anime.episodes != null ? anime.episodes.toString() : "N/A";
            String duration = anime.duration != null ? String.valueOf(anime.duration.toMinutes()) : "N/A";
            MessageEmbed embed = new EmbedBuilder().setTitle(anime.title, anime.url)
                    .setDescription(anime.synopsis)
                    .setColor(new Color(13231366))
                    .setTimestamp(OffsetDateTime.now())
                    .setFooter("MAL Rewrite", "https://gamepress.gg/grandorder/sites/grandorder/files/2018-08/196_Ereshkigal_4.png")
                    .setThumbnail(anime.images.getPreferredImageUrl())
                    .addField("Type", anime.type.toString(), false)
                    .addField("Episode", episodes, false)
                    .addField("Airing", isAiring , false)
                    .addField("Duration", duration + "minutes", false)
                    .addField("Producer", studio.getName(), true)
                    .addField("Rating", anime.score + " ⭐", true)
                    .build();
            event.getChannel().sendMessage(embed).queue();
        } catch (Exception e) {
            event.getChannel().sendMessage("Unable to find Any anime named: " + name).queue();
        }
    }
    public void getTopSeasonalAnime(MessageReceivedEvent event) {
        System.out.println();
        try {
            Collection<Anime> results = jikan.query().season().current().execute().collectList().block();
            for (Anime a : results) {
                System.out.println(a.title);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
