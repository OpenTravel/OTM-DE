/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.node;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.controllers.ProjectController;
import com.sabre.schemas.controllers.RepositoryController;
import com.sabre.schemas.stl2developer.OtmRegistry;

/**
 * @author Pawel Jedruch
 * 
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(OtmRegistry.class)
@PowerMockIgnore({ "com.sabre.schemacompiler.*" })
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
