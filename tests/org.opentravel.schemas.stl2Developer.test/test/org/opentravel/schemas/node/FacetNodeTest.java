
package org.opentravel.schemas.node;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.opentravel.schemas.modelObject.FacetMO;
import org.opentravel.schemas.modelObject.ModelObject;
import org.opentravel.schemas.modelObject.ModelObjectFactory;
import org.opentravel.schemas.node.FacetNode;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.Node;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ModelObjectFactory.class, VersionSchemeFactory.class })
public class FacetNodeTest {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void shouldSortModelObject() {
        FacetMO mockFacet = Mockito.mock(FacetMO.class);
        PowerMockito.mockStatic(ModelObjectFactory.class);
        PowerMockito.when(
                ModelObjectFactory.newModelObject(Matchers.any(TLModelElement.class),
                        Matchers.any(INode.class))).thenReturn((ModelObject) mockFacet);

        TLFacet facetMock = Mockito.mock(TLFacet.class);
        Mockito.when(facetMock.getValidationIdentity()).thenReturn("");
        FacetNode node = new FacetNode(facetMock);
        node.sort();

        Mockito.verify(mockFacet).sort();
    }

    @Test
    public void shouldOrderBeSynchronizedForElements() {
        TLFacet facet = new TLFacet();
        TLProperty e2 = createElement("e2");
        TLProperty e3 = createElement("e3");
        TLProperty e1 = createElement("e1");
        facet.addElement(e2);
        facet.addElement(e3);
        facet.addElement(e1);
        FacetNode node = new FacetNode(facet);
        node.sort();

        List<String> actualList = Lists.transform(node.getChildren(), new Function<Node, String>() {

            @Override
            public String apply(Node node) {
                return node.getName();
            }
        });
        List<String> expectedList = Lists.transform(Arrays.asList(e1, e2, e3),
                new Function<TLProperty, String>() {

                    @Override
                    public String apply(TLProperty node) {
                        return node.getName();
                    }
                });
        Assert.assertEquals(expectedList, actualList);
    }

    @Test
    public void shouldOrderBeSynchronizedForAttributes() {
        TLFacet facet = new TLFacet();
        TLAttribute a3 = createAttribute("a3");
        TLAttribute a2 = createAttribute("a2");
        TLAttribute a1 = createAttribute("a1");
        facet.addAttribute(a2);
        facet.addAttribute(a3);
        facet.addAttribute(a1);
        FacetNode node = new FacetNode(facet);
        node.sort();

        List<String> actualList = Lists.transform(node.getChildren(), new Function<Node, String>() {

            @Override
            public String apply(Node node) {
                return node.getName();
            }
        });
        List<String> expectedList = Lists.transform(Arrays.asList(a1, a2, a3),
                new Function<TLAttribute, String>() {

                    @Override
                    public String apply(TLAttribute node) {
                        return node.getName();
                    }
                });
        Assert.assertEquals(expectedList, actualList);
    }

    @Test
    public void shouldOrderBeSynchronizedForIndicators() throws VersionSchemeException {
        PowerMockito.mockStatic(VersionSchemeFactory.class);
        VersionSchemeFactory mock = Mockito.mock(VersionSchemeFactory.class);
        PowerMockito.when(VersionSchemeFactory.getInstance()).thenReturn(mock);
        Mockito.when(mock.getDefaultVersionScheme()).thenReturn("mockValue");
        Mockito.when(mock.getVersionScheme(Matchers.anyString())).thenReturn(
                Mockito.mock(VersionScheme.class));
        TLFacet facet = new TLFacet();
        TLIndicator i3 = createIndicator("i3");
        TLIndicator i2 = createIndicator("i2");
        TLIndicator i1 = createIndicator("i1");
        facet.addIndicator(i2);
        facet.addIndicator(i3);
        facet.addIndicator(i1);
        FacetNode node = new FacetNode(facet);
        node.sort();

        List<String> actualList = Lists.transform(node.getChildren(), new Function<Node, String>() {

            @Override
            public String apply(Node node) {
                return node.getName();
            }
        });
        List<String> expectedList = Lists.transform(Arrays.asList(i1, i2, i3),
                new Function<TLIndicator, String>() {

                    @Override
                    public String apply(TLIndicator node) {
                        return node.getName();
                    }
                });
        Assert.assertEquals(expectedList, actualList);
    }

    private TLProperty createElement(String name) {
        TLProperty tlProperty = new TLProperty();
        tlProperty.setName(name);
        return tlProperty;
    }

    private TLIndicator createIndicator(String name) {
        TLIndicator tlIndicator = new TLIndicator();
        tlIndicator.setName(name);
        return tlIndicator;
    }

    private TLAttribute createAttribute(String name) {
        TLAttribute tlAttribute = new TLAttribute();
        tlAttribute.setName(name);
        return tlAttribute;
    }

}
