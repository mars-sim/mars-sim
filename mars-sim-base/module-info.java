module mars-sim-base {

	requires java.util;
	
    requires javafx;
//    requires javafx.base;	
//	requires javafx.graphics;
//    requires javafx.scene;
//    requires javafx.stage.Stage;
//    requires javafx.application.Application;
//    requires javafx.application.Platform;
//    
	requires com.almasb.fxgl.dsl;
	requires com.almasb.fxgl.app;
//	requires com.almasb.fxgl.settings;

    requires junit;

    requires mars-sim-headless;
    requires mars-sim-fxgl;
    
	exports mars-sim-base;
}