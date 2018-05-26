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
package org.opentravel.schemas.node.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLMimeType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLParamLocation;
import org.opentravel.schemacompiler.model.TLReferenceType;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemas.controllers.ValidationManager;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.INodeListener;
import org.opentravel.schemas.node.listeners.ListenerFactory;
import org.opentravel.schemas.node.listeners.ResourceDependencyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base generic class for all resource nodes EXCEPT ResourceNode which is a full node. Provides alternative to
 * ModelObjects used in full nodes. Includes some utility classes for matching TL enumerations to GUI strings.
 * 
 * @author Dave
 *
 * @param <TL>
 *            - the matching TLModel elements
 */
public abstract class ResourceBase<TL> extends Node implements ResourceMemberInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceBase.class);
	// TODO - use same generic approach to nodes' tlObj
	protected TL tlObj;
	protected LibraryNode library;

	// These nodes are never presented in navigator tree so they don't need a children handler.
	private List<Node> rChildren = new ArrayList<>();

	public ResourceBase(TL obj) {
		this.tlObj = obj;
		assert tlObj instanceof TLModelElement;
		if (GetNode((TLModelElement) tlObj) == null)
			ListenerFactory.setIdentityListner(this);

		// Sometimes the constructor will need to be invoked super on a newly constructed tl object (for example:
		// ResourceParameter)
		if (getTLOwner() instanceof TLModelElement) {
			parent = this.getNode(getTLOwner().getListeners());

			assert parent != null;
			assert parent instanceof ResourceMemberInterface;
			// assert parent.getLibrary() != null;
			//
			// setLibrary(parent.getLibrary());
			((ResourceMemberInterface) getParent()).addChild(this);
			addChildren(); // can't add children unless parent known.
			addListeners();
		}
	}

	/**
	 * Set the object, add a listener and add children.
	 * 
	 * @param obj
	 */
	public ResourceBase(TL obj, ResourceMemberInterface parent) {
		this.tlObj = obj;
		if (tlObj instanceof TLModelElement)
			ListenerFactory.setIdentityListner(this);
		this.parent = (Node) parent;

		setLibrary(((Node) parent).getLibrary());
		parent.addChild(this);
		addChildren();
		addListeners();
	}

	@Override
	public void addChild(ResourceMemberInterface child) {
		if (!getChildren().contains(child))
			getChildren().add((Node) child);
	}

	/**
	 * Return the rChildren array common to all resource base sub-types
	 */
	@Override
	public List<Node> getChildren() {
		return rChildren;
	}

	public void addListeners() {
	}

	@Override
	public List<Node> getNavChildren(boolean deep) {
		return getChildren();
	}

	/**
	 * Resource members should call this <b>after</b> doing member specific deletes.
	 */
	@Override
	public void delete() {
		// LOGGER.debug("Deleting " + this.getClass().getSimpleName() + " " + this);
		clearListeners();
		if (getParent() != null)
			if (getParent().getChildrenHandler() != null)
				getParent().getChildrenHandler().clear(this);

		if (getParent() instanceof ResourceBase) {
			((ResourceBase<?>) getParent()).rChildren.remove(this);
		}
		deleted = true;
	}

	public static String[] getHttpMethodStrings() {
		int i = 0;
		String[] values = new String[HttpMethod.values().length];
		for (HttpMethod l : HttpMethod.values())
			values[i++] = l.toString();
		return values;

	}

	public static String[] getMimeTypeStrings() {
		int i = 0;
		String[] values = new String[TLMimeType.values().length];
		for (TLMimeType l : TLMimeType.values())
			values[i++] = l.toString(); // display values are from: l.toContentType()
		return values;
	}

	/**
	 * @return - string array with all possible locations
	 */
	public static String[] getParamLocations() {
		int i = 0;
		String[] values = new String[TLParamLocation.values().length];
		for (TLParamLocation l : TLParamLocation.values())
			values[i++] = l.toString();
		return values;
	}

	/**
	 * @return all TL Modeled strings or for abstract resources just the first string
	 */
	public String[] getReferenceTypeStrings() {
		String[] values;
		// TODO - JUNIT - assure first TLRefereneType equals NONE
		// Action Facets on Abstract Resources can not be set to anything but NONE.
		if (getOwningComponent().isAbstract()) {
			values = new String[1];
			values[0] = TLReferenceType.values()[0].toString();
		} else {
			int i = 0;
			values = new String[TLReferenceType.values().length];
			for (TLReferenceType l : TLReferenceType.values())
				values[i++] = l.toString();
		}
		// LOGGER.debug("Reference Type Strings returned: " + values.toString());
		return values;
	}

	public void addChildren() {
	}

	/**
	 * @return non-empty string
	 */
	@Override
	public String getDecoration() {
		return "  (" + this.getClass().getSimpleName() + ")";
	}

	@Override
	public String getDescription() {
		return (tlObj instanceof TLDocumentationOwner) && ((TLDocumentationOwner) tlObj).getDocumentation() != null
				? ((TLDocumentationOwner) tlObj).getDocumentation().getDescription() : "";
	}

	@Override
	public String getLabel() {
		return getName();
	}

	@Override
	public LibraryNode getLibrary() {
		return getOwningComponent() != null ? getOwningComponent().getLibrary() : null;
	}

	@Override
	public ResourceNode getOwningResource() {
		return getOwningComponent();
	}

	@Override
	public ResourceNode getOwningComponent() {
		Node node = this;
		while (!(node instanceof ResourceNode) && node != null)
			node = node.getParent();
		return (ResourceNode) node;
	}

	@Override
	public TLModelElement getTLModelObject() {
		return (TLModelElement) tlObj;
	}

	@Override
	public ValidationFindings getValidationFindings() {
		return ValidationManager.validate(getTLModelObject(), false);
	}

	@Override
	public Collection<String> getValidationMessages() {
		ArrayList<String> msgs = new ArrayList<>();
		ValidationFindings findings = ValidationManager.validate(getTLModelObject(), false);
		for (String f : findings.getValidationMessages(FindingType.ERROR, FindingMessageFormat.MESSAGE_ONLY_FORMAT))
			msgs.add(f);
		for (String f : findings.getValidationMessages(FindingType.WARNING, FindingMessageFormat.MESSAGE_ONLY_FORMAT))
			msgs.add(f);
		return msgs;
	}

	@Override
	public boolean hasNavChildren(boolean deep) {
		return !getChildren().isEmpty();
	}

	/**
	 * Resources are not versioned. Override default node behavior that manages versioning.
	 */
	// FIXME - edit-ability should be based on library state and business object
	@Override
	public boolean isDeleteable() {
		if (getLibrary() == null || parent == null || deleted)
			return false;
		return true;
	}

	@Override
	public boolean isEditable() {
		return getOwningComponent() != null || getOwningComponent().getLibrary() != null
				? getOwningComponent().getLibrary().isEditable() : false;
	}

	@Override
	public boolean isValid() {
		// Set false when checking ONLY this object and its children
		return ValidationManager.isValid(this);
		// ValidationFindings findings = ValidationManager.validate(getTLModelObject(), false);
		// try {
		// findings = TLModelCompileValidator.validateModelElement((TLModelElement) tlObj, false);
		// } catch (Exception e) {
		// // LOGGER.debug("Validation threw error: " + e.getLocalizedMessage());
		// return false;
		// }
		// return !isDeleted() && findings != null ? findings.count(FindingType.ERROR) == 0 : false;

		// return !isDeleted() ? TLModelCompileValidator.validateModelElement((TLModelElement) tlObj, false).count(
		// FindingType.ERROR) == 0 : false;
	}

	@Override
	public boolean isValid_NoWarnings() {
		return ValidationManager.isValidNoWarnings(this);
		// ValidationFindings findings = ValidationManager.validate(getTLModelObject(), false);
		// try {
		// findings = TLModelCompileValidator.validateModelElement((TLModelElement) tlObj, false);
		// } catch (Exception e) {
		// // LOGGER.debug("Validation threw error: " + e.getLocalizedMessage());
		// return false;
		// }
		// return !isDeleted() && findings != null ? findings.count(FindingType.WARNING) == 0 : false;
		// return !isDeleted() ? TLModelCompileValidator.validateModelElement((TLModelElement) tlObj, false).count(
		// FindingType.WARNING) == 0 : false;
	}

	/**
	 * Do Nothing. Individual classes must override if they are dependent on other objects.
	 */
	@Override
	public void removeDependency(ResourceMemberInterface dependent) {
		// LOGGER.debug(this + " has no dependency on " + dependent);
	}

	/**
	 * Utility method to remove all ResourceDependacyListners from a TL Model Element (tlObj) for a specific node.
	 * 
	 * @param tl
	 *            tlObj containing the listeners to remove
	 * @param dependent
	 *            node listening to the TL model element
	 */
	protected void removeListeners(TLModelElement tl, Node dependent) {
		Collection<ModelElementListener> listeners = new ArrayList<>(tl.getListeners());
		for (ModelElementListener listener : listeners)
			if (listener instanceof ResourceDependencyListener)
				if (((ResourceDependencyListener) listener).getNode() == dependent)
					tl.removeListener(listener);
	}

	@Override
	public void setDescription(final String description) {
		if (tlObj instanceof TLDocumentationOwner) {
			TLDocumentationOwner docOwner = (TLDocumentationOwner) tlObj;
			TLDocumentation doc = docOwner.getDocumentation();
			if (doc == null) {
				doc = new TLDocumentation();
				docOwner.setDocumentation(doc);
			}
			doc.setDescription(description);
		}
	}

	protected void clearListeners() {
		if (tlObj instanceof TLModelElement) {
			ArrayList<ModelElementListener> listeners = new ArrayList<>(((TLModelElement) tlObj).getListeners());
			for (ModelElementListener l : listeners)
				if (l instanceof INodeListener) {
					((TLModelElement) tlObj).removeListener(l);
				}
		}
	}

	/**
	 * Enumeration that denotes all of the GUI allowable HTTP methods for a REST action request. See compliler
	 * TLHttpMethod for complete list
	 */
	public enum HttpMethod {
		GET, PUT, POST, DELETE
		// ,OPTIONS,HEAD,PATCH;
	}
}
