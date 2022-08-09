package bot;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    REMOVE,
    LIST
}

public class CommandMap extends ListenerAdapter {
    private ArrayList<String> generalCommands = new ArrayList<>();
    private ArrayList<String> animeCommands = new ArrayList<>();
    private ArrayList<String> musicCommands = new ArrayList<>();
    private ArrayList<String> playlistCommands = new ArrayList<>();

    private static final String PREFIX = "-";
    private static final int SIZEIMAGE = 31;
    private MongoClient client = MongoClients.create(System.getenv("MONGO2"));
//    private MongoClient client = MongoClients.create(Dotenv.load().get("MONGO2"));

    private MongoDatabase db = client.getDatabase("playlist");
    private MongoDatabase dbAnime = client.getDatabase("anime");
    private MongoDatabase dbHyoon = client.getDatabase("photos");

    private final MusicCommands musicHandler = new MusicCommands();
    private final PlaylistCommands playlistHandler = new PlaylistCommands(db);
    private final AnimeCommands animeHandler = new AnimeCommands();

    //Creates a fixated command list for the bot to use
    public CommandMap() {
        generalCommands.add(String.valueOf(PREFIX + command.HELP).toLowerCase() + " -- Shows List of commands");
        generalCommands.add(String.valueOf(PREFIX + command.INFO).toLowerCase() + " -- Information about me");
        generalCommands.add(String.valueOf(PREFIX + command.PFP).toLowerCase() + " @[user] -- Grab Profile Picture of mentioned user");
        generalCommands.add(String.valueOf(PREFIX + command.DEL).toLowerCase() + " -- deletes the specified amount of messages");
        generalCommands.add(String.valueOf(PREFIX + command.ERESHKIGAL).toLowerCase() + " -- ;)");
        animeCommands.add(String.valueOf(PREFIX + command.SEARCH).toLowerCase() + " -- Search for an anime by name");
        musicCommands.add(String.valueOf(PREFIX + command.JOIN).toLowerCase() + " -- Add bot to channel");
        musicCommands.add(String.valueOf(PREFIX + command.LEAVE).toLowerCase() + " -- Remove bot from channel");
        musicCommands.add(String.valueOf(PREFIX + command.PLAY).toLowerCase() + " [URL/Keyword] -- adds the music into the queue ");
        musicCommands.add(String.valueOf(PREFIX + command.SKIP).toLowerCase() + " -- Skips current music playing and plays next in the queue");
        musicCommands.add(String.valueOf(PREFIX + command.CLEAR).toLowerCase() + " -- Clears the current music queue");
        musicCommands.add(String.valueOf(PREFIX + command.QUEUE).toLowerCase() + " -- Prints all the songs in the queue");
        musicCommands.add(String.valueOf(PREFIX + command.NOW).toLowerCase() + " -- Prints the information of the currently playing song");
        musicCommands.add(String.valueOf(PREFIX + command.REPEAT).toLowerCase() + " -- Repeats the current song");
        musicCommands.add(String.valueOf(PREFIX + command.SEEK).toLowerCase() + " [seconds] -- seeks to a specific place in the song");
        playlistCommands.add(String.valueOf(PREFIX + command.CREATE).toLowerCase() + "[name] [private/public] -- Creates an instance of a playlist to add songs");
        playlistCommands.add(String.valueOf(PREFIX + command.SAVE).toLowerCase() + "[name] [url] -- saves the link of a audio source to the specified playlist");
        playlistCommands.add(String.valueOf(PREFIX + command.READ).toLowerCase() + "[name] -- Reads the instance of the playlist");
        playlistCommands.add(String.valueOf(PREFIX + command.PLAYLIST).toLowerCase() + "[name] -- Queues all songs in the playlist");
        playlistCommands.add(String.valueOf(PREFIX + command.RENAME).toLowerCase() + "[old playlist] [new playlist] -- Renames an old playlist to a new playlist");
        playlistCommands.add(String.valueOf(PREFIX + command.REMOVE).toLowerCase() + "[playlist] [position] -- Removes a particular audio source from the playlist");
        playlistCommands.add(String.valueOf(PREFIX + command.LIST).toLowerCase() + "-- Lists all publicly owned and personal playlists");

    }

    // Detecting whether user initiated a command
    public void get(String[] command, MessageReceivedEvent event) {
        switch (command[0].toLowerCase()) {
            case PREFIX + "help":
                help(event);
                break;
            case PREFIX + "info":
                info(event);
                break;
            case PREFIX + "pfp":
                pfp(command, event);
                break;
            case PREFIX + "del":
                del(command, event);
                break;
            case PREFIX + "ereshkigal":
                ereshkigal(event);
                break;
            case PREFIX + "search":
                animeHandler.searchAnimeByName(command, event);
                break;
            case PREFIX + "top":
                animeHandler.getTopSeasonalAnime(event);
            case PREFIX + "join":
                musicHandler.join(event);
                break;
            case PREFIX + "play":
            case PREFIX + "p":
                musicHandler.play(command, event);
                break;
            case PREFIX + "clear":
                musicHandler.clear(command, event);
                break;
            case PREFIX + "s":
            case PREFIX + "skip":
                musicHandler.skip(event);
                break;
            case PREFIX + "repeat":
                musicHandler.repeat(event);
                break;
            case PREFIX + "leave":
                musicHandler.leave(event);
                break;
            case PREFIX + "now":
                musicHandler.now(event);
                break;
            case PREFIX + "queue":
                musicHandler.queue(event);
                break;
            case PREFIX + "seek":
                musicHandler.seek(command, event);
                break;
            case PREFIX + "supremacy":
                hyoon(event);
                break;
            case PREFIX + "boost":
                musicHandler.bassBoost(command, event);
                break;
            case PREFIX + "read":
                playlistHandler.readPlaylist(command, event);
                break;
            case PREFIX + "save":
                playlistHandler.savePlaylist(command, event);
                break;
            case PREFIX + "create":
                playlistHandler.createPlaylist(command, event);
                break;
            case PREFIX + "playlist":
                playlistHandler.playPlaylist(command, event);
                break;
            case PREFIX + "rename":
                playlistHandler.renamePlaylist(command, event);
                break;
            case PREFIX + "remove":
                playlistHandler.removePlaylist(command, event);
                break;
            case PREFIX + "list":
                playlistHandler.listAllPlaylist(event);
                break;
            default:
                System.out.println("Enter default");
                break;
        }
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

    //Prints a picture of Ereshkigal
    private void ereshkigal(MessageReceivedEvent event) {
        event.getChannel().sendMessage("https://gamepress.gg/grandorder/sites/grandorder/files/2018-08/196_Ereshkigal_4.png").queue();
    }

    //Given an argument check whether the argument is a mention for discord
    private boolean checkValidID(String[] message) {
        return message.length > 1 && message[1].startsWith("<@!");
    }

    //Takes argument of a mentioned user. if it is not a mentioned user, then display users avatar,
    // else if ID is not valid then invoke a message, else display the mentioned user's avatar

    private void pfp(String[] message, MessageReceivedEvent event) {
        if (message.length == 1) {
            String url = event.getAuthor().getAvatarUrl();
            event.getChannel().sendMessage(url).queue();
        } else if (!checkValidID(message)) {
            event.getChannel().sendMessage("Invalid ID!").queue();
        } else {
            Member user = event.getMessage().getMentionedMembers().get(0);
            String url = user.getUser().getAvatarUrl();
            event.getChannel().sendMessage(url).queue();
        }

    }

    //Display bot commands
    private void help(MessageReceivedEvent event) {
        StringBuilder message = new StringBuilder("```General Commands: \n");
        for (String c : generalCommands) {
            message.append(c).append("\n");
        }
        message.append("\n");
        message.append("Anime Commands: \n");
        for (String c : animeCommands) {
            message.append(c).append("\n");
        }
        message.append("\n");
        message.append("Music Commands: \n");
        for (String c : musicCommands) {
            message.append(c).append("\n");
        }
        message.append("\n");
        message.append("Playlist Commands: \n");
        for (String c : playlistCommands) {
            message.append(c).append("\n");
        }
        message.append("```");
        event.getChannel().sendMessage(message.toString()).queue();

    }

    // Display information about bot and developer
    private void info(MessageReceivedEvent event) {
        event.getChannel().sendMessage("This bot is under development by Simon Zhou :)").queue();
    }

    // Delete up to 20 amount of messages. NOTE: Bot is unable to delete messages beyond 2 weeks old and is quite slow.
    // Improvement could be done to increase efficiency however,this function should not even be used too often.

    private void del(String[] message, MessageReceivedEvent event) { //$del @user x
        if (message.length < 2) {
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


