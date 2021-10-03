import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReadJson  {

    public JSONArray readReturnJSONData(String playlistName) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        FileReader reader = new FileReader("./src/data/" + playlistName + ".json");

       Object obj =  jsonParser.parse(reader);
        JSONArray jsonArray = (JSONArray) obj;

        if (jsonArray.isEmpty()) {
            throw new ParseException(-1);
        }

        return jsonArray;
    }


    public List<String> readJSONData (String playlistName,String key) throws IOException, ParseException {
        List<String> savedPlaylist = new ArrayList<>();
        JSONParser jsonParser = new JSONParser();
        FileReader reader = new FileReader("./src/data/" + playlistName + ".json");

        Object obj =  jsonParser.parse(reader);
        JSONArray jsonArray = (JSONArray) obj;
        if (jsonArray.isEmpty() )
            throw new ParseException(-1);

        for (int i = 0 ; i < jsonArray.size(); i++) {
            JSONObject temp = (JSONObject) jsonArray.get(i);
            String link = (String) temp.get(key);
            System.out.println(link);
            savedPlaylist.add(i,link);
        }

        return savedPlaylist;
    }

}
