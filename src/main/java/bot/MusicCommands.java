package bot;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.*;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MusicCommands {
    private static final String PREFIX = "-";

    public MusicCommands() {

    }

    public void bassBoost(String[] message, MessageReceivedEvent event) {
        int percent = Integer.parseInt(message[1]);
        if (percent > 1000) {
            event.getChannel().sendMessage("Value must be below 1000").queue();
        } else {
            PlayerManager.getInstance().getMusicManager(event.getGuild()).trackScheduler.bassBoost(percent);
            event.getChannel().sendMessage("Bass is boosted by: " + percent + "%").queue();
        }
    }

    public void seek(String[] message, MessageReceivedEvent event) {
        if (!event.getMember().getVoiceState().getChannel().equals(event.getGuild().getSelfMember().getVoiceState().getChannel())) {
            event.getChannel().sendMessage("Must be in the same voice channel!");
            return;
        }
        long totalTime;
        if (message.length == 1) {
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
            } else {
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

    public void queue(MessageReceivedEvent event) {
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
    public void now(MessageReceivedEvent event) {
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
            } else
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


    private String convertTime(Long time) {
        Long hour = TimeUnit.MILLISECONDS.toHours(time);
        Long minute = TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(hour);
        Long second = TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(minute) - TimeUnit.HOURS.toSeconds(hour);
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
    public void leave(MessageReceivedEvent event) {
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
    public void repeat(MessageReceivedEvent event) {
        if (!event.getMember().getVoiceState().getChannel().equals(event.getGuild().getSelfMember().getVoiceState().getChannel())) {
            event.getChannel().sendMessage("Must be in the same voice channel!").queue();
            return;
        }
        PlayerManager.getInstance().getMusicManager(event.getGuild()).trackScheduler.setRepeat();
        Boolean repeatStat = PlayerManager.getInstance().getMusicManager(event.getGuild()).trackScheduler.getRepeat();
        if (repeatStat)
            event.getChannel().sendMessage("Repeat " + ":regional_indicator_o:" + ":regional_indicator_n: " + "<a:guraone:812015049979199549>").queue();
        else
            event.getChannel().sendMessage("Repeat " + ":regional_indicator_o:" + ":regional_indicator_f:" + ":regional_indicator_f:")
                    .queue();
    }

    //Skip the current song and play the next song
    public void skip(MessageReceivedEvent event) {
        if (!event.getMember().getVoiceState().getChannel().equals(event.getGuild().getSelfMember().getVoiceState().getChannel())) {
            event.getChannel().sendMessage("Must be in the same voice channel!").queue();
            return;
        }
        TrackScheduler ts = PlayerManager.getInstance()
                .getMusicManager(event.getGuild())
                .trackScheduler;
        if (ts.currentTrack() == null) {
            event.getChannel().sendMessage("No songs are currently being played").queue();
        } else {
            ts.nextTrack();
            event.getChannel().sendMessage("Skipping ... ").queue();
        }
    }

    //Clear the Queue
    public void clear(String[] message, MessageReceivedEvent event) {
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
            } catch (Exception e) {
                event.getChannel().sendMessage("Song is currently playing! use " + PREFIX + "skip").queue();
            }
        }
    }

    // Using Lavaplayer API, Play takes an argument and checks whether if it is a keyword or a URL.
    // If it is a keyword, use API to create a youtube search and call handle, else call handle
    public void play(String[] message, MessageReceivedEvent event) {
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
        handle(channel, self, user, link, event);
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
    public void handle(TextChannel channel, Member selfUser, Member user, String url, MessageReceivedEvent event) {

        final Member member = user;
        final GuildVoiceState memberVoiceState = member.getVoiceState();
        if (!memberVoiceState.inVoiceChannel()) {
            channel.sendMessage("You need to be in a voice channel!").queue();
            return;
        }
        if (!event.getGuild().getAudioManager().isConnected())
            join(event);
        PlayerManager.getInstance().loadAndPlay(channel, url);
    }

    //make bot join the same voice channel as user. If not in the same voice channel then invoke a message
    public void join(MessageReceivedEvent event) {
        VoiceChannel channel = event.getMember().getVoiceState().getChannel();
        if (channel == null) {
            event.getChannel().sendMessage("You are not in a voice channel!").queue();
            return;
        }
        AudioManager audioManager = event.getGuild().getAudioManager();
        audioManager.openAudioConnection(channel);
    }
}
