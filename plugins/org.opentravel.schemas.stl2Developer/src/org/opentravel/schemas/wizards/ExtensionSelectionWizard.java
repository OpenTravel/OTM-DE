
package org.opentravel.schemas.wizards;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.type.ExtensionTreeContentProvider;
import org.opentravel.schemas.trees.type.TypeTreeExtensionSelectionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wizard that allows the user to select an entity from which the current entity will extend.
 * 
 * 
 * @author S. Livezey
 */
// TODO - can this be combined with the TypeSelectionWizard?

public class ExtensionSelectionWizard extends Wizard implements IDoubleClickListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionSelectionWizard.class);

    private final Node curNode;
    private TypeSelectionPage selectionPage;
    private ExtensionInheritancePage inheritancePage;
    private WizardDialog dialog;

    public ExtensionSelectionWizard(final Node n) {
        // Same as type selection
        super();
        curNode = n;
    }

    @Override
    public void addPages() {
        // LOGGER.debug("add page - curNode is: "+curNode);
        selectionPage = new TypeSelectionPage("Extension Selection", "Select Extension",
                "Select an entity from which the selected type will extend.", null, curNode);
        selectionPage.addDoubleClickListener(this);
        selectionPage.setTypeSelectionFilter(new TypeTreeExtensionSelectionFilter(curNode
                .getModelObject()));
        selectionPage.setTypeTreeContentProvider(new ExtensionTreeContentProvider());
        addPage(selectionPage);

        // Additional page needed for extensions.
        if (!curNode.isExtensionPointFacet()) {
            inheritancePage = new ExtensionInheritancePage(
                    "Inherited Fields",
                    "Inherited Fields",
                    "Select the desired member of the inheritance hierarchy for each of the properties displayed below.",
                    curNode);
            selectionPage.setTypeSelectionListener(inheritancePage);
            addPage(inheritancePage);
        }
        // else
        // LOGGER.debug("curNode is an extension point facet ... so skip the inheritance page.");
    }

    @Override
    public boolean canFinish() {
        // Same as type selection
        return selectionPage.getSelectedNode() == null ? false : true;
    }

    @Override
    public boolean performFinish() {
        if (curNode != null) {
            curNode.setExtendsType(selectionPage.getSelectedNode());
            if (!curNode.isExtensionPointFacet())
                inheritancePage.doPerformFinish();
            OtmRegistry.getMainController().refresh();
        }
        return true;
    }

    public boolean postExtensionSelectionWizard(final Shell shell) {
        // Same as type selection
        if (curNode == null) {
            return false; // DO Nothing
        }

        dialog = new WizardDialog(shell, this);
        dialog.setPageSize(700, 600);
        dialog.create();
        dialog.open();

        return true;
    }

    @Override
    public void doubleClick(final DoubleClickEvent event) {
        // Same as type selection
        if (canFinish()) {
            performFinish();
            dialog.close();
        }
    }

}
