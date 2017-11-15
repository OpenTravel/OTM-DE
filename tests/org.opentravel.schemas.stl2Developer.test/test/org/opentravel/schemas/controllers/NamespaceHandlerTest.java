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
package org.opentravel.schemas.controllers;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.controllers.ProjectController;
import org.opentravel.schemas.controllers.RepositoryController;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.handlers.NamespaceHandler;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Pawel Jedruch
 * 
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(OtmRegistry.class)
@PowerMockIgnore({ "org.opentravel.schemacompiler.*" })
public class NamespaceHandlerTest {

    public static final String BASE_NAMESPACE = "http://www.example.com/test";
    private static NamespaceHandler handler;

    @BeforeClass
    public static void beforeTests() {
        handler = NamespaceHandler.getNamespaceHandler(Mockito.mock(ProjectNode.class));
    }

    @Before
    public void beforeEachTest() {
        PowerMockito.mockStatic(OtmRegistry.class);
        MainController mockMC = Mockito.mock(MainController.class);
        PowerMockito.when(OtmRegistry.getMainController()).thenReturn(mockMC);
        ProjectController mockPC = Mockito.mock(ProjectController.class);
        Mockito.when(mockMC.getProjectController()).thenReturn(mockPC);
        RepositoryController mockRC = Mockito.mock(RepositoryController.class);
        Mockito.when(mockMC.getRepositoryController()).thenReturn(mockRC);

        Mockito.when(mockPC.getSuggestedNamespaces()).thenReturn(new ArrayList<String>());
        Mockito.when(mockRC.getRootNamespaces()).thenReturn(
                Collections.singletonList(BASE_NAMESPACE));
    }

    @Test
    public void getNSBaseWithoutExtensionShouldRemoveVersion() {
        String ns = handler.createValidNamespace(BASE_NAMESPACE, "", "1.0.0");
        String baseName = handler.getNSBase(ns);
        Assert.assertEquals(BASE_NAMESPACE, baseName);
    }

    @Test
    public void getNSBaseWithExtensionShouldRemoveVersion() {
        String ns = handler.createValidNamespace(BASE_NAMESPACE, "Ext", "1.0.0");
        String baseName = handler.getNSBase(ns);
        Assert.assertEquals(BASE_NAMESPACE, baseName);
    }

    @Test
    public void getNSBaseForUnmanagedNamespace() {
        String ns = "unmanaged";
        String actualE = handler.getNSBase(ns);
        Assert.assertEquals(ns, actualE);
    }

    @Test
    public void getNSExtensionWithoutExtensionShouldReturnEmpty() {
        String ns = handler.createValidNamespace(BASE_NAMESPACE, "", "1.0.0");
        String ext = handler.getNSExtension(ns);
        Assert.assertEquals("", ext);
    }

    @Test
    public void getNSExtensionWithExtensionShouldReturnExtension() {
        String expected = "Ext";
        String ns = handler.createValidNamespace(BASE_NAMESPACE, expected, "1.0.0");
        String actualE = handler.getNSExtension(ns);
        Assert.assertEquals(expected, actualE);
    }

    @Test
    public void getNSExtensionWithExtensionShouldReturnExtension2() {
        String expected = "Ext/moreComplex";
        String ns = handler.createValidNamespace(BASE_NAMESPACE, expected, "1.0.0");
        String actualE = handler.getNSExtension(ns);
        Assert.assertEquals(expected, actualE);
    }

    @Test
    public void getNSExtensionForUnmanagedNamespace() {
        String ns = "unmanaged";
        String actualE = handler.getNSExtension(ns);
        Assert.assertEquals("", actualE);
    }
}
