package org.cytoscape.welcome.internal.view;

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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OpenSessionPanel extends AbstractWelcomeScreenChildPanel {

	private static final long serialVersionUID = 591882944100485039L;

	private static final Logger logger = LoggerFactory.getLogger(OpenSessionPanel.class);

	private static final String ICON_LOCATION = "/images/Icons/open-file-32.png";
	private static final String GAL_FILTERED_EXAMPLE_BUTTON_LABEL = "Sample Yeast Network";
	private static final String SAMPLE_DATA_DIR = "sampleData";
	private static final String GAL_FILTERED_CYS = "galFiltered.cys";
	private BufferedImage openIconImg;
	private ImageIcon openIcon;

	// Display up to 7 files due to space.
	private static final int MAX_FILES = 7;
	
	// Minimal number of example files to show.
	// For example, set to 1 to force to always show one example file (e.g. "gal filtered").
	private static final int MIN_EXAMPLE_FILES = 0;

	private final List<FileInfo> exampleFiles;
	
	private CyServiceRegistrar serviceRegistrar;
	
	public OpenSessionPanel(final CyServiceRegistrar serviceRegistrar) {
		super("Open Recent Session");
		this.serviceRegistrar = serviceRegistrar;
		
		try {
			openIconImg = ImageIO.read(WelcomeScreenDialog.class.getClassLoader().getResource(ICON_LOCATION));
		} catch (IOException e) {
			e.printStackTrace();
		}

		openIcon = new ImageIcon(openIconImg);
		exampleFiles = getExamples();
		
		initComponents();
	}

	public void update() {
		removeAll();
		initComponents();
	}

	private void initComponents() {
		final List<FileInfo> targetFiles = new ArrayList<>();
		
		final RecentlyOpenedTracker fileTracker = serviceRegistrar.getService(RecentlyOpenedTracker.class);
		final List<URL> recentFiles = fileTracker.getRecentlyOpenedURLs();
		int fileCount = Math.min(recentFiles.size(), MAX_FILES);
		
		for (int i = 0; i < fileCount; i++) {
			final URL url = recentFiles.get(i);
			URI fileURI = null;
			
			try {
				fileURI = url.toURI();
			} catch (URISyntaxException e2) {
				logger.error("Invalid file URL.", e2);
				continue;
			}
			
			final File file = new File(fileURI);
			
			if (file.exists() && file.canRead()) {
				// Add example file instead, so the link can have the proper label and help text.
				FileInfo fi = new FileInfo(file, file.getName(), file.getAbsolutePath());
				final int index = exampleFiles.indexOf(fi);
				
				if (index >= 0)
					fi = exampleFiles.get(index);
				
				targetFiles.add(fi);
			}
		}
		
		// Update file count
		fileCount = targetFiles.size();
		
		// This sets to maximal number of example files that could be added:
        int maxExampleFiles = (fileCount < MAX_FILES) ? (MAX_FILES - fileCount) : 0;
        // This sets the actual number of example files that will be added:
		maxExampleFiles = Math.min(exampleFiles.size(), maxExampleFiles);
		
		if (maxExampleFiles < MIN_EXAMPLE_FILES)
			maxExampleFiles = MIN_EXAMPLE_FILES;
		
		for (int i = 0; i < maxExampleFiles && i < exampleFiles.size(); i++) {
			final FileInfo fi = exampleFiles.get(i);
			
			if (targetFiles.contains(fi))
				continue; // Don't add it twice!
			
			if (fi.getFile() != null && fi.getFile().exists() && fi.getFile().canRead())
				targetFiles.add(fi);
			else
				logger.error("failed to get example file " + fi.getLabel() + " at: " + fi.getFile());
		}

		final DialogTaskManager taskManager = serviceRegistrar.getService(DialogTaskManager.class);
		final OpenSessionTaskFactory openSessionTaskFactory = serviceRegistrar.getService(OpenSessionTaskFactory.class);
		
		final JButton openButton = new JButton("Open Session File...", openIcon);
		openButton.setIconTextGap(20);
		openButton.setHorizontalAlignment(SwingConstants.LEFT);
		openButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, openButton.getMinimumSize().height));
		openButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeParentWindow();
				taskManager.execute(openSessionTaskFactory.createTaskIterator());
			}
		});

		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
		
		final ParallelGroup hGroup = layout.createParallelGroup(LEADING, true);
		final SequentialGroup vGroup = layout.createSequentialGroup();
		
		layout.setHorizontalGroup(hGroup);
		layout.setVerticalGroup(vGroup.addContainerGap());
		
		for (final FileInfo fi : targetFiles) {
			final File file = fi.getFile();
			final JLabel fileLabel = new JLabel(" - " + fi.getLabel());
			fileLabel.setFont(fileLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			fileLabel.setForeground(LINK_FONT_COLOR);
			fileLabel.setHorizontalAlignment(SwingConstants.LEFT);
			fileLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			fileLabel.setToolTipText(fi.getHelp());
			fileLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					onSessionLinkClicked(file);
				}
			});

			hGroup.addComponent(fileLabel, PREFERRED_SIZE, DEFAULT_SIZE, 300);
			vGroup.addComponent(fileLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
		}
		
		hGroup.addComponent(openButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
		vGroup.addComponent(openButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
	}

	// This returns to location of the example files.
	private final File getExampleDir() {
		final CyApplicationConfiguration applicationCfg = serviceRegistrar.getService(CyApplicationConfiguration.class);
		
		if (applicationCfg != null) {
			return new File(applicationCfg.getInstallationDirectoryLocation() + "/" + SAMPLE_DATA_DIR + "/");
		} else {
			logger.error("application configuration is null, cannot find the installation directory");
			return null;
		}
	}
	
	// This returns a list of example files.
	private List<FileInfo> getExamples() {
		final List<FileInfo> examples = new ArrayList<>();
		String galFilteredToolTip = "";
		final File exampleDir = getExampleDir();
		
		if (exampleDir != null && exampleDir.exists()) {
			galFilteredToolTip = "<html>This (<b>" + GAL_FILTERED_CYS + "</b>) and other example files can be found in:<br />"
					+ exampleDir.getAbsolutePath() + "</html>";
		}
		
		final FileInfo galFiltered =
				new FileInfo(getSampleFile(GAL_FILTERED_CYS), GAL_FILTERED_EXAMPLE_BUTTON_LABEL, galFilteredToolTip);
		examples.add(galFiltered);
		
		return examples;
	}
	
	// This get the location for "galFiltered.cys".
	private final File getSampleFile(final String filename) {
		final CyApplicationConfiguration applicationCfg = serviceRegistrar.getService(CyApplicationConfiguration.class);
		
		if (applicationCfg != null) {
			return new File(applicationCfg.getInstallationDirectoryLocation() + "/" + SAMPLE_DATA_DIR + "/" +  filename);
		} else {
			logger.error("application configuration is null, cannot find the installation directory");
			return null;
		}
	}
	
	private void onSessionLinkClicked(final File file) {
		closeParentWindow();
		
		if (file.exists()) {
			final CyNetworkManager netManager = serviceRegistrar.getService(CyNetworkManager.class);
			final CyTableManager tableManager = serviceRegistrar.getService(CyTableManager.class);
			
			if (netManager.getNetworkSet().isEmpty() && tableManager.getAllTables(false).isEmpty())
				openSession(file);
			else
				openSessionWithWarning(file);
		} else {
			JOptionPane.showMessageDialog(
					OpenSessionPanel.this.getTopLevelAncestor(),
					"Session file not found:\n" + file.getAbsolutePath(),
					"File not Found",
					JOptionPane.WARNING_MESSAGE
			);
			
			final RecentlyOpenedTracker fileTracker = serviceRegistrar.getService(RecentlyOpenedTracker.class);
			
			try {
				fileTracker.remove(file.toURI().toURL());
			} catch (Exception e) {
				logger.error("Error removing session file from RecentlyOpenedTracker.", e);
			}
		}
	}
	
	private void openSession(final File file) {
		final OpenSessionTaskFactory taskFactory = serviceRegistrar.getService(OpenSessionTaskFactory.class);
		final DialogTaskManager taskManager = serviceRegistrar.getService(DialogTaskManager.class);
		taskManager.execute(taskFactory.createTaskIterator(file));
	}
	
	private void openSessionWithWarning(final File file) {
		if (JOptionPane.showConfirmDialog(
				OpenSessionPanel.this.getTopLevelAncestor(),
				"Current session (all networks and tables) will be lost.\nDo you want to continue?",
				"Open Session",
				JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
			openSession(file);
	}
	
	private final class FileInfo {
		
		final private File file;
		final private String label;
		final private String help;
		
		FileInfo(final File file, final String label, final String help) {
			this.file = file;
			this.label = label;
			this.help  =  help;
		}

		final File getFile() {
			return file;
		}

		final String getLabel() {
			return label;
		}

		final String getHelp() {
			return help;
		}

		@Override
		public int hashCode() {
			final int prime = 17;
			int result = 7;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((file == null) ? 0 : file.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FileInfo other = (FileInfo) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (file == null) {
				if (other.file != null)
					return false;
			} else if (!file.equals(other.file)) {
				return false;
			}
			return true;
		}

		private OpenSessionPanel getOuterType() {
			return OpenSessionPanel.this;
		}
	}
}
