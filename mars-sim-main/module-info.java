module mars-sim-main {

    requires java.awt;
    requires java.io;
    requires java.net;
    requires java.util;
    requires java.text;
    
    requires javax.swing;
    
    requires junit;

    requires mars-sim-core;
    requires mars-sim-ui;
//    requires mars-sim-console;
    requires mars-sim-headless;
      
	exports mars-sim-main;
}