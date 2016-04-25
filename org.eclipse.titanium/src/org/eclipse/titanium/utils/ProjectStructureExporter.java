/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titan.common.utils.MapAppender;
import org.eclipse.titan.common.utils.StringUtils;

public class ProjectStructureExporter {

	private IProject project;

	private static class ResourceData {
		enum ResourceType {
			FILE,
			PROJECT,
			FOLDER
		}

		private ResourceType type;
		private String projectRelativePath;
		private URI absoluteURI;
	}

	private static class ResourceVisitor implements IResourceVisitor {
		private List<ResourceData> resources = new ArrayList<ResourceData>();

		@Override
		public boolean visit(final IResource resource) throws CoreException {
			final ResourceData data = new ResourceData();
			switch (resource.getType()) {

			case IResource.PROJECT:
				data.type = ResourceData.ResourceType.PROJECT;
				break;
			case IResource.FOLDER:
				data.type = ResourceData.ResourceType.FOLDER;
				if (isIgnored(resource)) {
					return false;
				}
				break;
			case IResource.FILE:
				data.type = ResourceData.ResourceType.FILE;
				break;
			default:
				break;
			}

			data.projectRelativePath = resource.getProjectRelativePath().toString();
			data.absoluteURI = resource.getLocationURI();
			resources.add(data);
			return true;
		}

		private boolean isIgnored(final IResource resource) {
			return ".sonar".equals(resource.getName()) || ".sonar_titanium".equals(resource.getName());
		}

	}

	public ProjectStructureExporter(final IProject project) {
		this.project = project;
	}

	public void saveTo(final URI path) throws IOException {
		final ResourceVisitor visitor = new ResourceVisitor();
		try {
			project.accept(visitor);
		} catch (CoreException e1) {
			ErrorReporter.logExceptionStackTrace("Error while collecting resources", e1);
		}

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(path)), "UTF-8"));
			final MapAppender appender = new MapAppender(writer, StringUtils.lineSeparator(), ";");
			for (ResourceData data : visitor.resources) {
				appender.append(data.type, data.projectRelativePath, data.absoluteURI.toString());
			}
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

}
