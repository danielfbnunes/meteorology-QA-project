package ua.pt.meteorology;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@CrossOrigin(origins = "*")
public class MeteorologyResources {
    
    static final Logger logger = LoggerFactory.getLogger(MeteorologyResources.class);
    
    private static int numberOfRequests = 0;
    private static int hits = 0;
    private static int misses = 0;
    
    private static Map<String, Long> location2globalId;
    
    private static Map<Long, String> weatherType2description;
    
    private static Map<Long, String> windType2description;
    
    public static Map<Long, String> getWeatherType2description() {
        return weatherType2description;
    }
    
    public static Map<String, Long> getLocation2globalId() {
        return location2globalId;
    }
    
    public static void insertNewLocation(Map<String, Object> temp){
        try {
            numberOfRequests++;
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getForObject("http://api.ipma.pt/open-data/forecast/meteorology/cities/daily/" + (Long) temp.get("globalIdLocal") + ".json", String.class);
            location2globalId.put((String) temp.get("local"), (Long) temp.get("globalIdLocal"));
            hits++;
        }catch(HttpClientErrorException e){
            logger.info("Can't request from this city: {}", (String) temp.get("local"));
            misses++;
        }
    }
    
    public static void globalIdData() throws ParseException{
        location2globalId = new HashMap<>();
        try{
            numberOfRequests++;
            final String uri = "http://api.ipma.pt/open-data/distrits-islands.json";
            RestTemplate restTemplate = new RestTemplate();
            String result = restTemplate.getForObject(uri, String.class);
            hits++;
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(result);
            for (Object obj : (JSONArray) json.get("data")){
                Map<String,Object> temp = (Map<String,Object>) obj;
                insertNewLocation(temp);
            }
        }catch(HttpClientErrorException e){
            misses++;
        }
    }
    
    public static JSONObject getJsonFromApi(String uri) throws ParseException{
        numberOfRequests++;
        try{
            RestTemplate restTemplate = new RestTemplate();
            String result = restTemplate.getForObject(uri, String.class);
            hits++;
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(result);
        }catch(HttpClientErrorException e){
            misses++;
            return null;
        }
    }
    
    public static void windType() throws ParseException{
        windType2description = new HashMap<>();
        JSONObject json = getJsonFromApi("http://api.ipma.pt/open-data/wind-speed-daily-classe.json");
        if (json != null){
            for (Object obj : (JSONArray) json.get("data")){
                Map<String,Object> temp = (Map<String,Object>) obj;
                windType2description.put(Long.parseLong((String) temp.get("classWindSpeed")), (String) temp.get("descClassWindSpeedDailyEN"));
            }
        }
    }
    
    public static void weatherType() throws ParseException{
        weatherType2description = new HashMap<>();
        JSONObject json = getJsonFromApi("http://api.ipma.pt/open-data/weather-type-classe.json");
        if (json != null){
            for (Object obj : (JSONArray) json.get("data")){
                Map<String,Object> temp = (Map<String,Object>) obj;
                weatherType2description.put((Long) temp.get("idWeatherType"), (String) temp.get("descIdWeatherTypeEN"));
            }
        }
    }
    
    private static Map<String, Object> localCache = new HashMap<>();
    
    @GetMapping("all_cities")
    public Object getAllCities() throws ParseException{
        //get global id of location passed as argument
        if (location2globalId == null){
            globalIdData();
        }
        
        //return all cities in the api
        JSONArray jArray = new JSONArray();
        for (int i = 0; i < location2globalId.keySet().size(); i++){
            jArray.add(location2globalId.keySet().toArray()[i]);
        }

        return jArray;   
    }
    
    @GetMapping("weatherTypes")
    public Object getWeatherDesc() throws ParseException{
        //get global id of location passed as argument
        if (weatherType2description == null){
            weatherType();
        }
        
        //return all cities in the api
        JSONObject jsonObj = new JSONObject();
        for (int i = 0; i < weatherType2description.keySet().size(); i++){
            jsonObj.put(weatherType2description.keySet().toArray()[i], weatherType2description.get(weatherType2description.keySet().toArray()[i]));
        }

        return jsonObj;   
    }
    
    @GetMapping("windTypes")
    public Object getWindDesc() throws ParseException{
        //get global id of location passed as argument
        if (windType2description == null){
            windType();
        }
        
        //return all cities in the api
        JSONObject jsonObj = new JSONObject();
        for (int i = 0; i < windType2description.keySet().size(); i++){
            jsonObj.put(windType2description.keySet().toArray()[i], windType2description.get(windType2description.keySet().toArray()[i]));
        }

        return jsonObj;   
    }
    
    @GetMapping("get_local_data/{local}/{first_day}/{last_day}")
    public static Object getLocalData(@PathVariable("local") final String name, @PathVariable("first_day") final int first_day, @PathVariable("last_day") final int last_day) throws ParseException {
        //days must be in [0-4]
        if ((!(0 <= first_day && first_day <= 4)) || (!(0 <= last_day && last_day <= 4))){
            JSONParser parser = new JSONParser();
            return parser.parse("{\"Error\" : \"The number of days must be contained in [0-4]\"}");
        }
        
        //last day must be bigger or equal to the first day
        if (first_day > last_day){
            JSONParser parser = new JSONParser();
            return parser.parse("{\"Error\" : \"Last day must be bigger or equal to the first day\"}");
        }
        
        //get global id of location passed as argument
        if (location2globalId == null || weatherType2description == null || windType2description == null){
            globalIdData();
            weatherType();
            windType();
        }
        
        try {

            String result;
            
            //go to local cache and try to get the response from there
            if (localCache.containsKey(name)){
                logger.info("[{}] CACHE ", name);
                
                Map<String, Object> temp = new HashMap<>();
                temp.put("data", ((ArrayList) localCache.get(name)).toArray());
                Gson gson = new Gson(); 
                result = gson.toJson(temp);
            }else{
                numberOfRequests++;
                //cal impa api and get result in a String
                final String uri = "http://api.ipma.pt/open-data/forecast/meteorology/cities/daily/" + location2globalId.get(name) + ".json";
                RestTemplate restTemplate = new RestTemplate();
                result = restTemplate.getForObject(uri, String.class);
                hits++;
                
                logger.info("[{}] API : (req-{} ; hits-{} ; misses-{})", name, numberOfRequests, hits, misses);

                //save in local cache
                Map<String, Object> mapObj = new Gson().fromJson(result, new TypeToken<HashMap<String, Object>>() {}.getType());
                localCache.put(name, mapObj.get("data"));

                //time to live
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                       localCache.remove(name);
                    }
                  }, 30000);
            }
            
            //create api's response in json
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(result);
                
            //return only the 'selected' number of days
            JSONArray jArray = new JSONArray();
            for (int i = first_day; i <= last_day; i++){
                jArray.add(((JSONArray) json.get("data")).get(i));
            }
            
            return jArray;
        }
        catch(HttpClientErrorException e){
            misses++;
            JSONParser parser = new JSONParser();
            return parser.parse("{\"Error\" : \"City \'" + name + "\' not found\"}");
        }
        
    }
    
}
