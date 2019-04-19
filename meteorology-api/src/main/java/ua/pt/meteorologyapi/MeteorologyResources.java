/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.pt.meteorologyapi;

import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author dn
 */
@RestController
public class MeteorologyResources {
    
    public static Map<String, Long> location2globalId;
    
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
    
    @GetMapping("get_local_data/{local}")
    public JSONObject getLocalData(@PathVariable("local") final String name) throws ParseException {
        
        //get global id of location passed as argument
        if (location2globalId == null){
            globalIdData();
        }
              
        final String uri = "http://api.ipma.pt/open-data/forecast/meteorology/cities/daily/" + location2globalId.get(name) + ".json";
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(uri, String.class);
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(result);
        return json;
    }
    
}
