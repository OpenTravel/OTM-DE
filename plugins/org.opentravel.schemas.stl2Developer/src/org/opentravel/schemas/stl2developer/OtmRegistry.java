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
package org.opentravel.schemas.stl2developer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.utils.RCPUtils;
import org.opentravel.schemas.views.ContextsView;
import org.opentravel.schemas.views.DocumentationView;
import org.opentravel.schemas.views.FacetView;
import org.opentravel.schemas.views.NavigatorView;
import org.opentravel.schemas.views.OtmView;
import org.opentravel.schemas.views.PropertiesView;
import org.opentravel.schemas.views.RepositoryView;
import org.opentravel.schemas.views.TypeView;
import org.opentravel.schemas.views.ValidationResultsView;
import org.opentravel.schemas.views.example.ExampleView;
import org.opentravel.schemas.views.example.ExampleXmlView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static access point for controllers and views.
 * 
 * @author Agnieszka Janowska
 * 
 */
public class OtmRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(OtmRegistry.class);

    private static MainController mainController;

    private static MainWindow mainWindow;
    private static ExampleView exampleView;
    private static ExampleXmlView exampleXmlView;
    private static ValidationResultsView validationResultsView;
    private static NavigatorView navigatorView;
    private static TypeView typeView;
    private static FacetView facetView;
    private static PropertiesView propertiesView;
    private static ContextsView contextsView;
    private static DocumentationView documentationView;
    private static RepositoryView repositoryView;

    /**
     * @return all active otmViews ( extension of eclipse view parts )
     */
    public static List<OtmView> getAllActiveViews() {
        List<OtmView> views = new ArrayList<OtmView>();
        if (exampleView != null)
            views.add(exampleView);
        if (exampleXmlView != null)
            views.add(exampleXmlView);
        if (validationResultsView != null)
            views.add(validationResultsView);
        if (navigatorView != null)
            views.add(navigatorView);
        if (typeView != null)
            views.add(typeView);
        if (contextsView != null)
            views.add(contextsView);
        if (documentationView != null)
            views.add(documentationView);
        return (views);
    }

    /**
     * @return the exampleXmlView
     */
    public static ExampleXmlView getExampleXmlView() {
        return exampleXmlView;
    }

    /**
     * @param exampleXmlView
     *            the exampleXmlView to set
     */
    public static void registerExampleXmlView(final ExampleXmlView exampleXmlView) {
        OtmRegistry.exampleXmlView = exampleXmlView;
        LOGGER.info("Registered ExampleXmlView");

    }

    /**
     * @return the mainWindow use mainWindow.hadDisplay() to check if it is "headless"
     */
    public static MainWindow getMainWindow() {
        // need main window created before workbench will create it.
        if (mainWindow == null) {
            mainWindow = new MainWindow();
        }
        return mainWindow;
    }

    /**
     * @param mainWindow
     *            - the mainWindow to set
     */
    public static void registerMainView(final MainWindow mainWindow) {
        if (OtmRegistry.mainWindow != null)
            LOGGER.debug("Registering ANOTHER main window.");
        OtmRegistry.mainWindow = mainWindow;
        LOGGER.info("Registered MainWindow");
    }

    /**
     * @return the exampleView
     */
    public static OtmView getExampleView() {
        return exampleView;
    }

    /**
     * @param exampleView
     *            the exampleView to set
     */
    public static void registerExampleView(final ExampleView exampleView) {
        OtmRegistry.exampleView = exampleView;
        LOGGER.info("Registered ExampleView");
    }

    /**
     * @return the validationResultsView
     */
    public static ValidationResultsView getValidationResultsView() {
        if (validationResultsView == null) {
            RCPUtils.findOrCreateView(ValidationResultsView.VIEW_ID);
        }
        return validationResultsView;
    }

    /**
     * @param validationResultsView
     *            the validationResultsView to set
     */
    public static void registerValidationResultsView(
            final ValidationResultsView validationResultsView) {
        OtmRegistry.validationResultsView = validationResultsView;
        LOGGER.info("Registered ValidationResultsView");
    }

    /**
     * @return the navigatorView
     */
    public static OtmView getNavigatorView() {
        if (navigatorView == null) {
            RCPUtils.findOrCreateView(NavigatorView.VIEW_ID);
        }
        return navigatorView;
    }

    /**
     * @param navigatorView
     *            the navigatorView to set
     */
    public static void registerNavigatorView(final NavigatorView navigatorView) {
        OtmRegistry.navigatorView = navigatorView;
        LOGGER.info("Registered NavigatorView");
    }

    /**
     * @return the typeView
     */
    public static OtmView getTypeView() {
        if (typeView == null) {
            RCPUtils.findOrCreateView(TypeView.VIEW_ID);
        }
        return typeView;
    }

    /**
     * @param typeView
     *            the typeView to set
     */
    public static void registerFacetView(final FacetView view) {
        OtmRegistry.facetView = view;
        LOGGER.info("Registered FacetView");
    }

    /**
     * @return the typeView
     */
    public static OtmView getFacetView() {
        return facetView;
    }

    /**
     * @param typeView
     *            the typeView to set
     */
    public static void registerPropertiesView(final PropertiesView view) {
        OtmRegistry.propertiesView = view;
        LOGGER.info("Registered PropertiesView");
    }

    /**
     * @return the PropertiesView
     */
    public static OtmView getPropertiesView() {
        return propertiesView;
    }

    /**
     * @param typeView
     *            the typeView to set
     */
    public static void registerTypeView(final TypeView typeView) {
        OtmRegistry.typeView = typeView;
        LOGGER.info("Registered TypeView");
    }

    /**
     * @return the contextsView
     */
    public static ContextsView getContextsView() {
        return contextsView;
    }

    /**
     * @param contextsView
     *            the contextsView to set
     */
    public static void registerContextsView(final ContextsView contextsView) {
        OtmRegistry.contextsView = contextsView;
        LOGGER.info("Registered ContextsView");
    }

    /**
     * @return the documentationView
     */
    public static DocumentationView getDocumentationView() {
        return documentationView;
    }

    /**
     * @param documentationView
     *            the documentationView to set
     */
    public static void registerDocumentationView(DocumentationView documentationView) {
        OtmRegistry.documentationView = documentationView;
        LOGGER.info("Registered DocumentationView");
    }

    /**
     * @return
     */
    public static Shell getActiveShell() {
        if (PlatformUI.getWorkbench() == null)
            return null;
        if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null)
            return null;
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    }

    public static void registerMainController(MainController mainController) {
        if (OtmRegistry.mainController != null)
            LOGGER.debug("Registering ANOTHER main controller.");
        OtmRegistry.mainController = mainController;
    }

    /**
     * @return the main controller
     */
    public static MainController getMainController() {
        if (OtmRegistry.mainController == null)
            OtmRegistry.mainController = new MainController();
        return mainController;
    }

    public static void registerRepositoryView(RepositoryView repositoryView) {
        OtmRegistry.repositoryView = repositoryView;
    }

    public static OtmView getRepositoryView() {
        if (repositoryView == null) {
            RCPUtils.findOrCreateView(RepositoryView.VIEW_ID);
        }
        return repositoryView;
    }
}
