import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class Main {

    public static JDABuilder jda;

    public static void main (String args[]) throws Exception {
           // Activates discord Bot
            Dotenv dotenv = Dotenv.load();

            jda = JDABuilder.createDefault(dotenv.get("MY_ENV_APIKEY"));
            jda.setActivity(Activity.playing("-help"));
            JDA bot;
            bot = jda.build();
            bot.addEventListener(new Listener());
            bot.addEventListener(new CommandMap());
        }
    }

