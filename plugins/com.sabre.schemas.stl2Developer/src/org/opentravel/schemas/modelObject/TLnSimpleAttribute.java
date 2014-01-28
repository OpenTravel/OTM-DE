/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.modelObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.LibraryElement;
import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemacompiler.model.TLAttributeType;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLDocumentationOwner;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.model.TLEquivalentOwner;
import com.sabre.schemacompiler.model.TLExample;
import com.sabre.schemacompiler.model.TLExampleOwner;
import com.sabre.schemacompiler.model.TLModel;
import com.sabre.schemacompiler.model.TLModelElement;
import com.sabre.schemacompiler.model.TLSimpleFacet;
import com.sabre.schemacompiler.model.TLValueWithAttributes;

/**
 * TL Model Attribute for use with Simple Facets.
 * 
 * The TL Model does not have attributes on simple facets or value with attributes while the GUI
 * model does. This class simulates a TL Model element for use with simple facets.
 */
public class TLnSimpleAttribute extends TLModelElement implements TLEquivalentOwner,
        TLDocumentationOwner, TLExampleOwner {
    private static final Logger LOGGER = LoggerFactory.getLogger(TLnSimpleAttribute.class);

    // the VWA or CoreObject that owns this simple facet.
    private TLModelElement parentObject;

    public TLnSimpleAttribute() {
        parentObject = null;
        throw new IllegalArgumentException("Invalid constructor for TLnSimpleAttribute.");
    }

    /**
     * NOTE - caller MUST set parent entity since it is not known to the TLSimpleFacet.
     * 
     * @param parentEntity
     */
    public TLnSimpleAttribute(TLModelElement parentEntity) {
        parentObject = parentEntity;
    }

    /**
     * @param parentObject
     *            the parentObject to set
     */
    public void setParentObject(TLModelElement parentObject) {
        this.parentObject = parentObject;
    }

    /**
     * @return the parentObject
     */
    public TLModelElement getParentObject() {
        return parentObject;
    }

    public NamedEntity getType() {
        if (parentObject == null)
            throw new IllegalStateException("TLnSimpleAttribute not initialized properly.");

        NamedEntity type = null;
        if (parentObject instanceof TLValueWithAttributes) {
            type = ((TLValueWithAttributes) parentObject).getParentType();
        } else if (parentObject instanceof TLCoreObject) {
            type = ((TLCoreObject) parentObject).getSimpleFacet().getSimpleType();
        }
        return type;
    }

    public void setType(final NamedEntity srcType) {
        if (srcType == null)
            return;

        if (!(srcType instanceof TLAttributeType)) {
            LOGGER.error("Invalid argument: " + srcType.getValidationIdentity());
            // return;
            throw new IllegalArgumentException("Can not set simple attribute type to argument: "
                    + srcType);
        }
        if (parentObject instanceof TLValueWithAttributes) {
            ((TLValueWithAttributes) parentObject).setParentType((TLAttributeType) srcType);
        } else if (parentObject instanceof TLCoreObject) {
            ((TLCoreObject) parentObject).getSimpleFacet().setSimpleType(srcType);
        }
    }

    @Override
    public String getValidationIdentity() {
        return parentObject != null ? parentObject.getValidationIdentity() : "";
    }

    @Override
    public AbstractLibrary getOwningLibrary() {
        if (parentObject instanceof TLValueWithAttributes) {
            return ((TLValueWithAttributes) parentObject).getOwningLibrary();
        } else if (parentObject instanceof TLCoreObject) {
            return ((TLCoreObject) parentObject).getOwningLibrary();
        }
        return null;
    }

    @Override
    public LibraryElement cloneElement(AbstractLibrary tlLib) {
        TLnSimpleAttribute tlSa = new TLnSimpleAttribute(parentObject);
        return tlSa;
    }

    public String getName() {
        if (parentObject instanceof TLValueWithAttributes) {
            return ((TLValueWithAttributes) parentObject).getLocalName() + "_Value";
        } else if (parentObject instanceof TLCoreObject) {
            return ((TLCoreObject) parentObject).getLocalName() + "_Simple";
        }
        return "Undefined";
    }

    public String getExample() {
        final TLExampleOwner exampleOwner = getExampleOwner();

        if (exampleOwner != null) {
            final List<TLExample> exampleList = exampleOwner.getExamples();
            String example = null;

            if (exampleList.size() > 0) {
                example = exampleList.get(0).getValue();
            }
            return example;
        }

        return "";
    }

    private TLExampleOwner getExampleOwner() {
        TLExampleOwner exampleOwner = null;

        if (parentObject instanceof TLValueWithAttributes) {
            exampleOwner = (TLExampleOwner) parentObject;
        }
        if (parentObject instanceof TLCoreObject) {
            exampleOwner = ((TLCoreObject) parentObject).getSimpleFacet();
        }
        return exampleOwner;
    }

    private TLEquivalentOwner getEquivalentOwner() {
        TLEquivalentOwner equivalentOwner = null;

        if (parentObject instanceof TLValueWithAttributes) {
            equivalentOwner = (TLEquivalentOwner) parentObject;
        }
        if (parentObject instanceof TLCoreObject) {
            equivalentOwner = ((TLCoreObject) parentObject).getSimpleFacet();
        }
        return equivalentOwner;
    }

    public TLEquivalent getTLEquivalent(final int index) {
        if (parentObject instanceof TLValueWithAttributes) {
            return ((TLValueWithAttributes) parentObject).getEquivalents().get(index);
        }
        if (parentObject instanceof TLCoreObject) {
            return ((TLCoreObject) parentObject).getEquivalents().get(index);
        }
        return null;
    }

    @Override
    public List<TLEquivalent> getEquivalents() {
        final TLEquivalentOwner equivalentOwner = getEquivalentOwner();
        if (equivalentOwner != null) {
            return equivalentOwner.getEquivalents();
        }
        return new ArrayList<TLEquivalent>();
    }

    @Override
    public void addEquivalent(final TLEquivalent tle) {
        final TLEquivalentOwner equivalentOwner = getEquivalentOwner();
        if (equivalentOwner != null) {
            equivalentOwner.addEquivalent(tle);
        }
    }

    @Override
    public TLDocumentation getDocumentation() {
        if (parentObject instanceof TLValueWithAttributes) {
            return ((TLValueWithAttributes) parentObject).getDocumentation();
        }
        if (parentObject instanceof TLCoreObject) {
            TLSimpleFacet simpleFacet = ((TLCoreObject) parentObject).getSimpleFacet();
            if (simpleFacet != null)
                return simpleFacet.getDocumentation();
        }
        return null;
    }

    @Override
    public void setDocumentation(final TLDocumentation doc) {
        if (parentObject instanceof TLValueWithAttributes) {
            ((TLValueWithAttributes) parentObject).setDocumentation(doc);
        }
        if (parentObject instanceof TLCoreObject) {
            TLSimpleFacet simpleFacet = ((TLCoreObject) parentObject).getSimpleFacet();
            if (simpleFacet != null)
                simpleFacet.setDocumentation(doc);
        }
    }

    public boolean isMandatory() {
        return true;
    }

    @Override
    public TLModel getOwningModel() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemacompiler.model.TLEquivalentOwner#getEquivalent(java.lang.String)
     */
    @Override
    public TLEquivalent getEquivalent(final String context) {
        final TLEquivalentOwner equivalentOwner = getEquivalentOwner();
        if (equivalentOwner != null) {
            return equivalentOwner.getEquivalent(context);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemacompiler.model.TLEquivalentOwner#addEquivalent(int,
     * com.sabre.schemacompiler.model.TLEquivalent)
     */
    @Override
    public void addEquivalent(final int index, final TLEquivalent equivalent) {
        final TLEquivalentOwner equivalentOwner = getEquivalentOwner();
        if (equivalentOwner != null) {
            equivalentOwner.addEquivalent(index, equivalent);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sabre.schemacompiler.model.TLEquivalentOwner#removeEquivalent(com.sabre.schemacompiler
     * .model.TLEquivalent)
     */
    @Override
    public void removeEquivalent(final TLEquivalent equivalent) {
        final TLEquivalentOwner equivalentOwner = getEquivalentOwner();
        if (equivalentOwner != null) {
            equivalentOwner.removeEquivalent(equivalent);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemacompiler.model.TLEquivalentOwner#moveUp(com.sabre.schemacompiler.model.
     * TLEquivalent)
     */
    @Override
    public void moveUp(final TLEquivalent equivalent) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sabre.schemacompiler.model.TLEquivalentOwner#moveDown(com.sabre.schemacompiler.model.
     * TLEquivalent)
     */
    @Override
    public void moveDown(final TLEquivalent equivalent) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemacompiler.model.TLEquivalentOwner#sortEquivalents(java.util.Comparator)
     */
    @Override
    public void sortEquivalents(final Comparator<TLEquivalent> comparator) {
        final TLEquivalentOwner equivalentOwner = getEquivalentOwner();
        if (equivalentOwner != null) {
            equivalentOwner.sortEquivalents(comparator);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemacompiler.model.TLExampleOwner#getExamples()
     */
    @Override
    public List<TLExample> getExamples() {
        final TLExampleOwner exampleOwner = getExampleOwner();
        if (exampleOwner != null) {
            return exampleOwner.getExamples();
        }
        return new ArrayList<TLExample>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemacompiler.model.TLExampleOwner#getExample(java.lang.String)
     */
    @Override
    public TLExample getExample(final String contextId) {
        final TLExampleOwner exampleOwner = getExampleOwner();
        if (exampleOwner != null) {
            return exampleOwner.getExample(contextId);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sabre.schemacompiler.model.TLExampleOwner#addExample(com.sabre.schemacompiler.model.TLExample
     * )
     */
    @Override
    public void addExample(final TLExample example) {
        final TLExampleOwner exampleOwner = getExampleOwner();
        if (exampleOwner != null) {
            exampleOwner.addExample(example);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemacompiler.model.TLExampleOwner#addExample(int,
     * com.sabre.schemacompiler.model.TLExample)
     */
    @Override
    public void addExample(final int index, final TLExample example) {
        final TLExampleOwner exampleOwner = getExampleOwner();
        if (exampleOwner != null) {
            exampleOwner.addExample(index, example);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sabre.schemacompiler.model.TLExampleOwner#removeExample(com.sabre.schemacompiler.model
     * .TLExample)
     */
    @Override
    public void removeExample(final TLExample example) {
        final TLExampleOwner exampleOwner = getExampleOwner();
        if (exampleOwner != null) {
            exampleOwner.removeExample(example);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sabre.schemacompiler.model.TLExampleOwner#moveUp(com.sabre.schemacompiler.model.TLExample
     * )
     */
    @Override
    public void moveUp(final TLExample example) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sabre.schemacompiler.model.TLExampleOwner#moveDown(com.sabre.schemacompiler.model.TLExample
     * )
     */
    @Override
    public void moveDown(final TLExample example) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemacompiler.model.TLExampleOwner#sortExamples(java.util.Comparator)
     */
    @Override
    public void sortExamples(final Comparator<TLExample> comparator) {
        final TLExampleOwner exampleOwner = getExampleOwner();
        if (exampleOwner != null) {
            exampleOwner.sortExamples(comparator);
        }
    }

}
