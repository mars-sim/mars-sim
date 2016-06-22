package org.mars_sim.msp.demo;

import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.animation.PathTransition.OrientationType;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.Glow;
import javafx.scene.effect.Reflection;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Simple animation of labels adapted from http://www.javaworld.com/article/2074530/core-java/javafx-2--simultaneous-animated-text-strings.html
 * 
 * @author Dustin
 */
public class SlideDemo extends Application
{
   /**
    * Generate Path upon which animation will occur.
    * 
    * @return Generated path.
    */
   private Path generateCurvyPath()
   {
      final Path path = new Path();
      path.getElements().add(new MoveTo(70,20));
      path.getElements().add(new CubicCurveTo(430, 0, 430, 120, 250, 120));
      path.getElements().add(new CubicCurveTo(50, 120, 50, 240, 430, 240));
      path.setOpacity(0.0);
      return path;
   }

   /**
    * Generate the path transition.
    * 
    * @param shape Shape to travel along path.
    * @param path Path to be traveled upon.
    * @param duration Duration of single animation.
    * @param delay Delay before beginning first animation.
    * @param orientation Orientation of shape during animation.
    * @return PathTransition.
    */
   private PathTransition generatePathTransition(
      final Shape shape, final Path path,
      final Duration duration, final Duration delay,
      final OrientationType orientation)
   {
      final PathTransition pathTransition = new PathTransition();
      pathTransition.setDuration(duration);
      pathTransition.setDelay(delay);
      pathTransition.setPath(path);
      pathTransition.setNode(shape);
      pathTransition.setOrientation(orientation);
      pathTransition.setCycleCount(Timeline.INDEFINITE);
      pathTransition.setAutoReverse(true);
      return pathTransition;
   }

   /**
    * Generate RMOUG text string with appropriate fill, font, and effect.
    * 
    * @return "RMOUG" text string with fill, font, and effect.
    */
   private Text generateTitleText()
   {
      return TextBuilder.create().text("Mars Simulation Project").x(20).y(20).fill(Color.DARKGRAY)
                        .font(Font.font(java.awt.Font.SERIF, 75))
                        .effect(new Glow(0.25)).build();
   }

   /**
    * Generate a text string with appropriate position, fill,
    * and font.
    * 
    * @return text string with specified font, fill, and position.
    */
   private Text generateDaysText()
   {
      return TextBuilder.create().text("Settlement Founding Day, 15-Adios-01")
                        .x(380).y(240).fill(Color.DARKOLIVEGREEN)
                        .font(Font.font(java.awt.Font.SERIF, 50)).build();
   }

   /**
    * Location String with specified effect, font, and position.
    * 
    * @return Location String with specified effect, font, and position.
    */
   private Text generateLocationText()
   {
      final Reflection reflection = new Reflection();
      reflection.setFraction(1.0);
      return TextBuilder.create()
                        .text("Schiaparelli Point, Mars").x(20).y(20)
                        .font(Font.font(java.awt.Font.SANS_SERIF, 25))
                        .effect(reflection)
                        .build();
   }

   /**
    * Apply animation.
    *  
    * @param group Group to which animation is to be applied.
    */
   private void applyAnimation(final Group group)
   {
      final Path path = generateCurvyPath();
      group.getChildren().add(path);
      final Shape rmoug = generateTitleText();
      group.getChildren().add(rmoug);
      final Shape td = generateDaysText();
      group.getChildren().add(td);
      final Shape denver = generateLocationText();
      group.getChildren().add(denver);
      final PathTransition rmougTransition =
         generatePathTransition(
            rmoug, path, Duration.seconds(8.0), Duration.seconds(0.5),
            OrientationType.NONE);
      final PathTransition tdTransition =
         generatePathTransition(
            td, path, Duration.seconds(5.5), Duration.seconds(0.1),
            OrientationType.NONE);
      final PathTransition denverTransition =
         generatePathTransition(
            denver, path, Duration.seconds(30), Duration.seconds(3),
            OrientationType.ORTHOGONAL_TO_TANGENT);
      final ParallelTransition parallelTransition =
         new ParallelTransition(rmougTransition, tdTransition, denverTransition);
      parallelTransition.play(); 
   }

   /**
    * JavaFX Applcation starting method.
    * 
    * @param stage Primary stage.
    * @throws Exception Potential JavaFX application exception.
    */
   @Override
   public void start(Stage stage) throws Exception
   {
      final Group rootGroup = new Group();
      final Scene scene = new Scene(rootGroup, 1024, 800, Color.GHOSTWHITE);
      stage.setScene(scene);
      stage.setTitle("Mars Simulation Project - Settlement Founding Day");
      stage.show();
      applyAnimation(rootGroup);
   }

   /**
    * Main function for running JavaFX animation demo.
    * 
    * @param arguments Command-line arguments; none expected.
    */
   public static void main(final String[] arguments)
   {
      Application.launch(arguments);
   }
}