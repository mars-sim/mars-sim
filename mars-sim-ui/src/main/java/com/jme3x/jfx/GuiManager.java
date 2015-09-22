package com.jme3x.jfx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.input.RawInputListener;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import com.jme3.ui.Picture;
import com.jme3x.jfx.cursor.ICursorDisplayProvider;
import com.jme3x.jfx.window.AbstractWindow;
import com.sun.javafx.cursor.CursorType;

public class GuiManager {

	private static final Logger		logger			= LoggerFactory.getLogger(GuiManager.class);

	private JmeFxScreenContainer	jmefx;
	private Group					highLevelGroup;

	/**
	 * a list of all attached huds, using copyonwrite to allow reading from other threads in a save way
	 */
	private List<AbstractHud>		attachedHuds	= new CopyOnWriteArrayList<>();
	private Material				customMaterial;

	public Group getRootGroup() {
		return this.highLevelGroup;
	}

	public JmeFxScreenContainer getjmeFXContainer() {
		return this.jmefx;
	}

	public GuiManager(final Node guiParent, final AssetManager assetManager, final Application application, final boolean fullscreen, final ICursorDisplayProvider cursorDisplayProvider) {
		this(guiParent, assetManager, application, fullscreen, cursorDisplayProvider, new Material(assetManager, "Common/MatDefs/Gui/Gui.j3md"));
		this.getCustomMaterial().setColor("Color", ColorRGBA.White);
	}

	/**
	 * creates a new JMEFX container, this is a rather expensive operation and should only be done one time fr the 2d fullscreengui. Additionals should only be necessary for 3d guis, should be called from JME thread
	 *
	 * @param guiParent
	 * @param assetManager
	 * @param application
	 * @param fullscreen
	 * @param customMaterial
	 *            allows to specify a own Material for the gui, requires the MaterialParamter Texture with type Texture, wich will contain the RenderTarget of jfx
	 * 
	 */
	public GuiManager(final Node guiParent, final AssetManager assetManager, final Application application, final boolean fullscreen, final ICursorDisplayProvider cursorDisplayProvider, final Material customMaterial) {
		this(guiParent, assetManager, application, fullscreen, cursorDisplayProvider, customMaterial, true);
	}

	/**
	 * creates a new JMEFX container, this is a rather expensive operation and should only be done one time fr the 2d fullscreengui. Additionals should only be necessary for 3d guis, should be called from JME thread
	 *
	 * @param guiParent
	 * @param assetManager
	 * @param application
	 * @param fullscreen
	 * @param customMaterial
	 *            allows to specify a own Material for the gui, requires the MaterialParamter Texture with type Texture, wich will contain the RenderTarget of jfx
	 * @param useRecommendedJFXSettings
	 *            -> apply some default settings that are recommended
	 * 
	 */
	public GuiManager(final Node guiParent, final AssetManager assetManager, final Application application, final boolean fullscreen, final ICursorDisplayProvider cursorDisplayProvider, final Material customMaterial, boolean useRecommendedJFXSettings) {
		this(guiParent, assetManager, application, fullscreen, cursorDisplayProvider, customMaterial, useRecommendedJFXSettings, false);

	}

	/**
	 * creates a new JMEFX container, this is a rather expensive operation and should only be done one time fr the 2d fullscreengui. Additionals should only be necessary for 3d guis, should be called from JME thread
	 *
	 * @param guiParent
	 * @param assetManager
	 * @param application
	 * @param fullscreen
	 * @param customMaterial
	 *            allows to specify a own Material for the gui, requires the MaterialParamter Texture with type Texture, wich will contain the RenderTarget of jfx
	 * @param useRecommendedJFXSettings
	 *            -> apply some default settings that are recommended
	 * 
	 */
	public GuiManager(final Node guiParent, final AssetManager assetManager, final Application application, final boolean fullscreen, final ICursorDisplayProvider cursorDisplayProvider, final Material customMaterial,
			boolean useRecommendedJFXSettings, boolean resumeJMEFocusOnEventFail) {
		if (useRecommendedJFXSettings) {

			System.setProperty("javafx.animation.fullspeed", "true"); // reduce laggyness of animations, bad for business apps great for games
			System.setProperty("prism.order", "sw"); // use software rendering, keep the gpu free for jme, use another core for jfx in software mode and all win
			System.setProperty("prism.vsync", "false"); // jme should limit rendering speed anyway or?

		}

		this.customMaterial = customMaterial;
		this.jmefx = JmeFxContainer.install(application, guiParent, fullscreen, cursorDisplayProvider);
		this.initMaterial(this.jmefx.getJmeNode());

		if (cursorDisplayProvider != null) {
			for (final CursorType type : CursorType.values()) {
				cursorDisplayProvider.setup(type);
			}
		}
		this.initRootGroup(resumeJMEFocusOnEventFail);

	}

	private void initMaterial(final Picture jmeNode) {
		this.getCustomMaterial().getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

		final MatParam jfxRenderTarget = jmeNode.getMaterial().getParam("Texture");
		assert this.customMaterial.getMaterialDef().getMaterialParam("Texture") != null : "CustomMaterial must habe Texture parameter";
		this.customMaterial.setTexture("Texture", (Texture) jfxRenderTarget.getValue());
		jmeNode.setMaterial(this.customMaterial);
	}

	private void initRootGroup(boolean redirectFocusToJMEOnUnusedKeyEvents) {
		final Semaphore waitForInit = new Semaphore(0);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				GuiManager.this.highLevelGroup = new Group();

				if (redirectFocusToJMEOnUnusedKeyEvents) {
					GuiManager.this.highLevelGroup.addEventHandler(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
						@Override
						public void handle(KeyEvent event) {
							// if this is called the event was not consumed, like textfield ect. do -> transfer it back to JME!
							GuiManager.this.jmefx.loseFocus();
							System.out.println(event.getEventType() + " Unhandled keyevent " + event.isConsumed());
						};
					});

				}

				// ensure that on every focues change between windows/huds modality is preserved!
				GuiManager.this.highLevelGroup.getChildren().addListener(new ListChangeListener<Object>() {
					boolean	ignoreEvents	= false;

					@Override
					public void onChanged(final Change<?> c) {
						// ensure it is not triggerd by the events it produces
						if (this.ignoreEvents) {
							return;
						}
						this.ignoreEvents = true;
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								GuiManager.this.sortWindowsBeforeHudsAndEnforceModality();
								ignoreEvents = false;
							}
						});
					}
				});

				final Scene scene = new Scene(GuiManager.this.highLevelGroup);
				scene.setFill(new Color(0, 0, 0, 0));
				GuiManager.this.jmefx.setScene(scene, GuiManager.this.highLevelGroup);

				waitForInit.release();
			}
		});
		waitForInit.acquireUninterruptibly();
	}

	/**
	 * bind your input suppliery here, for 2d the normal inputmanager will suffice, Events are expected to be in the JME thread
	 *
	 * @return
	 */
	public RawInputListener getInputRedirector() {
		return this.jmefx.inputListener;
	}

	/**
	 * removes a hud, if this is not called in the jfx thread this is done async, else it is done instantly
	 *
	 * @param hud
	 */
	public void detachHudAsync(final AbstractHud hud) {
		if (hud == null) {
			GuiManager.logger.warn("trying to remove null hud!");
			return;
		}
		final Runnable attachTask = new Runnable() {
			@Override
			public void run() {
				if (!hud.isAttached()) {
					return;
				}
				GuiManager.logger.debug("Detaching {}", hud);
				GuiManager.this.attachedHuds.remove(hud);
				if (hud instanceof AbstractWindow) {
					AbstractWindow window = (AbstractWindow) hud;
					window.onClose();
				}
				GuiManager.this.highLevelGroup.getChildren().remove(hud.getNode());
				hud.setAttached(false, null);
			}
		};
		FxPlatformExecutor.runOnFxApplication(attachTask);
	}

	/**
	 * adds a hud, if this is not called in the jfx thread this is done async, else it is done instantly
	 *
	 * @param hud
	 */
	public void attachHudAsync(final AbstractHud hud) {
		final Runnable attachTask = new Runnable() {
			@Override
			public void run() {
				if (hud.isAttached()) {
					return;
				}
				GuiManager.logger.debug("Attaching {}", hud);
				assert !GuiManager.this.attachedHuds.contains(hud) : "Duplicated attach of " + hud + " isAttached state error?";
				if (!hud.isInitialized()) {
					GuiManager.logger.warn("Late init of {} call initialize early to prevent microlags", hud.getClass().getName());
					hud.precache();
				}
				GuiManager.this.attachedHuds.add(hud);
				GuiManager.this.highLevelGroup.getChildren().add(hud.getNode());
				hud.setAttached(true, GuiManager.this);
				if (hud instanceof AbstractWindow) {
					// TODO find way to calculate size without render 1 frame
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							final AbstractWindow casted = (AbstractWindow) hud;
							if (casted.externalized().get()) {
								casted.doNotCallMeExternalize(true); // spawn as externalized window!
							}
						}
					});

				}

			}
		};
		FxPlatformExecutor.runOnFxApplication(attachTask);
	}

	/**
	 * expected bahaviour, if a window is attached, move it to front <br>
	 * if a hud is attached move it behind all windows, but before already existing huds <br>
	 * dont change order of windows or order of huds.
	 **/
	private void sortWindowsBeforeHudsAndEnforceModality() {
		// TODO efficiency

		final ObservableList<javafx.scene.Node> currentOrder = this.highLevelGroup.getChildren();

		// read current order and split by windows and huds
		final ArrayList<AbstractWindow> orderedWindows = new ArrayList<>();
		final ArrayList<AbstractWindow> orderedModalWindows = new ArrayList<>();
		final ArrayList<AbstractHud> orderdHuds = new ArrayList<>();

		final ArrayList<javafx.scene.Node> others = new ArrayList<>();
		boolean switchToModal = false;
		nextNode: for (final javafx.scene.Node n : currentOrder) {
			for (final AbstractHud hud : this.attachedHuds) {
				if (hud.getNode() == n) {
					if (hud instanceof AbstractWindow) {
						final AbstractWindow casted = (AbstractWindow) hud;
						if (casted.modalProperty().get()) {
							if (currentOrder.get(0) == casted.getNode()) {
								switchToModal = true;
							}
							orderedModalWindows.add((AbstractWindow) hud);
							continue nextNode;
						} else {
							orderedWindows.add((AbstractWindow) hud);
							continue nextNode;
						}
					} else {
						orderdHuds.add(hud);
						continue nextNode;
					}
				}
			}
			others.add(n);
		}

		// clean current list, add huds first then windows

		currentOrder.clear();
		// put everything else somewhare + ugly hack for dragimage
		for (final javafx.scene.Node other : others) {
			if (!"dragimage:true;".equals(other.getStyle())) {
				currentOrder.add(other);
			}
		}
		for (final AbstractHud hud : orderdHuds) {
			// disable them if a modal window exist(till a better solution is found for input interception)
			hud.getNode().disableProperty().set(orderedModalWindows.size() > 0);
			currentOrder.add(hud.getNode());
		}
		for (final AbstractWindow window : orderedWindows) {
			// disable them if a modal window exist(till a better solution is found for input interception)
			window.getNode().disableProperty().set(orderedModalWindows.size() > 0);
			currentOrder.add(window.getNode());
		}
		for (final AbstractWindow modalWindow : orderedModalWindows) {
			currentOrder.add(modalWindow.getNode());
			modalWindow.getNode().requestFocus();
		}
		// ugly hack to make sure the dragimage stays on front
		for (final javafx.scene.Node other : others) {
			if ("dragimage:true;".equals(other.getStyle())) {
				currentOrder.add(other);
			}
		}
		if (!switchToModal && (orderedModalWindows.size() > 0)) {
			GuiManager.logger.warn("TODO FocusDenied sound/visual representation");
		}

	}

	public List<AbstractHud> getAttachedHuds() {
		return Collections.unmodifiableList(this.attachedHuds);
	}

	/**
	 * this inputlistener recives all! events, even those that are normally consumed by JFX. <br>
	 * Usecase
	 *
	 * @param rawInputListenerAdapter
	 */
	public void setEverListeningRawInputListener(final RawInputListener rawInputListenerAdapter) {
		this.jmefx.setEverListeningRawInputListener(rawInputListenerAdapter);
	}

	private Material getCustomMaterial() {
		return this.customMaterial;
	}

	float[]	insets	= new float[4];

	/**
	 * used for maximized window and window movements to restrict certains areas, eg a custom taskbar order is up right down left <br>
	 * not a copy!
	 * 
	 * @return
	 */
	public float[] getWindowMargins() {
		return this.insets;
	}
}
