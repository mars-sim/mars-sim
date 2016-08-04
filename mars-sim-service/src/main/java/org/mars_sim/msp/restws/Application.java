package org.mars_sim.msp.restws;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Main application to kick start service via Spring Boot.
 *
 */
@SpringBootApplication
@EnableSwagger2
public class Application {
	
	private static Log log = LogFactory.getLog(Application.class);
	
	private Simulation simulation;

	public Application() {
		log.info("Building the simulator");
		
		// Bootstrap could reload a saved simulation
		// No auto saving enabled
		MarsBootstrap bootstrap = new MarsBootstrap();

		simulation = bootstrap.buildSimulation();
	}

	@Bean
	public Simulation getSimulation() {
		return simulation;
	}

	@Bean
	public MissionManager getMissionManager() {
		return getSimulation().getMissionManager();
	}
	
	@Bean
	public UnitManager getUnitManager() {
		return getSimulation().getUnitManager();
	}
	
	@Bean
	public HistoricalEventManager getHistoricalEventManager() {
		return getSimulation().getEventManager();
	}
	
    @SuppressWarnings("unchecked")
	@Bean
    public Docket simulationApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("simulation")
                .apiInfo(apiInfo())
                .select()
                .paths(or(regex("/simulation.*"), regex("/persons.*"), regex("/missions.*"), regex("/vehicles.*"), regex("/settlements.*")))
                .build();
    }
    
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("REST service for mars-sim")
                .description("Spring REST of the Mars Sim project with Swagger")
                .termsOfServiceUrl("http://mars-sim.sourceforge.net/")
                .contact("Barry Evans")
                .license("Apache License Version 2.0")
                .licenseUrl("https://github.com/IBM-Bluemix/news-aggregator/blob/master/LICENSE")
                .version("2.0")
                .build();
    }
    
    public static void main(String[] args) {
        ApplicationContext context =  SpringApplication.run(Application.class, args);
        
        // Start simulation after everything has loaded
		log.info("Simulation started");
		Simulation sim = context.getBean(Simulation.class);
		sim.start(true);
    }
}
