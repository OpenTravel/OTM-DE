package com.sabre.schemas.utils;

import org.mockito.Mockito;

import com.sabre.schemas.node.Node;
import com.sabre.schemas.types.Type;

public class MockUtils {

    public static void mockToReturnType(Node parent, Node type) {
        Type typeMock = Mockito.mock(Type.class);
        Mockito.when(typeMock.getTypeNode()).thenReturn(type);
        Mockito.when(parent.getTypeClass()).thenReturn(typeMock);
    }
}
