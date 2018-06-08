package org.mars_sim.msp.ui.javafx.nsmenufx;

import de.codecentric.centerdevice.MenuToolkit;
import de.codecentric.centerdevice.dialogs.about.AboutStageBuilder;
import de.codecentric.centerdevice.icns.IcnsParser;
import de.codecentric.centerdevice.icns.IcnsType;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Calendar;

/**
 * Created by jan on 30/05/16.
 */
public class AboutMenu extends Application {

  @Override public void start(Stage primaryStage) throws Exception {
    primaryStage.setScene(new Scene(new StackPane()));
    primaryStage.show();

    MenuToolkit tk = MenuToolkit.toolkit();

    AboutStageBuilder aboutStageBuilder = AboutStageBuilder.start("About MyApp")
        .withAppName("MyApp").withCloseOnFocusLoss().withHtml("<i>Some descriptive text</i>")
        .withVersionString("Version 1.0b").withCopyright("Copyright \u00A9 " + Calendar
            .getInstance().get(Calendar.YEAR));


    try {
      IcnsParser parser = IcnsParser.forFile(AboutStageBuilder.DEFAULT_APP_ICON);
      aboutStageBuilder = aboutStageBuilder.withImage(new Image(parser.getIconStream(IcnsType.ic08)));
    } catch (IOException e) {
      // Too bad, cannot load dummy image
    }

    Menu applicationMenu = tk.createDefaultApplicationMenu("MyApp", aboutStageBuilder.build());

    MenuBar bar = new MenuBar();
    bar.getMenus().add(applicationMenu);
    tk.setMenuBar(bar);
  }
}
