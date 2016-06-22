/* Mars Simulation Project
 * Application.java
 * @version 3.1.0 2016-06-21
 * @author Manny Kung
 */

package org.mars_sim.msp.core.spring;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class Applcation {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Applcation.class, args);

        System.out.println("Running Applcation : Let's inspect the beans provided by Spring Boot :");

        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            System.out.println(beanName);
        }
    }

}