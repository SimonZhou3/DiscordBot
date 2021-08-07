import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class GuildMusicManager {
    public final AudioPlayer audioPlayer;
    public final TrackScheduler trackScheduler;
    private final AudioPlayerHandler handler;

    public GuildMusicManager (AudioPlayerManager manager) {
        this.audioPlayer = manager.createPlayer();
        trackScheduler = new TrackScheduler(audioPlayer);
        audioPlayer.addListener(trackScheduler);
        handler = new AudioPlayerHandler(audioPlayer);
    }

    public AudioPlayerHandler getHandler () {
        return handler;
    }
}
