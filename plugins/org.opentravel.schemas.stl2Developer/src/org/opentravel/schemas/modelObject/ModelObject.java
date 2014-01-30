/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.modelObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.opentravel.schemas.controllers.ContextController;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.XsdNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLEquivalentOwner;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLModelElement;

/**
 * The ModelObject abstract class provides a template for working with underlying model source
 * objects. Model Object class is use for actual components and not organizational objects.
 * 
 * Note - model objects do not know about any synthetic objects used in the GUI such as facet nodes,
 * roles, or simpleFacets. They provide access to roles, aliases etc via getters and setters on
 * their base objects (core/business).
 * 
 * Note: there are no model objects for libraries or projects.
 * 
 * TODO - reconcile how documentation is managed here with Documentation Controller. Examples and
 * Equivalents should have the same structure applied (i believe).
 * 
 * GOAL - the original goal was for this class was to isolate node classes from changes in the TL
 * model. The rate of change in that model is slowed way down. Evolution - Either all access to the
 * TLModelObject should be factored out of Node and upper classes OR this should be trimmed way down
 * to only only be kept where needed to simplify implementation for node.
 * 
 * If we make this class and its sub-classes as thin as practical: Consider adding interface. When
 * re-factored, should any behaviors move up into node?
 * 
 * 
 * @author Dave Hollander
 * 
 */
public abstract class ModelObject<TL> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ModelObject.class);

    protected TL srcObj;
    protected INode node;

    public ModelObject() {
        srcObj = null;
    }

    public ModelObject(final TL obj) {
        srcObj = obj;
    }

    /**
     * Node allows MO level assignments to be reflected back into the node model. Use for type
     * assignments. Allows business logic and use constraints to be implemented in node classes to
     * help keep this layer thin.
     * 
     * @return node associated with this model object.
     */
    public INode getNode() {
        return node;
    }

    public void setNode(final INode node) {
        this.node = node;
    }

    public AbstractLibrary getOwningLibrary() {
        return srcObj instanceof LibraryMember ? ((LibraryMember) srcObj).getOwningLibrary() : null;
    }

    // /**
    // * AssignedType = name. Note - node.getAssignedTypeName() will add prefix if in different NS
    // * from object.
    // *
    // * @return - assignedTypeName
    // */
    // public String getAssignedTypeName() {
    // return getTLType() == null ? "" : getTLType().getLocalName();
    // }

    public String getAssignedName() {
        return getTLType() == null || getTLType().getLocalName() == null ? "" : getTLType()
                .getLocalName();
    }

    public String getAssignedPrefix() {
        final NamedEntity type = getTLType();
        if (type == null)
            return "";

        if (type.getOwningLibrary() == null) {
            // LOGGER.debug("Providing assigned prefix of xsd.");
            return "xsd";
        }
        return type.getOwningLibrary().getPrefix();
    }

    /**
     * List all children in default display order. Returns empty list if no children.
     * 
     * @return
     */
    public List<?> getChildren() {
        return Collections.emptyList();
    }

    /**
     * List all inherited children in default display order. Returns empty list if no children.
     * 
     * @return
     */
    public List<?> getInheritedChildren() {
        return Collections.emptyList();
    }

    /**
     * @return - Return the type name of the component
     */
    public abstract String getComponentType();

    public TLDocumentation getDocumentation() {
        TLDocumentation tld = null;
        if (this.isDocumentationOwner()) {
            final TLDocumentationOwner docOwner = (TLDocumentationOwner) srcObj;
            tld = docOwner.getDocumentation();
            if (tld == null) {
                tld = createDocumentation();
            }
        }
        return tld;
    }

    public TLDocumentation createDocumentation() {
        final TLDocumentation tld = new TLDocumentation();
        if (this.isDocumentationOwner()) {
            final TLDocumentationOwner docOwner = (TLDocumentationOwner) srcObj;
            docOwner.setDocumentation(tld);
        }
        return tld;
    }

    public void setDocumentation(TLDocumentation documentation) {
        if (this.isDocumentationOwner()) {
            TLDocumentationOwner docOwner = (TLDocumentationOwner) srcObj;
            docOwner.setDocumentation(documentation);
        }
    }

    public String getEquivalent(final String context) {
        String equivalent = null;

        if (srcObj instanceof TLEquivalentOwner) {
            final TLEquivalent tlEquivalent = ((TLEquivalentOwner) srcObj).getEquivalent(context);
            if (tlEquivalent != null) {
                equivalent = tlEquivalent.getDescription();
            }
        }
        return equivalent;
    }

    /**
     * Change equivalent context
     * 
     * @param srcContext
     * @param targetContext
     */
    public void changeEquivalentContext(final String srcContext, final String targetContext) {
        if (!(srcObj instanceof TLEquivalentOwner))
            return;
        final TLEquivalent tle = ((TLEquivalentOwner) srcObj).getEquivalent(srcContext);
        if (tle == null)
            return;
        tle.setContext(targetContext);
    }

    /**
     * Change Example context
     * 
     * @param srcContext
     * @param targetContext
     */
    public void changeExampleContext(final String srcContext, final String targetContext) {
        if (!(srcObj instanceof TLExampleOwner))
            return;
        final TLExample tle = ((TLExampleOwner) srcObj).getExample(srcContext);
        if (tle == null)
            return;
        tle.setContext(targetContext);
    }

    public String getExample(final String context) {
        String example = null;
        if (srcObj instanceof TLExampleOwner) {
            final TLExample tlExample = ((TLExampleOwner) srcObj).getExample(context);
            if (tlExample != null) {
                example = tlExample.getValue();
            }
        }

        return example;
    }

    /**
     * Search for multiple equivalents with the same ID. If found, create a new context and assign
     * it to one of them.
     * 
     * @param obj
     * @return true if equivalents were changed/fixed.
     */
    public boolean fixEquivalents(TLEquivalentOwner obj) {
        if (obj.getEquivalents().size() <= 1)
            return false;

        boolean fixed = false;
        ContextController cc = OtmRegistry.getMainController().getContextController();
        LibraryNode ln = ((Node) this.node).getLibrary();
        List<String> availIDs = new ArrayList<String>(cc.getAvailableContextIds(ln));
        List<String> usedIDs = new ArrayList<String>();
        List<TLEquivalent> fixList = new ArrayList<TLEquivalent>();

        // find duplicates as well as available contexts.
        for (TLEquivalent equivalent : obj.getEquivalents()) {
            if (usedIDs.contains(equivalent.getContext()))
                fixList.add(equivalent);
            else {
                if (!availIDs.remove(equivalent.getContext()))
                    LOGGER.debug("TROUBLE in CONTEXT City. Context used that is not in context map.");
                usedIDs.add(equivalent.getContext());
            }
        }

        // fix any duplicates
        int i = 1;
        for (TLEquivalent toBeFixed : fixList) {
            if (availIDs.isEmpty()) {
                cc.newContext(ln, "id" + i, "http://www.examples.com/app" + i++);
            } else {
                toBeFixed.setContext(availIDs.get(0));
                availIDs.remove(0);
                fixed = true;
            }
            LOGGER.debug("Fixed context on equivalent - set to " + toBeFixed.getContext());
        }
        return fixed;
    }

    /**
     * Search for multiple examples with the same ID. If found, create a new context and assign it
     * to one of them.
     * 
     * @param obj
     * @return true if the examples were changed/fixed.
     */
    public boolean fixExamples(TLExampleOwner obj) {
        if (obj.getExamples().size() <= 1)
            return false;
        boolean fixed = false;

        ContextController cc = OtmRegistry.getMainController().getContextController();
        LibraryNode ln = ((Node) this.node).getLibrary();
        List<String> availIDs = new ArrayList<String>(cc.getAvailableContextIds(ln));
        List<String> usedIDs = new ArrayList<String>();
        List<TLExample> fixList = new ArrayList<TLExample>();

        // find duplicates as well as available contexts.
        for (TLExample example : obj.getExamples()) {
            if (usedIDs.contains(example.getContext()))
                fixList.add(example);
            else {
                // First time used -- remove from available and add to used.
                availIDs.remove(example.getContext());
                usedIDs.add(example.getContext());
            }
        }

        // fix any duplicates
        int i = 1;
        for (TLExample toBeFixed : fixList) {
            if (availIDs.isEmpty()) {
                cc.newContext(ln, "id" + i, "http://www.examples.com/app" + i++);
            } else {
                toBeFixed.setContext(availIDs.get(0));
                availIDs.remove(0);
                fixed = true;
            }
            LOGGER.debug("Fixed " + getName() + ", example context set to "
                    + toBeFixed.getContext());
        }
        return fixed;
    }

    protected abstract AbstractLibrary getLibrary(TL obj);

    public int getMaxLength() {
        return -1;
    }

    public int getMinLength() {
        return -1;
    }

    public int getFractionDigits() {
        return -1;
    }

    public int getTotalDigits() {
        return -1;
    }

    public String getMinInclusive() {
        return null;
    }

    public String getMaxInclusive() {
        return null;
    }

    public String getMinExclusive() {
        return null;
    }

    public String getMaxExclusive() {
        return null;
    }

    /**
     * Label is the node's name plus any optional additional generated text that clarifies the
     * role/purpose/type of the node to the user. Labels can not be set. Label to be used in tree
     * views. Sub types must override to provide additional text.
     * 
     * @return - name
     */
    public String getLabel() {
        return getName();
    }

    /**
     * @return - the user managed name of the object.
     */
    public abstract String getName();

    public abstract String getNamePrefix();

    public abstract String getNamespace();

    public String getPattern() {
        return "";
    }

    public int getRepeat() {
        return -1;
    }

    /**
     * Get the local name of the extension type.
     * 
     * @return
     */
    public String getExtendsType() {
        return "";
    }

    public String getExtendsTypeNS() {
        return "";
    }

    /**
     * Return true if this is extended by the passed MO
     * 
     * @return
     */
    public boolean isExtendedBy(NamedEntity extension) {
        // LOGGER.debug("model object supertype used to answer is extended.");
        return false;
    }

    public TL getTLModelObj() {
        return srcObj;
    }

    public boolean isComplexAssignable() {
        return false;
    }

    public boolean isComplexFacet() {
        return false;
    }

    public boolean isIndicatorElement() {
        return false;
    }

    public boolean isIndicatorProperty() {
        return false;
    }

    public boolean isMandatory() {
        return false;
    }

    // 6/30 - seems broken. did not find AttributeMO
    // USED ALOT
    // Assert.assertTrue(ap.getModelObject().isSimpleAssignable());
    public boolean isSimpleAssignable() {
        return false;
    }

    public boolean setEquivalent(final String equ, final String context) {
        if (srcObj instanceof TLEquivalentOwner) {
            final TLEquivalentOwner equivalentOwner = (TLEquivalentOwner) srcObj;
            TLEquivalent tlEquivalent = equivalentOwner.getEquivalent(context);

            if (tlEquivalent == null) {
                tlEquivalent = new TLEquivalent();
                tlEquivalent.setContext(context);
                equivalentOwner.addEquivalent(tlEquivalent);
            }

            tlEquivalent.setDescription(equ);
            return true;
        }
        return false;
    }

    public void removeEquivalent(final String context) {
        if (srcObj instanceof TLEquivalentOwner) {
            final TLEquivalentOwner equivalentOwner = (TLEquivalentOwner) srcObj;
            final TLEquivalent tlEquivalent = equivalentOwner.getEquivalent(context);
            ((TLEquivalentOwner) srcObj).removeEquivalent(tlEquivalent);
        }
    }

    public boolean setExample(final String example, final String context) {
        if (srcObj instanceof TLExampleOwner) {
            final TLExampleOwner exampleOwner = (TLExampleOwner) srcObj;
            TLExample tlExample = exampleOwner.getExample(context);

            if (tlExample == null) {
                tlExample = new TLExample();
                tlExample.setContext(context);
                exampleOwner.addExample(tlExample);
            }

            tlExample.setValue(example);
            return true;
        }
        return false;
    }

    public void removeExample(final String context) {
        if (srcObj instanceof TLExampleOwner) {
            final TLExampleOwner exampleOwner = (TLExampleOwner) srcObj;
            final TLExample tlExample = exampleOwner.getExample(context);
            exampleOwner.removeExample(tlExample);
        }
    }

    public boolean setMandatory(final boolean selection) {
        return false;
    }

    public boolean setMaxLength(final int length) {
        return false;
    }

    public boolean setMinLength(final int length) {
        return false;
    }

    public boolean setFractionDigits(final int digits) {
        return false;
    }

    public boolean setTotalDigits(final int digits) {
        return false;
    }

    public boolean setMinInclusive(final String value) {
        return false;
    }

    public boolean setMaxInclusive(final String value) {
        return false;
    }

    public boolean setMinExclusive(final String value) {
        return false;
    }

    public boolean setMaxExclusive(final String value) {
        return false;
    }

    public abstract boolean setName(String name);

    public boolean setPattern(final String pattern) {
        return false;
    }

    public boolean setRepeat(final int count) {
        return false;
    }

    /**
     * Set the type of underlying TL Model object. Also assure the name conforms to the rules for
     * that type of property.
     * 
     * @param mo
     */
    public void setTLType(final ModelObject<?> mo) {
    }

    /**
     * This should only be used by sub-types. The sub-types set the TL model objects.
     * 
     * @param tlObj
     */
    public void setTLType(NamedEntity attributeType) {
    }

    // public void setTLType(final TLAttributeType tlObj) {
    // // this.type = tlObj;
    // }
    //
    // public void setTLType(final TLPropertyType tlObj) {
    // // this.type = tlObj;
    // }

    public void setExtendsType(final ModelObject<?> mo) {
    }

    // public boolean isRole() {
    // return false;
    // }

    // public boolean isRoleFacet() {
    // return false;
    // }
    //
    // public boolean isRoleProperty() {
    // return false;
    // }

    /**
     * Remove the TL object from the TL model. Does <b>not</b> delete the modelObject.
     */
    public abstract void delete();

    // @Deprecated
    // public void setExtension(final boolean state) {
    // }

    // @Deprecated
    // public boolean isExtendable() {
    // return false;
    // }

    public boolean isDocumentationOwner() {
        return srcObj instanceof TLDocumentationOwner && !(srcObj instanceof TLListFacet);
    }

    public String getDescriptionDoc() {
        final TLDocumentation tld = getDocumentation();
        return (tld == null || tld.getDescription() == null) ? "" : tld.getDescription();
    }

    public List<TLDocumentationItem> getDeveloperDoc() {
        return getDocumentation() != null ? (getDocumentation().getImplementers()) : null;
    }

    public String getDeveloperDoc(final int i) {
        final TLDocumentation tld = getDocumentation();
        return (tld == null || tld.getImplementers() == null || tld.getImplementers().size() <= i) ? ""
                : tld.getImplementers().get(i).getText();
    }

    /**
     * ************************************* Documentation *************************************
     * 
     * These are commented out because the documentation view uses its own documentation management
     * utilities.
     * 
     * TODO - convert the rest of the users to use DocumentationNodeModelManaager then delete these.
     * Also see if OtmActions that use the setters are still used.
     * 
     */

    // public String getDeprecatedDoc(final int i) {
    // final TLDocumentation tld = getDocumentation();
    // return (tld == null || tld.getDeprecations() == null || tld.getDeprecations().size() <= i) ?
    // "" : tld
    // .getDeprecations().get(i).getText();
    // }
    // public String getReferenceDoc(final int i) {
    // final TLDocumentation tld = getDocumentation();
    // return (tld == null || tld.getReferences() == null || tld.getReferences().size() <= i) ? "" :
    // tld
    // .getReferences().get(i).getText();
    // }
    //
    // public String getMoreInfo(final int i) {
    // final TLDocumentation tld = getDocumentation();
    // return (tld == null || tld.getMoreInfos() == null || tld.getMoreInfos().size() <= i) ? "" :
    // tld.getMoreInfos()
    // .get(i).getText();
    // }
    //
    // public String getOtherDoc(final String context) {
    // final TLDocumentation tld = getDocumentation();
    // String text = "";
    // if (tld != null) {
    // final TLAdditionalDocumentationItem otherDoc = tld.getOtherDoc(context);
    // if (otherDoc != null) {
    // text = otherDoc.getText();
    // }
    // }
    // return text;
    // }

    public void addDeprecation(String string) {
        TLDocumentation tld = getDocumentation();
        if (tld == null) {
            tld = createDocumentation();
        }
        TLDocumentationItem deprecation = new TLDocumentationItem();
        deprecation.setText(string);
        tld.addDeprecation(deprecation);
    }

    /**
     * Set the deprecation to the passed string
     * 
     * @param i
     *            - index of the deprecation to set
     * @param string
     */
    public void setDeprecatedDoc(final String string, final int i) {
        TLDocumentation tld = getDocumentation();
        if (tld == null) {
            tld = createDocumentation();
        }
        TLDocumentationItem deprecation = null;
        if (tld.getDeprecations().isEmpty()) {
            deprecation = new TLDocumentationItem();
            tld.addDeprecation(deprecation);
        } else {
            deprecation = tld.getDeprecations().get(i);
        }
        deprecation.setText(string);
    }

    public void addDescription(String string) {
        TLDocumentation tld = getDocumentation();
        if (tld == null) {
            tld = createDocumentation();
        }
        if (tld.getDescription() == null || tld.getDescription().isEmpty())
            tld.setDescription(string);
        else
            tld.setDescription(tld.getDescription() + " " + string);
    }

    public void setDescriptionDoc(final String string) {
        TLDocumentation tld = getDocumentation();
        if (tld == null) {
            tld = createDocumentation();
        }
        tld.setDescription(string);
    }

    public void addImplementer(String string) {
        TLDocumentation tld = getDocumentation();
        if (tld == null) {
            tld = createDocumentation();
        }
        TLDocumentationItem implementer = new TLDocumentationItem();
        implementer.setText(string);
        tld.addImplementer(implementer);
    }

    public void setDeveloperDoc(final String string, final int index) {
        TLDocumentation tld = getDocumentation();
        if (tld == null) {
            tld = createDocumentation();
        }
        TLDocumentationItem devDoc = null;
        if (tld.getImplementers().isEmpty()) {
            devDoc = new TLDocumentationItem();
            tld.addImplementer(devDoc);
        } else {
            devDoc = tld.getImplementers().get(index);
        }
        devDoc.setText(string);
    }

    public void addReference(String string) {
        TLDocumentation tld = getDocumentation();
        if (tld == null) {
            tld = createDocumentation();
        }
        TLDocumentationItem ref = new TLDocumentationItem();
        ref.setText(string);
        tld.addReference(ref);
    }

    public void setReferenceDoc(final String string, final int index) {
        TLDocumentation tld = getDocumentation();
        if (tld == null) {
            tld = createDocumentation();
        }
        TLDocumentationItem refDoc = null;
        if (tld.getReferences().isEmpty()) {
            refDoc = new TLDocumentationItem();
            tld.addReference(refDoc);
        } else {
            refDoc = tld.getReferences().get(index);
        }
        refDoc.setText(string);
    }

    public void addMoreInfo(String string) {
        TLDocumentation tld = getDocumentation();
        if (tld == null) {
            tld = createDocumentation();
        }
        TLDocumentationItem mi = new TLDocumentationItem();
        mi.setText(string);
        tld.addMoreInfo(mi);
    }

    public void setMoreInfo(final String string, final int index) {
        TLDocumentation tld = getDocumentation();
        if (tld == null) {
            tld = createDocumentation();
        }
        TLDocumentationItem infoDoc = null;
        if (tld.getMoreInfos().isEmpty()) {
            infoDoc = new TLDocumentationItem();
            tld.addMoreInfo(infoDoc);
        } else {
            infoDoc = tld.getMoreInfos().get(index);
        }
        infoDoc.setText(string);
    }

    public void addOther(String string) {
        TLDocumentation tld = getDocumentation();
        if (tld == null) {
            tld = createDocumentation();
        }
        TLAdditionalDocumentationItem other = new TLAdditionalDocumentationItem();
        other.setContext(OtmRegistry.getMainController().getContextController()
                .getDefaultContextId());
        other.setText(string);
        tld.addOtherDoc(other);
    }

    public void setOtherDoc(final String string, final String context) {
        TLDocumentation tld = getDocumentation();
        if (tld == null) {
            tld = createDocumentation();
        }
        TLAdditionalDocumentationItem otherDoc = tld.getOtherDoc(context);
        if (otherDoc == null) {
            otherDoc = new TLAdditionalDocumentationItem();
            otherDoc.setContext(context);
            tld.addOtherDoc(otherDoc);
        }
        otherDoc.setText(string);
    }

    public void removeOtherDoc(final String context) {
        final TLDocumentation tld = getDocumentation();
        if (tld != null) {
            final TLAdditionalDocumentationItem otherDoc = tld.getOtherDoc(context);
            tld.removeOtherDoc(otherDoc);
        }
    }

    public void addAlias(final TLAlias tla) {
    }

    public void addToLibrary(AbstractLibrary tlLibrary) {
        if (srcObj instanceof LibraryMember && tlLibrary instanceof TLLibrary) {
            LibraryMember lm = (LibraryMember) srcObj;
            if (lm.getOwningLibrary() != tlLibrary) {
                // lm.setOwningLibrary(tlLibrary); // just sets library field--don't use
                tlLibrary.addNamedMember(lm);
                // Question - do we need to move if already in a different lib?
            }
        }
    }

    // public boolean isSimpleType() {
    // return false;
    // }

    public boolean moveUp() {
        LOGGER.debug("ModelObject:moveUp() NOT IMPLEMENTED for object class "
                + getClass().getSimpleName());
        return false;
    }

    public boolean moveDown() {
        LOGGER.debug("ModelObject:moveDpwn() NOT IMPLEMENTED for object class "
                + getClass().getSimpleName());
        return false;
    }

    protected int indexOf() {
        return 0;
    }

    public void removeFromTLParent() {
    }

    public void addToTLParent(final ModelObject<?> mo, int index) {
    }

    public void addToTLParent(final ModelObject<?> mo) {
    }

    // Override in model objects that have assigned types.
    public void clearTLType() {
        // LOGGER.error("clear not needed for a "+this.getClass().getSimpleName());
    }

    /**
     * Only a few objects have model object types. The ones that do return the assigned type.
     * 
     * @return the type assigned or null
     */
    public NamedEntity getTLType() {
        // return type;
        return null;
    }

    public NamedEntity getTLBase() {
        return null;
    }

    /**
     * Create the tl model representation of a jaxB element attached to the xsd Node.
     * 
     * @param xsdNode
     * @return
     */
    public LibraryMember buildTLModel(XsdNode xsdNode) {
        return null;
    }

    /**
     * @return - list of TLContexts or else empty list Contexts are used in OtherDocs, facets,
     *         examples and equivalents. Overridden for attributes/elements/indicators that have
     *         examples and equivalents
     */
    public List<TLContext> getContexts() {
        if (!(getTLModelObj() instanceof LibraryMember))
            return Collections.emptyList();
        if (!(((LibraryMember) getTLModelObj()).getOwningLibrary() instanceof TLLibrary))
            return Collections.emptyList();

        ArrayList<TLContext> list = new ArrayList<TLContext>();
        HashSet<String> ids = new HashSet<String>();
        if (!(getTLModelObj() instanceof LibraryMember))
            return list;

        if (getTLModelObj() instanceof TLBusinessObject) {
            TLBusinessObject tlBO = (TLBusinessObject) getTLModelObj();
            if (tlBO.getCustomFacets() != null) {
                for (TLFacet f : tlBO.getCustomFacets()) {
                    ids.add(f.getContext());
                }
            }
            if (tlBO.getQueryFacets() != null) {
                for (TLFacet f : tlBO.getQueryFacets()) {
                    ids.add(f.getContext());
                }
            }
        }
        if (getTLModelObj() instanceof TLEquivalentOwner) {
            TLEquivalentOwner tle = (TLEquivalentOwner) getTLModelObj();
            for (TLEquivalent e : tle.getEquivalents())
                ids.add(e.getContext());
        }
        if (getTLModelObj() instanceof TLExampleOwner) {
            TLExampleOwner tle = (TLExampleOwner) getTLModelObj();
            for (TLExample e : tle.getExamples())
                ids.add(e.getContext());
        }

        if (getTLModelObj() instanceof TLDocumentationOwner) {
            TLDocumentationOwner tld = (TLDocumentationOwner) getTLModelObj();

            if (tld.getDocumentation() != null) {
                for (TLAdditionalDocumentationItem doc : tld.getDocumentation().getOtherDocs()) {
                    ids.add(doc.getContext());
                }
            }
        }

        // now use the unique ids in the hash to extract the contexts from the TL Library.
        TLLibrary tlLib = (TLLibrary) ((LibraryMember) getTLModelObj()).getOwningLibrary();
        for (String id : ids) {
            TLContext tlc = tlLib.getContext(id);
            if (tlc != null)
                list.add(tlLib.getContext(id));
        }
        return list;
    }

    public void setList(boolean selected) {
    }

    public boolean isSimpleList() {
        return false;
    }

    public void sort() {
        LOGGER.debug("ModelObject:sort() NOT IMPLEMENTED for object class "
                + getClass().getSimpleName());
    }

    /**
     * Attempt to add a child to this object.
     * 
     * @return false if the child could not be added.
     */
    public boolean addChild(TLModelElement child) {
        return false;
    }

}
