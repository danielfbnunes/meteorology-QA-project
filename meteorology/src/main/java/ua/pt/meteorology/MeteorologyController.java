package ua.pt.meteorology;

import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 *
 * @author dn
 */
@Controller
public class MeteorologyController {
        
    @GetMapping("meteorology")
    public String showIndexPage() throws ParseException{
        //get global id of location passed as argument
        if (MeteorologyResources.getLocation2globalId() == null || MeteorologyResources.getWeatherType2description() == null){
            MeteorologyResources.globalIdData();
            MeteorologyResources.weatherType();
        }
        return "index.html";
    }
}
