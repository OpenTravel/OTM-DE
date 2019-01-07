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
package org.opentravel.schemas.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLLibraryMember;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.EditNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.interfaces.AliasOwner;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.ChoiceFacetNode;
import org.opentravel.schemas.node.typeProviders.ChoiceObjectNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.node.typeProviders.CustomFacetNode;
import org.opentravel.schemas.node.typeProviders.QueryFacetNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.NavigatorMenus;
import org.opentravel.schemas.trees.type.ExtensionTreeContentProvider;
import org.opentravel.schemas.trees.type.TypeTreeExtensionSelectionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Good references:
 * http://www.eclipse.org/articles/article.php?file=Article-JFaceWizards/index.html
 */
public class NewComponentWizard extends Wizard implements IDoubleClickListener {
	protected Node targetNode = null;

	public NewComponentWizardPage ncPage1;
	private TypeSelectionPage serviceSubjectSelectionPage;
	private WizardDialog dialog;
	public NavigatorMenus libraryTreeView;
	private EditNode editNode = new EditNode();
	private Node result;

	private final static Logger LOGGER = LoggerFactory.getLogger(NewComponentWizard.class);

	public NewComponentWizard(Node n) {
		super();
		targetNode = n;
		// LOGGER.debug("New Component Wizard created, set targetnode to: " + targetNode.getName());
	}

	@Override
	public void addPages() {
		ImageDescriptor imageDesc = Images.getImageRegistry().getDescriptor("AddComponent");
		ncPage1 = new NewComponentWizardPage("CreateComponent", Messages.getString("wizard.newObject.title"), imageDesc,
				targetNode);
		addPage(ncPage1);

		serviceSubjectSelectionPage = new TypeSelectionPage(
				Messages.getString("wizard.newObject.page.service.pageName"),
				Messages.getString("wizard.newObject.page.service.title"),
				Messages.getString("wizard.newObject.page.service.description"), null, targetNode);
		serviceSubjectSelectionPage.addDoubleClickListener(this);
		// Set the filter to only business objects.
		TLBusinessObject tlbo = new TLBusinessObject();
		// ModelObject<?> tlmo = new BusinessObjMO(tlbo);
		Node filterType = new BusinessObjectNode(tlbo);
		serviceSubjectSelectionPage.setTypeSelectionFilter(new TypeTreeExtensionSelectionFilter(filterType));
		// serviceSubjectSelectionPage.setTypeSelectionFilter(new TypeTreeExtensionSelectionFilter(tlmo));
		serviceSubjectSelectionPage.setTypeTreeContentProvider(new ExtensionTreeContentProvider());
		addPage(serviceSubjectSelectionPage);

	}

	@Override
	public boolean canFinish() {
		// We can finish if an extension facet is selected because it has no name.
		if (ncPage1.getComponentType().equals(ComponentNodeType.EXTENSION_POINT.getDescription()))
			return true;
		if ((ncPage1.getComponentType().isEmpty()) || ncPage1.getName().isEmpty())
			return false;
		return true;
	}

	@Override
	public boolean performFinish() {
		result = newComponent(targetNode, serviceSubjectSelectionPage.getSelectedNode(), ncPage1.getName(),
				ncPage1.getDescription(), ComponentNodeType.fromString(ncPage1.getComponentType()));
		return true;
	}

	/**
	 * Launch the new component wizard.
	 * 
	 * @param parentNode
	 * @return
	 */
	// invoker must instantiate the class first: NewComponent wizard = new NewComponent();
	public Node run(final Shell shell) {
		dialog = new WizardDialog(shell, this);
		dialog.create();
		dialog.open();
		// if (editNode == null || editNode.getName() == null || editNode.getName().isEmpty()) {
		// return null;
		// }
		return result;
	}

	@Override
	public void doubleClick(final DoubleClickEvent event) {
		if (canFinish()) {
			performFinish();
			dialog.close();
		}
	}

	// *******************************************************************************
	// Create the node on finish
	//
	/**
	 * Create a new component node and model object and link it to the passed node's head library Complex or Simple Root
	 * node. Used for creating model objects from nodes constructed by GUI otmHandlers and wizards.
	 * 
	 * @see {@link NewComponent_Tests.java}
	 * @param parent
	 *            parent for the new object. New object will be placed into parent's library.
	 * @param subject
	 *            subject node for services and custom facets
	 * @param type
	 *            objectType defined in {@link ComponentNodeType}
	 * @param name
	 *            name to assign new object
	 * @param description
	 *            description to assign new object
	 * @return node created
	 * 
	 */
	public Node newComponent(final Node parent, final Node subject, final String name, final String description,
			final ComponentNodeType type) {
		if (parent == null || parent.getLibrary() == null)
			return null;

		LibraryNode lib = parent.getLibrary().getHead();
		Node cn = null;
		ContextualFacetNode cf = null;
		TLContextualFacet tlcf = null;

		switch (type) {
		case SERVICE:
			ServiceNode svc = new ServiceNode(parent.getLibrary());
			svc.setName(name);
			svc.addCRUDQ_Operations(subject);
			return svc;
		case ALIAS:
			return parent instanceof AliasOwner ? new AliasNode((AliasOwner) parent, name) : null;
		case BUSINESS:
			cn = linkNewNode(new TLBusinessObject(), lib, name, description);
			break;
		case CHOICE:
			cn = linkNewNode(new TLChoiceObject(), lib, name, description);
			break;
		case CORE:
			cn = linkNewNode(new TLCoreObject(), lib, name, description);
			break;
		case VWA:
			cn = linkNewNode(new TLValueWithAttributes(), lib, name, description);
			break;
		case EXTENSION_POINT:
			cn = linkNewNode(new TLExtensionPointFacet(), lib, name, description);
			break;
		case OPEN_ENUM:
			cn = linkNewNode(new TLOpenEnumeration(), lib, name, description);
			break;
		case CLOSED_ENUM:
			cn = linkNewNode(new TLClosedEnumeration(), lib, name, description);
			break;
		case SIMPLE:
			cn = linkNewNode(new TLSimple(), lib, name, description);
			break;
		case CHOICE_FACET:
			cn = linkContextual(new ChoiceFacetNode(), subject, lib, name, description);
			break;
		case CUSTOM_FACET:
			cn = linkContextual(new CustomFacetNode(), subject, lib, name, description);
			break;
		case QUERY_FACET:
			cn = linkContextual(new QueryFacetNode(), subject, lib, name, description);
			break;
		default:
			break;
		}
		return cn;
	}

	private Node linkContextual(ContextualFacetNode cf, Node subject, LibraryNode lib, String name,
			String description) {
		cf.setName(name);
		cf.setDescription(description);
		lib.addMember(cf);
		if (subject instanceof ContextualFacetOwnerInterface)
			cf.setOwner((ContextualFacetOwnerInterface) subject);
		return cf;
	}

	private Node linkNewNode(TLLibraryMember tlObj, final LibraryNode lib, final String name,
			final String description) {
		LibraryMemberInterface cn = NodeFactory.newLibraryMember(tlObj);

		if (cn != null) {
			((Node) cn).setExtensible(true);
			cn.setName(name);
			((Node) cn).setDescription(description);
			lib.addMember(cn);
			if (cn instanceof ChoiceObjectNode) {
				((ChoiceObjectNode) cn).addFacet("A");
				((ChoiceObjectNode) cn).addFacet("B");
			}
		}
		return (Node) cn;
	}
}
