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
/**
 * 
 */
package org.opentravel.schemas.node;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.ElementReferenceNode;
import org.opentravel.schemas.node.properties.IdNode;
import org.opentravel.schemas.node.properties.IndicatorElementNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.utils.FacetNodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
// TODO - test deleting the source and the clone with full visit node afterwards
public class Clone_Tests {
	private static final Logger LOGGER = LoggerFactory.getLogger(Clone_Tests.class);

	ModelNode model = null;
	NodeTesters tt = new NodeTesters();
	SimpleTypeNode builtin = null;
	private LibraryNode ln = null;
	MainController mc = null;
	MockLibrary mockLibrary = null;
	DefaultProjectController pc = null;
	ProjectNode defaultProject = null;

	@Before
	public void beforeEachTest() {
		mc = new MainController();
		mockLibrary = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
		ln = mockLibrary.createNewLibrary("http://example.com/test", "test", defaultProject);
		ln.setEditable(true);
		builtin = (SimpleTypeNode) NodeFinders.findNodeByName("date", Node.XSD_NAMESPACE);
	}

	@Test
	public void shouldCloneTLObjects() {
		// the first step in cloning is cloning the TL Object. This is a facade for the TL model cloneElement()
		FacetNode facet = FacetNodeBuilder.create(ln).addElements("E1").addAttributes("A1").addIndicators("I1").build();
		// TODO - Add these to FacetNodeBuilder
		new IdNode(facet, "Id");
		new ElementReferenceNode(facet, "elementRef");
		new IndicatorElementNode(facet, "indicatorElement");
		assert facet.getChildren().size() == 6;

		// Check each property as they are cloned. Clones have no owner.
		List<Node> kids = new ArrayList<Node>(facet.getChildren()); // list get added to by clone
		for (Node n : kids) {
			((TypeUser) n).setAssignedType(builtin);
			LibraryElement clone = n.cloneTLObj();
			assert clone != null;
			if (clone instanceof TLProperty) {
				// elements are of type TLPropery
				assert ((TLProperty) clone).getName().equals(n.getName());
				assert ((TLProperty) clone).getType().equals(((TypeUser) n).getAssignedTLObject());
				assert ((TLProperty) clone).getOwner() == null;
			}
		}

		// TODO - test cloning non-properties, GUI only uses for properties
		// Assert clones exist, has correct type and builtin count is larger
		LOGGER.debug("Done");
	}

	@Test
	public void shouldCloneElements() {
		FacetNode facet = FacetNodeBuilder.create(ln).addElements("E1", "E2", "E3").build();
		int assignedCount = builtin.getWhereAssignedCount();

		// Given 3 elements were cloned
		List<Node> kids = new ArrayList<Node>(facet.getChildren()); // list get added to by clone
		for (Node n : kids) {
			assert n instanceof TypeUser;
			((TypeUser) n).setAssignedType(builtin);
			// TypeUser clone = (TypeUser) n.clone();
			TypeUser clone = (TypeUser) n.clone("_Clone");
			// TypeUser clone = (TypeUser) n.clone(facet, "Clone");
			// CHECK - listener on the clone tl is NOT original element
			assert Node.GetNode(clone.getTLModelObject()) == clone;
			assert Node.GetNode(clone.getTLModelObject()) != n;
		}

		// Assert clones exist, has correct type and builtin count is larger
		assert facet.getChildren().size() == 6;
		assert builtin.getWhereAssignedCount() == assignedCount + 6;
		for (Node n : facet.getChildren())
			assert ((TypeUser) n).getAssignedType() == builtin;
		LOGGER.debug("Done");
	}

	@Test
	public void shouldCloneAttributes() {
		FacetNode facet = FacetNodeBuilder.create(ln).addAttributes("A1", "A2", "A3").build();
		int assignedCount = builtin.getWhereAssignedCount();

		// Given 3 elements were cloned
		List<Node> kids = new ArrayList<Node>(facet.getChildren()); // list get added to by clone
		for (Node n : kids) {
			assert n instanceof TypeUser;
			((TypeUser) n).setAssignedType(builtin);
			// TypeUser clone = (TypeUser) n.clone();
			TypeUser clone = (TypeUser) n.clone("_Clone");
			// TypeUser clone = (TypeUser) n.clone(facet, "Clone");
			// CHECK - listener on the clone tl is NOT original element
			assert Node.GetNode(clone.getTLModelObject()) == clone;
			assert Node.GetNode(clone.getTLModelObject()) != n;
		}

		// Assert clones exist, has correct type and builtin count is larger
		assert facet.getChildren().size() == 6;
		assert builtin.getWhereAssignedCount() == assignedCount + 6;
		for (Node n : facet.getChildren())
			assert ((TypeUser) n).getAssignedType() == builtin;
		LOGGER.debug("Done");
	}

	@Test
	public void shouldCloneOtherPropertyTypes() {
		FacetNode facet = FacetNodeBuilder.create(ln).addElements("E1").addAttributes("A1").addIndicators("I1").build();
		// TODO - Add these to FacetNodeBuilder
		new IdNode(facet, "Id");
		new ElementReferenceNode(facet, "elementRef");
		new IndicatorElementNode(facet, "indicatorElement");
		assert facet.getChildren().size() == 6;

		List<Node> kids = new ArrayList<Node>(facet.getChildren()); // list get added to by clone
		for (Node n : kids)
			n.clone("_Clone");

		assert facet.getChildren().size() == 12;
	}

	@Test
	public void shouldFailPreTests() {
		FacetNode facet = FacetNodeBuilder.create(ln).addElements("E1", "E2", "E3").build();
		Node kid = facet.getChildren().get(0);
		// ln.remove(kid); // leaves library and parent set
		kid.setLibrary(null);
		kid.setParent(null);
		assert kid.clone() == null;
	}

	@Test
	public void cloneTest() throws Exception {
		MainController mc = new MainController();
		LoadFiles lf = new LoadFiles();
		model = mc.getModelNode();

		LibraryNode source = lf.loadFile5Clean(mc);
		new LibraryChainNode(source); // Test in a chain
		// test cloning within library.
		source.setEditable(true);
		cloneMembers(source, source);

		LOGGER.debug("Testing cloning properties.");
		for (Node ne : source.getDescendants_NamedTypes())
			cloneProperties(ne);
		tt.visitAllNodes(source);

		// commented some libs out to keep the total time down
		LibraryNode target = lf.loadFile1(mc);
		new LibraryChainNode(target); // Test in a chain
		lf.loadTestGroupA(mc);

		lf.cleanModel();
		Node.getModelNode().visitAllNodes(tt.new TestNode());

		LOGGER.debug("\n");
		LOGGER.debug("Testing cloning to new library.");
		for (LibraryNode ln : Node.getAllLibraries()) {
			if (ln.getNamespace().equals(target.getNamespace()))
				continue;
			if (ln.isBuiltIn())
				continue; // these have errors
			ln.setEditable(true);
			cloneMembers(ln, target);
			LOGGER.debug("Cloned members of " + ln);
		}
		LOGGER.debug("Done cloning - starting final check.");
		Node.getModelNode().visitAllNodes(tt.new TestNode());
	}

	private int cloneMembers(LibraryNode ln, LibraryNode target) {
		int mbrCount = 0, equCount = 0;
		Node clone;

		for (Node n : ln.getDescendants_NamedTypes()) {
			// Assert.assertNotNull(n.cloneNew(null)); // no library, so it will fail node tests
			equCount = countEquivelents(n);
			if (n instanceof ServiceNode)
				continue;
			if (ln == target)
				clone = n.clone("_COPY");
			else
				clone = n.clone(target, null);
			if (clone != null) {
				tt.visitAllNodes(clone);
				if (countEquivelents(clone) != equCount)
					LOGGER.debug("Equ error on " + clone);
			}
			mbrCount++;
		}
		return mbrCount;
	}

	private int countEquivelents(Node n) {
		for (Node p : n.getDescendants()) {
			if (p instanceof ElementNode) {
				return ((TLProperty) p.getTLModelObject()).getEquivalents().size();
			}
		}
		return 0;
	}

	private void cloneProperties(Node n) {
		if (n.isNamedType())
			for (Node p : n.getDescendants()) {
				if (p instanceof PropertyNode) {
					if (p.getParent() instanceof ComponentNode)
						((ComponentNode) p.getParent()).addProperty(p.clone());
					else
						LOGGER.debug(p + "has invalid class of parent.");
				}
			}
		tt.visitAllNodes(n);
	}

}
