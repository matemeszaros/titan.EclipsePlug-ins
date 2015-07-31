/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.parsers;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titan.log.viewer.console.TITANDebugConsole;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.factories.MessageAnalyserFactory;
import org.eclipse.titan.log.viewer.models.FilterPattern;
import org.eclipse.titan.log.viewer.models.LogFileMetaData;
import org.eclipse.titan.log.viewer.models.LogRecordIndex;
import org.eclipse.titan.log.viewer.parsers.data.LogRecord;
import org.eclipse.titan.log.viewer.parsers.data.TestCase;
import org.eclipse.titan.log.viewer.preferences.PreferencesHandler;
import org.eclipse.titan.log.viewer.preferences.PreferencesHolder;
import org.eclipse.titan.log.viewer.readers.TestFileReader;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.views.msc.model.EventObject;
import org.eclipse.titan.log.viewer.views.msc.model.EventType;
import org.eclipse.titan.log.viewer.views.msc.model.ExecutionModel;
import org.eclipse.titan.log.viewer.views.msc.model.TestComponent;

/**
 * Singleton for handling selection of log parser
 */
public class Parser {

	private static final String MAPPING = "Mapping"; //$NON-NLS-1$
	private static final String UNMAPPING = "Unmapping"; //$NON-NLS-1$
	private static final String DISCONNECTING = "Disconnecting"; //$NON-NLS-1$
	private static final String CONNECTING = "Connecting"; //$NON-NLS-1$
	private int tcRecords = 0, ptcs = 0, sends = 0, recs = 0, maps = 0,
			cons = 0, enqs = 0;
	private long start = 0, end = 0;
	private final LogFileMetaData logFileMetaData;
	//TODO maybe we don't even need to store this information
	private LogRecordIndex[] logRecordIndexes;
	private ExecutionModel executionModel;
	private MessageAnalyser messageAnalyser;
	private final Decipherer decipherer = new Decipherer();

	private boolean mtcTerminated = false;
	private Set<String> currentlyLivingComponents;
	private Map<String, TestComponent> components;
	private List<Integer> setverdictVector = new ArrayList<Integer>();
	private List<ConnectedRecord> errorVector = new ArrayList<ConnectedRecord>();
	private List<ConnectedRecord> failVector = new ArrayList<ConnectedRecord>();
	private boolean displaySetverdictError;
	private boolean displaySetverdictFail;
	private boolean displaySetverdictInconc;
	private boolean displaySetverdictNone;
	private boolean displaySetverdictPass;
	private boolean filterConnectingPorts;
	private boolean filterMappingPorts;
	private Map<String, Boolean> filteredSilentEvents;
	private boolean wasCanceled = false;

	private List<Integer> eventVector = new ArrayList<Integer>();

	private EventObjectFactory eventObjectFactory = new EventObjectFactory();
	
	public Parser(final LogFileMetaData logFileMetaData) {
		this.logFileMetaData = logFileMetaData;
	}

	/**
	 * Returns the number of port connections
	 * 
	 * @return the number of port connections
	 */
	public int getCons() {
		return this.cons;
	}

	/**
	 * Returns the number records in the test case
	 * 
	 * @return the number records in the test case
	 */
	public int getTestCaseRecords() {
		return this.tcRecords;
	}

	/**
	 * Returns the number of port mappings
	 * 
	 * @return the number of port mappings
	 */
	public int getMaps() {
		return this.maps;
	}

	/**
	 * Returns the number of component creations
	 * 
	 * @return the number of component creations
	 */
	public int getPtcs() {
		return this.ptcs;
	}

	/**
	 * Returns the number of received messages
	 * 
	 * @return the number of received messages
	 */
	public int getRecs() {
		return this.recs;
	}

	/**
	 * Returns the number of sent messages
	 * 
	 * @return the number of sent messages
	 */
	public int getSends() {
		return this.sends;
	}

	/**
	 * Gets the start time
	 * 
	 * @return the start time
	 */
	public long getStart() {
		return this.start;
	}

	/**
	 * Sets the start time
	 * 
	 * @param start
	 *            the start time
	 */
	public void setStart(final long start) {
		this.start = start;
	}

	/**
	 * Gets the end time
	 * 
	 * @return the end time
	 */
	public long getEnd() {
		return this.end;
	}

	/**
	 * Sets the end time
	 * 
	 * @param end
	 *            the end time
	 */
	public void setEnd(final long end) {
		this.end = end;
	}
	
	/**
	 * Gets the number of enqueued messages
	 * @return
	 */
	public int getEnqs() {
		return enqs;
	}

	/**
	 * @return whether the parsing was canceled by the user or not.
	 * */
	public boolean wasCanceled() {
		return wasCanceled;
	}

	/**
	 * @return the number of events in the testcase.
	 * */
	public int getNumberOfEvents() {
		return eventVector.size();
	}

	/**
	 * @return whether the MTC has terminated correctly according to the log or it is not mentioned.
	 * */
	public boolean getMTCTerminated() {
		return mtcTerminated;
	}

	/**
	 * This function will parse eventObject from the log file
	 * 
	 * @param testCase
	 *            test case, PreferenceHolder preferences
	 * @return ExecutionModel throws IOException
	 */
	//FIXME signals, send, receive and silent events must be delayed.
	//TODO Preferences could be calculated
	public ExecutionModel preParse(final TestCase testCase,
			final LogRecordIndex[] logRecordIndexes, final PreferencesHolder preferences,
			final FilterPattern filterPattern, final IProgressMonitor monitor)
			throws IOException, ParseException, TechnicalException {
		IProgressMonitor internalMonitor = monitor == null ? new NullProgressMonitor() : monitor;
		wasCanceled = false;
		this.logRecordIndexes = logRecordIndexes;


		this.executionModel = new ExecutionModel(this);
		this.executionModel.setFilterPattern(filterPattern);

		// Add ignored components
		this.executionModel.addIgnoredComponent(preferences.getIgnoredComponents());
		this.executionModel.setSutName(preferences.getSutName());

		// Add ignored signals
		this.executionModel.addIgnoredSignals(preferences.getIgnoredSignals());

		// Add ignored functions
		this.executionModel.addIgnoredFunctions(preferences.getIgnoredFunctions());

		// needed for memento of MSC view
		this.executionModel.setContainedTestCase(testCase);
		this.executionModel.getTestCase().setTestCaseName(testCase.getTestCaseName());
		this.executionModel.getTestCase().setVerdict(testCase.getVerdict());

		// The SUT element object always exists at all times, to make sure
		// it becomes visible before any test starts.
		EventObject sut = createEventObject(null, EventType.SYSTEM_CREATE);
		sut.setName(preferences.getSutName());
		sut.setEventNumber(0);
		this.executionModel.addComponent(sut);
		executionModel.addLifeLineInfo(sut);

		EventObject hc = createEventObject(null, EventType.HC_CREATE);
		hc.setEventNumber(1);
		this.executionModel.addComponent(hc);
		executionModel.addLifeLineInfo(hc);

		EventObject mtc = createEventObject(null, EventType.MTC_CREATE);
		mtc.setEventNumber(2);
		this.executionModel.addComponent(mtc);
		executionModel.addLifeLineInfo(mtc);

		components = this.executionModel.getComponents();
		this.messageAnalyser = MessageAnalyserFactory.createMessageAnalyser(this.logFileMetaData);
		setUpFromPreferences(preferences, filterPattern);

		if (Constants.DEBUG) {
			TITANDebugConsole.getConsole().newMessageStream().println("Message type = " + this.messageAnalyser.getType()); //$NON-NLS-1$
		}

		this.tcRecords = logRecordIndexes.length;
		TestFileReader reader = null;
		try {
			reader = new TestFileReader(this.logFileMetaData.getFilePath(), logRecordIndexes);
			eventVector = new ArrayList<Integer>();

			internalMonitor.beginTask("Loading...", reader.size());
			/** Stores the components that was not terminated before the given log record.
			 * It can be used for partial log files.*/
			currentlyLivingComponents = new HashSet<String>();
			while (reader.hasNextRecord() && !internalMonitor.isCanceled()) {

				try {
					LogRecord logRecord = reader.getNextRecord();
					// Add test case record number offset to record
					logRecord.setRecordNumber(testCase.getStartRecordNumber() + logRecord.getRecordNumber());

					preParseLogRecord(logRecord, currentlyLivingComponents);
					internalMonitor.worked(1);
				} catch (ParseException e) {
					ErrorReporter.logExceptionStackTrace(e);
					ParseException throwable = new ParseException(e.getMessage(), 0);
					throwable.initCause(e);
					throw throwable;
				}
			}
		} finally {
			IOUtils.closeQuietly(reader);
		}

		// remove some of those components that are part of the surrounding system
		currentlyLivingComponents.remove(Constants.MTC_REFERENCE);
		currentlyLivingComponents.remove(Constants.HC_REFERENCE);
		currentlyLivingComponents.remove(Constants.SUT_REFERENCE);

		// The components which were not terminated
		int additionalIndex = 3;
		for (String compRef : currentlyLivingComponents) {
			EventObject event = new EventObject(EventType.PTC_TERMINATE);
			event.setEventNumber(eventVector.size() + additionalIndex);
			additionalIndex++;
			event.setReference(compRef);
			event.setName(compRef);

			executionModel.addLifeLineInfo(event);
		}

		wasCanceled = internalMonitor.isCanceled();

		internalMonitor.done();

		// if no mtc termination is made, do it here
		if (!mtcTerminated) {
			mtc = eventObjectFactory.createEventObject(EventType.MTC_TERMINATE, null, this.messageAnalyser, this.logFileMetaData.getTimeStampConstant());
			mtc.setEventNumber(eventVector.size() + additionalIndex);
			additionalIndex++;
			executionModel.addLifeLineInfo(mtc);
		}

		hc = eventObjectFactory.createEventObject(EventType.HC_TERMINATE, null, this.messageAnalyser, this.logFileMetaData.getTimeStampConstant());
		hc.setEventNumber(eventVector.size() + additionalIndex);
		additionalIndex++;
		executionModel.addLifeLineInfo(hc);

		// The last thing that "dies" in a log is the SUT, that still exists
		// after the test case is over. Still add a marker for this in the
		// log.
		sut = eventObjectFactory.createEventObject(EventType.SYSTEM_TERMINATE, null, this.messageAnalyser, this.logFileMetaData.getTimeStampConstant());
		sut.setEventNumber(eventVector.size() + additionalIndex);
		executionModel.addLifeLineInfo(sut);

		setVerdict();

		return this.executionModel;

	} // parse

	private void setVerdict() {
		int[] setverdictArray = new int[setverdictVector.size()];
		for (int i = 0; i < setverdictVector.size(); i++) {
			int verdictPlace = setverdictVector.get(i);
			setverdictArray[i] = verdictPlace;
		}
		this.executionModel.setSetverdict(setverdictArray);
	}

	private void setUpFromPreferences(PreferencesHolder preferences, FilterPattern filterPattern) {
		// Preferences for setverdict
		this.messageAnalyser.setErrorCausedBy(preferences.getErrorCausedBy());
		this.messageAnalyser.setFailCausedBy(preferences.getFailCausedBy());
		if (filterPattern == null) {
			displaySetverdictError = preferences.getSetverdictError();
			displaySetverdictFail = preferences.getSetverdictFail();
			displaySetverdictInconc = preferences.getSetverdictInconc();
			displaySetverdictNone = preferences.getSetverdictNone();
			displaySetverdictPass = preferences.getSetverdictPass();
			filterConnectingPorts = preferences.getFilteredConnectingPorts();
			filterMappingPorts = preferences.getFilteredMappingPorts();
			filteredSilentEvents = preferences.getFilteredSilentEvents();
		} else {
			displaySetverdictError = true;
			displaySetverdictFail = true;
			displaySetverdictInconc = true;
			displaySetverdictNone = true;
			displaySetverdictPass = true;
			filterConnectingPorts = false;
			filterMappingPorts = false;
			filteredSilentEvents = null;
		}
	}

	/**
	 * Parses a given region from the log file and returns the contents as a list of events.
	 * The SUT, MTC and System components are always added.
	 *
	 * @param startIndex the index of the event to be displayed.
	 * @param endIndex the index of the last event to be displayed.
	 * @param monitor the monitor to be used to report progress on.
	 *
	 * @return the events parsed from the given region.
	 * */
	public List<EventObject> parseRegion(final int startIndex, final int endIndex, final IProgressMonitor monitor)
			throws IOException, ParseException {
		IProgressMonitor internalMonitor = monitor == null ? new NullProgressMonitor() : monitor;
		TestFileReader reader = null;
		wasCanceled = false;
		List<EventObject> result = new ArrayList<EventObject>(endIndex - startIndex + 6 + 1);

		final PreferencesHolder preferences = PreferencesHandler.getInstance().getPreferences(this.logFileMetaData.getProjectName());

		// needed for memento of MSC view
		final TestCase testCase = this.executionModel.getTestCase();

		// The SUT element object always exists at all times, to make sure
		// it becomes visible before any test starts.
		EventObject sut = createEventObject(null, EventType.SYSTEM_CREATE);
		sut.setName(preferences.getSutName());
		result.add(sut);

		EventObject hc = createEventObject(null, EventType.HC_CREATE);
		result.add(hc);

		EventObject mtc = createEventObject(null, EventType.MTC_CREATE);
		result.add(mtc);

		if (Constants.DEBUG) {
			TITANDebugConsole.getConsole().newMessageStream().println("Message type = " + this.messageAnalyser.getType()); //$NON-NLS-1$
		}

		this.tcRecords = logRecordIndexes.length;
		try {
			reader = new TestFileReader(this.logFileMetaData.getFilePath(), logRecordIndexes);

			internalMonitor.beginTask("Loading...", reader.size());
			for (int i = startIndex; i <= endIndex && i < eventVector.size() && !internalMonitor.isCanceled(); i++) {
				try {
					int actualIndex = eventVector.get(i) - testCase.getStartRecordNumber();
					reader.setCurrentLogRecord(actualIndex);
					LogRecord logRecord = reader.getNextRecord();
					// Add test case record number offset to record
					logRecord.setRecordNumber(testCase.getStartRecordNumber() + logRecord.getRecordNumber());

					EventObject event = parseLogRecord(logRecord, i);
					if (event != null) {
						result.add(event);
					}
					internalMonitor.worked(1);
				} catch (ParseException e) {
					ErrorReporter.logExceptionStackTrace(e);
					ParseException throwable = new ParseException(e.getMessage(), 0);
					throwable.initCause(e);
					throw throwable;
				}
			}
		} finally {
			IOUtils.closeQuietly(reader);
		}

		wasCanceled = internalMonitor.isCanceled();

		internalMonitor.done();

		for (String compRef : currentlyLivingComponents) {
			EventObject event = new EventObject(EventType.PTC_TERMINATE);
			event.setReference(compRef);
			event.setName(compRef);

			result.add(event);
		}

		// if no mtc termination is made, do it here
		if (!mtcTerminated) {
			mtc = eventObjectFactory.createEventObject(EventType.MTC_TERMINATE, null, this.messageAnalyser, this.logFileMetaData.getTimeStampConstant());
			result.add(mtc);
		}

		hc = eventObjectFactory.createEventObject(EventType.HC_TERMINATE, null, this.messageAnalyser, this.logFileMetaData.getTimeStampConstant());
		result.add(hc);

		// The last thing that "dies" in a log is the SUT, that still exists
		// after the test case is over. Still add a marker for this in the
		// log.
		sut = eventObjectFactory.createEventObject(EventType.SYSTEM_TERMINATE, null, this.messageAnalyser, this.logFileMetaData.getTimeStampConstant());
		result.add(sut);

		return result;
	} // parse

	/**
	 * Parse a single log record.
	 *
	 * @param logRecord the record to analyze.
	 * */
	private void preParseLogRecord(final LogRecord logRecord, final Set<String> currentlyLivingComponents) {
		final String message = logRecord.getMessage();
		this.messageAnalyser.setMessage(message);

		boolean isLogRecordIgnored = false;
		if (executionModel.getFilterPattern() != null) {
			isLogRecordIgnored = !executionModel.getFilterPattern().match(logRecord);
		}

		// This if-else branch is ordered so that the most common
		// types of events are checked first and the less common
		// ones in dropping order. Send and receives are the most
		// interesting events we have in any test system.

		// This branch handles send events.
		if (this.messageAnalyser.isSend()) {
			String sendType = messageAnalyser.getSendType();
			if (!isLogRecordIgnored && !isSignalIgnored(sendType)
					&& components != null) {
				String sendTarget = messageAnalyser.getSendTarget();
				TestComponent tc = components.get(sendTarget);
				if (tc != null) {
					String logComponentRef = logRecord.getComponentReference();
					Set<String> sources = tc.getMappedFromReference(messageAnalyser.getSendSource());
					String source = getSource(logComponentRef, sources);

					if (source != null && !isComponentIgnored(source)) {
						sends++;
						eventVector.add(logRecord.getRecordNumber());
					}
				}
			}
		} else if (messageAnalyser.isReceive()) {
			// This branch handles receive events.
			String receiveType = messageAnalyser.getReceiveType();
			if (!isLogRecordIgnored && !isSignalIgnored(receiveType)
					&& components != null) {
				String sourceRef = messageAnalyser.getReceiveSource();
				TestComponent tc = components.get(sourceRef);
				if (tc != null) {
					String receiveTargetPort = messageAnalyser.getReceiveTarget();
					String logComponentRef = logRecord.getComponentReference();
					Set<String> targetRefs = tc.getMappedFromReference(receiveTargetPort);

					String targetRef = getTargetRef(logComponentRef, targetRefs);

					if (targetRef != null && !isComponentIgnored(targetRef)) {
						recs++;
						eventVector.add(logRecord.getRecordNumber());
					}
				}
			}
		} else if (messageAnalyser.isEnqueued()) {
			// This branch handles enqueued events.
			String receiveType = messageAnalyser.getReceiveType();
			if (!isLogRecordIgnored && !isSignalIgnored(receiveType)
					&& components != null) {
				String sourceRef = messageAnalyser.getReceiveSource();
				TestComponent tc = components.get(sourceRef);
				if (tc != null) {
					String enqueuedTargetPort = messageAnalyser.getEnqueuedTarget();
					String logComponentRef = logRecord.getComponentReference();
					Set<String> targetRefs = tc.getMappedFromReference(enqueuedTargetPort);

					// In case of valid log files it cannot happen.
					String targetRef = getTargetRef(logComponentRef, targetRefs);

					if (targetRef != null && !isComponentIgnored(targetRef)) {
						enqs++;
						eventVector.add(logRecord.getRecordNumber());
					}
				}
			}
		} else if (this.messageAnalyser.isReceiveOperation()) {
			// This branch handles receive operation events. AFAIK,
			// it is obsolete in new (>= 1.7.pl0) TITAN versions.
			String recieveType = messageAnalyser.getReceiveOperationType();
			if (!isLogRecordIgnored && !isSignalIgnored(recieveType)
					&& components != null) {
				String sourceRef = messageAnalyser.getReceiveSource();
				TestComponent tc = components.get(sourceRef);
				if (tc != null) {
					String receiveTargetPort = messageAnalyser.getReceiveOperationTarget();
					String logComponentRef = logRecord.getComponentReference();
					Set<String> targetRefs = tc.getMappedFromReference(receiveTargetPort);

					String targetRef = getTargetRef(logComponentRef, targetRefs);
					if (targetRef != null && !isComponentIgnored(targetRef)) {
						recs++;
						eventVector.add(logRecord.getRecordNumber());
					}
				}
			}
		} else if (this.messageAnalyser.isStartFunction()) {
			// This branch handles function start events.
			if (components != null) {
				String targetRef = this.messageAnalyser.getStartFunctionReference();
				if ((targetRef != null)
						&& !isLogRecordIgnored
						&& !isComponentIgnored(targetRef)
						&& !isFunctionIgnored(this.messageAnalyser.getStartFunctionName())) {
					eventVector.add(logRecord.getRecordNumber());
				}
			}
		} else if (this.messageAnalyser.isComponentCreation()) {
			// This branch handles component creation events.
			if (components != null) {
				EventObject event = createEventObject(logRecord, EventType.PTC_CREATE);
				event.setEventNumber(eventVector.size() + 3);
				String reference = event.getReference();
				currentlyLivingComponents.add(reference);
				TestComponent tc = components.get(reference);
				if (tc == null) {
					addComponent(event);
					if ((reference != null)
							&& !isComponentIgnored(reference)) {
						this.ptcs++;
						eventVector.add(logRecord.getRecordNumber());
						executionModel.addLifeLineInfo(event);
					}
				} else {
					if (!isComponentIgnored(reference)) {
						logRecord.setComponentReference(Constants.MTC_REFERENCE);
						eventVector.add(logRecord.getRecordNumber());
						executionModel.addLifeLineInfo(event);
					}
				}
			}
		} else if (this.messageAnalyser.isComponentDone()) {
			// This branch handles component done events.
			EventObject event = createEventObject(logRecord, EventType.PTC_DONE);

			String reference = event.getReference();
			if ((reference != null)
					&& !isComponentIgnored(reference)) {
				eventVector.add(logRecord.getRecordNumber());
			}
		} else if (this.messageAnalyser.isComponentTermination()) {
			// This branch handles component termination events.
			if (components != null) {
				String ref = this.messageAnalyser.getComponentTerminationReference();
				currentlyLivingComponents.remove(ref);
				if ((ref != null) && !isComponentIgnored(ref)) {
					String terminationVerdict = this.messageAnalyser.getComponentTerminationVerdict();
					TestComponent component = components.get(ref);
					if (component != null) {
						component.setVerdict(terminationVerdict);
					}

					EventObject event = createEventObject(logRecord, EventType.PTC_TERMINATE);
					event.setEventNumber(eventVector.size() + 3);
					event.setReference(ref);
					eventVector.add(logRecord.getRecordNumber());
					executionModel.addLifeLineInfo(event);
				}
			}
		} else if (this.messageAnalyser.isPortMapping()) {
			// This branch handles port mapping events.
			String mappingSource = this.messageAnalyser	.getPortMappingSource();
			final String compRef = this.messageAnalyser.getComponentRef(mappingSource);

			String target = this.messageAnalyser.getPortMappingTarget();
			String targetRef = this.messageAnalyser.getComponentRef(target);
			String sourcePort = this.messageAnalyser.getPort(mappingSource);
			String targetPort = this.messageAnalyser.getPort(target);

			if (!filterMappingPorts && !isLogRecordIgnored
					&& !(isComponentIgnored(compRef) || isComponentIgnored(targetRef))) {

				logRecord.setComponentReference(compRef);
				eventVector.add(logRecord.getRecordNumber());
				this.maps++;
			}
			// Used for mapping ports to reference
			addPortMapping(compRef, sourcePort, targetRef, targetPort);
		} else if (this.messageAnalyser.isPortUnmapping()) {
			// This branch handles port unmapping events.
			String unmappingSource = this.messageAnalyser.getPortUnMapping();
			String compRef = this.messageAnalyser.getComponentRef(unmappingSource);
			compRef = this.messageAnalyser.getComponentRef(compRef);

			String target = this.messageAnalyser.getPortUnMappingTarget();
			String targetRef = this.messageAnalyser.getComponentRef(target);

			if (!filterMappingPorts && !isLogRecordIgnored
					&& !(isComponentIgnored(compRef) || isComponentIgnored(targetRef))) {
				logRecord.setComponentReference(compRef);
				eventVector.add(logRecord.getRecordNumber());
				this.maps++;
			}

			// Used for mapping ports to reference
			// removePortMapping(messageAnalyser.getPortMappingSource(),
		} else if (this.messageAnalyser.isPortConnection()) {
			// This branch handles port connection events.
			String connectionSource = this.messageAnalyser.getPortConnectionSource();
			final String compRef = this.messageAnalyser.getComponentRef(connectionSource);

			// Used for connect port and reference
			String target = this.messageAnalyser.getPortConnectionTarget();
			String targetRef = this.messageAnalyser.getComponentRef(target);
			String sourcePort = this.messageAnalyser.getPort(connectionSource);
			String targetPort = this.messageAnalyser.getPort(target);

			if (!filterConnectingPorts && !isLogRecordIgnored
					&& !(isComponentIgnored(compRef) || isComponentIgnored(targetRef))) {
				logRecord.setComponentReference(compRef);
				eventVector.add(logRecord.getRecordNumber());
				this.cons++;
			}
			addPortMapping(compRef, sourcePort, targetRef, targetPort);
		} else if (this.messageAnalyser.isPortDisconnection()) {
			// This branch handles port disconnection events.
			String disconnectionSource = this.messageAnalyser.getPortDisconnectionSource();
			final String compRef = this.messageAnalyser.getComponentRef(disconnectionSource);
			// Used for disconnect port and reference
			String target = this.messageAnalyser.getPortConnectionTarget();
			String targetRef = this.messageAnalyser.getComponentRef(target);

			if (!filterConnectingPorts && !isLogRecordIgnored
					&& !(isComponentIgnored(compRef) || isComponentIgnored(targetRef))) {
				logRecord.setComponentReference(compRef);
				eventVector.add(logRecord.getRecordNumber());
				this.cons++;
			}
		} else if (this.messageAnalyser.isTestcaseStart()) {
			// This branch handles test case start events.
			eventVector.add(logRecord.getRecordNumber());

		} else if (this.messageAnalyser.isTestcaseEnd()) {
			// This branch handles test case end events.
			eventVector.add(logRecord.getRecordNumber());
			notifyChange();
		} else if (this.messageAnalyser.isMTCCreation()) {
			EventObject event = createEventObject(logRecord, EventType.MTC_CREATE);
			addComponent(event);
			eventVector.add(logRecord.getRecordNumber());
		} else if (this.messageAnalyser.isMTCTermination()) {
			// This branch handles master test component termination events.
			eventVector.add(logRecord.getRecordNumber());
			mtcTerminated = true;
		} else if (this.messageAnalyser.isMTCDone()) {
			// This branch handles master test component done and sets the verdict.
			eventVector.add(logRecord.getRecordNumber());
		} else if (this.messageAnalyser.isSetverdict()) {

			if ((logRecord.getComponentReference() != null) && !isLogRecordIgnored
					&& !isComponentIgnored(logRecord.getComponentReference())) {

				// Get type - change type inconc
				String setverdicttype = this.messageAnalyser.getSetverdictType();
				if (Constants.TEST_CASE_VERDICT_INCONCLUSIVE.equals(setverdicttype)
						&& displaySetverdictInconc) {
					setverdictVector.add(eventVector.size());
					eventVector.add(logRecord.getRecordNumber());
				} else if (Constants.TEST_CASE_VERDICT_ERROR.equals(setverdicttype)
						&& displaySetverdictError) {
					setverdictVector.add(eventVector.size());
					eventVector.add(logRecord.getRecordNumber());
				} else if (Constants.TEST_CASE_VERDICT_FAIL.equals(setverdicttype)
						&& displaySetverdictFail) {
					setverdictVector.add(eventVector.size());
					eventVector.add(logRecord.getRecordNumber());
				} else if (Constants.TEST_CASE_VERDICT_NONE.equals(setverdicttype)
						&& displaySetverdictNone) {
					setverdictVector.add(eventVector.size());
					eventVector.add(logRecord.getRecordNumber());
				} else if (Constants.TEST_CASE_VERDICT_PASS.equals(setverdicttype)
						&& displaySetverdictPass) {
					setverdictVector.add(eventVector.size());
					eventVector.add(logRecord.getRecordNumber());
				}

				errorVector.clear();
				failVector.clear();


			}
		} else {
			// Silent events
			Boolean filtered = isEventIgnored(logRecord.getEventType());
			String compRef = logRecord.getComponentReference();
			if (compRef == null || compRef.length() == 0) {
				compRef = this.messageAnalyser.isSilentEvent();
			}

			if (compRef != null && compRef.length() > 0 && !isComponentIgnored(logRecord.getComponentReference())) {
				addDummyComponent(compRef);
				currentlyLivingComponents.add(compRef);
			}

			isLogRecordIgnored = isLogRecordIgnored || isComponentIgnored(logRecord.getComponentReference());

			if (!filtered && !isLogRecordIgnored) {
				eventVector.add(logRecord.getRecordNumber());
			}

		}
		// check if Dynamic test case error, used for
		// setverdict(error)
		if (this.messageAnalyser.isDynamicTestCaseError()) {
			ConnectedRecord connectedRecord = new ConnectedRecord(logRecord.getRecordOffset(), logRecord.getRecordLength(), logRecord.getRecordNumber());
			errorVector.add(connectedRecord);
		} // check if fail messages, used for setverdict(fail)
		if (this.messageAnalyser.isFailMessages()) {
			ConnectedRecord connectedRecord = new ConnectedRecord(logRecord.getRecordOffset(), logRecord.getRecordLength(), logRecord.getRecordNumber());
			failVector.add(connectedRecord);
		}
	}

	private String getSource(String logComponentRef, Set<String> sources) {
		String source = null;
		if (sources != null) {
			for (String source1 : sources) {
				source = source1;
				if (logComponentRef != null && logComponentRef.equals(source)) {
					break;
				}
			}
		}
		return source;
	}

	private String getTargetRef(String logComponentRef, Set<String> targetRefs) {
		String targetRef = null;
		if (targetRefs != null) {
			for (String targetRef1 : targetRefs) {
				targetRef = targetRef1;
				if (logComponentRef != null && logComponentRef.equals(targetRef)) {
					break;
				}
			}
		}
		return targetRef;
	}

	/**
	 * Creates a component whose component creation log record has not been found.
	 */
	private void addDummyComponent(final String compRef) {
		if (components.get(compRef) != null) {
			return;
		}

		EventObject object = new EventObject(EventType.PTC_CREATE);
		object.setReference(compRef);
		object.setName(compRef);
		object.setRecordNumber(0);
		object.setEventNumber(0);
		addComponent(object);
		executionModel.addLifeLineInfo(object);
		this.ptcs++;
	}

	/**
	 * Parse a single log record.
	 *
	 * @param logRecord the record to analyze.
	 * @param eventIndex the index of the event to be used in setverdict handling.
	 * */
	private EventObject parseLogRecord(final LogRecord logRecord, final int eventIndex) {
		final String message = logRecord.getMessage();
		this.messageAnalyser.setMessage(message);

		// TODO remove filtering from this function
		
		boolean isLogRecordIgnored = false;
		if (executionModel.getFilterPattern() != null) {
			isLogRecordIgnored = !executionModel.getFilterPattern().match(logRecord);
		}
		
		// This if-else branch is ordered so that the most common
		// types of events are checked first and the less common
		// ones in dropping order. Send and receives are the most
		// interesting events we have in any test system.

		// This branch handles send events.
		if (this.messageAnalyser.isSend()) {
			String sendType = messageAnalyser.getSendType();
			if (!isSignalIgnored(sendType) && components != null) {
				String sendTarget = messageAnalyser.getSendTarget();
				TestComponent tc = components.get(sendTarget);
				if (tc != null) {
					String logComponentRef = logRecord.getComponentReference();
					Set<String> sources = tc.getMappedFromReference(messageAnalyser.getSendSource());
					String source =  getSource(logComponentRef, sources);

					if (source != null && !isComponentIgnored(source)) {
						sends++;
						EventObject event = createEventObject(logRecord, EventType.SEND);
						String deciphered = decipherer.decipher(sendType.trim(), messageAnalyser.getSendValue());
						if (deciphered != null) {
							event.setName(deciphered);
						} else {
							event.setName(sendType);
						}

						event.setReference(source);
						event.setTarget(sendTarget);
						if (tc.getAlternative() != null) {
							event.setTarget(tc.getAlternative());
						}
						return event;
					}
				}
			}
		} else if (messageAnalyser.isReceive()) {
			// This branch handles receive events.
			String recieveType = messageAnalyser.getReceiveType();
			if (!isSignalIgnored(recieveType) && !isLogRecordIgnored && components != null) {
				String sourceRef = messageAnalyser.getReceiveSource();
				TestComponent tc = components.get(sourceRef);
				if (tc != null) {
					String recieveTargetPort = messageAnalyser.getReceiveTarget();
					String logComponentRef = logRecord.getComponentReference();
					Set<String> targetRefs = tc.getMappedFromReference(recieveTargetPort);
					String targetRef = getTargetRef(logComponentRef, targetRefs);

					if (targetRef != null && !isComponentIgnored(targetRef)) {
						return handleReceive(logRecord, recieveType, sourceRef, tc, recieveTargetPort, targetRef);
					}
				}
			}
		} else if (messageAnalyser.isEnqueued()) {
			// This branch handles enqueued events.
			String recieveType = messageAnalyser.getReceiveType();
			if (!isSignalIgnored(recieveType) && !isLogRecordIgnored && components != null) {
				String sourceRef = messageAnalyser.getReceiveSource();
				TestComponent tc = components.get(sourceRef);
				if (tc != null) {
					String enqueuedTargetPort = messageAnalyser.getEnqueuedTarget();
					String logComponentRef = logRecord.getComponentReference();
					Set<String> targetRefs = tc.getMappedFromReference(enqueuedTargetPort);

					// In case of valid log files it cannot happen.
					String targetRef = getTargetRef(logComponentRef, targetRefs);

					if (targetRef != null && !isComponentIgnored(targetRef)) {
						enqs++;
						EventObject event = createEventObject(logRecord, EventType.ENQUEUED);
						String msg = messageAnalyser.getReceiveValue();

						String deciphered = decipherer.decipher(recieveType, msg);
						if (deciphered != null) {
							event.setName(deciphered);
						} else {
							event.setName(recieveType);
						}
						event.setReference(sourceRef);
						event.setTarget(targetRef);
						event.setPort(enqueuedTargetPort);
						if (tc.getAlternative() != null) {
							event.setReference(tc.getAlternative());
						}
						return event;
					}
				}
			}
		} else if (this.messageAnalyser.isReceiveOperation()) {
			// This branch handles receive operation events. AFAIK,
			// it is obsolete in new (>= 1.7.pl0) TITAN versions.
			String recieveType = messageAnalyser.getReceiveOperationType();
			if (!isSignalIgnored(recieveType) && components != null) {
				String sourceRef = messageAnalyser.getReceiveSource();
				TestComponent tc = components.get(sourceRef);
				if (tc != null) {
					String receiveTargetPort = messageAnalyser.getReceiveOperationTarget();
					String logComponentRef = logRecord.getComponentReference();
					Set<String> targetRefs = tc.getMappedFromReference(receiveTargetPort);
					String targetRef = getTargetRef(logComponentRef, targetRefs);

					if (targetRef != null && !isComponentIgnored(targetRef)) {
						return handleReceive(logRecord, recieveType, sourceRef, tc, receiveTargetPort, targetRef);
					}
				}
			}
		} else if (this.messageAnalyser.isStartFunction()) {
			// This branch handles function start events.
			if (components != null) {
				String targetRef = this.messageAnalyser.getStartFunctionReference();
				if ((targetRef != null)
						&& !isComponentIgnored(targetRef)
						&& !isFunctionIgnored(this.messageAnalyser.getStartFunctionName())) {
					EventObject event = createEventObject(logRecord, EventType.FUNCTION);
					event.setName(this.messageAnalyser.getStartFunctionName());
					event.setReference(Constants.MTC_REFERENCE);
					event.setTarget(targetRef);
					return event;
				}
			}
		} else if (this.messageAnalyser.isComponentCreation()) {
			// This branch handles component creation events.
			if (components != null) {
				EventObject event = createEventObject(logRecord, EventType.PTC_CREATE);
				String reference = event.getReference();
				TestComponent tc = components.get(reference);
				if (tc == null || tc.getRecordNumber() == logRecord.getRecordNumber()) {
					if ((reference != null)
							&& !isComponentIgnored(reference)) {
						return event;
					}
				} else {
					if (!isComponentIgnored(reference)) {
						logRecord.setComponentReference(reference);
						event = eventObjectFactory.createEventObject(EventType.SILENT_EVENT, logRecord, this.messageAnalyser, this.logFileMetaData.getTimeStampConstant());
						return event;
					}
				}
			}
		} else if (this.messageAnalyser.isComponentDone()) {
			// This branch handles component done events.
			EventObject event = createEventObject(logRecord, EventType.PTC_DONE);

			String reference = event.getReference();
			if ((reference != null)
					&& !isComponentIgnored(reference)) {
				return event;
			}
		} else if (this.messageAnalyser.isComponentTermination()) {
			// This branch handles component termination events.
			if (components != null) {
				String ref = this.messageAnalyser.getComponentTerminationReference();
				if ((ref != null) && !isComponentIgnored(ref)) {
					String terminationVerdict = this.messageAnalyser.getComponentTerminationVerdict();
					TestComponent component = components.get(ref);
					if (component != null) {
						component.setVerdict(terminationVerdict);
					}

					EventObject event = createEventObject(logRecord, EventType.PTC_TERMINATE);
					event.setReference(ref);
					event.setName(terminationVerdict);
					return event;
				}
			}
		} else if (this.messageAnalyser.isPortMapping()) {
			// This branch handles port mapping events.
			String mappingSource = this.messageAnalyser.getPortMappingSource();
			final String compRef = this.messageAnalyser.getComponentRef(mappingSource);

			String target = this.messageAnalyser.getPortMappingTarget();
			String targetRef = this.messageAnalyser.getComponentRef(target);
			String sourcePort = this.messageAnalyser.getPort(mappingSource);
			String targetPort = this.messageAnalyser.getPort(target);

			if (!filterMappingPorts 
					&& !(isComponentIgnored(compRef) || isComponentIgnored(targetRef))) {

				logRecord.setComponentReference(compRef);
				EventObject event = createEventObject(logRecord, EventType.MAPPING_PORT);
				event.setPort(sourcePort);
				event.setTargetPort(targetPort);
				event.setTarget(targetRef);
				event.setName(MAPPING);
				return event;
			}
			// Used for mapping ports to reference
			addPortMapping(compRef, sourcePort, targetRef, targetPort);
		} else if (this.messageAnalyser.isPortUnmapping()) {
			// This branch handles port unmapping events.
			String unmappingSource = this.messageAnalyser.getPortUnMapping();
			String compRef = this.messageAnalyser.getComponentRef(unmappingSource);
			compRef = this.messageAnalyser.getComponentRef(compRef);

			String target = this.messageAnalyser.getPortUnMappingTarget();
			String targetRef = this.messageAnalyser.getComponentRef(target);
			String sourcePort = this.messageAnalyser.getPort(unmappingSource);
			String targetPort = this.messageAnalyser.getPort(target);

			if (!filterMappingPorts 
					&& !(isComponentIgnored(compRef) || isComponentIgnored(targetRef))) {
				logRecord.setComponentReference(compRef);
				EventObject event = createEventObject(logRecord, EventType.UNMAPPING_PORT);
				event.setPort(sourcePort);
				event.setTargetPort(targetPort);
				event.setTarget(targetRef);
				event.setName(UNMAPPING);
				return event;
			}

			// Used for mapping ports to reference
		} else if (this.messageAnalyser.isPortConnection()) {
			// This branch handles port connection events.
			String connectionSource = this.messageAnalyser.getPortConnectionSource();
			final String compRef = this.messageAnalyser.getComponentRef(connectionSource);

			// Used for connect port and reference
			String target = this.messageAnalyser.getPortConnectionTarget();
			String targetRef = this.messageAnalyser.getComponentRef(target);
			String sourcePort = this.messageAnalyser.getPort(connectionSource);
			String targetPort = this.messageAnalyser.getPort(target);

			if (!filterConnectingPorts 
					&& !(isComponentIgnored(compRef) || isComponentIgnored(targetRef))) {
				logRecord.setComponentReference(compRef);
				EventObject event = createEventObject(logRecord, EventType.CONNECTING_PORT);
				event.setPort(sourcePort);
				event.setTargetPort(targetPort);
				event.setTarget(targetRef);
				event.setName(CONNECTING);
				return event;
			}
			addPortMapping(compRef, sourcePort, targetRef, targetPort);
		} else if (this.messageAnalyser.isPortDisconnection()) {
			// This branch handles port disconnection events.
			String disconnectionSource = this.messageAnalyser.getPortDisconnectionSource();
			final String compRef = this.messageAnalyser.getComponentRef(disconnectionSource);
			// Used for disconnect port and reference
			String target = this.messageAnalyser.getPortConnectionTarget();
			String targetRef = this.messageAnalyser.getComponentRef(target);
			String sourcePort = this.messageAnalyser.getPort(disconnectionSource);
			String targetPort = this.messageAnalyser.getPort(target);

			if (!filterConnectingPorts 
					&& !(isComponentIgnored(compRef) || isComponentIgnored(targetRef))) {
				logRecord.setComponentReference(compRef);
				EventObject event = createEventObject(logRecord, EventType.DISCONNECTING_PORT);
				event.setPort(sourcePort);
				event.setTargetPort(targetPort);
				event.setTarget(targetRef);
				event.setName(DISCONNECTING);
				return event;
			}
		} else if (this.messageAnalyser.isTestcaseStart()) {
			// This branch handles test case start events.
			return eventObjectFactory.createEventObject(EventType.TC_START, logRecord, this.messageAnalyser, this.logFileMetaData.getTimeStampConstant());
		} else if (this.messageAnalyser.isTestcaseEnd()) {
			// This branch handles test case end events.
			return eventObjectFactory.createEventObject(EventType.TC_END, logRecord, this.messageAnalyser, this.logFileMetaData.getTimeStampConstant());
		} else if (this.messageAnalyser.isMTCCreation()) {
			EventObject event = createEventObject(logRecord, EventType.MTC_CREATE);
			addComponent(event);
			return event;
		} else if (this.messageAnalyser.isMTCTermination()) {
			// This branch handles master test component termination events.
			return eventObjectFactory.createEventObject(EventType.MTC_TERMINATE, logRecord, this.messageAnalyser, this.logFileMetaData.getTimeStampConstant());
		} else if (this.messageAnalyser.isMTCDone()) {
			// This branch handles master test component done and sets the verdict.
			if (components != null) {
				TestComponent component = components.get(Constants.MTC_REFERENCE);
				if (component != null) {
					String verdict = this.messageAnalyser.getMTCVerdict();
					component.setVerdict(verdict);
				}
			}
			EventObject event = createEventObject(logRecord, EventType.SILENT_EVENT);
			event.setReference(Constants.MTC_REFERENCE);
			return event;
		} else if (this.messageAnalyser.isSetverdict()) {

			if ((logRecord.getComponentReference() != null)
					&& !isComponentIgnored(logRecord.getComponentReference())) {

				// Get type - change type inconc
				String setverdicttype = this.messageAnalyser.getSetverdictType();
				if (Constants.TEST_CASE_VERDICT_INCONCLUSIVE.equals(setverdicttype)
						&& displaySetverdictInconc) {
					setverdictVector.add(eventIndex);
					EventObject event = createEventObject(logRecord, EventType.SETVERDICT_INCONC);
					event.setName(setverdicttype);
					return event;
				} else if (Constants.TEST_CASE_VERDICT_ERROR.equals(setverdicttype)
						&& displaySetverdictError) {
					setverdictVector.add(eventIndex);
					EventObject event = createEventObject(logRecord, EventType.SETVERDICT);
					event.setName(setverdicttype);
					ConnectedRecord[] connectedRecords = errorVector.toArray(new ConnectedRecord[errorVector.size()]);
					event.setConnectedRecords(connectedRecords);
					return event;
				} else if (Constants.TEST_CASE_VERDICT_FAIL.equals(setverdicttype)
						&& displaySetverdictFail) {
					setverdictVector.add(eventIndex);
					EventObject event = createEventObject(logRecord, EventType.SETVERDICT);
					event.setName(setverdicttype);
					ConnectedRecord[] connectedRecords = failVector.toArray(new ConnectedRecord[failVector.size()]);
					event.setConnectedRecords(connectedRecords);
					return event;
				} else if (Constants.TEST_CASE_VERDICT_NONE.equals(setverdicttype)
						&& displaySetverdictNone) {
					setverdictVector.add(eventIndex);
					EventObject event = createEventObject(logRecord, EventType.SETVERDICT_NONE);
					event.setName(setverdicttype);
					return event;
				} else if (Constants.TEST_CASE_VERDICT_PASS.equals(setverdicttype)
						&& displaySetverdictPass) {
					setverdictVector.add(eventIndex);
					EventType setverdictPass = EventType.SETVERDICT_PASS;
					EventObject event = createEventObject(logRecord, setverdictPass);
					event.setName(setverdicttype);
					return event;
				}

				errorVector.clear();
				failVector.clear();
				

			}
		} else {
			// Silent events
			Boolean filtered = isEventIgnored(logRecord.getEventType());
			if (!filtered) {
				// component reference exists in the log record
				if ((logRecord.getComponentReference() != null)
						&& (logRecord.getComponentReference().trim().length() > 0)) {
					return eventObjectFactory.createEventObject(EventType.SILENT_EVENT, logRecord, this.messageAnalyser, this.logFileMetaData.getTimeStampConstant());
				} else {
					final String compRef = this.messageAnalyser.isSilentEvent();
					logRecord.setComponentReference(compRef != null ? compRef : Constants.MTC_REFERENCE);
					return eventObjectFactory.createEventObject(EventType.SILENT_EVENT, logRecord, this.messageAnalyser, this.logFileMetaData.getTimeStampConstant());
				}
			}
		}
		// check if Dynamic test case error, used for
		// setverdict(error)
		if (this.messageAnalyser.isDynamicTestCaseError()) {
			ConnectedRecord connectedRecord = new ConnectedRecord(logRecord.getRecordOffset(), logRecord.getRecordLength(), logRecord.getRecordNumber());
			errorVector.add(connectedRecord);
		} // check if fail messages, used for setverdict(fail)
		if (this.messageAnalyser.isFailMessages()) {
			ConnectedRecord connectedRecord = new ConnectedRecord(logRecord.getRecordOffset(), logRecord.getRecordLength(), logRecord.getRecordNumber());
			failVector.add(connectedRecord);
		}

		return null;
	}

	private EventObject createEventObject(LogRecord logRecord, EventType eventType) {
		return eventObjectFactory.createEventObject(eventType, logRecord, this.messageAnalyser, this.logFileMetaData.getTimeStampConstant());
	}

	private EventObject handleReceive(LogRecord logRecord, String receiveType, String sourceRef, TestComponent tc, String receiveTargetPort, String targetRef) {
		recs++;
		EventObject event = createEventObject(logRecord, EventType.RECEIVE);
		String deciphered = decipherer.decipher(receiveType, messageAnalyser.getReceiveValue());
		if (deciphered != null) {
			event.setName(deciphered);
		} else {
			event.setName(receiveType);
		}
		event.setReference(sourceRef);
		event.setTarget(targetRef);
		event.setPort(receiveTargetPort);
		if (tc.getAlternative() != null) {
			event.setReference(tc.getAlternative());
		}
		return event;
	}


	private void notifyChange() {
		this.executionModel.notifyChange();
	}

	private void addPortMapping(final String portMappingSourceRef,
			final String portMappingSourcePort, final String portMappingTargetRef,
			final String portMappingTargetPort) {
		this.executionModel.addPortMapping(portMappingSourceRef,
				portMappingSourcePort, portMappingTargetRef,
				portMappingTargetPort);
	}

	private void addComponent(final EventObject event) {
		this.executionModel.addComponent(event);
	}

	private boolean isComponentIgnored(final String source) {
		return this.executionModel.isComponentIgnored(source);
	}

	private boolean isSignalIgnored(final String sendType) {
		return this.executionModel.isSignalIgnored(sendType);
	}

	private boolean isFunctionIgnored(final String sendType) {
		return this.executionModel.isFunctionIgnored(sendType);
	}
	
	private boolean isEventIgnored(final String event) {
		if (this.executionModel.getFilterPattern() != null) {
			return this.executionModel.isEventIgnored(event);
		}

		return filteredSilentEvents.get(event);
	}

	/**
	 * Sets the deciphering rule set
	 * @see Decipherer#setDecipheringRuleset(String)
	 */
	public boolean setDecipheringRuleset(final String name) {
		return decipherer.setDecipheringRuleset(name);
	}
}
