package org.mars_sim.javafx.tools;

import java.util.Calendar;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
 
/**
*
* @author Lawrence PremKumar
*/
 
public class TodaysDateWiget extends Application {
 
    final double WIDTH = 150.0;
    final double HEIGHT = 150.0;
 
    public static void main(String[] args) {
        Application.launch(TodaysDateWiget.class, args);
    }
 
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello World");
        Group root = new Group();
        Scene scene = new Scene(root, 300, 250, Color.LIGHTGREEN);
        Button btn = new Button();
        btn.setLayoutX(100);
        btn.setLayoutY(80);
        btn.setText("Hello World");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                Stage st = new Stage(StageStyle.UNDECORATED);
                Group group = new Group();
                Scene s = new Scene(group ,WIDTH+3,HEIGHT+5);
                s.setFill(null);
                group.getChildren().addAll(getToDayControl());
                st.setScene(s);
                st.show();
 
            }
        });
        btn.fire();
        root.getChildren().add(btn);
        primaryStage.setScene(scene);
        //primaryStage.setVisible(false);
        primaryStage.close();
    }
 
    public Group getToDayControl(){
        String[] months = {"Jan", "Feb","Mar", "Apr", "May",          "Jun", "Jul","Aug", "Sep", "Oct", "Nov","Dec"};
        Calendar cal =  Calendar.getInstance();
 
        Group ctrl = new Group();
        Rectangle rect = new Rectangle();
        rect.setWidth(WIDTH);
        rect.setHeight(HEIGHT);
        rect.setArcHeight(10.0);
        rect.setArcWidth(10.0);
 
        Rectangle headerRect = new Rectangle();
        headerRect.setWidth(WIDTH);
        headerRect.setHeight(30);
        headerRect.setArcHeight(10.0);
        headerRect.setArcWidth(10.0);
 
        Stop[] stops = new Stop[] { new Stop(0, Color.color(0.31, 0.31, 0.31, 0.443)), new Stop(1,  Color.color(0, 0, 0, 0.737))};
        LinearGradient lg = new LinearGradient( 0.482, -0.017, 0.518, 1.017, true, CycleMethod.REFLECT, stops);
        headerRect.setFill(lg);
 
        Rectangle footerRect = new Rectangle();
        footerRect.setY(headerRect.getBoundsInLocal().getHeight() -4);
        footerRect.setWidth(WIDTH);
        footerRect.setHeight(125);
        footerRect.setFill(Color.color(0.51,  0.671,  0.992));
 
        final Text currentMon = new Text(months[(cal.get(Calendar.MONTH) )]);
        currentMon.setFont(Font.font("null", FontWeight.BOLD, 24));
        currentMon.setTranslateX((footerRect.getBoundsInLocal().getWidth() - currentMon.getBoundsInLocal().getWidth())/2.0);
        currentMon.setTranslateY(23);
        currentMon.setFill(Color.WHITE);
 
        final Text currentDate = new          Text(Integer.toString(cal.get(Calendar.DATE)));
        currentDate.setFont(new Font(100.0));
        currentDate.setTranslateX((footerRect.getBoundsInLocal().getWidth() - currentDate.getBoundsInLocal().getWidth())/2.0);
        currentDate.setTranslateY(120);
        currentDate.setFill(Color.WHITE);
 
        ctrl.getChildren().addAll(rect, headerRect, footerRect , currentMon,currentDate);
 
        DropShadow ds = new DropShadow();
        ds.setOffsetY(3.0);
        ds.setOffsetX(3.0);
        ds.setColor(Color.GRAY);
        ctrl.setEffect(ds);
 
        return ctrl;
    }
}