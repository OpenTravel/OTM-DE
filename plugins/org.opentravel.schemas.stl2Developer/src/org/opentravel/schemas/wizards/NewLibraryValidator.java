
package org.opentravel.schemas.wizards;

import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.NamespaceHandler;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.preferences.GeneralPreferencePage;
import org.opentravel.schemas.properties.Messages;

/**
 * @author Agnieszka Janowska
 * 
 */
public class NewLibraryValidator implements FormValidator {

    private final LibraryNode library;
    private final String baseNS;

    public NewLibraryValidator(final LibraryNode library, final String baseNamespace) {
        this.library = library;
        baseNS = baseNamespace;
    }

    /**
     * Validate the library name, path and namespace
     */
    @Override
    public void validate() throws ValidationException {
        final String[] unacceptable = new String[] { Node.UNDEFINED_PROPERTY_TXT };

        final String namespace = library.getNamespace();
        final String prefix = library.getNamePrefix();
        final NamespaceHandler handler = library.getNsHandler();

        // Make sure the fields are filled out
        throwExceptionIfEmpty(library.getPath(), unacceptable,
                Messages.getString("library.validation.url"));

        throwExceptionIfEmpty(library.getName(), unacceptable,
                Messages.getString("library.validation.name"));

        throwExceptionIfEmpty(namespace, unacceptable,
                Messages.getString("library.validation.namespace"));

        throwExceptionIfEmpty(prefix, unacceptable, Messages.getString("library.validation.prefix"));

        if (GeneralPreferencePage.areNamespacesManaged())
            if (!namespace.startsWith(baseNS))
                throw new ValidationException("Namespace must contain project base namespace.");

        String result = handler.isValidNamespace(namespace);
        if (!result.isEmpty())
            throw new ValidationException(result);

    }

    /**
     * @param libraryUrl
     * @throws ValidationException
     */
    private void throwExceptionIfEmpty(final Object obj, final String[] unacceptableStrings,
            final String message) throws ValidationException {
        if (obj == null || obj.toString() == null || obj.toString().isEmpty()) {
            throw new ValidationException(message);
        }
        for (final String s : unacceptableStrings) {
            if (obj.toString().equals(s)) {
                throw new ValidationException(message);
            }
        }
    }
}
