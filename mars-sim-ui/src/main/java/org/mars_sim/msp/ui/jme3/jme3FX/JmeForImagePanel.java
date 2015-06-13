package org.mars_sim.msp.ui.jme3.jme3FX;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.jme3.app.DebugKeysAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;

/**
 * JmeForImagePanel create a jme'SimpleApplication viewable into an JavaFX's ImagePanel.
 *
 * You can manage the wrapped SimpleApplication by calling enqueue (by example to add/remove
 * AppState, Node, Light, to change the AppSettings...).
 *
 * See TestDisplayInImagePanel.java for sample usage.
 *
 * The usage of the class is optional, it can avoid some
 * pitfall in the configuration. If you want a better control, I suggest you to browse
 * the source of this class to see sample of configuration and usage of
 * SceneProcessorCopyToImage.
 *
 * @TODO auto-stop when the ImagePanel is removed from JavaFX Stage
 * @author davidB
 */
public class JmeForImagePanel {

	private static SimpleApplication makeJmeApplication(int framerate) {
		AppSettings settings = new AppSettings(true);

		// important to use those settings
		settings.setFullscreen(false);
		settings.setUseInput(false);
		settings.setFrameRate(Math.max(1, Math.min(60, framerate)));
		settings.setCustomRenderer(JmeContextOffscreenSurface.class);

		SimpleApplication app = new SimpleApplication(){
			@Override
			public void simpleInitApp() {
				// to prevent a NPE (due to setUseInput(null)) on Application.stop()
				getStateManager().detach(getStateManager().getState(DebugKeysAppState.class));
			}
		};
		app.setSettings(settings);
		app.setShowSettings(false);
		return app;
	}

	private SimpleApplication jmeApp0;
	private SceneProcessorCopyToImagePanel jmeAppDisplayBinder = new SceneProcessorCopyToImagePanel();

	/**
	 * Lazy creation of the wrapped SimpleApplication.
	 */
	private SimpleApplication findOrCreate() {
		if (jmeApp0 == null) {
			jmeApp0 = makeJmeApplication(30);
			jmeApp0.start();
		}
		return jmeApp0;
	}

	/**
	 * Bind the wrapped SimpleApplication to an imageView.
	 *
	 * <ul>
	 * <li>Only one imageView can be binded.</li>
	 * <li>Only jmeApp.getViewPort(), jmeApp.getGuiViewPort() are binded</li>
	 * </ul>
	 *
	 * @param imageView destination
	 * @return Future when bind is done (async)
	 */
	public Future<Boolean> bind(final ImagePanel imageView) {
		return enqueue(new Function<SimpleApplication, Boolean>(){
			public Boolean apply(SimpleApplication jmeApp) {
				jmeAppDisplayBinder.bind(imageView, jmeApp);
				return true;
			}
		});
	}

	public Future<Boolean> unbind() {
		return enqueue(new Function<SimpleApplication, Boolean>() {
			public Boolean apply(SimpleApplication jmeApp) {
				jmeAppDisplayBinder.unbind();
				return true;
			}
		});
	}

	/**
	 * Enqueue action to apply in Jme's Thread
	 * Action can be add/remove AppState, Node, Light,
	 * change the AppSettings....
	 *
	 * @param f(jmeApp) the action to apply
	 */
	public <R> Future<R> enqueue(final Function<SimpleApplication,R> f) {
		final SimpleApplication jmeApp = findOrCreate();
		return jmeApp.enqueue(new Callable<R>(){
			@Override
			public R call() throws Exception {
				return f.apply(jmeApp);
			}
		});
	}

	public void stop(boolean waitFor) {
		if (jmeApp0 != null){
			try {
				unbind().get();
			} catch (Exception exc) {
				//TODO
				exc.printStackTrace();
			}
			jmeApp0.stop(waitFor);
		}
	}
}
