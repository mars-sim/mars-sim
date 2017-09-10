/**
 * Mars Simulation Project
 * MenuApp.java
 * @version 3.1.0 2017-05-12
 * @author Manny KUng
 */

package org.mars_sim.msp.ui.javafx.mainmenu;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Pos;
//import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
//import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
//import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.Arrays;
import java.util.List;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.ui.javafx.config.StarfieldFX;

@SuppressWarnings("restriction")
public class MenuApp extends Application {

    private static final int WIDTH = MainMenu.WIDTH;
    private static final int HEIGHT = MainMenu.HEIGHT;
    private static final int X_OFFSET = 10;
    private static final int Y_OFFSET = 240;


    private Pane root = new Pane();
    private VBox menuBox;
    private HBox optionMenu = new HBox();
    private Line line;
    

    private MainMenu mainMenu;
    private SpinningGlobe spinningGlobe;   
    
    private List<Pair<String, Runnable>> menuData;
    
    public MenuApp(MainMenu mainMenu) {
    	
    	this.mainMenu = mainMenu;
  
    }
    
    public void setupMenuData() {
    	
    	menuData = Arrays.asList(
                new Pair<String, Runnable>("New Sim", () -> mainMenu.runOne()),
                new Pair<String, Runnable>("Load Sim", () -> mainMenu.runTwo()),
                //new Pair<String, Runnable>("Multiplayer", () -> {}),
                new Pair<String, Runnable>("Tutorial", () -> {}),
                new Pair<String, Runnable>("Benchmark", () -> {}),
                new Pair<String, Runnable>("Game Options", () -> {}),
                new Pair<String, Runnable>("Credits", () -> {}),
                new Pair<String, Runnable>("Exit to Desktop", () -> mainMenu.exitDialog(mainMenu.getStage()))   //Platform::exit)
        );
    }    
    
    Parent createContent() {
        //addBackground();
    	addRect();
    	addStarfield();
        addTitle();
        addGlobe();
        
        addLine(X_OFFSET, Y_OFFSET);
        addMenu(X_OFFSET + 10, Y_OFFSET - 25);
        //startAnimation();
        addFooter();
        
        return root;
    }

    private void addRect() {
        Rectangle rect = new Rectangle(WIDTH, HEIGHT);
        
        root.getChildren().add(rect);
    }

    private void addStarfield() {
        StarfieldFX sf = new StarfieldFX();
        Parent starfield = sf.createStars(WIDTH-2, HEIGHT-2);

        root.getChildren().add(starfield);
    }

    private void addGlobe() {
    	spinningGlobe = new SpinningGlobe(mainMenu);
        Parent globe = spinningGlobe.createDraggingGlobe();   
        globe.setTranslateX(WIDTH/3D - SpinningGlobe.WIDTH/2D);
        globe.setTranslateY(50);
 
        root.getChildren().add(globe);	
    }
    
    private void addBackground() {
        ImageView imageView = new ImageView(new Image(this.getClass().getResource("/images/mainMenu/mars.jpg").toExternalForm()));
        imageView.setFitWidth(WIDTH);
        imageView.setFitHeight(HEIGHT);

        root.getChildren().add(imageView);
    }

    private void addTitle() {
        MenuTitle title = new MenuTitle("Mars Simulation Project", 36, Color.LIGHTGOLDENRODYELLOW, true);//DARKGOLDENROD);
        title.setTranslateX(WIDTH / 2 - title.getTitleWidth() / 2);
        title.setTranslateY(44);
        
        MenuTitle version = new MenuTitle("Version " + Simulation.VERSION, 18, Color.DARKGOLDENROD, true);//DARKORANGE);//.DARKGOLDENROD);//.LIGHTGRAY);//.GRAY); BLACK);//.DARKGRAY);/
        version.setTranslateX(WIDTH / 2 - version.getTitleWidth() / 2);
        version.setTranslateY(80);
        
        root.getChildren().addAll(title, version);
        
    }

    private void addFooter() {

        MenuTitle build = new MenuTitle("Build " + Simulation.BUILD + "", 16, Color.BURLYWOOD, true);//DARKORANGE);//.DARKGOLDENROD);//.LIGHTGRAY);//.GRAY); BLACK);//.DARKGRAY);/
        build.setTranslateX(WIDTH - build.getTitleWidth() - 10);
        build.setTranslateY(HEIGHT-15);
        
        root.getChildren().addAll(build);
        
    }
    private void addLine(double x, double y) {
        line = new Line(x, y, x, y + 430);
        line.setStrokeWidth(2);
        line.setStroke(Color.DARKGOLDENROD);//LIGHTGOLDENRODYELLOW);//color(139D/255D, 69D/255D, 19D/255D));//.LIGHTSALMON);//.LIGHTSLATEGRAY);//.CORAL);//.LIGHTGOLDENRODYELLOW);//color(1, 1, 1, 0.75));
        line.setEffect(new DropShadow(8, Color.BLACK));
        //line.setEffect(new BoxBlur(1, 1, 3));
        line.setScaleY(0);

        optionMenu.getChildren().add(line);
        //root.getChildren().add(line);
    }

    private void addMenu(double x, double y) {
    	setupMenuData();
    	
    	menuBox = new VBox(-5);
        menuData.forEach(data -> {
            MenuItem item = new MenuItem(data.getKey());
            item.setOnAction(data.getValue());
            item.setTranslateX(-300);

            Rectangle clip = new Rectangle(300, 40);
            clip.translateXProperty().bind(item.translateXProperty().negate());

            item.setClip(clip);
            menuBox.getChildren().add(item);
        });
        
        menuBox.setTranslateX(x);
        //menuBox.setTranslateY(y);
        optionMenu.getChildren().add(menuBox);
        optionMenu.setTranslateX(WIDTH/1.45);//2.6);
        optionMenu.setTranslateY(y);
        
        boolean flag = false;
	    for (Node node : root.getChildren()) {
	    	if (node == optionMenu) {
	    		flag = true;
	    		break;
	    	}
	    }
    	if (!flag)
        	root.getChildren().add(optionMenu);
    	
    }

    
    void startAnimation() {
        ScaleTransition st = new ScaleTransition(Duration.seconds(1), line);
        st.setToY(1);
        st.setOnFinished(e -> {

        	int size = menuBox.getChildren().size();
            for (int i = 0; i < size; i++) {
                Node n = menuBox.getChildren().get(i);

                TranslateTransition tt = new TranslateTransition(Duration.seconds(1 + i * 0.15), n);
                tt.setToX(0);
                tt.setOnFinished(e2 -> n.setClip(null));
                tt.play();
            }
        });
        st.play();
    }

    void endAnimation() {
        ScaleTransition st = new ScaleTransition(Duration.seconds(.5), line);
        st.setToY(0);
        st.play();
        st.setOnFinished(e -> {  
        });
    }
 
    void clearMenuItems() {
    	optionMenu.getChildren().remove(line);
        line = null;
	    menuBox.getChildren().removeAll(menuData);
        //menuData.forEach(data -> {
        //    menuBox.getChildren().remove(menuData);
        //});
        menuData = null;
        optionMenu.getChildren().remove(menuBox);
        menuBox = null;
        addLine(X_OFFSET, Y_OFFSET);
        addMenu(X_OFFSET + 10, Y_OFFSET - 25);	
    }
    
    public HBox getOptionMenu() {
    	return optionMenu;
    }
    
    public Pane getRoot() {
    	return root;
    }
    
    public SpinningGlobe getSpinningGlobe() {
    	return spinningGlobe;
    }
    
    public List<Pair<String, Runnable>> getMenuData() {
    	return menuData;
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(createContent());
        primaryStage.setTitle("mars-sim Main Menu");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
