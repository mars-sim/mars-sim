/**
 * Mars Simulation Project
 * AutoFillTextBoxFactory.java
 * @version 3.1.0 2017-10-18
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.javafx.autofill;

import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 *
 * @author Narayan G. Maharjan
 * @see <a href="http://www.blog.ngopal.com.np"> Blog </a>
 */
public interface AutoFillTextBoxFactory<T> {
    
    /**
     * Keeps the array of String which contains the 
     * words to be matched on typing.
     * @param data 
     */
    void setData(ObservableList<T> data);
    
     /**
     * Give the data containing possible fast matching words
     * @return <a href="http://download.oracle.com/javafx/2.0/api/javafx/collections/ObservableList.html"> ObservableList </a>          
     */
    ObservableList<T> getData();
    
    /**
     * the main listview of the AutoFillTextBox
     * @return <a href="http://download.oracle.com/javafx/2.0/api/javafx/scene/control/ListView.html"> ListView </a>          
     */
    ListView<T> getListview();
    
     /**
     * the textbox of the AutoFillTextBox
     * @return <a href="http://download.oracle.com/javafx/2.0/api/javafx/scene/control/ListView.html"> TextView </a>          
     */
    TextField getTextbox();
    
    /**
     * This defines how many max listcell to be visibled in listview when
     * matched words are occured on typing.
     * @param limit 
     */
    void setListLimit(int limit);
    
    /**
     * this gives the limit of listcell to be visibled in listview
     * @return int
     */
    int getListLimit();
    
    /**
     * This sets the AutoFilterMode which can show as filter type
     * rather than searched type if value is true.
     * @param filter 
     */
    void setFilterMode(boolean filter);
        
    /**
     * 
     * @return boolean value of Filtermode
     */
    boolean getFilterMode();
}
