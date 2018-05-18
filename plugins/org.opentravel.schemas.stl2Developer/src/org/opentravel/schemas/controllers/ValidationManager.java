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
package org.opentravel.schemas.controllers;

import java.util.concurrent.Semaphore;

import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.compile.TLModelCompileValidator;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.typeProviders.ImpliedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validation Manager. Static methods to run validation from both UI and background threads. Validation is compute
 * intensive and dependent on having a stable TL Model.
 * <p>
 * A blocking semaphore is kept in this singleton allowing operations such as closing projects to block validation until
 * complete.
 * 
 * @author Dave Hollander
 * 
 */
public class ValidationManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(ValidationManager.class);

	// For this to work, it needs to be a singleton
	final static Semaphore semaphore = new Semaphore(1);

	/**
	 * Prevent validation from running until released.
	 */
	public static void block() {
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			LOGGER.error("Exception obtaining semaphore: ", e.getLocalizedMessage());
			e.printStackTrace();
		}
		// LOGGER.debug("Validation blocked.");
	}

	/**
	 * Release the lock and let validation run
	 */
	public static void unblock() {
		semaphore.release();
		// LOGGER.debug("Validation unblocked.");
	}

	/**
	 * Examine findings and return if errors/warnings were found. Allows error and warning tests on same set of
	 * findings.
	 * 
	 * @param findings
	 * @param type
	 * @return
	 */
	public static boolean isValid(ValidationFindings findings, FindingType type) {
		return findings != null && findings.count(type) == 0 ? true : false;
	}

	/**
	 * Validate the node using TL Model compiler validation.
	 * 
	 * @param node
	 * @return return true if there are no error findings
	 */
	public static boolean isValid(Node node) {
		if (node instanceof ImpliedNode)
			return true;
		if (node instanceof InheritedInterface)
			return true; // Out of context the compiler may flag invalid errors or warnings
		if (node == null || node.getLibrary() == null)
			return false;
		if (node.getLibrary().isBuiltIn())
			return true; // skip built in libraries and their content
		ValidationFindings findings = validate(node);
		if (findings == null)
			return true;
		return findings.count(FindingType.ERROR) == 0 ? true : false;
	}

	/**
	 * Validate the node using TL Model compiler validation.
	 * 
	 * @param node
	 * @return return true if there are no warning or error findings
	 */
	public static boolean isValidNoWarnings(Node node) {
		if (node == null || node.getLibrary() == null)
			return false;
		if (node.getLibrary().isBuiltIn())
			return true; // skip built in libraries and their content
		ValidationFindings findings = validate(node);
		return findings != null && findings.count(FindingType.WARNING) == 0 ? true : false;
	}

	/**
	 * Validate the node using TL Model compiler validation. Only do deep dependencies validation on libraries.
	 * 
	 * @param node
	 * @return return findings
	 */
	public static ValidationFindings validate(Node node) {
		ValidationFindings findings = null;
		if (node == null || node.getLibrary() == null || node.isDeleted() || node instanceof ImpliedNode)
			return findings;
		if (node instanceof LibraryNavNode)
			node = ((LibraryNavNode) node).getLibrary();
		if (node instanceof LibraryChainNode)
			node = ((LibraryChainNode) node).getHead();
		// findings = validate(node.getTLModelObject(), true);
		findings = validate(node.getTLModelObject(), node instanceof LibraryNode);
		// LOGGER.debug("Validate ran on " + node + " " + node.getClass().getSimpleName() + " " + findings.count());
		return findings;
	}

	/**
	 * Validate the node using TL Model compiler validation. Acquire semaphore before validating.
	 * 
	 * @param TL
	 *            Model object
	 * @return return findings
	 */
	public static synchronized ValidationFindings validate(TLModelElement tlObj, boolean deep) {
		// block();
		ValidationFindings findings = null;
		try {
			findings = TLModelCompileValidator.validateModelElement(tlObj, deep);
		} catch (Exception e) {
			// LOGGER.debug("Validation threw error: " + e.getLocalizedMessage());
		}
		// finally {
		//// unblock();
		// }
		// logFindings(findings);
		return findings;
	}

	public static ValidationFindings validate(LibraryChainNode lcn) {
		ValidationFindings findings = new ValidationFindings();
		for (LibraryNode ln : lcn.getLibraries())
			findings.addAll(validate(ln));
		return findings;
	}

	public static ValidationFindings validate(TLExtensionPointFacet xpFacet) {
		return validate(xpFacet, false);
	}

	public static void logFindings(ValidationFindings findings) {
		if (findings == null)
			return;
		for (String f : findings.getValidationMessages(FindingType.ERROR, FindingMessageFormat.MESSAGE_ONLY_FORMAT))
			LOGGER.debug("Finding: " + f);
	}

}
