/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.schemas.modelObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
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
import org.opentravel.schemas.node.XsdNode;
import org.opentravel.schemas.node.controllers.DocumentationNodeModelManager;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ModelObject abstract class provides a template for working with underlying model source objects. Model Object
 * class is use for actual components and not organizational objects.
 * 
 * Note - model objects do not know about any synthetic objects used in the GUI such as facet nodes, roles, or
 * simpleFacets. They provide access to roles, aliases etc via getters and setters on their base objects
 * (core/business).
 * 
 * Note: there are no model objects for libraries or projects.
 * 
 * TODO - reconcile how documentation is managed here with Documentation Controller. Examples and Equivalents should
 * have the same structure applied (i believe).
 * 
 * GOAL - the original goal was for this class was to isolate node classes from changes in the TL model. The rate of
 * change in that model is slowed way down. Evolution - Either all access to the TLModelObject should be factored out of
 * Node and upper classes OR this should be trimmed way down to only only be kept where needed to simplify
 * implementation for node.
 * 
 * If we make this class and its sub-classes as thin as practical: Consider adding interface. When re-factored, should
 * any behaviors move up into node?
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
	 * Node allows MO level assignments to be reflected back into the node model. Use for type assignments. Allows
	 * business logic and use constraints to be implemented in node classes to help keep this layer thin.
	 * 
	 * @return node associated with this model object.
	 */
	public INode getNode() {
		return node;
	}

	public void setNode(final INode node) {
		this.node = node;
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
		if (node instanceof ExtensionOwner)
			LOGGER.debug("isExtended should be overridden for " + node.getClass().getSimpleName());
		// throw new IllegalStateException("isExtended should be overridden for " + node.getClass().getSimpleName());
		return true;
	}

	public abstract TL getTLModelObj();

	// 6/30 - seems broken. did not find AttributeMO
	// USED ALOT
	// Assert.assertTrue(ap.getModelObject().isSimpleAssignable());
	public boolean isSimpleAssignable() {
		return false;
	}

	public abstract boolean setName(String name);

	public boolean setRepeat(final int count) {
		return false;
	}

	/**
	 * This should only be used by sub-types. The sub-types set the TL model objects.
	 * 
	 * @param tlObj
	 */
	public void setTLType(NamedEntity attributeType) {
	}

	public void setExtendsType(final ModelObject<?> mo) {
		// LOGGER.debug("Set extends type not implemented for: " + this.getClass().getSimpleName());
	}

	/**
	 * Remove the TL object from the TL model. Does <b>not</b> delete the modelObject.
	 */
	public abstract void delete();

	public boolean isDocumentationOwner() {
		return srcObj instanceof TLDocumentationOwner && !(srcObj instanceof TLListFacet);
	}

	/**
	 * @return 1st deprecation string or null
	 */
	public String getDeprecation() {
		final TLDocumentation tld = getDocumentation();
		if (tld == null || tld.getDeprecations() == null || tld.getDeprecations().isEmpty())
			return null;
		return tld.getDeprecations().get(0).getText().isEmpty() ? null : tld.getDeprecations().get(0).getText();
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
		return (tld == null || tld.getImplementers() == null || tld.getImplementers().size() <= i) ? "" : tld
				.getImplementers().get(i).getText();
	}

	/**
	 * ************************************* Documentation *************************************
	 * 
	 * These are commented out because the documentation view uses its own documentation management utilities.
	 * 
	 * TODO - convert the rest of the users to use DocumentationNodeModelManaager then delete these. Also see if
	 * OtmActions that use the setters are still used.
	 * 
	 * @see DocumentationNodeModelManager
	 */

	public void addDeprecation(String string) {
		TLDocumentation tld = getDocumentation();
		if (tld == null) {
			tld = createDocumentation();
		}
		TLDocumentationItem deprecation = new TLDocumentationItem();
		deprecation.setText(string);
		tld.addDeprecation(deprecation);
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

	public boolean moveUp() {
		// LOGGER.debug("ModelObject:moveUp() NOT IMPLEMENTED for object class " + getClass().getSimpleName());
		return false;
	}

	public boolean moveDown() {
		// LOGGER.debug("ModelObject:moveDpwn() NOT IMPLEMENTED for object class " + getClass().getSimpleName());
		return false;
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
	 * @return null (always!)
	 */
	public LibraryMember buildTLModel(XsdNode xsdNode) {
		return null;
	}

	/**
	 * @return - list of TLContexts or else empty list Contexts are used in OtherDocs, facets, examples and equivalents.
	 *         Overridden for attributes/elements/indicators that have examples and equivalents
	 */
	@Deprecated
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

	public void sort() {
		// LOGGER.debug("ModelObject:sort() NOT IMPLEMENTED for object class " + getClass().getSimpleName());
	}

	/**
	 * Attempt to add a child to this object.
	 * 
	 * @return false if the child could not be added.
	 */
	public boolean addChild(TLModelElement child) {
		return false;
	}

	protected static String emptyIfNull(final String string) {
		return string == null ? "" : string;
	}

}
