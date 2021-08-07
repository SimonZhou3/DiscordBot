import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class Main {

    public static JDABuilder jda;
    public static String discordToken = "ODM5OTgwNzQyNzU0NTY2MTU1.YJRjBw.NidptbTlUO4oPMI2onlOedN-ePU";


    public static void main (String args[]) throws Exception {
           // Activates discord Bot
            jda = JDABuilder.createDefault(discordToken);
            jda.setActivity(Activity.playing("-help"));
            JDA bot;
            bot = jda.build();
            bot.addEventListener(new Listener());
            bot.addEventListener(new CommandMap());
        }
    }

