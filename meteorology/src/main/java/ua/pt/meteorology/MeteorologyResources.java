/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.pt.meteorology;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author dn
 */
@RestController
public class MeteorologyResources {
    
    private static Map<String, Long> location2globalId;
    
    private static Map<Long, String> weatherType2description;
    
    private static Map<String, Object> localCache = new HashMap<>();

    public static Map<Long, String> getWeatherType2description() {
        return weatherType2description;
    }
    
    public static Map<String, Long> getLocation2globalId() {
        return location2globalId;
    }
    
    public void globalIdData() throws ParseException{
        location2globalId = new HashMap<>();
        final String uri = "http://api.ipma.pt/open-data/distrits-islands.json";
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(uri, String.class);
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(result);
        for (Object obj : (JSONArray) json.get("data")){
            Map<String,Object> temp = (Map<String,Object>) obj;
            location2globalId.put((String) temp.get("local"), (Long) temp.get("globalIdLocal"));
        }
    }
    
    public void weatherType() throws ParseException{
        weatherType2description = new HashMap<>();
        final String uri = "http://api.ipma.pt/open-data/weather-type-classe.json";
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(uri, String.class);
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(result);
        for (Object obj : (JSONArray) json.get("data")){
            Map<String,Object> temp = (Map<String,Object>) obj;
            weatherType2description.put((Long) temp.get("idWeatherType"), (String) temp.get("descIdWeatherTypeEN"));
        }
    }
    
    @GetMapping("get_local_data/{local}/{days}")
    public Object getLocalData(@PathVariable("local") final String name, @PathVariable("days") final int days) throws ParseException {
        //days must be in [0-4]
        if (!(0 <= days && days <= 4)){
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse("{\"Error\" : \"The number of days must be contained in [0-4]\"}");
            return json;
        }
        
        //get global id of location passed as argument
        if (location2globalId == null || weatherType2description == null){
            globalIdData();
            weatherType();
        }
        
        try {
            //cal impa api and get result in a String
            final String uri = "http://api.ipma.pt/open-data/forecast/meteorology/cities/daily/" + location2globalId.get(name) + ".json";
            RestTemplate restTemplate = new RestTemplate();
            String result = restTemplate.getForObject(uri, String.class);

            //save in local cache
            Map<String, Object> mapObj = new Gson().fromJson(result, new TypeToken<HashMap<String, Object>>() {}.getType());
            localCache.put(name, mapObj.get("data"));

            //create api's response in json
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(result);
            
            //return only the 'selected' number of days
            JSONArray jArray = new JSONArray();
            for (int i = 0; i <= days; i++){
                jArray.add(((JSONArray) json.get("data")).get(i));
            }
            
            System.out.println(weatherType2description);
            return jArray;
        }
        catch(HttpClientErrorException e){
            
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse("{\"Error\" : \"City \'" + name + "\' not found\"}");
            return json;
        }
    }
    
}
