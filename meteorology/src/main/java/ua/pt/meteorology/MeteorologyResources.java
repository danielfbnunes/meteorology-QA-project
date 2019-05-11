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

/**
 *
 * @author dn
 */
@RestController
@CrossOrigin(origins = "*")
public class MeteorologyResources {
    
    private MeteorologyExternalApi externalApi = new MeteorologyExternalApi();
    
    static final Logger logger = LoggerFactory.getLogger(MeteorologyResources.class);
    
    private Map<String, Object> localCache = new HashMap<>();
    
    private int numberOfRequests = 0;
    private int hits = 0;
    private int misses = 0;
    
    static final String CLASSWINDSPEED = "classWindSpeed";
    static final String IDWEATHERTYPE = "idWeatherType";
    static final String CLASSPRECINT = "classPrecInt";
    static final String CITIES_BASE_URL = "http://api.ipma.pt/open-data/forecast/meteorology/cities/daily/";
    static final String URL_TERMINATION = ".json";
    
    static final String DARK_SKY = "https://api.darksky.net/forecast/7f01ccfd2bda82f95d5930bcb54a4dac/";
    static final String DARK_SKY_TERMINATION = "?exclude=currently,flags,minutely,hourly,alerts";
    
    private Map<String, Object[]> location2globalId;
    
    private Map<Long, String> weatherType2description;
    
    private Map<Long, String> windType2description;

    public void setExternalApi(MeteorologyExternalApi externalApi) {
        this.externalApi = externalApi;
    }
             
    public Map<Long, String> getWeatherType2description() {
        return weatherType2description;
    }

    public Map<Long, String> getWindType2description() {
        return windType2description;
    }
       
    public Map<String, Object[]> getLocation2globalId() {
        return location2globalId;
    }

    public Map<String, Object> getLocalCache() {
        return localCache;
    }
    
    /**
     * Request api and returns its data in JSON format.
     * @param uri Api url.
     * @return JSON data.
     * @throws ParseException 
     */
    public JSONObject getJsonFromApi(String uri) throws ParseException{
        numberOfRequests++;
        if (externalApi.testConnection(uri)){
            hits++;
            return externalApi.getJSONObjectFromApi(uri);
        }
        misses++;
        return null;
    }
    
    /**
     * Fills 'location2globalId' with data requested to the api. Here, we associate
     * the local with an array made with the globalId, latitude and longitude from
     * the local in question.
     * @throws ParseException 
     */
    public void globalIdData() throws ParseException{
        location2globalId = new HashMap<>();
        JSONObject json = getJsonFromApi("http://api.ipma.pt/open-data/distrits-islands.json");
                
        if (json != null){
            for (Object obj : (JSONArray) json.get("data")){
                Map<String,Object> temp = (Map<String,Object>) obj;
                numberOfRequests++;
                if (externalApi.testConnection(CITIES_BASE_URL + (Long) temp.get("globalIdLocal") + URL_TERMINATION)){
                    Object[] globalIdAndLatLong = new Object[] {(Long) temp.get("globalIdLocal"), Float.parseFloat((String) temp.get("latitude")), Float.parseFloat((String) temp.get("longitude"))};
                    location2globalId.put((String) temp.get("local"), globalIdAndLatLong);
                    hits++;
                }else{
                    logger.info("Can't request from this city: {}", (String) temp.get("local"));
                    misses++;
                }
            }
        }
    }
    
    /**
     * Fills 'windType2description' with data requested to the api. Here we
     * associate the classWindSpeed to the description.
     * @throws ParseException 
     */
    public void windType() throws ParseException{
        windType2description = new HashMap<>();
        JSONObject json = getJsonFromApi("http://api.ipma.pt/open-data/wind-speed-daily-classe.json");
        if (json != null){
            for (Object obj : (JSONArray) json.get("data")){
                Map<String,Object> temp = (Map<String,Object>) obj;
                windType2description.put(Long.parseLong((String) temp.get(CLASSWINDSPEED)), (String) temp.get("descClassWindSpeedDailyEN"));
            }
        }
    }
    
    /**
     * Fills 'weatherType2description' with data requested to the api. Here we
     * associate the idWeatherType to the description.
     * @throws ParseException 
     */
    public void weatherType() throws ParseException{
        weatherType2description = new HashMap<>();
        JSONObject json = getJsonFromApi("http://api.ipma.pt/open-data/weather-type-classe.json");
        if (json != null){
            for (Object obj : (JSONArray) json.get("data")){
                Map<String,Object> temp = (Map<String,Object>) obj;
                weatherType2description.put((Long) temp.get(IDWEATHERTYPE), (String) temp.get("descIdWeatherTypeEN"));
            }
        }
    }
    
    /**
     * Maps all cities available in the url 'http://localhost:8080/all_cities'.
     * @return JSON Array with all cities available.
     * @throws ParseException 
     */
    @GetMapping("all_cities")
    public JSONArray getAllCities() throws ParseException{
        //fill location2globalId if null
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
    
    /**
     * Maps the idWeatherType to its description with the url 'http://localhost:8080/weatherTypes'.
     * @return JSON object with the idWeatherType associated to its description.
     * @throws ParseException 
     */
    @GetMapping("weatherTypes")
    public JSONObject getWeatherDesc() throws ParseException{
        //fill weatherType2description if null
        if (weatherType2description == null){
            weatherType();
        }
        
        //return idWeatherType associated to its description
        JSONObject jsonObj = new JSONObject();
        for (int i = 0; i < weatherType2description.keySet().size(); i++){
            jsonObj.put(weatherType2description.keySet().toArray()[i], weatherType2description.get(weatherType2description.keySet().toArray()[i]));
        }

        return jsonObj;   
    }
    
    /**
     * Maps the classWindSpeed to its description with the url 'http://localhost:8080/windTypes'.
     * @return JSON object with the classWindSpeed associated to its description.
     * @throws ParseException 
     */
    @GetMapping("windTypes")
    public JSONObject getWindDesc() throws ParseException{
        //fill windType2description if null
        if (windType2description == null){
            windType();
        }
        
        //return classWindSpeed associated with its description
        JSONObject jsonObj = new JSONObject();
        for (int i = 0; i < windType2description.keySet().size(); i++){
            jsonObj.put(windType2description.keySet().toArray()[i], windType2description.get(windType2description.keySet().toArray()[i]));
        }

        return jsonObj;   
    }
    
    /**
     * Initialize info.
     * @throws ParseException 
     */
    @GetMapping("meteorologyInit")
    public void initializeData() throws ParseException{
        globalIdData();
        weatherType();
        windType();
    }
    
    /**
     * Returns meteorology prevision given a local and a space of time defined by
     * a start day and an end day.
     * @param name City in question.
     * @param first_day Prevision starting day.
     * @param last_day Prevision ending day.
     * @return Returns prevision or error response.
     * @throws ParseException 
     */
    @GetMapping("get_local_data/{local}/{first_day}/{last_day}")
    public Object getLocalData(@PathVariable("local") final String name, @PathVariable("first_day") final int first_day, @PathVariable("last_day") final int last_day) throws ParseException {
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
        
        //check for null and fill with info
        if (location2globalId == null || weatherType2description == null || windType2description == null){
            globalIdData();
            weatherType();
            windType();
        }
        
        String result;

        //go to local cache and try to get the response from there
        if (localCache.containsKey(name)){
            logger.info("[{}] CACHE ", name);

            Map<String, Object> temp = new HashMap<>();
            temp.put("data", ((ArrayList) localCache.get(name)).toArray());
            Gson gson = new Gson(); 
            result = gson.toJson(temp);
        
        }else{
            
            //check if city is in available cities.
            if (!location2globalId.containsKey(name)){
                JSONParser parser = new JSONParser();
                return parser.parse("{\"Error\" : \"City \'" + name + "\' not found\"}");
            }
            
            numberOfRequests++;
            String firstApi = CITIES_BASE_URL + location2globalId.get(name)[0] + URL_TERMINATION;
            
            //first test connection and try with impa api
            if (externalApi.testConnection(firstApi)){
                //call impa api and get result in a String
                result = externalApi.getStringFromApi(firstApi);            
            }else{
                //call darksky api and get result in a String
                result = externalApi.getStringFromAlternativeApi(DARK_SKY + location2globalId.get(name)[1] + "," + location2globalId.get(name)[2] + ",", (System.currentTimeMillis() / 1000), DARK_SKY_TERMINATION);
            }
                            
            hits++;

            logger.info("[{}] API : (req-{} ; hits-{} ; misses-{})", name, numberOfRequests, hits, misses);
            
            //save in local cache
            Map<String, Object> mapObj = new Gson().fromJson(result, new TypeToken<HashMap<String, Object>>() {}.getType());
            localCache.put(name, parseGSONObject(mapObj).get("data"));

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
    
    /**
     * Parse some elements from double to integer.
     * @param mapObj Map with double values not converted.
     * @return Map with double values converted to integer.
     */
    private Map<String, Object> parseGSONObject(Map<String,Object> mapObj){
        for(int i = 0; i < ((ArrayList<Map<String, Object>>)mapObj.get("data")).size(); i++){
            if (((ArrayList<Map<String, Object>>)mapObj.get("data")).get(i).containsKey(CLASSPRECINT)){
                ((ArrayList<Map<String, Object>>)mapObj.get("data")).get(i).put(CLASSPRECINT,((Double) ((ArrayList<Map<String, Object>>)mapObj.get("data")).get(i).get(CLASSPRECINT)).intValue());
            }
            ((ArrayList<Map<String, Object>>)mapObj.get("data")).get(i).put(IDWEATHERTYPE,((Double) ((ArrayList<Map<String, Object>>)mapObj.get("data")).get(i).get(IDWEATHERTYPE)).intValue());
            ((ArrayList<Map<String, Object>>)mapObj.get("data")).get(i).put(CLASSWINDSPEED,((Double) ((ArrayList<Map<String, Object>>)mapObj.get("data")).get(i).get(CLASSWINDSPEED)).intValue());
        }
        return mapObj;
    }
}
