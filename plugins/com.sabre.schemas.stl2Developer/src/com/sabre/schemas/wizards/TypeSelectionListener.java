/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.wizards;

import com.sabre.schemas.node.Node;

/**
 * Interface used by components wishing to be notified of type selections prior to the completion of
 * a type selection wizard.
 * 
 * @author S. Livezey
 */
public interface TypeSelectionListener {

    /**
     * Called when a node is selected by the user in the <code>TypeSelectionPage</code> of a wizard.
     * The return value of this listener indicates whether the type selection page should allow
     * advancement to the next wizard page.
     * 
     * @param selectedType
     *            the type node that was selected by the user
     * @return boolean
     */
    public boolean notifyTypeSelected(Node selectedExtension);

}
