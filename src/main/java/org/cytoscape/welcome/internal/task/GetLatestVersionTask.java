package org.cytoscape.welcome.internal.task;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
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

public class GetLatestVersionTask extends AbstractTask {

	private static final String NEWS_URL = "http://chianti.ucsd.edu/cytoscape-news/news.html";
	
	private String latestVersion;
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		Document doc = Jsoup.connect(NEWS_URL).get();
		Elements metaTags = doc.getElementsByTag("meta");
		
		for (Element tag : metaTags) {
			if ("latestVersion".equals(tag.attr("name"))) {
				latestVersion = tag.attr("content");
				break;
			}
		}
	}

	public String getLatestVersion() {
		return latestVersion;
	}
}
