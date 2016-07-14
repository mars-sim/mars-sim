# RESTful webservice using the Mars Sim project simulation engine.

## Overview
This project provides a RESTful web service interface to the Mars-Sim simulation engine. In this PoC it provides access to the Person and Settlement entities as well as details of the core simulation status.  

The implementation uses the Spring WS project and heavily uses the @RestContoller annotation. The pattern used is that the entities of the simulation and converted into lightweight and flattened DTO representations that are then automatically converted into in JSON for transmission.

It uses MapStruct (http://mapstruct.org/) code generator to create mapper classes to mapping between the core classes of the msp-core into more lightweight (& flattened) DTO objects. A code generator approach produces the quickest and type-safe mapping code. The generator is executed automatically by the MVN project.

The RESTful service is documented using the Swagger OpenAPI specification (http://swagger.io/). The specification is generated directly from the Java REST Controllers via annotations that comes from Springfox (http://springfox.github.io/springfox/). These provide a wrapper for the Swagger UI that can be used directly from the browser.

Access to the health of the service is provided by the Spring Actuator module.

## Web Service Model Approach
The service implements a RESTurl approach. Some of the object entities within the marc-sim engine are very large and have a deep object graph. Such object graphs are not suited to being returned as a single JSON document as it can cause performance problems when under heavy use. Below summerises the approach.

Name|Description|Operation|Example URL
---------|-----------------|---------------|-------------------
All Entities|Each controller has an endpoint to list all entities in the simulation. This endpoint supports paging of the results. A *Summary* JSON is returned in this situation.|GET|/*Entities*/
Single Entity|More details of specific Units can be retrieved from a Controller by passing the unique identifier as a Request parameter. In this situation a single *Details* JSON is returned containing more information. References to other Units are returned; not the secondary Unit details|GET|/*Entities*/{identifier}
Entity Child objects|References to more details sub-objects of the main entity is returned as separate URLs. Generally these return an unpaged list of objects; in the case of Units these will be the appropriate *Summary* JSON object; not the *Details*. For example Persons at a Settlement.|GET|/Settlements/2/persons

## Re-using with Mars-sim core
The code simulation engine is re-used from the 3.07 Mars-Sim release without change (one change below). The code is imported via a MVN dependency.

### Unit class problem
The main Unit class has no unique identifier. This makes it impossible for the RESTful URLs to function without a fixed identifier. 

To get around this problem the main Unit class has a extended implementation within this problem. This is obviously bad design to deliberately change an imported class but the msp JARS are taken unchanged; this is one of the objectives of this project. The mitigation is for the main Unit class to be modified in the next Mars-Sim release.

### UnitManager overloading
The UnitManager class provides no easy means to look up specific entities. If there was a unique identifier assigned to each Unit then this would be simpler. The controllers codes has to search through the total list until the correct one is found.

## Service Running Locally
To deployed locally clone the repository and run **mvn spring-boot:run**. All code will be generated automatically as part of the Maven build.

* Swagger UI page @ http://localhost:8080/swagger-ui.html This provides a easy to use browser based interface to the underlying WebService.
* Swagger spec generated from code @ http://localhost:8080/v2/api-docs?group=simulation
* A list of all Persons @ http://localhost:8080/persons

The service have the Spring Acuator (https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html) enabled. Interesting end points:
* http:XXX:YYY/health - general status
* http:XXX:YYY/mappings - all registered endpoints
* http:XXX:YYY/metrics - lots of useful counters

## Eclipse project
The code is defined as a standard Maven project. The code generator works fine from a command line Maven but the Eclipse project has an issue. Eclipse Mars needed a M2E Connector loaded to support the extra lifecycle. Also the generated source does not get added to the default build path when the MVN project is improved; it has to be added manually.

The code should be included in the Eclipse project by running mvn eclipse:eclipse.