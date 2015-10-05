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

	private final RecentlyOpenedTracker fileTracker;
	private final DialogTaskManager taskManager;
	private final OpenSessionTaskFactory openSessionTaskFactory;
	
	/**
	 * {@link CyApplicationConfiguration} service used to obtain the directories.
	 */
	private final CyApplicationConfiguration applicationConfiguration;

	public OpenSessionPanel(final RecentlyOpenedTracker fileTracker, final DialogTaskManager taskManager,
			final OpenSessionTaskFactory openSessionTaskFactory,
			final CyApplicationConfiguration applicationConfiguration ) {
		super("Open Recent Session");
		this.fileTracker = fileTracker;
		this.taskManager = taskManager;
		this.openSessionTaskFactory = openSessionTaskFactory;
		this.applicationConfiguration = applicationConfiguration;
		initComponents();
	}

	private void initComponents() {
		try {
			openIconImg = ImageIO.read(WelcomeScreenDialog.class.getClassLoader().getResource(ICON_LOCATION));
		} catch (IOException e) {
			e.printStackTrace();
		}

		openIcon = new ImageIcon(openIconImg);

		final List<URL> recentFiles = fileTracker.getRecentlyOpenedURLs();
		int fileCount = recentFiles.size();
		
		if (fileCount > MAX_FILES) {
			fileCount = MAX_FILES;
		}	
		
		
		// This sets to maximal number of example files that could be added:
        int maxExampleFiles = 0;
		if (fileCount < MAX_FILES ) {
			maxExampleFiles = MAX_FILES - fileCount;
		}
		
		// This sets the actual number of example files that will be added:
		final List<ExampleFile>  examples = getExamples();
		int exampleFileCount = examples.size();
		if (exampleFileCount > maxExampleFiles) {
			exampleFileCount = maxExampleFiles;
		}
		if ( exampleFileCount < MIN_EXAMPLE_FILES) {
			exampleFileCount = MIN_EXAMPLE_FILES;
		}

		
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
		
		
		for (int i = 0; i < fileCount; i++) {
			final URL target = recentFiles.get(i);

			URI fileURI = null;
			try {
				fileURI = target.toURI();
			} catch (URISyntaxException e2) {
				logger.error("Invalid file URL.", e2);
				continue;
			}
			
			final File targetFile = new File(fileURI);
			final JLabel fileLabel = new JLabel();
			fileLabel.setText(" - " + targetFile.getName());
			fileLabel.setFont(fileLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			fileLabel.setForeground(LINK_FONT_COLOR);
			fileLabel.setHorizontalAlignment(SwingConstants.LEFT);
			fileLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			fileLabel.setToolTipText(fileURI.toString());
			fileLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (targetFile.exists()) {
						taskManager.execute(openSessionTaskFactory.createTaskIterator(targetFile));
						closeParentWindow();
					} else {
						JOptionPane.showMessageDialog(OpenSessionPanel.this.getTopLevelAncestor(),
								"Session file not found:\n" + targetFile.getAbsolutePath(),
								"File not Found",
								JOptionPane.WARNING_MESSAGE);
					}
				}
			});

			hGroup.addComponent(fileLabel, PREFERRED_SIZE, DEFAULT_SIZE, 300);
			vGroup.addComponent(fileLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
		}
		
		// This adds the link to the example files:
		for (int i = 0; i < exampleFileCount; i++) {
			final ExampleFile example = examples.get(i);
			if ( example != null ) {
			    addLinkToExample(hGroup, vGroup, example);
			}
		}
		
		hGroup.addComponent(openButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
		vGroup.addComponent(openButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
	}

	// This adds the link to one example file.
	private void addLinkToExample(final ParallelGroup hGroup,
			final SequentialGroup vGroup, final ExampleFile ex) {
		if ( ex.getLocation() != null && ex.getLocation().exists() &&
				ex.getLocation().canRead()	) {
			final JLabel fileLabel = new JLabel();
			fileLabel.setText(" - " + ex.getLabel());
			fileLabel.setFont(fileLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			fileLabel.setForeground(LINK_FONT_COLOR);
			fileLabel.setHorizontalAlignment(SwingConstants.LEFT);
			fileLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			fileLabel.setToolTipText( ex.getLocation().toString());
			if (ex.getHelp() != null && ex.getHelp().length() > 0 ) {
				fileLabel.setToolTipText( ex.getHelp());
			}
			else {
				fileLabel.setToolTipText( ex.getLocation().toString());
			}
			fileLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
						taskManager.execute(openSessionTaskFactory.createTaskIterator(ex.getLocation()));
						closeParentWindow();
				}
			});
			hGroup.addComponent(fileLabel, PREFERRED_SIZE, DEFAULT_SIZE, 300);
			vGroup.addComponent(fileLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
		}
		else {
		   logger.error("failed to get example file " + ex.getLabel() + " at: " + ex.getLocation() );
		}
	}
	
	// This returns to location of the example files.
	private final File getExampleDirLocation() {
		if (applicationConfiguration != null ) {
			return new File( applicationConfiguration.getInstallationDirectoryLocation() + "/" + SAMPLE_DATA_DIR + "/" );
		}
		else {
			logger.error("application configuration is null, cannot find the installation directory");
			return null;
		}
	}
	
	// This returns a list of example files.
	private List<ExampleFile> getExamples() {
		final List<ExampleFile> examples = new ArrayList<ExampleFile>();
		String galFilteredToolTip = "";
		final File example_dir = getExampleDirLocation();
		if ( example_dir != null && example_dir.exists() ) {
			galFilteredToolTip = "This (\"gal Filtered\"), and other examples files, can be found in " + example_dir;
		}
		final ExampleFile galFiltered = new ExampleFile(getGalFilteredLocation(), GAL_FILTERED_EXAMPLE_BUTTON_LABEL, galFilteredToolTip );
		examples.add( galFiltered );
		return  examples;
	}
	
	// This get the location for "galFiltered.cys".
	private final File getGalFilteredLocation() {
		if (applicationConfiguration != null ) {
			return new File( applicationConfiguration.getInstallationDirectoryLocation() + "/" + SAMPLE_DATA_DIR + "/" +  GAL_FILTERED_CYS );
		}
		else {
			logger.error("application configuration is null, cannot find the installation directory");
			return null;
		}
	}
	


}
