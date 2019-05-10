package ua.pt.meteorology;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 * @author dn
 */
@RunWith(SpringRunner.class)
public class MeteorologyResourcesTest {

    public MeteorologyResourcesTest() {
    }
    
    
    
    private MeteorologyResources meteoRes;
    
    private MeteorologyResources testSubject = new MeteorologyResources();

    @Mock
    private MeteorologyExternalApi externalApi;

    @Before
    public void setup() throws ParseException{
        meteoRes = new MeteorologyResources();
        
        when(externalApi.testConnection(ArgumentMatchers.anyString()))
                .thenReturn(true);
                
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        data.put("local", "Aveiro");
        data.put("globalIdLocal", Long.parseLong("12345"));
        data.put("latitude", "40.6413");
        data.put("longitude", "-8.6535");
        JSONArray js = new JSONArray();
        js.add(data);
        map.put("data", js);
        
        when(externalApi.getJSONObjectFromApi("http://api.ipma.pt/open-data/distrits-islands.json"))
                .thenReturn(new JSONObject(map));
        
        data = new HashMap<>();
        data.put("classWindSpeed", "12345");
        data.put("descClassWindSpeedDailyEN", "xpto");
        js = new JSONArray();
        js.add(data);
        map.put("data", js);
        when(externalApi.getJSONObjectFromApi("http://api.ipma.pt/open-data/wind-speed-daily-classe.json"))
                .thenReturn(new JSONObject(map));
        
        data = new HashMap<>();
        data.put("idWeatherType", Long.parseLong("12345"));
        data.put("descIdWeatherTypeEN", "xpto");
        js = new JSONArray();
        js.add(data);
        map.put("data", js);
        when(externalApi.getJSONObjectFromApi("http://api.ipma.pt/open-data/weather-type-classe.json"))
                .thenReturn(new JSONObject(map));    
        
        when(externalApi.getStringFromApi("http://api.ipma.pt/open-data/forecast/meteorology/cities/daily/12345.json"))
                .thenReturn("{\"data\" : [{\"tMax\" : 20, \"tMin\" : 10, \"idWeatherType\" : 1, \"classWindSpeed\" : 2}]}");
               
        testSubject.setExternalApi(externalApi);
    }
      
    @After
    public void tearDown(){
        meteoRes = null;
        externalApi = null;
    }
        
    @Test
    public void testCacheAfterNewRequest() throws ParseException{
        meteoRes.getLocalData("Coimbra", 0, 4);
        assertTrue(meteoRes.getLocalCache().containsKey("Coimbra"));
    }
                   
    @Test
    public void testCacheAfter30SecondsOfNewRequest() throws ParseException, InterruptedException{
        meteoRes.getLocalData("Lisboa", 0, 4); 
        TimeUnit.SECONDS.sleep(30);
        assertFalse(meteoRes.getLocalCache().containsKey("Lisboa"));
    }

    @Test
    public void testResponseFromCacheEqualsToResponseFromApi() throws ParseException{
        JSONArray jApi = (JSONArray) meteoRes.getLocalData("Porto", 0, 4);
        JSONArray jCache = (JSONArray) meteoRes.getLocalData("Porto", 0, 4);
        assertEquals(jApi.toJSONString(), jCache.toJSONString());
    }
    
    @Test
    public void testGetJsonFromApi_BadUrl() throws ParseException{
        JSONObject j = meteoRes.getJsonFromApi("http://api.ipma.pt/open-data/not_an_url");
        assertEquals(null, j);
    }
    
    
    @Test
    public void testInitializeData() throws ParseException {
        meteoRes.initializeData();
        assertFalse(meteoRes.getLocation2globalId().isEmpty());
        assertFalse(meteoRes.getWindType2description().isEmpty());
        assertFalse(meteoRes.getWeatherType2description().isEmpty());
    }
    
    @Test
    public void testGetAllCities() throws ParseException {
        JSONArray j = testSubject.getAllCities();
        JSONArray jsonArray = new JSONArray();
        jsonArray.add("Aveiro");
        assertEquals(j, jsonArray);
    }
    
    @Test
    public void testGetWeatherDesc() throws ParseException{
        JSONObject j = testSubject.getWeatherDesc();
        JSONObject temp = new JSONObject();
        temp.put("12345", "xpto");
        assertTrue(j.toJSONString().equals(temp.toJSONString()));
    }
    
    @Test
    public void testGetWindDesc() throws ParseException{
        JSONObject j = testSubject.getWindDesc();
        JSONObject temp = new JSONObject();
        temp.put("12345", "xpto");
        assertTrue(j.toJSONString().equals(temp.toJSONString()));
    }
    
    @Test
    public void testGlobalIdData() throws ParseException {       
        testSubject.globalIdData();
        assertTrue(((Object[]) testSubject.getLocation2globalId().get("Aveiro"))[0].equals(Long.parseLong("12345")));
        assertTrue(((Object[]) testSubject.getLocation2globalId().get("Aveiro"))[1].equals(Float.parseFloat("40.6413")));
        assertTrue(((Object[]) testSubject.getLocation2globalId().get("Aveiro"))[2].equals(Float.parseFloat("-8.6535")));
    }
    
    @Test
    public void testWindType() throws ParseException {
        testSubject.windType();
        assertTrue(testSubject.getWindType2description().containsKey(Long.parseLong("12345")));
        assertTrue(testSubject.getWindType2description().get(Long.parseLong("12345")).equals("xpto"));
    }
    
    @Test
    public void testWeatherType() throws ParseException {
        testSubject.weatherType();
        assertTrue(testSubject.getWeatherType2description().containsKey(Long.parseLong("12345")));
        assertTrue(testSubject.getWeatherType2description().get(Long.parseLong("12345")).equals("xpto"));
    }

    @Test
    public void testGetLocalDataFromApi() throws ParseException {
        testSubject.getLocalData("Aveiro", 0, 0);
        assertTrue(testSubject.getLocalCache().containsKey("Aveiro"));
        Map<String, Object> mapObj = new Gson().fromJson("{\"Aveiro\" : [{\"tMax\" : 20, \"tMin\" : 10, \"idWeatherType\" : 1, \"classWindSpeed\" : 2}]}", new TypeToken<HashMap<String, Object>>() {}.getType());
        ((ArrayList<Map<String, Object>>)mapObj.get("Aveiro")).get(0).put("idWeatherType",((Double) ((ArrayList<Map<String, Object>>)mapObj.get("Aveiro")).get(0).get("idWeatherType")).intValue());
        ((ArrayList<Map<String, Object>>)mapObj.get("Aveiro")).get(0).put("classWindSpeed",((Double) ((ArrayList<Map<String, Object>>)mapObj.get("Aveiro")).get(0).get("classWindSpeed")).intValue());
        assertEquals(testSubject.getLocalCache(), mapObj);
    }
    
    @Test
    public void testGetJsonFromApi_RightUrl() throws ParseException {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        data = new HashMap<>();
        data.put("classWindSpeed", "12345");
        data.put("descClassWindSpeedDailyEN", "xpto");
        JSONArray js = new JSONArray();
        js.add(data);
        map.put("data", js);
        JSONObject j = testSubject.getJsonFromApi("http://api.ipma.pt/open-data/wind-speed-daily-classe.json");
        assertEquals(j, new JSONObject(j));
    }
        
    @Test
    public void testAllCities_ResponseIs200AndContentIsJSON() {
        given().
        when().
                get("http://localhost:8080/all_cities").
        then().
                assertThat().
                statusCode(200).
        and().
                contentType(ContentType.JSON);
    }
    
    @Test
    public void testAllCities_VerifyIfAllElementsAreString() {
        JSONArray jsonArray = given().
        when().
                get("http://localhost:8080/all_cities").
                as(JSONArray.class);
        
        for (int i = 0; i < jsonArray.size(); i++){
            assertThat(jsonArray.get(i), instanceOf(String.class));
        }
    }
      
    @Test
    public void testGetWeatherDesc_ResponseIs200AndContentIsJSON() {
        given().
        when().
                get("http://localhost:8080/weatherTypes").
        then().
                assertThat().
                statusCode(200).
        and().
                contentType(ContentType.JSON);
    }
    
    @Test
    public void testGetWeatherDesc_AllKeysAndValuesAreString() {
        JSONObject jsonObject = given().
        when().
                get("http://localhost:8080/weatherTypes").
                as(JSONObject.class);
        
        Object[] all_keys = jsonObject.keySet().toArray();
        
        for (int i = 0; i < all_keys.length; i++){
            assertThat(all_keys[i], instanceOf(String.class));
            assertThat(jsonObject.get(all_keys[i]), instanceOf(String.class));
        }
    }
        
    @Test
    public void testGetWindDesc_ResponseIs200AndContentIsJSON() {
        given().
        when().
                get("http://localhost:8080/windTypes").
        then().
                assertThat().
                statusCode(200).
        and().
                contentType(ContentType.JSON);
    }
    
    @Test
    public void testGetWindDesc_AllKeysAndValuesAreString() {
        JSONObject jsonObject = given().
        when().
                get("http://localhost:8080/windTypes").
                as(JSONObject.class);
        
        Object[] all_keys = jsonObject.keySet().toArray();
        
        for (int i = 0; i < all_keys.length; i++){
            assertThat(all_keys[i], instanceOf(String.class));
            assertThat(jsonObject.get(all_keys[i]), instanceOf(String.class));
        }
    }
    
    @Test
    public void testGetLocalData_ResponseIs200AndContentIsJSON() {
        given().
        when().
                get("http://localhost:8080/get_local_data/Aveiro/0/4").
        then().
                assertThat().
                statusCode(200).
        and().
                contentType(ContentType.JSON);
    }
    
    @Test
    public void testGetLocalData_CheckResponseArraySize() {
        String local = "Aveiro";
        int start_day = 1;
        int end_day = 3;
        int expected_size = 3;
        
        given().
        when().
                get("http://localhost:8080/get_local_data/"+local+"/"+start_day+"/"+end_day).
        then().
                assertThat().
                body("size()", is(expected_size));
    }
    
    @Test
    public void testGetLocalData_BadStartDay() throws ParseException {
        String local = "Aveiro";
        int start_day = -2;
        int end_day = 2;
        
        JSONObject json = (JSONObject) meteoRes.getLocalData(local, start_day, end_day);
        
        JSONObject j = new JSONObject();
        j.put("Error", "The number of days must be contained in [0-4]");
        assertEquals(j, json);
    }
    
    @Test
    public void testGetLocalData_BadEndDay() throws ParseException {
        String local = "Aveiro";
        int start_day = 2;
        int end_day = -2;
        
        JSONObject json = (JSONObject) meteoRes.getLocalData(local, start_day, end_day);
        
        JSONObject j = new JSONObject();
        j.put("Error", "The number of days must be contained in [0-4]");
        assertEquals(j, json);
    }
    
    @Test
    public void testGetLocalData_EndDayBiggerThanStartDay() throws ParseException {
        String local = "Aveiro";
        int start_day = 3;
        int end_day = 2;
        
        JSONObject json = (JSONObject) meteoRes.getLocalData(local, start_day, end_day);
        
        JSONObject j = new JSONObject();
        j.put("Error", "Last day must be bigger or equal to the first day");
        assertEquals(j, json);
    }
    
    @Test
    public void testGetLocalData_NonExistantCity() throws ParseException {
        String local = "Xpto";
        int start_day = 1;
        int end_day = 2;
        
        JSONObject json = (JSONObject) meteoRes.getLocalData(local, start_day, end_day);
        
        JSONObject j = new JSONObject();
        j.put("Error", "City \'Xpto\' not found");
        assertEquals(j, json);
    }

}
