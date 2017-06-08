package org.cytoscape.welcome.internal.task;

import java.util.Collection;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewCollectionTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.welcome.internal.VisualStyleBuilder;
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

public class CreateCustomViewTask extends AbstractNetworkViewCollectionTask {

	private final VisualStyleBuilder builder;
	private final CyServiceRegistrar serviceRegistrar;

	public CreateCustomViewTask(
			final Collection<CyNetworkView> networkViews,
			final VisualStyleBuilder builder,
			final CyServiceRegistrar serviceRegistrar
	) {
		super(networkViews);
		this.builder = builder;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Visualizing Network");

		VisualMappingManager vmManager = serviceRegistrar.getService(VisualMappingManager.class);

		double progress = 0.0d;
		tm.setProgress(progress);
		final double increment = 1.0d / networkViews.size();

		for (final CyNetworkView netView : networkViews) {
			final CyNetwork network = netView.getModel();
			tm.setStatusMessage("Visualizing " + network.getRow(network).get(CyNetwork.NAME, String.class));

			final VisualStyle newStyle = builder.buildVisualStyle(netView);
			vmManager.addVisualStyle(newStyle);
			vmManager.setCurrentVisualStyle(newStyle);
			newStyle.apply(netView);
			netView.updateView();

			progress += increment;
		}
	}
}
