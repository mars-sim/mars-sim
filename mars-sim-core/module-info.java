module mars-sim-core {

    requires junit;
    requires jdom;
    requires guava;
    requires guice;
    requires log4j;
    requires commons-collections;
    requires javax.json;
    requires xz;
    requires reactfx;
    requires fxgl;
    
    requires mars-sim-ui;
    requires mars-sim-mapdata;
    requires mars-sim-core;
    
	exports mars-sim-core;
}