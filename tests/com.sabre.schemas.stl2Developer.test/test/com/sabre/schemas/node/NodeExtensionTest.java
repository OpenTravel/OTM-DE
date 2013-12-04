package com.sabre.schemas.node;

import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Version;

import com.sabre.schemacompiler.saver.LibrarySaveException;
import com.sabre.schemas.utils.BaseProjectTest;
import com.sabre.schemas.utils.ComponentNodeBuilder;
import com.sabre.schemas.utils.LibraryNodeBuilder;

public class NodeExtensionTest extends BaseProjectTest {

    private LibraryNode lib;

    @Override
    protected void callBeforeEachTest() throws LibrarySaveException {
        lib = LibraryNodeBuilder.create("Example", "http://example.org", "p", new Version(1, 1, 1))
                .build(defaultProject, pc);

    }

    @Test
    public void isExtendedByTypeShouldReturnTrueForSuperType() {
        BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("name").get(lib);
        BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("Extend")
                .extend(boBase).get(lib);
        Assert.assertTrue(boExtend.isExtendedBy(boBase));
    }

    @Test
    public void isExtendedByShouldReturnFalseForSuperSuperType() {
        BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("name").get(lib);
        BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("Extend")
                .extend(boBase).get(lib);
        BusinessObjectNode boExtend2 = ComponentNodeBuilder.createBusinessObject("Extend2")
                .extend(boExtend).get(lib);
        Assert.assertFalse(boExtend2.isExtendedBy(boBase));
    }

    @Test
    public void isInstanceOfShouldReturnTrueForSuperType() {
        BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("name").get(lib);
        BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("Extend")
                .extend(boBase).get(lib);
        Assert.assertTrue(boExtend.isInstanceOf(boBase));
    }

    @Test
    public void isInstanceOfShouldReturnTrueForSuperSuperType() {
        BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("name").get(lib);
        BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("Extend")
                .extend(boBase).get(lib);
        BusinessObjectNode boExtend2 = ComponentNodeBuilder.createBusinessObject("Extend2")
                .extend(boExtend).get(lib);
        Assert.assertTrue(boExtend2.isInstanceOf(boBase));
    }

    @Test
    public void isInstanceOfShouldReturnForSuperSuperTypeExtendingSubclass() {
        BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("name").get(lib);
        BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("Extend")
                .extend(boBase).get(lib);
        BusinessObjectNode boExtend2 = ComponentNodeBuilder.createBusinessObject("Extend2")
                .extend(boExtend).get(lib);
        Assert.assertFalse(boBase.isInstanceOf(boExtend2));
    }

    @Test
    public void isInstanceOfShouldReturnFalseForSuperSuperType() {
        BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("name").get(lib);
        BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("Extend").get(lib);
        BusinessObjectNode boExtend2 = ComponentNodeBuilder.createBusinessObject("Extend2")
                .extend(boExtend).get(lib);
        Assert.assertFalse(boExtend2.isInstanceOf(boBase));
    }
}
