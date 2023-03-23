package bot;

import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private BlockingQueue<AudioTrack> queue;
    private boolean repeat = false;
    private AudioTrack currentlyPlaying = null;
    private EqualizerFactory equalizer;
    private static final float[] BASS_BOOST = {-0.05f, 0.07f, 0.16f, 0.03f, -0.05f, -0.11f};

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void queue(AudioTrack track) {
        if (!this.player.startTrack(track, true)) {
            this.queue.offer(track);
        } else {
            currentlyPlaying = track;
        }
    }

    public void nextTrack() {
        AudioTrack at = this.queue.poll();
        currentlyPlaying = at;
        this.player.startTrack(at, false);
        if (at == null) {
            currentlyPlaying = null;
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            if (repeat) {
                this.player.startTrack(track.makeClone(), false);
                return;
            }
            nextTrack();
        }
    }

    public void playTrack(AudioTrack track) {
        this.player.startTrack(track, false);
    }

    public void clearQueue() {
        queue.clear();
    }

    public AudioTrack clearQueue(int value) throws ArrayIndexOutOfBoundsException {
        int position = value - 1; // index
        if (position == 0) {
            throw new ArrayIndexOutOfBoundsException();
        }
        List<AudioTrack> track = currentQueue();
        AudioTrack song = track.get(position);
        queue.remove(song);
        return song;
    }

    public void setRepeat() {
        if (!repeat)
            repeat = true;
        else
            repeat = false;
    }

    public void setRepeat(boolean stat) {
        repeat = stat;
    }

    public boolean getRepeat() {
        return repeat;
    }

    public AudioTrack currentTrack() {
        return currentlyPlaying;
    }

    public void setCurrentlyPlaying() {
        currentlyPlaying = this.player.getPlayingTrack();
    }

    public List<AudioTrack> currentQueue() {
        List<AudioTrack> returnQueue = new ArrayList<>();
        if (currentlyPlaying != null) {
            returnQueue.add(currentlyPlaying);
        }
        for (AudioTrack s : queue)
            returnQueue.add(s);
        return returnQueue;
    }

    public void bassBoost(int percent) {
        if (equalizer == null) {
            equalizer = new EqualizerFactory();
        }
        AudioPlayer player = this.player;
        player.setFilterFactory(equalizer);
        final float multiplier = percent / 100.0f;
        for (int i = 0; i < BASS_BOOST.length; i++) {
            equalizer.setGain(i, BASS_BOOST[i] * multiplier);
        }
    }

    public void shuffle() {
        ArrayList<AudioTrack> shuffledQueue = new ArrayList<AudioTrack>(this.queue);
        Collections.shuffle(shuffledQueue);
        this.queue = new LinkedBlockingQueue<>(shuffledQueue);
    }
}
