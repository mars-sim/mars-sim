/*
 * Mars Simulation Project
 * First.java
 * @date 2022-11-25
 * @author Manny Kung
 */

package org.mars_sim.fxgl;

import static com.almasb.fxgl.dsl.FXGL.addUINode;
import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.getAssetLoader;
import static com.almasb.fxgl.dsl.FXGL.getGameScene;
import static com.almasb.fxgl.dsl.FXGL.getPhysicsWorld;
import static com.almasb.fxgl.dsl.FXGL.getSaveLoadService;
import static com.almasb.fxgl.dsl.FXGL.getSceneService;
import static com.almasb.fxgl.dsl.FXGL.getUIFactoryService;
import static com.almasb.fxgl.dsl.FXGL.getd;
import static com.almasb.fxgl.dsl.FXGL.getdp;
import static com.almasb.fxgl.dsl.FXGL.getip;
import static com.almasb.fxgl.dsl.FXGL.inc;
import static com.almasb.fxgl.dsl.FXGL.onKeyDown;
import static com.almasb.fxgl.dsl.FXGL.play;
import static com.almasb.fxgl.dsl.FXGL.run;
import static com.almasb.fxgl.dsl.FXGL.set;

import java.util.EnumSet;
import java.util.Map;
import java.util.logging.Logger;

import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.MenuItem;
import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.inventory.Inventory;
import com.almasb.fxgl.inventory.view.InventoryView;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.profile.DataFile;
import com.almasb.fxgl.profile.SaveLoadHandler;
import com.almasb.fxgl.scene.SubScene;

import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.util.Duration;


public class MarsWorld {
    
//    private GameWorld world = FXGL.getGameWorld();
    
    /** initialized logger for this class. */
    private static Logger logger = Logger.getLogger(MarsWorld.class.getName());
    
    private static final int POP_X = 180;
    
    private static final String TIME = "time";
    private static final String POP = "pop";
    
    private static final String IDLE_BUTTON_STYLE = "-fx-background-color: transparent; -fx-text-fill: black";
    private static final String HOVERED_BUTTON_STYLE = "-fx-background-color: transparent, -fx-outer-border, -fx-inner-border, -fx-body-color; -fx-text-fill: black";


    private Entity woodEntity = new Entity();
	private Entity commander;
	private Entity settlement;
	
	private Entity greenhouse;
	private Entity hab;
	private Entity workshop;

    public enum EntityType {
        COMMANDER, SETTLEMENT, HAB, GREENHOUSE, WORKSHOP
    }

    private PopSubScene popSubScene;
    
	public void initSettings(GameSettings settings) {
		settings.setWidth(1366);
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
        
        run(() -> inc(TIME, 0.025), Duration.seconds(.25));

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
                    
                    play("drop.wav");
                    
                    logger.info(message + ": " + answer);
                    settlement.removeFromWorld();
                    
                    FXGL.getNotificationService().pushNotification("You are inside a settlement.");
                    
                    // Enter a settlement
                    initInside();
                });
            }
        });
    }

    public void initInput() {

        popSubScene = new PopSubScene();

        popSubScene.getInput().addAction(new UserAction("Close Pop") {
            @Override
            protected void onActionBegin() {
                getSceneService().popSubScene();
            }
        }, KeyCode.P);

        onKeyDown(KeyCode.P, "Open Pop", () -> {
                play("drop.wav");
                getSceneService().pushSubScene(popSubScene);
            });
        
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
    }


    public void initUI() {
        var clockText = getUIFactoryService().newText("", Color.BLACK, 18.0);
        clockText.textProperty().bind(getdp(TIME).asString("Sol: %.3f"));
        clockText.setStyle("-fx-background-color: yellow");
        clockText.setStyle("-fx-text-fill: black");
        addUINode(clockText, 10, 20);
        
//        var popText = getUIFactoryService().newText("", Color.BLACK, 18.0);
//        popText.textProperty().bind(FXGL.getWorldProperties().intProperty(POP).asString("Pop: %d"));
//        popText.setStyle("-fx-background-color: cyan");

        var popText = getUIFactoryService().newButton(getip(POP).asString("Pop: %d"));
        popText.textProperty().bind(getip(POP).asString("Pop: %d"));
//        popText.setStyle("-fx-font-weight: bold");
        popText.setStyle("-fx-font-size: 14pt");
        popText.setStyle("-fx-text-fill: black");
//        popText.setStyle("-fx-border-color:yellow; -fx-padding:0px;");
        

        popText.setStyle(IDLE_BUTTON_STYLE);
        popText.setOnMouseEntered(e -> popText.setStyle(HOVERED_BUTTON_STYLE));
        popText.setOnMouseExited(e -> popText.setStyle(IDLE_BUTTON_STYLE));
        
        popText.setOnAction(e -> {
                play("drop.wav");
                logger.config("is in : " + getSceneService().isInHierarchy(popSubScene));
                getSceneService().pushSubScene(popSubScene);
            });
     
        addUINode(popText, POP_X, 0);
//        pickupItem(woodEntity, POP, "Pop Modifiers", 1));

    }
    
    public void pickupItem(Entity item, String name, String description, int quantity) {
        if (getip(name.toLowerCase()).get() > 0) {
            popSubScene.playerInventory.add(item, name, description, popSubScene.view, quantity);
//            inc(name.toLowerCase(), 1);
            popSubScene.view.getListView().refresh();
        }
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
        vars.put(TIME, 0.0);
        vars.put(POP, 1000);
    }


    public void onUpdate(double tpf) {
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

    public void onPreInit() {
        getSaveLoadService().addHandler(new SaveLoadHandler() {
            @Override
            public void onSave(DataFile data) {
                // create a new bundle to store your data
                var bundle = new Bundle("gameData");

                // store some data
                double time = getd(TIME);
                bundle.put(TIME, time);
                
                int pop = FXGL.geti(POP);
                bundle.put(POP, pop);

                // give the bundle to data file
                data.putBundle(bundle);
            }

            @Override
            public void onLoad(DataFile data) {
                // get your previously saved bundle
                var bundle = data.getBundle("gameData");

                // retrieve some data
                double time = bundle.get(TIME);
                // update your game with saved data
                set(TIME, time);
                
                // retrieve some data
                int pop = bundle.get(POP);
                // update your game with saved data
                set(POP, pop);
            }
        });
    }
    
    private class PopSubScene extends SubScene {

        public Inventory<Entity> playerInventory = new Inventory<Entity>(10);

        public InventoryView<Entity> view = new InventoryView<>(playerInventory);

        public PopSubScene() {
            getContentRoot().getChildren().addAll(view);
            getContentRoot().setTranslateX(POP_X - 25);
            getContentRoot().setTranslateY(15);

            Button dropOne = getUIFactoryService().newButton("Button 0");
            dropOne.prefHeight(30.0);
            dropOne.prefWidth(135.0);
            dropOne.setTranslateX(35.0);
            dropOne.setTranslateY(320.0);

            dropOne.setOnAction(actionEvent -> {
                var selectedItem = (Entity) view.getListView().getSelectionModel().getSelectedItem();

                if (selectedItem != null) {
                    var item = popSubScene.playerInventory.getData((Entity) selectedItem).get(0).getUserItem();
                    playerInventory.incrementQuantity(item, -1);
                }
                view.getListView().refresh();
            });

            Button dropAll = getUIFactoryService().newButton("Button 1");
            dropAll.prefHeight(30.0);
            dropAll.prefWidth(135.0);
            dropAll.setTranslateX(35.0);
            dropAll.setTranslateY(370.0);

            dropAll.setOnAction(actionEvent -> {

                var selectedItem = (Entity) view.getListView().getSelectionModel().getSelectedItem();

                if (selectedItem != null) {
                    var itemData = popSubScene.playerInventory.getData((Entity) selectedItem).get(0).getUserItem();
                    playerInventory.remove(itemData);
                }
                view.getListView().refresh();
            });

            this.getContentRoot().getChildren().addAll(dropOne, dropAll);
        }
    }
}
