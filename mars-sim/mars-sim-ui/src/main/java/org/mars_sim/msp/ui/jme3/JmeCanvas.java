package org.mars_sim.msp.ui.jme3;

import java.awt.Canvas;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.bushe.swing.event.EventServiceExistsException;
import org.bushe.swing.event.EventServiceLocator;
import org.bushe.swing.event.ThreadSafeEventService;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;

public class JmeCanvas {

	  /** initialized logger for this class. */
    private static Logger logger = Logger.getLogger(JmeCanvas.class.getName());

    //private JmeWindow jmeWindow;

	public Canvas setupJME() {
    	logger.info("JmeCanvas's setupJME() is in " + Thread.currentThread().getName() + " Thread");
/*
    	try {
			EventServiceLocator.setEventService(EventServiceLocator.SERVICE_NAME_SWING_EVENT_SERVICE,
					new ThreadSafeEventService());
		} catch (EventServiceExistsException e) {
			e.printStackTrace();
		}
*/
		final com.jme3.app.Application rtt = new TestRenderToTexture();
		AppSettings settings = new AppSettings(true);
		settings.setVSync(true);
		rtt.setSettings(settings);
		rtt.createCanvas();
		JmeCanvasContext ctx = (JmeCanvasContext) rtt.getContext();
		Canvas canvas = ctx.getCanvas();
		ctx.setSystemListener(rtt);

		//jmeWindow = new JmeWindow(canvas);

		rtt.startCanvas();
		rtt.enqueue(new Callable<Void>() {
			public Void call() {
				rtt.setPauseOnLostFocus(false);
				((SimpleApplication) rtt).getFlyByCamera().setDragToRotate(true);
				return null;
			}
		});

		return canvas;
	}

}