/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.controllers;

/**
 * Coordinates the actions related to documentation items between model and view. Needs to be
 * initialized with the proper view.
 * 
 * @author Agnieszka Janowska
 * 
 */
public interface DocumentationController {

    /**
     * Adds documentation item to the currently selected documentation type list
     */
    void addDocItem();

    /**
     * Removes documentation item from the currently selected documentation type list
     */
    void deleteDocItems();

    /**
     * Moves up documentation item on the currently selected documentation type list. Guards from
     * moving the documentation item out of the current type list.
     */
    void upDocItem();

    /**
     * Moves down documentation item on the currently selected documentation type list. Guards from
     * moving the documentation item out of the current type list.
     */
    void downDocItem();

    /**
     * Clears (but not removes) the value of the selected documentation item
     */
    void clearDocItem();

    /**
     * Changes the type root of selected documentation item to new one selected in the view
     */
    void changeDocItemsType();

    /**
     * Clones the selected documentation items
     */
    void cloneDocItems();
}
