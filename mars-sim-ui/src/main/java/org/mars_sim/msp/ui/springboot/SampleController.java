/* Mars Simulation Project
 * SampleController.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 * $LastChangedDate$
 * $LastChangedRevision$


package org.mars_sim.msp.ui.springboot;

//import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class SampleController {

    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("message", "[Note : loaded via a thymeleaf template]");
        return "hello";
    }

    @RequestMapping(value="/greeting", method=RequestMethod.GET)
    public String greetingForm(Model model) {
        model.addAttribute("greeting", new Greeting());
        return "greeting";
    }
 
}
 */
