/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
