/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.pt.meteorology;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 *
 * @author DanielNunes
 */
@Controller
public class MeteorologyController { 
    @GetMapping("/home")
    public String home() {
        return "index.html";
    }
}
