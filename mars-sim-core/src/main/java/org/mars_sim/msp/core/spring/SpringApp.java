/* Mars Simulation Project
 * SpringApp.java
 * @version 3.1.0 2016-06-21
 * @author Manny Kung
 

package org.mars_sim.msp.core.spring;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class SpringApp {

    public static void main(String[] args) {
    	
    	ApplicationContext ctx = SpringApplication.run(SpringApp.class, args);

        System.out.println();
        System.out.println("------------ Running App : Let's inspect the beans provided by Spring Boot ------------");
        System.out.println();

        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            System.out.println(beanName);
        }
        
        System.out.println();
        System.out.println("------------ Now open http://localhost:8080/ in your browser ------------");
        System.out.println();

    }

}

*/