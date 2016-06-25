/* Mars Simulation Project
 * RestSampleController.java
 * @version 3.1.0 2016-06-21
 * @author Manny Kung
 

package org.mars_sim.msp.core.spring;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class RestSampleController {

    @RequestMapping("/")
    public String index() {
        return "Running RestSampleController--the class that has @RestController annotation: Greetings from Spring Boot!";
    }

}
*/