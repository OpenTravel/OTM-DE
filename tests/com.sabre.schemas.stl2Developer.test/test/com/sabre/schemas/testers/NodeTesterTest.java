/*
 * Copyright (c) 2012, Sabre Inc.
 */
package com.sabre.schemas.testers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.sabre.schemas.node.ComponentNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.utils.MockUtils;

public class NodeTesterTest {

    private NodeTester tester;

    @Before
    public void beforeEachTest() {
        tester = new NodeTester();
    }

    @Test
    public void hasTypeShouldReturnTrueForNodeWithType() {
        ComponentNode parent = Mockito.mock(ComponentNode.class);
        ComponentNode type = Mockito.mock(ComponentNode.class);
        MockUtils.mockToReturnType(parent, type);
        boolean actualValue = tester.test(parent, NodeTester.HAS_TYPE, null, null);
        Assert.assertEquals(true, actualValue);
    }

    @Test
    public void hasTypeShouldReturnFalseForNodeWithoutType() {
        ComponentNode parent = Mockito.mock(ComponentNode.class);
        boolean actualValue = tester.test(parent, NodeTester.HAS_TYPE, null, null);
        Assert.assertEquals(false, actualValue);
    }

    @Test
    public void hasTypeshouldReturnFalseForNotNode() {
        boolean actualValue = tester.test(new Object(), NodeTester.HAS_TYPE, null, null);
        Assert.assertEquals(false, actualValue);
    }

    @Test
    public void isDeleteableShouldReturnFalseForNonDeleteableNode() {
        Node noDeleteable = Mockito.mock(Node.class);
        Mockito.when(noDeleteable.isDeleteable()).thenReturn(false);
        boolean actualValue = tester.test(noDeleteable, NodeTester.IS_DELETEABLE, null, null);
        Assert.assertEquals(false, actualValue);
    }

    @Test
    public void isDeleteableShouldReturnTrueForDeleteableNode() {
        Node noDeleteable = Mockito.mock(Node.class);
        Mockito.when(noDeleteable.isDeleteable()).thenReturn(true);
        boolean actualValue = tester.test(noDeleteable, NodeTester.IS_DELETEABLE, null, null);
        Assert.assertEquals(true, actualValue);
    }

    @Test
    public void isDeleteableShouldReturnFalseForNotNode() {
        boolean actualValue = tester.test(new Object(), NodeTester.IS_DELETEABLE, null, null);
        Assert.assertEquals(false, actualValue);
    }

}
