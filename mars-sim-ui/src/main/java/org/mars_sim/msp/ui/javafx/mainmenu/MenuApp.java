/**
 * Mars Simulation Project
 * MenuApp.java
 * @version 3.1.0 2017-05-12
 * @author Manny KUng
 */

package org.mars_sim.msp.ui.javafx.mainmenu;

import javafx.animation.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.Arrays;
import java.util.List;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.ui.javafx.config.StarfieldFX;

import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.input.Input;

public class MenuApp {

    private static final int WIDTH = MainMenu.WIDTH;
    private static final int HEIGHT = MainMenu.HEIGHT;
    private static final int X_OFFSET = 10;
    private static final int Y_OFFSET = 240;

    private boolean isFXGL;

    private AnchorPane root = new AnchorPane();
    
    private StackPane titleStackPane;
    
    private VBox menuBox;
    private VBox modeBox;
    
    private HBox optionMenu = new HBox();
    
    private Line line;
    
    private MainMenu mainMenu;
    private SpinningGlobe spinningGlobe;   
    
    private List<Pair<String, Runnable>> menuChoice;
    private List<Pair<String, Runnable>> modeChoice;
    
    public MenuApp(MainMenu mainMenu, boolean isFXGL) {
    	
    	this.mainMenu = mainMenu;
    	this.isFXGL = isFXGL;
    	
    	//root.setMaxSize(WIDTH + 20, HEIGHT + 20);
    	//root.setMaxSize(WIDTH-10, HEIGHT-20);
    }
    
    public void setupMenuData() {
    	
    	menuChoice = Arrays.asList(
            new Pair<String, Runnable>("New Sim", () -> setupModeChoice()),// mainMenu.runNew(isFXGL)),
            new Pair<String, Runnable>("Load Sim", () -> mainMenu.runLoad(isFXGL)),
            //new Pair<String, Runnable>("Multiplayer", () -> {}),
            new Pair<String, Runnable>("Tutorial", () -> {}),
            new Pair<String, Runnable>("Benchmark", () -> {}),
            new Pair<String, Runnable>("Settings", () -> mainMenu.runSettings()),
            new Pair<String, Runnable>("Credits", () -> {}),
            new Pair<String, Runnable>("Exit", () -> {
            	//Platform::exit
            	if (isFXGL) {
    		        Input input = FXGL.getInput();
    				input.mockKeyPress(KeyCode.ESCAPE);
    		        input.mockKeyRelease(KeyCode.ESCAPE);
            	}
            	else {
            		MainMenu.dialogOnExit(mainMenu.getPane());
            	}
            })
        );
    }    
    
    public void selectMode() {
        int y = Y_OFFSET - 25;

    	StackPane pane = mainMenu.createCommanderPane();
    	
        clearLineItems();
    	clearModeBoxItems();
    	
        optionMenu.getChildren().add(pane);

        optionMenu.setTranslateX(WIDTH/1.45);//2.6);
        optionMenu.setTranslateY(y);

    }
    
    public void setupModeChoice() {
    	    	
    	endLineAnimation();
    	endMenuBoxAnimation();
    	
        clearLineItems();    	
    	clearMenuBoxItems();

    	mainMenu.removeEvenHandler();
    	
    	addLine(X_OFFSET, Y_OFFSET, 175);

    	modeBox = new VBox(-5);

        int x = X_OFFSET + 10;
        int y = Y_OFFSET - 25;

    	modeChoice = Arrays.asList(
            new Pair<String, Runnable>("Commander Mode", () -> selectMode()),	
            new Pair<String, Runnable>("Sandbox Mode", () -> mainMenu.runNew(isFXGL, false)),
            new Pair<String, Runnable>("Exit", () -> {
            	//Platform::exit
            	if (isFXGL) {
    		        Input input = FXGL.getInput();
    				input.mockKeyPress(KeyCode.ESCAPE);
    		        input.mockKeyRelease(KeyCode.ESCAPE);
            	}
            	else {
            		MainMenu.dialogOnExit(mainMenu.getPane());
            	}
            })
        );
    	
    	modeChoice.forEach(data -> {
    		int width = 280;
            MenuItem item = new MenuItem(data.getKey(), width);
            item.setOnAction(data.getValue());
            item.setTranslateX(-width);

            Rectangle clip = new Rectangle(width, 40);
            clip.translateXProperty().bind(item.translateXProperty().negate());

            item.setClip(clip);
            modeBox.getChildren().add(item);
        });
        
        modeBox.setTranslateX(x);
        optionMenu.getChildren().add(modeBox);
        optionMenu.setTranslateX(WIDTH/1.45);//2.6);
        optionMenu.setTranslateY(y);
            	
        startAnimation(modeBox);
		
        startBoxAnimation();
    }    
 
    
    public AnchorPane createContent() {
    	addStarfield();
        addGlobe();     
        addTitle();
        
        return root;
    }
    
    public void addLineMenuBox() {
    	addLine(X_OFFSET, Y_OFFSET, 430);
    	addMenuBox(X_OFFSET + 10, Y_OFFSET - 25);	
    }
    	
//    private void addRect() {
//        Rectangle rect = new Rectangle(WIDTH, HEIGHT);
//        rect.setFill(Color.rgb(0, 0, 0, .80));
//        root.getChildren().add(rect);
//    }

    private void addStarfield() {
        StarfieldFX sf = new StarfieldFX();
        Parent starfield = sf.createStars(WIDTH-5, HEIGHT-5);
        root.getChildren().add(starfield);
    }

    private void addGlobe() {
    	spinningGlobe = new SpinningGlobe(mainMenu);
        Parent globe = spinningGlobe.createDraggingGlobe();   
        //globe.setTranslateX(20);//WIDTH/3D - SpinningGlobe.WIDTH/2D);// + 40);
        //globe.setTranslateY(50);
        root.getChildren().add(globe);	
    }
    
    
//    private void addBackground() {
//        ImageView imageView = new ImageView(new Image(this.getClass().getResource("/images/mainMenu/mars.jpg").toExternalForm()));
//        imageView.setFitWidth(WIDTH);
//        imageView.setFitHeight(HEIGHT);
//
//        root.getChildren().add(imageView);
//    }

    private void addTitle() {
        MenuTitle title = new MenuTitle("Mars Simulation Project", 36, Color.LIGHTGOLDENRODYELLOW, true);//DARKGOLDENROD);
        title.setTranslateX(WIDTH / 2 - title.getTitleWidth() / 2);
        title.setTranslateY(44);
        
        MenuTitle version = new MenuTitle("Version " + Simulation.VERSION, 18, Color.DARKGOLDENROD, false);//DARKORANGE);//.DARKGOLDENROD);//.LIGHTGRAY);//.GRAY); BLACK);//.DARKGRAY);/
        version.setTranslateX(WIDTH / 2 - version.getTitleWidth() / 2);
        version.setTranslateY(80);
        
        MenuTitle build = new MenuTitle("Build " + Simulation.BUILD, 14, Color.DARKGOLDENROD, false);//DARKORANGE);//.DARKGOLDENROD);//.LIGHTGRAY);//.GRAY); BLACK);//.DARKGRAY);/
        build.setTranslateX(15);
        build.setTranslateY(HEIGHT - 10);

        MenuTitle year = new MenuTitle("All Rights Reserved, 2018", 14, Color.DARKGOLDENROD, false);//DARKORANGE);//.DARKGOLDENROD);//.LIGHTGRAY);//.GRAY); BLACK);//.DARKGRAY);/
        year.setTranslateX(WIDTH - year.getTitleWidth() - 10);
        year.setTranslateY(HEIGHT - 10);
     
        MenuTitle site = new MenuTitle("https://mars-sim.github.io/", 16, Color.DARKGOLDENROD, false);//DARKORANGE);//.DARKGOLDENROD);//.LIGHTGRAY);//.GRAY); BLACK);//.DARKGRAY);/
        site.setTranslateX((WIDTH - site.getTitleWidth())/2);
        site.setTranslateY(HEIGHT - 10);
        
        titleStackPane = new StackPane(title, version, year, site, build, optionMenu);
        root.getChildren().addAll(titleStackPane);
        
    }

    
    private void addLine(double x, double y, double yLength) {
    	if (line == null) {
	        line = new Line(x, y, x, y + yLength);
	        line.setStrokeWidth(2);
	        line.setStroke(Color.DARKGOLDENROD);//LIGHTGOLDENRODYELLOW);//color(139D/255D, 69D/255D, 19D/255D));//.LIGHTSALMON);//.LIGHTSLATEGRAY);//.CORAL);//.LIGHTGOLDENRODYELLOW);//color(1, 1, 1, 0.75));
	        line.setEffect(new DropShadow(8, Color.BLACK));
	        //line.setEffect(new BoxBlur(1, 1, 3));
	        line.setScaleY(0);
	        
	        optionMenu.getChildren().add(line);
    	}
    }

    private void addMenuBox(double x, double y) {
    	if (menuBox == null) {
	    	menuBox = new VBox(-5);	        

	    	if (menuChoice == null) {
		    	setupMenuData();
	
		    	menuChoice.forEach(data -> {
		       		int width = 250;
	
		            MenuItem item = new MenuItem(data.getKey(), width);
		            item.setOnAction(data.getValue());
		            item.setTranslateX(-width);
		
		            Rectangle clip = new Rectangle(width, 40);
		            clip.translateXProperty().bind(item.translateXProperty().negate());
		
		            item.setClip(clip);
		            menuBox.getChildren().add(item);
		        });
	    	}
	        
	        menuBox.setTranslateX(x);
	        //menuBox.setTranslateY(y);
	        optionMenu.getChildren().add(menuBox);
	        optionMenu.setTranslateX(WIDTH/1.45);//2.6);
	        optionMenu.setTranslateY(y);
	        
	//        boolean flag = false;
	//	    for (Node node : root.getChildren()) {
	//	    	if (node == optionMenu) {
	//	    		flag = true;
	//	    		break;
	//	    	}
	//	    }
	//    	if (!flag)
	//    		root.getChildren().add(optionMenu);
    	}    	
    }

    public void startBoxAnimation() {
		FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), getOptionMenu());
		fadeTransition.setFromValue(0.0);
		fadeTransition.setToValue(1.0);
		fadeTransition.play();
    }
    
    public void startAnimation(VBox currentBox) {
        ScaleTransition st = new ScaleTransition(Duration.seconds(1), line);
        st.setToY(1);
        
        st.setOnFinished(e -> {
        	int size = currentBox.getChildren().size();
            for (int i = 0; i < size; i++) {
                Node n = currentBox.getChildren().get(i);

                TranslateTransition tt = new TranslateTransition(Duration.seconds(1 + i * 0.15), n);
                tt.setToX(0);
                tt.setOnFinished(e2 -> n.setClip(null));
                tt.play();
            }
        });
        st.play();
    }

    public void endLineAnimation() {
        ScaleTransition st = new ScaleTransition(Duration.seconds(.5), line);
        st.setToY(0);
        st.play();
//        st.setOnFinished(e -> clearLineItems());
//        clearLineItems();
    }
    
    public void endMenuBoxAnimation() {		
		FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), getOptionMenu());
		fadeTransition.setFromValue(1.0);
		fadeTransition.setToValue(0.0);
		fadeTransition.play();
//		fadeTransition.setOnFinished(e -> clearMenuBoxItems());	
//		clearMenuBoxItems();
    }

    public void clearLineItems() {
        if (line != null) {
	    	optionMenu.getChildren().remove(line);
	        line = null;
        }
    }
    
    public void clearMenuBoxItems() {
        
        if (menuChoice != null) {
		    menuBox.getChildren().removeAll(menuChoice);
		    //menuBox.forEach(data -> {
	        //    menuBox.getChildren().remove(menuData);
	        //});
	        menuChoice = null;
        }

        if (menuBox != null) {
	        optionMenu.getChildren().remove(menuBox);
	        menuBox = null;
        }
//        addLine(X_OFFSET, Y_OFFSET);
//        addMenuBox(X_OFFSET + 10, Y_OFFSET - 25);	
    }

    public void clearModeBoxItems() {
        
        if (modeChoice != null) {
		    modeBox.getChildren().removeAll(modeChoice);
		    //menuBox.forEach(data -> {
	        //    menuBox.getChildren().remove(menuData);
	        //});
		    modeChoice = null;
        }

        if (modeBox != null) {
	        optionMenu.getChildren().remove(modeBox);
	        modeBox = null;
        }
//        addLine(X_OFFSET, Y_OFFSET);
//        addMenuBox(X_OFFSET + 10, Y_OFFSET - 25);	
    }

    public HBox getOptionMenu() {
    	return optionMenu;
    }
    
    public Pane getRoot() {
    	return root;
    }

    public Pane getTitleStackPane() {
    	return titleStackPane;
    }
    
    public SpinningGlobe getSpinningGlobe() {
    	return spinningGlobe;
    }
    
    public List<Pair<String, Runnable>> getMenuData() {
    	return menuChoice;
    }
    
    public VBox getMenuBox() {
    	return menuBox;
    }

    public VBox getModeBox() {
    	return modeBox;
    }

	public void destroy() {
		root = null;
	    titleStackPane = null;
	    menuBox = null;
	    optionMenu = null;
	    line = null;    
	    mainMenu = null;
	    spinningGlobe = null;     
	    menuChoice = null;
	}
}
