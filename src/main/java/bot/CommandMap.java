package bot;

import com.github.doomsdayrs.jikan4java.core.Connector;
import com.github.doomsdayrs.jikan4java.data.enums.Season;
import com.github.doomsdayrs.jikan4java.data.model.main.manga.Manga;
import com.github.doomsdayrs.jikan4java.data.model.main.season.SeasonSearch;
import com.github.doomsdayrs.jikan4java.data.model.main.season.SeasonSearchAnime;
import com.github.doomsdayrs.jikan4java.data.model.support.basic.meta.Genre;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import okhttp3.ResponseBody;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.*;
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
    SAVE
}

public class CommandMap  extends ListenerAdapter {
    private ArrayList<String> generalCommands = new ArrayList<>();
    private ArrayList<String> animeCommands = new ArrayList<>();
    private ArrayList<String> musicCommands = new ArrayList<>();
    private ArrayList<String> playlistCommands = new ArrayList<>();
    private List<String> page = new ArrayList<>();
    private int totalPage;
    private String messages;
    
    private int pageNumber = 0;
    private static final String PREFIX = "-";
    private static final int SIZEIMAGE = 22;


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
    }

    //Detecting whether user initiated a bot.command
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
                queueTemp(event);
                break;
            case PREFIX + "seek":
                seek(command, event);
                break;
            case PREFIX + "joe":
                joe(event);
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
            case PREFIX + "describe":
                describePlaylist(command,event);
                break;
            default:
                System.out.println("Enter default");
                break;
        }
    }

    private void describePlaylist(String[] command, MessageReceivedEvent event) {
    }

    private void playPlaylist(String[] message, MessageReceivedEvent event) {
        String playlistName = getArg(message);
        ReadJson reader = new ReadJson();
        TextChannel channel = event.getTextChannel();
        Member self = event.getGuild().getSelfMember();
        Member user = event.getMember();
        String link ="";
            try {
               List<String> playlist = reader.readJSONData(playlistName,"link");
                Collections.shuffle(playlist);
               for (int i = 0; i < playlist.size();i++) {
                   link = playlist.get(i);
                   if (isUrl(playlist.get(i))) {
                      link = link.replaceAll("\\s","");
                   }
                   handle(channel,self,user,link,event);

               }
            } catch (IOException e) {
                event.getChannel().sendMessage("No File was Found").queue();
            } catch (ParseException e) {
                event.getChannel().sendMessage("Unable to read File").queue();
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
            event.getChannel().sendMessage("bot.Playlist name must be one word only!").queue();
            return;
        } else {
            String playlistName = message[1];
            File tempFile = new File("./src/data/" + playlistName + ".json");
            if (!tempFile.exists() && !tempFile.isDirectory()) { //if file does not exist or is a directory, then throw input exception
                WriteJson writer = new WriteJson();
                try {
                    tempFile.createNewFile();
                    event.getChannel().sendMessage("Playlist " +playlistName + " has been created").queue();
                    writer.writeMultipleJson("ListOfPlaylist",playlistName,"","name","description");

                } catch (IOException e) {
                    event.getChannel().sendMessage("Playlist " + playlistName + " already exists!").queue();
                } catch (ParseException e) {
                    //stub
                }
            } else {
                event.getChannel().sendMessage("Playlist " + playlistName + " already exists!").queue();
            }
        }
        return;
}

    private void savePlaylist(String[] message, MessageReceivedEvent event) {
        String jsonName = message[1];
        String url = "";
        for (int i = 2; i < message.length; i ++) {
            url += message[i];
            if (i + 1 < message.length) {
                url += " ";
            }
        }
        if (!isUrl(url))
            url = "ytsearch:" + url;
        String formatter = "";
        WriteJson writer = new WriteJson();
        try {
          List<String> playlist = writer.writeJson(jsonName,url,"link");
            for (int i = 0; i < playlist.size(); i++) {
                formatter += playlist.get(i) +"\n";
            }
            event.getChannel().sendMessage("Saved to playlist " + jsonName).queue();
        } catch (IOException e) {
            event.getChannel().sendMessage("JSON file does not exist").queue();
        } catch (ParseException e) {
            event.getChannel().sendMessage("Error in reading json file").queue();
        }
    }

    private void readPlaylist(String[] message, MessageReceivedEvent event) {
        int size = 0;
        String playlistName = getArg(message);
        ReadJson reader = new ReadJson();
        String formatter = "";

        try {
            List<String> playlist = reader.readJSONData(playlistName, "link");
            size = playlist.size();
            for (int i = 0; i < playlist.size(); i++) {
                int position = i + 1;
                formatter += position +". " + playlist.get(i) +"\n";
            }


            event.getChannel().sendMessage(formatter).queue();
        } catch (IOException e) {
            event.getChannel().sendMessage("Error in json file").queue();
        } catch (ParseException e) {
            event.getChannel().sendMessage("Error in reading json file").queue();
        }
        catch (IllegalArgumentException e) {
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
        long totalTime;
        if (message.length == 1 ) {
            event.getChannel().sendMessage("Please include a time to seek to").queue();
            return;
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
        TrackScheduler ts = PlayerManager.getInstance().getMusicManager(event.getGuild()).trackScheduler;
        if (ts.currentTrack() == null) {
            event.getChannel().sendMessage("No song is currently playing").queue();
        }
        ts.currentTrack().setPosition(totalTime);
        event.getChannel().sendMessage("Seeking... <a:guraone:812015049979199549>").queue();
    }

    private void hyoon(MessageReceivedEvent event) {
        Random random = new Random();
        int randomInd = random.nextInt(SIZEIMAGE);
        System.out.println(randomInd);
        String path = "./src/data/Hyoon"+ String.valueOf(randomInd)+".png";
        File file = new File(path);
        event.getChannel().sendFile(file).queue();
    }

    private void joe(MessageReceivedEvent event) {
        event.getChannel().sendMessage("mama").queue();
    }


    private void queueTemp(MessageReceivedEvent event) {
        TrackScheduler ts = PlayerManager.getInstance().getMusicManager(event.getGuild()).trackScheduler;
        List<AudioTrack> currentQueue = ts.currentQueue();
        List<String> list = new ArrayList<>();
        MessageEmbed messageEmbed;

        if (currentQueue.size() == 0) {
            messageEmbed = new EmbedBuilder()
                    .setDescription("No Songs in the queue")
                    .build();
            event.getChannel().sendMessage(messageEmbed).queue();
            return;
        } else {
            String compileList = "";
            int position = 1;

            for (AudioTrack at : currentQueue) {
                String titleFormat = String.valueOf(position) + ". " + at.getInfo().title + "\n";
                if ((compileList.length() + titleFormat.length() < 2000)) {
                    compileList += titleFormat;
                    position++;
                } else {
                    System.out.println(compileList);
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


    //Prints the  queue of songs
    private void queue(MessageReceivedEvent event) {
        TrackScheduler ts = PlayerManager.getInstance().getMusicManager(event.getGuild()).trackScheduler;
        List<AudioTrack> currentQueue = ts.currentQueue();
        List<String> list = new ArrayList<>();
        MessageEmbed messageEmbed;
        pageNumber = 0;
        if (currentQueue.size() == 0) {
            messageEmbed = new EmbedBuilder()
                    .setDescription("No Songs in the queue")
                    .build();
            event.getChannel().sendMessage(messageEmbed).queue();
            return;
        } else {
            String compileList = "";
            int position = 1;

            for (AudioTrack at : currentQueue) {
                String titleFormat = String.valueOf(position) + ". " + at.getInfo().title + "\n";
                if ((compileList.length() + titleFormat.length() < 2000)) {
                    compileList += titleFormat;
                    position++;
                } else {
                    System.out.println(compileList);
                    list.add(compileList);
                    compileList = titleFormat;
                }
            }
            if (list.isEmpty()) {
                list.add(compileList);
            }

            System.out.println("After completing the page book the size of the book is : " + this.page.size());
            messageEmbed = new EmbedBuilder()
                    .setTitle("  ``` List of Songs  ```")
                    .setDescription("```" + list.get(0) + "```")
                    .setColor(new Color(4818551))
                    .setTimestamp(OffsetDateTime.now())
                    .setFooter("Youtube Analytics", "https://gamepress.gg/grandorder/sites/grandorder/files/2018-08/196_Ereshkigal_4.png")
                    .build();
            event.getChannel().sendMessage(messageEmbed).queue(message -> {
                messages = message.getId();
                message.addReaction("➡").submit();

            });

            }
}




    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (!event.getUser().isBot()) {
            addHelper(event);
        }
}

    private void addHelper(MessageReactionAddEvent event) {

        if (event.getReactionEmote().getName().equals("➡") ) {
            System.out.println("we should enter here when we clicked it...");
            this.pageNumber++;
        }
        else if (event.getReactionEmote().getName().equals("⬅"))
            this.pageNumber--;

        event.getChannel().retrieveMessageById(event.getMessageId()).queue(message -> {
            for (MessageReaction r: message.getReactions()) {
                r.removeReaction(event.getUser()).submit();
                r.removeReaction().submit();
            }
        });

        TrackScheduler ts = PlayerManager.getInstance().getMusicManager(event.getGuild()).trackScheduler;
        List<AudioTrack> currentQueue = ts.currentQueue();
        String compileList = "";
        int position = 1;
        List<String> list = new ArrayList<>();

        for (AudioTrack at : currentQueue) {
            String titleFormat = String.valueOf(position) + ". " + at.getInfo().title + "\n";
            if ((compileList.length() + titleFormat.length() < 2000)) {
                compileList += titleFormat;
                position++;
            } else {
                System.out.println(compileList);
                list.add(compileList);
                compileList = titleFormat;
            }
        }
        event.getChannel().retrieveMessageById(event.getMessageId()).queue(message -> {
            if (pageNumber == 0) {
                message.addReaction("➡").queue();
            }
            else if (pageNumber > 0 && pageNumber < list.size() - 1 ) {
                message.addReaction("⬅").queue();
                message.addReaction("➡").queue();
            } else {
                message.addReaction("⬅").queue();
            }
            System.out.println("size of list: " + list.size());

            if (list.isEmpty()) {
                MessageEmbed messageEmbed = new EmbedBuilder()
                        .setDescription("No Songs in the queue")
                        .build();
                message. editMessage(messageEmbed).queue();
                return;
            }
            MessageEmbed messageEmbed = new EmbedBuilder()
                    .setTitle("  ``` List of Songs  ```")
                    .setDescription("```" + list.get(pageNumber) + "```")
                    .setColor(new Color(4818551))
                    .setTimestamp(OffsetDateTime.now())
                    .setFooter("Youtube Analytics", "https://gamepress.gg/grandorder/sites/grandorder/files/2018-08/196_Ereshkigal_4.png")
                    .build();
            message.editMessage(messageEmbed).queue();
        });
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
    private void leave (MessageReceivedEvent event) {
        if (!event.getMember().getVoiceState().getChannel().equals(event.getGuild().getSelfMember().getVoiceState().getChannel())) {
            event.getChannel().sendMessage("Must be in the same voice channel!");
            return;
        }
        PlayerManager.getInstance()
                .getMusicManager(event.getGuild())
                .trackScheduler.clearQueue();
        PlayerManager.getInstance()
                .getMusicManager(event.getGuild())
                .trackScheduler.setRepeat(false);
        PlayerManager.getInstance().getMusicManager(event.getGuild()).trackScheduler.setCurrentlyPlaying();
        AudioManager audioManager = event.getGuild().getAudioManager();
        audioManager.closeAudioConnection();
    }

    //Repeat the currently playing song
    private void repeat (MessageReceivedEvent event) {
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
            String[] findID = request.split("[{}]"); //split each category for each of its own anime
            String[] split = findID[2].split(","); //split each of its anime into different type
            split = split[0].split(":"); //split type into values
            if (split[1].isEmpty()) { //check to see if id is a real number
                event.getChannel().sendMessage("There exist no anime with that name");
            }
            int id = Integer.parseInt(split[1]); //split [1] will always contain the id of the anime
           String dataAnimeParse = new Connector().request("https://api.jikan.moe/v3/anime/" + id).string();

              PrintWriter out = new PrintWriter("./src/data/anime.json");
              out.println(dataAnimeParse);
              out.close();
            FileReader reader = new FileReader("./src/data/anime.json");
            JSONParser jsonParser = new JSONParser();
            Object obj =  jsonParser.parse(reader);
            JSONObject jsonObject = (JSONObject) obj;
            String title = getParse(jsonObject,"title");
            String link = getParse(jsonObject,"url");
            String imageURL = getParse(jsonObject,"image_url");
            String episodes = getParse(jsonObject,"episodes");
            String status = getParse(jsonObject,"status");
            String type = getParse(jsonObject, "type");
            String score = getParse(jsonObject,"score");
            String duration = getParse(jsonObject,"duration");
            JSONArray jsonArray = (JSONArray) jsonObject.get("producers");
            JSONObject arrayObject = (JSONObject) jsonArray.get(0);
            String producer = (String) arrayObject.get("name");
            String synopsis = getParse(jsonObject,"synopsis");


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
            //
        }
    }
    private String getParse (JSONObject jsonObject,String key) {
        return jsonObject.get(key).toString();
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
            event.getChannel().sendMessage(embed).queue();

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
            event.getChannel().sendMessage(embed).queue();

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

            event.getChannel().sendMessage(embed).queue();

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


