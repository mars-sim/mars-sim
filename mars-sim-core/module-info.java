module mars-sim-core {

	requires java;
    requires javax.json;
    
    requires junit;
    requires jdom;
    requires guava;
    requires guice;
    requires log4j;
    requires commons-collections;
    requires text-io;
    requires gson;
    requires commons-lang3;
//    requires xz;
    
//	requires java.desktop;
    
//    requires reactfx;
//   requires fxgl;
//    requires javafx.collections.FXCollections;
//    requires javafx.collections.ObservableList;
    
//   requires mars-sim-ui;
    requires mars-sim-mapdata;
    
	exports mars-sim-core;
}