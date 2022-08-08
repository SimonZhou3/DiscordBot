package bot;

import com.mongodb.MongoNamespace;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class PlaylistCommands extends MusicCommands {

    private final MongoDatabase db;

    public PlaylistCommands(MongoDatabase db) {
        this.db = db;
    }

    private boolean isUrl(String link) {
        try {
            new URL(link).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getArg(String[] message) {
        StringBuilder arg = new StringBuilder(message[1]);
        for (int i = 2; i < message.length; i++) {
            arg.append(" ").append(message[i]);
        }
        return arg.toString();
    }

    public void removePlaylist(String[] message, MessageReceivedEvent event) {
        String playlistName = message[1];
        int position = Integer.parseInt(message[2]);
        FindIterable<Document> iterable = db.getCollection(playlistName).find();
        Iterator<Document> it = iterable.iterator();
        int i = 1;
        while (it.hasNext()) {
            Document doc = (Document) it.next();
            if (i == position) {
                Bson filter = Filters.eq("_id", doc.get("_id"));
                String link = (String) doc.get("link");
                db.getCollection(playlistName).findOneAndDelete(filter);
                event.getChannel().sendMessage("Successfully deleted: " + link).queue();
                break;
            }
            i++;
        }
    }

    public void renamePlaylist(String[] message, MessageReceivedEvent event) {
        String oldPlaylistName = message[1];
        String newPlaylistName = message[2];
        MongoCollection<Document> collection = db.getCollection(oldPlaylistName);
        MongoNamespace newName = new MongoNamespace("playlist", newPlaylistName);
        collection.renameCollection(newName);
        event.getChannel().sendMessage("Renamed " + oldPlaylistName + " to " + newPlaylistName).queue();
    }

    public void playPlaylist(String[] message, MessageReceivedEvent event) {
        String playlistName = getArg(message);
        TextChannel channel = event.getTextChannel();
        Member self = event.getGuild().getSelfMember();
        Member user = event.getMember();
        String link = "";
        List<String> playlist = new ArrayList<>();
        FindIterable<Document> iterable = db.getCollection(playlistName).find();
        for (Document doc : iterable) {
            link = (String) doc.get("link");
            if (isUrl(link)) {
                playlist.add(link);
            }
        }
        Collections.shuffle(playlist);
        for (String s : playlist) {
            handle(channel, self, user, s, event);
        }
    }

    //MODIFIES: this
    //EFFECT: Creates a JSON file with the name of the playlist, bot.Playlist name must be one word.
    public void createPlaylist(String[] message, MessageReceivedEvent event) {
        if (message.length != 2) {
            event.getChannel().sendMessage("Playlist name must be one word only!").queue();
        } else {
            String playlistName = message[1];
            db.getCollection(playlistName);
            event.getChannel().sendMessage("Playlist: " + playlistName + " has been created!").queue();
        }
    }

    public void savePlaylist(String[] message, MessageReceivedEvent event) {
        String playListName = message[1];
        StringBuilder url = new StringBuilder();
        for (int i = 2; i < message.length; i++) {
            url.append(message[i]);
            if (i + 1 < message.length) {
                url.append(" ");
            }
        }
        if (url.length() == 0) {
            event.getChannel().sendMessage("source of audio is empty!").queue();
            return;
        }
        if (!isUrl(url.toString())) {
            event.getChannel().sendMessage("Must be a link!").queue();
            return;
        }
        MongoCollection<Document> col = db.getCollection(playListName);
        Document obj = new Document("link", url.toString());
        col.insertOne(obj);
        event.getChannel().sendMessage("Playlist: " + playListName + " has saved a song!").queue();

    }

    public void readPlaylist(String[] message, MessageReceivedEvent event) {
        int size = 0;
        String playlistName = getArg(message);

        StringBuilder formatter = new StringBuilder();
        int position = 1;

        FindIterable<Document> iterable = db.getCollection(playlistName).find();
        Iterator it = iterable.iterator();
        while (it.hasNext()) {
            Document doc = (Document) it.next();
            String link = (String) doc.get("link");
//                try {
//                    URL rest = new URL(link);
//                    HttpURLConnection hr = (HttpURLConnection) rest.openConnection();
//                    if (hr.getResponseCode() == 200) {
//                        InputStream im = hr.getInputStream();
//                        StringBuffer sb = new StringBuffer();
//                        BufferedReader br = new BufferedReader(new InputStreamReader(im));
//                        String line = br.readLine();
//                        StringBuilder contentBuilder = new StringBuilder();
//                        while (line != null) {
//                            contentBuilder.append(line);
//                            line = br.readLine();
//                        }
//                        Document doc = Jsoup.parse(html);
//                        System.out.println(contentBuilder);
//                    }
//                } catch (Exception e) {
//                    event.getChannel().sendMessage(e.getMessage()).queue();
//                }
            formatter.append(position).append(". ").append(doc.get("link")).append("\n");
            position++;
        }
        try {
            event.getChannel().sendMessage(formatter.toString()).queue();
        } catch (IllegalArgumentException e) {
            event.getChannel().sendMessage("There are " + size + " songs in the playlist").queue();
        }
    }
}
