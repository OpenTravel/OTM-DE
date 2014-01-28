/**
 * 
 */
package org.opentravel.schemas.node;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Save the model contents between edit sessions.
 * 
 * The actual persisted information is inworkspace\.metadata\.plugins\org.eclipse.ui.workbench in
 * the workbench.xml file.
 * 
 * @author Dave Hollander
 * 
 */
public class ModelContentsFactory implements IElementFactory {
    public static String ModelContentsFactory_ID = "org.opentravel.schemas.modelContentFactory";
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelContentsFactory.class);

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IElementFactory#createElement(org.eclipse.ui.IMemento)
     */
    @Override
    public IAdaptable createElement(IMemento memento) {
        LOGGER.debug("Created element from memento.");
        return null;
    }

}
