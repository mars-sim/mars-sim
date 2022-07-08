/*
 * Mars Simulation Project
 * First.java
 * @date 2022-07-07
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

import java.util.Map;

import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.CollisionHandler;

import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class First {

	private Entity player;

    public enum EntityType {
        PLAYER, COIN
    }

	public void initSettings(GameSettings settings) {
		settings.setWidth(1366);// 1024);
		settings.setHeight(768);
//		 settings.setStageStyle(StageStyle.UNDECORATED);
		settings.setTitle("  Mars Simulation Project  ");
		settings.setVersion("3.4.0");
		settings.setProfilingEnabled(false); // turn off fps
		settings.setCloseConfirmation(false); // turn off exit dialog
		settings.setIntroEnabled(false); // turn off intro
//		settings.setMenuEnabled(false); // turn off menus
		settings.setCloseConfirmation(true);
	}

    public void initGame() {
//    	player = FXGL.entityBuilder()
//                .at(400, 300)
////                .view(new Rectangle(40, 40, Color.BLUE))
//                // 3. add a new instance of component to entity
////                .with(new RotatingComponent())
//                .view("brick.png")
//                .buildAndAttach();

    	player = entityBuilder()
                .type(EntityType.PLAYER)
                .at(300, 300)
                .viewWithBBox("brick.png")
                .with(new CollidableComponent(true))
                .buildAndAttach();

        entityBuilder()
                .type(EntityType.COIN)
                .at(500, 200)
                .viewWithBBox(new Circle(15, 15, 15, Color.YELLOW))
                .with(new CollidableComponent(true))
                .buildAndAttach();
    }

	public void initPhysics() {
        getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.COIN) {

            // order of types is the same as passed into the constructor
            @Override
            protected void onCollisionBegin(Entity player, Entity coin) {
                coin.removeFromWorld();
            }
        });
    }

    public void initInput() {

        FXGL.onKey(KeyCode.D, () -> {
            player.translateX(5); // move right 5 pixels
        });

        FXGL.onKey(KeyCode.A, () -> {
            player.translateX(-5); // move left 5 pixels
        });

        FXGL.onKey(KeyCode.W, () -> {
            player.translateY(-5); // move up 5 pixels
        });

        FXGL.onKey(KeyCode.S, () -> {
            player.translateY(5); // move down 5 pixels
        });

        onKeyDown(KeyCode.F, () -> {
            play("drop.wav");
        });
    }


    public void initUI() {
        Text textPixels = new Text();
        textPixels.setTranslateX(50); // x = 50
        textPixels.setTranslateY(100); // y = 100

        textPixels.textProperty().bind(getWorldProperties().intProperty("pixelsMoved").asString());

        getGameScene().addUINode(textPixels); // add to the scene graph

        var brickTexture = getAssetLoader().loadTexture("brick.png");
        brickTexture.setTranslateX(50);
        brickTexture.setTranslateY(450);

        getGameScene().addUINode(brickTexture);
    }

    public void initGameVars(Map<String, Object> vars) {
        vars.put("pixelsMoved", 0);
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
