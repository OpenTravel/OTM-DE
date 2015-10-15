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
package org.opentravel.schemas.node.controllers;

import java.util.LinkedList;
import java.util.List;

import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.repository.impl.BuiltInProject;
import org.opentravel.schemas.modelObject.FacetMO;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.EnumerationClosedNode;
import org.opentravel.schemas.node.EnumerationOpenNode;
import org.opentravel.schemas.node.LibraryChainNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.RenamableFacet;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.SimpleAttributeNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.springframework.util.Assert;

/**
 * Purpose of this class is to having fluent interface to check against different model structure.
 * 
 * This fluent interface uses a Matcher to test properties. Get() will run all matching assertions and return boolean
 * result. http://www.martinfowler.com/bliki/FluentInterface.html
 * 
 * Usage Example:
 * 
 * <pre>
 * // check if node is VWA's simple property
 * NodeUtils.checker(node).ownerIs(ComponentNodeType.VWA).is(PropertyNodeType.SIMPLE).get()
 * 
 * <pre>
 * 
 * @author Pawel Jedruch
 */
public class NodeUtils {

	public static NodeChecker checker(Node node) {
		return new NodeChecker(node);
	}

	private static interface Matcher {
		public boolean match();
	}

	private static class ComponentMatcher implements Matcher {

		private Node node;
		private ComponentNodeType type;

		public ComponentMatcher(Node node, ComponentNodeType type) {
			this.node = node;
			this.type = type;
		}

		@Override
		public boolean match() {
			return (node instanceof ComponentNode) && isComponent(node, type);
		}

		private Boolean isComponent(Node node, ComponentNodeType type) {
			switch (type) {
			case ALIAS:
				return node.isAlias();
			case BUSINESS:
				return node.isBusinessObject();
			case CLOSED_ENUM:
				return node instanceof EnumerationClosedNode;
			case OPEN_ENUM:
				return node instanceof EnumerationOpenNode;
			case CORE:
				return node.isCoreObject();
			case EXTENSION_POINT:
				return node.isExtensionPointFacet();
			case MESSAGE:
				return node.isMessage();
			case OPERATION:
				return node.isOperation();
			case REQUEST:
				return false; // TODO: how to check this ?
			case RESPONSE:
				return false; // TODO: how to check this ?
			case NOTIFICATION:
				return false; // TODO: how to check this ?
			case SERVICE:
				return node.isService();
			case SIMPLE:
				return node.isSimpleType();
			case VWA:
				return node.isValueWithAttributes();
			default:
				return false;

			}
		}

	}

	private static class PropertyMatcher implements Matcher {

		private Node node;
		private PropertyNodeType type;

		public PropertyMatcher(Node node, PropertyNodeType type) {
			this.node = node;
			this.type = type;
		}

		@Override
		public boolean match() {
			if (node instanceof PropertyNode) {
				PropertyNode pn = (PropertyNode) node;
				return type.equals(pn.getPropertyType());
			}
			return false;
		}

	}

	public static class NodeChecker {
		private final Node node;
		private final List<Matcher> matches = new LinkedList<Matcher>();

		public NodeChecker(Node node) {
			this.node = node;
		}

		public boolean get() {
			for (Matcher m : matches) {
				if (!m.match())
					return false;
			}
			return true;
		}

		protected List<Matcher> getMatches() {
			return matches;
		}

		public NodeChecker is(ComponentNodeType type) {
			getMatches().add(new ComponentMatcher(node, type));
			return this;
		}

		public NodeChecker ownerIs(ComponentNodeType type) {
			getMatches().add(new ComponentMatcher(node.getOwningComponent(), type));
			return this;
		}

		public NodeChecker is(PropertyNodeType type) {
			getMatches().add(new PropertyMatcher(node, type));
			return this;
		}

		public NodeChecker isExampleSupported() {
			getMatches().add(new Matcher() {

				@Override
				public boolean match() {
					return node.getTLModelObject() instanceof TLExampleOwner;
				}
			});
			return this;
		}

		/**
		 * Check if given node is facet that is inherited from extension.
		 */
		public NodeChecker isInheritedFacet() {
			getMatches().add(new Matcher() {

				@Override
				public boolean match() {
					if (node instanceof RenamableFacet) {
						RenamableFacet f = (RenamableFacet) node;
						if (f.getModelObject() instanceof FacetMO) {
							FacetMO ff = (FacetMO) f.getModelObject();
							return ff.isInherited();
						} else {
							// DEBUG
						}
					}
					return false;
				}

			});
			return this;
		}

		public NodeChecker isPatch() {
			getMatches().add(new Matcher() {

				@Override
				public boolean match() {
					if (node.getLibrary() == null)
						return false;
					if (node.getLibrary().isInChain()) {
						LibraryChainNode chain = node.getLibrary().getChain();
						return chain.isPatch();
					}
					return node.getLibrary().isPatchVersion();
				}

			});
			return this;
		}

		public NodeChecker isInMinorOrPatch() {
			getMatches().add(new Matcher() {

				@Override
				public boolean match() {
					if (node.getLibrary() == null)
						return false;
					if (node.getLibrary().isInChain()) {
						LibraryChainNode chain = node.getLibrary().getChain();
						return chain.isMinor() || chain.isPatch();
					}
					return node.getLibrary().isPatchVersion();
				}

			});
			return this;
		}

		public NodeChecker existInPreviousVersions() {
			getMatches().add(new Matcher() {

				@Override
				public boolean match() {
					if (node.getLibrary().isInChain()) {
						// Simple attributes will exist on all versions of the object
						if (node instanceof SimpleAttributeNode)
							return true;
						// Other nodes will have existed if they are not in the head of the chain
						LibraryNode head = node.getLibrary().getChain().getHead();
						return head != node.getLibrary();
					}
					return false;
				}

			});
			return this;
		}

		/**
		 * True only if in a version chain and is in the head version (latest version).
		 * 
		 * @return
		 */
		public NodeChecker isNewToChain() {
			getMatches().add(new Matcher() {

				@Override
				public boolean match() {
					Assert.notNull(node.getLibrary());

					if (node.getLibrary().isInChain()) {
						// Simple attributes will exist on all versions of the object
						if (node instanceof SimpleAttributeNode)
							return false;
						// Other nodes will have existed if they are not in the head of the chain
						return node.getChain().getHead() == node.getLibrary();
					}
					return false;
				}

			});
			return this;
		}
	}

	// TODO: find better place for this method

	public static boolean isBuildInProject(ProjectNode n) {
		return BuiltInProject.BUILTIN_PROJECT_ID.equals(n.getProject().getProjectId());
	}

	public static boolean isProject(Node n) {
		return n instanceof ProjectNode;
	}

	public static boolean isDefaultProject(Node n) {
		return n == OtmRegistry.getMainController().getProjectController().getDefaultProject();
	}

}
