package bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;


public class Main {



    public static JDABuilder jda;


    public static void main (String args[]) throws Exception {

           // Activates discord Bot using secure dotenv to keep API key safe
//            Dotenv dotenv = Dotenv.load();
            jda = JDABuilder.createDefault(System.getenv("MY_ENV_APIKEY"));
//            jda = JDABuilder.createDefault(dotenv.get("MY_ENV_APIKEY"));
            jda.setActivity(Activity.streaming(" -help","https://www.twitch.tv/gumsf"));
            JDA bot;
            bot = jda.build();
            bot.addEventListener(new Listener());
//            bot.addEventListener(new bot.CommandMap());

        }
    }

