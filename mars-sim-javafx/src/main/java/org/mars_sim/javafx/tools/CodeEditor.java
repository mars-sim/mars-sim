package org.mars_sim.javafx.tools;

import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

/**
 * A syntax highlighting code editor for JavaFX created by wrapping a
 * CodeMirror code editor in a WebView.
 *
 * See http://codemirror.net for more information on using the codemirror editor.
 */
public class CodeEditor extends StackPane {
  /** a webview used to encapsulate the CodeMirror JavaScript. */
  final WebView webview = new WebView();

  /** a snapshot of the code to be edited kept for easy initilization and reversion of editable code. */
  private String editingCode;

  /**
   * a template for editing code - this can be changed to any template derived from the
   * supported modes at http://codemirror.net to allow syntax highlighted editing of
   * a wide variety of languages.
   */
  private final String editingTemplate =
    "<!doctype html>" +
    "<html>" +
    "<head>" +
    "  <link rel=\"stylesheet\" href=\"http://codemirror.net/lib/codemirror.css\">" +
    "  <script src=\"http://codemirror.net/lib/codemirror.js\"></script>" +
    "  <script src=\"http://codemirror.net/mode/clike/clike.js\"></script>" +
    "</head>" +
    "<body>" +
    "<form><textarea id=\"code\" name=\"code\">\n" +
    "${code}" +
    "</textarea></form>" +
    "<script>" +
    "  var editor = CodeMirror.fromTextArea(document.getElementById(\"code\"), {" +
    "    lineNumbers: true," +
    "    matchBrackets: true," +
    "    mode: \"text/x-java\"" +
    "  });" +
    "</script>" +
    "</body>" +
    "</html>";

  /** applies the editing template to the editing code to create the html+javascript source for a code editor. */
  private String applyEditingTemplate() {
    return editingTemplate.replace("${code}", editingCode);
  }

  /** sets the current code in the editor and creates an editing snapshot of the code which can be reverted to. */
  public void setCode(String newCode) {
    this.editingCode = newCode;
    webview.getEngine().loadContent(applyEditingTemplate());
  }

  /** returns the current code in the editor and updates an editing snapshot of the code which can be reverted to. */
  public String getCodeAndSnapshot() {
    this.editingCode = (String ) webview.getEngine().executeScript("editor.getValue();");
    return editingCode;
  }

  /** revert edits of the code to the last edit snapshot taken. */
  public void revertEdits() {
    setCode(editingCode);
  }

  /**
   * Create a new code editor.
   * @param editingCode the initial code to be edited in the code editor.
   */
  CodeEditor(String editingCode) {
    this.editingCode = editingCode;

    webview.setPrefSize(650, 325);
    webview.setMinSize(650, 325);
    webview.getEngine().loadContent(applyEditingTemplate());

    this.getChildren().add(webview);
  }
}
