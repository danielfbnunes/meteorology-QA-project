package ua.pt.meteorology;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
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
    
    private static final String WEATHERTYPE = "idWeatherType"; 
    
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
    
    public String getStringFromAlternativeApi(String uri, long time, String termination) throws ParseException{
        JSONObject js = new JSONObject();
        js.put("data", new JSONArray());
        Calendar calendar = Calendar.getInstance();
        LocalDate localDate;
        for (int i = 0; i <= 5; i++){
            JSONObject jsObj = getJSONObjectFromApi(uri + (time + (i * 86400)) + termination);
            JSONObject jsonFinal = null;
            if (i==0){
                localDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                jsonFinal = parseJSON2TypeRequired((JSONObject) (((JSONArray)((JSONObject)jsObj.get("daily")).get("data")).get(0)), ""+(localDate.getYear())+'-'+String.format("%02d", (localDate.getMonthValue()))+'-'+String.format("%02d", (localDate.getDayOfMonth())));            
            }else{
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                localDate = calendar.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                jsonFinal = parseJSON2TypeRequired((JSONObject) (((JSONArray)((JSONObject)jsObj.get("daily")).get("data")).get(0)), ""+(localDate.getYear())+'-'+String.format("%02d", (localDate.getMonthValue()))+'-'+String.format("%02d", (localDate.getDayOfMonth())));
            }
            ((JSONArray) js.get("data")).add(jsonFinal);
        }
        
        return js.toJSONString();        
    }
    
    public JSONObject parseJSON2TypeRequired(JSONObject jsonInicial, String data){
        JSONObject jsonFinal = new JSONObject();
        jsonFinal.put("forecastDate", data);
        
        String icon = (String) jsonInicial.get("icon");
        
        jsonFinal.put(WEATHERTYPE, getWeatherTypeFromIcon(icon));
        
        DecimalFormat df = new DecimalFormat("#.#");
        jsonFinal.put("tMin", df.format((((Double)jsonInicial.get("temperatureMin"))-32)/1.8));
        jsonFinal.put("tMax", df.format((((Double)jsonInicial.get("temperatureMax"))-32)/1.8));
        jsonFinal.put("windVel", jsonInicial.get("windSpeed"));
        jsonFinal.put("precipitaProb", df.format(Double.parseDouble(""+ jsonInicial.get("precipProbability"))*100));
        return jsonFinal;
    }
    
    public int getWeatherTypeFromIcon(String icon){
        if (icon.equals("clear-day") || icon.equals("clear-night")) return 1;
        else if (icon.equals("rain")) return 6;
        else if (icon.equals("snow") || icon.equals("sleet")) return 18;
        else if (icon.equals("wind") || icon.equals("fog")) return 16;
        else if (icon.equals("cloudy")) return 4;
        else if (icon.equals("partly-cloudy-day") || icon.equals("partly-cloudy-night")) return 2;
        else return -99;
    }
    
}
