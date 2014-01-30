
package org.opentravel.schemas.navigation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.opentravel.schemas.node.Node;

public class GlobalSelectionProvider extends AbstractSourceProvider {

    public final static String NODES = "org.opentravel.schemas.selection.nodes";
    public final static String NAVIGATION_VIEW = "org.opentravel.schemas.selection.navigationView";
    public final static String TYPE_VIEW = "org.opentravel.schemas.selection.typeView";
    private List<Node> navigationNodes = Collections.emptyList();
    private List<Node> typeNodes = Collections.emptyList();

    @Override
    public void dispose() {

    }

    @Override
    public Map<String, Object> getCurrentState() {
        Map<String, Object> currentState = new HashMap<String, Object>(1);
        currentState.put(NAVIGATION_VIEW, navigationNodes);
        currentState.put(TYPE_VIEW, typeNodes);
        currentState.put(NODES, getGlobalSelectedNodes());
        return currentState;
    }

    /**
     * @return
     */
    private List<Node> getGlobalSelectedNodes() {
        if (typeNodes.isEmpty()) {
            return navigationNodes;
        }
        return typeNodes;
    }

    @Override
    public String[] getProvidedSourceNames() {
        return new String[] { NODES, NAVIGATION_VIEW, TYPE_VIEW };
    }

    public void navigationSelectionChange(List<Node> nodes) {
        navigationNodes = emptyIfNull(nodes);
        fireSourceChanged(ISources.WORKBENCH, NAVIGATION_VIEW, navigationNodes);
        fireSourceChanged(ISources.WORKBENCH, NODES, getGlobalSelectedNodes());
    }

    public void typeSelectionChange(List<Node> nodes) {
        typeNodes = emptyIfNull(nodes);
        fireSourceChanged(ISources.WORKBENCH, TYPE_VIEW, typeNodes);
        fireSourceChanged(ISources.WORKBENCH, NODES, getGlobalSelectedNodes());
    }

    public List<Node> getNavigationSelection() {
        return navigationNodes;
    }

    private <T> List<T> emptyIfNull(List<T> nodes) {
        if (nodes == null) {
            return Collections.emptyList();
        } else {
            return nodes;
        }

    }

}
