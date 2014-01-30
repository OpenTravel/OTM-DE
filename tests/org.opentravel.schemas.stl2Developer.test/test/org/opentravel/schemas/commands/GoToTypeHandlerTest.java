
package org.opentravel.schemas.commands;

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
import org.opentravel.schemas.commands.GoToTypeHandler;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.utils.MockUtils;
import org.opentravel.schemas.views.NavigatorView;
import org.opentravel.schemas.views.OtmAbstractView;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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
