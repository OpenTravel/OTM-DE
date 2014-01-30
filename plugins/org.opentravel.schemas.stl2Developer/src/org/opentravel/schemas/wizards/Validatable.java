
package org.opentravel.schemas.wizards;

/**
 * @author Agnieszka Janowska
 * 
 */
public interface Validatable {

    void setValidator(FormValidator validator);

    FormValidator getValidator();

}
