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
package org.opentravel.schemas.testUtils;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Assert;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFinding;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.ChoiceObjectNode;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.EnumerationClosedNode;
import org.opentravel.schemas.node.EnumerationOpenNode;
import org.opentravel.schemas.node.ExtensionPointNode;
import org.opentravel.schemas.node.FacetNode;
import org.opentravel.schemas.node.LibraryChainNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.SimpleTypeNode;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.interfaces.SimpleComponentInterface;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.IndicatorNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a mock library in the runtime-OT2Editor.product directory. Is added to the passed project.
 * 
 * @author Dave Hollander
 * 
 */
public class MockLibrary {
	static final Logger LOGGER = LoggerFactory.getLogger(MockLibrary.class);

	/**
	 * Create an unmanaged, editable library with one business object.
	 */
	public LibraryNode createNewLibrary(String ns, String name, ProjectNode parent) {
		LibraryNode ln = createNewLibrary_Empty(ns, name, parent);
		addBusinessObjectToLibrary(ln, name + "InitialBO");
		Assert.assertEquals(1, ln.getDescendants_NamedTypes().size());
		return ln;
	}

	/**
	 * Create an unmanaged, editable library.
	 */
	public LibraryNode createNewLibrary_Empty(String ns, String name, ProjectNode parent) {
		TLLibrary tllib = new TLLibrary();
		tllib.setName(name);
		tllib.setStatus(TLLibraryStatus.DRAFT);
		// causes compiler errors - tllib.setNamespaceAndVersion(ns, "0.0.0");
		tllib.setNamespaceAndVersion(ns, "1.0.0");
		tllib.setPrefix("nsPrefix");
		// LOGGER.debug("Set new library namespace to: " + tllib.getPrefix() + ":"
		// + tllib.getNamespace());

		String testPath;
		try {
			testPath = createTempFile(name + "-Test", ".otm");
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		URL testURL = URLUtils.toURL(new File(testPath));
		tllib.setLibraryUrl(testURL);
		LibraryNode ln = new LibraryNode(tllib, parent);
		ln.setEditable(true); // override ns policy

		// Has to be saved to be used in a project. Is not editable yet, so
		// can't use lib controller
		try {
			new LibraryModelSaver().saveLibrary(tllib);
		} catch (LibrarySaveException e) {
			LOGGER.debug("Error Saving: ", e);
		}

		ValidationFindings findings = ln.validate();
		boolean valid = findings.count(FindingType.ERROR) == 0 ? true : false;
		if (!valid)
			printFindings(findings);

		Assert.assertTrue(valid);
		return ln;
	}

	/**
	 * Create new library as a managed library in a chain.
	 */
	public LibraryChainNode createNewManagedLibrary(String ns, String name, ProjectNode parent) {
		LibraryNode ln = createNewLibrary(ns, name, parent);
		LibraryChainNode lcn = new LibraryChainNode(ln);
		lcn.getHead().setEditable(true);
		return lcn;
	}

	/**
	 * Create new library as a managed library in a chain.
	 */
	public LibraryChainNode createNewManagedLibrary_Empty(String ns, String name, ProjectNode parent) {
		LibraryNode ln = createNewLibrary_Empty(ns, name, parent);
		LibraryChainNode lcn = new LibraryChainNode(ln);
		lcn.getHead().setEditable(true);
		return lcn;
	}

	/**
	 * Create new library as a managed library in a chain. While not managed in repository, Library will be in the
	 * namespace of the project so it will be forced editable.
	 */
	public LibraryChainNode createNewManagedLibrary(String name, ProjectNode parent) {
		LibraryNode ln = createNewLibrary(parent.getNamespace(), name, parent);
		Assert.assertTrue(ln.isInProjectNS());
		LibraryChainNode lcn = new LibraryChainNode(ln);
		// not needed -- ln.setEditable(true); // override ns policy for chains
		Assert.assertTrue(ln.isEditable());
		return lcn;
	}

	public static void printDescendants_NamedTypes(Node ln) {
		String names = "printDescendants_NamedTypes: ";
		for (Node n : ln.getDescendants_NamedTypes()) {
			names += n.getName() + " ";
			// if (n.getName().equals("co2"))
			// LOGGER.debug("Here");
		}
		LOGGER.debug(names);
	}

	public static void printDescendants(Node ln) {
		String names = "printDescendants: ";
		for (Node n : ln.getDescendants()) {
			names += n.getName() + " ";
		}
		LOGGER.debug(names);
	}

	public static void printFindings(ValidationFindings findings) {
		for (ValidationFinding finding : findings.getAllFindingsAsList()) {
			LOGGER.debug("FINDING: " + finding.getFormattedMessage(FindingMessageFormat.IDENTIFIED_FORMAT));
		}
	}

	public static String createTempFile(String name, String suffix) throws IOException {
		final File tempDir = File.createTempFile("temp-otm-" + name, Long.toString(System.nanoTime()));

		if (!(tempDir.delete())) {
			throw new IOException("Could not delete temp file: " + tempDir.getAbsolutePath());
		}

		if (!(tempDir.mkdir())) {
			throw new IOException("Could not create temp directory: " + tempDir.getAbsolutePath());
		}
		// Because the tempDir contains the *.bak file the deleteOnExit will not work.
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (tempDir.exists()) {
					deleteContents(tempDir);
				}
			}
		});
		tempDir.deleteOnExit();
		File f = File.createTempFile(name, suffix, tempDir);
		f.deleteOnExit();
		return f.getPath();
	}

	public static void deleteContents(File fileOrFolder) {
		if (fileOrFolder.isDirectory()) {
			for (File folderMember : fileOrFolder.listFiles()) {
				deleteContents(folderMember);
			}
		}
		fileOrFolder.delete();
	}

	/**
	 * Add one of each object type to the library. Does <b>not</b> create extension points.
	 * 
	 * @param ln
	 */
	public int addOneOfEach(LibraryNode ln, String nameRoot) {
		LOGGER.debug("Adding one of each object type to " + ln + " with name root of " + nameRoot);
		int initialCount = ln.getDescendants_NamedTypes().size();
		addBusinessObjectToLibrary(ln, nameRoot + "BO");
		addClosedEnumToLibrary(ln, nameRoot + "CE");
		addCoreObjectToLibrary(ln, nameRoot + "CO");
		addOpenEnumToLibrary(ln, nameRoot + "OE");
		addSimpleTypeToLibrary(ln, nameRoot + "S");
		addVWA_ToLibrary(ln, nameRoot + "VWA");

		int finalCount = ln.getDescendants_NamedTypes().size();
		if (ln.isEditable())
			Assert.assertEquals(6 + initialCount, finalCount);
		else
			Assert.assertEquals(initialCount, finalCount);
		return finalCount;
	}

	/**
	 * @return new business object or null if library is not editable
	 */
	public BusinessObjectNode addBusinessObjectToLibrary_Empty(LibraryNode ln, String name) {
		if (name.isEmpty())
			name = "TestBO";

		BusinessObjectNode newNode = (BusinessObjectNode) NodeFactory.newComponent(new TLBusinessObject());
		newNode.setName(name);
		ln.addMember(newNode);
		newNode.setExtensible(true);
		return ln.isEditable() ? newNode : null;
	}

	/**
	 * @return new business object with 2 properties or null if library is not editable
	 */
	public BusinessObjectNode addBusinessObjectToLibrary(LibraryNode ln, String name) {
		BusinessObjectNode newNode = addBusinessObjectToLibrary_Empty(ln, name);
		if (newNode == null)
			return null;

		TypeProvider string = (TypeProvider) NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE);
		PropertyNode newProp = new ElementNode(newNode.getIDFacet(), "TestID");
		newProp.setAssignedType(string);

		newProp = new ElementNode(newNode.getSummaryFacet(), "TestSum");
		newProp.setAssignedType(string);
		return ln.isEditable() ? newNode : null;
	}

	/**
	 * Create a choice object with an alias, summary and two choice facets all with properties.
	 */
	public ChoiceObjectNode addChoice(LibraryNode ln, String name) {
		if (name.isEmpty())
			name = "ChoiceTest";
		Node string = NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE);

		ChoiceObjectNode choice = new ChoiceObjectNode(new TLChoiceObject());
		choice.setName(name);
		if (ln != null)
			ln.addMember(choice);
		choice.addAlias("CAlias");

		// Add properties to shared facet
		PropertyOwnerInterface shared = choice.getSharedFacet();
		new ElementNode(shared, "shared1");

		// Add two choice facets
		FacetNode f1 = choice.addFacet("c1");
		new ElementNode(f1, "c1p1");
		new AttributeNode(f1, "c1p2");
		new IndicatorNode(f1, "c1p3");

		FacetNode f2 = choice.addFacet("c2");
		new ElementNode(f2, "c2p1");
		new AttributeNode(f2, "c2p2");
		new IndicatorNode(f2, "c2p3");

		return choice;
	}

	/**
	 * Create several nodes that use each other as types
	 * 
	 * @param ln
	 * @return
	 */
	public Node addNestedTypes(LibraryNode ln) {
		BusinessObjectNode n1 = (BusinessObjectNode) NodeFactory.newComponent(new TLBusinessObject());
		n1.setName("N1");
		ln.addMember(n1);

		CoreObjectNode n2 = (CoreObjectNode) NodeFactory.newComponent(new TLCoreObject());
		n2.setName("N2");
		ln.addMember(n2);
		n2.setSimpleType((TypeProvider) NodeFinders.findNodeByName("int", ModelNode.XSD_NAMESPACE));
		PropertyNode n2Prop = new ElementNode(n2.getSummaryFacet(), n1.getName());
		n2Prop.setAssignedType(n1);

		CoreObjectNode n3 = (CoreObjectNode) NodeFactory.newComponent(new TLCoreObject());
		n3.setName("N3");
		ln.addMember(n3);
		n3.setSimpleType((TypeProvider) NodeFinders.findNodeByName("int", ModelNode.XSD_NAMESPACE));
		TypeUser n3PropA = new ElementNode(n3.getSummaryFacet(), n1.getName());
		n3PropA.setAssignedType(n1);
		PropertyNode n3PropB = new ElementNode(n3.getSummaryFacet(), n2.getName());
		n3PropB.setAssignedType((TypeProvider) n2.getSummaryFacet());

		TypeUser newProp = new ElementNode(n1.getIDFacet(), "TestID");
		newProp.setAssignedType((TypeProvider) NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE));
		newProp = new ElementNode(n1.getSummaryFacet(), n2.getName());
		newProp.setAssignedType(n2);
		newProp = new ElementNode(n1.getSummaryFacet(), "TestSumB");
		newProp.setAssignedType(n3.getSimpleFacet());
		return n1;
	}

	public CoreObjectNode addCoreObjectToLibrary_Empty(LibraryNode ln, String name) {
		if (name.isEmpty())
			name = "TestCore";
		CoreObjectNode newNode = (CoreObjectNode) NodeFactory.newComponent(new TLCoreObject());
		newNode.setName(name);
		newNode.setSimpleType((TypeProvider) NodeFinders.findNodeByName("int", ModelNode.XSD_NAMESPACE));
		ln.addMember(newNode);
		return newNode;
	}

	public CoreObjectNode addCoreObjectToLibrary(LibraryNode ln, String name) {
		CoreObjectNode newNode = addCoreObjectToLibrary_Empty(ln, name);
		TypeUser newProp = new ElementNode(newNode.getSummaryFacet(), "TestElement");
		newProp.setAssignedType((TypeProvider) NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE));
		return newNode;
	}

	/**
	 * Create a simple type node and assign type to xsd:int
	 */
	public SimpleTypeNode addSimpleTypeToLibrary(LibraryNode ln, String name) {
		if (name.isEmpty())
			name = "SimpleType";
		SimpleTypeNode sn = new SimpleTypeNode(new TLSimple());
		sn.setName(name);
		sn.setAssignedType((TypeProvider) NodeFinders.findNodeByName("int", ModelNode.XSD_NAMESPACE));
		ln.addMember(sn);
		return sn;
	}

	public VWA_Node addVWA_ToLibrary(LibraryNode ln, String name) {
		if (name.isEmpty())
			name = "TestVWA";
		VWA_Node newNode = (VWA_Node) NodeFactory.newComponent(new TLValueWithAttributes());
		newNode.setName(name);
		newNode.setSimpleType((TypeProvider) NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE));
		PropertyNode newProp = new AttributeNode(newNode.getAttributeFacet(), "TestAttribute");
		newProp.setAssignedType((TypeProvider) NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE));
		ln.addMember(newNode);
		return newNode;
	}

	public EnumerationOpenNode addOpenEnumToLibrary(LibraryNode ln, String name) {
		if (name.isEmpty())
			name = "TestOpen";
		EnumerationOpenNode newNode = (EnumerationOpenNode) NodeFactory.newComponent(new TLOpenEnumeration());
		newNode.setName(name);
		ln.addMember(newNode);
		newNode.addLiteral("Lit-01");
		return newNode;
	}

	public EnumerationClosedNode addClosedEnumToLibrary(LibraryNode ln, String name) {
		if (name.isEmpty())
			name = "TestClosed";
		EnumerationClosedNode newNode = (EnumerationClosedNode) NodeFactory.newComponent(new TLClosedEnumeration());
		newNode.setName(name);
		ln.addMember(newNode);
		newNode.addLiteral("Lit-C1");
		return newNode;
	}

	/**
	 * Create a simple type with assigned simple type (date)
	 */
	public SimpleComponentInterface createSimple(String name) {
		SimpleTypeNode n2 = new SimpleTypeNode(new TLSimple());
		n2.setName(name);
		((SimpleTypeNode) n2).setAssignedType(getSimpleTypeProvider());
		return n2;
	}

	/**
	 * Create a complex type with property assigned to simple type (date)
	 */
	public ComplexComponentInterface createComplex(String name) {
		CoreObjectNode n2 = new CoreObjectNode(new TLCoreObject());
		n2.setName(name);
		PropertyNode child = new ElementNode(n2.getSummaryFacet(), name + "Property");
		child.setAssignedType(getSimpleTypeProvider());
		return n2;
	}

	/**
	 * @return an XSD simple (date) assigned to type provider
	 */
	public TypeProvider getSimpleTypeProvider() {
		return (TypeProvider) NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
	}

	/**
	 * @param ln
	 *            - library to add extension point to
	 * @param eln
	 *            - library containing business object to extend. must be different.
	 * @return
	 */
	public ExtensionPointNode addEP(LibraryNode ln, LibraryNode eln) {
		FacetNode facet = null;
		for (Node d : eln.getDescendants_NamedTypes())
			if (d instanceof BusinessObjectNode)
				facet = ((BusinessObjectNode) d).getSummaryFacet();
		return addExtensionPoint(ln, facet);
	}

	/**
	 * 
	 * @param ln
	 * @param facet
	 *            - facet in different library to extend.
	 * @return
	 */
	public ExtensionPointNode addExtensionPoint(LibraryNode ln, FacetNode facet) {
		ExtensionPointNode ep = new ExtensionPointNode(new TLExtensionPointFacet());
		ln.addMember(ep);
		ep.setExtension(facet);
		return ep;
	}

	/**
	 * Traverse all navChildren and assure all libraries are built in.
	 */
	public void assertOnlyBuiltInLibraries(Node node) {
		for (Node n : node.getNavChildren()) {
			if (n instanceof LibraryNode)
				assertTrue("Remaining libraries must be built-in.", ((LibraryNode) n).isBuiltIn());
			assertOnlyBuiltInLibraries(n);
		}
	}

}
