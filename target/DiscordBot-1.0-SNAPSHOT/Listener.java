import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Listener extends ListenerAdapter {
    private CommandMap commands = new CommandMap();
    public static String PREFIX = "-";

        public void onMessageReceived (MessageReceivedEvent event) {
            String[] messageReceived = event.getMessage().getContentRaw().split(" ");
            for (String s : messageReceived) {
                System.out.print(s + " "); //prints out the message
            }
        System.out.println();
            if (messageReceived[0].startsWith(PREFIX)) { //if the message is a command then ....
               Member user = event.getMember();
               try {
                   System.out.println("User: " + user.getEffectiveName());
               } catch (Exception e) {
               }

               commands.get(messageReceived, event);
            }
        }
        }


