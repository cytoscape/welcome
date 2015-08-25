package org.cytoscape.welcome.internal;

/*
 * #%L
 * Cytoscape Welcome Screen Impl (welcome-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import static org.cytoscape.work.ServiceProperties.IN_TOOL_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyVersion;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.analyze.AnalyzeNetworkCollectionTaskFactory;
import org.cytoscape.task.create.NewEmptyNetworkViewFactory;
import org.cytoscape.task.read.LoadNetworkURLTaskFactory;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.welcome.internal.task.ApplySelectedLayoutTaskFactory;
import org.cytoscape.welcome.internal.task.GenerateCustomStyleTaskFactory;
import org.cytoscape.welcome.internal.view.NewNetworkPanel;
import org.cytoscape.welcome.internal.view.NewsPanel;
import org.cytoscape.welcome.internal.view.OpenSessionPanel;
import org.cytoscape.welcome.internal.view.WelcomeScreenAction;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(BundleContext bc) {
		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);
		CyApplicationManager applicationManager = getService(bc, CyApplicationManager.class);
		CyVersion cyVersion = getService(bc, CyVersion.class);
		NewEmptyNetworkViewFactory newEmptyNetworkViewFactory = getService(bc, NewEmptyNetworkViewFactory.class);
		BendFactory bendFactory = getService(bc, BendFactory.class);
		VisualMappingManager vmm = getService(bc, VisualMappingManager.class);
		VisualStyleFactory vsFactoryServiceRef = getService(bc, VisualStyleFactory.class);
		VisualMappingFunctionFactory continupousMappingFactoryRef = getService(bc, VisualMappingFunctionFactory.class,
				"(mapping.type=continuous)");
		VisualMappingFunctionFactory passthroughMappingFactoryRef = getService(bc, VisualMappingFunctionFactory.class,
				"(mapping.type=passthrough)");
		VisualMappingFunctionFactory discreteMappingFactoryRef = getService(bc, VisualMappingFunctionFactory.class,
				"(mapping.type=discrete)");
		VisualStyleBuilder vsBuilder = new VisualStyleBuilder(vsFactoryServiceRef, continupousMappingFactoryRef,
				discreteMappingFactoryRef, passthroughMappingFactoryRef, bendFactory);

		AnalyzeNetworkCollectionTaskFactory analyzeNetworkCollectionTaskFactory = getService(bc,
				AnalyzeNetworkCollectionTaskFactory.class);
		CySwingApplication cytoscapeDesktop = getService(bc, CySwingApplication.class);
		OpenBrowser openBrowserServiceRef = getService(bc, OpenBrowser.class);
		RecentlyOpenedTracker recentlyOpenedTrackerServiceRef = getService(bc, RecentlyOpenedTracker.class);
		OpenSessionTaskFactory openSessionTaskFactory = getService(bc, OpenSessionTaskFactory.class);
		DialogTaskManager dialogTaskManagerServiceRef = getService(bc, DialogTaskManager.class);
		TaskFactory importNetworkFileTF = getService(bc, TaskFactory.class, "(id=loadNetworkFileTaskFactory)");
		LoadNetworkURLTaskFactory importNetworkTF = getService(bc, LoadNetworkURLTaskFactory.class);
		DataSourceManager dsManagerServiceRef = getService(bc, DataSourceManager.class);
		@SuppressWarnings("unchecked")
		CyProperty<Properties> cytoscapePropertiesServiceRef = getService(bc, CyProperty.class,
				"(cyPropertyName=cytoscape3.props)");
		IconManager iconManager = getService(bc, IconManager.class);

		// Build Child Panels
		final OpenSessionPanel openPanel = new OpenSessionPanel(recentlyOpenedTrackerServiceRef, dialogTaskManagerServiceRef, openSessionTaskFactory);
//		final GeneSearchPanel geneSearchPanel = new GeneSearchPanel(dialogTaskManagerServiceRef, networkReaderManager, networkManager, networkViewFactory, layoutAlgorithmManager, vmm, networkViewManager, webServiceClient);

		final NewNetworkPanel newNetPanel = new NewNetworkPanel(bc, dialogTaskManagerServiceRef,
				importNetworkFileTF, importNetworkTF, dsManagerServiceRef, newEmptyNetworkViewFactory);
		registerAllServices(bc, newNetPanel, new Properties());

		// TODO: implement contents
		final NewsPanel newsPanel = new NewsPanel(cyVersion, iconManager);

		// Show Welcome Screen
		final WelcomeScreenAction welcomeScreenAction = new WelcomeScreenAction(newNetPanel, openPanel,
				newsPanel, cytoscapePropertiesServiceRef, cytoscapeDesktop, openBrowserServiceRef);
		registerAllServices(bc, welcomeScreenAction, new Properties());

		// Export preset tasks
		final GenerateCustomStyleTaskFactory generateCustomStyleTaskFactory = new GenerateCustomStyleTaskFactory(
				analyzeNetworkCollectionTaskFactory, applicationManager, vsBuilder, vmm);
		Properties generateCustomStyleTaskFactoryProps = new Properties();
		generateCustomStyleTaskFactoryProps.setProperty(PREFERRED_MENU, "Tools.Workflow[3.0]");
		generateCustomStyleTaskFactoryProps.setProperty(MENU_GRAVITY, "20.0");
		generateCustomStyleTaskFactoryProps.setProperty(TITLE, "Analyze selected networks and create custom styles");
		generateCustomStyleTaskFactoryProps.setProperty(IN_TOOL_BAR, "false");
		generateCustomStyleTaskFactoryProps.setProperty(ServiceProperties.ENABLE_FOR, "networkAndView");
		registerAllServices(bc, generateCustomStyleTaskFactory, generateCustomStyleTaskFactoryProps);

		// This is a preset task, so register it first.
		final Map<String, String> propMap = new HashMap<>();
		propMap.put(NewNetworkPanel.WORKFLOW_ID, "generateCustomStyleTaskFactory");
		propMap.put(NewNetworkPanel.WORKFLOW_NAME, "Analyze network and create custom style");
		propMap.put(NewNetworkPanel.WORKFLOW_DESCRIPTION,
				"Analyze current/selected networks and create custom style for each network.");
		newNetPanel.addTaskFactory(generateCustomStyleTaskFactory, propMap);

		// Define listener
		registerServiceListener(bc, newNetPanel, "addTaskFactory", "removeTaskFactory", TaskFactory.class);
		
		// Export task
		CyLayoutAlgorithmManager cyLayoutAlgorithmManager = getService(bc, CyLayoutAlgorithmManager.class);
		final ApplySelectedLayoutTaskFactory applySelectedLayoutTaskFactory = new ApplySelectedLayoutTaskFactory(
				registrar, applicationManager, cyLayoutAlgorithmManager);
		Properties applySelectedLayoutTaskFactoryProps = new Properties();
		applySelectedLayoutTaskFactoryProps.setProperty(NewNetworkPanel.WORKFLOW_ID, "applySelectedLayoutTaskFactory");
		applySelectedLayoutTaskFactoryProps.setProperty(NewNetworkPanel.WORKFLOW_NAME, "Apply layout algorithm of your choice");
		applySelectedLayoutTaskFactoryProps.setProperty(NewNetworkPanel.WORKFLOW_DESCRIPTION, "Apply a layout algorithm to the network.");
		registerService(bc, applySelectedLayoutTaskFactory, TaskFactory.class, applySelectedLayoutTaskFactoryProps );
	}
}
