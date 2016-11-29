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

import org.controlsfx.control.InfoOverlay;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import com.sibvisions.rad.ui.javafx.ext.mdi.FXInternalWindow;

public class JFXPannableView extends JInternalFrame
implements InternalFrameListener {
//JFrame{

    //boolean wait = true;

	private String imageUrl;
	private JFXPanel panel;
	private Scene scene;
	//private StackPane stack;
	//private Text hello;

    private Image backgroundImage;
    private ImageView backgroundImageView;
    private ScrollPane scroll;
    private MainDesktopPane desktop;


    @SuppressWarnings("restriction")
	public JFXPannableView(MainDesktopPane desktop){
        super("MarsScape", true, false, true, true);
        this.desktop = desktop;

        imageUrl = getClass().getResource("/maps/Mars_Viking_MDIM21_ClrMosaic_global_2500m(compressed).jpg").toExternalForm();
        backgroundImage = new Image(imageUrl);
       	backgroundImageView = new ImageView(imageUrl);
    }

    @SuppressWarnings("restriction")
	public void createJFX() {
      	// In non-java mode, Why "Exception in thread "AWT-EventQueue-0" java.lang.RuntimeException: Internal graphics not initialized yet" ?
       	double width = desktop.getMainScene().getAnchorPane().getWidth();
		double height = desktop.getMainScene().getAnchorPane().getHeight();

        panel = new JFXPanel();
        	//StackPane layout = new StackPane(infoOverlay);

        Platform.runLater(new Runnable(){
            @Override
            public void run() {

            	ScrollPane scrollPane = createMap();
                //StackPane stack = new StackPane();
				//stack.getChildren().add(scrollPane);//hello);

		      	//String info = "This is a pannable Mars map with pixel size 8536 x 4268. Drag mouse cursor to the left/right/top/bottom to see more.";
		       	//InfoOverlay infoOverlay = new InfoOverlay(stack, info);


                scene = new Scene(scrollPane, width, height);
        		//stack.prefHeightProperty().bind(desktop.getMainScene().getBorderPane().heightProperty());
        		//stack.prefWidthProperty().bind(desktop.getMainScene().getBorderPane().widthProperty());

                //hello = new Text("Hello");
                //scene.setFill(Color.BLACK);
                //hello.setFill(Color.WHEAT);
                //hello.setEffect(new Reflection());

                panel.setScene(scene);

                //wait = false;
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

    public void centerMap(ScrollPane scrollPane, Pane pane) {
  	    // bind the preferred size of the scroll area to the size of the scene.
  	    scrollPane.prefWidthProperty().bind(pane.widthProperty());
  	    scrollPane.prefHeightProperty().bind(pane.widthProperty());

  	    // center the scroll contents.
  	    scrollPane.setHvalue(scrollPane.getHmin() + (scrollPane.getHmax() - scrollPane.getHmin()) / 2);
  	    scrollPane.setVvalue(scrollPane.getVmin() + (scrollPane.getVmax() - scrollPane.getVmin()) / 2);

    }

    public ScrollPane createMap() {

  	    // construct the scene contents over a stacked background.

       	//StackPane layout = new StackPane(backgroundImageView);
  	    StackPane layout = new StackPane();
  	    layout.getChildren().setAll(
  	    		backgroundImageView
  	      //, createKillButton()
  	    );

  	    // wrap the scene contents in a pannable scroll pane.
  	    scroll = createScrollPane(layout);
  	    centerMap(scroll, desktop.getMainScene().getAnchorPane());
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