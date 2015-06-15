package com.jme3x.jfx.injfx;

import java.util.concurrent.Future;
import java.util.function.Function;

import javafx.scene.image.ImageView;

import com.jme3.app.DebugKeysAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;

/**
 * JmeForImageView create a jme'SimpleApplication viewable into an JavaFX's ImageView.
 *
 * You can manage the wrapped SimpleApplication by calling enqueue (by example to add/remove
 * AppState, Node, Light, to change the AppSettings...).
 *
 * See TestDisplayInImageView.java for sample usage.
 *
 * The usage of the class is optional, it can avoid some
 * pitfall in the configuration. If you want a better control, I suggest you to browse
 * the source of this class to see sample of configuration and usage of
 * SceneProcessorCopyToImage.
 *
 * @TODO auto-stop when the ImageView is removed from JavaFX Stage
 * @author davidB
 */
public class JmeForImageView {

	private static SimpleApplication makeJmeApplication(int framerate) {
		AppSettings settings = new AppSettings(true);

		// important to use those settings
		settings.setFullscreen(false);
		settings.setUseInput(false);
		settings.setFrameRate(Math.max(1, Math.min(60, framerate)));
		settings.setCustomRenderer(com.jme3x.jfx.injfx.JmeContextOffscreenSurface.class);

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
	private SceneProcessorCopyToImageView jmeAppDisplayBinder = new SceneProcessorCopyToImageView();

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
	public Future<Boolean> bind(ImageView imageView) {
		return enqueue((jmeApp) -> {
			jmeAppDisplayBinder.bind(imageView, jmeApp);
			return true;
		});
	}

	public Future<Boolean> unbind() {
		return enqueue((jmeApp) -> {
			jmeAppDisplayBinder.unbind();
			return true;
		});
	}

	/**
	 * Enqueue action to apply in Jme's Thread
	 * Action can be add/remove AppState, Node, Light,
	 * change the AppSettings....
	 *
	 * @param f(jmeApp) the action to apply
	 */
	public <R> Future<R> enqueue(Function<SimpleApplication,R> f) {
		SimpleApplication jmeApp = findOrCreate();
		return jmeApp.enqueue(() -> {return f.apply(jmeApp);});
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
