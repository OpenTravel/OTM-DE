/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.wizards;

/**
 * @author Agnieszka Janowska
 * 
 */
public interface Validatable {

    void setValidator(FormValidator validator);

    FormValidator getValidator();

}
