/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.controllers;

import java.util.List;

import org.opentravel.schemas.controllers.DefaultContextController.ContextViewType;
import org.opentravel.schemas.node.ContextNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.TLContext;
import com.sabre.schemacompiler.model.TLContextReferrer;
import com.sabre.schemacompiler.model.TLLibrary;

/**
 * Controller for the OTM Context identifiers. Context is defined in the model as an application
 * context (URI) and ID. IDs are for the user to select which context to apply/view.
 * 
 * Implements the Model View Controller Design Pattern. Controller handle all requests coming from
 * the view, user interface or application. The controller is responsible for accessing model.
 * 
 * Unlike some MVC implementations, this implementation is designed to have a single controller for
 * the whole application and not individual controllers for each view.
 * 
 * Controller view contexts exposed in the functions are: Default - Each library in a model may have
 * multiple contexts, one of which is designated the "default" context. Selected - the context
 * currently selected in the context view tree.
 * 
 * @author Agnieszka Janowska
 * 
 */
public interface ContextController {

    /**
     * Add contexts from the passed library.
     */
    void addContexts(LibraryNode ln);

    /**
     * Changes contexts for given entity (such as Equivalent or Example)
     * 
     * @param referrer
     */
    void changeContext(TLContextReferrer referrer);

    /**
     * Clears the contexts from the model. Clears the selected contexts. Invoke when the model is
     * created or changed
     */
    void clearContexts();

    /**
     * Clears the library's contexts from the model. Clears the selected contexts for that library.
     */
    void clearContexts(LibraryNode ln);

    /**
     * Clones the selected context and adds it to the same library
     */
    void cloneContext();

    /**
     * Clones the selected context and adds it to other selected library
     */
    void copyContext();

    /**
     * Clones the contexts used in the node and adds them to library
     */
    void copyContext(Node node, LibraryNode ln);

    /**
     * @return the application context associated with the library-contextId
     */
    String getApplicationContext(LibraryNode ln, String contextId);

    /**
     * @return all context Ids available from the current property or navigator view node.
     */
    List<String> getAvailableContextIds();

    List<String> getAvailableContextIds(AbstractLibrary tlib);

    /**
     * @return list of all contextIds available for the passed library.
     */
    List<String> getAvailableContextIds(LibraryNode ln);

    /**
     * Return the model manager. TEST USE ONLY!
     */
    ContextModelManager getContextModelManager();

    /**
     * @return the context node for the library-contextId
     */
    ContextNode getContextNode(LibraryNode ln, String id);

    TLContext getDefaultContext(TLLibrary library);

    /**
     * @return current property or navigator view library's default context id
     */
    String getDefaultContextId();

    /**
     * @return passed library's default context id
     */
    String getDefaultContextId(LibraryNode ln);

    ContextNode getRoot();

    /**
     * @return the selected context id for the specified view. If the selected node is in a
     *         different library, then the default for that library is returned and set as selected.
     */
    String getSelectedId(ContextViewType view, LibraryNode ln);

    /**
     * Merges selected context with another context
     */
    void mergeContext();

    /**
     * Create a new context to the context list of the currently selected context node. Get the
     * library from the context view or navigation selected. Use ID and application values from the
     * context view.
     */
    void newContext();

    /**
     * Create a new context with the given id and application context value.
     * 
     * @param id
     * @param value
     */
    void newContext(LibraryNode library, String id, String value);

    /**
     * Sets selected context as default for given library
     */
    void setDefaultContext();

    /**
     * Set the selected context id for the specified view.
     * 
     * @return false if the context for library-contextId was not found.
     */
    boolean setSelectedId(ContextViewType view, LibraryNode ln, String id);

    /**
     * Refresh context root.
     */
    void refreshContexts();

}
