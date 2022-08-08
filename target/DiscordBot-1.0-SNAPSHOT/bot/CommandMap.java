package bot;

import com.github.doomsdayrs.jikan4java.core.Connector;
import com.github.doomsdayrs.jikan4java.data.enums.Season;
import com.github.doomsdayrs.jikan4java.data.model.main.manga.Manga;
import com.github.doomsdayrs.jikan4java.data.model.main.season.SeasonSearch;
import com.github.doomsdayrs.jikan4java.data.model.main.season.SeasonSearchAnime;
import com.github.doomsdayrs.jikan4java.data.model.support.basic.meta.Genre;
import com.mongodb.MongoNamespace;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import okhttp3.ResponseBody;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.awt.*;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

enum command {
    HELP,
    INFO,
    PFP,
    DEL,
    ERESHKIGAL,
    FUTURE,
    ANI,
    SEARCH,
    JOIN,
    PLAY,
    LEAVE,
    SKIP,
    REPEAT,
    QUEUE,
    NOW,
    SEEK,
    CLEAR,
    PLAYLIST,
    READ,
    CREATE,
    SAVE,
    RENAME,
    REMOVE
}

public class CommandMap extends ListenerAdapter {
    private ArrayList<String> generalCommands = new ArrayList<>();
    private ArrayList<String> animeCommands = new ArrayList<>();
    private ArrayList<String> musicCommands = new ArrayList<>();
    private ArrayList<String> playlistCommands = new ArrayList<>();
    private List<String> page = new ArrayList<>();

    private int pageNumber = 0;
    private static final String PREFIX = "-";
    private static final int SIZEIMAGE = 31;
     private MongoClient client = MongoClients.create(System.getenv("MONGO2"));
//    private MongoClient client = MongoClients.create(Dotenv.load().get("MONGO2"));

    private MongoDatabase db = client.getDatabase("playlist");
    private MongoDatabase dbAnime = client.getDatabase("anime");
    private MongoDatabase dbHyoon = client.getDatabase("photos");


    //Creates a fixated bot.command list for the bot to use
    public CommandMap() {
        generalCommands.add(String.valueOf(PREFIX + command.HELP).toLowerCase() + " -- Shows List of commands");
        generalCommands.add(String.valueOf(PREFIX +command.INFO).toLowerCase() + " -- Information about me");
        generalCommands.add(String.valueOf(PREFIX +command.PFP).toLowerCase() + " @[user] -- Grab Profile Picture of mentioned user");
        generalCommands.add(String.valueOf(PREFIX +command.DEL).toLowerCase() + " -- deletes the specified amount of messages");
        generalCommands.add(String.valueOf(PREFIX +command.ERESHKIGAL).toLowerCase() + " -- ;)");
        animeCommands.add(String.valueOf(PREFIX + command.FUTURE).toLowerCase() + " -- Get a random anticipated anime coming soon");
        animeCommands.add(String.valueOf(PREFIX + command.ANI).toLowerCase() + " -- Get a random anime from a specified year and season");
        animeCommands.add(String.valueOf(PREFIX + command.SEARCH).toLowerCase() + " -- Search for an anime by name");
        musicCommands.add(String.valueOf(PREFIX + command.JOIN).toLowerCase() + " -- Add bot to channel");
        musicCommands.add(String.valueOf(PREFIX + command.LEAVE).toLowerCase() + " -- Remove bot from channel");
        musicCommands.add(String.valueOf(PREFIX + command.PLAY).toLowerCase() + " [URL/Keyword] -- adds the music into the queue ");
        musicCommands.add(String.valueOf(PREFIX + command.SKIP).toLowerCase() + " -- Skips current music playing and plays next in the queue");
        musicCommands.add(String.valueOf(PREFIX + command.CLEAR).toLowerCase() + " -- Clears the current music queue" );
        musicCommands.add(String.valueOf(PREFIX + command.QUEUE).toLowerCase() + " -- Prints all the songs in the queue");
        musicCommands.add(String.valueOf(PREFIX + command.NOW).toLowerCase() + " -- Prints the information of the currently playing song");
        musicCommands.add(String.valueOf(PREFIX + command.REPEAT).toLowerCase() + " -- Repeats the current song");
        musicCommands.add(String.valueOf(PREFIX + command.SEEK).toLowerCase() + " [seconds] -- seeks to a specific place in the song");
        playlistCommands.add(String.valueOf(PREFIX + command.CREATE).toLowerCase() + "[name] -- Creates an instance of a playlist to add songs");
        playlistCommands.add(String.valueOf(PREFIX + command.SAVE).toLowerCase() + "[name] [url] -- saves the link of a audio source to the specified playlist");
        playlistCommands.add(String.valueOf(PREFIX + command.READ).toLowerCase() + "[name] -- Reads the instance of the playlist");
        playlistCommands.add(String.valueOf(PREFIX + command.PLAYLIST).toLowerCase() + "[name] -- Queues all songs in the playlist");
        playlistCommands.add(String.valueOf(PREFIX + command.RENAME).toLowerCase() + "[old playlist] [new playlist] -- Renames an old playlist to a new playlist");
        playlistCommands.add(String.valueOf(PREFIX + command.REMOVE).toLowerCase() + "[playlist] [position] -- Removes a particular audio source from the playlist");

    }

    // Detecting whether user initiated a bot.command
    public void get(String[] command, MessageReceivedEvent event) {
        switch (command[0].toLowerCase()) {
            case PREFIX + "help" :
                help(event);
                break;
            case PREFIX + "info" :
                info(event);
                break;
            case PREFIX + "pfp":
                pfp(command,event);
                break;
            case PREFIX + "del":
                del(command,event);
                break;
            case PREFIX + "ereshkigal":
                ereshkigal(event);
                break;
            case PREFIX + "future":
                future(event);
                break;
            case PREFIX + "ani":
                rand(command,event);
                break;
            case PREFIX +  "search":
                searchByName(command,event);
                break;
            case PREFIX + "manga":
                manga(command,event);
                break;
            case PREFIX + "join":
                join(event);
                break;
            case PREFIX + "play":
            case PREFIX + "p":
                play(command,event);
                break;
            case PREFIX + "clear":
                clear(command,event);
                break;
            case PREFIX + "s":
            case PREFIX + "skip":
                skip(event);
                break;
            case PREFIX + "repeat":
                repeat(event);
                break;
            case PREFIX + "leave":
                leave(event);
                break;
            case PREFIX + "now":
                now(event);
                break;
            case PREFIX + "queue":
                queue(event);
                break;
            case PREFIX + "seek":
                seek(command, event);
                break;
            case PREFIX + "supremacy":
                hyoon(event);
                break;
            case PREFIX +"boost":
                bassBoost(command,event);
                break;
            case PREFIX + "read":
                readPlaylist(command,event);
                break;
            case PREFIX + "save":
                savePlaylist(command,event);
                break;
            case PREFIX + "create":
               createPlaylist(command,event);
                break;
            case PREFIX + "playlist":
                playPlaylist(command,event);
                break;
            case PREFIX + "rename":
                renamePlaylist(command,event);
                break;
            case PREFIX + "remove":
                removePlaylist(command, event);
            default:
                System.out.println("Enter default");
                break;
        }
    }

    private void removePlaylist(String[] message, MessageReceivedEvent event) {
        String playlistName = message[1];
        int position = Integer.parseInt(message[2]);
        FindIterable<Document> iterable = db.getCollection(playlistName).find();
        Iterator it = iterable.iterator();
        int i = 1;
        while(it.hasNext()) {
            Document doc = (Document) it.next();
            if (i == position) {
               Bson filter = Filters.eq("_id",doc.get("_id"));
               String link = (String) doc.get("link");
               db.getCollection(playlistName).findOneAndDelete(filter);
               event.getChannel().sendMessage("Sucesfully deleted: " + link).queue();
               break;
            }
            i++;
        }
    }

    private void renamePlaylist(String[] message, MessageReceivedEvent event) {
        String oldPlaylistName = message[1];
        String newPlaylistName = message[2];
        MongoCollection<Document> collection = db.getCollection(oldPlaylistName);
        MongoNamespace newName = new MongoNamespace("playlist",newPlaylistName);
        collection.renameCollection(newName);
        event.getChannel().sendMessage("Renamed " + oldPlaylistName + " to " + newPlaylistName).queue();
    }

    private void playPlaylist(String[] message, MessageReceivedEvent event) {
        String playlistName = getArg(message);
        TextChannel channel = event.getTextChannel();
        Member self = event.getGuild().getSelfMember();
        Member user = event.getMember();
        String link = "";
        List<String> playlist = new ArrayList<>();
        FindIterable<Document> iterable = db.getCollection(playlistName).find();
        Iterator it = iterable.iterator();
        while (it.hasNext()) {
            Document doc = (Document) it.next();
            link = (String) doc.get("link");
            if (isUrl(link)) {
                playlist.add(link);
            }
        }
        Collections.shuffle(playlist);
        for (int i = 0; i < playlist.size(); i++) {
            handle(channel,self,user,playlist.get(i),event);
        }
    }

    private String getArg(String[] message) {
        String arg = message[1];
        for (int i = 2; i < message.length; i ++) {
            arg += " " + message[i];
        }
        return arg;
    }

    //MODIFIES: this
    //EFFECT: Creates a JSON file with the name of the playlist, bot.Playlist name must be one word.
private void createPlaylist(String[] message, MessageReceivedEvent event) {
        if (message.length!= 2) {
            event.getChannel().sendMessage("Playlist name must be one word only!").queue();
            return;
        } else {
            String playlistName = message[1];
            db.getCollection(playlistName);
            event.getChannel().sendMessage("Playlist: " + playlistName +" has been created!").queue();
        }
}

    private void savePlaylist(String[] message, MessageReceivedEvent event) {
        String playListName = message[1];
        String url = "";
        for (int i = 2; i < message.length; i ++) {
            url += message[i];
            if (i + 1 < message.length) {
                url += " ";
            }
        }
        if (url.isEmpty()) {
            event.getChannel().sendMessage("source of audio is empty!").queue();
            return;
        }
        if (!isUrl(url))
            url = "ytsearch:" + url;
        MongoCollection col = db.getCollection(playListName);
        Document obj = new Document("link",url);
        col.insertOne(obj);
        event.getChannel().sendMessage("Playlist: " + playListName + " has saved a song!").queue();

    }

    private void readPlaylist(String[] message, MessageReceivedEvent event) {
        int size = 0;
        String playlistName = getArg(message);

        String formatter = "";
        int position = 1;

        FindIterable<Document> iterable = db.getCollection(playlistName).find();
        Iterator it = iterable.iterator();
        while(it.hasNext()) {
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
            formatter += position +". " + doc.get("link") +"\n";
            position++;
            }
        try {
            event.getChannel().sendMessage(formatter).queue();
        } catch (IllegalArgumentException e) {
            event.getChannel().sendMessage("There are " + size + " songs in the playlist").queue();
        }
    }

    private void bassBoost(String[] message, MessageReceivedEvent event) {
        int percent = Integer.valueOf(message[1]);
        if (percent > 1000) {
            event.getChannel().sendMessage("Value must be below 1000").queue();
            return;
        } else {
            PlayerManager.getInstance().getMusicManager(event.getGuild()).trackScheduler.bassBoost(percent);
            event.getChannel().sendMessage("Bass is boosted by: " + percent + "%").queue();
        }
    }


    private void seek(String[] message, MessageReceivedEvent event) {
        if (!event.getMember().getVoiceState().getChannel().equals(event.getGuild().getSelfMember().getVoiceState().getChannel())) {
            event.getChannel().sendMessage("Must be in the same voice channel!");
            return;
        }
        long totalTime;
        if (message.length == 1 ) {
            event.getChannel().sendMessage("Please include a time to seek to").queue();
            return;
        }
        TrackScheduler ts = PlayerManager.getInstance().getMusicManager(event.getGuild()).trackScheduler;
        if (ts.currentTrack() == null) {
            event.getChannel().sendMessage("No song is currently playing").queue();
        }
        String time = message[1];
        if (time.contains(":")) {
            String[] timeFormat = time.split(":");
            if (timeFormat.length == 2) {
                long minutes = TimeUnit.MINUTES.toMillis(Long.valueOf(timeFormat[0]));
                long seconds = TimeUnit.SECONDS.toMillis(Long.valueOf(timeFormat[1]));
                totalTime = minutes + seconds;
            }
             else {
                 long hours = TimeUnit.HOURS.toMillis(Long.valueOf(timeFormat[0]));
                 long minutes = TimeUnit.MINUTES.toMillis(Long.valueOf(timeFormat[1]));
                 long seconds = TimeUnit.SECONDS.toMillis(Long.valueOf(timeFormat[2]));
                 totalTime = hours + minutes + seconds;
            }
        } else {
            try {
                totalTime = TimeUnit.SECONDS.toMillis(Long.valueOf(time));
            } catch (Exception e) {
                event.getChannel().sendMessage("Argument must be a number in seconds!").queue();
                return;
            }
        }
        ts.currentTrack().setPosition(totalTime);
        event.getChannel().sendMessage("Seeking... <a:guraone:812015049979199549>").queue();
    }

    private void hyoon(MessageReceivedEvent event) {
        Random random = new Random();
        int randomInd = random.nextInt(SIZEIMAGE);
        String path = "hyoon" + String.valueOf(randomInd);
        for (Document hyoon : dbHyoon.getCollection("hyoon").find()) {
            if (hyoon.containsKey(path)) {
                event.getChannel().sendMessage((String) hyoon.get(path)).queue();
            }
        }
    }

    private void queue(MessageReceivedEvent event) {
        TrackScheduler ts = PlayerManager.getInstance().getMusicManager(event.getGuild()).trackScheduler;
        List<AudioTrack> currentQueue = ts.currentQueue();
        List<String> list = new ArrayList<>();
        MessageEmbed messageEmbed;

        if (currentQueue.size() == 0) {
            messageEmbed = new EmbedBuilder()
                    .setDescription("No Songs in the queue")
                    .build();
            event.getChannel().sendMessage(messageEmbed).queue();
        } else {
            String compileList = "";
            int position = 1;

            for (AudioTrack at : currentQueue) {
                String titleFormat = String.valueOf(position) + ". " + at.getInfo().title + "\n";
                if ((compileList.length() + titleFormat.length() < 2000)) {
                    compileList += titleFormat;
                    position++;
                } else {
                    list.add(compileList);
                    compileList = titleFormat;
                }
            }
            if (list.isEmpty()) {
                list.add(compileList);
            }
            messageEmbed = new EmbedBuilder()
                    .setTitle("  ``` List of Songs  ```")
                    .setDescription("```" + list.get(0) + "```")
                    .setColor(new Color(4818551))
                    .setTimestamp(OffsetDateTime.now())
                    .setFooter("Youtube Analytics", "https://gamepress.gg/grandorder/sites/grandorder/files/2018-08/196_Ereshkigal_4.png")
                    .build();
            event.getChannel().sendMessage(messageEmbed).queue();
        }
    }

    //prints currently playing song
    private void now(MessageReceivedEvent event) {
        TrackScheduler ts = PlayerManager.getInstance().getMusicManager(event.getGuild()).trackScheduler;
         AudioTrack audioTrack = ts.currentTrack();
         if (audioTrack == null || audioTrack.getPosition() == audioTrack.getDuration()) {
             MessageEmbed messageEmbed = new EmbedBuilder()
                     .setDescription("No Song is playing currently!")
                     .build();
             event.getChannel().sendMessage(messageEmbed).queue();
             return;
         }
         String title = audioTrack.getInfo().title;
         String url = audioTrack.getInfo().uri;
         String totalTime = convertTime(audioTrack.getDuration());
         String currentTime = convertTime(audioTrack.getPosition());
         float timeRatio = (float) audioTrack.getPosition() / (float) audioTrack.getDuration();
         int position = Math.round(timeRatio * 49);
        System.out.println(position);
         String visualizer = "|";
         for (int i = 0; i < 50; i++) {
                if (i == position) {
                    visualizer += ":red_circle:";
                }
                else
                    visualizer += "-";
         }
         visualizer += "|";
         MessageEmbed messageEmbed = new EmbedBuilder()
                 .setTitle("  ```" + title + "```", url)
                 .setDescription(currentTime + "/" + totalTime + "\n" + visualizer)
                 .setColor(new Color(4818551))
                 .setTimestamp(OffsetDateTime.now())
                 .setFooter("Youtube Analytics", "https://gamepress.gg/grandorder/sites/grandorder/files/2018-08/196_Ereshkigal_4.png")
                 .build();
        event.getChannel().sendMessage(messageEmbed).queue();
         }


    private String convertTime (Long time) {
        Long hour = TimeUnit.MILLISECONDS.toHours(time);
        Long minute = TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(hour);
        Long second = TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(minute) -TimeUnit.HOURS.toSeconds(hour);
        String secondFormat = second.toString();
        String minuteFormat = minute.toString();
        if (second < 10) {
            secondFormat = "0" + second;
        }
        if (hour == 0) {
            return minute + ":" + secondFormat;
        } else {
            if (minute < 10)
                    minuteFormat = "0" + minute;
            return hour + ":" + minuteFormat + ":" + secondFormat;
        }
    }

    //Makes bot leave and clear the queue
    public void leave (MessageReceivedEvent event) {
        if (!event.getMember().getVoiceState().getChannel().equals(event.getGuild().getSelfMember().getVoiceState().getChannel())) {
            event.getChannel().sendMessage("Must be in the same voice channel!").queue();
            return;
        }
        TrackScheduler ts = PlayerManager.getInstance()
                .getMusicManager(event.getGuild())
                .trackScheduler;
        ts.clearQueue();
        ts.setRepeat(false);
        ts.setCurrentlyPlaying();
        ts.nextTrack();
        AudioManager audioManager = event.getGuild().getAudioManager();
        audioManager.closeAudioConnection();
    }

    //Repeat the currently playing song
    private void repeat (MessageReceivedEvent event) {
        if (!event.getMember().getVoiceState().getChannel().equals(event.getGuild().getSelfMember().getVoiceState().getChannel())) {
            event.getChannel().sendMessage("Must be in the same voice channel!").queue();
            return;
        }
        PlayerManager.getInstance().getMusicManager(event.getGuild()).trackScheduler.setRepeat();
       Boolean repeatStat = PlayerManager.getInstance().getMusicManager(event.getGuild()).trackScheduler.getRepeat();
       if (repeatStat)
         event.getChannel().sendMessage("Repeat " + ":regional_indicator_o:" + ":regional_indicator_n: " + "<a:guraone:812015049979199549>").queue();
       else
           event.getChannel().sendMessage("Repeat " + ":regional_indicator_o:" + ":regional_indicator_f:"+ ":regional_indicator_f:")
                   .queue();
    }

    //Skip the current song and play the next song
    private void skip (MessageReceivedEvent event) {
        if (!event.getMember().getVoiceState().getChannel().equals(event.getGuild().getSelfMember().getVoiceState().getChannel())) {
            event.getChannel().sendMessage("Must be in the same voice channel!").queue();
            return;
        }
        TrackScheduler ts = PlayerManager.getInstance()
                .getMusicManager(event.getGuild())
                .trackScheduler;
        if (ts.currentTrack() == null) {
            event.getChannel().sendMessage("No songs are currently being played").queue();
        }
        else {
            ts.nextTrack();
            event.getChannel().sendMessage("Skipping ... ").queue();
        }
    }

    //Clear the Queue
    private void clear (String[] message, MessageReceivedEvent event) {
        if (message.length == 1) {
            PlayerManager.getInstance()
                    .getMusicManager(event.getGuild())
                    .trackScheduler.clearQueue();
            event.getChannel().sendMessage("Queue has been cleared! " + ":x:").queue();
        } else {
            int position = Integer.valueOf(message[1]);
            try {
                AudioTrack song = PlayerManager.getInstance().getMusicManager(event.getGuild()).trackScheduler.clearQueue(position);
                event.getChannel().sendMessage("` Cleared " + song.getInfo().title + "` :x:").queue();
            }  catch (Exception e) {
                event.getChannel().sendMessage("Song is currently playing! use " + PREFIX + "skip").queue();
            }

        }

    }

    //Using Lavaplayer API, Play takes an argument and checks whether if it is a keyword or a URL.
    // If it is a keyword, use API to create a youtube search and call handle, else call handle

    private void play (String[] message, MessageReceivedEvent event) {
        if (event.getGuild().getSelfMember().getVoiceState().getChannel() != null && !event.getMember().getVoiceState().getChannel().equals(event.getGuild().getSelfMember().getVoiceState().getChannel())) {
            event.getChannel().sendMessage("Must be in the same voice channel!").queue();
            return;
        }
        TextChannel channel = event.getTextChannel();
        Member self = event.getGuild().getSelfMember();
        Member user = event.getMember();
        String link = message[1];
        for (int i = 2; i < message.length; i++)
            link += message[i];
        link = String.join(" ", link);
        if (!isUrl(link))
            link = "ytsearch:" + link;
        handle(channel,self,user,link,event);
    }

    private boolean isUrl(String link) {
        try {
            new URL(link).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    //invokes and plays the URL
    public void handle (TextChannel channel, Member selfUser, Member user, String url,MessageReceivedEvent event) {

        final Member member = user;
        final GuildVoiceState memberVoiceState = member.getVoiceState();
        if (!memberVoiceState.inVoiceChannel()) {
            channel.sendMessage("You need to be in a voice channel!").queue();
            return;
        }
        if (!event.getGuild().getAudioManager().isConnected())
            join(event);
        PlayerManager.getInstance().loadAndPlay(channel,url);
    }

    //make bot join the same voice channel as user. If not in the same voice channel then invoke a message
    private void join(MessageReceivedEvent event) {
        VoiceChannel channel = event.getMember().getVoiceState().getChannel();
        if (channel == null) {
            event.getChannel().sendMessage("You are not in a voice channel!").queue();
            return;
        }
        AudioManager audioManager = event.getGuild().getAudioManager();
        audioManager.openAudioConnection(channel);
    }

    //Search an bot.Anime using Jikan API using name. NOTE: Request to find MAL_ID and call the function ani


    private void searchByName (String[] message, MessageReceivedEvent event) {
        String name = message[1];
        for (int i = 2; i < message.length;i++) { //concatenates the name of the anime
            name += "%" + message[i];
        }
        String url = "https://api.jikan.moe/v3/search/anime?q=" + name; //using Jikan request a search URL
        try {
            ResponseBody as = new Connector().request(url);
            String request = as.string(); //parse all the information into a string
            Document doc = Document.parse(request);
            MongoCollection col = dbAnime.getCollection("animeCollection");
             ArrayList<Document> resultQuery = (ArrayList<Document>) doc.get("results");
             Document firstSearched = resultQuery.get(0);
             Object id = firstSearched.get("mal_id");
            System.out.println(id);
           String dataAnimeParse = new Connector().request("https://api.jikan.moe/v3/anime/" + id).string();
            doc = Document.parse(dataAnimeParse);
            col = dbAnime.getCollection("animeCollection");

            String title = (String) doc.get("title");
            String link = (String) doc.get("url");
            String imageURL = (String) doc.get("image_url");
            String episodes =  doc.get("episodes").toString();
            String status = (String) doc.get("status");
            String type = (String) doc.get("type");
            String score =  doc.get("score").toString();
            String duration = (String) doc.get("duration");
            ArrayList<Document> producers = (ArrayList<Document>) doc.get("studios");
            Document firstProducer = producers.get(0);
            String producer = (String) firstProducer.get("name");
            String synopsis = (String) doc.get("synopsis");

            MessageEmbed embed = new EmbedBuilder().setTitle(title, link)
                    .setDescription(synopsis)
                    .setColor(new Color(13231366))
                    .setTimestamp(OffsetDateTime.now())
                    .setFooter("MAL Rewrite", "https://gamepress.gg/grandorder/sites/grandorder/files/2018-08/196_Ereshkigal_4.png")
                    .setThumbnail(imageURL)
                    .addField("Type",type,false)
                    .addField("Episode", episodes, false)
                    .addField("Airing", status, false)
                    .addField("Duration", duration, false)
                    .addField("Producer", producer, true)
                    .addField("Rating",  score  + " ⭐",true)
                    .build();
            event.getChannel().sendMessage(embed).queue();



        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    //Search manga by ID, Not sure if this function is useful, Could be improved, but will keep it as is for now.
    private void manga (String[] message,MessageReceivedEvent event) {
        int id = Integer.parseInt(message[1]);
        CompletableFuture<Manga> as = new Connector().retrieveManga(id);
        while (!as.isDone()){}

        try {
            Manga manga = as.get();
            int episode = manga.component13();
            String episodes = Integer.toString(episode);
            MessageEmbed embed = new EmbedBuilder().setTitle(manga.getTitle(), manga.getUrl())
                    .setDescription(manga.getSynopsis())
                    .setColor(new Color(13231366))
                    .setTimestamp(OffsetDateTime.now())
                    .setFooter("MAL Rewrite", "https://gamepress.gg/grandorder/sites/grandorder/files/2018-08/196_Ereshkigal_4.png")
                    .setThumbnail(manga.getImageURL())
                    .addField("Volumes", episodes, false)
                    .addField("Airing", manga.component10(), false)
                    .addField("Producer", manga.component27().get(0).component3(), true)
                    .build();
            event.getChannel().sendMessage((Message) embed).queue();

        } catch (Exception e) {
            //
        }

    }

    //Randomly chooses an anime depending on user input from a season and year
    private void rand (String[] message, MessageReceivedEvent event) {
        int year = Integer.parseInt(message[1]);
        Season season;
        switch (message[2].toLowerCase()) {
            case "winter":
                season = Season.WINTER;
                break;
            case "summer":
                season = Season.SUMMER;
                break;
            case "fall":
                season = Season.FALL;
                break;
            case "spring":
                season = Season.SPRING;
                break;
            default:
                event.getChannel().sendMessage("Invalid Year!").queue();
                return;
        }

        CompletableFuture<SeasonSearch> seasonAnime = new Connector().seasonSearch(year, season);
        while (!seasonAnime.isDone()) {
        }
        try {
            ArrayList<SeasonSearchAnime> list = seasonAnime.get().getAnimes();
            if (list.size() == 0) {
                event.getChannel().sendMessage("No animes in this year!").queue();
                return;
            }
            Random random = new Random();
            int randomInd = random.nextInt(list.size());
            SeasonSearchAnime anime = list.get(randomInd);
            int episode = anime.getEpisodeCount();
            String episodes = Integer.toString(episode);
            MessageEmbed embed = new EmbedBuilder().setTitle(anime.getTitle(), anime.getUrl())
                    .setDescription(anime.getSynopsis())
                    .setColor(new Color(13231366))
                    .setTimestamp(OffsetDateTime.now())
                    .setFooter("MAL Rewrite", "https://gamepress.gg/grandorder/sites/grandorder/files/2018-08/196_Ereshkigal_4.png")
                    .setThumbnail(anime.getImageURL())
                    .addField("Type",anime.getType(),false)
                    .addField("Episode", episodes, false)
                    .addField("Airing", anime.getAiring_start(), false)
                    .addField("Producer", anime.getProducers().get(0).component3(), true)
                    .addField("Rating",  Double.toString(anime.getScore()) + " ⭐",true)
                    .build();
            event.getChannel().sendMessage((Message) embed).queue();

        } catch (Exception e) {

        }
    }

    //Finds a random future anime coming up
    private void future(MessageReceivedEvent event) {

        CompletableFuture<SeasonSearch> futureAnime = new Connector().seasonLater();

        while (!futureAnime.isDone()) {}

        try {
            ArrayList<SeasonSearchAnime> list = futureAnime.get().getAnimes();
            Random random = new Random();
           int randomInd = random.nextInt(list.size());
           SeasonSearchAnime anime = list.get(randomInd);
            int episode = anime.getEpisodeCount();
            String episodes = String.valueOf(episode);
            String genre = "";
            List<Genre> genres = anime.getGenres();
            for (Genre g : genres) {
                genre += g.component3() + " ";
            }
            MessageEmbed embed = new EmbedBuilder().setTitle(anime.getTitle(), anime.getUrl())
                    .setDescription(anime.getSynopsis())
                    .setColor(new Color(13231366))
                    .setTimestamp(OffsetDateTime.parse("2021-05-19T23:55:38.715Z"))
                    .setFooter("MAL Rewrite", "https://gamepress.gg/grandorder/sites/grandorder/files/2018-08/196_Ereshkigal_4.png")
                    .setThumbnail(anime.getImageURL())
                    .addField("Episode", episodes, false)
                    .addField("Airing", anime.getAiring_start(), false)
                    .addField("Duration", "24 Minutes per Episode", false)
                    .addField("Producer", anime.getProducers().get(0).component3(), true)
                    .build();

            event.getChannel().sendMessage((Message) embed).queue();

            } catch (Exception e) {
        }
    }

    //Prints a picture of Ereshkigal
    private void ereshkigal(MessageReceivedEvent event) {
        event.getChannel().sendMessage("https://gamepress.gg/grandorder/sites/grandorder/files/2018-08/196_Ereshkigal_4.png").queue();
    }

    //Given an argument check whether the argument is a mention for discord
    private boolean checkValidID(String[] message) {
        if (message.length > 1 && message[1].startsWith("<@!")) {
           return true;
        } else {
            return false;
        }
    }

    //Takes argument of a mentioned user. if it is not a mentioned user, then display users avatar,
    // else if ID is not valid then invoke a message, else display the mentioned user's avatar

    private void pfp(String[] message, MessageReceivedEvent event) {
        if (message.length == 1) {
            String url = event.getAuthor().getAvatarUrl();
            event.getChannel().sendMessage(url).queue();
        }
         else if (!checkValidID(message)) {
            event.getChannel().sendMessage("Invalid ID!").queue();
        }
        else {
          Member user = event.getMessage().getMentionedMembers().get(0);
           String url = user.getUser().getAvatarUrl();
            event.getChannel().sendMessage(url).queue();
        }

    }

    //Display bot commands
    private void help(MessageReceivedEvent event) {
        String message = "```General Commands: \n";
        for (String c : generalCommands) {
            message += c + "\n";
        }
        message += "\n";
        message += "bot.Anime Commands: \n";
        for (String c: animeCommands) {
            message += c +"\n";
        }
        message += "\n";
        message += "Music Commands: \n";
        for (String c : musicCommands) {
            message += c + "\n";
        }
        message += "\n";
        message += "bot.Playlist Commands: \n";
        for (String c : playlistCommands) {
            message += c + "\n";
        }
        message += "```";
        event.getChannel().sendMessage(message).queue();

}

    //Display information about bot and developer
    private void info(MessageReceivedEvent event) {
        event.getChannel().sendMessage("This bot is under development by Simon Zhou :)").queue();
    }

    //Delete up to 20 amount of messages. NOTE: Bot is unable to delete messages beyond 2 weeks old and is quite slow.
    // Improvement could be done to increase efficiency however,this function should not even be used too often.

    private void del(String[] message, MessageReceivedEvent event) { //$del @user x
        if (message.length < 2 ) {
            event.getChannel().sendMessage("Invalid Format!").queue();
        } else {
            try {
                int noDel = Integer.parseInt(message[1]);
                if (noDel > 20) {
                    event.getChannel().sendMessage("Please insert a smaller number").queue();
                    return;
                }
                List<Message> messages = event.getChannel().getHistory().retrievePast(noDel + 1).complete();
                for (Message m : messages) {
                    event.getChannel().deleteMessageById(m.getId()).queue();
                }

            } catch (Exception e) {
                event.getChannel().sendMessage("Invalid Number!").queue();
            }
        }
    }



    
}


