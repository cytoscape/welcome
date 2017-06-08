package org.cytoscape.welcome.internal.task;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.analyze.AnalyzeNetworkCollectionTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.welcome.internal.VisualStyleBuilder;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape Welcome Screen Impl (welcome-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

public class GenerateCustomStyleTask extends AbstractTask {

	private final VisualStyleBuilder builder;
	private final CyServiceRegistrar serviceRegistrar;

	GenerateCustomStyleTask(final VisualStyleBuilder builder, final CyServiceRegistrar serviceRegistrar) {
		this.builder = builder;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final AnalyzeNetworkCollectionTaskFactory taskFactory; 
		
		try {
			taskFactory = serviceRegistrar.getService(AnalyzeNetworkCollectionTaskFactory.class);
		} catch (Throwable t) {
			throw new Exception("Network Analyzer is currently unavailable.\n"
					+ "Make sure Cytoscape has finished starting up and try again.");
		}

		final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
		final List<CyNetworkView> selectedViews = applicationManager.getSelectedNetworkViews();
		final CyNetworkView currentView = applicationManager.getCurrentNetworkView();

		final Set<CyNetworkView> networkViews = new HashSet<>(selectedViews);
		networkViews.add(currentView);

		final Set<CyNetwork> networks = new HashSet<>();
		networks.add(currentView.getModel());
		
		for (final CyNetworkView view : selectedViews)
			networks.add(view.getModel());

		final TaskIterator iterator = taskFactory.createTaskIterator(networks);
		final CreateCustomViewTask task = new CreateCustomViewTask(networkViews, builder, serviceRegistrar);
		
		insertTasksAfterCurrentTask(task);
		insertTasksAfterCurrentTask(iterator);
	}
}
