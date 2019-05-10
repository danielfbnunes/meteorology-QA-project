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
    
    /**
     * Interface mapping function
     * @return Interface file name
     */
    @GetMapping("meteorology")
    public String showIndexPage() {
        // Get initial data from api before returnin                                                                                                                                                        g the page to the user.
        new RestTemplate().getForObject("http://localhost:8080/meteorologyInit", String.class);
        return "index.html";
    }
}
