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

import java.net.Authenticator;

import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.opentravel.schemas.actions.proxy.EclipseAuthenticator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.opentravel.schemas.stl2Developer"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    private BundleListener coreUINetListener;

    /**
     * The constructor
     */
    public Activator() {
        coreUINetListener = new UiNetBundleListener();
    }

    /**
     * The org.eclispe.core.net (< 1.2.100.XXX) by default is setting first Authenticator found in
     * extension registry (defined using the extension point "org.eclipse.core.net.authenticator" ).
     * This listener responsibility is to make sure that our custom authenticator will always be
     * enabled.
     * 
     */
    class UiNetBundleListener implements BundleListener {

        final String PLUGIN_ID = "org.eclipse.core.net";

        @Override
        public void bundleChanged(BundleEvent event) {
            if (PLUGIN_ID.equals(event.getBundle().getSymbolicName())
                    && event.getBundle().getState() == Bundle.ACTIVE) {
                setGlobalAuthenticator();
            }
        }
    };

    private void setGlobalAuthenticator() {
        Authenticator.setDefault(new EclipseAuthenticator());
    }

    public IProxyService getProxyService() {
        ServiceReference<IProxyService> proxyRef = this.getBundle().getBundleContext()
                .getServiceReference(IProxyService.class);
        return getBundle().getBundleContext().getService(proxyRef);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext )
     */
    @Override
    public void start(final BundleContext context) throws Exception {
        context.addBundleListener(coreUINetListener);
        super.start(context);
        plugin = this;
        setGlobalAuthenticator();
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        plugin = null;
        context.removeBundleListener(coreUINetListener);
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in relative path
     * 
     * @param path
     *            the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(final String path) {
        final ImageDescriptor imageDescriptorFromPlugin = imageDescriptorFromPlugin(PLUGIN_ID, path);
        return imageDescriptorFromPlugin;
    }
}
