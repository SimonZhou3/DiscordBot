package bot;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WriteJson {

    public List<String> writeJson(String playlistName, String url, String key) throws IOException, ParseException {
        File tempFile = new File("./src/data/" + playlistName + ".json");
        if (!tempFile.exists() && !tempFile.isDirectory()) { //if file does not exist or is a directory, then throw input exception
            throw new IOException();
        }
        List<String> links = new ArrayList<>();
        JSONArray jsonArray;
        JSONObject playlist = new JSONObject();
        playlist.put(key, url);
        try {
            jsonArray = new ReadJson().readReturnJSONData(playlistName);
        } catch (Exception e) {
            jsonArray = new JSONArray();
        }
        jsonArray.add(playlist);
        for (int i = 0 ; i < jsonArray.size();i++) {
            JSONObject temp = (JSONObject) jsonArray.get(i);
            links.add((String)temp.get(key));
        }

        try {
            FileWriter file = new FileWriter("./src/data/" + playlistName + ".json");
            file.write(jsonArray.toJSONString());
            file.flush();
        } catch (Exception e) {
            System.out.println("An exception has occurred");
        }
        return links;
    }

    public List<String> writeMultipleJson(String playlistName, String url,String description, String key, String secondKey) throws IOException, ParseException {
        File tempFile = new File("./src/data/playlist" + playlistName + ".json");
        if (!tempFile.exists() && !tempFile.isDirectory()) { //if file does not exist or is a directory, then throw input exception
            throw new IOException();
        }
        List<String> links = new ArrayList<>();
        JSONArray jsonArray;
        JSONObject playlist = new JSONObject();
        playlist.put(key, url);
        playlist.put(secondKey,description);
        try {
            jsonArray = new ReadJson().readReturnJSONData(playlistName);
        } catch (Exception e) {
            jsonArray = new JSONArray();
        }
        jsonArray.add(playlist);
        for (int i = 0 ; i < jsonArray.size();i++) {
            JSONObject temp = (JSONObject) jsonArray.get(i);
            links.add((String)temp.get(key));
            links.add((String)temp.get(secondKey));
        }

        try {
            FileWriter file = new FileWriter("./src/data/" + playlistName + ".json");
            file.write(jsonArray.toJSONString());
            file.flush();
        } catch (Exception e) {
            System.out.println("An exception has occurred");
        }
        return links;
    }



}
