module mars-sim-network {
	
    requires java.io;
    requires java.net;
    requires java.util;
    requires java.awt;
    requires java.time;
    requires java.text;
    
    requires javax.imageio.ImageIO;

    requires javafx.animation.KeyFrame;
    requires javafx.animation.Timeline;
    
    requires javafx.application.Application;
    requires javafx.application.Platform;

    requires javafx.util.Duration;

    requires javafx.geometry.Pos;
    requires javafx.scene.Node;
    requires javafx.scene.Scene;
    requires javafx.scene.control.TextArea;
    requires javafx.scene.control.Alert;
    requires javafx.scene.control.Label;
    requires javafx.scene.control.ButtonType;
    requires javafx.scene.control.Alert.AlertType;
    requires javafx.scene.image.Image;
    requires javafx.scene.image.ImageView;
    requires javafx.scene.layout.StackPane;
    requires javafx.scene.layout.VBox;
    requires javafx.scene.paint.Color;
    requires javafx.stage.Modality;
    requires javafx.stage.Stage;
    requires javafx.stage.StageStyle;

    requires junit;

//    requires mars-sim-core;
//    requires mars-sim-mapdata;
    
	exports mars-sim-network;
}