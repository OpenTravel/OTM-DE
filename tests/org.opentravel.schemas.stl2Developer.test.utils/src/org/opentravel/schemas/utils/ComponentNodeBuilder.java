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
package org.opentravel.schemas.utils;

import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.interfaces.FacetOwner;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.ExtensionPointNode;
import org.opentravel.schemas.node.objectMembers.OperationNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.ChoiceObjectNode;
import org.opentravel.schemas.node.typeProviders.EnumerationClosedNode;
import org.opentravel.schemas.node.typeProviders.EnumerationOpenNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.types.SimpleAttributeOwner;
import org.opentravel.schemas.types.TypeProvider;

/**
 * @author Pawel Jedruch
 * 
 */
public class ComponentNodeBuilder<T extends ComponentNode> {

	public static class ExtensionPointBuilder extends ComponentNodeBuilder<ExtensionPointNode> {

		public ExtensionPointBuilder(ExtensionPointNode coreObject) {
			super(coreObject);
		}

		public ExtensionPointBuilder extend(Node property) {
			componentObject.setExtension(property);
			return this;
		}
	}

	public static class EnumerationOpenBuilder extends ComponentNodeBuilder<EnumerationOpenNode> {

		public EnumerationOpenBuilder(EnumerationOpenNode coreObject) {
			super(coreObject);
		}

		public EnumerationOpenBuilder extend(Node property) {
			componentObject.setExtension(property);
			return this;
		}
	}

	public static class EnumerationClosedBuilder extends ComponentNodeBuilder<EnumerationClosedNode> {

		public EnumerationClosedBuilder(EnumerationClosedNode coreObject) {
			super(coreObject);
		}

		public EnumerationClosedBuilder extend(Node property) {
			componentObject.setExtension(property);
			return this;
		}
	}

	public static class SimpleNodeBuilder extends ComponentNodeBuilder<SimpleTypeNode> {

		public SimpleNodeBuilder(SimpleTypeNode coreObject) {
			super(coreObject);
		}

		public SimpleNodeBuilder assignType(Node property) {
			componentObject.setAssignedType((TypeProvider) property);
			return this;
		}
	}

	public static class ServiceNodeBuilder extends ComponentNodeBuilder<ServiceNode> {

		public ServiceNodeBuilder(ServiceNode coreObject) {
			super(coreObject);
		}

		public ServiceNodeBuilder createCRUDQOperations(BusinessObjectNode buisinesObject) {
			componentObject.addCRUDQ_Operations(buisinesObject);
			return this;
		}

		public ServiceNodeBuilder addOperation(String name) {
			new OperationNode(componentObject, name);
			return this;
		}
	}

	public static class CoreNodeBuilder extends ComponentNodeBuilder<CoreObjectNode> {

		public CoreNodeBuilder(CoreObjectNode coreObject) {
			super(coreObject);
		}

		public CoreNodeBuilder addToSummaryFacet(PropertyNode property) {
			componentObject.getFacet_Summary().addProperty(property);
			return this;
		}

		public CoreNodeBuilder extend(CoreObjectNode boBase) {
			componentObject.setExtension(boBase);
			return this;
		}

	}

	public static class ChoiceNodeBuilder extends ComponentNodeBuilder<ChoiceObjectNode> {

		public ChoiceNodeBuilder(ChoiceObjectNode coreObject) {
			super(coreObject);
		}

		public ChoiceNodeBuilder addToSummaryFacet(PropertyNode property) {
			componentObject.getFacet_Summary().addProperty(property);
			return this;
		}

		public ChoiceNodeBuilder extend(ChoiceObjectNode boBase) {
			componentObject.setExtension(boBase);
			return this;
		}

	}

	public static class VWANodeBuilder extends ComponentNodeBuilder<VWA_Node> {

		public VWANodeBuilder(VWA_Node coreObject) {
			super(coreObject);
		}

		public VWANodeBuilder addAttribute(PropertyNode property) {
			componentObject.getFacet_Attributes().addProperty(property);
			return this;
		}

		public VWANodeBuilder extend(VWA_Node base) {
			componentObject.setExtension(base);
			return this;
		}
	}

	public static class BusinessNodeBuilder extends ComponentNodeBuilder<BusinessObjectNode> {
		public BusinessNodeBuilder(BusinessObjectNode businessObject) {
			super(businessObject);
		}

		// public BusinessNodeBuilder addQueryFacet(String name) {
		// // String context = null;
		// ContextualFacetNode newFacet = new QueryFacetNode();
		// componentObject.getTLModelObject().addQueryFacet(newFacet.getTLModelObject());
		// // componentObject.getModelObject().addQueryFacet((TLContextualFacet) newFacet.getTLModelObject());
		// componentObject.linkChild(newFacet);
		// newFacet.setName(name);
		// return this;
		// }

		// @Deprecated
		// public BusinessNodeBuilder addCustomFacet(String name) {
		// // String context = null;
		// CustomFacetNode newFacet = new CustomFacetNode();
		// componentObject.getTLModelObject().addCustomFacet(newFacet.getTLModelObject());
		// // componentObject.getModelObject().addCustomFacet((TLContextualFacet) newFacet.getTLModelObject());
		// componentObject.linkChild(newFacet);
		// newFacet.setName(name);
		// return this;
		// }

		public BusinessNodeBuilder extend(BusinessObjectNode boBase) {
			componentObject.setExtension(boBase);
			return this;
		}

		public BusinessNodeBuilder addAlias(String name) {
			new AliasNode(componentObject, name);
			return this;
		}
	}

	protected final T componentObject;

	public ComponentNodeBuilder(T coreObject) {
		this.componentObject = coreObject;
	}

	public static ComponentNodeBuilder<ComponentNode> createSimpleCore(String name) {
		CoreObjectNode newNode = (CoreObjectNode) NodeFactory.newLibraryMember(new TLCoreObject());
		newNode.setName(name);
		newNode.setAssignedType((TypeProvider) NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE));
		PropertyNode newProp = new ElementNode(newNode.getFacet_Summary(), "Property",
				((TypeProvider) NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE)));
		return new ComponentNodeBuilder<ComponentNode>(newNode);
	}

	public static CoreNodeBuilder createCoreObject(String name) {
		TLCoreObject tlCoreObject = new TLCoreObject();
		tlCoreObject.setName(name);
		tlCoreObject.getSimpleListFacet();
		CoreObjectNode newNode = (CoreObjectNode) NodeFactory.newLibraryMember(tlCoreObject);
		return new CoreNodeBuilder(newNode);
	}

	public static ChoiceNodeBuilder createChoiceObject(String name) {
		TLChoiceObject tlChoiceObject = new TLChoiceObject();
		tlChoiceObject.setName(name);
		// tlCoreObject.getSimpleListFacet();
		ChoiceObjectNode newNode = (ChoiceObjectNode) NodeFactory.newLibraryMember(tlChoiceObject);
		return new ChoiceNodeBuilder(newNode);
	}

	public static VWANodeBuilder createVWA(String name) {
		TLValueWithAttributes tl = new TLValueWithAttributes();
		tl.setName(name);
		TLSimple simple = new TLSimple();
		simple.setName("fakeName");
		tl.setParentType(simple); // assignment should fail because it is recursive
		// type assignment will fail because of no library
		VWA_Node vwa = (VWA_Node) NodeFactory.newLibraryMember(tl);
		return new VWANodeBuilder(vwa);
	}

	public static BusinessNodeBuilder createBusinessObject(String name) {
		TLBusinessObject tl = new TLBusinessObject();
		tl.setName(name);
		BusinessObjectNode businessNode = (BusinessObjectNode) NodeFactory.newLibraryMember(tl);
		return new BusinessNodeBuilder(businessNode);
	}

	public static SimpleNodeBuilder createSimpleObject(String name) {
		TLSimple simple = new TLSimple();
		simple.setName(name);
		SimpleTypeNode so = (SimpleTypeNode) NodeFactory.newLibraryMember(simple);
		return new SimpleNodeBuilder(so);
	}

	public static ExtensionPointBuilder createExtensionPoint(String name) {
		TLExtensionPointFacet simple = new TLExtensionPointFacet();
		ExtensionPointNode so = (ExtensionPointNode) NodeFactory.newLibraryMember(simple);
		return new ExtensionPointBuilder(so);
	}

	public static EnumerationOpenBuilder createEnumerationOpen(String name) {
		TLOpenEnumeration simple = new TLOpenEnumeration();
		simple.setName(name);
		EnumerationOpenNode so = (EnumerationOpenNode) NodeFactory.newLibraryMember(simple);
		return new EnumerationOpenBuilder(so);
	}

	public static EnumerationClosedBuilder createEnumerationClosed(String name) {
		TLClosedEnumeration simple = new TLClosedEnumeration();
		simple.setName(name);
		EnumerationClosedNode so = (EnumerationClosedNode) NodeFactory.newLibraryMember(simple);
		return new EnumerationClosedBuilder(so);
	}

	public static ServiceNodeBuilder createService(String name, LibraryNode libraryNode) {
		TLService service = new TLService();
		service.setName(name);
		ServiceNode serviceNode = new ServiceNode(service, libraryNode);
		return new ServiceNodeBuilder(serviceNode);
	}

	public ComponentNodeBuilder<T> addProperty(String name) {
		TypeProvider type = ((TypeProvider) NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE));
		PropertyNode newProp = new ElementNode(((FacetOwner) componentObject).getFacet_Summary(), name, type);
		return this;
	}

	public ComponentNodeBuilder<T> setAssignedType() {
		if (componentObject instanceof SimpleAttributeOwner) {
			((SimpleAttributeOwner) componentObject)
					.setAssignedType((TypeProvider) NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE));
		}
		return this;
	}

	public T get() {
		return componentObject;
	}

	public T get(LibraryNode target) {
		assert componentObject instanceof LibraryMemberInterface;
		assert target != null;
		target.addMember((LibraryMemberInterface) componentObject);
		return componentObject;
	}

}
