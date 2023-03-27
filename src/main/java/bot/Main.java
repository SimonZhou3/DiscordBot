package bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;


public class Main {


    public static JDABuilder jda;

    public static void main (String args[]) throws Exception {

           // Activates discord Bot using secure dotenv to keep API key safe

            jda = JDABuilder.createDefault(System.getenv("MY_ENV_APIKEY"));
//            jda = JDABuilder.createDefault(Dotenv.load().get("MY_ENV_APIKEY"));
//        jda = JDABuilder.createDefault(Dotenv.load().get("TEST_ENV_APIKEY"));
            jda.setActivity(Activity.streaming(" -help","https://www.twitch.tv/gumsf"));
            jda.enableIntents(GatewayIntent.MESSAGE_CONTENT);
            JDA bot;
            bot = jda.build();
            bot.addEventListener(new Listener());

        }
    }

