package Risosu.it.EGDDTheMovieDB25Julio.Controller;

import ch.qos.logback.core.model.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/Principal")
public class ControllerPrincipal {

    @GetMapping
    public String Index() {
        return "Index";
    }
    @GetMapping("/Login")
    public String Login(){
        return "Login";
    }

}
