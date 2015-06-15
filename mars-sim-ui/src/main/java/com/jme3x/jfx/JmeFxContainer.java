package com.jme3x.jfx;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.PopupWindow;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import com.jme3.app.Application;
import com.jme3.input.RawInputListener;
import com.jme3.texture.Image;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;
import com.jme3x.jfx.cursor.ICursorDisplayProvider;
import com.jme3x.jfx.util.FormatUtils;
import com.sun.glass.ui.Accessible;
import com.sun.glass.ui.Pixels;
import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.cursor.CursorType;
import com.sun.javafx.embed.AbstractEvents;
import com.sun.javafx.embed.EmbeddedSceneInterface;
import com.sun.javafx.embed.EmbeddedStageInterface;
import com.sun.javafx.embed.HostInterface;
import com.sun.javafx.scene.SceneHelper;
import com.sun.javafx.scene.SceneHelper.SceneAccessor;
import com.sun.javafx.stage.EmbeddedWindow;

/**
 * Need to pass -Dprism.dirtyopts=false on startup
 *
 * @author abies / Artur Biesiadowski
 */

public abstract class JmeFxContainer {

	private List<IFocusListener>		jfxFocusListeners	= new ArrayList<>();

	EmbeddedStageInterface				stagePeer;
	EmbeddedSceneInterface				scenePeer;
	protected volatile EmbeddedWindow	stage;
	HostInterface						hostContainer;
	JmeFXInputListener					inputListener;
	int									pWidth;
	int									pHeight;
	volatile Scene						scene;
	Image								jmeImage;
	Texture2D							tex;
	ByteBuffer							jmeData;
	int									alphaByteOffset		= 3;
	ByteBuffer							fxData;
	boolean								fxDataReady			= false;
	int									oldX				= -1;
	int									oldY				= -1;
	boolean								focus;
	Application							app;
	boolean								fullScreenSuppport;
	CompletableFuture<Format>			nativeFormat		= new CompletableFuture<Format>();
	ICursorDisplayProvider				cursorDisplayProvider;
	private Parent						rootNode;

	private Function<ByteBuffer, Void>	reorderData;

	/** Indent the window position to account for window decoration by Ronn */
	private int							windowOffsetX;
	private int							windowOffsetY;

	public static JmeFxScreenContainer install(final Application app, final com.jme3.scene.Node guiNode, final boolean fullScreenSupport, final ICursorDisplayProvider cursorDisplayProvider) {
		final JmeFxScreenContainer ctr = new JmeFxScreenContainer(app.getAssetManager(), app, fullScreenSupport, cursorDisplayProvider);
		guiNode.attachChild(ctr.getJmeNode());
		ctr.inputListener = new JmeFXInputListener(ctr);
		app.getInputManager().addRawInputListener(ctr.inputListener);

		if (fullScreenSupport) {
			JmeFxContainer.installSceneAccessorHack();
		}

		return ctr;
	}

	public static JmeFxTextureContainer createTextureContainer(Application app, int width, int height) {
		final JmeFxTextureContainer ctr = new JmeFxTextureContainer(app, width, height);

		JmeFxContainer.installSceneAccessorHack();

		return ctr;
	}

	public JmeFXInputListener getInputListener() {
		return this.inputListener;
	}

	protected JmeFxContainer() {
		this.initFx();
	}

	protected EmbeddedSceneInterface getScenePeer() {
		return this.scenePeer;
	}

	protected EmbeddedStageInterface getStagePeer() {
		return this.stagePeer;
	}

	public int getWindowX() {
		return 0;
	}

	public int getWindowY() {
		return 0;
	}

	private void initFx() {
		PlatformImpl.startup(new Runnable() {

			@Override
			public void run() {
				// TODO 3.1: use Format.ARGB8 and Format.BGRA8 and remove used of exchangeData, fx2jme_ARGB82ABGR8,...
				switch (Pixels.getNativeFormat()) {
				case Pixels.Format.BYTE_ARGB:
					try {
						JmeFxContainer.this.nativeFormat.complete(Format.valueOf("ARGB8"));
						JmeFxContainer.this.reorderData = null;
						JmeFxContainer.this.alphaByteOffset = 0;
					} catch (Exception exc) {
						JmeFxContainer.this.nativeFormat.complete(Format.ABGR8);
						JmeFxContainer.this.reorderData = FormatUtils::reorder_ARGB82ABGR8;
						JmeFxContainer.this.alphaByteOffset = 0;
					}
					break;
				case Pixels.Format.BYTE_BGRA_PRE:
					try {
						JmeFxContainer.this.nativeFormat.complete(Format.valueOf("BGRA8"));
						JmeFxContainer.this.reorderData = null;
						JmeFxContainer.this.alphaByteOffset = 3;
					} catch (Exception exc) {
						JmeFxContainer.this.nativeFormat.complete(Format.ABGR8);
						JmeFxContainer.this.reorderData = FormatUtils::reorder_BGRA82ABGR8;
						JmeFxContainer.this.alphaByteOffset = 0;
					}
					break;
				default:
					try {
						JmeFxContainer.this.nativeFormat.complete(Format.valueOf("ARGB8"));
						JmeFxContainer.this.reorderData = null;
						JmeFxContainer.this.alphaByteOffset = 0;
					} catch (Exception exc) {
						JmeFxContainer.this.nativeFormat.complete(Format.ABGR8);
						JmeFxContainer.this.reorderData = FormatUtils::reorder_ARGB82ABGR8;
						JmeFxContainer.this.alphaByteOffset = 0;
					}
					break;
				}
			}
		});

	}

	void setFxEnabled(final boolean enabled) {
	}

	public Scene getScene() {
		return this.scene;
	}

	public EmbeddedWindow getStage() {
		return this.stage;
	}

	public void setScene(final Scene newScene) {
		this.setScene(newScene, newScene.getRoot());
	}

	public void setScene(final Scene newScene, final Parent highLevelGroup) {
		this.rootNode = highLevelGroup;
		FxPlatformExecutor.runOnFxApplication(new Runnable() {

			@Override
			public void run() {
				JmeFxContainer.this.setSceneImpl(newScene);
			}
		});
	}

	/*
	 * Called on JavaFX app thread.
	 */

	protected void setSceneImpl(final Scene newScene) {
		if (this.stage != null && newScene == null) {
			this.stage.hide();
			this.stage = null;
		}

		this.scene = newScene;
		if (this.stage == null && newScene != null) {
			this.stage = new EmbeddedWindow(this.hostContainer);
		}
		if (this.stage != null) {
			this.stage.setScene(newScene);
			if (!this.stage.isShowing()) {
				this.stage.show();
			}
		}

		JmeFxContainer.sceneContainerMap.put(this.stage, this);
	}

	protected final Semaphore	imageExchange	= new Semaphore(1);
	public CursorType			lastcursor;

	void paintComponent() {
		if (this.scenePeer == null) {
			return;
		}

		final boolean lock = this.imageExchange.tryAcquire();
		if (!lock) {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					JmeFxContainer.this.paintComponent();
				}
			});
			return;
		}

		try {

			final ByteBuffer data = this.fxData;
			data.clear();

			final IntBuffer buf = data.asIntBuffer();

			if (!this.scenePeer.getPixels(buf, this.pWidth, this.pHeight)) {
				return;
			}

			if (this.fullScreenSuppport) {
				for (final PopupSnapper ps : this.activeSnappers) {
					ps.paint(buf, this.pWidth, this.pHeight);
				}
			}

			data.flip();
			data.limit(this.pWidth * this.pHeight * 4);
			if (this.reorderData != null) {
				this.reorderData.apply(data);
				data.position(0);
			}
			this.fxDataReady = true;

		} catch (final Exception exc) {
			exc.printStackTrace();
		} finally {
			this.imageExchange.release();
		}
		this.app.enqueue(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				final boolean updateImage = JmeFxContainer.this.imageExchange.tryAcquire();
				// we update only if we can do that in nonblocking mode
				// if would need to block, it means that another callable with
				// newer data will be
				// enqueued soon, so we can just ignore this repaint
				if (updateImage) {
					try {
						if (JmeFxContainer.this.fxDataReady) {
							JmeFxContainer.this.fxDataReady = false;
							final ByteBuffer tmp = JmeFxContainer.this.jmeData;
							JmeFxContainer.this.jmeData = JmeFxContainer.this.fxData;
							JmeFxContainer.this.fxData = tmp;
						}
					} finally {
						JmeFxContainer.this.imageExchange.release();
					}
					JmeFxContainer.this.jmeImage.setData(JmeFxContainer.this.jmeData);
				} else {
					// System.out.println("Skipping update due to contention");
				}
				return null;
			}
		});

	}

	boolean[]	mouseButtonState	= new boolean[3];

	public boolean isCovered(final int x, final int y) {
		if (x < 0 || x >= this.pWidth) {
			return false;
		}
		if (y < 0 || y >= this.pHeight) {
			return false;
		}
		final ByteBuffer data = this.jmeImage.getData(0);
		data.limit(data.capacity());
		final int alpha = data.get(this.alphaByteOffset + 4 * (y * this.pWidth + x));
		data.limit(0);
		return alpha != 0;
	}

	public void grabFocus() {
		if (!this.focus && this.stagePeer != null) {
			this.stagePeer.setFocused(true, AbstractEvents.FOCUSEVENT_ACTIVATED);
			this.focus = true;
			for (IFocusListener jfxFocusListener : this.jfxFocusListeners) {
				jfxFocusListener.onJFXFocusGain();
			}

		}
	}

	public void loseFocus() {
		if (this.focus && this.stagePeer != null) {
			this.stagePeer.setFocused(false, AbstractEvents.FOCUSEVENT_DEACTIVATED);
			this.focus = false;
			for (IFocusListener jfxFocusListener : this.jfxFocusListeners) {
				jfxFocusListener.onJFXFocusLoose();
			}
		}
	}

	private final BitSet	keyStateSet	= new BitSet(0xFF);

	int retrieveKeyState() {
		int embedModifiers = 0;

		if (this.keyStateSet.get(KeyEvent.VK_SHIFT)) {
			embedModifiers |= AbstractEvents.MODIFIER_SHIFT;
		}

		if (this.keyStateSet.get(KeyEvent.VK_CONTROL)) {
			embedModifiers |= AbstractEvents.MODIFIER_CONTROL;
		}

		if (this.keyStateSet.get(KeyEvent.VK_ALT)) {
			embedModifiers |= AbstractEvents.MODIFIER_ALT;
		}

		if (this.keyStateSet.get(KeyEvent.VK_META)) {
			embedModifiers |= AbstractEvents.MODIFIER_META;
		}
		return embedModifiers;
	}

	Map<Window, PopupSnapper>			snappers			= new IdentityHashMap<>();
	List<PopupSnapper>					activeSnappers		= new CopyOnWriteArrayList<>();
	static Map<Window, JmeFxContainer>	sceneContainerMap	= new ConcurrentHashMap<>();

	static boolean						sceneAccessorHackInstalled;

	static void installSceneAccessorHack() {

		if (JmeFxContainer.sceneAccessorHackInstalled) {
			return;
		}

		try {
			final Field f = SceneHelper.class.getDeclaredField("sceneAccessor");
			f.setAccessible(true);
			final SceneAccessor orig = (SceneAccessor) f.get(null);

			final SceneAccessor sa = new SceneAccessor() {

				@Override
				public void setPaused(final boolean paused) {
					orig.setPaused(paused);
				}

				@Override
				public void parentEffectiveOrientationInvalidated(final Scene scene) {
					orig.parentEffectiveOrientationInvalidated(scene);
				}

				@Override
				public Accessible getAccessible(Scene scene) {
					return null;
				}

				@Override
				public Camera getEffectiveCamera(final Scene scene) {
					return orig.getEffectiveCamera(scene);
				}

				@Override
				public Scene createPopupScene(final Parent root) {
					final Scene scene = orig.createPopupScene(root);

					scene.windowProperty().addListener(new ChangeListener<Window>() {

						@Override
						public void changed(final javafx.beans.value.ObservableValue<? extends Window> observable, final Window oldValue, final Window window) {
							window.addEventHandler(WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>() {

								@Override
								public void handle(final WindowEvent event) {
									JmeFxContainer container = JmeFxContainer.sceneContainerMap.get(((PopupWindow) window).getOwnerWindow());
									if (container != null) {
										final PopupSnapper ps = new PopupSnapper(container, window, scene);
										synchronized (container.snappers) {
											container.snappers.put(window, ps);
										}
										ps.start();
									}

								}
							});
						};
					});

					scene.windowProperty().addListener(new ChangeListener<Window>() {

						@Override
						public void changed(final javafx.beans.value.ObservableValue<? extends Window> observable, final Window oldValue, final Window window) {
							window.addEventHandler(WindowEvent.WINDOW_HIDDEN, new EventHandler<WindowEvent>() {

								@Override
								public void handle(final WindowEvent event) {
									JmeFxContainer container = JmeFxContainer.sceneContainerMap.get(((PopupWindow) window).getOwnerWindow());
									if (container != null) {

										final PopupSnapper ps;
										synchronized (container.snappers) {
											ps = container.snappers.remove(window);
										}
										if (ps == null) {
											System.out.println("Cannot find snapper for window " + window);
										} else {
											ps.stop();
										}
									}
								}
							});
						};
					});

					return scene;
				}

				@Override
				public void setTransientFocusContainer(final Scene scene, final javafx.scene.Node node) {

				}
			};

			f.set(null, sa);
		} catch (final Exception exc) {
			exc.printStackTrace();
		}

		JmeFxContainer.sceneAccessorHackInstalled = true;
	}

	/**
	 * call via gui manager!
	 *
	 * @param rawInputListenerAdapter
	 */
	public void setEverListeningRawInputListener(final RawInputListener rawInputListenerAdapter) {
		this.inputListener.setEverListeningRawInputListener(rawInputListenerAdapter);
	}

	public Parent getRootNode() {
		return this.rootNode;
	}

	/**
	 * Indent the window position to account for window decoration.
	 */
	public void setWindowOffsetX(final int windowOffsetX) {
		this.windowOffsetX = windowOffsetX;
	}

	/**
	 * Indent the window position to account for window decoration.
	 */
	public void setWindowOffsetY(final int windowOffsetY) {
		this.windowOffsetY = windowOffsetY;
	}

	/**
	 * Indent the window position to account for window decoration.
	 */
	public int getWindowOffsetX() {
		return this.windowOffsetX;
	}

	/**
	 * Indent the window position to account for window decoration.
	 */
	public int getWindowOffsetY() {
		return this.windowOffsetY;
	}

	public void dispose() {
		if (this.tex != null)
			this.tex.setImage(null);
		if (this.jmeImage != null)
			this.jmeImage.dispose();
		if (this.jmeData != null)
			BufferUtils.destroyDirectBuffer(this.jmeData);
		if (this.fxData != null)
			BufferUtils.destroyDirectBuffer(this.fxData);
	}

	@Override
	protected void finalize() throws Throwable {
		this.dispose();
		super.finalize();
	}

	public ObservableList<Node> getRootChildren() {
		if (this.rootNode instanceof Group) {
			return ((Group) this.rootNode).getChildren();
		} else if (this.rootNode instanceof Pane) {
			return ((Pane) this.rootNode).getChildren();
		} else {
			return FXCollections.emptyObservableList();
		}
	}

	public abstract int getXPosition();

	public abstract int getYPosition();

	public boolean addFocusListener(IFocusListener listener) {
		assert Platform.isFxApplicationThread();
		return this.jfxFocusListeners.add(listener);
	}

	public boolean removeFocusListener(IFocusListener listener) {
		assert Platform.isFxApplicationThread();
		return this.jfxFocusListeners.remove(listener);
	}
}