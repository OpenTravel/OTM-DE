/*
 * Copyright (c) 2013, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package org.opentravel.schemas.schemacompiler;

import java.util.Enumeration;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * Activator that ensures the SPI-Fly bundle has been started prior to accessing OTM
 * schema compiler classes from this bundle.
 */
public class SchemaCompilerActivator implements BundleActivator {
	
	private static BundleContext context;
	
	/**
	 * Returns the singleton instance of this bundle's context once the service has been started.
	 * 
	 * @return BundleContext
	 */
	public static BundleContext getContext() {
		return context;
	}
	
	/**
	 * Ensures that the SPI-Fly bundle and all 'SPI-Provider' bundles are started before
	 * allowing this consumer bundle to start.  This is important because SPI-Fly must register
	 * all of the provider bundles before any service lookups are performed by the consumer.
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		startProviderBundles( bundleContext );
		SchemaCompilerActivator.context = bundleContext;
	}
	
	/**
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		SchemaCompilerActivator.context = null;
	}
	
	/**
	 * Searches the list of registered bundles for an 'SPI-Provider' header.  Any provider
	 * bundles that are identified are started by this method before returning.
	 * 
	 * @throws BundleException  thrown if an exception occurs while attempting to start one of the provider bundles
	 */
	private void startProviderBundles(BundleContext context) throws BundleException {
		for (Bundle bundle : context.getBundles()) {
			Enumeration<String> bundleHeaders = bundle.getHeaders().keys();
			boolean isProvider = false;
			
			if (bundle == context.getBundle()) {
				continue; // Skip the provider if it is for this local bundle
			}
			while (!isProvider && bundleHeaders.hasMoreElements()) {
				String header = bundleHeaders.nextElement();
				isProvider = (header != null) && header.equals("SPI-Provider");
			}
			if (isProvider) {
				startDependentBundle( context, bundle.getSymbolicName() );
			}
		}
	}
	
	/**
	 * Starts the bundle with the indicated symbolic name.
	 * 
	 * @param context  the context to use for OSGi bundle lookups
	 * @param symbolicName  the symbolic name of the bundle to start
	 * @throws BundleException  thrown if an exception occurs while attempting to start the bundle
	 */
	private void startDependentBundle(BundleContext context, String symbolicName) throws BundleException {
		for (Bundle bundle : context.getBundles()) {
			if (bundle.getSymbolicName().equals(symbolicName)) {
				bundle.start();
				break;
			}
		}
	}
	
}
