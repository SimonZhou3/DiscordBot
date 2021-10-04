package bot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerManager {

    private static PlayerManager INSTANCE;

    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;


    public PlayerManager() {
        musicManagers = new HashMap<>();
        audioPlayerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        return musicManagers.computeIfAbsent(guild.getIdLong(),(guildID) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getHandler());
            return guildMusicManager;
        });
    }


    public void loadAndPlay (TextChannel textChannel, String url) {
        final GuildMusicManager musicManager = getMusicManager(textChannel.getGuild());
        audioPlayerManager.loadItemOrdered(musicManager, url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                musicManager.trackScheduler.queue(audioTrack);
                musicManager.trackScheduler.setCurrentlyPlaying();
                textChannel.sendMessage("` Adding to Queue: ")
                        .append(audioTrack.getInfo().title)
                        .append(" by ")
                        .append(audioTrack.getInfo().author + "`").queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                if (audioPlaylist.isSearchResult()) {
                    AudioTrack track = audioPlaylist.getTracks().get(0);
                    musicManager.trackScheduler.queue(track);
                    textChannel.sendMessage("` Adding to Queue: ")
                            .append(track.getInfo().title)
                            .append(" by ")
                            .append(track.getInfo().author + "`").queue();
                   return;
                } else {
                    List<AudioTrack> playlist = audioPlaylist.getTracks();
                    for (AudioTrack at : playlist)
                        musicManager.trackScheduler.queue(at);
                    textChannel.sendMessage("` Adding to Queue: ")
                            .append(String.valueOf(+playlist.size()))
                            .append(" songs from bot.Playlist ")
                            .append(audioPlaylist.getName() + "`")
                            .queue();
                }
            }

            @Override
            public void noMatches() {
                System.out.println("This will print because it has no matches!");
            }

            @Override
            public void loadFailed(FriendlyException e) {

            }
        });

    }

    public static PlayerManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }

        return INSTANCE;
    }
}
