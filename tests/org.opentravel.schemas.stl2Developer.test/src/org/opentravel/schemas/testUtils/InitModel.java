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
/**
 * 
 */
package org.opentravel.schemas.testUtils;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemas.controllers.MainController;

/**
 * @author Dave Hollander
 *
 */
public class InitModel {

	protected MainController mainController;

	/**
	 * @return the mainWindow
	 */
	public MainController getMainController() {
		return mainController;
	}

	public InitModel() {
		mainController = new MainController();
	}

	@Test
	public void testInitModel() throws Exception {
		InitModel tm = this;
		MainController mc = tm.getMainController();
		Assert.assertNotNull(mc);

		Assert.assertNotNull(mc.getLibraryController());
		Assert.assertNotNull(mc.getModelController());
		Assert.assertNotNull(mc.getProjectController());
		Assert.assertNotNull(mc.getContextController());

		Assert.assertNotNull(mc.getModelNode());
		// Assert.assertTrue(537 < Node.getNodeCount());
	}

}
