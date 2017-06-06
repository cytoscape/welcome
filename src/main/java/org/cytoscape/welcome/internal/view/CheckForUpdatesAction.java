package org.cytoscape.welcome.internal.view;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.cytoscape.application.CyVersion;
import org.cytoscape.application.events.CyStartEvent;
import org.cytoscape.application.events.CyStartListener;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

	private static final String NEWS_URL = "http://chianti.ucsd.edu/cytoscape-news/news.html";
	
	public static final String DO_NOT_DISPLAY_PROP_NAME = "hideWelcomeScreen";
	public static final String TEMP_DO_NOT_DISPLAY_PROP_NAME = "tempHideWelcomeScreen";
	private static final String NAME = "Check for Updates...";
	private static final String PARENT_NAME = "Help";

	private boolean hide;
	private UpdatesDialog dialog;

	private final CyServiceRegistrar serviceRegistrar;

	public CheckForUpdatesAction(final CyServiceRegistrar serviceRegistrar) {
		super(NAME);
		setPreferredMenu(PARENT_NAME);
		this.setMenuGravity(1.5f);

		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		new SwingWorker<String, String>() {
			
			boolean error = false;
			String latestVersion = "";
			
			@Override
			public String doInBackground() {
				SwingUtilities.invokeLater(() -> showDialog(null));
				
				if (!isPreRelease(getCurrentVersion())) {
					try {
						latestVersion = getLatestVersion();
					} catch (Exception e) {
						e.printStackTrace();
						error = true;
					}
				}
				
				return latestVersion;
			}
			@Override
			protected void done() {
				if (dialog != null) {
					if (error)
						dialog.showError();
					else
						dialog.update(latestVersion);
					
					dialog.pack();
				}
			}
		}.execute();
	}

	@Override
	public void handleEvent(CyStartEvent cyStartEvent) {
		// Displays the dialog after startup based on whether the specified property has been set.
		final Properties props = getCyProperties();
		final String hideString = props.getProperty(DO_NOT_DISPLAY_PROP_NAME);
		hide = parseBoolean(hideString);

		if (!hide) {
			final String tempHideString = props.getProperty(TEMP_DO_NOT_DISPLAY_PROP_NAME);
			hide = parseBoolean(tempHideString);
		}

		if (!hide) {
			final String systemHideString = System.getProperty(DO_NOT_DISPLAY_PROP_NAME);
			hide = parseBoolean(systemHideString);
		}

		// remove this property regardless!
		props.remove(TEMP_DO_NOT_DISPLAY_PROP_NAME);

		if (!hide) {
			final String version = getCurrentVersion();
			
			if (isPreRelease(version))
				return; // The current version is a pre-release (snapshot, beta, etc), so let's not bother the user
			
			try {
				String latestVersion = getLatestVersion();
				
				// Is the current version up to date?
				if (!latestVersion.isEmpty() && !version.equals(latestVersion))
					SwingUtilities.invokeLater(() -> showDialog(latestVersion));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static boolean isPreRelease(final String version) {
		return version.contains("-");
	}

	private void showDialog(String latestVersion) {
		JFrame owner = serviceRegistrar.getService(CySwingApplication.class).getJFrame();
		dialog = new UpdatesDialog(owner, latestVersion, hide, serviceRegistrar);
		dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		dialog.pack();
		dialog.setLocationRelativeTo(owner);
		dialog.setVisible(true);

		// Update property
		hide = dialog.getHideStatus();
		getCyProperties().setProperty(DO_NOT_DISPLAY_PROP_NAME, ((Boolean) hide).toString());
		dialog = null;
	}
	
	private String getCurrentVersion() {
		return serviceRegistrar.getService(CyVersion.class).getVersion();
	}
	
	private String getLatestVersion() throws Exception {
		Document doc = Jsoup.connect(NEWS_URL).get();
		Elements metaTags = doc.getElementsByTag("meta");
		
		for (Element tag : metaTags) {
			if ("latestVersion".equals(tag.attr("name")))
				return tag.attr("content");
		}

		return "";
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
