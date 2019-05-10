/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.pt.meteorology;

import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 * @author dn
 */
@RunWith(SpringRunner.class)
public class MeteorologyExternalApiTest {
    
    private MeteorologyExternalApi meteoExtApi;

    @Before
    public void setup() {
        meteoExtApi = new MeteorologyExternalApi();
    }
    
    @After
    public void tearDown(){
        meteoExtApi = null;
    }
    
    public MeteorologyExternalApiTest() {
    }

    @Test
    public void testTestConnection_GoodUrl() {
        assertTrue(meteoExtApi.testConnection("http://api.ipma.pt/open-data/wind-speed-daily-classe.json"));
    }
    
    @Test
    public void testTestConnection_BadUrl() {
        assertFalse(meteoExtApi.testConnection("http://api.ipma.pt/open-data/wind-speed-daily-classe.jso"));
    }

    @Test
    public void testGetJSONObjectFromApi() throws Exception {
        JSONObject json = meteoExtApi.getJSONObjectFromApi("http://api.ipma.pt/open-data/wind-speed-daily-classe.json");
        assertEquals("{\"owner\":\"IPMA\",\"country\":\"PT\",\"data\":[{\"descClassWindSpeedDailyEN\":\"--\",\"descClassWindSpeedDailyPT\":\"---\",\"classWindSpeed\":\"-99\"},{\"descClassWindSpeedDailyEN\":\"Weak\",\"descClassWindSpeedDailyPT\":\"Fraco\",\"classWindSpeed\":\"1\"},{\"descClassWindSpeedDailyEN\":\"Moderate\",\"descClassWindSpeedDailyPT\":\"Moderado\",\"classWindSpeed\":\"2\"},{\"descClassWindSpeedDailyEN\":\"Strong\",\"descClassWindSpeedDailyPT\":\"Forte\",\"classWindSpeed\":\"3\"},{\"descClassWindSpeedDailyEN\":\"Very strong\",\"descClassWindSpeedDailyPT\":\"Muito forte\",\"classWindSpeed\":\"4\"}]}", json.toJSONString());
    }

    @Test
    public void testGetStringFromAlternativeApi() throws Exception {
        String j = meteoExtApi.getStringFromAlternativeApi("https://api.darksky.net/forecast/7f01ccfd2bda82f95d5930bcb54a4dac/38.67,-27.22,", 1557149220, "?exclude=currently,flags,minutely,hourly,alerts");
        String j2 = meteoExtApi.getStringFromAlternativeApi("https://api.darksky.net/forecast/7f01ccfd2bda82f95d5930bcb54a4dac/38.67,-27.22,", 1557149220, "?exclude=currently,flags,minutely,hourly,alerts");
        assertEquals(j, j2);
    }

    @Test
    public void testParseJSON2TypeRequired() {
        JSONObject json = new JSONObject();
        json.put("icon", "xpto");
        json.put("temperatureMin", 32.0);
        json.put("temperatureMax", 32.0);
        json.put("windSpeed", 1);
        json.put("precipProbability", 0.4);
        String data = "2019-01-01";
        JSONObject j = meteoExtApi.parseJSON2TypeRequired(json, data);
        assertEquals("{\"tMax\":\"0\",\"precipitaProb\":\"40\",\"idWeatherType\":-99,\"windVel\":1,\"tMin\":\"0\",\"forecastDate\":\"2019-01-01\"}", j.toJSONString());
    }

    @Test
    public void testGetWeatherTypeFromIcon_ClearDayOrNight() {
        assertEquals(1,meteoExtApi.getWeatherTypeFromIcon("clear-day"));
        assertEquals(1,meteoExtApi.getWeatherTypeFromIcon("clear-night"));
    }
    
    @Test
    public void testGetWeatherTypeFromIcon_Rain() {
        assertEquals(6,meteoExtApi.getWeatherTypeFromIcon("rain"));
    }
    
    @Test
    public void testGetWeatherTypeFromIcon_WindOrFog() {
        assertEquals(16,meteoExtApi.getWeatherTypeFromIcon("wind"));
        assertEquals(16,meteoExtApi.getWeatherTypeFromIcon("fog"));
    }
    
    @Test
    public void testGetWeatherTypeFromIcon_Cloudy() {
        assertEquals(4,meteoExtApi.getWeatherTypeFromIcon("cloudy"));
    }
    
    @Test
    public void testGetWeatherTypeFromIcon_PartlyCloudyDayOrNight() {
        assertEquals(2,meteoExtApi.getWeatherTypeFromIcon("partly-cloudy-day"));
        assertEquals(2,meteoExtApi.getWeatherTypeFromIcon("partly-cloudy-night"));

    }
    
    @Test
    public void testGetWeatherTypeFromIcon_SnowOrSleet() {
        assertEquals(18,meteoExtApi.getWeatherTypeFromIcon("snow"));
        assertEquals(18,meteoExtApi.getWeatherTypeFromIcon("sleet"));
    }
        
    @Test
    public void testGetWeatherTypeFromIcon_BadIcon() {
        assertEquals(-99,meteoExtApi.getWeatherTypeFromIcon("null"));
    }
    
}
