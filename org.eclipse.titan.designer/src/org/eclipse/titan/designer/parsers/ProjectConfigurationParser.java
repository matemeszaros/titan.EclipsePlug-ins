/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.common.parsers.TITANMarker;
import org.eclipse.titan.common.parsers.cfg.CfgAnalyzer;
import org.eclipse.titan.common.parsers.cfg.CfgDefinitionInformation;
import org.eclipse.titan.common.parsers.cfg.CfgLocation;
import org.eclipse.titan.common.parsers.cfg.CfgParseResult;
import org.eclipse.titan.common.parsers.cfg.CfgParseResult.Macro;
import org.eclipse.titan.common.path.PathConverter;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.MarkerHandler;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.core.ProjectBasedBuilder;
import org.eclipse.titan.designer.core.TITANNature;
import org.eclipse.titan.designer.editors.EditorTracker;
import org.eclipse.titan.designer.editors.GlobalIntervalHandler;
import org.eclipse.titan.designer.editors.ISemanticTITANEditor;
import org.eclipse.titan.designer.editors.configeditor.ConfigEditor;
import org.eclipse.titan.designer.editors.configeditor.ConfigFoldingSupport;
import org.eclipse.titan.designer.editors.configeditor.ConfigTextEditor;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * This is project level root of all parsing related activities. Every data that
 * was extracted from files while parsing them has its root here.
 * <p>
 * In not noted elsewhere all operations that modify the internal states are
 * executed in a parallel WorkspaceJob, which will have scheduling rules
 * required to access related resources.
 *
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ProjectConfigurationParser {
	private static final String SOURCE_ANALYSING = "Analysing the config file";
	private static final String PARSING = "parsing";
	private IProject project;
	protected Map<IFile, String> uptodateFiles;
	private Set<IFile> highlySyntaxErroneousFiles;
	protected Map<IFile, String> fileMap;
	protected Map<String, CfgDefinitionInformation> definitions;

	/**
	 * Counts how many parallel analyzer threads are running. Should not be
	 * more than 2. It can be 2 if there were changes while the existing
	 * analyzes run, which have to be checked by a subsequent check.
	 * */
	private AtomicInteger analyzersRunning = new AtomicInteger();
	// The workspacejob of the last registered full analysis. External users
	// might need this to synchronize to.
	private volatile WorkspaceJob lastAnalyzes = null;

	/**
	 * Basic constructor initializing the class's members, for the given
	 * project.
	 *
	 * @param project
	 *                the project for which this instance will be
	 *                responsible for.
	 * */
	public ProjectConfigurationParser(final IProject project) {
		this.project = project;
		uptodateFiles = new ConcurrentHashMap<IFile, String>();
		highlySyntaxErroneousFiles = Collections.synchronizedSet(new HashSet<IFile>());
		fileMap = new ConcurrentHashMap<IFile, String>();
		definitions = new ConcurrentHashMap<String, CfgDefinitionInformation>();
	}

	/**
	 * Get all constant definitions with location information for the
	 * current project.
	 *
	 * @return the constant definitions.
	 */
	public Map<String, CfgDefinitionInformation> getAllDefinitions() {
		return definitions;
	}

	/**
	 * Checks if a given file is already identified as a Runtime
	 * Configuration file.
	 *
	 * @param file
	 *                the file to check
	 * @return true if it is known by the on-the-fly configuration file
	 *         parser to be a valid configuration file
	 * */
	public boolean isFileKnown(final IFile file) {
		return (fileMap.get(file) != null);
	}

	/**
	 * Reports that the provided file has changed and so it's stored
	 * information became out of date.
	 * <ul>
	 * <li>If on-the-fly parsing is enabled re-analyzes the out-dated file
	 * <li>If it is not enabled, only stores that this file is out of date
	 * for later
	 * </ul>
	 *
	 * @param outdatedFile
	 *                the file which seems to have changed
	 *
	 * @return the WorkspaceJob in which the operation is running
	 * */
	public WorkspaceJob reportOutdating(final IFile outdatedFile) {
		WorkspaceJob op = new WorkspaceJob("Reporting outdate for: " + outdatedFile.getName()) {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				if (uptodateFiles.containsKey(outdatedFile)) {
					uptodateFiles.remove(outdatedFile);
				}

				return Status.OK_STATUS;
			}
		};
		op.setPriority(Job.SHORT);
		op.setSystem(true);
		op.setUser(false);
		op.setRule(outdatedFile);
		op.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
		op.schedule();
		return op;
	}

	/**
	 * Reports that the provided file has changed and so it's stored
	 * information became out of date.
	 * <ul>
	 * <li>If on-the-fly parsing is enabled re-analyzes the out-dated file
	 * <li>If it is not enabled, only stores that this file is out of date
	 * for later
	 * </ul>
	 *
	 * @param outdatedFiles
	 *                the file which seems to have changed
	 *
	 * @return the WorkspaceJob in which the operation is running
	 * */
	public WorkspaceJob reportOutdating(final List<IFile> outdatedFiles) {
		WorkspaceJob op = new WorkspaceJob("Reporting outdate for " + outdatedFiles.size() + " files") {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				for (IFile file : outdatedFiles) {
					if (uptodateFiles.containsKey(file)) {
						uptodateFiles.remove(file);
					}
				}

				return Status.OK_STATUS;
			}
		};
		op.setPriority(Job.SHORT);
		op.setSystem(true);
		op.setUser(false);
		op.setRule(project);
		op.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
		op.schedule();
		return op;
	}

	/**
	 * Removes data related to modules, that were deleted or moved.
	 **/
	private void removedReferencestoRemovedFiles() {
		List<IFile> filesToRemove = new ArrayList<IFile>();
		for (IFile file : fileMap.keySet()) {
			if (!file.isAccessible()) {
				uptodateFiles.remove(file);
				filesToRemove.add(file);
			}
		}

		for (IFile file : filesToRemove) {
			fileMap.remove(file);
		}
	}

	private IStatus internalDoAnalyzeSyntactically(final IProgressMonitor monitor) {
		removedReferencestoRemovedFiles();

		final IContainer[] workingDirectories = ProjectBasedBuilder.getProjectBasedBuilder(project).getWorkingDirectoryResources(false);

		OutdatedFileCollector visitor = new OutdatedFileCollector(workingDirectories, uptodateFiles, highlySyntaxErroneousFiles);
		try {
			project.accept(visitor);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
		List<IFile> filesToCheck = visitor.getCFGFilesToCheck();

		for (IFile file : uptodateFiles.keySet()) {
			MarkerHandler.markAllMarkersForRemoval(file, GeneralConstants.ONTHEFLY_SEMANTIC_MARKER);
			MarkerHandler.markAllMarkersForRemoval(file, GeneralConstants.ONTHEFLY_MIXED_MARKER);
		}


		// Remove all markers and definitions from the files that need
		// to be parsed. The innermost loop usually executes once.
		// It's not that expensive. :)
		for ( final IFile file : filesToCheck ) {
			removeMarkersAndDefinitions( file );
		}

		// parsing the files
		monitor.beginTask( PARSING, IProgressMonitor.UNKNOWN );

		final List<Macro> macros = new ArrayList<Macro>();
		final Map<String, String> env = System.getenv();

		final List<IFile> filesChecked = new ArrayList<IFile>();
		while( !filesToCheck.isEmpty() ) {
			final IFile file = filesToCheck.get( 0 );
			if( !filesChecked.contains( file ) ) {
				// parse a file only if the operation was not canceled
				// and the file is not yet up-to-date
				if (monitor.isCanceled()) {
					// just don't do anything
				} else if (!file.isAccessible()) {
					TITANDebugConsole.println("The file " + file.getLocationURI() + " does not seem to exist.");
				} else if (!uptodateFiles.containsKey(file)) {
					monitor.subTask(file.getProjectRelativePath().toOSString());
					// parse the contents of the file
					fileBasedAnalysis( file, macros, filesToCheck, filesChecked );
				}
				monitor.worked(1);
				filesChecked.add( file );
			}
			filesToCheck.remove( 0 );
		}

		filesToCheck.clear();

		// Check if macro references are valid.
		// This can be done only after all of the files are parsed
		checkMacroErrors( macros, definitions, env );

		// Semantic checking will start here

		monitor.done();

		for (IFile file : uptodateFiles.keySet()) {
			MarkerHandler.removeAllOnTheFlyMarkedMarkers(file);
		}

		for (IFile file : filesToCheck) {
			if (!uptodateFiles.containsKey(file)) {
				MarkerHandler.removeAllOnTheFlyMarkedMarkers(file);
			}
		}

		return Status.OK_STATUS;
	}

	/**
	 * Remove markers and definitions of file
	 * @param aFile file
	 */
	private void removeMarkersAndDefinitions( final IFile aFile ) {
		MarkerHandler.markAllOnTheFlyMarkersForRemoval( aFile );
		MarkerHandler.markAllTaskMarkersForRemoval( aFile );
		final Set<Map.Entry<String, CfgDefinitionInformation>> entries = definitions.entrySet();
		for ( final Iterator<Map.Entry<String, CfgDefinitionInformation>> mapIter = entries.iterator(); mapIter.hasNext(); ) {
			final Map.Entry<String, CfgDefinitionInformation> entry = mapIter.next();
			final CfgDefinitionInformation info = entry.getValue();
			final List<CfgLocation> list = info.getLocations();
			for ( final Iterator<CfgLocation> listIter = list.iterator(); listIter.hasNext(); ) {
				final CfgLocation location = listIter.next();
				if ( location.getFile().equals( aFile ) ) {
					listIter.remove();
				}
			}
			if (list.isEmpty()) {
				mapIter.remove();
			}
		}
	}

	/**
	 * Analyzes all of the files which are in the same project with the
	 * provided file.
	 * <ul>
	 * <li>the files possibly needed to analyze are collected first
	 * <li>those files, which are known to be up-to-date are filtered from
	 * this list
	 * <li>the files left in the list are analyzed in a new workspace job
	 * </ul>
	 *
	 * @return the WorkspaceJob in which the operation is running
	 * */
	public WorkspaceJob analyzeAll() {
		if (!project.isAccessible() || !TITANNature.hasTITANNature(project)) {
			return null;
		}

		final WorkspaceJob op = new WorkspaceJob(SOURCE_ANALYSING) {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				IProgressMonitor internalMonitor = monitor == null ? new NullProgressMonitor() : monitor;

				if (internalMonitor.isCanceled()) {
					analyzersRunning.decrementAndGet();
					return Status.CANCEL_STATUS;
				}

				final int priority = getThread().getPriority();

				try {
					getThread().setPriority(Thread.MIN_PRIORITY);
					internalDoAnalyzeSyntactically(internalMonitor);
				} finally {
					getThread().setPriority(priority);
					analyzersRunning.decrementAndGet();
				}

				return Status.OK_STATUS;
			}

		};
		op.setPriority(Job.LONG);
		op.setSystem(true);
		op.setUser(false);
		op.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));

		op.setRule(project);

		if (analyzersRunning.get() > 0) {
			if (lastAnalyzes != null && lastAnalyzes.getState() != Job.RUNNING) {
				lastAnalyzes.cancel();
			}
		}
		op.schedule();

		lastAnalyzes = op;
		analyzersRunning.incrementAndGet();

		return lastAnalyzes;
	}

	/**
	 * Parses the provided file.
	 *
	 * @param file (in) the file to be parsed
	 * @param aMacros (in/out) collected macro references
	 * @param aFilesChecked files, which are already processed (there are no duplicates)
	 * @param aFilesToCheck files, which will be processed (there are no duplicates)
	 */
	private void fileBasedAnalysis( final IFile file,
									final List<Macro> aMacros,
									final List<IFile> aFilesToCheck,
									final List<IFile> aFilesChecked ) {
		List<TITANMarker> warnings = null;
		List<SyntacticErrorStorage> errorsStored = null;
		IDocument document = null;
		ISemanticTITANEditor tempEditor = null;
		List<ISemanticTITANEditor> editors = null;
		if (EditorTracker.containsKey(file)) {
			editors = EditorTracker.getEditor(file);
			tempEditor = editors.get(0);
			document = tempEditor.getDocument();
		}
		ConfigTextEditor editor = null;
		if (tempEditor instanceof ConfigTextEditor) {
			editor = (ConfigTextEditor) tempEditor;
		}

		String oldConfigFilePath = fileMap.get(file);
		if (oldConfigFilePath != null) {
			fileMap.remove(file);
		}

		CfgAnalyzer cfgAnalyzer = new CfgAnalyzer();
		cfgAnalyzer.parse(file, document == null ? null : document.get());
		errorsStored = cfgAnalyzer.getErrorStorage();
		final CfgParseResult cfgParseResult = cfgAnalyzer.getCfgParseResult();
		if ( cfgParseResult != null ) {
			warnings = cfgParseResult.getWarnings();
			aMacros.addAll( cfgParseResult.getMacros() );
			definitions.putAll( cfgParseResult.getDefinitions() );

			// add included files to the aFilesToCheck list
			final List<String> includeFilenames = cfgParseResult.getIncludeFiles();
			for ( final String includeFilename : includeFilenames ) {
				// example value: includeFilename == MyExample2.cfg
				// example value: file == L/hw/src/MyExample.cfg
				final IPath includeFilePath = PathConverter.getProjectRelativePath( file, includeFilename );
				// example value: includeFilePath == src/MyExample2.cfg
				if ( includeFilePath != null ) {
					final IFile includeFile = project.getFile( includeFilePath );
					// example value: includeFile == L/hw/src/MyExample2.cfg
					// includeFile is null if the file does not exist in the project
					if ( includeFile != null &&
						 !uptodateFiles.containsKey( includeFile ) &&
						 !aFilesChecked.contains( includeFile ) &&
						 !aFilesToCheck.contains( includeFile ) ) {
						removeMarkersAndDefinitions( includeFile );
						aFilesToCheck.add( includeFile );
					}
				}
			}

			if (editor != null && editor.getDocument() != null) {
				ConfigEditor parentEditor = editor.getParentEditor();
				if ( errorsStored == null || errorsStored.isEmpty() ) {
					parentEditor.setParseTreeRoot(cfgParseResult.getParseTreeRoot());
					parentEditor.setTokens(cfgParseResult.getTokens());
					parentEditor.refresh(cfgAnalyzer);
					parentEditor.setErrorMessage(null);
				} else {
					if(errorsStored.size()>1) {
						parentEditor.setErrorMessage("There were " + errorsStored.size() + " problems found while parsing");
					} else {
						parentEditor.setErrorMessage("There was 1 problem found while parsing");
					}
				}
			}
		}

		fileMap.put(file, file.getFullPath().toOSString());
		uptodateFiles.put(file, file.getFullPath().toOSString());

		if (document != null) {
			GlobalIntervalHandler.putInterval(document, cfgAnalyzer.getRootInterval());
		}

		if (warnings != null) {
			for (TITANMarker marker : warnings) {
				if (file.isAccessible()) {
					Location location = new Location(file, marker.getLine(), marker.getOffset(), marker.getEndOffset());
					location.reportExternalProblem(marker.getMessage(), marker.getSeverity(),
							GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER);
				}
			}
		}

		if (errorsStored != null && !errorsStored.isEmpty()) {
			String reportLevel = Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
					PreferenceConstants.REPORTERRORSINEXTENSIONSYNTAX, GeneralConstants.WARNING, null);
			int errorLevel;
			if (GeneralConstants.ERROR.equals(reportLevel)) {
				errorLevel = IMarker.SEVERITY_ERROR;
			} else if (GeneralConstants.WARNING.equals(reportLevel)) {
				errorLevel = IMarker.SEVERITY_WARNING;
			} else {
				return;
			}
			for (int i = 0; i < errorsStored.size(); i++) {
				ParserMarkerSupport.createOnTheFlySyntacticMarker(file, errorsStored.get(i), errorLevel);
			}
		}

		if (document != null && editors != null) {
			ConfigFoldingSupport foldingSupport = new ConfigFoldingSupport();
			final IDocument tempDocument = document;
			final List<ISemanticTITANEditor> editors2 = editors;
			final List<Position> positions = foldingSupport.calculatePositions(tempDocument);
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
	}

	/**
	 * Gets the value of a macro or an environment variable
	 * @param aDefinition macro or environment variable
	 * @param aDefines definitions from the [DEFINE] sections
	 * @param aEnv environment variables
	 * @return macro or environment variable value, or null if there is no such definition
	 */
	private String getDefinitionValue( final String aDefinition,
									   final Map<String, CfgDefinitionInformation> aDefines,
									   final Map<String, String> aEnv){
		if ( aDefines != null && aDefines.containsKey( aDefinition ) ) {
			return aDefines.get( aDefinition ).getValue();
		} else if ( aEnv != null && aEnv.containsKey( aDefinition ) ) {
			return aEnv.get( aDefinition );
		} else {
			return null;
		}
	}

	/**
	 * Checks if all the collected macros are valid,
	 * puts error markers if needed
	 * @param aMacros collected macro references
	 * @param aDefines definitions from the [DEFINE] sections
	 * @param aEnv environment variables
	 */
	public void checkMacroErrors( final List<Macro> aMacros,
								  final Map<String, CfgDefinitionInformation> aDefines,
								  final Map<String, String> aEnv ) {
		for ( final Macro macro : aMacros ) {
			final String value = getDefinitionValue( macro.getMacroName(), aDefines, aEnv );
			if ( value == null ) {
				final IFile file = macro.getFile();
				if ( file != null && file.isAccessible() ) {
					final TITANMarker marker = macro.getErrorMarker();
					final Location location = new Location( file, marker.getLine(),
															marker.getOffset(), marker.getEndOffset() );
					location.reportExternalProblem( marker.getMessage(), marker.getSeverity(),
													GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER );
				}
			}
		}
	}

}
