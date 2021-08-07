import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WriteJson {

    public List<String> writeJson(String playlistName, String url) throws IOException, ParseException {
        List<String> links = new ArrayList<>();
        JSONArray jsonArray;
        JSONObject playlist = new JSONObject();
        playlist.put("link", url);
        try {
            jsonArray = new ReadJson().readReturnJSONData(playlistName);
        } catch (Exception e) {
            jsonArray = new JSONArray();
        }
        jsonArray.add(playlist);
        JSONObject playlistObject = new JSONObject();
        playlistObject.put("playlist",jsonArray);
        for (int i = 0 ; i < jsonArray.size();i++) {
            JSONObject temp = (JSONObject) jsonArray.get(i);
            links.add((String)temp.get("link"));
        }

        try {
            FileWriter file = new FileWriter("./src/data/" + playlistName + ".json");
            file.write(playlistObject.toJSONString());
            file.flush();
        } catch (Exception e) {
            System.out.println("An exception has occurred");
        }
        return links;
    }



}
