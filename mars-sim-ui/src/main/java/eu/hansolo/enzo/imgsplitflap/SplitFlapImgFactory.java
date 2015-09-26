/*
 * Copyright (c) 2015 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.enzo.imgsplitflap;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;


/**
 * User: hansolo
 * Date: 06.05.14
 * Time: 12:40
 */
public enum SplitFlapImgFactory {
    INSTANCE;
    
    private static final double PREFERRED_WIDTH  = 234;
    private static final double PREFERRED_HEIGHT = 402;
    private static final double ASPECT_RATIO     = PREFERRED_HEIGHT / PREFERRED_WIDTH;    
    private Color frameColor;
    private Color backgroundColor;
    private Color flapColor;


    // ******************** Constructors **************************************
    private SplitFlapImgFactory() {       
        frameColor      = Color.rgb(46, 47, 43);
        backgroundColor = Color.BLACK;
        flapColor       = Color.rgb(50, 50, 45);
    }


    // ******************** Methods *******************************************
    public void setFrameColor(final Color FRAME_COLOR) {
        frameColor = FRAME_COLOR;
    }
    public void setBackgroundColor(final Color BACKGROUND_COLOR) {
        backgroundColor = BACKGROUND_COLOR;
    }
    public void setFlapColor(final Color FLAP_COLOR) {
        flapColor = FLAP_COLOR;
    }

    public Image createBackgroundImage(final double WIDTH, final double HEIGHT) {        
        double width  = WIDTH;
        double height = HEIGHT;

        if (ASPECT_RATIO * width > height) {
            width = 1 / (ASPECT_RATIO / height);
        } else if (1 / (ASPECT_RATIO / height) > width) {
            height = ASPECT_RATIO * width;
        }

        Canvas          canvas = new Canvas(width, height);
        GraphicsContext ctx    = canvas.getGraphicsContext2D();        

        // Adjust the shadows
        InnerShadow upperFlapInnerShadowBlack = new InnerShadow(0.01282 * width, 0, 0, Color.BLACK);
        InnerShadow upperFlapInnerShadowWhite = new InnerShadow(0.00855 * width, 0, 1, Color.rgb(255, 255, 255, 0.65));
        upperFlapInnerShadowWhite.setInput(upperFlapInnerShadowBlack);
        
        InnerShadow lowerFlapInnerShadowBlack = new InnerShadow(0.01282 * width, 0, 0, Color.BLACK);
        InnerShadow lowerFlapInnerShadowWhite = new InnerShadow(0.00855 * width, 0, -1, Color.rgb(255, 255, 255, 0.65));
        lowerFlapInnerShadowWhite.setInput(lowerFlapInnerShadowBlack);
                                           
        ctx.clearRect(0, 0, width, height);
        
        // frame
        ctx.save();
        ctx.setFill(frameColor);
        ctx.fillRect(0.0, 0.0, width, height);
        ctx.restore();

        // background
        ctx.save();        
        ctx.setFill(backgroundColor);
        ctx.fillRect(0.05982905982905983 * width, 0.03482587064676617 * height,
                     0.8803418803418803 * width, 0.9203980099502488 * height);                        
        ctx.restore();

        // wheel right frame
        ctx.save();                                       
        ctx.setFill(new LinearGradient(0, 0.39054726368159204 * height,
                                          0, 0.5298507462686567 * height,
                                          false, CycleMethod.NO_CYCLE,
                                          new Stop(0.0, Color.rgb(28, 28, 28)),
                                          new Stop(0.1, Color.rgb(85, 85, 85)),
                                          new Stop(1.0, Color.rgb(25, 25, 25))));
        ctx.fillRect(0.8547008547008547 * width, 0.39054726368159204 * height, 
                     0.06837606837606838 * width, 0.13930348258706468 * height);
        ctx.restore();

        // wheel right
        ctx.save();        
        ctx.setFill(new LinearGradient(0, 0.39552238805970147 * height,
                                          0, 0.5248756218905473 * height,
                                          false, CycleMethod.NO_CYCLE,
                                          new Stop(0.0, Color.rgb(125, 125, 125)),
                                          new Stop(0.1, Color.rgb(212, 212, 212)),
                                          new Stop(0.65, Color.rgb(60, 60, 60)),
                                          new Stop(0.9, Color.rgb(107, 107, 107)),
                                          new Stop(1.0, Color.rgb(83, 83, 83))));
        ctx.fillRect(0.8632478632478633 * width, 0.39552238805970147 * height,
                     0.05128205128205128 * width, 0.12935323383084577 * height);                                
        ctx.restore();

        // wheel left frame
        ctx.save();
        ctx.setFill(new LinearGradient(0, 0.39054726368159204 * height,
                                          0, 0.5298507462686567 * height,
                                          false, CycleMethod.NO_CYCLE,
                                          new Stop(0.0, Color.rgb(28, 28, 28)),
                                          new Stop(0.1, Color.rgb(85, 85, 85)),
                                          new Stop(1.0, Color.rgb(25, 25, 25))));
        ctx.fillRect(0.07692307692307693 * width, 0.39054726368159204 * height,
                     0.06837606837606838 * width, 0.13930348258706468 * height);                       
        ctx.restore();

        // wheel left
        ctx.save();
        ctx.setFill(new LinearGradient(0, 0.39552238805970147 * height,
                                          0, 0.5248756218905473 * height,
                                          false, CycleMethod.NO_CYCLE,
                                          new Stop(0.0, Color.rgb(125, 125, 125)),
                                          new Stop(0.1, Color.rgb(212, 212, 212)),
                                          new Stop(0.65, Color.rgb(60, 60, 60)),
                                          new Stop(0.9, Color.rgb(107, 107, 107)),
                                          new Stop(1.0, Color.rgb(83, 83, 83))));
        ctx.fillRect(0.08547008547008547 * width, 0.39552238805970147 * height,
                     0.05128205128205128 * width, 0.12935323383084577 * height);                                        
        ctx.restore();

        // lowerFlap4
        ctx.save();
        ctx.setEffect(lowerFlapInnerShadowBlack);
        ctx.beginPath();
        ctx.moveTo(0.07692307692307693 * width, 0.9228855721393034 * height);
        ctx.bezierCurveTo(0.07692307692307693 * width, 0.9228855721393034 * height, 0.07692307692307693 * width, 0.6094527363184079 * height, 0.07692307692307693 * width, 0.6094527363184079 * height);
        ctx.lineTo(0.1581196581196581 * width, 0.6094527363184079 * height);
        ctx.lineTo(0.1581196581196581 * width, 0.5398009950248757 * height);
        ctx.lineTo(0.8418803418803419 * width, 0.5398009950248757 * height);
        ctx.lineTo(0.8418803418803419 * width, 0.6094527363184079 * height);
        ctx.lineTo(0.9230769230769231 * width, 0.6094527363184079 * height);
        ctx.bezierCurveTo(0.9230769230769231 * width, 0.6094527363184079 * height, 0.9230769230769231 * width, 0.9228855721393034 * height, 0.9230769230769231 * width, 0.9228855721393034 * height);
        ctx.bezierCurveTo(0.9230769230769231 * width, 0.9378109452736318 * height, 0.905982905982906 * width, 0.9477611940298507 * height, 0.8803418803418803 * width, 0.9477611940298507 * height);
        ctx.bezierCurveTo(0.8803418803418803 * width, 0.9477611940298507 * height, 0.11965811965811966 * width, 0.9477611940298507 * height, 0.11965811965811966 * width, 0.9477611940298507 * height);
        ctx.bezierCurveTo(0.09401709401709402 * width, 0.9477611940298507 * height, 0.07692307692307693 * width, 0.9378109452736318 * height, 0.07692307692307693 * width, 0.9228855721393034 * height);
        ctx.closePath();
        ctx.setFill(flapColor.deriveColor(0, 1, 0.95, 1));
        ctx.fill();
        ctx.restore();

        // lowerFlap3
        ctx.save();
        ctx.setEffect(lowerFlapInnerShadowBlack);
        ctx.beginPath();
        ctx.moveTo(0.07692307692307693 * width, 0.9054726368159204 * height);
        ctx.bezierCurveTo(0.07692307692307693 * width, 0.9054726368159204 * height, 0.07692307692307693 * width, 0.5920398009950248 * height, 0.07692307692307693 * width, 0.5920398009950248 * height);
        ctx.lineTo(0.1581196581196581 * width, 0.5920398009950248 * height);
        ctx.lineTo(0.1581196581196581 * width, 0.5223880597014925 * height);
        ctx.lineTo(0.8418803418803419 * width, 0.5223880597014925 * height);
        ctx.lineTo(0.8418803418803419 * width, 0.5920398009950248 * height);
        ctx.lineTo(0.9230769230769231 * width, 0.5920398009950248 * height);
        ctx.bezierCurveTo(0.9230769230769231 * width, 0.5920398009950248 * height, 0.9230769230769231 * width, 0.9054726368159204 * height, 0.9230769230769231 * width, 0.9054726368159204 * height);
        ctx.bezierCurveTo(0.9230769230769231 * width, 0.9203980099502488 * height, 0.905982905982906 * width, 0.9303482587064676 * height, 0.8803418803418803 * width, 0.9303482587064676 * height);
        ctx.bezierCurveTo(0.8803418803418803 * width, 0.9303482587064676 * height, 0.11965811965811966 * width, 0.9303482587064676 * height, 0.11965811965811966 * width, 0.9303482587064676 * height);
        ctx.bezierCurveTo(0.09401709401709402 * width, 0.9303482587064676 * height, 0.07692307692307693 * width, 0.9203980099502488 * height, 0.07692307692307693 * width, 0.9054726368159204 * height);
        ctx.closePath();
        ctx.setFill(flapColor.deriveColor(0, 1, 0.95, 1));
        ctx.fill();
        ctx.restore();

        // lowerFlap2
        ctx.save();
        ctx.setEffect(lowerFlapInnerShadowBlack);
        ctx.beginPath();
        ctx.moveTo(0.07692307692307693 * width, 0.8880597014925373 * height);
        ctx.bezierCurveTo(0.07692307692307693 * width, 0.8880597014925373 * height, 0.07692307692307693 * width, 0.5746268656716418 * height, 0.07692307692307693 * width, 0.5746268656716418 * height);
        ctx.lineTo(0.1581196581196581 * width, 0.5746268656716418 * height);
        ctx.lineTo(0.1581196581196581 * width, 0.5049751243781094 * height);
        ctx.lineTo(0.8418803418803419 * width, 0.5049751243781094 * height);
        ctx.lineTo(0.8418803418803419 * width, 0.5746268656716418 * height);
        ctx.lineTo(0.9230769230769231 * width, 0.5746268656716418 * height);
        ctx.bezierCurveTo(0.9230769230769231 * width, 0.5746268656716418 * height, 0.9230769230769231 * width, 0.8880597014925373 * height, 0.9230769230769231 * width, 0.8880597014925373 * height);
        ctx.bezierCurveTo(0.9230769230769231 * width, 0.9029850746268657 * height, 0.905982905982906 * width, 0.9129353233830846 * height, 0.8803418803418803 * width, 0.9129353233830846 * height);
        ctx.bezierCurveTo(0.8803418803418803 * width, 0.9129353233830846 * height, 0.11965811965811966 * width, 0.9129353233830846 * height, 0.11965811965811966 * width, 0.9129353233830846 * height);
        ctx.bezierCurveTo(0.09401709401709402 * width, 0.9129353233830846 * height, 0.07692307692307693 * width, 0.9029850746268657 * height, 0.07692307692307693 * width, 0.8880597014925373 * height);
        ctx.closePath();
        ctx.setFill(flapColor.deriveColor(0, 1, 0.95, 1));
        ctx.fill();
        ctx.restore();

        // lowerFlap1
        ctx.save();
        ctx.setEffect(lowerFlapInnerShadowBlack);
        ctx.beginPath();
        ctx.moveTo(0.07692307692307693 * width, 0.8681592039800995 * height);
        ctx.bezierCurveTo(0.07692307692307693 * width, 0.8681592039800995 * height, 0.07692307692307693 * width, 0.554726368159204 * height, 0.07692307692307693 * width, 0.554726368159204 * height);
        ctx.lineTo(0.1581196581196581 * width, 0.554726368159204 * height);
        ctx.lineTo(0.1581196581196581 * width, 0.48507462686567165 * height);
        ctx.lineTo(0.8418803418803419 * width, 0.48507462686567165 * height);
        ctx.lineTo(0.8418803418803419 * width, 0.554726368159204 * height);
        ctx.lineTo(0.9230769230769231 * width, 0.554726368159204 * height);
        ctx.bezierCurveTo(0.9230769230769231 * width, 0.554726368159204 * height, 0.9230769230769231 * width, 0.8681592039800995 * height, 0.9230769230769231 * width, 0.8681592039800995 * height);
        ctx.bezierCurveTo(0.9230769230769231 * width, 0.8830845771144279 * height, 0.905982905982906 * width, 0.8930348258706468 * height, 0.8803418803418803 * width, 0.8930348258706468 * height);
        ctx.bezierCurveTo(0.8803418803418803 * width, 0.8930348258706468 * height, 0.11965811965811966 * width, 0.8930348258706468 * height, 0.11965811965811966 * width, 0.8930348258706468 * height);
        ctx.bezierCurveTo(0.09401709401709402 * width, 0.8930348258706468 * height, 0.07692307692307693 * width, 0.8830845771144279 * height, 0.07692307692307693 * width, 0.8681592039800995 * height);
        ctx.closePath();
        ctx.setFill(flapColor.deriveColor(0, 1, 0.95, 1));
        ctx.fill();
        ctx.restore();

        // lowerFlap
        ctx.save();
        ctx.setEffect(lowerFlapInnerShadowWhite);
        ctx.beginPath();
        ctx.moveTo(0.07692307692307693 * width, 0.8507462686567164 * height);
        ctx.bezierCurveTo(0.07692307692307693 * width, 0.8507462686567164 * height, 0.07692307692307693 * width, 0.5373134328358209 * height, 0.07692307692307693 * width, 0.5373134328358209 * height);
        ctx.lineTo(0.1581196581196581 * width, 0.5373134328358209 * height);
        ctx.lineTo(0.1581196581196581 * width, 0.46766169154228854 * height);
        ctx.lineTo(0.8418803418803419 * width, 0.46766169154228854 * height);
        ctx.lineTo(0.8418803418803419 * width, 0.5373134328358209 * height);
        ctx.lineTo(0.9230769230769231 * width, 0.5373134328358209 * height);
        ctx.bezierCurveTo(0.9230769230769231 * width, 0.5373134328358209 * height, 0.9230769230769231 * width, 0.8507462686567164 * height, 0.9230769230769231 * width, 0.8507462686567164 * height);
        ctx.bezierCurveTo(0.9230769230769231 * width, 0.8656716417910447 * height, 0.905982905982906 * width, 0.8756218905472637 * height, 0.8803418803418803 * width, 0.8756218905472637 * height);
        ctx.bezierCurveTo(0.8803418803418803 * width, 0.8756218905472637 * height, 0.11965811965811966 * width, 0.8756218905472637 * height, 0.11965811965811966 * width, 0.8756218905472637 * height);
        ctx.bezierCurveTo(0.09401709401709402 * width, 0.8756218905472637 * height, 0.07692307692307693 * width, 0.8656716417910447 * height, 0.07692307692307693 * width, 0.8507462686567164 * height);
        ctx.closePath();
        ctx.setFill(new LinearGradient(0, 0.46766169154228854 * height, 
                                       0, 0.8756218905472637 * height,
                                       false, CycleMethod.NO_CYCLE,
                                       new Stop(0.0, flapColor),
                                       new Stop(0.75, flapColor),
                                       new Stop(1.0, flapColor.deriveColor(0, 1, 0.95, 1))));
        ctx.fill();
        ctx.restore();

        // upperFlap
        ctx.save();
        ctx.setEffect(upperFlapInnerShadowWhite);
        ctx.beginPath();
        ctx.moveTo(0.07692307692307693 * width, 0.06965174129353234 * height);
        ctx.bezierCurveTo(0.07692307692307693 * width, 0.06965174129353234 * height, 0.07692307692307693 * width, 0.38308457711442784 * height, 0.07692307692307693 * width, 0.38308457711442784 * height);
        ctx.lineTo(0.1581196581196581 * width, 0.38308457711442784 * height);
        ctx.lineTo(0.1581196581196581 * width, 0.4527363184079602 * height);
        ctx.lineTo(0.8418803418803419 * width, 0.4527363184079602 * height);
        ctx.lineTo(0.8418803418803419 * width, 0.38308457711442784 * height);
        ctx.lineTo(0.9230769230769231 * width, 0.38308457711442784 * height);
        ctx.bezierCurveTo(0.9230769230769231 * width, 0.38308457711442784 * height, 0.9230769230769231 * width, 0.06965174129353234 * height, 0.9230769230769231 * width, 0.06965174129353234 * height);
        ctx.bezierCurveTo(0.9230769230769231 * width, 0.05472636815920398 * height, 0.905982905982906 * width, 0.04477611940298507 * height, 0.8803418803418803 * width, 0.04477611940298507 * height);
        ctx.bezierCurveTo(0.8803418803418803 * width, 0.04477611940298507 * height, 0.11965811965811966 * width, 0.04477611940298507 * height, 0.11965811965811966 * width, 0.04477611940298507 * height);
        ctx.bezierCurveTo(0.09401709401709402 * width, 0.04477611940298507 * height, 0.07692307692307693 * width, 0.05472636815920398 * height, 0.07692307692307693 * width, 0.06965174129353234 * height);
        ctx.closePath();
        ctx.setFill(new LinearGradient(0, 0.06965174129353234 * height, 
                                       0, 0.4527363184079602 * height,
                                       false, CycleMethod.NO_CYCLE,
                                       new Stop(0.0, flapColor.deriveColor(0, 1, 0.95, 1)),
                                       new Stop(0.75, flapColor),
                                       new Stop(1.0, flapColor)));
        ctx.fill();
        ctx.restore();
                
        // Take snapshot
        Pane  pane  = new Pane(canvas);
        Scene scene = new Scene(pane, Color.TRANSPARENT);

        WritableImage image = scene.snapshot(null);
        return image;
    }       
    
    public Image createFlapImage(final double WIDTH, final double HEIGHT) {        
        double width  = WIDTH;
        double height = HEIGHT;

        if (ASPECT_RATIO * width > height) {
            width = 1 / (ASPECT_RATIO / height);
        } else if (1 / (ASPECT_RATIO / height) > width) {
            height = ASPECT_RATIO * width;
        }
         
        InnerShadow upperFlapInnerShadowBlack = new InnerShadow(0.01282 * width, 0, 0, Color.BLACK);
        InnerShadow upperFlapInnerShadowWhite = new InnerShadow(0.00855 * width, 0, 1, Color.rgb(255, 255, 255, 0.65));
        upperFlapInnerShadowWhite.setInput(upperFlapInnerShadowBlack);
        
        width  = 0.84615 * WIDTH;
        height = 0.40796 * HEIGHT;

        Canvas          canvas = new Canvas(width, height);
        GraphicsContext ctx    = canvas.getGraphicsContext2D();        

        ctx.clearRect(0, 0, width, height);

        //upperFlap
        ctx.save();
        ctx.setEffect(upperFlapInnerShadowWhite);
        ctx.beginPath();
        ctx.moveTo(0, 0.06097560975609756 * height);
        ctx.bezierCurveTo(0, 0.06097560975609756 * height, 0, 0.8292682926829268 * height, 0, 0.8292682926829268 * height);
        ctx.lineTo(0.09595959595959595 * width, 0.8292682926829268 * height);
        ctx.lineTo(0.09595959595959595 * width, height);
        ctx.lineTo(0.9040404040404041 * width, height);
        ctx.lineTo(0.9040404040404041 * width, 0.8292682926829268 * height);
        ctx.lineTo(width, 0.8292682926829268 * height);
        ctx.bezierCurveTo(width, 0.8292682926829268 * height, width, 0.06097560975609756 * height, width, 0.06097560975609756 * height);
        ctx.bezierCurveTo(width, 0.024390243902439025 * height, 0.9797979797979798 * width, 0, 0.9494949494949495 * width, 0);
        ctx.bezierCurveTo(0.9494949494949495 * width, 0, 0.050505050505050504 * width, 0, 0.050505050505050504 * width, 0);
        ctx.bezierCurveTo(0.020202020202020204 * width, 0, 0, 0.024390243902439025 * height, 0, 0.06097560975609756 * height);
        ctx.closePath();
        ctx.setFill(new LinearGradient(0, 0, 0, height,
                                       false, CycleMethod.NO_CYCLE,
                                       new Stop(0.0, flapColor.deriveColor(0, 1, 0.95, 1)),
                                       new Stop(0.75, flapColor),
                                       new Stop(1.0, flapColor)));
        ctx.fill();
        ctx.restore();
        
        // Take snapshot
        Pane  pane  = new Pane(canvas);        
        Scene scene = new Scene(pane, Color.TRANSPARENT);

        WritableImage image = scene.snapshot(null);
        return image;
    }
}
