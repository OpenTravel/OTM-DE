/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.wizards;

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
     * org.opentravel.schemas.wizards.Validatable#setValidator(org.opentravel.schemas.wizards.FormValidator)
     */
    @Override
    public void setValidator(final FormValidator validator) {
        this.validator = validator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.wizards.Validatable#getValidator()
     */
    @Override
    public FormValidator getValidator() {
        return validator;
    }

}
