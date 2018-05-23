module mars-sim-main {
    requires java.io;
    requires java.net;
    requires java.util;
    requires javax.swing;
    requires java.text;
    requires javafx.stage.Stage;
    requires javafx.application.Application;
    requires javafx.application.Platform;
    
    requires junit;
    
    requires mars-sim-ui;
    requires mars-sim-mapdata;
    
    requires com.almasb.fxgl.app;
    requires com.almasb.fxgl.settings;
      
	exports mars-sim-main;
}