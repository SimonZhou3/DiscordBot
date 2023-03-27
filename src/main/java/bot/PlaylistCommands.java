package bot;

import com.mongodb.MongoCommandException;
import com.mongodb.MongoNamespace;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.json.JsonObject;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.exceptions.detailed.NotFoundException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public void listAllPlaylist(MessageReceivedEvent event) {
        String personalPlaylist = "";
        String publicPlaylist = "";
        MongoIterable<String> allCollections = db.listCollectionNames();
        for (String s : allCollections) {
            MongoCollection<Document> collection = db.getCollection(s);
            System.out.println(s);
            Bson filter = Filters.empty();
            Bson projection = Projections.fields(Projections.include("isPrivate", "userID"), Projections.excludeId());
            Document isPrivate = collection.find(filter).projection(projection).first();
            if (isPrivate != null) {
                if (isPrivate.isEmpty() || isPrivate.get("isPrivate").equals(false)) {
                    publicPlaylist += "- " + s + "\n";
                } else if (isPrivate.get("userID").equals(event.getAuthor().getId())) {
                    personalPlaylist += "- " + s + "\n";
                }
            }
        }
        MessageEmbed messageEmbed = new EmbedBuilder().setTitle("All Playlists")
                .setColor(new Color(13231366))
                .setTimestamp(OffsetDateTime.now())
                .setFooter("MAL Rewrite", "https://gamepress.gg/grandorder/sites/grandorder/files/2018-08/196_Ereshkigal_4.png")
                .addField("Public Playlist", publicPlaylist, false)
                .addField("Personal Playlist", personalPlaylist, false)
                .build();

        event.getChannel().sendMessageEmbeds(messageEmbed).queue();
    }

    public void removePlaylist(String[] message, MessageReceivedEvent event) {
        String playlistName = message[1];
        int position = Integer.parseInt(message[2]);
        MongoCollection<Document> collection = db.getCollection(playlistName);
        if (havePermissionToPlaylist(collection, event)) {
            FindIterable<Document> iterable = collection.find();
            Iterator<Document> it = iterable.iterator();
            int i = 1;
            while (it.hasNext()) {
                Document doc = (Document) it.next();
                if (!doc.containsKey("userID") && !doc.containsKey("isPrivate")) {
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
        } else {
            event.getChannel().sendMessage("You do not have permission for this playlist").queue();
        }
    }

    public void renamePlaylist(String[] message, MessageReceivedEvent event) {
        if (message.length < 1) {
            event.getChannel().sendMessage("``` Missing arguments! ```").queue();
            return;
        }
        String oldPlaylistName = message[1];
        String newPlaylistName = message[2];
        MongoCollection<Document> collection = db.getCollection(oldPlaylistName);
        if (havePermissionToPlaylist(collection, event)) {
            MongoNamespace newName = new MongoNamespace("playlist", newPlaylistName);
            try {
                collection.renameCollection(newName);
                event.getChannel().sendMessage("Renamed " + oldPlaylistName + " to " + newPlaylistName).queue();
            } catch (MongoCommandException e) {
                if (e.getErrorCode() == 48) {
                    event.getChannel().sendMessage("A playlist named " + newPlaylistName + " already exists").queue();
                }

            }
        }
    }

    public void playPlaylist(String[] message, MessageReceivedEvent event) {
        String playlistName = getArg(message);
        TextChannel channel = event.getChannel().asTextChannel();
        Member self = event.getGuild().getSelfMember();
        Member user = event.getMember();
        String link = "";
        List<String> playlist = new ArrayList<>();
        MongoCollection<Document> collection = db.getCollection(playlistName);
        if (!havePermissionToPlaylist(collection, event)) {
            event.getChannel().sendMessage("You do not have permission for this playlist").queue();
        } else {
            FindIterable<Document> iterable = collection.find();
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

    }

    //MODIFIES: this
    //EFFECT: Creates a JSON file with the name of the playlist, Playlist name must be one word.
    public void createPlaylist(String[] message, MessageReceivedEvent event) {
        if (message.length < 3) {
            event.getChannel().sendMessage("Invalid usage -- [name][public/private]").queue();
            return;
        }
        String playlistName = message[1];
        String status = message[2];
        if (!status.equals("public") && !status.equals("private")) {
            event.getChannel().sendMessage("Please Specify whether the playlist is public or private").queue();
            return;
        }
        db.getCollection(playlistName);
        if (status.equals("private")) {
            String userID = event.getAuthor().getId();
            Document obj = new Document("isPrivate", true);
            obj.append("userID", userID);
            MongoCollection<Document> col = db.getCollection(playlistName);
            col.insertOne(obj);
        } else {
            String userID = event.getAuthor().getId();
            Document obj = new Document("isPrivate", false);
            MongoCollection<Document> col = db.getCollection(playlistName);
            col.insertOne(obj);
        }
        event.getChannel().sendMessage("Playlist: " + playlistName + " has been created!").queue();

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
        if (!db.listCollectionNames().into(new ArrayList<String>()).contains(playListName)) {
            event.getChannel().sendMessage("` " + playListName + " has not been created yet`").queue();
            return;
        }
        MongoCollection<Document> col = db.getCollection(playListName);
        if (havePermissionToPlaylist(col, event)) {
            Document obj = new Document("link", url.toString());
            col.insertOne(obj);
            event.getChannel().sendMessage("`Playlist: " + playListName + " has saved a song!`").queue();
        } else {
            event.getChannel().sendMessage("You do not have permission for this playlist").queue();
        }
    }

    private boolean havePermissionToPlaylist(MongoCollection<Document> collection, MessageReceivedEvent event) {
        Bson filter = Filters.empty();
        Bson projection = Projections.fields(Projections.include("isPrivate", "userID"), Projections.excludeId());
        Document isPrivate = collection.find(filter).projection(projection).first();
        if (isPrivate.isEmpty() || isPrivate.get("isPrivate").equals(false) || (isPrivate.get("userID").equals(event.getAuthor().getId()))) {
            return true;
        } else if (!isPrivate.get("userID").equals(event.getAuthor().getId())) {
            return false;
        }
        return false;
    }

    private String getYouTubeId(String youTubeUrl) {
        String pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(youTubeUrl);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return "error";
        }
    }

    public void readPlaylist(String[] message, MessageReceivedEvent event) {
        int size = 0;
        String playlistName = getArg(message);

        StringBuilder formatter = new StringBuilder("`");
        int position = 1;
        MongoCollection<Document> collection = db.getCollection(playlistName);
        if (havePermissionToPlaylist(collection, event)) {
            if (collection.countDocuments() <= 0) {
                event.getChannel().sendMessage("```No songs are in this playlist```").queue();
            }
            FindIterable<Document> iterable = collection.find();
            Iterator it = iterable.iterator();
            while (it.hasNext()) {
                Document doc = (Document) it.next();
                if (!doc.containsKey("userID") && !doc.containsKey("isPrivate")) {
                    String link = getYouTubeId((String) doc.get("link"));
                    try {
//                        URL url = new URL("https://youtube.googleapis.com/youtube/v3/videos?part=snippet&id="+link+"&key="+ Dotenv.load().get("YOUTUBE_APIKEY"));
                        URL url = new URL("https://youtube.googleapis.com/youtube/v3/videos?part=snippet&id=" + link + "&key=" + System.getenv("YOUTUBE_APIKEY"));
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(5000);
                        connection.setReadTimeout(5000);
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(connection.getInputStream()));
                        String inputLine;
                        StringBuffer content = new StringBuffer();
                        while ((inputLine = in.readLine()) != null) {
                            content.append(inputLine);
                        }
                        JsonObject response = new JsonObject(content.toString());
                        formatter.append(position).append(". ").append(response.toBsonDocument().get("items").asArray().get(0).asDocument().get("snippet").asDocument().get("title").asString().getValue()).append("\n");
                        connection.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    position++;
                }
            }

            try {
                formatter.append("`");
                event.getChannel().sendMessage(formatter.toString()).queue();
            } catch (IllegalArgumentException e) {
                event.getChannel().sendMessage("There are " + size + " songs in the playlist").queue();
            }
        } else {
            event.getChannel().sendMessage("You do not have permission for this playlist").queue();
        }
    }

    public void convertSpotifyPlaylistToYoutubePlaylist(String[] message, MessageReceivedEvent event) {
        String regex = "(?<=https:\\/\\/open.spotify.com\\/playlist\\/)[^?]*";
        Pattern compiledPattern = Pattern.compile(regex);
        Matcher matcher = compiledPattern.matcher(message[1]);
        String spotifyURL;
        if (matcher.find()) {
            spotifyURL = matcher.group();
        } else {
            event.getChannel().sendMessage("Invalid Link!").queue();
            return;
        }

        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId(System.getenv("SPOTIFY_CLIENTID"))
                .setClientSecret(System.getenv("SPOTIFY_SECRETID"))
                .setRedirectUri(SpotifyHttpManager.makeUri("https://discord.com/"))
                .build();
        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials()
                .build();
        try {
            ClientCredentials clientCredentials = clientCredentialsRequest.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
            Paging<PlaylistTrack> getPlaylistsItemsRequest = spotifyApi.getPlaylistsItems(spotifyURL).build().execute();
            TextChannel channel = event.getChannel().asTextChannel();
            Member self = event.getGuild().getSelfMember();
            Member user = event.getMember();
            for (int i = 0; i < getPlaylistsItemsRequest.getItems().length; i++) {
                String query = "ytsearch:" + getPlaylistsItemsRequest.getItems()[i].getTrack().getName() + " " + ((Track) getPlaylistsItemsRequest.getItems()[i].getTrack()).getArtists()[0].getName();
                handle(channel, self, user, query, event);
            }
        } catch (NotFoundException e) {
            event.getChannel().sendMessage("Invalid Playlist Link or playlist is private!").queue();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
