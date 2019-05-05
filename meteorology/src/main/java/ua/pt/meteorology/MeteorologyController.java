package ua.pt.meteorology;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author dn
 */
@Controller
public class MeteorologyController {
        
    @GetMapping("meteorology")
    public String showIndexPage() {
        new RestTemplate().getForObject("http://localhost:8080/meteorologyInit", String.class);
        return "index.html";
    }
}
