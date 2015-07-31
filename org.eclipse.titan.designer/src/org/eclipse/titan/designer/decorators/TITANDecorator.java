/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.decorators;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.path.PathConverter;
import org.eclipse.titan.common.path.TITANPathUtilities;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.commonFilters.ExcludedResourceFilter;
import org.eclipse.titan.designer.commonFilters.ResourceExclusionHelper;
import org.eclipse.titan.designer.commonFilters.WorkingDirectoryFilter;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.core.ProjectBasedBuilder;
import org.eclipse.titan.designer.core.TITANNature;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titan.designer.properties.IPropertyChangeListener;
import org.eclipse.titan.designer.properties.PropertyNotificationManager;
import org.eclipse.titan.designer.properties.data.FolderBuildPropertyData;
import org.eclipse.titan.designer.properties.data.MakefileCreationData;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;

/**
 * This class decorates the resources of TITAN projects, with TITAN related
 * information.
 *
 * @author Kristof Szabados
 */
public final class TITANDecorator extends LabelProvider implements ILabelDecorator {
	public static final String DECORATOR_ID = ProductConstants.PRODUCT_ID_DESIGNER + ".decorators.TITANDecorator";

	static final String TRUE_STRING = "true";
	static final String FALSE_STRING = "false";
	static final String OPENING_PARENTHESIS = " [ ";
	static final String CLOSING_PARENTHESIS = " ] ";
	static final String CENTRAL_STORAGE = "centralstorage";
	static final String EXCLUDED_BY_CONVENTION = "excluded by convention";
	static final String EXCLUDED_BY_USER = "excluded by user";
	static final String EXCLUDED_BY_REGEXP = "excluded by regexp";
	static final String EXCLUDED_AS_WORKINGDIR = "excluded as workingdirectory";
	static final String SPACE = " ";
	static final String ABSOLUTE_PATH_OPTION = "a";
	static final String CENTRAL_STORAGE_OPTION = "c";
	static final String FORCE_OVERWRITE_OPTION = "f";
	static final String GNU_MAKE_OPTION = "g";
	static final String FUNCTION_TEST_OPTION = "R";
	static final String DYNAMIC_LINKING_OPTION = "l";
	static final String PREPROCESSOR_OPTION = "p";
	static final String SINGLE_MODE_OPTION = "s";
	static final String TARGET_EXECUTABLE_OPTION = "-e";
	static final String OUTPUTDIR_OPTION = "-o";

	static final String CHECK_ICON_NAME = "check.gif";

	private ResourceExclusionHelper helper;

	private static IPropertyChangeListener listener = new IPropertyChangeListener() {

		@Override
		public void propertyChanged(IResource resource) {
			if (resource instanceof IProject) {
				if (!resource.isAccessible()){ 
					return; 
				}
				IProject project = (IProject) resource;
				final List<IResource> allProjectResources = new ArrayList<IResource>();
				try {
					project.accept(new IResourceVisitor() {

						@Override
						public boolean visit(IResource resource) throws CoreException {
							allProjectResources.add(resource);
							return true;
						}
					});
				} catch (CoreException e) {
					ErrorReporter.logWarningExceptionStackTrace("While re-decorating " + project.getName(), e);
				}
				TITANDecorator.refreshSelectively(allProjectResources.toArray());
			} else {
				TITANDecorator.refreshSelectively(resource);
			}
		}
	};
	static {
		PropertyNotificationManager.addListener(listener);
	}


	public TITANDecorator() {
		resetMatchers();
	}

	private void resetMatchers() {
		helper = new ResourceExclusionHelper();
	}

	/**
	 * Used to decorate TITAN related projects.
	 *
	 * @param image
	 *                the input image to decorate, or <code>null</code> if
	 *                the element has no image
	 * @param element
	 *                the element whose image is being decorated
	 * @return the decorated image, or <code>null</code> if no decoration is
	 *         to be applied
	 *
	 * @see ILabelDecorator#decorateImage(Image, Object)
	 */
	@Override
	public Image decorateImage(final Image image, final Object element) {
		if (element == null || !(element instanceof IProject)) {
			return null;
		}
		IProject project = (IProject) element;
		try {
			if (!TITANNature.hasTITANNature(project)) {
				return null;
			}
			Object o = project.getSessionProperty(GeneralConstants.PROJECT_UP_TO_DATE);
			if (!Boolean.TRUE.equals(o)) {
				return null;
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return null;
		}

		OverlayImageIcon icon = new OverlayImageIcon(image, ImageCache.getImage(CHECK_ICON_NAME), OverlayImageIcon.Position.TOP_RIGHT);
		return icon.getImage();
	}

	/**
	 * Used to decorate TITAN related elements.
	 * <ul>
	 * <li>Projects are decorated with the command line arguments of the
	 * makefilegenerator, that could be used to generate the makefile for
	 * the project.
	 * <li>If a Folder is a centralstorage, then it is decorated with the
	 * "centralstorage" text.
	 * <li>If a File or folder is excluded from build, then it is decorated
	 * with the "excluded" text.
	 * </ul>
	 *
	 * @param text
	 *                the input text label to decorate
	 * @param element
	 *                the element whose image is being decorated
	 * @return the decorated text label, or <code>null</code> if no
	 *         decoration is to be applied
	 *
	 * @see ILabelDecorator#decorateText(String, Object)
	 * @see #propertiesAsParameters(IProject, boolean)
	 */
	@Override
	public String decorateText(final String text, final Object element) {
		if (!(element instanceof IResource)) {
			return text;
		}

		IResource baseResource = (IResource) element;
		if (!baseResource.isAccessible()) {
			return text;
		}

		IProject project = baseResource.getProject();
		if (project == null || !project.isAccessible() || !TITANNature.hasTITANNature(project)) {
			return text;
		}

		StringBuilder result = new StringBuilder(text);
		switch (baseResource.getType()) {
		case IResource.ROOT:
			break;
		case IResource.PROJECT:
			try {
				final IProject resource = (IProject) element;
				if (ExcludedResourceFilter.isActive() || WorkingDirectoryFilter.isActive()) {
					result.append("[filtered]");
				}

				if (TRUE_STRING.equals(resource.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
						ProjectBuildPropertyData.GENERATE_MAKEFILE_PROPERTY)))) {
					result.append(OPENING_PARENTHESIS);
					result.append(propertiesAsParameters(resource, false));
					result.append(CLOSING_PARENTHESIS);
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
			break;
		case IResource.FOLDER:
			try {
				final IFolder resource = (IFolder) element;
				if (TRUE_STRING.equals(resource.getPersistentProperty(new QualifiedName(FolderBuildPropertyData.QUALIFIER,
						FolderBuildPropertyData.CENTRAL_STORAGE_PROPERTY)))) {
					result.append(OPENING_PARENTHESIS);
					result.append(CENTRAL_STORAGE);
					result.append(CLOSING_PARENTHESIS);
				}
				if (ResourceExclusionHelper.isDirectlyExcluded(resource)) {
					result.append(OPENING_PARENTHESIS);
					result.append(EXCLUDED_BY_USER);
					result.append(CLOSING_PARENTHESIS);
				}
				if (helper.isExcludedByRegexp(resource.getName())) {
					result.append(OPENING_PARENTHESIS);
					result.append(EXCLUDED_BY_REGEXP);
					result.append(CLOSING_PARENTHESIS);
				}
				if (resource.getName().startsWith(".")) {
					result.append(OPENING_PARENTHESIS);
					result.append(EXCLUDED_BY_CONVENTION);
					result.append(CLOSING_PARENTHESIS);
				}
				if (resource.getName().contentEquals("Makefile") || resource.getName().contentEquals("makefile")) {
					result.append(OPENING_PARENTHESIS);
					result.append(EXCLUDED_BY_CONVENTION);
					result.append(CLOSING_PARENTHESIS);
				}

				final IContainer[] workingDirectories = ProjectBasedBuilder.getProjectBasedBuilder(resource.getProject())
						.getWorkingDirectoryResources(false);
				for (IContainer workingDirectory : workingDirectories) {
					if (workingDirectory.equals(resource)) {
						result.append(OPENING_PARENTHESIS);
						result.append(EXCLUDED_AS_WORKINGDIR);
						result.append(CLOSING_PARENTHESIS);
						break;
					}
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
			break;
		case IResource.FILE:
			final IFile resource = (IFile) element;
			if (ResourceExclusionHelper.isDirectlyExcluded(resource)) {
				result.append(OPENING_PARENTHESIS);
				result.append(EXCLUDED_BY_USER);
				result.append(CLOSING_PARENTHESIS);
			}
			if (helper.isExcludedByRegexp(resource.getName())) {
				result.append(OPENING_PARENTHESIS);
				result.append(EXCLUDED_BY_REGEXP);
				result.append(CLOSING_PARENTHESIS);
			}
			if (resource.getName().startsWith(".")) {
				result.append(OPENING_PARENTHESIS);
				result.append(EXCLUDED_BY_CONVENTION);
				result.append(CLOSING_PARENTHESIS);
			}
			if (resource.getName().contentEquals("Makefile") || resource.getName().contentEquals("makefile")) {
				result.append(OPENING_PARENTHESIS);
				result.append(EXCLUDED_BY_CONVENTION);
				result.append(CLOSING_PARENTHESIS);
			}
			break;
		default:
			break;
		}
		return result.toString();
	}

	/**
	 * This function returns the label decorator of a project.
	 * <ul>
	 * <li>If full is false, then only the command line switches are
	 * included in the result.
	 * <li>If full is true, then the whole command line argument is returned
	 * (for example this also includes the executable's name).
	 * </ul>
	 *
	 * @param project
	 *                The project whose decorating text we want.
	 * @param full
	 *                Whether the result should include the arguments of the
	 *                command line switches or not.
	 * @return The text to decorate the project resource, or the whole
	 *         command line.
	 */
	public static String propertiesAsParameters(final IProject project, final boolean full) {
		StringBuffer result = new StringBuffer();
		boolean pendingSpace = false;

		// FIXME this costs too much we should store the already
		// calculated data
		DecoratorVisitor visitor = new DecoratorVisitor();
		try {
			if (project.isAccessible()) {
				project.accept(visitor);
			}

			if (TRUE_STRING.equals(project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.USE_ABSOLUTEPATH_PROPERTY)))) {
				result.append(ABSOLUTE_PATH_OPTION);
			}

			int nofReferencedProjects = ProjectBasedBuilder.getProjectBasedBuilder(project).getReferencedProjects().length;
			boolean preProcessorOptionSet = false;

			if (visitor.getHasCentralStorage() || nofReferencedProjects != 0) {
				result.append(CENTRAL_STORAGE_OPTION);

				List<IProject> reachableProjects = ProjectBasedBuilder.getProjectBasedBuilder(project).getAllReachableProjects();
				IProject reachableProject;
				DecoratorVisitor visitor2;
				boolean prePorcessorOptionFound = false;
				for (int i = 0, size = reachableProjects.size(); i < size && !prePorcessorOptionFound; i++) {
					reachableProject = reachableProjects.get(i);
					visitor2 = new DecoratorVisitor();
					try {
						if (reachableProject.isAccessible()) {
							reachableProject.accept(visitor2);
							if (visitor2.getHasPreprecessable()) {
								prePorcessorOptionFound = true;
							}
						}
					} catch (CoreException e) {
						ErrorReporter.logExceptionStackTrace(e);
					}
				}

				if (prePorcessorOptionFound) {
					result.append(PREPROCESSOR_OPTION);
					preProcessorOptionSet = true;
				}
			}
			result.append(FORCE_OVERWRITE_OPTION);
			if (TRUE_STRING.equals(project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.GNU_MAKE_PROPERTY)))) {
				result.append(GNU_MAKE_OPTION);
			}
			if (visitor.getHasPreprecessable() && !preProcessorOptionSet) {
				result.append(PREPROCESSOR_OPTION);
			}
			if (TRUE_STRING.equals(project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.FUNCTIONTESTRUNTIME_PROPERTY)))) {
				result.append(FUNCTION_TEST_OPTION);
			}
			if (TRUE_STRING.equals(project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.DYNAMIC_LINKING_PROPERTY)))) {
				result.append(DYNAMIC_LINKING_OPTION);
			}
			if (TRUE_STRING.equals(project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.SINGLEMODE_PROPERTY)))) {
				result.append(SINGLE_MODE_OPTION);
			}
			if (result.length() > 0) {
				result.insert(0, '-');
				pendingSpace = true;
			}

			String targetExecutable = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.TARGET_EXECUTABLE_PROPERTY));
			if (targetExecutable != null && targetExecutable.length() != 0) {
				URI uri = TITANPathUtilities.resolvePath(targetExecutable, project.getLocationURI());
				targetExecutable = URIUtil.toPath(uri).toOSString();
				if (pendingSpace) {
					result.append(SPACE);
					pendingSpace = false;
				}
				result.append(TARGET_EXECUTABLE_OPTION);
				if (full) {
					boolean reportDebugInformation = Platform.getPreferencesService().getBoolean(
							ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION, false,
							null);
					result.append(SPACE + '\''
							+ PathConverter.convert(targetExecutable, reportDebugInformation,
									TITANDebugConsole.getConsole()) + '\'');
				}
				pendingSpace = true;
			} else if (full) {
				if (pendingSpace) {
					result.append(SPACE);
					pendingSpace = false;
				}
				result.append(TARGET_EXECUTABLE_OPTION);

				targetExecutable = project.getName();
				if (Platform.OS_WIN32.equals(Platform.getOS())) {
					targetExecutable += ".exe";
				}
				if (targetExecutable != null) {
					result.append(SPACE + '\'' + targetExecutable + '\'');
					// pendingSpace = true;
				}
				pendingSpace = true;
			}

			String codesplittingOption = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.CODE_SPLITTING_PROPERTY));
			if (codesplittingOption != null && codesplittingOption.length() != 0 && !GeneralConstants.NONE.equals(codesplittingOption)) {
				if (pendingSpace) {
					result.append(SPACE);
					pendingSpace = false;
				}
				result.append("-U");
				if (full) {
					result.append(SPACE + codesplittingOption);
				}
				pendingSpace = true;
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
		return result.toString();
	}

	/**
	 * Static function to reach the decorator from outside of it.
	 *
	 * @return The decorator instance
	 */
	private static TITANDecorator getDecorator() {
		if (!PlatformUI.isWorkbenchRunning()) {
			return null;
		}

		IDecoratorManager decoratorManager = Activator.getDefault().getWorkbench().getDecoratorManager();
		if (decoratorManager.getEnabled(DECORATOR_ID)) {
			return (TITANDecorator) decoratorManager.getLabelDecorator(DECORATOR_ID);
		}

		return null;
	}

	/**
	 * Reset the exclusion detection caches. Should only be called, when the
	 * exclusion property of some resources change.
	 * */
	public static void resetExclusion() {
		TITANDecorator decorator = getDecorator();
		if (decorator != null) {
			decorator.resetMatchers();
		}
	}

	/**
	 * Refresh the decorator for every object after some changes happened in
	 * the system.
	 *
	 * @see #fireLabelEvent(LabelProviderChangedEvent)
	 */
	public static void refreshAll() {
		TITANDecorator decorator = getDecorator();
		if (decorator != null) {
			decorator.fireLabelEvent(new LabelProviderChangedEvent(decorator));
		}
	}

	/**
	 * Refresh the decorator for a selected object after some changes
	 * happened in the system.
	 *
	 * @param resourceToRefresh
	 *                the resource whose decoration must be refreshed
	 * @see #fireLabelEvent(LabelProviderChangedEvent)
	 */
	public static void refreshSelectively(final IResource resourceToRefresh) {
		TITANDecorator decorator = getDecorator();
		if (decorator != null) {
			decorator.fireLabelEvent(new LabelProviderChangedEvent(decorator, resourceToRefresh));
		}
	}

	/**
	 * Refresh the decorator for every object contained in the list after
	 * some changes happened in the system.
	 *
	 * @param resourcesToRefresh
	 *                an array of resources whose decoration mustbe
	 *                refreshed
	 * @see #fireLabelEvent(LabelProviderChangedEvent)
	 */
	public static void refreshSelectively(final Object[] resourcesToRefresh) {
		TITANDecorator decorator = getDecorator();
		if (decorator != null) {
			decorator.fireLabelEvent(new LabelProviderChangedEvent(decorator, resourcesToRefresh));
		}
	}

	/**
	 * Fire a provider changed event, to make the decorator redraw itself.
	 *
	 * @param event
	 *                A fake LabelProviderChangedEvent.
	 *
	 * @see #refresh()
	 */
	private void fireLabelEvent(final LabelProviderChangedEvent event) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				fireLabelProviderChanged(event);
			}
		});
	}
}
