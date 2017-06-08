package org.cytoscape.welcome.internal.view;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.cytoscape.application.CyVersion;
import org.cytoscape.application.events.CyStartEvent;
import org.cytoscape.application.events.CyStartListener;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.welcome.internal.task.GetLatestVersionTask;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;

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

@SuppressWarnings("serial")
public class CheckForUpdatesAction extends AbstractCyAction implements CyStartListener {

	public static final String HIDE_UPDATES_PROP = "hideUpdatesNotification";
	public static final String HIDE_PROP = "hideWelcomeScreen";
	public static final String TEMP_HIDE_PROP = "tempHideWelcomeScreen";
	private static final String NAME = "Check for Updates";
	private static final String PARENT_NAME = "Help";

	private final String thisVersion;
	
	private final CyServiceRegistrar serviceRegistrar;

	public CheckForUpdatesAction(final CyServiceRegistrar serviceRegistrar) {
		super(NAME);
		setPreferredMenu(PARENT_NAME);
		setMenuGravity(9.999f);

		this.serviceRegistrar = serviceRegistrar;
		thisVersion = serviceRegistrar.getService(CyVersion.class).getVersion();
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
//		if (isPreRelease(getCurrentVersion()))
//			SwingUtilities.invokeLater(() -> showDialog(""));
		
		final GetLatestVersionTask task = new GetLatestVersionTask();
		runTask(task, new TaskObserver() {
			@Override
			public void taskFinished(ObservableTask task) {
			}
			@Override
			public void allFinished(FinishStatus finishStatus) {
				if (finishStatus.getType() == FinishStatus.Type.SUCCEEDED) {
					String latestVersion = task.getLatestVersion();
					SwingUtilities.invokeLater(() -> showDialog(latestVersion, false));
				}
			}
		});
	}

	@Override
	public void handleEvent(CyStartEvent cyStartEvent) {
		if (isPreRelease(thisVersion))
			return; // The current version is a pre-release (snapshot, beta, etc), so let's not bother the user
		
		final GetLatestVersionTask task = new GetLatestVersionTask();
		runTask(task, new TaskObserver() {
			@Override
			public void taskFinished(ObservableTask task) {
			}
			@Override
			public void allFinished(FinishStatus finishStatus) {
				if (finishStatus.getType() == FinishStatus.Type.SUCCEEDED) {
					// Always check the version when stating Cytoscape, in order to log statistics
					String latestVersion = task.getLatestVersion();
					
					// Displays the dialog after startup based on whether the specified property has been set.
					boolean hide = false;
					final Properties props = getCyProperties();
					
					// Hide if this version up to date
					if (latestVersion == null || latestVersion.isEmpty() || thisVersion.equals(latestVersion))
						hide = true;
					
					if (!hide) {
						final String hideVersion = props.getProperty(HIDE_UPDATES_PROP, "").trim();
						// If set to "true", always hide, no matter the new version
						hide = hideVersion.equalsIgnoreCase("true") || hideVersion.equals(latestVersion);
					}
					
					if (!hide) {
						final String tempHideString = props.getProperty(TEMP_HIDE_PROP);
						hide = parseBoolean(tempHideString);
					}

					if (!hide) {
						final String systemHideString = System.getProperty(HIDE_PROP);
						hide = parseBoolean(systemHideString);
					}
					
					// Remove this property regardless!
					props.remove(TEMP_HIDE_PROP);
					
					if (!hide)
						SwingUtilities.invokeLater(() -> showDialog(latestVersion, true));
				}
			}
		});
	}
	
	private void runTask(Task task, TaskObserver observer) {
		TaskIterator iterator = new TaskIterator(task);
		serviceRegistrar.getService(DialogTaskManager.class).execute(iterator, observer);
	}

	private static boolean isPreRelease(final String version) {
		return version.contains("-");
	}

	private void showDialog(String latestVersion, boolean hideOptionVisible) {
		JFrame owner = serviceRegistrar.getService(CySwingApplication.class).getJFrame();
		UpdatesDialog dialog = new UpdatesDialog(owner, thisVersion, latestVersion, hideOptionVisible, serviceRegistrar);
		dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		dialog.pack();
		dialog.setLocationRelativeTo(owner);
		dialog.setVisible(true);

		if (hideOptionVisible) {
			// Update property
			if (dialog.getHideStatus())
				getCyProperties().setProperty(HIDE_UPDATES_PROP, latestVersion);
			else
				getCyProperties().remove(HIDE_UPDATES_PROP);
		}
	}
	
	private Properties getCyProperties() {
		return (Properties) serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)")
				.getProperties();
	}
	
	private static boolean parseBoolean(String hideString) {
		boolean lhide = false;
		
		if (hideString == null) {
			lhide = false;
		} else {
			try {
				// might make it true!
				lhide = Boolean.parseBoolean(hideString);
			} catch (Exception ex) {
				lhide = false;
			}
		}
		
		return lhide;
	}
}
