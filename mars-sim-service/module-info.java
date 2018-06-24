module mars-sim-service {
	
    requires java.util;
  
    requires spring-boot-starter-web;
    requires spring-boot-starter-actuator;
    
    requires spring-webmvc;
    requires spring-web;
    requires spring-testhamcrest-all;
    requires springfox-swagger-ui;
    requires springfox-swagger2;
    requires mapstruct;
    requires junit;

    requires mars-sim-core;
    requires mars-sim-mapdata;
    
	exports mars-sim-service;
}