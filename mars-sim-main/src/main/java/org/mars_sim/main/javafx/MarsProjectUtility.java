/**
 * Mars Simulation Project
 * MarsProjectUtility.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */
//package org.mars_sim.main.javafx;
//
//import java.awt.Toolkit;
//
//import javafx.application.Application;
//import javafx.stage.Stage;
//
//
///**
// * MarsProjectUtility is the utility class for starting MSP with a JavaFX application thread.
// */
//public class MarsProjectUtility {
//
//    private MarsProjectUtility() {
//        throw new AssertionError("Class cannot be instantiated.");
//    }
//
//    public static void launchApp(AppLaunch appLaunch, String... sArArgs) {
//    	AppFX.appLaunch = appLaunch;
////    	MarsProjectFX.launch(MarsProjectFX.class, sArArgs);
//    }
//
//    // This must be public in order to instantiate successfully
//    public static class AppFX extends Application {
//
//        private static AppLaunch appLaunch;
//
//        @Override
//        public void start(Stage primaryStage) throws Exception {
//
//            if (appLaunch != null) {
//                appLaunch.start(this, primaryStage);
//            }
//        }
//
//        @Override
//        public void init() throws Exception {
//            if (appLaunch != null) {
//                appLaunch.init(this);
//            }
//        }
//
//        @Override
//        public void stop() throws Exception {
//            if (appLaunch != null) {
//                appLaunch.stop(this);
//            }
//        }
//    }
//
//    @FunctionalInterface
//    public static interface AppLaunch {
//        void start(Application app, Stage stage) throws Exception;
//       // Remove default keyword if you need to run in Java7 and below
//        default void init(Application app) throws Exception {
//        }
//
//        default void stop(Application app) throws Exception {
//        }
//    }
//
//    public static void beep() {
//        Toolkit.getDefaultToolkit().beep();
//    }
//}