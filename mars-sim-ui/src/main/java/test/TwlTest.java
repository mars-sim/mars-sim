package test;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.ResizableFrame;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.theme.ThemeManager;

/**
 * 
 * @author NateS
 */
public class TwlTest {

    private LWJGLRenderer renderer;
    private ThemeManager theme;
    private GUI gui;
    private Widget root;

    public TwlTest() throws Exception {
        renderer = new LWJGLRenderer();

        theme = ThemeManager.createThemeManager(SimpleTest.class.getResource("simple_demo.xml"), renderer);

        root = new Widget();
        root.setTheme("");

        gui = new GUI(root, renderer);
        gui.setSize();
        gui.applyTheme(theme);

        addTestAlert(10, 10, "&lt;minwidth");

        addTestAlert(10, 100, "Between min and max width");

        addTestAlert(10, 180, "Past max width but less than max height. "
                + "This is a lot of text. This is a lot of text. This is a lot of text. This is a lot of text. "
                + "This is a lot of text. This is a lot of text. This is a lot of text. This is a lot of text. "
                + "This is a lot of text. This is a lot of text. This is a lot of text. This is a lot of text. "
                + "This is a lot of text. This is a lot of text. This is a lot of text. This is a lot of text. "
                + "This is a lot of text. This is a lot of text. This is a lot of text. This is a lot of text. ");

        addTestAlert(10, 350, "Past max width and past max height. "
                + "This is a lot of text. This is a lot of text. This is a lot of text. This is a lot of text. "
                + "This is a lot of text. This is a lot of text. This is a lot of text. This is a lot of text. "
                + "This is a lot of text. This is a lot of text. This is a lot of text. This is a lot of text. "
                + "This is a lot of text. This is a lot of text. This is a lot of text. This is a lot of text. "
                + "This is a lot of text. This is a lot of text. This is a lot of text. This is a lot of text. "
                + "This is a lot of text. This is a lot of text. This is a lot of text. This is a lot of text. "
                + "This is a lot of text. This is a lot of text. This is a lot of text. This is a lot of text. "
                + "This is a lot of text. This is a lot of text. This is a lot of text. This is a lot of text. "
                + "This is a lot of text. This is a lot of text. This is a lot of text. This is a lot of text. "
                + "This is a lot of text. This is a lot of text. This is a lot of text. This is a lot of text. ");
    }
    
    public void run() {
        while(!Display.isCloseRequested()) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
            gui.update();
            Display.update();
            TestUtils.reduceInputLag();
        }
    }

    private void addTestAlert(int x, int y, String text) {
        Alert alert = new Alert(text);
        alert.addButton("OK");
        alert.addButton("Cancel");
        alert.setPosition(x, y);
        root.add(alert);
	alert.adjustSize();
    }

    public class Alert extends ResizableFrame {

        private Group buttonGroupH, buttonGroupV;
        private TextArea textArea;
        private ScrollPane scrollPane;

        public Alert(String text) {
            setTheme("/resizableframe");

            final HTMLTextAreaModel textAreaModel = new HTMLTextAreaModel(text);
            textArea = new TextArea(textAreaModel);

            scrollPane = new ScrollPane(textArea);
            scrollPane.setFixed(ScrollPane.Fixed.HORIZONTAL);

            DialogLayout layout = new DialogLayout();

            buttonGroupH = layout.createSequentialGroup();
            buttonGroupH.addGap();
            buttonGroupV = layout.createParallelGroup();

            layout.setTheme("/alertbox");
            layout.setHorizontalGroup(layout.createParallelGroup()
                    .addWidget(scrollPane)
                    .addGroup(buttonGroupH));
            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addWidget(scrollPane)
                    .addGroup(buttonGroupV));
            add(layout);
        }

        public void addButton(String text) {
            Button button = new Button(text);
            buttonGroupH.addWidget(button);
            buttonGroupV.addWidget(button);
        }
    }

    public static void main(String[] args) throws Exception {
        Display.setTitle("TWL Examples");
        Display.setDisplayMode(new DisplayMode(800, 600));
        Display.setVSyncEnabled(true);
        Display.create();
        TwlTest twlTest = new TwlTest();
        twlTest.run();
    }
}
