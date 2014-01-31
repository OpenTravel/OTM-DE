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
package org.opentravel.schemas.testers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.testers.NodeTester;
import org.opentravel.schemas.utils.MockUtils;

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
