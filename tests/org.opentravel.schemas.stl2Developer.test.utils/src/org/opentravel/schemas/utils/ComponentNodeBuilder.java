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
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemas.node.AliasNode;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.ChoiceObjectNode;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.EnumerationClosedNode;
import org.opentravel.schemas.node.EnumerationOpenNode;
import org.opentravel.schemas.node.ExtensionPointNode;
import org.opentravel.schemas.node.FacetNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.OperationNode;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.SimpleTypeNode;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.PropertyNode;
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
			componentObject.getSummaryFacet().addProperty(property);
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
			componentObject.getSummaryFacet().addProperty(property);
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
			componentObject.getAttributeFacet().addProperty(property);
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

		public BusinessNodeBuilder addQueryFacet(String name) {
			String context = null;
			FacetNode newFacet = createFacetNode(name, context, TLFacetType.QUERY);
			componentObject.getModelObject().addQueryFacet((TLFacet) newFacet.getTLModelObject());
			componentObject.linkChild(newFacet, false);
			newFacet.setName(name);
			return this;
		}

		public BusinessNodeBuilder addCustomFacet(String name) {
			String context = null;
			FacetNode newFacet = createFacetNode(name, context, TLFacetType.CUSTOM);
			componentObject.getModelObject().addCustomFacet((TLFacet) newFacet.getTLModelObject());
			componentObject.linkChild(newFacet, false);
			newFacet.setName(name);
			return this;
		}

		public BusinessNodeBuilder addCustomFacet(String name, String context) {
			FacetNode newFacet = createFacetNode(name, context, TLFacetType.CUSTOM);
			componentObject.getModelObject().addCustomFacet((TLFacet) newFacet.getTLModelObject());
			componentObject.linkChild(newFacet, false);
			newFacet.setName(name);
			return this;
		}

		private FacetNode createFacetNode(String name, String context, TLFacetType type) {
			TLFacet tl = createFacet(type);
			tl.setContext(context);
			tl.setLabel(name);
			FacetNode node = new FacetNode(tl);
			return node;
		}

		private TLFacet createFacet(TLFacetType tlFacetType) {
			TLFacet f = new TLFacet();
			f.setFacetType(tlFacetType);
			return f;
		}

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
		CoreObjectNode newNode = (CoreObjectNode) NodeFactory.newComponent(new TLCoreObject());
		newNode.setName(name);
		newNode.setSimpleType((TypeProvider) NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE));
		PropertyNode newProp = new ElementNode(newNode.getSummaryFacet(), "Property");
		newProp.setAssignedType((TypeProvider) NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE));
		return new ComponentNodeBuilder<ComponentNode>(newNode);
	}

	public static CoreNodeBuilder createCoreObject(String name) {
		TLCoreObject tlCoreObject = new TLCoreObject();
		tlCoreObject.setName(name);
		tlCoreObject.getSimpleListFacet();
		CoreObjectNode newNode = (CoreObjectNode) NodeFactory.newComponent(tlCoreObject);
		return new CoreNodeBuilder(newNode);
	}

	public static ChoiceNodeBuilder createChoiceObject(String name) {
		TLChoiceObject tlChoiceObject = new TLChoiceObject();
		tlChoiceObject.setName(name);
		// tlCoreObject.getSimpleListFacet();
		ChoiceObjectNode newNode = (ChoiceObjectNode) NodeFactory.newComponent(tlChoiceObject);
		return new ChoiceNodeBuilder(newNode);
	}

	public static VWANodeBuilder createVWA(String name) {
		TLValueWithAttributes tl = new TLValueWithAttributes();
		tl.setName(name);
		TLSimple simple = new TLSimple();
		simple.setName("fakeName");
		tl.setParentType(simple); // assignment should fail because it is recursive
		// type assignment will fail because of no library
		VWA_Node vwa = (VWA_Node) NodeFactory.newComponent(tl);
		return new VWANodeBuilder(vwa);
	}

	public static BusinessNodeBuilder createBusinessObject(String name) {
		TLBusinessObject tl = new TLBusinessObject();
		tl.setName(name);
		BusinessObjectNode businessNode = (BusinessObjectNode) NodeFactory.newComponent(tl);
		return new BusinessNodeBuilder(businessNode);
	}

	public static SimpleNodeBuilder createSimpleObject(String name) {
		TLSimple simple = new TLSimple();
		simple.setName(name);
		SimpleTypeNode so = (SimpleTypeNode) NodeFactory.newComponent(simple);
		return new SimpleNodeBuilder(so);
	}

	public static ExtensionPointBuilder createExtensionPoint(String name) {
		TLExtensionPointFacet simple = new TLExtensionPointFacet();
		ExtensionPointNode so = (ExtensionPointNode) NodeFactory.newComponent(simple);
		return new ExtensionPointBuilder(so);
	}

	public static EnumerationOpenBuilder createEnumerationOpen(String name) {
		TLOpenEnumeration simple = new TLOpenEnumeration();
		simple.setName(name);
		EnumerationOpenNode so = (EnumerationOpenNode) NodeFactory.newComponent(simple);
		return new EnumerationOpenBuilder(so);
	}

	public static EnumerationClosedBuilder createEnumerationClosed(String name) {
		TLClosedEnumeration simple = new TLClosedEnumeration();
		simple.setName(name);
		EnumerationClosedNode so = (EnumerationClosedNode) NodeFactory.newComponent(simple);
		return new EnumerationClosedBuilder(so);
	}

	public static ServiceNodeBuilder createService(String name, LibraryNode libraryNode) {
		TLService service = new TLService();
		service.setName(name);
		ServiceNode serviceNode = new ServiceNode(service, libraryNode);
		return new ServiceNodeBuilder(serviceNode);
	}

	public ComponentNodeBuilder<T> addProperty(String name) {
		PropertyNode newProp = new ElementNode(componentObject.getSummaryFacet(), name);
		newProp.setAssignedType((TypeProvider) NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE));
		return this;
	}

	public ComponentNodeBuilder<T> setSimpleType() {
		if (componentObject instanceof SimpleAttributeOwner) {
			((SimpleAttributeOwner) componentObject).setSimpleType((TypeProvider) NodeFinders.findNodeByName("string",
					Node.XSD_NAMESPACE));
		}
		return this;
	}

	public T get() {
		return componentObject;
	}

	public T get(LibraryNode target) {
		target.addMember(componentObject);
		return componentObject;
	}

}
