package com.jme3x.jfx;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.layout.Region;

/**
 * represents the content of a fullscreen hud,the hud is resized to fit the screen automatically.<br>
 * Content must have a Region(or subclass) at the root level
 * 
 * @author empire
 * 
 */
public abstract class AbstractHud {
	private Throwable							innerError				= null;
	protected Region							node;
	private boolean								initialized;
	private SimpleBooleanProperty				attached				= new SimpleBooleanProperty();
	private SimpleObjectProperty<GuiManager>	responsibleGuiManager	= new SimpleObjectProperty<>();

	/**
	 * Temp array for stylesheet adding before precaching
	 */
	protected List<String>						stylesToAdd				= new ArrayList<String>();

	/**
	 * Internal call, for guimanager statemanagement, do not call
	 * 
	 * @param value
	 * @param guiManager
	 */
	public void setAttached(final boolean value, final GuiManager guiManager) {
		assert Platform.isFxApplicationThread() : "parent change outside of JFX thread?";

		this.attached.set(value);
		assert guiManager == null || this.responsibleGuiManager.get() == null : "New guiManager but already old one!?";
		this.responsibleGuiManager.set(guiManager);
	}

	/**
	 * returns the GuiManager this is attached to or null;
	 * 
	 * @return
	 */
	public GuiManager getResponsibleGuiManager() {
		return this.responsibleGuiManager.get();
	}

	/**
	 * Adds a stylesheet to be used, can be called before precaching, in this case execution is defered till then <br>
	 * 
	 * @param stylesheet
	 *            the URi to be used, eg. <br>
	 *            DynamicCSS.class.getResource("/jarcss.css").toExternalForm(); <br>
	 *            or <br>
	 *            http://mysite.com/style.css <br>
	 * 
	 *            I know currently no way to load a directly from a InputStream and thus allow a JME style assetlocator based logic. As a workaround, locate file via assetmanager, copy to tmp folder and pass file uri into here
	 * 
	 * 
	 */
	public void addStyleSheet(final String stylesheet) {
		final Runnable inFXThread = new Runnable() {

			@Override
			public void run() {
				if (AbstractHud.this.initialized) {
					AbstractHud.this.node.getStylesheets().add(stylesheet);
				} else {
					AbstractHud.this.stylesToAdd.add(stylesheet);
				}
			}
		};
		if (Platform.isFxApplicationThread()) {
			inFXThread.run();
		} else {
			Platform.runLater(inFXThread);
		}

	}

	public ObservableValue<Boolean> attached() {
		return this.attached;
	}

	public boolean isAttached() {
		return this.attached.get();
	}

	/**
	 * initializes the hud, should load all ressources, fxml parsing ect. Allowed to be called from any thread,<br>
	 * blocks till jfx thread is finished with work
	 */
	public final void precache() {
		assert !this.initialized : "Duplicate init";

	if (Platform.isFxApplicationThread()) {
		AbstractHud.this.node = AbstractHud.this.doInit();
		if (AbstractHud.this.node != null) {
			AbstractHud.this.node.getStylesheets().addAll(AbstractHud.this.stylesToAdd);
		}
		AbstractHud.this.stylesToAdd = null;

		// we are not initially attached
		AbstractHud.this.attached.set(false);
	} else {
		final Semaphore waitForInit = new Semaphore(0);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					AbstractHud.this.node = AbstractHud.this.doInit();
					if (AbstractHud.this.node != null) {
						AbstractHud.this.node.getStylesheets().addAll(AbstractHud.this.stylesToAdd);
					}
					AbstractHud.this.stylesToAdd = null;

					// we are not initially attached
					AbstractHud.this.attached.set(false);
				} finally {
					waitForInit.release();
				}
			}
		});
		waitForInit.acquireUninterruptibly();
	}
	/**
	 * redirect inner error
	 */
	if (this.getInnerError() != null) {
		throw new RuntimeException("Error init hud", this.getInnerError());
	}
	AbstractHud.this.initialized = true;
	}

	public Region getNode() {
		return this.node;
	}

	protected Region doInit() {
		try {
			final Region node = AbstractHud.this.innerInit();
			assert node != null : "no node was loaded";
			node.sceneProperty().addListener(new ChangeListener<Scene>() {
				@Override
				public void changed(final ObservableValue<? extends Scene> observable, final Scene oldValue, final Scene newValue) {
					if (newValue == null) {
						node.prefWidthProperty().unbind();
						node.prefHeightProperty().unbind();
					} else {
						node.prefWidthProperty().bind(newValue.widthProperty());
						node.prefHeightProperty().bind(newValue.heightProperty());
					}
				}
			});
			return node;
		} catch (final Throwable t) {
			AbstractHud.this.setInnerError(t);
			return null;
		}
	}

	/**
	 * prepare load gui here, runs in jfx thread
	 * 
	 * @return
	 */
	protected abstract Region innerInit() throws Exception;

	public boolean isInitialized() {
		return this.initialized;
	}

	public Throwable getInnerError() {
		return this.innerError;
	}

	protected void setInnerError(final Throwable innerError) {
		this.innerError = innerError;
	}
}
