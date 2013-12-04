/*
 * Copyright (c) 2012, Sabre Inc.
 */
package com.sabre.schemas.commands;

import java.util.Collections;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.node.INode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.utils.MockUtils;
import com.sabre.schemas.views.NavigatorView;
import com.sabre.schemas.views.OtmAbstractView;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ HandlerUtil.class, OtmRegistry.class })
public class GoToTypeHandlerTest {

    private GoToTypeHandler handler;
    private MainController mainControllerMock;

    @Before
    public void beforeEachTest() {
        handler = new GoToTypeHandler();
        mainControllerMock = Mockito.mock(MainController.class);
        PowerMockito.mockStatic(OtmRegistry.class);
        Mockito.when(OtmRegistry.getMainController()).thenReturn(mainControllerMock);
        NavigatorView navigationMock = Mockito.mock(NavigatorView.class);
        Mockito.when(navigationMock.isReachable((Matchers.any(Node.class)))).thenReturn(true);
        Mockito.when(OtmRegistry.getNavigatorView()).thenReturn(navigationMock);
    }

    @Test
    public void shouldSelectTypeInNavigator() throws ExecutionException {
        Node typeNode = Mockito.mock(Node.class);
        Node selectedNode = Mockito.mock(Node.class);
        MockUtils.mockToReturnType(selectedNode, typeNode);

        PowerMockito.mockStatic(HandlerUtil.class);
        OtmAbstractView viewMock = Mockito.mock(OtmAbstractView.class);
        Mockito.when(viewMock.getSelectedNodes()).thenReturn(
                Collections.singletonList(selectedNode));
        Mockito.when(HandlerUtil.getActivePart((ExecutionEvent) Matchers.any())).thenReturn(
                viewMock);

        handler.execute(new ExecutionEvent());

        Mockito.verify(mainControllerMock).selectNavigatorNodeAndRefresh(typeNode);
    }

    @Test
    public void shouldDoNothingForNotOtmView() throws ExecutionException {
        PowerMockito.mockStatic(HandlerUtil.class);
        IWorkbenchPart viewMock = Mockito.mock(IWorkbenchPart.class);
        Mockito.when(HandlerUtil.getActivePart((ExecutionEvent) Matchers.any())).thenReturn(
                viewMock);

        handler.execute(new ExecutionEvent());

        Mockito.verify(mainControllerMock, Mockito.never()).selectNavigatorNodeAndRefresh(
                (INode) Matchers.any());
    }

}
