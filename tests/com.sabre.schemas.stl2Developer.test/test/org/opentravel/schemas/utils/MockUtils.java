package org.opentravel.schemas.utils;

import org.mockito.Mockito;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.types.Type;

public class MockUtils {

    public static void mockToReturnType(Node parent, Node type) {
        Type typeMock = Mockito.mock(Type.class);
        Mockito.when(typeMock.getTypeNode()).thenReturn(type);
        Mockito.when(parent.getTypeClass()).thenReturn(typeMock);
    }
}
