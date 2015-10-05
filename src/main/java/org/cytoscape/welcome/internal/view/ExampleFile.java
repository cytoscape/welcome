package org.cytoscape.welcome.internal.view;

import java.io.File;


/*
 * Helper class to hold information about example files.
 * 
 * #%L
 * Cytoscape Welcome Screen Impl (welcome-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2015 The Cytoscape Consortium
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
final class ExampleFile {
	
	final private File location;
	final private String label;
	final private String help;
	
	ExampleFile( final File location, final String label, final String help ) {
		this.location = location;
		this.label = label;
		this.help  =  help;
	}

	final File getLocation() {
		return location;
	}

	final String getLabel() {
		return label;
	}

	final String getHelp() {
		return help;
	}
}

