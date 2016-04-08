/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.actions;

import java.io.File;
import java.io.FilenameFilter;
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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.titan.common.Activator;
import org.eclipse.titan.common.graphics.ImageCache;
import org.eclipse.titan.common.log.merge.LogMerger;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.preferences.PreferenceConstants;
import org.eclipse.titan.common.product.ProductConstants;
import org.eclipse.titan.common.utils.FileUtils;
import org.eclipse.titan.common.utils.ResourceUtils;
import org.eclipse.titan.common.utils.SelectionUtils;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * This action is able to merge the selected log files into one, that the user will select.
 *   
 *   @author Kristof Szabados
 * */
public final class MergeLog extends AbstractHandler implements IWorkbenchWindowActionDelegate {
	private static final String MERGED_FILENAME_SUFFIX = "_merged_";

	/** This persistent property will be set on each merged log file. */
	public static final QualifiedName MERGED_FILE_PROPERTY = new QualifiedName(ProductConstants.PRODUCT_ID_COMMON, "mergedFile");
	private static final String LOG_FILE_EXTENSION = ".log";

	private ISelection selection;

	private static File staticOutput;
	private File outputFile;
	private boolean showDialog = true;

	@Override
	public void init(final IWorkbenchWindow window) {
		//Do nothing
	}

	@Override
	public void dispose() {
		// Do nothing
	}

	private void setOutputFile(final File outputFile) {
		this.outputFile = outputFile;
		staticOutput = outputFile;
	}

	/**
	 * Merges the log files provided in the parameter list.
	 *
	 * @param filesToMerge the list of files to be merged.
	 * @param sync true if the function should wait for the merge to finish,
	 * 	false if the function should just start the parallel thread.
	 * */
	public void run(final List<IFile> filesToMerge, final boolean sync) {
		doMerge(filesToMerge, sync);
	}

	@Override
	public void run(final IAction action) {
		final List<IFile> files = SelectionUtils.getAccessibleFilesFromSelection(selection);
		doMerge(files, false);
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		this.selection = selection;
	}

	/**
	 * If the parameter is true, the user can choose the destination of the merged log file.
	 * Default value is true.
	 *
	 * @param isEnabled
	 *            if true the dialog will appear, otherwise a default file name and location will be used.
	 */
	public void setShowDialog(final boolean isEnabled) {
		showDialog = isEnabled;
	}

	/**
	 * Display the dialog where the user can select the file to be the
	 * target of the merge.
	 */
	private void displayOutputSelectionDialog() {
		final FileDialog dialog = new FileDialog(null, SWT.SAVE);
		if (staticOutput != null) {
			dialog.setFileName(staticOutput.getName());
			dialog.setFilterPath(staticOutput.getParent());
		}
		String outFile = dialog.open();
		if (outFile == null) {
			setOutputFile(null);
			return;
		}
		outFile = outFile.trim();
		if (outFile.length() > 0) {
			setOutputFile(new File(outFile));
		}
	}

	/**
	 * Initializes the output file depending on the preference options.
	 *
	 * @param originalFile
	 *            The log file to merge.
	 */
	private void initializeOutputFile(final IFile originalFile) {
		if (staticOutput == null) {
			staticOutput = createMergedFileName(originalFile).toFile();
		}

		if (showDialog) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					displayOutputSelectionDialog();
				}
			});

			return;
		}

		setOutputFile(staticOutput);
		if (!staticOutput.exists()) {
			return;
		}


		handleFileExists(originalFile);
	}

	private IPath createMergedFileName(final IFile originalFile) {
		// original loc
		IPath temp = originalFile.getLocation();
		// original ext
		final String extension = temp.getFileExtension();
		// original - ext
		temp = temp.removeFileExtension();
		// original name (-ext)
		final String filename = temp.lastSegment();
		// original -1 dir
		temp = temp.removeLastSegments(1);

		return temp.append(filename + "_merged").addFileExtension(extension);
	}

	private void handleFileExists(final IFile originalFile) {
		final IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();
		final String mergeOption = prefStore.getString(PreferenceConstants.LOG_MERGE_OPTIONS);

		if (PreferenceConstants.LOG_MERGE_OPTIONS_CREATE.equals(mergeOption)) {
			setOutputFile(createNewFileWithUniqueName(originalFile));
			return;
		} else if (PreferenceConstants.LOG_MERGE_OPTIONS_OVERWRITE.equals(mergeOption)) {
			FileUtils.deleteQuietly(outputFile);
			return;
		}

		// pop up the question to the user
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				askUserToCreateOrOverwrite(prefStore, originalFile);
			}
		});
	}

	private void askUserToCreateOrOverwrite(final IPreferenceStore prefStore, final IFile originalFile) {
		final String[] buttonLabels = new String[] {
				"Create a new file",
				"Overwrite" };

		final MessageDialogWithToggle msgDialog = new MessageDialogWithToggle(
				null,
				"File already exists",
				null,
				"An error occured during log file merging. The file '"
						+ outputFile.getName()
						+ "' already exists. "
						+ "Do you want to keep the original file and choose another location/name for the new one?",
				MessageDialog.NONE, buttonLabels, SWT.DEFAULT, "Don't ask again", false);

		msgDialog.setBlockOnOpen(true);

		final int result = msgDialog.open() - 256;

		if (result == SWT.DEFAULT) {
			// The dialog was closed
			setOutputFile(null);
			return;
		}

		// lets save the chosen option to the preference store if the user checked the 'Dont ask' checkbox
		final boolean dontAskChecked = msgDialog.getToggleState();
		if (dontAskChecked) {
			if (result == 0) {
				// create a new file pressed
				prefStore.setValue(PreferenceConstants.LOG_MERGE_OPTIONS, PreferenceConstants.LOG_MERGE_OPTIONS_CREATE);
			} else if (result == 1) {
				// overwrite
				prefStore.setValue(PreferenceConstants.LOG_MERGE_OPTIONS, PreferenceConstants.LOG_MERGE_OPTIONS_OVERWRITE);
			}
		} else {
			prefStore.setValue(PreferenceConstants.LOG_MERGE_OPTIONS, PreferenceConstants.LOG_MERGE_OPTIONS_ASK);
		}

		if (result == 0) {
			// create a new file pressed
			if (dontAskChecked) {
				setOutputFile(createNewFileWithUniqueName(originalFile));
				return;
			}
			displayOutputSelectionDialog();
		} else {
			// overwrite
			FileUtils.deleteQuietly(outputFile);
		}
	}

	/**
	 * This method creates a new file with a unique name.
	 * The name of the file will be <code>{LOGFILENAME}_merged_{XX}.log</code> where LOGFILENAME is the name of the original file
	 * and XX is a number greater than the one in the already existing file's name.
	 * e.g.: if we are merging the file <code>mylog.log</code> and <code>mylog_merged_42.log</code> already exists,
	 * then the name of the new file will be <code>mylog_merged_43.log</code>
	 *
	 * @param originalFile
	 *            the original log file
	 */
	private File createNewFileWithUniqueName(final IFile originalFile) {
		String temp;
		if (originalFile.getName().endsWith(LOG_FILE_EXTENSION)) {
			temp = originalFile.getName().substring(0, originalFile.getName().length() - LOG_FILE_EXTENSION.length())
					+ MERGED_FILENAME_SUFFIX;
		} else {
			temp = originalFile.getName() + MERGED_FILENAME_SUFFIX;
		}

		final String outputFileNamePrefix = temp;
		final File[] files = outputFile.getParentFile().listFiles(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				return name.startsWith(outputFileNamePrefix);
			}
		});

		int max = 0;
		for (final File file : files) {
			final String fileName = file.getName();
			String suffix = fileName.substring(outputFileNamePrefix.length());
			suffix = suffix.substring(0, suffix.length() - LOG_FILE_EXTENSION.length());

			try {
				final int number = Integer.parseInt(suffix);
				if (number > max) {
					max = number;
				}
			} catch (final NumberFormatException e) {
				// Do nothing
			}
		}

		final String newOutpufFileName = outputFileNamePrefix + (max + 1) + LOG_FILE_EXTENSION;
		return new File(outputFile.getParent(), newOutpufFileName);
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPage iwPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		selection = iwPage.getSelection();
		final List<IFile> filesToMerge = SelectionUtils.getAccessibleFilesFromSelection(selection);
		doMerge(filesToMerge, false);

		return null;
	}

	/**
	 * Merges the log files provided in the parameter list.
	 *
	 * @param files the list of files to be merged.
	 * @param sync true if the function should wait for the merge to finish,
	 * 	false if the function should just start the parallel thread.
	 * */
	private void doMerge(final List<IFile> files, final boolean sync) {
		if (files.size() < 2) {
			return;
		}

		ResourceUtils.refreshResources(files);

		final WorkspaceJob mergeJob = new WorkspaceJob("Merging log files") {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				initializeOutputFile(files.get(0));

				if (outputFile == null) {
					return Status.CANCEL_STATUS;
				}

				boolean isErroneous = !new LogMerger().merge(files, outputFile, monitor);

				if (isErroneous) {
					ErrorReporter.parallelErrorDisplayInMessageDialog(
							"Merging of log files failed", 
							"There were some errors while merging the selected log files.\n"
								+ "Please check the error log for more information.");
				}

				final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				final IFile[] outputFiles = root.findFilesForLocationURI(outputFile.toURI());
				refreshOutputFilesAsync(outputFiles);

				return Status.OK_STATUS;
			}
		};

		mergeJob.setRule(createSchedulingRule(files));
		mergeJob.setPriority(Job.LONG);
		mergeJob.setUser(true);
		mergeJob.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
		mergeJob.schedule();

		if (sync) {
			try {
				mergeJob.join();
			} catch (InterruptedException e) {
				ErrorReporter.logExceptionStackTrace("Interrupted", e);
			}
		}
	}

	private ISchedulingRule createSchedulingRule(final List<IFile> files) {
		final IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
		ISchedulingRule combinedRule = null;
		for (final IFile file : files) {
			combinedRule = MultiRule.combine(ruleFactory.createRule(file), combinedRule);
		}
		return combinedRule;
	}

	private void refreshOutputFilesAsync(final IFile[] outputFiles) {
		final WorkspaceJob mergeJob = new WorkspaceJob("Refreshing output file information") {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				if (outputFiles != null) {
					ResourceUtils.refreshResources(Arrays.asList(outputFiles));
					for (IFile file : outputFiles) {
						ResourceUtils.setPersistentProperty(file, MERGED_FILE_PROPERTY.getQualifier(), MERGED_FILE_PROPERTY.getLocalName(), true);
					}
				}

				return Status.OK_STATUS;
			}
		};

		final IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
		ISchedulingRule combinedRule = null;
		for (final IFile file : outputFiles) {
			combinedRule = MultiRule.combine(ruleFactory.createRule(file), combinedRule);
		}

		mergeJob.setRule(combinedRule);
		mergeJob.setPriority(Job.LONG);
		mergeJob.setUser(true);
		mergeJob.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
		mergeJob.schedule();
	}
}
