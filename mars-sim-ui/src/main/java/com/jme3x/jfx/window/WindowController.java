package com.jme3x.jfx.window;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import com.jme3x.jfx.FXMLUtils;

public class WindowController {

	@FXML
	public Region			bottomBar;
	@FXML
	public Region			leftBar;

	@FXML
	public Region			topLeftCorner;

	@FXML
	public Label			title;

	@FXML
	public Button			externalize;

	@FXML
	public Button			maximize;

	@FXML
	public Region			bottomRightBar;

	@FXML
	public Region			rightBar;

	@FXML
	public Region			topRightCorner;

	@FXML
	public BorderPane		contentBorderPane;

	@FXML
	public Region			bottomLeftCorner;

	@FXML
	public Region			titleBar;

	@FXML
	public Region			titleButtonBar;

	@FXML
	public Region			topBar;

	@FXML
	public Button			close;

	private AbstractWindow	window;

	protected Vector2d		preMaximizeSize		= new Vector2d(100, 100);
	protected Vector2d		preMaximizeLocation	= new Vector2d(0, 0);
	protected Stage			externalStage;
	private Vector2d		preExternalizeSize;

	@FXML
	public void initialize() {
		assert FXMLUtils.checkClassInjection(this);
	}

	public void setWindow(final AbstractWindow abstractWindow) {
		this.contentBorderPane.setCenter(abstractWindow.getWindowContent());
		this.window = abstractWindow;

		this.initDragging();
		this.initResize(this.topBar, Cursor.N_RESIZE);
		this.initResize(this.topLeftCorner, Cursor.NW_RESIZE);
		this.initResize(this.topRightCorner, Cursor.NE_RESIZE);

		this.initResize(this.bottomBar, Cursor.S_RESIZE);
		this.initResize(this.bottomLeftCorner, Cursor.SW_RESIZE);
		this.initResize(this.bottomRightBar, Cursor.SE_RESIZE);

		this.initResize(this.leftBar, Cursor.W_RESIZE);
		this.initResize(this.bottomLeftCorner, Cursor.SW_RESIZE);
		this.initResize(this.topLeftCorner, Cursor.NW_RESIZE);

		this.initResize(this.rightBar, Cursor.E_RESIZE);
		this.initResize(this.bottomRightBar, Cursor.SE_RESIZE);
		this.initResize(this.topRightCorner, Cursor.NE_RESIZE);

		this.title.textProperty().bind(this.window.titleProperty());

		this.close.disableProperty().bind(this.window.closeAbleProperty().not());
		this.maximize.disableProperty().bind(this.window.maximizeAbleProperty().not());

		this.maximize.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				boolean oldState = WindowController.this.window.maximizedProperty().get();
				boolean newState = !oldState;
				WindowController.this.window.maximizedProperty().set(newState);

				if (newState) {
					WindowController.this.maximize();
				} else {
					WindowController.this.deMaximize();
				}

			}
		});

		this.close.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				WindowController.this.window.close();
			}
		});

		this.externalize.disableProperty().bind(this.window.externalizeAbleProperty().not());
		this.externalize.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				boolean oldState = WindowController.this.window.externalized().get();
				WindowController.this.window.externalized().set(!oldState);
			}
		});

		this.window.externalized().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (WindowController.this.window.attached().getValue()) {
					WindowController.this.doNotCallMeExternalize(newValue);
				}
			}
		});

	}

	private void deMaximize() {
		WindowController.this.window.getNode().maxHeightProperty().unbind();
		WindowController.this.window.getNode().minHeightProperty().unbind();
		WindowController.this.window.getNode().maxWidthProperty().unbind();
		WindowController.this.window.getNode().minWidthProperty().unbind();
		// restore previous
		WindowController.this.window.getNode().setMinSize(WindowController.this.preMaximizeSize.x, WindowController.this.preMaximizeSize.y);
		WindowController.this.window.getNode().setMaxSize(WindowController.this.preMaximizeSize.x, WindowController.this.preMaximizeSize.y);

		WindowController.this.window.getNode().setLayoutX(WindowController.this.preMaximizeLocation.x);
		WindowController.this.window.getNode().setLayoutY(WindowController.this.preMaximizeLocation.y);
	}

	private void maximize() {
		WindowController.this.preMaximizeSize = new Vector2d(WindowController.this.window.getNode().getWidth(), WindowController.this.window.getNode().getHeight());
		WindowController.this.preMaximizeLocation = new Vector2d(WindowController.this.window.getNode().getLayoutX(), WindowController.this.window.getNode().getLayoutY());

		float[] margins = WindowController.this.getMargins();

		float heightReducer = margins[0] + margins[2];
		float widthReducer = margins[1] + margins[3];

		// bind so resizing with jme3 window works
		WindowController.this.window.getNode().maxWidthProperty().bind(WindowController.this.window.getNode().getScene().widthProperty().subtract(widthReducer));
		WindowController.this.window.getNode().minWidthProperty().bind(WindowController.this.window.getNode().getScene().widthProperty().subtract(widthReducer));
		WindowController.this.window.getNode().maxHeightProperty().bind(WindowController.this.window.getNode().getScene().heightProperty().subtract(heightReducer));
		WindowController.this.window.getNode().minHeightProperty().bind(WindowController.this.window.getNode().getScene().heightProperty().subtract(heightReducer));

		WindowController.this.window.getNode().setLayoutX(margins[3]);
		WindowController.this.window.getNode().setLayoutY(margins[0]);
	}

	private float[] getMargins() {
		float[] margins = null;
		if (WindowController.this.window.getResponsibleGuiManager() == null) { // allow useage outside of gui manager!
			margins = new float[4];
		} else {
			margins = WindowController.this.window.getResponsibleGuiManager().getWindowMargins();
		}
		return margins;
	}

	/**
	 * use the externalizedproperty instead!
	 *
	 * @param externalized
	 */
	public void doNotCallMeExternalize(boolean externalized) {
		if (WindowController.this.externalStage != null) {
			WindowController.this.externalStage.setScene(null);
			WindowController.this.externalStage.close();
		}

		if (externalized) {
			if (this.window.maximizedProperty().get()) {
				this.deMaximize();
			}
			Vector2d cursize = new Vector2d(this.window.getNode().getWidth(), this.window.getNode().getHeight());
			double preminW = Math.max(cursize.x, this.calculateMinWidth(cursize));
			double preminH = Math.max(cursize.y, this.calculateMinHeight(cursize));
			this.preExternalizeSize = new Vector2d(preminW, preminH);

			double actualMinimumSizeX = this.calculateMinWidth(cursize);
			double actualMinimumSizeY = this.calculateMinHeight(cursize);

			this.window.getNode().setMinWidth(actualMinimumSizeX);
			this.window.getNode().setMinHeight(actualMinimumSizeY);

			WindowController.this.window.getResponsibleGuiManager().getRootGroup().getChildren().remove(WindowController.this.window.getNode());
			WindowController.this.externalStage = new Stage(StageStyle.UNDECORATED);
			WindowController.this.externalStage.titleProperty().bind(WindowController.this.window.titleProperty());
			double maxMinW = Math.max(WindowController.this.window.getNode().getWidth(), this.calculateMinWidth(cursize));
			double maxMinH = Math.max(WindowController.this.window.getNode().getHeight(), this.calculateMinHeight(cursize));
			WindowController.this.externalStage.setScene(new Scene(WindowController.this.window.getNode(), maxMinW, maxMinH));
			WindowController.this.window.getNode().setLayoutX(0);
			WindowController.this.window.getNode().setLayoutY(0);
			WindowController.this.externalStage.show();
		} else {
			this.window.getNode().setMinWidth(this.preExternalizeSize.x);
			this.window.getNode().setMaxWidth(this.preExternalizeSize.x);
			this.window.getNode().setMinHeight(this.preExternalizeSize.y);
			this.window.getNode().setMaxHeight(this.preExternalizeSize.y);
			WindowController.this.window.getResponsibleGuiManager().getRootGroup().getChildren().add(WindowController.this.window.getNode());
		}
	}

	private void initResize(Region draggable, Cursor cursor) {
		boolean resizeable = this.window.resizableProperty().get();
		final Vector2d initialMousePos = new Vector2d();
		final Vector2d initialSize = new Vector2d();
		final Vector2d initialPos = new Vector2d();
		draggable.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (resizeable) {
					initialSize.x = WindowController.this.window.getNode().getWidth();
					initialSize.y = WindowController.this.window.getNode().getHeight();

					if (WindowController.this.window.externalized().get()) {
						initialPos.x = WindowController.this.externalStage.getX();
						initialPos.y = WindowController.this.externalStage.getY();

					} else {
						initialPos.x = WindowController.this.window.getNode().getLayoutX();
						initialPos.y = WindowController.this.window.getNode().getLayoutY();
					}

					initialMousePos.x = mouseEvent.getScreenX();
					initialMousePos.y = mouseEvent.getScreenY();
					draggable.setCursor(cursor);
				}
			}
		});
		draggable.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (resizeable) {
					draggable.setCursor(cursor);
				}
			}
		});
		draggable.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (resizeable) {
					WindowController.this.resize(cursor, initialMousePos, new Vector2d(mouseEvent.getScreenX(), mouseEvent.getScreenY()), initialSize, initialPos);
				}
			}
		});
		draggable.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (resizeable) {
					draggable.setCursor(cursor);
				}
			}
		});
	}

	protected void resize(Cursor cursor, Vector2d initialMousePos, Vector2d currentMousePos, Vector2d initialSize, Vector2d initialPos) {
		Vector2d mouseDelta = currentMousePos.subtract(initialMousePos);
		double actualMinimumSizeX = this.calculateMinWidth(initialSize);
		double actualMinimumSizeY = this.calculateMinHeight(initialSize);

		if (cursor == Cursor.E_RESIZE || cursor == Cursor.NE_RESIZE || cursor == Cursor.SE_RESIZE) {
			double newXSize = initialSize.x + mouseDelta.x;
			if (newXSize < actualMinimumSizeX) {
				return;
			}
			if (this.window.externalized().get()) {
				this.externalStage.setMinWidth(Math.max(newXSize, 5));
				this.externalStage.setMaxWidth(Math.max(newXSize, 5));
			} else {
				if (this.window.getNode().getLayoutX() + newXSize < this.window.getNode().getScene().getWidth() && newXSize >= actualMinimumSizeX && currentMousePos.getX() >= 0) {
					this.window.getNode().setMinWidth(Math.max(newXSize, 5));
					this.window.getNode().setMaxWidth(Math.max(newXSize, 5));
				}
			}

		}

		if (cursor == Cursor.W_RESIZE || cursor == Cursor.NW_RESIZE || cursor == Cursor.SW_RESIZE) {
			double newXSize = initialSize.x - mouseDelta.x;
			if (newXSize < actualMinimumSizeX) {
				return;
			}
			if (this.window.externalized().get()) {
				this.externalStage.setMinWidth(Math.max(newXSize, 5));
				this.externalStage.setMaxWidth(Math.max(newXSize, 5));
				this.externalStage.setX(initialPos.x + mouseDelta.x);
			} else {
				if (initialPos.x + mouseDelta.x >= 0 && currentMousePos.getX() >= 0) {
					this.window.getNode().setMinWidth(Math.max(newXSize, 5));
					this.window.getNode().setMaxWidth(Math.max(newXSize, 5));
					this.window.setLayoutX(initialPos.x + mouseDelta.x);
				}
			}
		}

		if (cursor == Cursor.N_RESIZE || cursor == Cursor.NE_RESIZE || cursor == Cursor.NW_RESIZE) {
			double newYSize = initialSize.y - mouseDelta.y;
			if (newYSize < actualMinimumSizeY) {
				return;
			}
			if (this.window.externalized().get()) {
				this.externalStage.setMinHeight(Math.max(newYSize, 5));
				this.externalStage.setMaxHeight(Math.max(newYSize, 5));
				this.externalStage.setY(initialPos.y + mouseDelta.y);
			} else {
				if (initialPos.y + mouseDelta.y >= 0 && currentMousePos.getY() >= 0) {
					this.window.getNode().setMinHeight(Math.max(newYSize, 5));
					this.window.getNode().setMaxHeight(Math.max(newYSize, 5));
					this.window.setLayoutY(initialPos.y + mouseDelta.y);
				}
			}
		}

		if (cursor == Cursor.S_RESIZE || cursor == Cursor.SE_RESIZE || cursor == Cursor.SW_RESIZE) {
			double newYSize = initialSize.y + mouseDelta.y;
			if (newYSize < actualMinimumSizeX) {
				return;
			}
			if (this.window.externalized().get()) {
				this.externalStage.setMinHeight(Math.max(newYSize, 5));
				this.externalStage.setMaxHeight(Math.max(newYSize, 5));
			} else {
				if (this.window.getNode().getLayoutY() + newYSize < this.window.getNode().getScene().getHeight() && currentMousePos.getY() >= 0) {
					this.window.getNode().setMinHeight(Math.max(newYSize, 5));
					this.window.getNode().setMaxHeight(Math.max(newYSize, 5));
				}
			}
		}

		// sanity checks,
		// prevent in this case to resize a window so, that the titlebar is no longer reachable
		if (this.window.getNode().getLayoutY() < 0) {
			this.window.getNode().setLayoutY(0);
		}
	}

	private double calculateMinWidth(Vector2d initialSize) {
		double lb = this.leftBar.getMinWidth();
		double rb = this.rightBar.getMinWidth();
		double contentw = this.window.getWindowContent().minWidth(initialSize.y);
		double tbminW = this.topBar.getMinWidth();
		return lb + rb + Math.max(contentw, tbminW);
	}

	private double calculateMinHeight(Vector2d initialSize) {
		double bbh = this.bottomBar.getMinHeight();
		double tbh = this.titleBar.getMinHeight();
		double tb = this.topBar.getMinHeight();
		double content = this.window.getWindowContent().minHeight(initialSize.x);
		return bbh + tbh + tb + content;
	}

	private void initDragging() {
		boolean move = this.window.moveAbleProperty().get();
		final Vector2d dragDelta = new Vector2d();
		this.titleBar.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (move) {
					dragDelta.x = WindowController.this.window.getNode().getLayoutX() - mouseEvent.getSceneX();
					dragDelta.y = WindowController.this.window.getNode().getLayoutY() - mouseEvent.getSceneY();
					WindowController.this.titleBar.setCursor(Cursor.MOVE);
				}
			}
		});
		this.titleBar.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (move) {
					WindowController.this.titleBar.setCursor(Cursor.HAND);
				}
			}
		});
		this.titleBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (move) {
					if (WindowController.this.window.externalized().get()) {
						WindowController.this.externalStage.setX(mouseEvent.getScreenX() + dragDelta.x);
						WindowController.this.externalStage.setY(mouseEvent.getScreenY() + dragDelta.y);
					} else {
						if (mouseEvent.getSceneX() < 0) {
							return;
						}
						if (mouseEvent.getSceneY() < 0) {
							return;
						}

						if (mouseEvent.getSceneX() > WindowController.this.window.getNode().getScene().getWidth()) {
							return;
						}
						if (mouseEvent.getSceneY() > WindowController.this.window.getNode().getScene().getHeight()) {
							return;
						}
						WindowController.this.window.getNode().setLayoutX(mouseEvent.getSceneX() + dragDelta.x);
						WindowController.this.window.getNode().setLayoutY(Math.max(mouseEvent.getSceneY() + dragDelta.y, 0));
					}
				}
			}
		});
		this.titleBar.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (move) {
					WindowController.this.titleBar.setCursor(Cursor.HAND);
				}
			}
		});
	}

}
