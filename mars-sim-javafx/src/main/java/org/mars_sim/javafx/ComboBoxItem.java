package org.mars_sim.javafx;

/**
 * A wrapping class for a String that also has a boolean that indicates
 * whether this item should be disabled or not.
 * 
 * @author Jonatan Stenbacka
 */
public class ComboBoxItem implements Comparable<ComboBoxItem> {

    private String text;
    private boolean isHeader = false;

    public ComboBoxItem(String text, boolean isHeader) {

        this.text = text;
        this.isHeader = isHeader;
    }

    public ComboBoxItem(String text) {
        this.text = text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public boolean isHeader() {
        return isHeader;
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof ComboBoxItem) {
            ComboBoxItem item = (ComboBoxItem) obj;
            if (text.equals(item.getText())) {
                return true;
            }
        } else if (obj instanceof String) {
            String item = (String) obj;
            if (text.equals(item)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int compareTo(ComboBoxItem o) {
        return getText().compareTo(o.getText());
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }
}
