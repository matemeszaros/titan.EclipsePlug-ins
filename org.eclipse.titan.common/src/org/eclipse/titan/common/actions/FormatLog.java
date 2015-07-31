/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.titan.common.graphics.ImageCache;
import org.eclipse.titan.common.log.format.LogFormatter;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.path.TitanURIUtil;
import org.eclipse.titan.common.product.ProductConstants;
import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titan.common.utils.ResourceUtils;
import org.eclipse.titan.common.utils.SelectionUtils;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * @author Kristof Szabados
 * */
public final class FormatLog extends AbstractHandler implements IWorkbenchWindowActionDelegate {

	private List<IFile> files = new ArrayList<IFile>();

	@Override
	public void init(final IWorkbenchWindow window) {
		// Do nothing
	}

	@Override
	public void dispose() {
		files.clear();
	}

	/**
	 * Formats a single file.
	 *
	 * @param monitor the progress monitor to report progress.
	 * @param file the file file to format
	 * @param targetPath the path the result should be stored at.
	 * @param targetFiles the list of files that can be mapped to the target path.
	 * */
	private IStatus internalFormatter(final IProgressMonitor monitor, final IFile file, final URI targetPath, final IFile[] targetFiles) {
		ResourceUtils.refreshResources(Arrays.asList(file));

		final File source = new File(file.getLocationURI());
		final long fileSize = source.length();

		IProgressMonitor internalMonitor = monitor == null ? new NullProgressMonitor() : monitor;
		internalMonitor.beginTask("formatting " + file.getName(), (int) (fileSize / LogFormatter.TICK_SIZE));

		FileChannel inChannel = null;
		FileChannel outChannel = null;
		try {
			inChannel = new FileInputStream(source).getChannel();

			outChannel = openOutputFile(targetPath);

			new LogFormatter(internalMonitor, inChannel, outChannel).format();

		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace("Error while formatting log file: " + file.getLocation().toOSString(), e);
			return new Status(IStatus.ERROR, ProductConstants.PRODUCT_ID_COMMON, IStatus.OK, e.getMessage() != null ? e.getMessage() : "", e);
		} finally {
			IOUtils.closeQuietly(inChannel, outChannel);
		}

		if (targetFiles != null) {
			ResourceUtils.refreshResources(Arrays.asList(targetFiles));
		}

		internalMonitor.done();
		return Status.OK_STATUS;
	}

	private FileChannel openOutputFile(URI targetPath) throws FileNotFoundException {
		FileOutputStream outfile = null;
		try {
			outfile = new FileOutputStream(new File(targetPath), false);
		} catch (FileNotFoundException e) {
			String message = "Error while opening " + targetPath + " for writing";
			ErrorReporter.logExceptionStackTrace(message, e);
			throw new FileNotFoundException(message);
		}
		return outfile.getChannel();
	}

	@Override
	public void run(final IAction action) {
		doFormat();
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		files = SelectionUtils.getAccessibleFilesFromSelection(selection);
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		/** This is needed because AbstractHandler does not deal with selection, and
		 * selectionChanged is not called.
		*/
		IWorkbenchPage iwPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		ISelection selection = iwPage.getSelection();

		files = SelectionUtils.getAccessibleFilesFromSelection(selection);
		doFormat();
		return null;
	}

	public void formatFiles(List<IFile> files) {
		for (IFile file : files) {
			final URI targetPath = getTargetPath(file.getLocationURI());
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			final IFile[] targetFiles = root.findFilesForLocationURI(targetPath);
			internalFormatter(null, file, targetPath, targetFiles);
		}
	}

	private static URI getTargetPath(URI original) {
		String originalFileName = URIUtil.lastSegment(URIUtil.removeFileExtension(original));
		URI path = TitanURIUtil.removeLastSegment(original);

		String newFilename = originalFileName + "_formatted.log";
		return URIUtil.append(path, newFilename);
	}

	private void doFormat() {
		for (final IFile file : files) {
			final URI targetPath = getTargetPath(file.getLocationURI());

			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			final IFile[] targetFiles = root.findFilesForLocationURI(targetPath);

			WorkspaceJob saveJob = new WorkspaceJob("Formatting log file") {
				@Override
				public IStatus runInWorkspace(final IProgressMonitor monitor) {
					return internalFormatter(monitor, file, targetPath, targetFiles);
				}
			};

			saveJob.setRule(createRuleFromResources(file, targetFiles));
			saveJob.setPriority(Job.LONG);
			saveJob.setUser(true);
			saveJob.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
			saveJob.schedule();
		}
	}

	private ISchedulingRule createRuleFromResources(IFile file, IFile[] targetFiles) {
		IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
		ISchedulingRule rule1 = ruleFactory.createRule(file);
		ISchedulingRule combinedRule = MultiRule.combine(rule1, null);
		for (IFile targetFile : targetFiles) {
			combinedRule = MultiRule.combine(ruleFactory.createRule(targetFile), combinedRule);
		}
		return combinedRule;
	}
}
