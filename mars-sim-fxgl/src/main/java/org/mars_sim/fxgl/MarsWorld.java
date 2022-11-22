/*
 * Mars Simulation Project
 * First.java
 * @date 2022-11-21
 * @author Manny Kung
 */

package org.mars_sim.fxgl;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.getAssetLoader;
import static com.almasb.fxgl.dsl.FXGL.getGameScene;
import static com.almasb.fxgl.dsl.FXGL.getPhysicsWorld;
import static com.almasb.fxgl.dsl.FXGL.getWorldProperties;
import static com.almasb.fxgl.dsl.FXGL.onKeyDown;
import static com.almasb.fxgl.dsl.FXGL.play;

import java.util.EnumSet;
import java.util.Map;

import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.MenuItem;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.CollisionHandler;

import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;


public class MarsWorld {
    
//    private GameWorld world = FXGL.getGameWorld();
    
	private Entity commander;
	private Entity settlement;
	
	private Entity greenhouse;
	private Entity hab;
	private Entity workshop;

    public enum EntityType {
        COMMANDER, SETTLEMENT, HAB, GREENHOUSE, WORKSHOP
    }

	public void initSettings(GameSettings settings) {
		settings.setWidth(1366);// 1024);
		settings.setHeight(768);
//		 settings.setStageStyle(StageStyle.UNDECORATED);
		settings.setTitle(" Mars Simulation Project ");
		settings.setVersion("3.5.0");
		settings.setProfilingEnabled(false); // turn off fps
		settings.setCloseConfirmation(false); // turn off exit dialog
		settings.setIntroEnabled(false); // turn off intro
//		settings.setMenuEnabled(false); // turn off menus
		settings.setCloseConfirmation(true);
        settings.setMainMenuEnabled(true);
        settings.setEnabledMenuItems(EnumSet.allOf(MenuItem.class));
	}

    public void initGame() {
        
//    	player = FXGL.entityBuilder()
//                .at(400, 300)
////                .view(new Rectangle(40, 40, Color.BLUE))
//                // 3. add a new instance of component to entity
////                .with(new RotatingComponent())
//                .view("brick.png")
//                .buildAndAttach();

    	commander = entityBuilder()
                .type(EntityType.COMMANDER)
                .at(150, 150)
                .viewWithBBox("astronaut_24.png")
                .with(new CollidableComponent(true))
                .buildAndAttach();

        settlement = entityBuilder()
                .type(EntityType.SETTLEMENT)
                .at(300, 300)
                .viewWithBBox("colony_24.png")
                .with(new CollidableComponent(true))
                .buildAndAttach();
    }
    
    public void initSettlement() {
        hab = entityBuilder()
                .type(EntityType.HAB)
                .at(300, 300)
                .viewWithBBox("lander_hab_64.png")
                .scale(2, 2)
                .with(new CollidableComponent(true))
                .buildAndAttach();
        
        greenhouse = entityBuilder()
                .type(EntityType.GREENHOUSE)
                .at(450, 300)
                .viewWithBBox("garden_64.png")
                .scale(2, 2)
                .with(new CollidableComponent(true))
                .buildAndAttach();
        
        workshop = entityBuilder()
                .type(EntityType.WORKSHOP)
                .at(600, 300)
                .viewWithBBox("tools_64.png")
                .scale(2, 2)
                .with(new CollidableComponent(true))
                .buildAndAttach();
        
//        entityBuilder()
//                .type(EntityType.COIN)
//                .at(500, 200)
//                .viewWithBBox(new Circle(15, 15, 15, Color.YELLOW))
//                .with(new CollidableComponent(true))
//                .buildAndAttach();
        
//        world.addEntity(commander);
//        world.addEntity(settlement);
    }

	public void initPhysics() {
        getPhysicsWorld().addCollisionHandler(new CollisionHandler(
                EntityType.COMMANDER, 
                EntityType.SETTLEMENT
                ) {

            // order of types is the same as passed into the constructor
            @Override
            protected void onCollisionBegin(Entity commander, Entity settlement) {
                String message = "Entering this settlement";
                
//                FXGL.getDialogService().showMessageBox(message, () -> {
//                    // code to run after dialog is dismissed
//                });
                
                FXGL.getDialogService().showConfirmationBox("Do you want to enter this settlement ?", answer -> {
                    System.out.println(message + ": " + answer);
                    settlement.removeFromWorld();

                    FXGL.getNotificationService().pushNotification("You are inside a settlement.");
                    
                    // Enter a settlement
//                    initSettlement();
//                    world.addEntity(hab);
//                    world.addEntity(greenhouse);
                    initInside();
                });
            }
        });
    }

    public void initInput() {

        FXGL.onKey(KeyCode.D, () -> {
            commander.translateX(2.5); // move right 5 pixels
        });

        FXGL.onKey(KeyCode.A, () -> {
            commander.translateX(-2.5); // move left 5 pixels
        });

        FXGL.onKey(KeyCode.W, () -> {
            commander.translateY(-2.5); // move up 5 pixels
        });

        FXGL.onKey(KeyCode.S, () -> {
            commander.translateY(2.5); // move down 5 pixels
        });

        onKeyDown(KeyCode.F, () -> {
            play("drop.wav");
        });
    }


    public void initUI() {
//        Text textPixels = new Text();
//        textPixels.setTranslateX(10);
//        textPixels.setTranslateY(20);
//
//        textPixels.textProperty().bind(getWorldProperties().intProperty("pixelsMoved").asString());
//
//        getGameScene().addUINode(textPixels); // add to the scene graph
    }
    
    
    public void initOutside() {   
        var commanderTexture = getAssetLoader().loadTexture("astronaut_24.png");
        commanderTexture.setTranslateX(50);
        commanderTexture.setTranslateY(150);
        getGameScene().addUINode(commanderTexture);
        
        var settlementTexture = getAssetLoader().loadTexture("colony_24.png");
        settlementTexture.setTranslateX(300);
        settlementTexture.setTranslateY(300);
        getGameScene().addUINode(settlementTexture);
    }
    
    public void initInside() {
        var habTexture = getAssetLoader().loadTexture("lander_hab_64.png");
        habTexture.setTranslateX(300);
        habTexture.setTranslateY(300);
        getGameScene().addUINode(habTexture);
        
        var greenTexture = getAssetLoader().loadTexture("garden_64.png");
        greenTexture.setTranslateX(450);
        greenTexture.setTranslateY(300);
        getGameScene().addUINode(greenTexture);
        
        var workshopTexture = getAssetLoader().loadTexture("tools_64.png");
        workshopTexture.setTranslateX(600);
        workshopTexture.setTranslateY(300);
        getGameScene().addUINode(workshopTexture);
    }

    public void initGameVars(Map<String, Object> vars) {
//        vars.put("pixelsMoved", 0);
    }


    public void onUpdate(double tpf) {
    	;
	}

//    // 1. create class that extends Component
//    // Note: ideally in a separate file. It's included in this file for clarity.
//    private static class RotatingComponent extends Component {
//
//        @Override
//        public void onUpdate(double tpf) {
//            // 2. specify behavior of the entity enforced by this component
//            entity.rotateBy(tpf * 45);
//        }
//    }
}
