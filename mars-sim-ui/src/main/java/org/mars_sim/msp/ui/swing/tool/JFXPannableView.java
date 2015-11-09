package org.mars_sim.msp.ui.swing.tool;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.layout.BorderPane;

import java.awt.Dimension;
import java.beans.PropertyVetoException;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.mars_sim.msp.ui.swing.MainDesktopPane;

import com.sibvisions.rad.ui.javafx.ext.mdi.FXInternalWindow;

public class JFXPannableView extends JInternalFrame
implements InternalFrameListener {
//JFrame{

    JFXPanel panel;
    Scene scene;
    StackPane stack;
    Text hello;

    private Image backgroundImage;
    private ScrollPane scroll;
    MainDesktopPane desktop;
    boolean wait = true;

    @SuppressWarnings("restriction")
	public JFXPannableView(MainDesktopPane desktop){
        super("MarsScape", true, false, true, true);
        this.desktop = desktop;

    }

    @SuppressWarnings("restriction")
	public void createJFX() {
    	backgroundImage = new Image(this.getClass().getResource("/maps/Mars_Viking_MDIM21_ClrMosaic_global_2500m(compressed).jpg").toExternalForm());
       	// In non-java mode, Why "Exception in thread "AWT-EventQueue-0" java.lang.RuntimeException: Internal graphics not initialized yet" ?
       	double width = desktop.getMainScene().getBorderPane().getWidth();
		double height = desktop.getMainScene().getBorderPane().getHeight();

        panel = new JFXPanel();

        Platform.runLater(new Runnable(){
            @Override
            public void run() {

            	ScrollPane scrollPane = createMap();
                stack = new StackPane();
                scene = new Scene(stack, width, height);
        		stack.prefHeightProperty().bind(desktop.getMainScene().getBorderPane().heightProperty());
        		stack.prefWidthProperty().bind(desktop.getMainScene().getBorderPane().widthProperty());

                //hello = new Text("Hello");

                //scene.setFill(Color.BLACK);
                //hello.setFill(Color.WHEAT);
                //hello.setEffect(new Reflection());

                panel.setScene(scene);
                stack.getChildren().add(scrollPane);//hello);

                wait = false;
            }
        });

        this.getContentPane().add(panel);

        //this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //this.setSize(300, 300);
        //this.setVisible(true);


		setSize(new Dimension(600, 600));
		setMaximizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		addInternalFrameListener(this);
		desktop.add(this);

		Dimension desktopSize = new Dimension((int)width, (int)height);
		Dimension jInternalFrameSize = this.getSize();
	    int _width = (desktopSize.width - jInternalFrameSize.width) / 2;
	    int _height = (desktopSize.height - jInternalFrameSize.height/3) ;

		//System.out.println("width is " + width);
		//System.out.println("height is " + height);
		//System.out.println("desktopSize width is " + desktopSize.getWidth());
		//System.out.println("desktopSize height is " + desktopSize.getHeight());
		//System.out.println("stage.getScene().getWidth() is " + stage.getScene().getWidth());
		//System.out.println("stage.getScene().getHeight() is " + stage.getScene().getHeight());

	    setLocation(_width, _height);
	    //setSize(desktopSize);
	    //setLocation(0, 0);

	    setVisible(true);
    }
/*
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                new JFXPannableView();
            }
        });
    }
*/

    public void centerMap(BorderPane pane) {
  	    // bind the preferred size of the scroll area to the size of the scene.
  	    scroll.prefWidthProperty().bind(pane.widthProperty());
  	    scroll.prefHeightProperty().bind(pane.widthProperty());

  	    // center the scroll contents.
  	    scroll.setHvalue(scroll.getHmin() + (scroll.getHmax() - scroll.getHmin()) / 2);
  	    scroll.setVvalue(scroll.getVmin() + (scroll.getVmax() - scroll.getVmin()) / 2);

    }

    public ScrollPane createMap() {

  	    // construct the scene contents over a stacked background.
  	    StackPane layout = new StackPane();
  	    layout.getChildren().setAll(
  	      new ImageView(backgroundImage)
  	      //, createKillButton()
  	    );

  	    // wrap the scene contents in a pannable scroll pane.
  	    scroll = createScrollPane(layout);
  	    centerMap(desktop.getMainScene().getBorderPane());
  	    return scroll;
  	  }


    /** @return a control to place on the scene.
    private Button createKillButton() {
      final Button killButton = new Button("Kill the evil witch");
      killButton.setStyle("-fx-base: firebrick;");
      killButton.setTranslateX(65);
      killButton.setTranslateY(-130);
      killButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override public void handle(ActionEvent t) {
          killButton.setStyle("-fx-base: forestgreen;");
          killButton.setText("Ding-Dong! The Witch is Dead");
        }
      });
      return killButton;
    }
  */
    /** @return a ScrollPane which scrolls the layout. */
    private ScrollPane createScrollPane(Pane layout) {
      ScrollPane scroll = new ScrollPane();
      scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
      scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
      scroll.setPannable(true);
      scroll.setPrefSize(800, 600);
      scroll.setContent(layout);
      return scroll;
    }


	@Override
	public void internalFrameOpened(InternalFrameEvent e) {
		// TODO Auto-generated method stub
	}


	@Override
	public void internalFrameClosing(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		//tabPanelWeather.setViewer(null);
		//System.out.println("internalFrameClosing()");
	}


	@Override
	public void internalFrameClosed(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		//tabPanelWeather.setViewer(null);
		//System.out.println("internalFrameClosed()");
	}


	@Override
	public void internalFrameIconified(InternalFrameEvent e) {
		// TODO Auto-generated method stub
	}


	@Override
	public void internalFrameDeiconified(InternalFrameEvent e) {
		// TODO Auto-generated method stub
	}


	@Override
	public void internalFrameActivated(InternalFrameEvent e) {
		// TODO Auto-generated method stub
	}


	@Override
	public void internalFrameDeactivated(InternalFrameEvent e) {
		// TODO Auto-generated method stub

	}

}