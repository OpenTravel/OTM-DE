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
package org.opentravel.schemas.stl2Developer.reposvc;

import java.io.File;
import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Logger;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.opentravel.schemacompiler.index.FreeTextSearchService;
import org.opentravel.schemacompiler.index.FreeTextSearchServiceFactory;
import org.opentravel.schemacompiler.providers.JAXBContextResolver;
import org.opentravel.schemacompiler.providers.RepositoryServiceExceptionMapper;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.RepositoryComponentFactory;
import org.opentravel.schemacompiler.repository.RepositoryContentResource;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.RepositoryServlet;
import org.opentravel.schemacompiler.repository.impl.RemoteRepositoryUtils;

/**
 * Encapsulates the configuration and run-time environment of an OTA2.0 repository that is launched as an embedded Jetty
 * application to facilitate testing.
 * 
 * @author S. Livezey
 */
public class JettyTestServer {

	private Server jettyServer;
	private final File repositorySnapshotLocation;
	private final File repositoryRuntimeLocation;
	private final File repositoryIndexLocation;
	private final int port;

	/**
	 * Constructor that specifies the configuration of the Jetty server to be used for testing.
	 * 
	 * @param port
	 *            the server port to which HTTP requests will be directed on the local host
	 * @param snapshotLocation
	 *            the folder location that contains an initial snapshot of the OTA2.0 repository
	 * @param ota2config
	 */
	public JettyTestServer(int port, File snapshotLocation, File tmpLocation, File ota2config) {
		this.repositorySnapshotLocation = snapshotLocation;
		this.repositoryRuntimeLocation = new File(tmpLocation.getAbsolutePath(), "/test-repository");
		this.repositoryIndexLocation = new File(repositoryRuntimeLocation.getParentFile(), "/search-index");
		this.port = port;

		if ((repositorySnapshotLocation != null) && !repositorySnapshotLocation.exists()) {
			throw new IllegalArgumentException(
					"Repository Snapshot Not Found: " + repositorySnapshotLocation.getAbsolutePath());
		}
		if (!repositoryRuntimeLocation.exists() && !repositoryRuntimeLocation.mkdirs()) {
			throw new IllegalArgumentException(
					"Unable to create run-rime repository folder: " + repositoryRuntimeLocation.getAbsolutePath());
		}
		System.setProperty("stl2Developer.test.repo.runtime-location", repositoryRuntimeLocation.getAbsolutePath());
		System.setProperty("stl2Developer.test.repo.search-index-location", repositoryIndexLocation.getAbsolutePath());

		System.setProperty("ota2.repository.config", ota2config.getAbsolutePath());
		// disable logging becouse of invalid slf4j version
		org.eclipse.jetty.util.log.Log.setLog(new NoLogging());
	}

	/**
	 * Initializes the run-time repository from the snapshot and launches the Jetty server.
	 * 
	 * @throws Exception
	 *             thrown if the server cannot be started
	 */
	public synchronized void start() throws Exception {
		if (jettyServer != null) {
			throw new IllegalStateException("The Jetty server is already running.");
		}
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);

		context.setContextPath("/ota2-repository-service");
		context.addServlet(new ServletHolder(new RepositoryServlet(new RepositoryApplication())), "/service/*");
		jettyServer = new Server(port);
		jettyServer.setHandler(context);
		initializeRuntimeRepository();
		jettyServer.start();

		initializeRepositoryServices();
		indexTestRepository();
	}

	/**
	 * Adds this test server instance to the given repository manager.
	 * 
	 * @param manager
	 *            the repository manager instance to configure
	 * @throws RepositoryException
	 *             thrown if the configuration settings cannot be modified
	 */
	public RemoteRepository configureRepositoryManager(RepositoryManager manager) throws RepositoryException {
		RemoteRepository testRepository = (RemoteRepository) manager.getRepository("test-repository");

		if (testRepository == null) {
			testRepository = manager.addRemoteRepository("http://localhost:" + port + "/ota2-repository-service");
		}
		return testRepository;
	}

	/**
	 * Shuts down the Jetty server.
	 * 
	 * @throws Exception
	 *             thrown if the server cannot be shut down
	 */
	public synchronized void stop() throws Exception {
		if (jettyServer == null) {
			throw new IllegalStateException("The Jetty server is not running.");
		}
		jettyServer.stop();
		jettyServer.join();
		jettyServer = null;
	}

	/**
	 * Pings the Jetty service with a meta-data request that forces the initialization of the repository web service.
	 */
	private void initializeRepositoryServices() throws Exception {
		new RemoteRepositoryUtils().getRepositoryMetadata("http://localhost:" + port + "/ota2-repository-service");
	}

	/**
	 * Indexes the contents of the server's test repository.
	 */
	private void indexTestRepository() throws Exception {
        FreeTextSearchServiceFactory.initializeSingleton( RepositoryComponentFactory.getDefault().getRepositoryManager() );
		FreeTextSearchService service = FreeTextSearchServiceFactory.getInstance();
		
		if (!service.isRunning()) {
			service.startService();
		}

		while (!service.isRunning()) {
			try {
				System.out.println("Waiting for Indexing Service startup...");
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		service.indexAllRepositoryItems();
	}

	/**
	 * Initializes the run-time OTA2.0 repository by deleting any existing files and copying all of the files in the
	 * snapshot folder location.
	 * 
	 * @throws IOException
	 *             thrown if the contents of the repository cannot be initialized
	 */
	public void initializeRuntimeRepository() throws IOException {
		RepositoryTestUtils.deleteContents(repositoryIndexLocation);
		RepositoryTestUtils.deleteContents(repositoryRuntimeLocation);

		if (repositorySnapshotLocation != null) {
			RepositoryTestUtils.copyContents(repositorySnapshotLocation, repositoryRuntimeLocation);
		}
	}

	public class RepositoryApplication extends ResourceConfig {

		public RepositoryApplication() throws ClassNotFoundException {
			registerClasses(RepositoryContentResource.class, JAXBContextResolver.class,
					RepositoryServiceExceptionMapper.RepositoryExceptionMapper.class,
					RepositoryServiceExceptionMapper.RepositorySecurityExceptionMapper.class,
					RepositoryServiceExceptionMapper.JAXBExceptionMapper.class,
					RepositoryServiceExceptionMapper.IOExceptionMapper.class, MultiPartFeature.class);
		}
	}

	public class NoLogging implements Logger {
		@Override
		public String getName() {
			return "no";
		}

		@Override
		public void warn(String msg, Object... args) {
		}

		@Override
		public void warn(Throwable thrown) {
		}

		@Override
		public void warn(String msg, Throwable thrown) {
		}

		@Override
		public void info(String msg, Object... args) {
		}

		@Override
		public void info(Throwable thrown) {
		}

		@Override
		public void info(String msg, Throwable thrown) {
		}

		@Override
		public boolean isDebugEnabled() {
			return false;
		}

		@Override
		public void setDebugEnabled(boolean enabled) {
		}

		@Override
		public void debug(String msg, Object... args) {
		}

		@Override
		public void debug(Throwable thrown) {
		}

		@Override
		public void debug(String msg, Throwable thrown) {
		}

		@Override
		public Logger getLogger(String name) {
			return this;
		}

		@Override
		public void ignore(Throwable ignored) {
		}
	}
}
