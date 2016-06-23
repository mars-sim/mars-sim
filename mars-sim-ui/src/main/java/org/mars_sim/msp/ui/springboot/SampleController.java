/* Mars Simulation Project
 * SampleController.java
 * @version 3.1.0 2016-06-22
 * @author Manny Kung
 * $LastChangedDate$
 * $LastChangedRevision$
 */

package org.mars_sim.msp.ui.springboot;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SampleController {

    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("message", "[Note : loaded via a thymeleaf template]");
        return "backstory";
    }

}