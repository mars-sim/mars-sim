/* Mars Simulation Project
 * IndexController.java
 * @version 3.1.0 2016-06-23
 * @author Manny Kung
 * $LastChangedDate$
 * $LastChangedRevision$
 */
package org.mars_sim.msp.ui.springboot;

/* need to enable spring boot artifact in pom.xml

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
@RequestMapping(value = "/")
public class IndexController {
    private Log LOG = LogFactory.getLog(IndexController.class);


    @RequestMapping(method = RequestMethod.GET)
    public String index(ModelMap map) {
        return "starter";
    }

    @RequestMapping(value="/earthmoon", method = RequestMethod.GET)
    public String loadEarthmoon(ModelMap map) {
        return "earthmoon";
    }

    @RequestMapping(value="/mars", method = RequestMethod.GET)
    public String loadMars(ModelMap map) {
        return "mars";
    }
    
    @RequestMapping(value="/marsphobos", method = RequestMethod.GET)
    public String loadMarsPhobos(ModelMap map) {
        return "marsphobos";
    }
    
    @RequestMapping(value="/hello", method=RequestMethod.GET)
    public String hello(Model model) {
        model.addAttribute("message", "[Note : loaded via a thymeleaf template]");
        return "hello";
    }

    //@RequestMapping(value="/main", method=RequestMethod.GET)
    //public String loadMain(ModelMap map) {
    //    return "main";
    //}
   
    @RequestMapping(value="/starter", method=RequestMethod.GET)
    public String starter(Model model) {
        model.addAttribute("message", "[Note : loaded via a thymeleaf template]");
        return "starter";
    }
    
    @RequestMapping(value="/greeting", method=RequestMethod.GET)
    public String greetingForm(Model model) {
        model.addAttribute("greeting", new Greeting());
        return "greeting";
    }

    @RequestMapping(value="/greeting", method=RequestMethod.POST)
    public String greetingSubmit(@ModelAttribute Greeting greeting, Model model) {
        model.addAttribute("greeting", greeting);
        return "result";
    }

	//@RequestMapping(value = "/")
    //public String index(Model model) {
    //    model.addAttribute("message", "[Note : loaded via a thymeleaf template]");
    //    return "hello";
    //}
}
*/ 