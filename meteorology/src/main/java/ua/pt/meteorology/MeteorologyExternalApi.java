/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.pt.meteorology;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author dn
 */
public class MeteorologyExternalApi {
    
    public boolean testConnection(String uri){
        try {
            new RestTemplate().getForObject(uri, String.class);
            return true;
        }catch (HttpClientErrorException e){
            return false;
        }
    }
    
    public JSONObject getJSONObjectFromApi(String uri) throws ParseException{
        String result = new RestTemplate().getForObject(uri, String.class);
        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(result);

    }
    
    public String getStringFromApi(String uri) {
        return new RestTemplate().getForObject(uri, String.class);
    }
    
}
