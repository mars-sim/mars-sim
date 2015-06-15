package com.jme3x.jfx;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

//based on work by Alexander Berg http://stackoverflow.com/questions/19455059/allow-user-to-resize-an-undecorated-stage
public class ResizeAndMoveHelper {

	public static void addResizeListener(final Stage stage, final BorderPane menu) {
		final ResizeListener resizeListener = new ResizeListener(stage, menu);
		stage.getScene().addEventHandler(MouseEvent.MOUSE_MOVED, resizeListener);
		stage.getScene().addEventHandler(MouseEvent.MOUSE_PRESSED, resizeListener);
		stage.getScene().addEventHandler(MouseEvent.MOUSE_DRAGGED, resizeListener);
		final ObservableList<Node> children = stage.getScene().getRoot().getChildrenUnmodifiable();
		for (final Node child : children) {
			ResizeAndMoveHelper.addListenerDeeply(child, resizeListener);
		}
	}

	public static void addListenerDeeply(final Node node, final EventHandler<MouseEvent> listener) {
		node.addEventHandler(MouseEvent.MOUSE_MOVED, listener);
		node.addEventHandler(MouseEvent.MOUSE_PRESSED, listener);
		node.addEventHandler(MouseEvent.MOUSE_DRAGGED, listener);
		if (node instanceof Parent) {
			final Parent parent = (Parent) node;
			final ObservableList<Node> children = parent.getChildrenUnmodifiable();
			for (final Node child : children) {
				ResizeAndMoveHelper.addListenerDeeply(child, listener);
			}
		}
	}

	static class ResizeListener implements EventHandler<MouseEvent> {
		private Stage		stage;
		private Cursor		cursorEvent	= Cursor.DEFAULT;
		private int			border		= 4;
		private double		startX		= 0;
		private double		startY		= 0;

		protected double	dragDeltax;
		protected double	dragDeltay;

		private BorderPane	menu;
		protected boolean	resizing;

		public ResizeListener(final Stage stage, final BorderPane menu) {
			this.stage = stage;
			this.menu = menu;

			menu.setOnMouseDragged(new EventHandler<MouseEvent>() {
				@Override
				public void handle(final MouseEvent mouseEvent) {
					if (!ResizeListener.this.resizing) {
						stage.setX(mouseEvent.getScreenX() + ResizeListener.this.dragDeltax);
						stage.setY(mouseEvent.getScreenY() + ResizeListener.this.dragDeltay);
					}
				}
			});

			menu.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(final MouseEvent mouseEvent) {
					// record a delta distance for the drag and drop operation.
					ResizeListener.this.resizing = false;
					ResizeListener.this.dragDeltax = stage.getX() - mouseEvent.getScreenX();
					ResizeListener.this.dragDeltay = stage.getY() - mouseEvent.getScreenY();
				}
			});
		}

		@Override
		public void handle(final MouseEvent mouseEvent) {
			final EventType<? extends MouseEvent> mouseEventType = mouseEvent.getEventType();
			final Scene scene = this.stage.getScene();

			final double mouseEventX = mouseEvent.getSceneX(), mouseEventY = mouseEvent.getSceneY(), sceneWidth = scene.getWidth(), sceneHeight = scene.getHeight();

			if (MouseEvent.MOUSE_MOVED.equals(mouseEventType) == true) {
				if (mouseEventX < this.border && mouseEventY < this.border) {
					this.cursorEvent = Cursor.NW_RESIZE;
				} else if (mouseEventX < this.border && mouseEventY > sceneHeight - this.border) {
					this.cursorEvent = Cursor.SW_RESIZE;
				} else if (mouseEventX > sceneWidth - this.border && mouseEventY < this.border) {
					this.cursorEvent = Cursor.NE_RESIZE;
				} else if (mouseEventX > sceneWidth - this.border && mouseEventY > sceneHeight - this.border) {
					this.cursorEvent = Cursor.SE_RESIZE;
				} else if (mouseEventX < this.border) {
					this.cursorEvent = Cursor.W_RESIZE;
				} else if (mouseEventX > sceneWidth - this.border) {
					this.cursorEvent = Cursor.E_RESIZE;
				} else if (mouseEventY < this.border) {
					this.cursorEvent = Cursor.N_RESIZE;
				} else if (mouseEventY > sceneHeight - this.border) {
					this.cursorEvent = Cursor.S_RESIZE;
				} else {
					this.cursorEvent = Cursor.DEFAULT;

				}
				scene.setCursor(this.cursorEvent);
			} else if (MouseEvent.MOUSE_PRESSED.equals(mouseEventType) == true) {
				this.startX = this.stage.getWidth() - mouseEventX;
				this.startY = this.stage.getHeight() - mouseEventY;
			} else if (MouseEvent.MOUSE_DRAGGED.equals(mouseEventType) == true) {
				this.resizing = false;
				if (Cursor.DEFAULT.equals(this.cursorEvent) == false) {
					if (Cursor.W_RESIZE.equals(this.cursorEvent) == false && Cursor.E_RESIZE.equals(this.cursorEvent) == false) {
						final double minHeight = this.stage.getMinHeight() > (this.border * 2) ? this.stage.getMinHeight() : (this.border * 2);
						if (Cursor.NW_RESIZE.equals(this.cursorEvent) == true || Cursor.N_RESIZE.equals(this.cursorEvent) == true || Cursor.NE_RESIZE.equals(this.cursorEvent) == true) {
							if (this.stage.getHeight() > minHeight || mouseEventY < 0) {
								this.resizing = true;
								this.stage.setHeight(this.stage.getY() - mouseEvent.getScreenY() + this.stage.getHeight());
								this.stage.setY(mouseEvent.getScreenY());
							}
						} else {
							if (this.stage.getHeight() > minHeight || mouseEventY + this.startY - this.stage.getHeight() > 0) {
								this.resizing = true;
								this.stage.setHeight(mouseEventY + this.startY);
							}
						}
					}

					if (Cursor.N_RESIZE.equals(this.cursorEvent) == false && Cursor.S_RESIZE.equals(this.cursorEvent) == false) {
						final double minWidth = this.stage.getMinWidth() > (this.border * 2) ? this.stage.getMinWidth() : (this.border * 2);
						if (Cursor.NW_RESIZE.equals(this.cursorEvent) == true || Cursor.W_RESIZE.equals(this.cursorEvent) == true || Cursor.SW_RESIZE.equals(this.cursorEvent) == true) {
							if (this.stage.getWidth() > minWidth || mouseEventX < 0) {
								this.resizing = true;
								this.stage.setWidth(this.stage.getX() - mouseEvent.getScreenX() + this.stage.getWidth());
								this.stage.setX(mouseEvent.getScreenX());
							}
						} else {
							if (this.stage.getWidth() > minWidth || mouseEventX + this.startX - this.stage.getWidth() > 0) {
								this.resizing = true;
								this.stage.setWidth(mouseEventX + this.startX);
							}
						}
					}
				}

			}
		}
	}
}