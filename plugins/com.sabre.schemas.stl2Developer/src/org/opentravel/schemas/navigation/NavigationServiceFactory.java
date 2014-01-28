package org.opentravel.schemas.navigation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.services.ISourceProviderService;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.navigation.DefaultNavigationService.IdSelectionChangedEvent;
import org.opentravel.schemas.navigation.INavigationService.ISelectionHandler;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.testers.IsNavaigationEnabled;
import org.opentravel.schemas.views.DocumentationView;
import org.opentravel.schemas.views.NavigatorView;
import org.opentravel.schemas.views.RepositoryView;
import org.opentravel.schemas.views.TypeView;
import org.opentravel.schemas.views.ValidationResultsView;
import org.opentravel.schemas.views.example.ExampleView;

public class NavigationServiceFactory extends AbstractServiceFactory {

    @Override
    public Object create(@SuppressWarnings("rawtypes") Class serviceInterface,
            IServiceLocator parentLocator, IServiceLocator locator) {
        if (!INavigationService.class.equals(serviceInterface)) {
            return null;
        }
        INavigationService navigationService = null;
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null && window.getActivePage() != null) {
            Map<String, Integer> partsId = initPartsId();
            navigationService = createNavigationController(partsId);
            addSelectionListeners(initPartsId().keySet(), navigationService, window.getActivePage());
        }
        return navigationService;
    }

    private Map<String, Integer> initPartsId() {
        Map<String, Integer> partsId = new HashMap<String, Integer>();
        partsId.put(NavigatorView.VIEW_ID, 0);
        // Those views depends on NavigatorView
        partsId.put(TypeView.VIEW_ID, 5);
        partsId.put(ExampleView.VIEW_ID, 5);
        partsId.put(ValidationResultsView.VIEW_ID, 5);
        partsId.put(RepositoryView.VIEW_ID, 5);
        // Documentation View depends on NavigatorView and TypeView
        partsId.put(DocumentationView.VIEW_ID, 10);
        return partsId;
    }

    private void removeSelectionListeners(Set<String> keySet, DefaultNavigationService nc,
            IWorkbenchPage page) {
        for (String id : keySet) {
            page.removeSelectionListener(id, nc);
        }
    }

    private void addSelectionListeners(Set<String> keySet, INavigationService navigationService,
            IWorkbenchPage page) {
        for (String id : keySet) {
            page.addSelectionListener(id, navigationService);
        }
    }

    private DefaultNavigationService createNavigationController(final Map<String, Integer> partsId) {

        final DefaultNavigationService nc = new DefaultNavigationService(partsId);

        // need to refresh commands after selection change
        // be default refresh is before selectionChanged notification
        nc.addChangeListeners(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                setGlobalSelection(window);
                refreshForwardBackwardTests(window);
            }

            private void setGlobalSelection(IWorkbenchWindow window) {
                ISourceProviderService service = (ISourceProviderService) window
                        .getService(ISourceProviderService.class);
                GlobalSelectionProvider sessionSourceProvider = (GlobalSelectionProvider) service
                        .getSourceProvider(GlobalSelectionProvider.NODES);
                MainController mc = OtmRegistry.getMainController();
                // TODO: filter base on what selection change.
                sessionSourceProvider.navigationSelectionChange(mc.getSelectedNodes_NavigatorView());
                sessionSourceProvider.typeSelectionChange(mc.getSelectedNodes_TypeView());
            }

            private void refreshForwardBackwardTests(IWorkbenchWindow window) {
                IEvaluationService evaluationService = (IEvaluationService) window
                        .getService(IEvaluationService.class);
                if (evaluationService != null) {
                    for (String prop : IsNavaigationEnabled.getPropertiesNames()) {
                        evaluationService.requestEvaluation(prop);
                    }
                }
            }
        });
        ISelectionHandler handler = new ISelectionHandler() {

            @Override
            public void setSelection(final TreeSet<IdSelectionChangedEvent> events) {
                Display.getDefault().syncExec(new Runnable() {

                    @Override
                    public void run() {
                        removeSelectionListeners(partsId.keySet(), nc, PlatformUI.getWorkbench()
                                .getActiveWorkbenchWindow().getActivePage());
                        for (IdSelectionChangedEvent event : events) {
                            event.getSelectionProvider().setSelection(event.getSelection());
                        }
                        addSelectionListeners(partsId.keySet(), nc, PlatformUI.getWorkbench()
                                .getActiveWorkbenchWindow().getActivePage());
                    }

                });

            }
        };
        nc.setSelectionHandler(handler);
        return nc;
    }
}
