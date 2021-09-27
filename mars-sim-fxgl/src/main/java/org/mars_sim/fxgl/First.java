package org.mars_sim.fxgl;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;

import javafx.scene.shape.Rectangle;

public class First {
    protected void initGame() {
        FXGL.entityBuilder()
                .at(400, 300)
                .view(new Rectangle(40, 40))
                // 3. add a new instance of component to entity
                .with(new RotatingComponent())
                .buildAndAttach();
    }
    
    // 1. create class that extends Component
    // Note: ideally in a separate file. It's included in this file for clarity.
    private static class RotatingComponent extends Component {

        @Override
        public void onUpdate(double tpf) {
            // 2. specify behavior of the entity enforced by this component
            entity.rotateBy(tpf * 45);
        }
    }
}
