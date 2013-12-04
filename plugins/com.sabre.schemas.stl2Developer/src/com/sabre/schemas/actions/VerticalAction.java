/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import org.eclipse.jface.action.Action;

/**
 * @author Agnieszka Janowska
 * 
 */
public class VerticalAction extends Action {

    /**
	 *
	 */
    public VerticalAction() {
        super("Vertical");
        this.setEnabled(false);
        this.run();
    }

}
