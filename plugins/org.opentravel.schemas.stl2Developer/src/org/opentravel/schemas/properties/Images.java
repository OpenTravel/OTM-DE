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
package org.opentravel.schemas.properties;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.opentravel.schemas.stl2developer.OtmRegistry;

public class Images {
	private static ImageRegistry image_registry;

	public static final String builtInLib = "builtInLib";
	public static final String library = "library";
	public static final String libraryChain = "libraryChain";
	public static final String aggregateFolder = "aggregateFolder";
	public static final String Error = "Error";
	public static final String ErrorDecoration = "ErrorDecoration";
	public static final String WarningDecoration = "WarningDecoration";

	public static final String Alias = "Alias";
	public static final String Indicator = "Indicator";
	public static final String XSDAttribute = "XSDAttribute";
	public static final String XSDElement = "XSDElement";
	public static final String XSDComplexType = "XSDComplexType";
	public static final String XSDSimpleType = "XSDSimpleType";
	public static final String WhereUsed = "WhereUsed";
	public static final String ID_Reference = "ID_Reference";
	public static final String IndicatorElement = "IndicatorElement";

	public static final String Simple = "Simple";
	public static final String Enumeration = "Enumeration";
	public static final String RoleValue = "RoleValue";
	public static final String ValueWithAttr = "ValueWithAttr";
	public static final String BusinessObject = "BusinessObject";
	public static final String CoreObject = "CoreObjectMO";
	public static final String ChoiceObject = "ChoiceObject";
	public static final String Facet = "Facet";

	public static final String Folder = "folder";
	public static final String Resources = "Resources";
	public static final String Resource = "Resource";
	public static final String ParamGroup = "Parameters";
	public static final String ResourceAction = "ResourceAction";
	public static final String ActionFacet = "ActionFacet";
	public static final String ActionRequest = "ActionRequest";
	public static final String ActionResponse = "ActionResponse";
	public static final String ResourceParameter = "ResourceParameters";
	public static final String ResourceParentRef = "ResourceParentRef";

	public static final String Service = "Service";
	public static final String Operation = "Operation";
	public static final String Project = "Project";
	public static final String Repository = "Repository";
	public static final String Namespace = "Namespace";
	public static final String NamespaceManaged = "NamespaceManaged";

	public static final String Save = "Save";
	public static final String SaveAll = "SaveAll";
	public static final String Lock = "Lock";
	public static final String AddNode = "AddNode";
	public static final String Delete = "delete";
	public static final String AddComponent = "AddComponent";
	public static final String Validate = "Validate";
	public static final String Filter = "Filter";
	public static final String LinkedWithNavigator = "LinkedWithNavigator";
	public static final String MergeNodes = "MergeNodes";

	public static ImageRegistry getImageRegistry() {
		if (image_registry == null) {
			initializeRegistry();
		}
		return image_registry;
	}

	private static void initializeRegistry() {
		if (!OtmRegistry.getMainWindow().hasDisplay())
			return; // make safe for junit
		image_registry = new ImageRegistry();
		final String pluginID = "org.opentravel.schemas.stl2Developer";

		image_registry.put(ErrorDecoration, AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/error.gif"));
		image_registry.put(WarningDecoration,
				AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/warning_co.gif"));
		image_registry.put(Error, AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/error_st_obj.gif"));
		image_registry.put("AddComponent", AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/add_att.gif"));

		image_registry.put("library", AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/Library.png"));
		image_registry
				.put(libraryChain, AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/LibraryChain.gif"));
		image_registry
				.put(builtInLib, AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/LibraryBuiltIn.gif"));
		image_registry.put("Service", AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/service3.gif"));

		image_registry.put(Alias, AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/Alias.gif"));
		image_registry.put(Indicator, AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/Indicator.gif"));
		image_registry
				.put(XSDAttribute, AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/XSDAttribute.gif"));
		image_registry.put(XSDElement, AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/XSDElement.gif"));
		image_registry.put(XSDSimpleType,
				AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/XSDSimpleType.gif"));
		image_registry.put(XSDComplexType,
				AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/XSDComplexType.gif"));
		image_registry.put(Simple, AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/XSDSimpleType.gif"));
		image_registry
				.put(Enumeration, AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/XSDSimpleEnum.gif"));
		image_registry.put(ValueWithAttr, AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/VWA.gif"));
		image_registry.put(CoreObject, AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/CoreObject.gif"));
		image_registry.put(BusinessObject,
				AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/BusinessObject.png"));
		image_registry.put(ChoiceObject, AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/Choice.gif"));
		image_registry.put(Facet, AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/Facet.gif"));
		image_registry.put(RoleValue, AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/RoleValue.jpg"));

		image_registry.put("aggregateFolder",
				AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/aggregateFolder.gif"));
		image_registry.put("folder", AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/folder.gif"));
		image_registry.put("command.open",
				AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/command_open.png"));
		image_registry.put("file",
				AbstractUIPlugin.imageDescriptorFromPlugin("org.opentravel.schemas.stl2Developer", "icons/file.gif"));
		image_registry.put("family", AbstractUIPlugin.imageDescriptorFromPlugin("org.opentravel.schemas.stl2Developer",
				"icons/icon_OpenDrive.gif"));
		image_registry.put("facet", AbstractUIPlugin.imageDescriptorFromPlugin("org.opentravel.schemas.stl2Developer",
				"icons/icons-legend.gif"));
		image_registry.put("simpleFacet", AbstractUIPlugin.imageDescriptorFromPlugin(
				"org.opentravel.schemas.stl2Developer", "icons/icons-legend.gif"));
		image_registry.put("attribute", AbstractUIPlugin.imageDescriptorFromPlugin(
				"org.opentravel.schemas.stl2Developer", "icons/icon_attribute.jpg"));
		image_registry.put("attributeXSD", AbstractUIPlugin.imageDescriptorFromPlugin(
				"org.opentravel.schemas.stl2Developer", "icons/icon_attributeXSD.jpg"));
		image_registry.put("docItem",
				AbstractUIPlugin.imageDescriptorFromPlugin("org.opentravel.schemas.stl2Developer", "icons/url.gif"));
		image_registry.put("docRoot", AbstractUIPlugin.imageDescriptorFromPlugin(
				"org.opentravel.schemas.stl2Developer", "icons/doc_open.gif"));
		image_registry.put("context", AbstractUIPlugin.imageDescriptorFromPlugin(
				"org.opentravel.schemas.stl2Developer", "icons/contexts.gif"));
		image_registry.put(Delete, AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/delete_edit.gif"));
		image_registry.put("WhereUsed", AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/WhereUsed.png"));

		image_registry.put("Project", AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/project.gif"));
		image_registry.put("Repository", AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/repository.gif"));
		image_registry.put("NamespaceManaged",
				AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/namespace.gif"));
		image_registry.put("Namespace",
				AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/namespace_base.gif"));

		image_registry.put("Save", AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/save.gif"));
		image_registry.put("SaveAll", AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/saveAll.gif"));
		image_registry.put("Lock", AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/lock.gif"));

		image_registry.put("AddNode", AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/add_att.gif"));

		image_registry.put("ID_Reference",
				AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/id_reference.gif"));
		image_registry.put("IndicatorElement",
				AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/indicatorElement.gif"));
		image_registry.put(Validate, AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/validate.gif"));
		image_registry.put(Filter, AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/filter_ps.gif"));
		image_registry.put(LinkedWithNavigator,
				AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/linked.gif"));
		image_registry.put(MergeNodes, AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/merge.gif"));

		image_registry.put(Resource, AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/resource.gif"));
		image_registry.put(Resources, AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/resources.gif"));
		image_registry.put(ParamGroup, AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/Parameters.gif"));
		image_registry.put(ResourceAction,
				AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/ResourceAction.gif"));
		image_registry.put(ActionFacet, AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/ActionFacet.gif"));
		image_registry.put(ActionRequest, AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/request.gif"));
		image_registry.put(ActionResponse, AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/response.gif"));
		image_registry.put(ResourceParameter,
				AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/resource.gif"));
		image_registry.put(ResourceParentRef,
				AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, "icons/parent_ref.png"));
	}

}
