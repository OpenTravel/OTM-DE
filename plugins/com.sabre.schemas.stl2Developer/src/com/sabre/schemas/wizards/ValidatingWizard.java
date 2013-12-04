/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.wizards;

import org.eclipse.jface.wizard.Wizard;

/**
 * @author Agnieszka Janowska / Dave Hollander
 * 
 */
public abstract class ValidatingWizard extends Wizard implements Validatable {

    private FormValidator validator = new NullValidator();

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sabre.schemas.wizards.Validatable#setValidator(com.sabre.schemas.wizards.FormValidator)
     */
    @Override
    public void setValidator(final FormValidator validator) {
        this.validator = validator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.wizards.Validatable#getValidator()
     */
    @Override
    public FormValidator getValidator() {
        return validator;
    }

}
