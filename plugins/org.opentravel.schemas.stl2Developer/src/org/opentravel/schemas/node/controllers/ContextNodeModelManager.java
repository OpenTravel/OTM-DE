
package org.opentravel.schemas.node.controllers;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.controllers.ContextModelManager;
import org.opentravel.schemas.node.ContextNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.ContextNode.ContextNodeType;
import org.opentravel.schemas.properties.Images;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLLibrary;

/**
 * Creates and maintains a model complete with contextNodes for the context's in the model. Performs
 * all interactions with the TL model.
 * 
 * @author Agnieszka Janowska
 * 
 */
// TODO - this is way to complicated for how simple its function is. It will likely break
// when managed in public domain. SIMPLIFY.
//
public class ContextNodeModelManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContextNodeModelManager.class);
    ContextModelManager manager = null;

    public ContextNodeModelManager(ContextModelManager cmm) {
        manager = cmm;
    }

    /**
     * Starting with the model node, find all the user libraries and create context nodes for them.
     * 
     * @param modelNode
     * @return
     */
    public ContextNode createContextTreeRoot() {
        ContextNode root = new ContextNode();
        root.setType(ContextNodeType.OTHER_ITEM);
        return root;
    }

    public ContextNode createContextNodeForLibraryRoot(ContextNode parent) {
        if (parent == null)
            return null;

        TLContext item = parent.getModelController().createChild();
        if (item != null) {
            ContextNode node = createItemContextNode(item);
            parent.addChild(node);
            node.setLibraryNode(parent.getLibraryNode());
            return node;
        }
        throw new IllegalStateException("Could  not create library context node.");
        // return null;
    }

    public void addNodeToParent(ContextNode cn, ContextNode parent) {
        parent.addChild(cn);
        cn.setParent(parent);
    }

    public void removeContextNodeFromParent(ContextNode node) {
        ContextNode parent = node.getParent();
        parent.getModelController().removeChild(node.getModelObject());
        if (parent != null) {
            parent.removeChild(node);
        }
    }

    // Create a leaf node for each context.
    public ContextNode createItemContextNode(TLContext ctx) {
        ContextNode ctxNode = new ContextNode(ctx);
        ctxNode.setLabelProvider(contextLabelProvider(ctx));
        ctxNode.setApplicationContextController(applicationContextController(ctx));
        ctxNode.setContextIdController(contextIdController(ctx));
        ctxNode.setDescriptionController(descriptionController(ctx));
        ctxNode.setImageProvider(simpleImageProvider("context"));
        ctxNode.setType(ContextNodeType.CONTEXT_ITEM);
        return ctxNode;
    }

    // Create a branch node for a library.
    public ContextNode createLibraryContextNode(final LibraryNode ln) {
        TLLibrary lib = ln.getTLLibrary();
        ContextNode node = new ContextNode();
        node.setLabelProvider(simpleLabelProvider(lib.getName()));
        if (ln.isInChain())
            node.setImageProvider(simpleImageProvider("libraryChain"));
        else
            node.setImageProvider(simpleImageProvider("library"));
        node.setModelController(contextModelController(lib));
        node.setType(ContextNodeType.LIBRARY_ROOT);
        return node;
    }

    private NodeLabelProvider contextLabelProvider(final TLContext context) {
        return new NodeLabelProvider() {

            @Override
            public String getLabel() {
                return context.getContextId() + " (" + context.getApplicationContext() + ")";
            }
        };
    }

    private NodeValueController applicationContextController(final TLContext context) {
        return new NodeValueController() {

            @Override
            public String getValue() {
                return escapeNull(context.getApplicationContext());
            }

            @Override
            public void setValue(String value) {
                if (!value.equals(context.getApplicationContext())) {
                    String newValue = manager.setApplicationContext(context, value);
                    context.setApplicationContext(newValue); // Update node
                }
            }
        };
    }

    private NodeValueController contextIdController(final TLContext context) {
        return new NodeValueController() {

            @Override
            public String getValue() {
                return escapeNull(context.getContextId());
            }

            @Override
            public void setValue(String value) {
                if (!value.equals(context.getContextId())) {
                    String newValue = manager.setContextId(context, value);
                    context.setContextId(newValue); // Update node to represent what is in
                                                    // controller.
                }
            }

        };
    }

    private NodeValueController descriptionController(final TLContext context) {
        return new NodeValueController() {

            @Override
            public String getValue() {
                TLDocumentation documentation = getDocumentation(context);
                return escapeNull(documentation.getDescription());
            }

            /**
             * @param context
             * @return
             */
            private TLDocumentation getDocumentation(final TLContext context) {
                TLDocumentation documentation = context.getDocumentation();
                if (documentation == null) {
                    documentation = new TLDocumentation();
                    context.setDocumentation(documentation);
                }
                return documentation;
            }

            @Override
            public void setValue(String value) {
                TLDocumentation documentation = getDocumentation(context);
                documentation.setDescription(value);
            }

        };
    }

    private NodeLabelProvider simpleLabelProvider(final String label) {
        return new NodeLabelProvider() {

            @Override
            public String getLabel() {
                return label;
            }
        };
    }

    private NodeImageProvider simpleImageProvider(final String imageName) {
        return new NodeImageProvider() {

            @Override
            public Image getImage() {
                final ImageRegistry imageRegistry = Images.getImageRegistry();
                return imageRegistry.get(imageName);
            }
        };
    }

    private NodeModelController<TLContext> contextModelController(final TLLibrary library) {
        return new ContextNodeModelController(library);
    }

    private String escapeNull(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }

    /**
     * Debugging Utility.
     */
    public void printContextNode(ContextNode cn) {
        LOGGER.debug(" Node: " + cn.getContextId() + "\tkids = " + cn.getChildren().size()
                + "\tApp  = " + cn.getApplicationContext() + "\tType =" + cn.getType());
        for (ContextNode kid : cn.getChildren())
            printContextNode(kid);
    }

}
