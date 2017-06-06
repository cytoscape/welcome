package org.cytoscape.welcome.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;

import org.cytoscape.application.CyVersion;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.OpenBrowser;

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
public class UpdatesDialog extends JDialog {
	
	private static final String DOWNLOAD_URL = "http://cytoscape.org/download.php";

	private JPanel cardPanel;
	private JPanel loadingPanel;
	private JPanel statusPanel;
	private JCheckBox checkBox;
	private JButton downloadButton;
	private JButton closeButton;
	private final JLabel statusIconLabel = new JLabel();
	private final JLabel statusLabel = new JLabel();
	private JProgressBar progressBar;
	private final CardLayout cardLayout = new CardLayout();
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public UpdatesDialog(
			final Window owner,
			final String latestVersion,
			final boolean hide,
			final CyServiceRegistrar serviceRegistrar
	) {
		super(owner);
		this.serviceRegistrar = serviceRegistrar;

		initComponents();
		getCheckBox().setSelected(hide);
		update(latestVersion);
	}

	public boolean getHideStatus() {
		return getCheckBox().isSelected();
	}

	@Override
	public void setVisible(boolean b) {
		if (b) {
			invalidate();
			pack();
		}
		
		super.setVisible(b);
	}
	
	public void update(String latestVersion) {
		boolean downloadEnabled = false;
		String cardName = getLoadingPanel().getName();
		
		if (latestVersion != null) {
			cardName = getStatusPanel().getName();
			String version = serviceRegistrar.getService(CyVersion.class).getVersion();
			
			if (version.equals(latestVersion)) {
				statusIconLabel.setText(IconManager.ICON_CHECK_CIRCLE);
				statusIconLabel.setForeground(LookAndFeelUtil.getSuccessColor());
				statusLabel.setText("Cytoscape is up to date!");
			} else {
				if (version.contains("-")) {
					statusIconLabel.setText(IconManager.ICON_INFO_CIRCLE);
					statusIconLabel.setForeground(LookAndFeelUtil.getInfoColor());
					statusLabel.setText("This is a pre-release version of Cytoscape.");
				} else {
					statusIconLabel.setText(IconManager.ICON_EXCLAMATION_TRIANGLE);
					statusIconLabel.setForeground(LookAndFeelUtil.getWarnColor());
					statusLabel.setText("A new version of Cytoscape is available: " + latestVersion);
					downloadEnabled = true;
				}
			}
		}
		
		cardLayout.show(getCardPanel(), cardName);
		getDownloadButton().setVisible(downloadEnabled);
		
		if (getDownloadButton().isVisible()) {
			LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), getDownloadButton().getAction(),
					getCloseButton().getAction());
			getRootPane().setDefaultButton(getDownloadButton());
			getDownloadButton().requestFocusInWindow();
		} else {
			LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), getCloseButton().getAction(),
					getCloseButton().getAction());
			getRootPane().setDefaultButton(getCloseButton());
			getCloseButton().requestFocusInWindow();
		}
		
		revalidate();
	}
	
	public void showError() {
		statusIconLabel.setText(IconManager.ICON_MINUS_CIRCLE);
		statusIconLabel.setForeground(LookAndFeelUtil.getErrorColor());
		statusLabel.setText("Unnable to check for updates. Please try again later.");
		
		getDownloadButton().setVisible(false);
		
		cardLayout.show(getCardPanel(), getStatusPanel().getName());
	}
	
	private void initComponents() {
		this.setResizable(false);
		this.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		
		statusIconLabel.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(24.0f));
		
		final JPanel bottomPanel = LookAndFeelUtil.createOkCancelPanel(getDownloadButton(), getCloseButton(), 
				getCheckBox());

		final GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
				.addComponent(getCardPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(bottomPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getCardPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(bottomPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
	}
	
	private JPanel getCardPanel() {
		if (cardPanel == null) {
			cardPanel = new JPanel();
			cardPanel.setLayout(cardLayout);
			
			cardPanel.add(getLoadingPanel(), getLoadingPanel().getName());
			cardPanel.add(getStatusPanel(), getStatusPanel().getName());
		}
		
		return cardPanel;
	}
	
	private JPanel getLoadingPanel() {
		if (loadingPanel == null) {
			loadingPanel = new JPanel();
			loadingPanel.setName("LoadingPanel");
			
			JLabel label = new JLabel("Checking for Updates...");
			
			final GroupLayout layout = new GroupLayout(loadingPanel);
			loadingPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
					.addComponent(label, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getProgressBar(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGap(0, 0, Short.MAX_VALUE)
					.addComponent(label, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getProgressBar(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(0, 0, Short.MAX_VALUE)
			);
		}
		
		return loadingPanel;
	}
	
	private JPanel getStatusPanel() {
		if (statusPanel == null) {
			statusPanel = new JPanel();
			statusPanel.setName("StatusPanel");
			
			final int hpad = 40;
			final int vpad = 40;
			
			final GroupLayout layout = new GroupLayout(statusPanel);
			statusPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGap(hpad,  hpad, Short.MAX_VALUE)
					.addComponent(statusIconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(statusLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGap(hpad,  hpad, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(CENTER, false)
					.addGap(vpad)
					.addComponent(statusIconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(statusLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(vpad)
			);
			
			statusPanel.setMinimumSize(new Dimension(420, statusPanel.getPreferredSize().height));
		}
		
		return statusPanel;
	}
	
	@SuppressWarnings("unchecked")
	private JCheckBox getCheckBox() {
		if (checkBox == null) {
			checkBox = new JCheckBox("Do not notify me again");
			checkBox.setHorizontalAlignment(SwingConstants.LEFT);
			checkBox.addActionListener(evt -> {
				CyProperty<Properties> cyProps = serviceRegistrar.getService(CyProperty.class,
						"(cyPropertyName=cytoscape3.props)");
				cyProps.getProperties().setProperty(CheckForUpdatesAction.DO_NOT_DISPLAY_PROP_NAME,
						((Boolean) checkBox.isSelected()).toString());
			});
		}
		
		return checkBox;
	}
	
	private JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton(new AbstractAction("Close") {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
		}
		
		return closeButton;
	}
	
	private JButton getDownloadButton() {
		if (downloadButton == null) {
			downloadButton = new JButton(new AbstractAction("Download Now") {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
					serviceRegistrar.getService(OpenBrowser.class).openURL(DOWNLOAD_URL);
				}
			});
			downloadButton.setVisible(false);
		}
		
		return downloadButton;
	}
	
	public JProgressBar getProgressBar() {
		if (progressBar == null) {
			progressBar = new JProgressBar();
			progressBar.setIndeterminate(true);
		}
		
		return progressBar;
	}
}
