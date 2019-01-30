module mars-sim-javafx {
	
    requires java.io;
    requires java.net;
    requires java.util;
    requires java.awt;
    requires javax.net.ssl;
    requires javax.swing;
    
    requires org.w3c.dom.Document;
    requires org.w3c.dom.Element;
    requires org.w3c.dom.Text;
    
    requires javafx.stage;
    requires javafx.application.Application;
    requires javafx.application.Platform;
    requires javafx.animation;
    requires javafx.application.Application;
    requires javafx.beans.property;
    requires javafx.collections;
    requires javafx.event;
    requires javafx.geometry;
    requires javafx.scene;
    requires javafx.util;
    requires javafx.embed.swing.JFXPanel;
    
    requires netscape.javascript.JSObject;
    
    requires junit;
//    requires javafxsvg;
    requires batik-transcoder;
    requires jfreechart;
    requires trident;  
    requires jorbis;
    requires nimrodlf;
    requires weblaf-core;
    requires weblaf-ui;
    requires reactfx;
    
    requires controlsfx;

    requires jide-oss;
// For Java 8, use the following one maven artifact    
//    requires fontawesomefx;
// For Java 9, use the following 3 maven artifacts    
    requires fontawesomefx-commons;
    requires fontawesomefx-fontawesome;
    requires fontawesomefx-materialdesignfon;
    requires tilesfx;
    requires Medusa;
    requires LibFX;
    requires undecorator;
    requires jfoenix;
    requires datafx;
    requires flow;
    requires core;
    requires gradle-retrolambda;
    requires centerdevice-nsmenufx;
    requires wellbehavedfx;
    requires jiconfont-javafx;
    requires jiconfont-font_awesome;
    
    requires com.almasb.fxgl;
    
    requires mars-sim-core;
    requires mars-sim-mapdata;
    requires mars-sim-ui;
    
	exports mars-sim-javafx;
}