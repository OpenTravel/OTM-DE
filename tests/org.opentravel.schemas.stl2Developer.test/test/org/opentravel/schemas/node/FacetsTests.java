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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.DefaultRepositoryController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.modelObject.FacetMO;
import org.opentravel.schemas.modelObject.SimpleAttributeMO;
import org.opentravel.schemas.modelObject.SimpleFacetMO;
import org.opentravel.schemas.modelObject.TLnValueWithAttributesFacet;
import org.opentravel.schemas.node.facets.ChoiceFacetNode;
import org.opentravel.schemas.node.facets.ContextualFacetNode;
import org.opentravel.schemas.node.facets.CustomFacetNode;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.facets.ListFacetNode;
import org.opentravel.schemas.node.facets.OperationFacetNode;
import org.opentravel.schemas.node.facets.OperationNode;
import org.opentravel.schemas.node.facets.PropertyOwnerNode;
import org.opentravel.schemas.node.facets.QueryFacetNode;
import org.opentravel.schemas.node.facets.RoleFacetNode;
import org.opentravel.schemas.node.facets.SimpleFacetNode;
import org.opentravel.schemas.node.facets.UpdateFacetNode;
import org.opentravel.schemas.node.facets.VWA_AttributeFacetNode;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;
import org.opentravel.schemas.node.properties.RoleNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.types.TestTypes;
import org.opentravel.schemas.utils.FacetNodeBuilder;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Dave Hollander
 * 
 */
public class FacetsTests {
	static final Logger LOGGER = LoggerFactory.getLogger(FacetsTests.class);

	ModelNode model = null;
	TestTypes tt = new TestTypes();

	NodeTesters nt = new NodeTesters();
	LoadFiles lf = new LoadFiles();
	LibraryTests lt = new LibraryTests();
	MockLibrary ml = null;
	LibraryNode ln = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;

	@Before
	public void beforeAllTests() {
		mc = new MainController();
		ml = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
	}

	@After
	public void afterAllTests() {
		OTM16Upgrade.otm16Enabled = false;
	}

	@Test
	public void constructorTests() {

		// Given two libraries, one managed one not managed
		ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
		LibraryNode ln_inChain = ml.createNewLibrary("http://www.test.com/test1c", "test1c", defaultProject);
		new LibraryChainNode(ln_inChain);
		ln_inChain.setEditable(true);
		assertTrue("Library must exist.", ln != null);
		assertTrue("Library must exist.", ln_inChain != null);
		constructorTL_Tests();
	}

	private void constructorTL_Tests() {
		// Given - one of each TLFacet type.
		TLFacet tlf = new TLFacet();
		assertTrue("Facet must not be null.", tlf != null);
		TLAbstractFacet tlAf = new TLFacet();
		assertTrue("Facet must not be null.", tlAf != null);
		TLListFacet tlLf = new TLListFacet(tlAf); // tlaf should be simple or detail.
		assertTrue("Facet must not be null.", tlLf != null);
		TLSimpleFacet tlSf = new TLSimpleFacet();
		assertTrue("Facet must not be null.", tlSf != null);
		TLValueWithAttributes tlVWA = new TLValueWithAttributes();
		assertTrue("Facet must not be null.", tlVWA != null);
		TLnValueWithAttributesFacet tlVf = new TLnValueWithAttributesFacet(tlVWA);
		assertTrue("Facet must not be null.", tlVf != null);
	}

	@Test
	public void buildersTests() {
		ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);

		FacetNode facetNode1 = FacetNodeBuilder.create(ln).addElements("E1", "E2", "E3").build();
		FacetNode facetNode2 = FacetNodeBuilder.create(ln).addAttributes("A1", "A2", "A3").build();
		FacetNode facetNode3 = FacetNodeBuilder.create(ln).addIndicators("I1", "I2").addAliases("Alias1").build();
		assertTrue("Built node 1 must not be null.", facetNode1 != null);
		assertTrue("Built node 2 must not be null.", facetNode2 != null);
		assertTrue("Built node 3 must not be null.", facetNode3 != null);
	}

	@Test
	public void inheritanceTests() {
		// Given a BO that extends another BO so that there are inherited children
		ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
		BusinessObjectNode baseBO = ml.addBusinessObjectToLibrary(ln, "BaseBO");
		CustomFacetNode c1 = (CustomFacetNode) baseBO.addFacet("BaseC1", TLFacetType.CUSTOM);
		AttributeNode a1 = new AttributeNode(c1, "cAttr1");

		BusinessObjectNode extendedBO = ml.addBusinessObjectToLibrary_Empty(ln, "ExBO");
		new ElementNode(extendedBO.getSummaryFacet(), "ExEle");

		// When - objects are extended
		extendedBO.setExtension(baseBO);
		assertTrue("ExtendedBO extends BaseBO.", extendedBO.isExtendedBy(baseBO));

		// Then - there should be an inherited facet.
		assertTrue("Must have inherited child.", !extendedBO.getInheritedChildren().isEmpty());
		CustomFacetNode inheritedCustom = (CustomFacetNode) extendedBO.getCustomFacets(true).get(0);
		assertTrue("Must have inherited custom facet.", inheritedCustom != null);

		// Then - there should be inherited children in the facets.
		List<Node> inheritedKids = extendedBO.getSummaryFacet().getInheritedChildren();
		List<Node> kids = extendedBO.getSummaryFacet().getChildren();
		assertTrue("Extended BO summary must have properties.", !kids.isEmpty());
		assertTrue("Extended BO summary must have inherited properties.", !inheritedKids.isEmpty());

		// When - delete the attribute in c1 (base custom)
		inheritedKids = inheritedCustom.getInheritedChildren();
		kids = inheritedCustom.getChildren();
		c1.remove(a1);
		inheritedCustom.getChildren();
		// TODO - finish test
	}

	@Test
	public void copyFacetTest() {
		ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);

		// Given a facet with 8 mixed properties
		FacetNode facetNode1 = FacetNodeBuilder.create(ln).addElements("E1", "E2", "E3").addIndicators("I1", "I2")
				.addAttributes("A1", "A2", "A3").build();
		assertTrue("Starting facet must have 8 properties.", facetNode1.getChildren().size() == 8);

		// Given an standard facet and a VWA Attribute facet
		TLFacet tlf = new TLFacet();
		tlf.setFacetType(TLFacetType.SUMMARY);
		FacetNode fn = (FacetNode) NodeFactory.newComponentMember(null, tlf);
		VWA_Node vwa = ml.addVWA_ToLibrary(ln, "myVWA"); // vwa with one attr
		VWA_AttributeFacetNode an = (VWA_AttributeFacetNode) vwa.getAttributeFacet();

		// When copied
		fn.copyFacet(facetNode1); // FIXME - very slow
		an.copyFacet(facetNode1);

		// Then both should have 8 properties
		assertTrue("Facet must have 8 properties.", fn.getChildren().size() == 8);
		assertTrue("Attribute facet must have 9 properties.", an.getChildren().size() == 9);
	}

	// TODO
	// createPropertyTests()
	// getComponentType

	@Test
	public void renameableFacetTests() throws Exception {
		lf.loadTestGroupA(mc);
		for (LibraryNode lib : pc.getDefaultProject().getLibraries()) {
			lib.setEditable(true);
			assertTrue("Library must be editable.", lib.isEditable());
			ml.addChoice(lib, "choice1");
			classBasedTests(lib);
		}
	}

	private void classBasedTests(LibraryNode lib) {
		LOGGER.debug("Checking query facets in " + lib);
		for (Node n : lib.getDescendants()) {
			if (n instanceof ContextualFacetNode)
				checkFacet((ContextualFacetNode) n);
			else if (n instanceof SimpleFacetNode)
				checkFacet((SimpleFacetNode) n);
			else if (n instanceof OperationFacetNode)
				checkFacet((OperationFacetNode) n);
			else if (n instanceof ListFacetNode)
				checkFacet((ListFacetNode) n);
			else if (n instanceof OperationNode)
				checkFacet((OperationNode) n);
			else if (n instanceof VWA_AttributeFacetNode)
				checkFacet((VWA_AttributeFacetNode) n);
			else if (n instanceof RoleFacetNode)
				checkFacet((RoleFacetNode) n);
			else if (n instanceof FacetNode)
				checkBaseFacet((FacetNode) n);
		}
	}

	@Test
	public void otm16EnabledTests() {
		OTM16Upgrade.otm16Enabled = true;

		// Given libraries loaded from file that contain contextual facets
		// NOTE: load order is important if the compiler is to resolve contributors
		lf.loadFile_Facets1(defaultProject);
		lf.loadFile_Facets2(defaultProject);
		LibraryNode base = lf.loadFile_FacetBase(defaultProject);

		// Then libraries must have contextual facets and those facets must have owners.
		for (LibraryNode ln : defaultProject.getLibraries()) {
			List<ContextualFacetNode> facets = getContextualFacets(ln);
			assertTrue("Must have some contextual facets.", !facets.isEmpty());
			for (ContextualFacetNode cf : facets) {
				assertTrue("Must have owner.", cf.getOwningComponent() != null);
			}
		}

		// Then - check contributions to objects in the base library
		//
		// FacetTestBO must have 4 contextual facets. 2 local and 2 contributed
		checkFacetContents(base, "FacetTestBO", 2, 2);
		// extended BO must have 4 contextual facets as children and no inherited
		checkFacetContents(base, "ExtFacetTestBO", 2, 2);
		// FacetTestChoice must have 2 choice facets. One from base library and one local and one from lib1
		checkFacetContents(base, "FacetTestChoice", 1, 1);
		// extended choice must have 4 contextual facets as children and no inherited
		checkFacetContents(base, "ExtFacetTestChoice", 1, 1);

		// Then extensions must have inherited children
		for (Node n : base.getDescendants_LibraryMembers()) {
			if (n instanceof ExtensionOwner)
				if (((ExtensionOwner) n).getExtensionBase() != null) {
					// Contextual facets should all have inherited properties
					for (Node cf : n.getChildren())
						if (cf instanceof ContextualFacetNode) {
							List<?> moKids = ((FacetMO) cf.getModelObject()).getInheritedChildren();
							List<Node> iKids = cf.getInheritedChildren();
							LOGGER.debug(cf + " has " + moKids.size() + " inherited model kids and " + iKids.size()
									+ " inherited kids.");
							assertTrue("Must have inherited children.", !cf.getInheritedChildren().isEmpty());
						}
				}
		}
	}

	private List<ContextualFacetNode> getContextualFacets(Node container) {
		ArrayList<ContextualFacetNode> facets = new ArrayList<ContextualFacetNode>();
		for (Node n : container.getDescendants())
			if (n instanceof ContextualFacetNode)
				facets.add((ContextualFacetNode) n);
		return facets;
	}

	private void checkFacetContents(Node base, String targetName, int local, int contributed) {
		int localCnt = 0, contributedCnt = 0;
		int total = local + contributed;
		for (Node n : base.getDescendants_LibraryMembers())
			if (n.getName().equals(targetName)) {
				List<ContextualFacetNode> facets = getContextualFacets(n);
				assertTrue(targetName + " must have " + total + " contextual facets.", facets.size() == total);
				assertTrue(targetName + " must not have inherited children.", n.getInheritedChildren().isEmpty());
				for (ContextualFacetNode cf : facets)
					if (cf.isLocal())
						localCnt++;
					else
						contributedCnt++;
				assertTrue(targetName + " must have " + local + " local contextual facets.", localCnt == local);
				assertTrue(targetName + " must have " + contributed + " contributed facets.",
						contributedCnt == contributed);
			}
	}

	@Test
	public void otm16EnabledTests_Constructors() {
		OTM16Upgrade.otm16Enabled = true;
		// Given a versioned library
		ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
		LibraryNode ln_inChain = ml.createNewLibrary("http://www.test.com/test1c", "test1c", defaultProject);
		new LibraryChainNode(ln_inChain);
		ln_inChain.setEditable(true);
		assertTrue("Library must exist.", ln != null);
		assertTrue("Library must exist.", ln_inChain != null);

		// Create each of the contextual facets
		TLContextualFacet tlObj = new TLContextualFacet();
		ContextualFacetNode cf = null;
		ContextualFacetNode cfFactory = null;

		tlObj.setFacetType(TLFacetType.CUSTOM);
		cf = new CustomFacetNode(tlObj);
		cfFactory = NodeFactory.createFacet(tlObj);
		assertTrue("Must not be null.", cf != null);
		assertTrue("Must not be null.", cfFactory != null);

		tlObj.setFacetType(TLFacetType.QUERY);
		cf = new QueryFacetNode(tlObj);
		cfFactory = NodeFactory.createFacet(tlObj);
		assertTrue("Must not be null.", cf != null);
		assertTrue("Must not be null.", cfFactory != null);

		tlObj.setFacetType(TLFacetType.CHOICE);
		cf = new ChoiceFacetNode(tlObj);
		cfFactory = NodeFactory.createFacet(tlObj);
		assertTrue("Must not be null.", cf != null);
		assertTrue("Must not be null.", cfFactory != null);

		tlObj.setFacetType(TLFacetType.UPDATE);
		cf = new UpdateFacetNode(tlObj);
		cfFactory = NodeFactory.createFacet(tlObj);
		assertTrue("Must not be null.", cf != null);
		assertTrue("Must not be null.", cfFactory != null);
	}

	@Test
	public void roleFacetTests() throws Exception {
		String myNS = "http://local/junits";
		// ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
		ln = LibraryNodeBuilder.create("Example", myNS, "p", new Version(1, 1, 0)).build(defaultProject, pc);
		ln.setEditable(true);
		assertTrue("Library is minor version.", ln.isMinorVersion());

		CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "Core1");
		checkFacet(core.getRoleFacet());
		List<Node> inheritedKids = core.getRoleFacet().getInheritedChildren();
		// TODO - make sure minor version has inherited children
	}

	@Test
	public void repositoryTestsNeedToBeMoved() throws RepositoryException {
		String myNS = "http://local/junits";
		DefaultRepositoryController rc = (DefaultRepositoryController) mc.getRepositoryController();
		assertTrue("Repository controller must not be null.", rc != null);
		assertTrue("Local repository must not be null.", rc.getLocalRepository() != null);
		List<RepositoryNode> repos = rc.getAll();
		RepositoryNode localRepoNode = rc.getLocalRepository();
		LOGGER.debug("Repo namespace is ", rc.getLocalRepository().getNamespaceWithPrefix());
		Repository localRepo = localRepoNode.getRepository();
		List<String> repoRootNSs = localRepo.listRootNamespaces();
		List<String> repoNSs = localRepo.listAllNamespaces();
		List<String> repoBaseNSs = localRepo.listBaseNamespaces();
		LOGGER.debug("Repo Root namespaces: ", repoRootNSs);
		LOGGER.debug("Repo Base namespaces: ", repoBaseNSs);
		LOGGER.debug("Repo All namespaces: ", repoNSs);
		try {
			localRepo.createRootNamespace(myNS);
		} catch (Exception e) {
			LOGGER.debug("Error setting Repo Root namespaces: ", e.getLocalizedMessage());
		}
		LOGGER.debug("Repo Root namespaces: ", localRepo.listRootNamespaces());

		// Given - a library in the local repo namespace
		ln = ml.createNewLibrary(rc.getLocalRepository().getNamespace(), "test1r", defaultProject);
		assertTrue("Library must not be null.", ln != null);
		// When - managed
		List<LibraryChainNode> lcns = rc.manage(rc.getLocalRepository(), Collections.singletonList(ln));
		// Then
		assertTrue("There must be library chains.", !lcns.isEmpty());
	}

	public void checkFacet(ListFacetNode lf) {
		LOGGER.debug("Checking List Facet Node: " + lf);

		if (lf.isSimpleListFacet()) {
			LOGGER.debug("Simple List Facet Node");
			assertTrue(lf.getName().startsWith(lf.getOwningComponent().getName()));
			assertTrue(lf.getName().contains("Simple"));
			assertTrue(lf.getLabel().equals(ComponentNodeType.SIMPLE_LIST.getDescription()));
		}
		if (lf.isDetailListFacet()) {
			LOGGER.debug("Detail List Facet Node");
			assertTrue(lf.getName().startsWith(lf.getOwningComponent().getName()));
			assertTrue(lf.getName().contains("Detail"));
			assertTrue(lf.getLabel().equals(ComponentNodeType.DETAIL_LIST.getDescription()));
		}
		assertFalse("Must NOT be delete-able.", lf.isDeleteable());
		assertFalse("Must NOT be valid parent.", lf.isValidParentOf(PropertyNodeType.ATTRIBUTE));
		assertFalse("Must NOT be valid parent.", lf.isValidParentOf(PropertyNodeType.ELEMENT));
		assertFalse("Must NOT be valid parent.", lf.isValidParentOf(PropertyNodeType.INDICATOR));
	}

	public void checkFacet(SimpleFacetNode sf) {
		LOGGER.debug("Checking Simple Facet Node: " + sf);
		assertFalse("Must NOT be delete-able.", sf.isDeleteable());

		assertTrue(sf.getModelObject() instanceof SimpleFacetMO);
		assertTrue(sf.getTLModelObject() instanceof TLSimpleFacet);
		assertTrue(sf.getLibrary() == sf.getParent().getLibrary());

		if (sf.getOwningComponent() instanceof VWA_Node) {
			assertTrue("Simple facet must have 1 child.", sf.getChildren().size() == 1);
			Node sp = sf.getChildren().get(0);
			assertTrue(sp instanceof PropertyNode);
			assertTrue(sp.getModelObject() instanceof SimpleAttributeMO);
			assertTrue(sp.getType() != null);
			assertTrue(!sp.getType().getName().isEmpty());
			assertTrue(sp.getLibrary() == sf.getLibrary());
		}
	}

	public void checkFacet(VWA_AttributeFacetNode vf) {
		LOGGER.debug("Checking VWA Attribute Facet Node: " + vf);

		assertTrue("Must be valid parent of attribute.", vf.isValidParentOf(PropertyNodeType.ATTRIBUTE));
		assertTrue("Must be valid parent of indicator.", vf.isValidParentOf(PropertyNodeType.INDICATOR));
		assertTrue("Must be valid parent of id.", vf.isValidParentOf(PropertyNodeType.ID));

		assertFalse("Must NOT be delete-able.", vf.isDeleteable());
		assertFalse("Must NOT be type provider.", vf.isNamedEntity());
		assertFalse("Must NOT be assignable.", vf.isAssignable());
		assertFalse("Must NOT be valid parent of element.", vf.isValidParentOf(PropertyNodeType.ELEMENT));
		assertFalse("Must NOT be valid parent of ID Reference.", vf.isValidParentOf(PropertyNodeType.ID_REFERENCE));
		assertFalse("Must NOT be valid parent of indicator element.",
				vf.isValidParentOf(PropertyNodeType.INDICATOR_ELEMENT));

		// Behaviors
		if (vf.getOwningComponent().isEditable()) {
			// Given - a list with a new property
			List<Node> properties = new ArrayList<Node>();
			PropertyNode p = new AttributeNode(new TLAttribute(), null);
			p.setName("attr1");
			properties.add(p);
			// When - list is added to attribute facet
			vf.addProperties(properties, false);
			// Then
			assertTrue("Must have new property as child.", vf.getChildren().contains(p));
		}
	}

	/**
	 * WARNING - ADDS Properties!!!
	 * 
	 * @param rf
	 */
	public void checkFacet(PropertyOwnerNode rf) {
		if (rf instanceof ChoiceFacetNode)
			checkFacet((ChoiceFacetNode) rf);
		else if (rf instanceof CustomFacetNode)
			checkFacet((CustomFacetNode) rf);
		else if (rf instanceof ListFacetNode)
			checkFacet((ListFacetNode) rf);
		else if (rf instanceof OperationNode)
			checkFacet((OperationNode) rf);
		else if (rf instanceof OperationFacetNode)
			checkFacet((OperationFacetNode) rf);
		else if (rf instanceof QueryFacetNode)
			checkFacet((QueryFacetNode) rf);
		else if (rf instanceof QueryFacetNode)
			checkFacet((QueryFacetNode) rf);
		else if (rf instanceof RoleFacetNode)
			checkFacet((RoleFacetNode) rf);
		else if (rf instanceof SimpleFacetNode)
			checkFacet((SimpleFacetNode) rf);
		else if (rf instanceof UpdateFacetNode)
			checkFacet((UpdateFacetNode) rf);
		else if (rf instanceof VWA_AttributeFacetNode)
			checkFacet((VWA_AttributeFacetNode) rf);
		else
			checkBaseFacet(rf);
	}

	public void checkFacet(OperationNode sf) {
		LOGGER.debug("Checking Operation Node: " + sf);
		// assertTrue("Must be delete-able.", sf.isDeleteable());
	}

	public void checkFacet(OperationFacetNode of) {
		LOGGER.debug("Checking Operation Facet Node: " + of);

		assertTrue("Must be delete-able.", of.isDeleteable());
		assertFalse("Must NOT be type provider.", of.isNamedEntity());
		assertFalse("Must NOT be assignable.", of.isAssignable());
	}

	public void checkBaseFacet(PropertyOwnerNode fn) {
		if (fn.isIDFacet())
			LOGGER.debug("Checking Id Facet:      " + fn);
		else if (fn.isSummaryFacet())
			LOGGER.debug("Checking Summary Facet: " + fn);
		else if (fn.isDetailFacet())
			LOGGER.debug("Checking Detail Facet:  " + fn);
		else
			LOGGER.debug("Checking Facet:         " + fn + " of type " + fn.getFacetType());

		// Check children
		for (Node property : fn.getChildren()) {
			if (property instanceof AliasNode)
				continue;
			assertTrue(property instanceof PropertyNode);
			assertTrue(property.getType() != null);
			assertTrue(property.getParent() == fn);
		}

		assertTrue("Must be valid parent to attributes.", fn.isValidParentOf(PropertyNodeType.ATTRIBUTE));
		assertTrue("Must be valid parent to elements.", fn.isValidParentOf(PropertyNodeType.ELEMENT));

		if (fn.isIDFacet() || fn.isDetailFacet() || fn.isSummaryFacet())
			assertFalse("Must NOT be delete-able.", fn.isDeleteable());

		// Behaviors
		if (fn.getOwningComponent().isEditable()) {
			AttributeNode attr = new AttributeNode(fn, "att1");
			ElementNode ele = new ElementNode(fn, "ele1");
			assertTrue("Must be able to add attributes.", attr.getParent() == fn);
			assertTrue("Must be able to add elements.", ele.getParent() == fn);

			// Create array of attribute and element properties to add
			List<Node> properties = new ArrayList<Node>();
			AttributeNode attr2 = new AttributeNode(new TLAttribute(), null);
			ElementNode ele2 = new ElementNode(new TLProperty(), null);
			attr2.setName("att2");
			ele2.setName("ele2");
			properties.add(attr2);
			properties.add(ele2);

			// Then add using facet method
			fn.addProperties(properties, false);
			assertTrue("Must have new property as child.", fn.getChildren().contains(attr2));
			assertTrue("Must have new property as child.", fn.getChildren().contains(ele2));

			// Then remove them
			attr.delete();
			ele.delete();
			attr2.delete();
			ele2.delete();
		}
	}

	public void checkFacet(QueryFacetNode qn) {
		LOGGER.debug("Checking Query Facet: " + qn);
		checkBaseFacet(qn);
		if (qn.getOwningComponent().isEditable())
			assertTrue("Must be delete-able.", qn.isDeleteable());
	}

	public void checkFacet(ContextualFacetNode rf) {
		LOGGER.debug("Checking Contextual Facet: " + rf);
		checkBaseFacet(rf);

		final String NEWNAME = "myName";

		// final String NEWCONTEXT = "myContext"; // must be ignored
		// setContext()
		// String dc = rf.getLibrary().getDefaultContextId();
		// String fc = rf.getTLModelObject().getContext();
		// // assertTrue("Initial context must be default context.",
		// // rf.getLibrary().getDefaultContextId().equals(((TLFacet) rf.getTLModelObject()).getContext()));
		// rf.setContext(NEWCONTEXT); // ignored!
		// fc = rf.getTLModelObject().getContext();
		// assertTrue("Context must be set to default.",
		// rf.getLibrary().getDefaultContextId().equals(rf.getTLModelObject().getContext()));

		// setName()
		//
		assertTrue("Must be renamable.", rf.isRenameable());
		rf.setName(NEWNAME);
		String n = rf.getName();
		assertTrue("Facet must contain new name.",
				rf.getName().contains(NodeNameUtils.fixContextualFacetName(rf, NEWNAME)));

		// Inherited statements
		//
		assertTrue("Must be assignable.", rf.isAssignable());
		assertTrue("Must be assignable to complex.", rf.isComplexAssignable());
		assertTrue("Must be type provider.", rf.isNamedEntity());
		assertTrue("Must be valid parent to attributes.", rf.isValidParentOf(PropertyNodeType.ATTRIBUTE));
		assertTrue("Must be valid parent to elements.", rf.isValidParentOf(PropertyNodeType.ELEMENT));

		assertFalse("Must NOT be assignable to element ref", rf.isAssignableToElementRef());
		assertFalse("Must NOT be assignable to simple.", rf.isAssignableToSimple());
		assertFalse("Must NOT be assignable to simple.", rf.isSimpleAssignable());
		assertFalse("Must NOT be assignable to VWA.", rf.isAssignableToVWA());
		assertFalse("Must NOT be default facet.", rf.isDefaultFacet());
		assertFalse("Must NOT be named type.", rf.isNamedType());

		// Behaviors
		//
		AttributeNode attr = new AttributeNode(rf, "att1");
		ElementNode ele = new ElementNode(rf, "ele1");
		assertTrue("Must be able to add attributes.", attr.getParent() == rf);
		assertTrue("Must be able to add elements.", ele.getParent() == rf);

		if (rf instanceof QueryFacetNode)
			checkFacet((QueryFacetNode) rf);
		else if (rf instanceof CustomFacetNode)
			checkFacet((CustomFacetNode) rf);
		else if (rf instanceof UpdateFacetNode)
			checkFacet((UpdateFacetNode) rf);
		else if (rf instanceof ChoiceFacetNode)
			checkFacet((ChoiceFacetNode) rf);
	}

	public void checkFacet(UpdateFacetNode qn) {
		LOGGER.debug("Checking Update Contextual Facet: " + qn);
		assertTrue("Must be delete-able.", qn.isDeleteable());
	}

	public void checkFacet(CustomFacetNode qn) {
		LOGGER.debug("Checking Custom Facet: " + qn);
		checkBaseFacet(qn);
		if (qn.getOwningComponent().isEditable())
			assertTrue("Must be delete-able.", qn.isDeleteable());
	}

	public void checkFacet(ChoiceFacetNode qn) {
		LOGGER.debug("Checking Facet: " + qn);
		assertTrue("Must be delete-able.", qn.isDeleteable());
		checkBaseFacet(qn);
	}

	public void checkFacet(RoleFacetNode roleFacetNode) {
		LOGGER.debug("Checking Facet: " + roleFacetNode);

		assertTrue("Must be role facet.", roleFacetNode instanceof RoleFacetNode);
		// assertTrue("Must be delete-able.", roleFacet.isDeleteable());
		assertTrue("Must be assignable.", roleFacetNode.isAssignable());
		assertTrue("Must be assignable to complex.", roleFacetNode.isComplexAssignable());
		assertTrue("Must be type provider.", roleFacetNode.isNamedEntity());
		assertTrue("Must be valid parent to roles.", roleFacetNode.isValidParentOf(PropertyNodeType.ROLE));
		// FIXME - assertTrue("Must be assignable to VWA.", roleFacet.isAssignableToVWA());

		assertFalse("Must NOT be assignable to simple.", roleFacetNode.isAssignableToSimple());
		assertFalse("Must NOT be assignable to simple.", roleFacetNode.isSimpleAssignable());
		assertFalse("Must NOT be valid parent to attributes.",
				roleFacetNode.isValidParentOf(PropertyNodeType.ATTRIBUTE));
		assertFalse("Must NOT be valid parent to elements.", roleFacetNode.isValidParentOf(PropertyNodeType.ELEMENT));
		assertFalse("Must NOT be renamable.", roleFacetNode.isRenameable());

		assertFalse("Must NOT be assignable to element ref", roleFacetNode.isAssignableToElementRef());
		assertFalse("Must NOT be default facet.", roleFacetNode.isDefaultFacet());
		assertFalse("Must NOT be named type.", roleFacetNode.isNamedType());

		// Behaviors
		RoleNode role = new RoleNode((RoleFacetNode) roleFacetNode, "newRole1");
		assertTrue("Must be able to add roles.", role.getParent() == roleFacetNode);
		assertTrue("Must be able to add child.", roleFacetNode.getChildren().contains(role));
	}
}
