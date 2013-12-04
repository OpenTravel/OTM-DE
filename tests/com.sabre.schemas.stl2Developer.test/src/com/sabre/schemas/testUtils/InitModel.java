/**
 * 
 */
package com.sabre.schemas.testUtils;

import org.junit.Assert;
import org.junit.Test;

import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.node.Node;

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
	public void testInitModel () throws Exception {
		InitModel tm = this;
		MainController mc = tm.getMainController();
		Assert.assertNotNull(mc);
		
		Assert.assertNotNull(mc.getLibraryController());
		Assert.assertNotNull(mc.getModelController());
		Assert.assertNotNull(mc.getProjectController());
		Assert.assertNotNull(mc.getContextController());

		Assert.assertNotNull(mc.getModelNode());
		Assert.assertTrue(537 < Node.getNodeCount());	
	}
	
}
