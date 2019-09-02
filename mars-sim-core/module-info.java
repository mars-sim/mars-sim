module mars-sim-core {

    requires javax.json;
//    requires java.json;
//    requires java.util;
//    requires java.text;
	requires java;
    
    requires junit;
    requires jdom;
    requires guava;
    requires guice;
    requires log4j;
    requires commons-collections;
//    requires text-io;
    requires gson;
    requires commons-lang3;
//    requires xz;
    
    requires jackson;
	requires flogger;
    requires kotlin;
    
    requires mars-sim-mapdata;
    
	exports mars-sim-core;
}