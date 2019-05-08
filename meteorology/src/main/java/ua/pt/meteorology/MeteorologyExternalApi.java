/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.pt.meteorology;

import java.text.DecimalFormat;
import java.util.Date;
import org.json.simple.JSONArray;
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
    
    public String getStringFromAlternativeApi(String uri, String termination) throws ParseException{
        JSONObject js = new JSONObject();
        js.put("data", new JSONArray());
        long current_time = System.currentTimeMillis() / 1000;
        Date today = new Date();
        Date tomorrow = new Date();
        tomorrow.setDate(today.getDate()+1);
        
        for (int i = 0; i <= 5; i++){
            String result = new RestTemplate().getForObject(uri + (current_time + (i * 86400)) + termination, String.class);
            JSONParser parser = new JSONParser();
            JSONObject jsObj = (JSONObject) parser.parse(result);
            JSONObject jsonFinal = null;
            if (i==0){
                jsonFinal = parseJSON2TypeRequired((JSONObject) (((JSONArray)((JSONObject)jsObj.get("daily")).get("data")).get(0)), ""+(today.getYear()+1900)+'-'+String.format("%02d", (today.getMonth()+1))+'-'+String.format("%02d", (today.getDate())));            
            }else{
                jsonFinal = parseJSON2TypeRequired((JSONObject) (((JSONArray)((JSONObject)jsObj.get("daily")).get("data")).get(0)), ""+(tomorrow.getYear()+1900)+'-'+String.format("%02d", (tomorrow.getMonth()+1))+'-'+String.format("%02d", (tomorrow.getDate())));
                tomorrow.setDate(tomorrow.getDate()+1);
            }
            ((JSONArray) js.get("data")).add(jsonFinal);
        }
        
        return js.toJSONString();        
    }
    
    private JSONObject parseJSON2TypeRequired(JSONObject jsonInicial, String data){
        JSONObject jsonFinal = new JSONObject();
        jsonFinal.put("forecastDate", data);
        
        switch((String) jsonInicial.get("icon")){
            case "clear-day":
                jsonFinal.put("idWeatherType", 1);
                break;
            case "clear-night":
                jsonFinal.put("idWeatherType", 1);
                break;
            case "rain":
                jsonFinal.put("idWeatherType", 6);
                break;
            case "snow":
                jsonFinal.put("idWeatherType", 18);
                break;
            case "sleet":
                jsonFinal.put("idWeatherType", 18);
                break;
            case "wind":
                jsonFinal.put("idWeatherType", 16);
                break;
            case "fog":
                jsonFinal.put("idWeatherType", 16);
                break;
            case "cloudy":
                jsonFinal.put("idWeatherType", 4);
                break;
            case "partly-cloudy-day":
                jsonFinal.put("idWeatherType", 2);
                break;
            case "partly-cloudy-night":
                jsonFinal.put("idWeatherType", 2);
                break;
        }
        DecimalFormat df = new DecimalFormat("#.#");
        jsonFinal.put("tMin", df.format((((Double)jsonInicial.get("temperatureMin"))-32)/1.8));
        jsonFinal.put("tMax", df.format((((Double)jsonInicial.get("temperatureMax"))-32)/1.8));
        jsonFinal.put("windVel", jsonInicial.get("windSpeed"));
        jsonFinal.put("precipitaProb", ((Double) jsonInicial.get("precipProbability"))*100);
        return jsonFinal;
    }
    
}
