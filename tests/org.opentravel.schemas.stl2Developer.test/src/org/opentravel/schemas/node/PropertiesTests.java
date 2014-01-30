/**
 * 
 */
package org.opentravel.schemas.node;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.EnumerationClosedNode;
import org.opentravel.schemas.node.EnumerationOpenNode;
import org.opentravel.schemas.node.FacetNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.RoleFacetNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.ElementReferenceNode;
import org.opentravel.schemas.node.properties.EnumLiteralNode;
import org.opentravel.schemas.node.properties.IndicatorElementNode;
import org.opentravel.schemas.node.properties.IndicatorNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.RoleNode;
import org.opentravel.schemas.testUtils.MockLibrary;

import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLRole;

/**
 * @author Dave Hollander
 * 
 */
public class PropertiesTests {
    ModelNode model = null;
    MockLibrary mockLibrary = null;
    LibraryNode ln = null;
    MainController mc;
    DefaultProjectController pc;
    ProjectNode defaultProject;

    // Node_Tests nt = new Node_Tests();
    // LoadFiles lf = new LoadFiles();
    // LibraryTests lt = new LibraryTests();

    @Before
    public void beforeEachTest() {
        mc = new MainController();
        mockLibrary = new MockLibrary();
        pc = (DefaultProjectController) mc.getProjectController();
        defaultProject = pc.getDefaultProject();
        ln = mockLibrary.createNewLibrary("http://sabre.com/test", "test", defaultProject);
    }

    @Test
    public void createElements() {
        BusinessObjectNode bo = mockLibrary.addBusinessObjectToLibrary(ln, "TestBO");
        FacetNode summary = bo.getSummaryFacet();
        Assert.assertNotNull(summary);
        Node aType = NodeFinders.findNodeByName("date", Node.XSD_NAMESPACE);
        PropertyNode pn = null;

        pn = new ElementNode(summary, "A");
        Assert.assertNotNull(pn);
        Assert.assertNotNull(pn.getLibrary());
        Assert.assertEquals(pn.getName(), "A");

        pn = new ElementNode(new TLProperty(), summary);
        Assert.assertNotNull(pn);
        pn.setName("b");
        Assert.assertNotNull(pn.getLibrary());
        Assert.assertTrue(pn instanceof ElementNode);
        Assert.assertEquals(pn.getName(), "B");
        Assert.assertFalse(pn.getLabel().isEmpty());
        pn.setName("AAA");
        Assert.assertEquals(pn.getName(), "AAA");
        Assert.assertNotNull(pn.getLibrary());

        pn = (PropertyNode) pn.createProperty(aType);
        Assert.assertNotNull(pn);

        Assert.assertEquals(4, summary.getChildren().size()); // addBO creates one
    }

    @Test
    public void createElementRefs() {
        BusinessObjectNode bo = mockLibrary.addBusinessObjectToLibrary(ln, "TestBO");
        FacetNode summary = bo.getSummaryFacet();
        Assert.assertNotNull(summary);
        Node aType = NodeFinders.findNodeByName("date", Node.XSD_NAMESPACE);
        PropertyNode pn = null;

        pn = new ElementReferenceNode(summary, "A");
        Assert.assertNotNull(pn);
        Assert.assertNotNull(pn.getLibrary());
        Assert.assertEquals("ARef", pn.getName());

        pn = new ElementReferenceNode(new TLProperty(), summary);
        Assert.assertNotNull(pn);
        pn.setName("b");
        Assert.assertNotNull(pn.getLibrary());
        Assert.assertEquals(NodeNameUtils.fixElementRefName("B"), pn.getName());
        Assert.assertFalse(pn.getLabel().isEmpty());
        Assert.assertNotNull(pn.getLibrary());

        pn = (PropertyNode) pn.createProperty(aType);
        Assert.assertNotNull(pn);

        Assert.assertEquals(4, summary.getChildren().size()); // addBO creates one
    }

    @Test
    public void createAttributes() {
        BusinessObjectNode bo = mockLibrary.addBusinessObjectToLibrary(ln, "TestBO");
        FacetNode summary = bo.getSummaryFacet();
        Assert.assertNotNull(summary);
        Node aType = NodeFinders.findNodeByName("date", Node.XSD_NAMESPACE);
        PropertyNode pn, pn1 = null;

        pn1 = new AttributeNode(summary, "A");
        Assert.assertNotNull(pn1);
        Assert.assertNotNull(pn1.getLibrary());
        Assert.assertEquals("a", pn1.getName());

        pn = new AttributeNode(new TLAttribute(), summary);
        Assert.assertNotNull(pn);
        pn.setName("b");
        Assert.assertNotNull(pn.getLibrary());
        Assert.assertEquals(NodeNameUtils.fixAttributeName("B"), pn.getName());
        Assert.assertFalse(pn.getLabel().isEmpty());
        Assert.assertNotNull(pn.getLibrary());

        pn = (PropertyNode) pn1.createProperty(aType);
        Assert.assertNotNull(pn);

        // TODO - test descriptions ~!!!
        // TODO - test examples ~!!!
        // TODO - test equivalents ~!!!

        Assert.assertEquals(4, summary.getChildren().size()); // addBO creates one
    }

    @Test
    public void createIndicatorElements() {
        BusinessObjectNode bo = mockLibrary.addBusinessObjectToLibrary(ln, "TestBO");
        FacetNode summary = bo.getSummaryFacet();
        Assert.assertNotNull(summary);
        Node aType = NodeFinders.findNodeByName("date", Node.XSD_NAMESPACE);
        PropertyNode pn, pn1 = null;

        pn1 = new IndicatorElementNode(summary, "A");
        Assert.assertNotNull(pn1);
        Assert.assertNotNull(pn1.getLibrary());
        Assert.assertEquals("AInd", pn1.getName());

        pn = new IndicatorElementNode(new TLIndicator(), summary);
        Assert.assertNotNull(pn);
        pn.setName("b");
        Assert.assertNotNull(pn.getLibrary());
        Assert.assertEquals(NodeNameUtils.fixIndicatorElementName("B"), pn.getName());
        Assert.assertNotNull(pn.getDefaultType());
        Assert.assertFalse(pn.getLabel().isEmpty());
        Assert.assertNotNull(pn.getLibrary());

        pn = (PropertyNode) pn1.createProperty(aType);
        Assert.assertNotNull(pn);

        // TODO - test descriptions ~!!!
        // TODO - test examples ~!!!
        // TODO - test equivalents ~!!!

        Assert.assertEquals(4, summary.getChildren().size()); // addBO creates one
    }

    @Test
    public void createIndicator() {
        BusinessObjectNode bo = mockLibrary.addBusinessObjectToLibrary(ln, "TestBO");
        FacetNode summary = bo.getSummaryFacet();
        Assert.assertNotNull(summary);
        Node aType = NodeFinders.findNodeByName("date", Node.XSD_NAMESPACE);
        PropertyNode pn, pn1 = null;

        pn1 = new IndicatorNode(summary, "A");
        Assert.assertNotNull(pn1);
        Assert.assertNotNull(pn1.getLibrary());
        Assert.assertEquals("aInd", pn1.getName());

        pn = new IndicatorElementNode(new TLIndicator(), summary);
        Assert.assertNotNull(pn);
        pn.setName("b");
        Assert.assertNotNull(pn.getLibrary());
        Assert.assertEquals(NodeNameUtils.fixIndicatorElementName("b"), pn.getName());
        Assert.assertNotNull(pn.getDefaultType());
        Assert.assertFalse(pn.getLabel().isEmpty());
        Assert.assertNotNull(pn.getLibrary());

        pn = (PropertyNode) pn1.createProperty(aType);
        Assert.assertNotNull(pn);

        // TODO - test descriptions ~!!!
        // TODO - test examples ~!!!
        // TODO - test equivalents ~!!!

        Assert.assertEquals(4, summary.getChildren().size()); // addBO creates one
    }

    @Test
    public void createEnumLiterals() {
        EnumerationOpenNode open = new EnumerationOpenNode(new TLOpenEnumeration());
        EnumerationClosedNode closed = new EnumerationClosedNode(new TLClosedEnumeration());
        ln.addMember(open);
        ln.addMember(closed);
        EnumLiteralNode lit, litA;
        Node aType = NodeFinders.findNodeByName("date", Node.XSD_NAMESPACE);

        litA = new EnumLiteralNode(open, "A");
        Assert.assertNotNull("litA");
        lit = new EnumLiteralNode(new TLEnumValue(), open);
        lit.setName("B");
        Assert.assertNotNull("litA");

        lit = (EnumLiteralNode) litA.createProperty(aType);
        Assert.assertEquals(NodeNameUtils.fixEnumerationValue(aType.getName()), lit.getName());
        Assert.assertEquals(3, open.getChildren().size());

    }

    @Test
    public void createRoles() {
        CoreObjectNode core = mockLibrary.addCoreObjectToLibrary(ln, "Core");
        Node aType = NodeFinders.findNodeByName("date", Node.XSD_NAMESPACE);
        RoleFacetNode roles = core.getRoleFacet();
        RoleNode rn1, rn = null;

        rn1 = new RoleNode(roles, "A");
        Assert.assertNotNull(rn1);
        Assert.assertEquals("A", rn1.getName());

        rn = new RoleNode(new TLRole(), roles);
        rn.setName("B");
        Assert.assertNotNull(rn);
        Assert.assertEquals("B", rn.getName());

        rn = (RoleNode) rn1.createProperty(aType);
        Assert.assertNotNull(rn);
        Assert.assertEquals("date", rn.getName());

        Assert.assertEquals(3, roles.getChildren().size());
    }

    @Test
    public void changeRoles() {
        ln.setEditable(true);
        BusinessObjectNode bo = mockLibrary.addBusinessObjectToLibrary(ln, "ct");
        FacetNode summary = bo.getSummaryFacet();
        Node aType = NodeFinders.findNodeByName("date", Node.XSD_NAMESPACE);
        PropertyNode pn, epn, apn, ipn, rpn, iepn = null;
        String eText = "Element";
        String aText = "attribute";
        String iText = "xInd";
        String rText = "CtRef";
        String ieText = "XInd";

        epn = new ElementNode(summary, eText);
        epn.setAssignedType(aType);
        apn = new AttributeNode(summary, aText);
        apn.setAssignedType(NodeFinders.findNodeByName("id", Node.XSD_NAMESPACE));
        ipn = new IndicatorNode(summary, iText);
        iepn = new IndicatorElementNode(summary, ieText);
        rpn = new ElementReferenceNode(summary, rText);
        boolean x = rpn.setAssignedType(bo);
        Assert.assertEquals(eText, epn.getName());
        Assert.assertEquals(aText, apn.getName());
        Assert.assertEquals(iText, ipn.getName());
        Assert.assertEquals(rText, rpn.getName());
        Assert.assertEquals(ieText, iepn.getName());

        epn.setDescription(eText);
        Assert.assertEquals(eText, epn.getDescription());

        apn.copyDetails(epn);
        Assert.assertEquals(eText, apn.getDescription());

        List<Node> kids = new ArrayList<Node>(summary.getChildren());
        for (Node n : kids) {
            if (n instanceof PropertyNode)
                changeToAll((PropertyNode) n);
        }
        // Do it again to assure the alternateRoles logic works
        kids = new ArrayList<Node>(summary.getChildren());
        for (Node n : kids) {
            if (n instanceof PropertyNode)
                changeToAll((PropertyNode) n);
        }
    }

    private void changeToAll(PropertyNode pn) {
        int children = pn.getParent().getChildren().size();
        pn = pn.changePropertyRole(PropertyNodeType.ATTRIBUTE);
        pn = pn.changePropertyRole(PropertyNodeType.INDICATOR);
        pn = pn.changePropertyRole(PropertyNodeType.INDICATOR_ELEMENT);
        pn = pn.changePropertyRole(PropertyNodeType.ID_REFERENCE);
        pn = pn.changePropertyRole(PropertyNodeType.ELEMENT);
        Assert.assertEquals(children, pn.getParent().getChildren().size());
    }
}
