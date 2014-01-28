package org.opentravel.schemas.stl2Developer.reposvc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

import com.sun.jersey.api.core.ScanningResourceConfig;
import com.sun.jersey.core.spi.scanning.Scanner;
import com.sun.jersey.core.spi.scanning.ScannerException;
import com.sun.jersey.core.spi.scanning.ScannerListener;
import com.sun.jersey.core.util.Closing;

public class OSGiPackageConfig extends ScanningResourceConfig {

    public OSGiPackageConfig(String[] packages, Bundle bundle) {
        init(new OSGiPackageNameScanner(packages, bundle));
    }

    class OSGiPackageNameScanner implements Scanner {

        private final Bundle bundle;
        private final String[] packages;

        public OSGiPackageNameScanner(String[] packages, Bundle bundle) {
            this.bundle = bundle;
            this.packages = packages;
        }

        @Override
        public void scan(ScannerListener cfl) throws ScannerException {
            for (String p : packages) {
                scanPackage(p, cfl);
            }
        }

        private void scanPackage(String packageName, ScannerListener cfl) {
            Set<String> urls = getClassesFromPackage(packageName);
            for (String url : urls) {
                if (cfl.onAccept(url)) {
                    processUrl(url, cfl);
                }
            }
        }

        private void processUrl(final String url, final ScannerListener cfl) {
            try {
                URL systemURL = FileLocator.resolve(new URL(url));
                new Closing(new BufferedInputStream(systemURL.openStream()))
                        .f(new Closing.Closure() {

                            @Override
                            public void f(final InputStream in) throws IOException {
                                cfl.onProcess(url, in);
                            }
                        });
            } catch (IOException ex) {
                throw new ScannerException("IO error when scanning bundle file: " + url, ex);
            }

        }

        private Set<String> getClassesFromPackage(String packageName) {
            packageName = packageName.replace('.', '/');
            Bundle[] bundles;
            if (bundle.getBundleContext() != null) {
                bundles = bundle.getBundleContext().getBundles();
            } else {
                bundles = new Bundle[] { bundle };
            }
            Set<String> urls = new LinkedHashSet<String>();
            for (Bundle bd : bundles) {
                BundleWiring wire = bd.adapt(BundleWiring.class);
                List<URL> paths = wire
                        .findEntries("/", "*.class", BundleWiring.FINDENTRIES_RECURSE);
                for (URL u : paths) {
                    String str = u.toString();
                    if (str.contains(packageName)) {
                        urls.add(str);
                    }
                }
            }
            return urls;
        }

    }
}
