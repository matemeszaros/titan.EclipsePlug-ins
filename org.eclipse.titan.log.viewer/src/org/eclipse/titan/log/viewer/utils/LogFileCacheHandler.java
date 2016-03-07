/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.utils.FileUtils;
import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.exceptions.UserException;
import org.eclipse.titan.log.viewer.extractors.TestCaseExtractor;
import org.eclipse.titan.log.viewer.models.LogFileMetaData;
import org.eclipse.titan.log.viewer.models.LogRecordIndex;
import org.eclipse.titan.log.viewer.parsers.data.TestCase;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;

/**
 * This class is responsible for creating and updating the log file cache
 *
 */
public final class LogFileCacheHandler {

	/** long = 8 bytes, int = 4 bytes */
	private static final int LOG_RECORD_INDEX_SIZE = 8 + 2 * 4;
	/** 128 KB */
	private static final int BUFFER_SIZE = 128 * 1024;

	private LogFileCacheHandler() {
		// Do nothing
	}
	
	/**
	 * Checks if the log file cache is up to date
	 * 
	 * @param logFile the log file to cache
	 * @return true if the log file has changed, false if not
	 */
	public static boolean hasLogFileChanged(final IFile logFile) {
		File file = new File(logFile.getLocationURI());

		// Check if property file exists...
		File propertyFile = getPropertyFileForLogFile(logFile);
		if (!propertyFile.exists()) {
			return true;
		}

		// Check if property file (log file meta data is valid)
		if (isPropertyFileInvalid(logFile, propertyFile)){
			return true;
		}

		// Check if index file exists...
		File indexFile = getIndexFileForLogFile(logFile);
		if (!indexFile.exists()) {
			return true;
		}
		
		// Check if log record index file exists
		File logRecordIndexFile = getLogRecordIndexFileForLogFile(logFile);
		if (!logRecordIndexFile.exists()) {
			return true;
		}

		// Check if update is needed (log file file has changed)
		return updateNeeded(file, propertyFile);
	}

	private static boolean isPropertyFileInvalid(IFile logFile, File propertyFile) {
		try {
			LogFileMetaData logFileMetaData = logFileMetaDataReader(propertyFile);
			String projectName = logFile.getProject().getName();
			String projectRelativePath = File.separator + projectName + File.separator + logFile.getProjectRelativePath().toOSString();
			URI logFilePath = logFile.getLocationURI();
			if (!logFileMetaData.getProjectRelativePath().contentEquals(projectRelativePath)
					|| !logFileMetaData.getFilePath().equals(logFilePath)) {
				return true;
			}
		} catch (final IOException e) {
			return true;
		} catch (final ClassNotFoundException e) {
			return true;
		}
		return false;
	}

	/**
	 * Deletes the log file cache for the given log file, is it exist
	 * 
	 * @param logFile the log file
	 */
	private static void clearLogFilePropertyFile(final IFile logFile) {
		File propertyFile = getPropertyFileForLogFile(logFile); 
		if (propertyFile.exists()) {
			propertyFile.delete();
		}
	}
	
	/**
	 * Deletes the index file for the given log file, is it exist
	 * 
	 * @param logFile the log file
	 */
	private static void clearLogFileIndexFile(final IFile logFile) {
		File indexFile = getIndexFileForLogFile(logFile);
		FileUtils.deleteQuietly(indexFile);
	}
	
	/**
	 * Deletes the log record index file for the given log file, is it exist
	 * 
	 * @param logFile the log file
	 */
	private static void clearLogFileLogRecordIndexFile(final IFile logFile) {
		File indexFile = getLogRecordIndexFileForLogFile(logFile);
		FileUtils.deleteQuietly(indexFile);
	}
	
	/**
	 * Deletes the index file for the given log file, is it exist and all files in folder
	 * 
	 * @param logFile the log file
	 */
	public static void clearLogFolderCache(final IFolder logFolder) {
		File indexFile = getCacheFolderFor(logFolder);
 
		if (indexFile.exists() && indexFile.isDirectory()) {
			File[] filesInFolder = indexFile.listFiles();
			for (File aFilesInFolder : filesInFolder) {
				aFilesInFolder.delete();
			}
			FileUtils.deleteQuietly(indexFile);
		}
	}
	
	/**
	 * Creates an index file for with the given test case vector
	 * 
	 * @param indexFile the index file to write
	 * @param testCaseVector the test case vector
	 * @throws IOException if index file can not be written
	 */
	private static void createIndexFile(final File indexFile, final List<TestCase> testCaseVector) throws IOException {
		ObjectOutputStream objectFile = null;
		try {
		// Delete file if it already exist, path already created when creating property file
		if (indexFile.exists()) {
			try {
				indexFile.delete();
			} catch (SecurityException e) {
				ErrorReporter.logExceptionStackTrace(e);
				throw new IOException(Messages.getString("LogFileCacheHandler.0")); //$NON-NLS-1$
			}
		}
		// Create new file
		indexFile.createNewFile();
		// Write test case vector to file
		objectFile = new ObjectOutputStream(new FileOutputStream(indexFile));
		objectFile.writeObject(testCaseVector);
		
		} finally {
			IOUtils.closeQuietly(objectFile);
		}
	}
	
	/**
	 * @param logFile the log file
	 * @return a file which represents the cache file for the given log file
	 * 		   this file may or may not exist
	 */
	public static File getPropertyFileForLogFile(final IFile logFile) {
		return new File(getPropertyIFileForLogFile(logFile).getLocationURI());
	}

	/**
	 * @param logFile the log file
	 * @return a file which represents the cache file for the given log file
	 *         this file may or may not exist
	 */
	public static IFile getPropertyIFileForLogFile(final IFile logFile) {
		final IFolder cacheFolder = getCacheFolderFor(logFile);
		final String propertyFileName = logFile.getName() + Constants.PROPERTY_EXTENSION;

		return cacheFolder.getFile(propertyFileName);
	}
	

	/**
	 * Gets the cache file for a given IFile
	 * 
	 * @param logFile the log file to get the index file for
	 * @return File the index file, which may or may not exist
	 */
	public static File getIndexFileForLogFile(final IFile logFile) {
		return new File(getIndexIFileForLogFile(logFile).getLocationURI());
	}
	
	/**
	 * Gets the cache file for a given IFile
	 * 
	 * @param logFile the log file to get the index file for
	 * @return File the index file, which may or may not exist
	 */
	public static IFile getIndexIFileForLogFile(final IFile logFile) {
		final IFolder cacheFolder = getCacheFolderFor(logFile);
		final String indexFileName = logFile.getName() + Constants.INDEX_EXTENSION;

		return cacheFolder.getFile(indexFileName);
	}

	/**
	 * Gets the log record cache file for a given IFile
	 * 
	 * @param logFile the log file
	 * @return File the log record index file, which may or may not exist
	 */
	public static File getLogRecordIndexFileForLogFile(final IFile logFile) {
		return new File(getLogRecordIndexIFileForLogFile(logFile).getLocationURI());
	}
	
	/**
	 * Gets the log record cache file for a given IFile
	 * 
	 * @param logFile the log file
	 * @return File the log record index file, which may or may not exist
	 */
	public static IFile getLogRecordIndexIFileForLogFile(final IFile logFile) {
		final IFolder cacheFolder = getCacheFolderFor(logFile);
		final String logFileName = logFile.getName() + Constants.RECORD_INDEX_EXTENSION;

		return cacheFolder.getFile(logFileName);
	}

	/**
	 * @param logFile the log file
	 * @return a file which represents the index file for the given log file
	 *         this file may or may not exist
	 */
	private static File getCacheFolderFor(final IFolder logFolder) {
		final IPath projectRelativePath = Path.fromOSString(
				Constants.CACHE_DIRECTORY + File.separator + logFolder.getProjectRelativePath().toOSString());
		final IProject project = logFolder.getProject();

		return new File(project.getFolder(projectRelativePath).getLocationURI());
	}

	/**
	 * 
	 * @param logFile The logFile
	 * @return The cache folder for the given file
	 *         this folder may or may not exists
	 */
	private static IFolder getCacheFolderFor(final IFile logFile) {
		final IProject project = logFile.getProject();
		final IPath projectRelativePath = Path.fromOSString(
				Constants.CACHE_DIRECTORY + File.separator + logFile.getProjectRelativePath().removeLastSegments(1).toOSString());

		return project.getFolder(projectRelativePath);
	}

	/**
	 * @param logFile the log file
	 * @param propertyFile the property of the log file
	 * @return true if log file cache needs to be updated, otherwise false 
	 */
	private static boolean updateNeeded(final File logFile, final File propertyFile) {
		// Check if cache file is up to date
		LogFileMetaData storedData;
		try {
			storedData = logFileMetaDataReader(propertyFile);
		} catch (IOException e) {
			return true;
		} catch (ClassNotFoundException e) {
			return true;
		}

		return !Constants.CURRENT_VERSION.equals(storedData.getVersion())
				|| storedData.getSize() != logFile.length()
				|| storedData.getLastModified() != logFile.lastModified();

	}
	
	/**
	 * @param logFile the log file
	 * @param propertyFile the property file to create
	 * @throws IOException if properties can not be created
	 */
	private static void createPropertyFile(final File propertyFile, final LogFileMetaData logFileMetaData) throws IOException {
		// If property file exist, delete it
		if (propertyFile.exists()) {
			try {
				propertyFile.delete();
			} catch (SecurityException e) {
				ErrorReporter.logExceptionStackTrace(e);
				throw new IOException(Messages.getString("LogFileCacheHandler.1")); //$NON-NLS-1$
			}
		} else {
			// Create dir(s)
			propertyFile.getParentFile().mkdirs();
		}
		// Create file
		propertyFile.createNewFile();
		logFileMetaDataSerializer(logFileMetaData, propertyFile);
	}

	/**
	 * @param logFileMetaData file meta data
	 * @param propertyFile he property file to create
	 * @throws IOException if log file meta data can not be set
	 */
	private static void logFileMetaDataSerializer(final LogFileMetaData logFileMetaData, final File propertyFile) throws IOException  {
		FileOutputStream stream = null;
		ObjectOutputStream objectFile = null;
		try {
			stream = new FileOutputStream(propertyFile);

			objectFile = new ObjectOutputStream(stream);
			objectFile.writeObject(logFileMetaData);

		} finally {
			IOUtils.closeQuietly(stream, objectFile);
		}
	}
	
	/**
	 * @param propertyFile property file to read from
	 * @return Log file meta data
	 * @throws IOException if log file meta data can not be found
	 * @throws ClassNotFoundException if log file meta data can not be found
	 */
	public static LogFileMetaData logFileMetaDataReader(final File propertyFile) throws IOException, ClassNotFoundException  {
		FileInputStream stream = null;
		LogFileMetaData logFileMetaData = null;
		ObjectInputStream objectFile = null;
		try {
			stream = new FileInputStream(propertyFile);

			objectFile = new ObjectInputStream(stream);
			logFileMetaData = (LogFileMetaData) objectFile.readObject();
		} catch (final InvalidClassException e) {
			FileUtils.deleteQuietly(propertyFile);
		} finally {
			IOUtils.closeQuietly(stream, objectFile);
		}
		return logFileMetaData;
	}
	
	/**
	 * Reads log records from an index file
	 * 
	 * @param indexFile the index file
	 * @param startRecordIndex the log record index to start from 
	 * @param numberOfRecords the number of log records indexes to be read
	 * @return an array with the read log record indexes
	 * @throws FileNotFoundException if the index file is not found
	 * @throws IOException if i/o errors occurs 
	 */
	public static LogRecordIndex[] readLogRecordIndexFile(final File indexFile, final int startRecordIndex, final int numberOfRecords) throws IOException {
		int bytesToRead = numberOfRecords * LOG_RECORD_INDEX_SIZE;
		ByteBuffer buffer = ByteBuffer.allocateDirect(bytesToRead);
		FileInputStream fileInputStream = new FileInputStream(indexFile);
		FileChannel fileChannel = fileInputStream.getChannel();
		try {
			fileChannel.position((long) startRecordIndex * LOG_RECORD_INDEX_SIZE);
			int bytesRead = fileChannel.read(buffer);
			buffer.rewind();
			LogRecordIndex[] logRecordIndexes = null;
			if (bytesRead == bytesToRead) {
				logRecordIndexes = new LogRecordIndex[numberOfRecords];
				for (int counter = 0; counter < numberOfRecords; counter++) {
					logRecordIndexes[counter] = new LogRecordIndex(buffer.getLong(), buffer.getInt(), buffer.getInt());
				}
			}
			return logRecordIndexes;
		} finally {
			IOUtils.closeQuietly(fileChannel, fileInputStream);
		}
	}

	/**
	 * Writes the log record index file
	 * 
	 * @param indexFile the index file to write to
	 * @param logRecordIndexes an array with the log record indexes to be written
	 * @throws FileNotFoundException if the index file is not found
	 * @throws IOException if i/o errors occurs
	 */
	private static void writeLogRecordIndexFile(final File indexFile, final List<LogRecordIndex> logRecordIndexes) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
		FileOutputStream fileOutputStream = new FileOutputStream(indexFile);
		FileChannel file = fileOutputStream.getChannel();
		file.truncate(0);
		try {
			for (LogRecordIndex currLogRecordIndex : logRecordIndexes) {
				buffer.putLong(currLogRecordIndex.getFileOffset());
				buffer.putInt(currLogRecordIndex.getRecordLength());
				buffer.putInt(currLogRecordIndex.getRecordNumber());
				if (!buffer.hasRemaining()) {
					buffer.rewind();
					file.write(buffer);
					buffer.clear();
				}
				//logRecordIndexes.remove(i); -> causes error...
			}

			// Make sure that the remaining data is flushed to disc
			if (buffer.position() != 0) {
				buffer.flip();
				file.write(buffer);
			}

		} finally {
			IOUtils.closeQuietly(file, fileOutputStream);
		}
	}
	
	/**
	 * Gets the number of records in an index file
	 * @param indexFile the index file
	 * @return the number of records in the file
	 */
	public static int getNumberOfLogRecordIndexes(final File indexFile) {
		return (int) (indexFile.length() / LOG_RECORD_INDEX_SIZE);
	}

	/**
	 * Initializes the cache for the given log file.
	 */
	public static void fillCache(final IFile logFile, final LogFileMetaData logFileMetaData,
			final List<TestCase> testCaseVector, final List<LogRecordIndex> logRecordIndexVector) throws IOException {
		LogFileCacheHandler.createPropertyFile(LogFileCacheHandler.getPropertyFileForLogFile(logFile), logFileMetaData);
		LogFileCacheHandler.createIndexFile(LogFileCacheHandler.getIndexFileForLogFile(logFile), testCaseVector);
		LogFileCacheHandler.writeLogRecordIndexFile(LogFileCacheHandler.getLogRecordIndexFileForLogFile(logFile), logRecordIndexVector);
	}

	/**
	 * Clears the cache of the log file.
	 * The following files will be deleted (if exist)
	 * <li>property file</li>
	 * <li>test case index file</li>
	 * <li>log record index file</li>
	 * 
	 * @param logFile The log file
	 */
	public static void clearCache(final IFile logFile) {
		LogFileCacheHandler.clearLogFileIndexFile(logFile);
		LogFileCacheHandler.clearLogFileLogRecordIndexFile(logFile);
		LogFileCacheHandler.clearLogFilePropertyFile(logFile);
	}
	
	/**
	 * Should be called when a log file has changed. Closes the associated views. Shows a <code>MessageBox</code> where the user can decide if he/she wants to process
	 * the log file or just close the views. If the user choose yes, the processing will start in a new <code>WorkspaceJob</code>.
	 * @param logFile
	 */
	public static void handleLogFileChange(final IFile logFile) {
		final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		final IViewReference[] viewReferences = activePage.getViewReferences();
		ActionUtils.closeAssociatedViews(activePage, viewReferences, logFile);
		clearCache(logFile);

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				IViewPart view = activePage.findView("org.eclipse.ui.navigator.ProjectExplorer");
				if (view instanceof CommonNavigator) {
					CommonNavigator navigator = (CommonNavigator) view;
					navigator.getCommonViewer().collapseToLevel(logFile, AbstractTreeViewer.ALL_LEVELS);
				}
			}
		});
		
		MessageBox mb = new MessageBox(activePage.getActivePart().getSite().getShell(), SWT.ICON_ERROR | SWT.OK | SWT.CANCEL);
		mb.setText("The log file has been modified.");
		mb.setMessage("The log file has been modified. Click on OK to extract the test cases"
				+ " or CANCEL to close the associated views.");
		if (mb.open() == SWT.OK) {
			WorkspaceJob job = new WorkspaceJob("Testcase extraction from log file: " + logFile.getName()) {
				@Override
				public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
					boolean completed = LogFileCacheHandler.processLogFile(logFile, monitor, false);
					return completed ? Status.OK_STATUS : Status.CANCEL_STATUS;
				}
			};
			job.setPriority(Job.LONG);
			job.setUser(true);
			job.setRule(logFile.getProject());
			job.schedule();
		}
	}
	
	/**
	 * Processes the given LogFile if it has changed. The property file, index file, and log record index file will be created.
	 * Does nothing if the log file has not changed, or test case extraction is already running on the file.
	 * @param logFile The log file
	 * @param pMonitor Progress monitor.
	 * @param quietMode If false, the error messages will be displayed to the user.
	 * @return true if the processing was successful, false otherwise
	 */
	public static boolean processLogFile(final IFile logFile, final IProgressMonitor pMonitor, final boolean quietMode) {
		IProgressMonitor monitor = pMonitor == null ? new NullProgressMonitor() : pMonitor;
		
		if (!logFile.exists()) {
			if (!quietMode) {
				TitanLogExceptionHandler.handleException(new UserException("The log file does not exist: " + logFile.getName())); //$NON-NLS-1$
			}
			return false;
		}
		
		try {
			Object temp = logFile.getSessionProperty(Constants.EXTRACTION_RUNNING);
			if (temp != null && (Boolean) temp) {
				if (!quietMode) {
					TitanLogExceptionHandler.handleException(new TechnicalException("Test case extraction is running on the given logfile: " + logFile.getName())); //$NON-NLS-1$
				}
				return false;
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new UserException(e.getMessage()));
			return false;
		}
		
		if (!LogFileCacheHandler.hasLogFileChanged(logFile)) {
			return true;
		}

		try {
			logFile.setSessionProperty(Constants.EXTRACTION_RUNNING, true);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return false;
		}

		final LogFileHandler logFileHandler = new LogFileHandler(logFile);

		try {
			LogFileMetaData logFileMetaData = logFileHandler.autoDetect();

			final TestCaseExtractor testCaseExtractor = new TestCaseExtractor();
			testCaseExtractor.extractTestCasesFromLogFile(logFileMetaData, monitor);
			
			if (monitor.isCanceled()) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						IViewPart view = activePage.findView("org.eclipse.ui.navigator.ProjectExplorer");
						
						if (view instanceof CommonNavigator) {
							CommonNavigator navigator = (CommonNavigator) view;
							navigator.getCommonViewer().update(logFile, null);
							navigator.getCommonViewer().collapseToLevel(logFile, AbstractTreeViewer.ALL_LEVELS);
						}
					}
				});
				return false;
			}
			
			fillCache(logFile, logFileMetaData, testCaseExtractor.getTestCases(), testCaseExtractor.getLogRecordIndexes());

			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					IViewPart view = activePage.findView("org.eclipse.ui.navigator.ProjectExplorer");
					
					if (view instanceof CommonNavigator) {
						CommonNavigator navigator = (CommonNavigator) view;
						navigator.getCommonViewer().refresh(logFile, true);
						navigator.getCommonViewer().expandToLevel(logFile, AbstractTreeViewer.ALL_LEVELS);
					}
				}
			});
			
			if (testCaseExtractor.failedDuringExtraction()) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openInformation(null,
								Messages.getString("OpenTextTableProjectsViewMenuAction.8"),
								Messages.getString("OpenTextTableProjectsViewMenuAction.9"));
					}
				});
				return false;
			}
			
			
			
		} catch (IOException e) {
			if (!quietMode) {
				ErrorReporter.logExceptionStackTrace(e);
				TitanLogExceptionHandler.handleException(
						new TechnicalException(Messages.getString("OpenTextTableProjectsViewMenuAction.2") + e.getMessage()));  //$NON-NLS-1$
			}
			return false;
		} catch (TechnicalException e) { // invalid file format
			if (!quietMode) {
				MessageDialog.openError(Display.getDefault().getActiveShell(), "Invalid log file", e.getMessage());
			}
			return false;
		} finally {
			try{
				logFile.setSessionProperty(Constants.EXTRACTION_RUNNING, false);
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}

		return true;
	}

	/**
	 * Returns a scheduling rule for the given log file. This rule contains the log file, the property file and the two index files.
	 * 
	 * @param logFile
	 * @return The created MultiRule
	 */
	public static ISchedulingRule getSchedulingRule(final IFile logFile) {
		final ISchedulingRule[] rules = new ISchedulingRule[] {
				logFile,
				getPropertyIFileForLogFile(logFile),
				getIndexIFileForLogFile(logFile),
				getLogRecordIndexIFileForLogFile(logFile)
		};

		return new MultiRule(rules);
	}
}
