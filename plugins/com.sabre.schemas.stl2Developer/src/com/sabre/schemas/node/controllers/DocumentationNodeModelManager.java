/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.node.controllers;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.TLAdditionalDocumentationItem;
import com.sabre.schemacompiler.model.TLContext;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLDocumentationItem;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemas.node.DocumentationNode;
import com.sabre.schemas.node.DocumentationNode.DocumentationNodeType;
import com.sabre.schemas.properties.Images;
import com.sabre.schemas.stl2developer.OtmRegistry;

/**
 * Knows how to manage Documentation tree (based on {@link DocumentationNode} elements) for the
 * given model to keep them synchronized
 * 
 * @author Agnieszka Janowska
 * 
 */
public class DocumentationNodeModelManager {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(DocumentationNodeModelManager.class);

    public DocumentationNode createDocumentationTreeRoot(final TLDocumentation doc) {
        if (doc == null) {
            return null;
        }
        if (doc.getOwner() == null) {
            LOGGER.warn("Doc owner is null.");
            if (doc.getOwningLibrary() == null)
                LOGGER.warn("Doc owningLibrary is null.");
            return null;
        }

        DocumentationNode root = createDummyRoot();
        List<TLContext> contexts = retrieveContexts(doc);

        DocumentationNode desc = createDocumentationTypeRoot("Description",
                new DescriptionDocItemNodeModelController(doc));
        DocumentationNode desc1 = createDescriptionNodeForModel(doc);
        desc.addChild(desc1);
        root.addChild(desc);

        Map<String, NodeModelController<TLDocumentationItem>> map = new LinkedHashMap<String, NodeModelController<TLDocumentationItem>>(
                4);
        map.put("Implementers", new ImplementerDocItemNodeModelController(doc));
        map.put("Deprecations", new DeprecatedDocItemNodeModelController(doc));
        map.put("Reference Links", new ReferenceDocItemNodeModelController(doc));
        map.put("More Info Links", new MoreInfoDocItemNodeModelController(doc));
        for (Entry<String, NodeModelController<TLDocumentationItem>> type : map.entrySet()) {
            DocumentationNode typeNode = createDocumentationTypeRoot(type.getKey(), type.getValue());
            List<TLDocumentationItem> children = type.getValue().getChildren();
            for (int i = 0; i < children.size(); i++) {
                DocumentationNode itemNode = createDocItemNodeForModel(children.get(i));
                typeNode.addChild(itemNode);
            }
            root.addChild(typeNode);
        }

        DocumentationNode other = createOtherRoot("Other Doc", new NullDocItemNodeModelController());
        for (TLContext ctx : contexts) {
            DocumentationNode ctxNode = createContextNode(ctx, new OtherDocItemNodeModelController(
                    doc, ctx));
            TLAdditionalDocumentationItem item = doc.getOtherDoc(ctx.getContextId());
            if (item != null) {
                DocumentationNode itemNode = createDocItemNodeForModel(item);
                ctxNode.addChild(itemNode);
            }
            other.addChild(ctxNode);
        }
        root.addChild(other);
        // LOGGER.debug("Created doc node for "+doc.getOwner().getValidationIdentity());
        return root;
    }

    public DocumentationNode createDocItemNodeForTypeRoot(DocumentationNode parent) {
        TLDocumentationItem item = parent.getModelController().createChild();
        if (item != null) {
            DocumentationNode node = createDocItemNodeForModel(item);
            parent.addChild(node);
            return node;
        }
        return null;
    }

    public void removeDocItemNodeFromParent(DocumentationNode node) {
        DocumentationNode parent = node.getParent();
        parent.getModelController().removeChild(node.getDocItem());
        if (parent != null) {
            parent.removeChild(node);
        }
    }

    public void moveUpDocItemNode(DocumentationNode node) {
        DocumentationNode parent = node.getParent();
        parent.getModelController().moveChildUp(node.getDocItem());
        int index = parent.getChildren().indexOf(node);
        if (index > 0) {
            parent.removeChild(node);
            parent.addChild(index - 1, node);
        }
    }

    public void moveDownDocItemNode(DocumentationNode node) {
        DocumentationNode parent = node.getParent();
        parent.getModelController().moveChildDown(node.getDocItem());
        int index = parent.getChildren().indexOf(node);
        if (index < parent.getChildren().size() - 1) {
            parent.removeChild(node);
            parent.addChild(index + 1, node);
        }
    }

    public DocumentationNode moveToTypeRoot(DocumentationNode item, DocumentationNode newRoot) {
        if (newRoot == null || !newRoot.isTypeRoot()) {
            return null;
        }
        if (item == null || !item.isDocItem()) {
            return null;
        }
        DocumentationNode newNode = createDocItemNodeForTypeRoot(newRoot);
        newNode.setValue(item.getValue());
        removeDocItemNodeFromParent(item);
        return newNode;
    }

    private List<TLContext> retrieveContexts(final TLDocumentation doc) {
        List<TLContext> contexts = Collections.emptyList();
        if (doc.getOwner() == null) {
            LOGGER.warn("Doc owner is null.");
            if (doc.getOwningLibrary() == null)
                LOGGER.warn("Doc owningLibrary is null.");
            return contexts;
        }
        AbstractLibrary owningLibrary = doc.getOwner().getOwningLibrary();
        if (owningLibrary != null && owningLibrary instanceof TLLibrary) {
            TLLibrary lib = (TLLibrary) owningLibrary;
            contexts = lib.getContexts();
        }
        return contexts;
    }

    private DocumentationNode createDummyRoot() {
        DocumentationNode root = new DocumentationNode();
        root.setType(DocumentationNodeType.OTHER_ITEM);
        return root;
    }

    private DocumentationNode createContextNode(TLContext ctx,
            NodeModelController<TLDocumentationItem> modelController) {
        DocumentationNode ctxNode = new DocumentationNode();
        ctxNode.setLabelProvider(simpleLabelProvider(ctx.getContextId()));
        ctxNode.setImageProvider(simpleImageProvider("context"));
        ctxNode.setModelController(modelController);
        ctxNode.setType(DocumentationNodeType.DOCUMENTATION_TYPE_ROOT);
        return ctxNode;
    }

    private DocumentationNode createDescriptionNodeForModel(final TLDocumentation doc) {
        DocumentationNode desc1 = new DocumentationNode();
        desc1.setLabelProvider(descriptionLabelProvider(doc));
        desc1.setValueController(descriptionValueController(doc));
        desc1.setImageProvider(simpleImageProvider("docItem"));
        desc1.setType(DocumentationNodeType.DOCUMENTATION_ITEM);
        return desc1;
    }

    private DocumentationNode createDocItemNodeForModel(final TLDocumentationItem doc) {
        DocumentationNode dev1 = new DocumentationNode(doc);
        dev1.setLabelProvider(docItemLabelProvider(doc));
        dev1.setValueController(docItemValueController(doc));
        dev1.setImageProvider(simpleImageProvider("docItem"));
        dev1.setType(DocumentationNodeType.DOCUMENTATION_ITEM);
        return dev1;
    }

    private DocumentationNode createDocumentationTypeRoot(String type,
            NodeModelController<TLDocumentationItem> modelController) {
        DocumentationNode desc = new DocumentationNode();
        desc.setLabelProvider(simpleLabelProvider(type));
        desc.setModelController(modelController);
        desc.setImageProvider(simpleImageProvider("docRoot"));
        desc.setType(DocumentationNodeType.DOCUMENTATION_TYPE_ROOT);
        return desc;
    }

    private DocumentationNode createOtherRoot(String type,
            NodeModelController<TLDocumentationItem> modelController) {
        DocumentationNode desc = new DocumentationNode();
        desc.setLabelProvider(simpleLabelProvider(type));
        desc.setModelController(modelController);
        desc.setImageProvider(simpleImageProvider("docRoot"));
        desc.setType(DocumentationNodeType.OTHER_ITEM);
        return desc;
    }

    private NodeLabelProvider descriptionLabelProvider(final TLDocumentation doc) {
        return new NodeLabelProvider() {

            @Override
            public String getLabel() {
                String value = doc.getDescription();
                return trimToLabel(value);
            }

        };
    }

    private NodeLabelProvider docItemLabelProvider(final TLDocumentationItem doc) {
        return new NodeLabelProvider() {

            @Override
            public String getLabel() {
                String value = doc.getText();
                return trimToLabel(value);
            }

        };
    }

    private NodeValueController descriptionValueController(final TLDocumentation doc) {
        return new NodeValueController() {

            @Override
            public String getValue() {
                return escapeNull(doc.getDescription());
            }

            @Override
            public void setValue(String value) {
                doc.setDescription(value);
                if (OtmRegistry.getPropertiesView() != null)
                    OtmRegistry.getPropertiesView().refresh();
            }

        };
    }

    private NodeValueController docItemValueController(final TLDocumentationItem doc) {
        return new NodeValueController() {

            @Override
            public String getValue() {
                return escapeNull(doc.getText());
            }

            @Override
            public void setValue(String value) {
                doc.setText(value);
            }

        };
    }

    private String escapeNull(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }

    private String trimToLabel(String value) {
        if (value != null) {
            int length = value.length();// > 30 ? 30 : value.length();
            return value.substring(0, length).replaceAll("\\s", " ");
        }
        return "";
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

}
