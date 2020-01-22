module mars-sim-ui {
	
    requires java.io;
    requires java.net;
    requires java.util;
    requires java.awt;
    requires javax.net.ssl;
    requires javax.swing;
    
    requires org.w3c.dom.Document;
    requires org.w3c.dom.Element;
    requires org.w3c.dom.Text;
    
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
    requires weblaf-plugin;
    
    requires com.alee.managers.tooltip.TooltipManager;
    requires com.alee.managers.tooltip.TooltipWay;
     
    requires jide-oss;

    requires mars-sim-core;
    requires mars-sim-mapdata;
    requires mars-sim-console;
    
	exports mars-sim-ui;
}