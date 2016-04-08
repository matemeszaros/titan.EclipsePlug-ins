/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.common.parsers.TITANMarker;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.MarkerHandler;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Module.module_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.core.LoadBalancingUtilities;
import org.eclipse.titan.designer.core.ProjectBasedBuilder;
import org.eclipse.titan.designer.core.TITANNature;
import org.eclipse.titan.designer.editors.EditorTracker;
import org.eclipse.titan.designer.editors.FoldingSupport;
import org.eclipse.titan.designer.editors.GlobalIntervalHandler;
import org.eclipse.titan.designer.editors.ISemanticTITANEditor;
import org.eclipse.titan.designer.editors.ttcnppeditor.TTCNPPEditor;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Analyzer;
import org.eclipse.titan.designer.parsers.ttcn3parser.ITtcn3FileReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3Analyzer;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3FileReparser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Helper class to separate the responsibility of the source parser into smaller
 * parts. This class is responsible for handling the syntactic checking of the
 * source code of the projects
 * 
 * @author Kristof Szabados
 * */
public final class ProjectSourceSyntacticAnalyzer {
	private final IProject project;
	private final ProjectSourceParser sourceParser;

	// file, module name
	private Map<IFile, String> uptodateFiles;
	// file : these are parsed, but contain such errors that we can not
	// determine even the module.
	private Set<IFile> highlySyntaxErroneousFiles;
	// file, module name might be outdated
	private Map<IFile, String> fileMap;
	// include files
	private Map<String, IFile> includeFileMap;
	Map<IFile, List<TITANMarker>> unsupportedConstructMap;

	private volatile boolean syntacticallyOutdated = true;

	private static final int NUMBER_OF_PROCESSORS = Runtime.getRuntime().availableProcessors();

	/**
	 * A helper class to store parsed data, which was generated in parse
	 * threads running in parallel, but must be processed in a given fixed
	 * order.
	 * */
	static final class TemporalParseData {
		private Module module;
		private IFile file;
		private List<TITANMarker> unsupportedConstructs;
		private boolean hadParseErrors;
		private IDocument document;

		public TemporalParseData(final Module module, final IFile file, final List<TITANMarker> unsupportedConstructs,
				final boolean hadParseErrors, final IDocument document) {
			this.module = module;
			this.file = file;
			this.unsupportedConstructs = unsupportedConstructs;
			this.hadParseErrors = hadParseErrors;
			this.document = document;
		}

		public Module getModule() {
			return module;
		}

		public IFile getFile() {
			return file;
		}

		public List<TITANMarker> getUnsupportedConstructs() {
			return unsupportedConstructs;
		}

		public boolean hadParseErrors() {
			return hadParseErrors;
		}

		public IDocument getDocument() {
			return document;
		}
	}

	public ProjectSourceSyntacticAnalyzer(final IProject project, final ProjectSourceParser sourceParser) {
		this.project = project;
		this.sourceParser = sourceParser;

		uptodateFiles = new ConcurrentHashMap<IFile, String>();
		highlySyntaxErroneousFiles = Collections.synchronizedSet(new HashSet<IFile>());
		fileMap = new ConcurrentHashMap<IFile, String>();
		includeFileMap = new ConcurrentHashMap<String, IFile>();
		unsupportedConstructMap = new ConcurrentHashMap<IFile, List<TITANMarker>>();
	}

	/**
	 * Checks whether the internal data belonging to the provided file is
	 * syntactically out-dated.
	 * <p>
	 * If the project is syntactically out-dated, all files are handled as
	 * out-dated.
	 *
	 * @param file
	 *                the file to check.
	 *
	 * @return true if the data was reported to be out-dated since the last
	 *         analysis.
	 * */
	public boolean isOutdated(final IFile file) {
		return syntacticallyOutdated || !uptodateFiles.containsKey(file);
	}

	/**
	 * Returns the name of the module contained in the provided file, or
	 * null.
	 *
	 * @param file
	 *                the file whose module we are interested in
	 *
	 * @return the name of the module found in the file, or null
	 * */
	public String containedModule(final IFile file) {
		return fileMap.get(file);
	}

	/**
	 * Handles the renaming of a module.
	 *
	 * @param oldname
	 *                the old name of the module.
	 * @param newName
	 *                the new name of the module.
	 * */
	public void renameModule(final String oldname, final String newName) {
		IFile file = null;
		for (IFile key : fileMap.keySet()) {
			if (fileMap.get(key).equals(oldname)) {
				file = key;
			}
		}

		if (file == null) {
			return;
		}

		fileMap.put(file, newName);
		if (uptodateFiles.containsKey(file)) {
			uptodateFiles.put(file, newName);
		}

		sourceParser.getSemanticAnalyzer().renameModule(oldname, newName, file);
	}

	/**
	 * Reports that the provided file has changed and so it's stored
	 * information became out of date.
	 * <p>
	 * Stores that this file is out of date for later usage
	 * <p>
	 *
	 * @param outdatedFile
	 *                the file which seems to have changed
	 * */
	public void reportOutdating(final IFile outdatedFile) {
		final IPreferencesService service = Platform.getPreferencesService();
		final boolean useOnTheFlyParsing = service.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.USEONTHEFLYPARSING,
				true, null);

		synchronized (this) {
			syntacticallyOutdated = true;
			if (uptodateFiles.containsKey(outdatedFile)) {
				uptodateFiles.remove(outdatedFile);
				unsupportedConstructMap.remove(outdatedFile);
			}
			if (highlySyntaxErroneousFiles.contains(outdatedFile)) {
				highlySyntaxErroneousFiles.remove(outdatedFile);
			}
			sourceParser.getSemanticAnalyzer().reportOutdating(outdatedFile, useOnTheFlyParsing);
		}
	}

	/**
	 * Reports that the provided file has changed and so it's stored
	 * information became out of date.
	 * <p>
	 * Stores that this file is out of date for later usage, but leaves the
	 * semantic information intact.
	 * <p>
	 *
	 * @param outdatedFile
	 *                the file which seems to have changed
	 * */
	public void reportSyntacticOutdatingOnly(final IFile outdatedFile) {
		synchronized (this) {
			syntacticallyOutdated = true;
			if (uptodateFiles.containsKey(outdatedFile)) {
				uptodateFiles.remove(outdatedFile);
				unsupportedConstructMap.remove(outdatedFile);
			}
			if (highlySyntaxErroneousFiles.contains(outdatedFile)) {
				highlySyntaxErroneousFiles.remove(outdatedFile);
			}
		}
	}

	/**
	 * Reports that the contents of the provided folder has changed and so
	 * it's stored information became out of date.
	 * <p>
	 * Stores that every file in this folder is out of date for later
	 * <p>
	 *
	 * @param outdatedFolder
	 *                the folder whose files seems to have changed
	 * */
	public void reportOutdating(final IFolder outdatedFolder) {
		final IPath folderPath = outdatedFolder.getProjectRelativePath();
		final IPreferencesService service = Platform.getPreferencesService();
		final boolean useOnTheFlyParsing = service.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.USEONTHEFLYPARSING,
				true, null);

		synchronized (this) {
			syntacticallyOutdated = true;
			for (Iterator<IFile> iterator = uptodateFiles.keySet().iterator(); iterator.hasNext();) {
				IFile tempFile = iterator.next();
				IPath filepath = tempFile.getProjectRelativePath();
				if (folderPath.isPrefixOf(filepath)) {
					sourceParser.getSemanticAnalyzer().reportOutdating(tempFile, useOnTheFlyParsing);
					iterator.remove();
					unsupportedConstructMap.remove(tempFile);
				}
			}
			for (Iterator<IFile> iterator = highlySyntaxErroneousFiles.iterator(); iterator.hasNext();) {
				IFile tempFile = iterator.next();
				IPath filepath = tempFile.getProjectRelativePath();
				if (folderPath.isPrefixOf(filepath)) {
					iterator.remove();
				}
			}
		}
	}

	/**
	 * Removes data related to modules, that were deleted or moved.
	 **/
	private void removedReferencestoRemovedFiles() {
		List<IFile> filesToRemove = new ArrayList<IFile>();
		for (IFile file : fileMap.keySet()) {
			if (!file.isAccessible()) {
				uptodateFiles.remove(file);
				highlySyntaxErroneousFiles.remove(file);
				String moduleName = fileMap.get(file);
				filesToRemove.add(file);

				sourceParser.getSemanticAnalyzer().removedReferencestoRemovedFiles(file, moduleName);
			}
		}

		for (IFile file : filesToRemove) {
			fileMap.remove(file);
			unsupportedConstructMap.remove(file);

			MarkerHandler.markAllMarkersForRemoval(file);
			MarkerHandler.removeAllMarkedMarkers(file);
		}
	}

	/**
	 * The entry point of incremental parsing.
	 * <p>
	 * Handles the data storages, calls the module level incremental parser
	 * on the file, and if everything fails does a full parsing to correct
	 * possibly invalid states.
	 *
	 * @param file
	 *                the edited file
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * */
	public void updateSyntax(final IFile file, final TTCN3ReparseUpdater reparser) {
		if (uptodateFiles.containsKey(file)) {
			final String moduleName = uptodateFiles.get(file);
			Module module = sourceParser.getSemanticAnalyzer().internalGetModuleByName(moduleName, false);
			sourceParser.getSemanticAnalyzer().reportSemanticOutdating(file);

			if (module != null && module_type.TTCN3_MODULE.equals(module.getModuletype())) {
				try {
					reparser.setUnsupportedConstructs(unsupportedConstructMap);
					try {
						((TTCN3Module) module).updateSyntax(reparser, sourceParser);
						reparser.updateLocation(((TTCN3Module) module).getLocation());
						MarkerHandler.markAllOnTheFlyMarkersForRemoval(file, reparser.getDamageStart(), reparser.getDamageEnd());
					} catch (ReParseException e) {
						syntacticallyOutdated = true;

						uptodateFiles.remove(file);
						sourceParser.getSemanticAnalyzer().reportSemanticOutdating(file);
						String oldModuleName = fileMap.get(file);
						if (oldModuleName != null) {
							sourceParser.getSemanticAnalyzer().removeModule(oldModuleName);
							fileMap.remove(file);
						}
						unsupportedConstructMap.remove(file);

						reparser.maxDamage();
						
						ITtcn3FileReparser r = new Ttcn3FileReparser( reparser, file, sourceParser, fileMap, uptodateFiles, highlySyntaxErroneousFiles );
						syntacticallyOutdated = r.parse();

					}

					MarkerHandler.removeAllOnTheFlySyntacticMarkedMarkers(file);
					//update the position of the markers located after the damaged region
					MarkerHandler.updateMarkers(file, reparser.getFirstLine(), reparser.getLineShift(), reparser.getDamageEnd(), reparser.getShift());
				} catch (Exception e) {
					// This catch is extremely important, as
					// it is supposed to protect the project
					// parser, from whatever might go wrong
					// inside the analysis.
					ErrorReporter.logExceptionStackTrace(e);
				}
			} else {
				reportOutdating(file);
			}
		} else if (highlySyntaxErroneousFiles.contains(file)) {
			reportOutdating(file);
		} else {
			MarkerHandler.markAllMarkersForRemoval(file);
			TemporalParseData temp = fileBasedTTCN3Analysis(file);
			postFileBasedGeneralAnalysis(temp);
		}

		reparser.reportSyntaxErrors();
	}

	/**
	 * Internal function. Used by delayed semantic check. It differs from
	 * the base implementation in that it does not remove semantic markers,
	 * and does not try to refresh semantic structures.
	 *
	 * @param monitor
	 *                the progress monitor to provide feedback to the user
	 *                about the progress.
	 *
	 * @return the status of the operation when it finished.
	 * */
	synchronized IStatus internalDoAnalyzeSyntactically2(final IProgressMonitor monitor) {
		if (!project.isAccessible() || !TITANNature.hasTITANNature(project)) {
			return Status.CANCEL_STATUS;
		}
		MessageConsoleStream stream = TITANDebugConsole.getConsole().newMessageStream();
		monitor.beginTask("On-the-fly syntactic checking of project: " + project.getName(), 1);

		if (syntacticallyOutdated) {
			syntacticallyOutdated = false;

			long absoluteStart = System.nanoTime();
			IPreferencesService preferenceService = Platform.getPreferencesService();
			boolean reportDebugInformation = preferenceService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
					PreferenceConstants.DISPLAYDEBUGINFORMATION, true, null);

			removedReferencestoRemovedFiles();

			final IContainer[] workingDirectories = ProjectBasedBuilder.getProjectBasedBuilder(project).getWorkingDirectoryResources(
					false);

			OutdatedFileCollector visitor = new OutdatedFileCollector(workingDirectories, uptodateFiles, highlySyntaxErroneousFiles);
			try {
				project.accept(visitor);
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
			final List<IFile> ttcn3FilesToCheck = visitor.getTTCN3FilesToCheck();
			final List<IFile> asn1FilesToCheck = visitor.getASN1FilesToCheck();

			List<IFile> allCheckedFiles = new ArrayList<IFile>();

			allCheckedFiles.addAll(uptodateFiles.keySet());

			// remove all markers from the files that need to be
			// parsed
			for (IFile file : ttcn3FilesToCheck) {
				MarkerHandler.markAllMarkersForRemoval(file, GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER);
				MarkerHandler.markAllTaskMarkersForRemoval(file);
			}
			allCheckedFiles.addAll(ttcn3FilesToCheck);

			for (IFile file : asn1FilesToCheck) {
				MarkerHandler.markAllMarkersForRemoval(file, GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER);
				MarkerHandler.markAllTaskMarkersForRemoval(file);
			}
			allCheckedFiles.addAll(asn1FilesToCheck);

			// parsing the files
			final SubProgressMonitor parseMonitor = new SubProgressMonitor(monitor, 1);
			parseMonitor.beginTask("Parse", ttcn3FilesToCheck.size() + asn1FilesToCheck.size());

			final ThreadPoolExecutor executor = new ThreadPoolExecutor(NUMBER_OF_PROCESSORS, NUMBER_OF_PROCESSORS, 10, TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>());
			executor.setThreadFactory(new ThreadFactory() {
				@Override
				public Thread newThread(final Runnable r) {
					Thread t = new Thread(r);
					t.setPriority(LoadBalancingUtilities.getThreadPriority());
					return t;
				}
			});
			final TemporalParseData[] tempResults = new TemporalParseData[ttcn3FilesToCheck.size() + asn1FilesToCheck.size()];
			int nofFilesProcessed = 0;

			final CountDownLatch latch = new CountDownLatch(ttcn3FilesToCheck.size() + asn1FilesToCheck.size());
			for (IFile file : ttcn3FilesToCheck) {
				// parse a file only if the operation was not
				// canceled and the file is not yet up-to-date
				if (monitor.isCanceled()) {
					parseMonitor.done();
					monitor.done();
					return Status.CANCEL_STATUS;
				} else if (!file.isAccessible()) {
					if (reportDebugInformation) {
						TITANDebugConsole.println("The file " + file.getLocationURI() + " does not seem to exist.",stream);
					}
					latch.countDown();
				} else if (!uptodateFiles.containsKey(file) && !highlySyntaxErroneousFiles.contains(file)) {
					parseMonitor.subTask("Syntactically analyzing file: " + file.getProjectRelativePath().toOSString());
					// parse the contents of the file
					final IFile tempFile = file;
					final int index = nofFilesProcessed;
					nofFilesProcessed++;
					executor.execute(new Runnable() {
						@Override
						public void run() {
							if (monitor.isCanceled()) {
								return;
							}

							TemporalParseData temp = fileBasedTTCN3Analysis(tempFile);
							tempResults[index] = temp;
							latch.countDown();
							parseMonitor.worked(1);

							LoadBalancingUtilities.syntaxAnalyzerProcessedAFile();
						}
					});
				}
			}

			ttcn3FilesToCheck.clear();

			for (IFile file : asn1FilesToCheck) {
				// parse a file only if the operation was not
				// canceled and the file is not yet up-to-date
				if (monitor.isCanceled()) {
					parseMonitor.done();
					monitor.done();
					return Status.CANCEL_STATUS;
				} else if (!file.isAccessible()) {
					if (reportDebugInformation) {
						TITANDebugConsole.println("The file " + file.getLocationURI() + " does not seem to exist.",stream);
					}
					latch.countDown();
				} else if (!uptodateFiles.containsKey(file) && !highlySyntaxErroneousFiles.contains(file)) {
					parseMonitor.subTask("Syntactically analyzing file: " + file.getProjectRelativePath().toOSString());
					// parse the contents of the file
					final IFile tempFile = file;
					final int index = nofFilesProcessed;
					nofFilesProcessed++;
					executor.execute(new Runnable() {
						@Override
						public void run() {
							if (monitor.isCanceled()) {
								return;
							}

							TemporalParseData temp = fileBasedASN1Analysis(tempFile);
							tempResults[index] = temp;
							latch.countDown();
							parseMonitor.worked(1);

							LoadBalancingUtilities.syntaxAnalyzerProcessedAFile();
						}
					});
				}
			}

			try {
				latch.await();
			} catch (InterruptedException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
			executor.shutdown();
			try {
				executor.awaitTermination(30, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
			executor.shutdownNow();

			MarkerHandler.removeAllOnTheFlyMarkedMarkers(project);

			parseMonitor.done();
			asn1FilesToCheck.clear();
			if (reportDebugInformation) {
				//MessageConsoleStream stream = TITANDebugConsole.getConsole().newMessageStream();
				TITANDebugConsole.println("  **It took " + (System.nanoTime() - absoluteStart) * (1e-9) + " seconds till the files ("
								+ uptodateFiles.size() + " pieces) of project " + project.getName()
								+ " got syntactically analyzed",stream);
			}
		} else {
			monitor.worked(1);
			monitor.done();
		}

		return Status.OK_STATUS;
	}

	void removeTTCNPPFilesIndirectlyModifiedByTTCNINFiles() {
		ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(project);
		Set<String> moduleNames = projectSourceParser.getKnownModuleNames();
		for (String moduleName : moduleNames) {
			Module module = projectSourceParser.getModuleByName(moduleName);
			if (module == null || !(module instanceof TTCN3Module)) {
				continue;
			}
			TTCN3Module ttcnppModule = (TTCN3Module) module;
			Set<IFile> includedFiles = ttcnppModule.getIncludedFiles();
			if (includedFiles == null || includedFiles.isEmpty()) {
				continue;
			}
			boolean isTTCNPPupToDate = true;
			for (IFile f : includedFiles) {
				if (!uptodateFiles.containsKey(f)) {
					isTTCNPPupToDate = false;
					break;
				}
			}
			if (!isTTCNPPupToDate) {
				uptodateFiles.remove(ttcnppModule.getLocation().getFile());
			}
		}
	}

	/**
	 * Internal function.
	 *
	 * @param monitor
	 *                the progress monitor to provide feedback to the user
	 *                about the progress.
	 *
	 * @return the status of the operation when it finished.
	 * */
	synchronized IStatus internalDoAnalyzeSyntactically(final IProgressMonitor monitor) {
		if (!project.isAccessible() || !TITANNature.hasTITANNature(project)) {
			return Status.CANCEL_STATUS;
		}

		MessageConsoleStream stream = TITANDebugConsole.getConsole().newMessageStream();
		monitor.beginTask("On-the-fly syntactic checking of project: " + project.getName(), 1);

		if (syntacticallyOutdated) {
			syntacticallyOutdated = false;

			long absoluteStart = System.nanoTime();
			IPreferencesService preferenceService = Platform.getPreferencesService();
			boolean reportDebugInformation = preferenceService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
					PreferenceConstants.DISPLAYDEBUGINFORMATION, true, null);

			removedReferencestoRemovedFiles();

			removeTTCNPPFilesIndirectlyModifiedByTTCNINFiles();

			final IContainer[] workingDirectories = ProjectBasedBuilder.getProjectBasedBuilder(project).getWorkingDirectoryResources(
					false);

			OutdatedFileCollector visitor = new OutdatedFileCollector(workingDirectories, uptodateFiles, highlySyntaxErroneousFiles);
			try {
				project.accept(visitor);
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
			final List<IFile> ttcn3FilesToCheck = visitor.getTTCN3FilesToCheck();
			final List<IFile> asn1FilesToCheck = visitor.getASN1FilesToCheck();
			final List<IFile> ttcninFilesModified = visitor.getTtcninFilesModified();

			// nothing to do with these files
			for (IFile f : ttcninFilesModified) {
				uptodateFiles.put(f, f.getName());
				includeFileMap.put(f.getName(), f);
			}

			List<IFile> allCheckedFiles = new ArrayList<IFile>();

			// remove the semantic markers from all of the uptodate
			// files;
			for (IFile file : uptodateFiles.keySet()) {
				MarkerHandler.markMarkersForRemoval(GeneralConstants.ONTHEFLY_SEMANTIC_MARKER, file);
				MarkerHandler.markMarkersForRemoval(GeneralConstants.ONTHEFLY_MIXED_MARKER, file);
			}
			allCheckedFiles.addAll(uptodateFiles.keySet());

			// remove all markers from the files that need to be
			// parsed
			for (IFile file : ttcn3FilesToCheck) {
				MarkerHandler.markAllOnTheFlyMarkersForRemoval(file);
				MarkerHandler.markAllTaskMarkersForRemoval(file);

				if (!fileMap.containsKey(file)) {
					ParserMarkerSupport.removeAllCompilerMarkers(file);
					ParserMarkerSupport.removeAllOnTheFlyMarkers(file);
				}
			}
			allCheckedFiles.addAll(ttcn3FilesToCheck);

			for (IFile file : asn1FilesToCheck) {
				MarkerHandler.markAllOnTheFlyMarkersForRemoval(file);
				MarkerHandler.markAllTaskMarkersForRemoval(file);

				if (!fileMap.containsKey(file)) {
					ParserMarkerSupport.removeAllCompilerMarkers(file);
					ParserMarkerSupport.removeAllOnTheFlyMarkers(file);
				}
			}
			allCheckedFiles.addAll(asn1FilesToCheck);

			// parsing the files
			final SubProgressMonitor parseMonitor = new SubProgressMonitor(monitor, 1);
			parseMonitor.beginTask("Parse", ttcn3FilesToCheck.size() + asn1FilesToCheck.size());

			final ThreadPoolExecutor executor = new ThreadPoolExecutor(NUMBER_OF_PROCESSORS, NUMBER_OF_PROCESSORS, 10, TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>());
			executor.setThreadFactory(new ThreadFactory() {
				@Override
				public Thread newThread(final Runnable r) {
					Thread t = new Thread(r);
					t.setPriority(LoadBalancingUtilities.getThreadPriority());
					return t;
				}
			});
			final TemporalParseData[] tempResults = new TemporalParseData[ttcn3FilesToCheck.size() + asn1FilesToCheck.size()];
			int nofFilesProcessed = 0;

			final CountDownLatch latch = new CountDownLatch(ttcn3FilesToCheck.size() + asn1FilesToCheck.size());
			for (IFile file : ttcn3FilesToCheck) {
				// parse a file only if the operation was not
				// canceled and the file is not yet up-to-date
				if (monitor.isCanceled()) {
					parseMonitor.done();
					monitor.done();
					return Status.CANCEL_STATUS;
				} else if (!file.isAccessible()) {
					if (reportDebugInformation) {
						TITANDebugConsole.println("The file " + file.getLocationURI() + " does not seem to exist.",stream);
					}
					latch.countDown();
				} else if (!uptodateFiles.containsKey(file) && !highlySyntaxErroneousFiles.contains(file)) {
					// Checked whether the linked file
					// exists at all, if no continue
					if (file.isLinked()) {
						File f = new File(file.getLocation().toOSString());
						if (!f.exists()) {
							if (reportDebugInformation) {
								//MessageConsoleStream stream = TITANDebugConsole.getConsole().newMessageStream();
								TITANDebugConsole.println("The file " + file.getLocationURI()
												+ " does not seem to exist.",stream);
							}
							latch.countDown();
							continue;
						}
					}

					parseMonitor.subTask("Syntactically analyzing file: " + file.getProjectRelativePath().toOSString());
					// parse the contents of the file
					final IFile tempFile = file;
					final int index = nofFilesProcessed;
					nofFilesProcessed++;
					executor.execute(new Runnable() {
						@Override
						public void run() {
							if (monitor.isCanceled()) {
								latch.countDown();
								parseMonitor.worked(1);
								return;
							}

							try {
								TemporalParseData temp = fileBasedTTCN3Analysis(tempFile);
								tempResults[index] = temp;
							} finally {
								latch.countDown();
								parseMonitor.worked(1);

								LoadBalancingUtilities.syntaxAnalyzerProcessedAFile();
							}
						}
					});
				}
			}

			ttcn3FilesToCheck.clear();

			for (IFile file : asn1FilesToCheck) {
				// parse a file only if the operation was not
				// canceled and the file is not yet up-to-date
				if (monitor.isCanceled()) {
					parseMonitor.done();
					monitor.done();
					return Status.CANCEL_STATUS;
				} else if (!file.isAccessible()) {
					if (reportDebugInformation) {
						TITANDebugConsole.println("The file " + file.getLocationURI() + " does not seem to exist.",stream);
					}
					latch.countDown();
				} else if (!uptodateFiles.containsKey(file) && !highlySyntaxErroneousFiles.contains(file)) {
					parseMonitor.subTask("Syntactically analyzing file: " + file.getProjectRelativePath().toOSString());
					// parse the contents of the file
					final IFile tempFile = file;
					final int index = nofFilesProcessed;
					nofFilesProcessed++;
					executor.execute(new Runnable() {
						@Override
						public void run() {
							if (monitor.isCanceled()) {
								latch.countDown();
								parseMonitor.worked(1);
								return;
							}

							try {
								TemporalParseData temp = fileBasedASN1Analysis(tempFile);
								tempResults[index] = temp;
							} finally {
								latch.countDown();
								parseMonitor.worked(1);

								LoadBalancingUtilities.syntaxAnalyzerProcessedAFile();
							}
						}
					});
				}
			}

			try {
				latch.await();
			} catch (InterruptedException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
			executor.shutdown();
			try {
				executor.awaitTermination(30, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
			executor.shutdownNow();

			for (TemporalParseData temp : tempResults) {
				if (temp != null) {
					postFileBasedGeneralAnalysis(temp);
				}
			}

			parseMonitor.done();
			asn1FilesToCheck.clear();
			if (reportDebugInformation) {
				//MessageConsoleStream stream = TITANDebugConsole.getConsole().newMessageStream();
				TITANDebugConsole.println("  **It took " + (System.nanoTime() - absoluteStart) * (1e-9) + " seconds till the files ("
								+ uptodateFiles.size() + " pieces) of project " + project.getName()
								+ " got syntactically analyzed",stream);
			}
		} else {
			monitor.worked(1);
			monitor.done();
		}

		return Status.OK_STATUS;
	}

	/**
	 * Parses the provided file.
	 *
	 * @param file
	 *                the file to be parsed
	 *
	 * @return the temporal data structure needed to insert the parsed
	 *         module in the list of modules, in the post-analyzes step.
	 * */
	private TemporalParseData fileBasedTTCN3Analysis(final IFile file) {
		return fileBasedGeneralAnalysis(file, new TTCN3Analyzer());
	}

	/**
	 * Parses the provided file.
	 *
	 * @param file
	 *                the file to be parsed
	 *
	 * @return the temporal data structure needed to insert the parsed
	 *         module in the list of modules, in the post-analyzes step.
	 * */
	private TemporalParseData fileBasedASN1Analysis(final IFile file) {
		return fileBasedGeneralAnalysis(file, new ASN1Analyzer());
	}

	/**
	 * Parses the provided file.
	 *
	 * @param file
	 *                the file to be parsed
	 * @param analyzer
	 *                the source code analyzer that should be used to
	 *                analyze the code
	 *
	 * @return the temporal data structure needed to insert the parsed
	 *         module in the list of modules, in the post-analyzes step.
	 * */
	private TemporalParseData fileBasedGeneralAnalysis(final IFile file, final ISourceAnalyzer analyzer) {
		if (analyzer == null) {
			return null;
		}

		IDocument document = null;
		ISemanticTITANEditor editor = null;
		List<ISemanticTITANEditor> editors = null;
		if (EditorTracker.containsKey(file)) {
			editors = EditorTracker.getEditor(file);
			editor = editors.get(0);
			document = editor.getDocument();
		}

		unsupportedConstructMap.remove(file);

		try {
			analyzer.parse(file, document == null ? null : document.get());
		} catch (FileNotFoundException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		final boolean hadParseErrors = processParserErrors(file, analyzer);
		
		List<TITANMarker> warnings = analyzer.getWarnings();
		List<TITANMarker> unsupportedConstructs = analyzer.getUnsupportedConstructs();
		Module module = analyzer.getModule();
		
		if (warnings != null) {
			for (TITANMarker marker : warnings) {
				if (file.isAccessible()) {
					Location location = new Location(file, marker.getLine(), marker.getOffset(), marker.getEndOffset());
					location.reportExternalProblem(marker.getMessage(), marker.getSeverity(),
							GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER);
				}
			}
		}

		if (document != null) {
			GlobalIntervalHandler.putInterval(document, analyzer.getRootInterval());
		}

		if (document != null && editors != null && analyzer.getRootInterval() != null) {
			final List<Position> positions = (new FoldingSupport()).calculatePositions(document);
			final List<ISemanticTITANEditor> editors2 = editors;

			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					for (ISemanticTITANEditor editor : editors2) {
						editor.updateFoldingStructure(positions);
						editor.invalidateTextPresentation();
					}
				}
			});
		}

		// add annotations on inactive code
		if (module != null && module instanceof TTCN3Module && document != null && editors != null) {
			final TTCN3Module ttcnModule = (TTCN3Module) module;
			final List<ISemanticTITANEditor> editors2 = editors;
			final List<Location> icList = ttcnModule.getInactiveCodeLocations();
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					for (ISemanticTITANEditor editor : editors2) {
						if (editor instanceof TTCNPPEditor) {
							((TTCNPPEditor) editor).updateInactiveCodeAnnotations(icList);
						}
					}
				}
			});
		}

		return new TemporalParseData(module, file, unsupportedConstructs, hadParseErrors, document);
	}
	
	/**
	 * Handle the errors in fileBasedGeneralAnalysis(), and add them to the markers
	 * @param aFile the parsed file
	 * @param aAnalyzer analyzer, that collected the errors
	 * @return true if it had parse errors
	 */
	private boolean processParserErrors(final IFile aFile, final ISourceAnalyzer aAnalyzer) {
		List<SyntacticErrorStorage> errors = null;

		errors = aAnalyzer.getErrorStorage();
		
		if (errors != null) {
			for (int i = 0; i < errors.size(); i++) {
				ParserMarkerSupport.createOnTheFlySyntacticMarker(aFile, errors.get(i), IMarker.SEVERITY_ERROR);
			}
		}

		return errors != null && !errors.isEmpty();
	}

	/**
	 * Uses the parsed data structure to decide if the module found can be
	 * inserted into the list of known modules. And inserts it if possible.
	 *
	 * @param parsedData
	 *                the parsed data to insert into the semantic database.
	 * */
	private void postFileBasedGeneralAnalysis(final TemporalParseData parsedData) {
		final Module module = parsedData.getModule();
		if (module != null && module.getIdentifier() != null) {
			if (!sourceParser.getSemanticAnalyzer().addModule(module)) {
				syntacticallyOutdated = true;
				return;
			}

			final IFile file = parsedData.getFile();
			fileMap.put(file, module.getName());

			final List<TITANMarker> unsupportedConstructs = parsedData.getUnsupportedConstructs();
			if (unsupportedConstructs != null && !unsupportedConstructs.isEmpty()) {
				unsupportedConstructMap.put(file, unsupportedConstructs);
			}

			if (module.getLocation().getEndOffset() == -1 && parsedData.hadParseErrors()) {
				if (parsedData.getDocument() == null) {
					module.getLocation().setEndOffset((int) new File(file.getLocationURI()).length());
				} else {
					module.getLocation().setEndOffset(parsedData.getDocument().getLength());
				}
			}

			uptodateFiles.put(file, module.getName());
		} else {
			syntacticallyOutdated = true;
			highlySyntaxErroneousFiles.add(parsedData.getFile());
		}
	}

	/**
	 * Returns the TTCN-3 include file with the provided name, or null.
	 * 
	 * @param name
	 *                the file name to return.
	 * @param uptodateOnly
	 *                allow finding only the up-to-date modules.
	 * 
	 * @return the file handler having the provided name
	 * */
	IFile internalGetTTCN3IncludeFileByName(final String name) {
		if (includeFileMap.containsKey(name)) {
			return includeFileMap.get(name);
		}
		return null;
	}
}
