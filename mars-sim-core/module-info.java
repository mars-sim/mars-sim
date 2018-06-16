module mars-sim-core {

	requires java;
    requires javax.json;
    
    requires junit;
    requires jdom;
    requires guava;
    requires guice;
    requires log4j;
    requires commons-collections;

    requires xz;
    
//    requires reactfx;
//   requires fxgl;
//    requires javafx.collections.FXCollections;
//    requires javafx.collections.ObservableList;
    
//   requires mars-sim-ui;
    requires mars-sim-mapdata;
    
	exports mars-sim-core;
}