package org.mars_sim.javafx.tools;

//from https://gist.github.com/jewelsea/1422815/e1ec3117c48d73f8154229bcbeb5675fe7e56120


import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Wraps a jQueryUI datepicker in a JavaFX Node.
 *
 * See http://jqueryui.com/demos/datepicker/ for more information on the wrapped jQueryUI component.
 */
public class DatePicker extends StackPane {
  /** default format for a date returned by the jquery date picker (this never needs to change). */
  private static final SimpleDateFormat jQueryUiDateFormat = new SimpleDateFormat("MM/dd/yy");

  // values representing the current date and format.
  private final ReadOnlyObjectWrapper<Date> date = new ReadOnlyObjectWrapper<Date>(new Date());
  private       SimpleDateFormat      dateFormat = new SimpleDateFormat("MM/dd/yy");
  private final ReadOnlyStringWrapper dateString = new ReadOnlyStringWrapper(dateFormat.format(date.get()));

  /** @param dateFormat a new formatter for the dateString property.  */
  public void setDateFormat(SimpleDateFormat dateFormat) {
    this.dateFormat = dateFormat;
    dateString.set(dateFormat.format(date.get()));
  }

  /** @return date object property of the selected date (initializes to today). */
  public ReadOnlyObjectProperty<Date> dateProperty() {
    return date.getReadOnlyProperty();
  }
  /** @return the current value of the selected date object. */
  public Date getDate() {
    return date.get();
  }

  /** @return a string based date property, formatted by a settable formatter (default format will be an the default platform locale short date (e.g. MM/dd/yy). */
  public ReadOnlyStringProperty dateStringProperty() {
    return dateString.getReadOnlyProperty();
  }
  /** @return the current value of a string based date property. */
  public String getDateString() {
    return dateString.get();
  }

  /** helper enum for managing themes. */
  enum Theme {
    base("base"), blacktie("black-tie"), blitzer("blitzer"), cupertino("cupertino"), dotluv("dot-luv"),
    excitebike("excite-bike"), hotsneaks("hot-sneaks"), humanity("humanity"), mintchoc("mint-choc"),
    redmond("redmond"), smoothness("smoothness"), southstreet("south-street"), start("start"), swankypurse("swanky-purse"),
    trontastic("trontastic"), uidarkness("ui-darkness"), uilightness("ui-lightness"), vader("vader");

    final private String themeName;
    Theme(String themeName) { this.themeName = themeName; }
    @Override public String toString() { return themeName; }
  }

  /** @param htmlTemplateUrl refers to html hosted at the specified url used to construct a datepicker. */
  DatePicker(String htmlTemplateUrl) {
    super();
    final WebView webView = new WebView();
    webView.getEngine().load(htmlTemplateUrl);
    initPicker(webView);
  }

  public DatePicker()                                                     { this(Theme.redmond); }
  public DatePicker(Theme theme)                                          { this(theme, null); }
  public DatePicker(Theme theme, String initJavaScript)                   { this(theme, initJavaScript, null); }
  public DatePicker(Theme theme, String initJavaScript, String customCSS) { this(theme, initJavaScript, customCSS, null); }
  /**
   * constructs a new date picker based upon an inline html template.
   * @param theme to change the look and feel of the date picker.
   * @param initJavaScript custom JavaScript to initialize the datepicker's appearance and functionality; e.g. maxDate: '+1m +1w'
   * @param customCSS to allow you to customize the look and feel of the date picker.
   * @param googleCdnApiKey to use a google cdn key for retrieving the jquery javascript and css (see http://code.google.com/apis/libraries/devguide.html)
   */
  public DatePicker(Theme theme, String initJavaScript, String customCSS, String googleCdnApiKey) {
    super();
    final WebView webView = new WebView();
    webView.getEngine().loadContent(getInlineHtml(theme, initJavaScript, customCSS, googleCdnApiKey));
    initPicker(webView);
  }

  // initialize the date picker.
  private void initPicker(WebView webView) {
    // attach a handler for an alert function call which will set the DatePicker's date property.
    webView.getEngine().setOnAlert(new EventHandler<WebEvent<String>>() {
      @Override public void handle(WebEvent<String> event) {
        try { date.set(jQueryUiDateFormat.parse(event.getData())); } catch (ParseException e) { /* no action required */ }
      }
    });

    // place the webView holding the jQuery date picker inside this node.
    this.getChildren().add(webView);

    // monitor the date for changes and update the formatted date string to keep it in sync.
    date.addListener(new ChangeListener<Date>() {
      @Override public void changed(ObservableValue<? extends Date> observableValue, Date oldDate, Date newDate) {
        dateString.set(dateFormat.format(newDate));
      }
    });

    // workaround as I don't know how to size the stack to the size of the enclosed WebPane's html content.
    this.setMaxSize(330, 280);//307, 241);
  }

  // return an inline html template based upon the provided initialization parameters.
  private String getInlineHtml(Theme theme, String initJavaScript, String customCSS, String googleCdnApiKey) {
    return
      "<!DOCTYPE html>" +
      "<html lang=\"en\">" +
      "<head>" +
        "<meta charset=\"utf-8\">" +
          "<title>jQuery UI Datepicker - Display inline</title>" +
          (googleCdnApiKey != null ? ("<script type=\"text/javascript\" src=\"https://www.google.com/jsapi?key=" + googleCdnApiKey+ "\"></script>") : "") +
          "<link rel=\"stylesheet\" href=\"http://ajax.googleapis.com/ajax/libs/jqueryui/1.7.1/themes/" + theme + "/jquery-ui.css\">" +
          "<link rel=\"stylesheet\" type=\"text/css\" href=\"http://yui.yahooapis.com/3.4.1/build/cssreset/cssreset-min.css\">" +
          "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js\"></script>" +
          "<script src=\"http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.min.js\"></script>" +
          "<style type=\"text/css\">#ui-datepicker-div {display: none;} " + (customCSS != null ? customCSS : "") + "</style>" +
          "<script>" +
          "$(function() {" +
            "$(\"#datepicker\").datepicker({" +
              "onSelect: function(dateText, inst) { alert(dateText); }" +
              (initJavaScript != null ? ("," + initJavaScript) : "") +
            "});" +
          "});" +
          "</script>" +
      "</head>" +
      "<body><span id=\"datepicker\"></span></body>" +
      "</html>";
  }
}