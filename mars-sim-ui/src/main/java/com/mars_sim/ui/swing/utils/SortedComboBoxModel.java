/*
 * Mars Simulation Project
 * SortedComboBoxModel.java
 * @date 2024-06-29
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 * This is a ComboBox model that automatically sorted elements according to a comparator
 * Element can be added but not as a specific index hence it doesnot implement the 
 * MutableComboBoxModel interface.
 */
@SuppressWarnings("serial")
public class SortedComboBoxModel<E> extends AbstractListModel<E> 
            implements ComboBoxModel<E> {

    private List<E> choices = new ArrayList<>();
    private Comparator<E> comparator;
    private Object selectedObject;

    public SortedComboBoxModel(Collection<E> initialList, Comparator<E> comparator) {
        this.comparator = comparator;
        if (!initialList.isEmpty()) {
            this.choices.addAll(initialList);

            Collections.sort(choices, comparator);
            selectedObject = choices.get(0);
        }
    }

    public void addElement(E newItem) {
        choices.add(newItem);
        Collections.sort(choices, comparator);

        int index = choices.indexOf(newItem);
        fireIntervalAdded(this, index, index);
    }

    @Override
    public int getSize() {
        return choices.size();
    }

    @Override
    public E getElementAt(int index) {
        return choices.get(index);
    }

    @Override
    public void setSelectedItem(Object anObject) {
        if ((selectedObject != null && !selectedObject.equals( anObject )) ||
            selectedObject == null && anObject != null) {
            selectedObject = anObject;
            fireContentsChanged(this, -1, -1);
        }
    }

    @Override
    public Object getSelectedItem() {
        return selectedObject;
    }
}
