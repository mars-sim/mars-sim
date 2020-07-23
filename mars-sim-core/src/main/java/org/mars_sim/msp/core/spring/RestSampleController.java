/* Mars Simulation Project
 * RestSampleController.java
 * @version 3.1.1 2020-07-22
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
