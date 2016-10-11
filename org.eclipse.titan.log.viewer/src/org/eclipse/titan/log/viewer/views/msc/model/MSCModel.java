/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.model;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.titan.log.viewer.models.LogFileMetaData;
import org.eclipse.titan.log.viewer.parsers.data.LogRecord;
import org.eclipse.titan.log.viewer.readers.ValueReader;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.views.msc.ui.core.ComponentCreation;
import org.eclipse.titan.log.viewer.views.msc.ui.core.ComponentTermination;
import org.eclipse.titan.log.viewer.views.msc.ui.core.Enqueued;
import org.eclipse.titan.log.viewer.views.msc.ui.core.Frame;
import org.eclipse.titan.log.viewer.views.msc.ui.core.Function;
import org.eclipse.titan.log.viewer.views.msc.ui.core.FunctionDone;
import org.eclipse.titan.log.viewer.views.msc.ui.core.FunctionNode;
import org.eclipse.titan.log.viewer.views.msc.ui.core.Lifeline;
import org.eclipse.titan.log.viewer.views.msc.ui.core.LifelineHeader;
import org.eclipse.titan.log.viewer.views.msc.ui.core.MSCNode;
import org.eclipse.titan.log.viewer.views.msc.ui.core.PortConnection;
import org.eclipse.titan.log.viewer.views.msc.ui.core.PortDisconnection;
import org.eclipse.titan.log.viewer.views.msc.ui.core.PortEventNode;
import org.eclipse.titan.log.viewer.views.msc.ui.core.PortMapping;
import org.eclipse.titan.log.viewer.views.msc.ui.core.PortUnmapping;
import org.eclipse.titan.log.viewer.views.msc.ui.core.ReceiveSignal;
import org.eclipse.titan.log.viewer.views.msc.ui.core.SendSignal;
import org.eclipse.titan.log.viewer.views.msc.ui.core.SetverdictComp;
import org.eclipse.titan.log.viewer.views.msc.ui.core.SetverdictUnknown;
import org.eclipse.titan.log.viewer.views.msc.ui.core.Signal;
import org.eclipse.titan.log.viewer.views.msc.ui.core.SilentEvent;
import org.eclipse.titan.log.viewer.views.msc.ui.core.TestCaseEnd;
import org.eclipse.titan.log.viewer.views.msc.ui.core.TestCaseStart;
import org.eclipse.titan.log.viewer.views.msc.ui.core.TimeStampNode;
import org.eclipse.titan.log.viewer.views.msc.util.MSCConstants;

/**
 * Model for MSC View
 * <p>
 * These shift things are a hack but needed in order to properly handle artificial nodes.
 */
public class MSCModel {
	
	private ExecutionModel model;
	private Map<String, Lifeline> lifelines;
	private Lifeline sutLifeline;
	private Frame frame;
	private URI logFilePath;
	private String sutName;  
	private static final int MAX_CHARS = 100;

	/**
	 * Constructor
	 * 
	 * @param model the execution model
	 * @param logFileMetaData the log file metadata (need for silent event tooltip)
	 * @param sutName the name of the SUT
	 */
	public MSCModel(final ExecutionModel model, final LogFileMetaData logFileMetaData, final String sutName) {
		this.model = model;
		this.logFilePath = logFileMetaData.getFilePath();
		this.sutName = sutName;
		this.lifelines = new HashMap<String, Lifeline>();
	}

	/**
	 * 
	 * @return the frame
	 */
	public Frame getModelFrame() {
		if (this.model == null) {
			return null;
		}
		
		this.frame = new Frame(this);
		this.frame.setName(this.model.getTestCase().getTestCaseName());

		List<EventObject> events = this.model.getLifelineInformation();
		if (sutLifeline == null) {
			this.sutLifeline = new Lifeline();
		}

		for (EventObject event : events) {
			int eventNumber = event.getEventNumber();
			sutLifeline.setCurrentEventOccurrence(eventNumber);
			extractLifeLineAndHeaderNodes(event, eventNumber);
		}

		sutLifeline.setCurrentEventOccurrence(model.getNumberOfEvents());

		return this.frame;
	}

	/**
	 * Collects the nodes that belong to the range provided.
	 *
	 * @param startIndex the index of the first event to process.
	 * @param endIndex the index of the last event to process.
	 *
	 * @return the list of the found nodes.
	 * */
	public List<MSCNode> getNodes(final int startIndex, final int endIndex) {
		if (sutLifeline == null) {
			this.sutLifeline = new Lifeline();
		}

		List<IEventObject> events = this.model.getEvents(startIndex, endIndex);
		List<MSCNode> result = new ArrayList<MSCNode>();
		for (int i = 0; i < 3; i++) {
			MSCNode[] nodes = extractNodes(events.get(i), i);
			for (MSCNode node : nodes) {
				if (!(node instanceof Lifeline) && !(node instanceof LifelineHeader)) {
					result.add(node);
				}
			}
		}
		for (int i = 3; i < events.size(); i++) {
			MSCNode[] nodes = extractNodes(events.get(i), startIndex + i);
			for (MSCNode node : nodes) {
				if (!(node instanceof Lifeline) && !(node instanceof LifelineHeader)) {
					result.add(node);
				}
			}
		}

		return result;
	}

	/**
	 * Extracts the lifeline and lifelineheader nodes from the provided event object.
	 *
	 * @param ievent the event to extract the data from.
	 * @param occurrence the number at which time this event happened in its order.
	 *
	 * */
	private void extractLifeLineAndHeaderNodes(final IEventObject ievent, final int occurrence) {
		if (!(ievent instanceof EventObject)) {
			return;
		}

		EventObject event = (EventObject) ievent;
		String ref = event.getReference();
		String time = event.getTime();
		switch (event.getType()) {
		// Creation of System Component
		case SYSTEM_CREATE:
			ref = Constants.SUT_REFERENCE;
			createLifelineAndHeaderComponent(ref, time, this.sutLifeline, occurrence);
			break;
			// Creation of Host Controller (HC)
		case HC_CREATE:
			ref = Constants.HC_REFERENCE;
			createLifelineAndHeaderComponent(ref, time, new Lifeline(), occurrence);
			break;
			// Creation of Main Test Component (MTC)
		case MTC_CREATE:
			ref = Constants.MTC_REFERENCE;
			if (this.lifelines.get(ref) == null) {
				createLifelineAndHeaderComponent(ref, time, new Lifeline(), occurrence);
			} else {
				createLifelineAndHeaderComponent(ref, time, this.lifelines.get(ref), occurrence);
			}
			break;
		// Creation of Parallel Test Component (PTC)
		case PTC_CREATE:
			createLifelineAndHeaderComponent(ref, time, new Lifeline(), occurrence);
			break;
			// Termination of System Component
		case SYSTEM_TERMINATE:
			ref = Constants.SUT_REFERENCE;
			terminateLifeLineComponent(ref, time, occurrence);
			break;
			// Termination of Host Controller (HC)
		case HC_TERMINATE:
			ref = Constants.HC_REFERENCE;
			terminateLifeLineComponent(ref, time, occurrence);
			break;
			// Termination of Main Test Component (MTC)
		case MTC_TERMINATE:
			ref = Constants.MTC_REFERENCE;
			terminateLifeLineComponent(ref, time, occurrence);
			break;		
			// Termination of Parallel Test Component (PTC)
		case PTC_TERMINATE:
			terminateLifeLineComponent(ref, time, occurrence);
			break;
		default:
			break;
		}
	}

	private void createLifelineAndHeaderComponent(final String ref, final String time, final Lifeline lifeline, final int occurrence) {
		if (this.lifelines.containsKey(ref)) {
			return;
		}

		// Add life line
		this.lifelines.put(ref, lifeline);
		// Get and set name
		String name = getComponentNameFromReference(ref);
		//Set name without ref so that the visual order works
		lifeline.setName(name);
		this.frame.addLifeLine(lifeline);
		
		// Create and add Header
		LifelineHeader lifeLineHeader = new LifelineHeader(lifeline, this.frame);
		if (!name.contentEquals(this.sutName)
				&& !name.contentEquals(MSCConstants.MTC_NAME)
				&& !name.contentEquals(ref)) {
			name = name + " (" + ref + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		lifeLineHeader.setName(name);
		this.frame.addLifeLineHeader(lifeLineHeader);

		// Create and add Start
		lifeline.setCurrentEventOccurrence(occurrence);
		ComponentCreation start = new ComponentCreation(occurrence, lifeline);
		start.setName(name);
		lifeline.setStart(start);
	}

	private void terminateLifeLineComponent(final String ref, final String time, final int occurrence) {
		// Get life line
		Lifeline tmpLifeline = this.lifelines.get(ref);
		if (tmpLifeline == null) {
			return;
		}
		
		// Get name
		String name = getComponentNameFromReference(ref);
		if (!name.contentEquals(this.sutName)
		   && !name.contentEquals(MSCConstants.MTC_NAME)
		   && !name.contentEquals(ref)) {
			
			name = name + " (" + ref + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Create and add stop
		String verdict = getComponentVerdictFromReference(ref);
		ComponentTermination stop = new ComponentTermination(occurrence, tmpLifeline, verdict);
		stop.setName(name);
		tmpLifeline.setStop(stop);
	}

	/**
	 * Extracts the nodes from the provided event object.
	 *
	 * @param ievent the event to extract the data from.
	 * @param occurrence the number at which time this event happened in its order.
	 *
	 * @return the resulting list of nodes.
	 * */
	private MSCNode[] extractNodes(final IEventObject ievent, final int occurrence) {
		if (!(ievent instanceof EventObject)) {
			return new MSCNode[] {};
		}

		EventObject event = (EventObject) ievent;
		String ref = event.getReference();
		String target = event.getTarget();
		String name = event.getName();
		String time = event.getTime();
		String type = event.getEventType();
		String sourcePort = event.getPort();
		String targetPort = event.getTargetPort();
		switch (event.getType()) {
		
			// TC start
			case TC_START:
				return testCaseStart(name, time, this.frame.lifeLinesCount(), occurrence);
				
			// TC end
			case TC_END:
				return testCaseEnd(name, time, this.frame.lifeLinesCount(), occurrence);
		
			// Creation of System Component
			case SYSTEM_CREATE:
				ref = Constants.SUT_REFERENCE;
				return createComponent(ref, time, this.lifelines.get(ref), occurrence);
				// Creation of Host Controller (HC)
			case HC_CREATE:
				ref = Constants.HC_REFERENCE;
				return createComponent(ref, time, this.lifelines.get(ref), occurrence);

			// Creation of Main Test Component (MTC)
			case MTC_CREATE: {
				ref = Constants.MTC_REFERENCE;
				Lifeline lifeLine = this.lifelines.get(ref);
				return createComponent(ref, time, lifeLine, occurrence);
			}
			// Creation of Parallel Test Component (PTC)
			case PTC_CREATE:
				return createComponent(ref, time, this.lifelines.get(ref), occurrence);

			// Termination of System Component
			case SYSTEM_TERMINATE:
				ref = Constants.SUT_REFERENCE;
				return terminateComponent(ref, time, occurrence);

				// Termination of Host Controller (HC)
			case HC_TERMINATE:
				ref = Constants.HC_REFERENCE;
				return terminateComponent(ref, time, occurrence);

			// Termination of Main Test Component (MTC)
			case MTC_TERMINATE:
				ref = Constants.MTC_REFERENCE;
				return terminateComponent(ref, time, occurrence);
			
			// Termination of Parallel Test Component (PTC)
			case PTC_TERMINATE:
				return terminateComponent(ref, time, occurrence);
				
			// Messages
			case SEND:
				return addSignal(new SendSignal(), ref, target, name, time, occurrence);
			case RECEIVE:
				return addSignal(new ReceiveSignal(), ref, target, name, time, occurrence);
				
			// Enqueued messages
			case ENQUEUED:
				return addEnqueued(ref, target, name, time, occurrence);

			// Silent events
			case SILENT_EVENT:
				LogRecord logrecord = null;
				try {
					logrecord = ValueReader.getInstance().readLogRecordFromLogFile(this.logFilePath, event);
				} catch (final IOException valueException) {
					return addSilentEvent(ref, "", type, time, occurrence);
				} catch (final ParseException e) {
					return addSilentEvent(ref, "", type, time, occurrence);
				}

				String messageText = getMessageTextFromRecord(logrecord);

				return addSilentEvent(ref, messageText, type, time, occurrence);

			// Functions
			case FUNCTION:
				return addFunctionNode(new Function(), ref, target, name, time, occurrence);

			// Function done
			case PTC_DONE:
				return addFunctionNode(new FunctionDone(), ref, target, name, time, occurrence);

			// Setverdict
			case SETVERDICT:
			case SETVERDICT_INCONC:
			case SETVERDICT_NONE:
			case SETVERDICT_PASS:
				return addSetVerdict(ref, name, time, occurrence);
				
			// Port mappings
			case MAPPING_PORT:
				return addPortEventNode(new PortMapping(sourcePort, targetPort), ref, target, time, occurrence);

			// Port unmappings
			case UNMAPPING_PORT:
				return addPortEventNode(new PortUnmapping(sourcePort, targetPort), ref, target, time, occurrence);

			// Port connections
			case CONNECTING_PORT:
				return addPortEventNode(new PortConnection(sourcePort, targetPort), ref, target, time, occurrence);

			// Port disconnections 
			case DISCONNECTING_PORT:
				return addPortEventNode(new PortDisconnection(sourcePort, targetPort), ref, target, time, occurrence);

			default:
				return new MSCNode[]{};
		}
	}

	private String getMessageTextFromRecord(LogRecord logrecord) {
		String messageText = "";
		if (logrecord != null) {
			messageText = logrecord.getMessage();
		}
		messageText = messageText.trim();
		messageText = messageText.replaceAll("\r\n", "\n");
		// Limit number of characters in tool tip, otherwise Value view should be used
		if (messageText.length() > MAX_CHARS) {
			messageText = messageText.substring(0, MAX_CHARS).trim() + " ...\n\n for more, use Value View."; //$NON-NLS-1$
		}
		return messageText;
	}

	private MSCNode[] testCaseStart(final String name, final String time, final int width, final int occurrence) {
		// Create Test Case Start
		TestCaseStart testCaseStart = new TestCaseStart(occurrence, width);
		testCaseStart.setName(name);

		// Create and add Time Stamp
		return new MSCNode[] {testCaseStart, new TimeStampNode(occurrence, time)};
	}

	private MSCNode[] testCaseEnd(final String name, final String time, final int width, final int occurrence) {
		// Create Test Case End
		TestCaseEnd testCaseEnd = new TestCaseEnd(occurrence, name, width);
		testCaseEnd.setName(name);
		
		// Create and add Time Stamp
		return new MSCNode[] {testCaseEnd, new TimeStampNode(occurrence, time)};
	}

	private MSCNode[] createComponent(final String ref, final String time, final Lifeline lifeline, final int occurrence) {
			String name = getComponentNameFromReference(ref);
			if (!name.contentEquals(this.sutName)
					&& !name.contentEquals(MSCConstants.MTC_NAME)
					&& !name.contentEquals(ref)) {		
						name = name + " (" + ref + ")"; //$NON-NLS-1$ //$NON-NLS-2$
					}
			ComponentCreation start = new ComponentCreation(occurrence, lifeline);
			start.setName(name);
			return new MSCNode[] {start, new TimeStampNode(occurrence, time)};
	}
	
	private MSCNode[] terminateComponent(final String ref, final String time, final int occurrence) {
		// Get life line
		Lifeline tmpLifeline = this.lifelines.get(ref);
		if (tmpLifeline == null) {
			return new MSCNode[] {};
		}
		
		// Get name
		String name = getComponentNameFromReference(ref);
		if (!name.contentEquals(this.sutName)
		   && !name.contentEquals(MSCConstants.MTC_NAME)
		   && !name.contentEquals(ref)) {
			
			name = name + " (" + ref + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Create and add stop
		String verdict = getComponentVerdictFromReference(ref);
		ComponentTermination stop = new ComponentTermination(occurrence, tmpLifeline, verdict);
		stop.setName(name);
		tmpLifeline.setStop(stop);
		
		// Create and add Time Stamp
		return new MSCNode[] {stop, new TimeStampNode(occurrence, time)};
	}

	private MSCNode[] addSignal(final Signal signal, final String ref, final String target, final String name, final String time, final int occurrence) {
		Lifeline source = this.lifelines.get(ref);
		Lifeline dest = this.lifelines.get(target);
		if ((source == null) || (dest == null)) {
			return new MSCNode[] {};
		}

		source.setCurrentEventOccurrence(occurrence);
		dest.setCurrentEventOccurrence(occurrence);
		signal.setStartLifeline(source);
		signal.setEndLifeline(dest);
		signal.setName(name);

		// Create and add Time Stamp
		return new MSCNode[] {signal, new TimeStampNode(occurrence, time)};
	}
	
	private MSCNode[] addEnqueued(final String ref, final String target, final String name, final String time, final int occurrence) {
		Enqueued message = new Enqueued();
		Lifeline source = this.lifelines.get(ref);
		Lifeline dest = this.lifelines.get(target);
		if ((source == null) || (dest == null)) {
			return new MSCNode[] {};
		}

		source.setCurrentEventOccurrence(occurrence);
		dest.setCurrentEventOccurrence(occurrence);
		message.setStartLifeline(source);
		message.setEndLifeline(dest);
		message.setName(name);

		// Create and add Time Stamp

		return new MSCNode[] {message, new TimeStampNode(occurrence, time)};
	}
	
	private MSCNode[] addSilentEvent(final String ref, final String name, final String type, final String time, final int occurrence) {
		String silentEventType = null;
		Set<String> types = Constants.EVENT_CATEGORIES.keySet();
		for (String currType : types) {
			if (type.startsWith(currType)) {
				silentEventType = currType;
				break;
			}
		}
		if (silentEventType == null) {
			return new MSCNode[] {};
		}
		
		// Get life line
		Lifeline tmpLifeline = this.lifelines.get(ref);
		if (tmpLifeline == null) {
			return new MSCNode[] {};
		}

		// Create and add silent event
		SilentEvent silentEvent = new SilentEvent(occurrence, tmpLifeline, type);
		silentEvent.setName(type + "\n" + name); //$NON-NLS-1$

		// Create and add Time Stamp
		return new MSCNode[] {silentEvent, new TimeStampNode(occurrence, time)};
	}

	private MSCNode[] addFunctionNode(FunctionNode function, String ref, String target, String name, String time, int occurrence) {
		Lifeline source = this.lifelines.get(ref);
		Lifeline destination = this.lifelines.get(target);
		if ((source == null) || (destination == null)) {
			return new MSCNode[] {};
		}

		source.setCurrentEventOccurrence(occurrence);
		destination.setCurrentEventOccurrence(occurrence);
		function.setStartLifeline(source);
		function.setEndLifeline(destination);
		function.setName(name);

		// Create and add Time Stamp
		return new MSCNode[] {function, new TimeStampNode(occurrence, time)};
	}

	private MSCNode[] addPortEventNode(PortEventNode portEventNode, String ref, String target, String time, int occurrence) {
		Lifeline source = this.lifelines.get(ref);
		Lifeline destination = this.lifelines.get(target);
		if ((source == null) || (destination == null)) {
			return new MSCNode[] {};
		}

		source.setCurrentEventOccurrence(occurrence);
		destination.setCurrentEventOccurrence(occurrence);
		portEventNode.setStartLifeline(source);
		portEventNode.setEndLifeline(destination);

		// Create and add Time Stamp
		return new MSCNode[] {portEventNode, new TimeStampNode(occurrence, time)};
	}

	private MSCNode[] addSetVerdict(final String ref, final String name, final String time, final int occurrence) {
		MSCNode[] temp = new MSCNode[2];
		if ((ref == null) || (ref.length() == 0)) {
			SetverdictUnknown setverdictUnknown = new SetverdictUnknown(occurrence, name);
			setverdictUnknown.setName(name);
			temp[0] = setverdictUnknown;
		} else {
			// Get life line
			Lifeline tmpLifeline = this.lifelines.get(ref);
			if (tmpLifeline == null) {
				return new MSCNode[] {};
			}
			SetverdictComp setverdictComp = new SetverdictComp(occurrence, tmpLifeline, name);
			setverdictComp.setName(name);
			temp[0] = setverdictComp;
		}
		
		// Create and add Time Stamp
		temp[1] = new TimeStampNode(occurrence, time);
		return temp;
	}
	
	
	/**
	 * This method is used to extract the name from a given
	 * component once the component reference is known.
	 */
	private String getComponentNameFromReference(final String ref) {
		if (ref.contentEquals(Constants.MTC_REFERENCE)) {
			return MSCConstants.MTC_NAME;
		} else if (ref.contentEquals(Constants.SUT_REFERENCE)) {
			return this.sutName;
		}
		Map<String, TestComponent> components = this.model.getComponents();
		if (components != null) {
			TestComponent component = components.get(ref);
			if (component != null) {
				if (component.getName().trim().length() > 0) {
					return component.getName();
				} 

				return ref;
			}
		}
		return ref;
	}	
	
	/**
	 * This method is used to extract the verdict from a given
	 * component once the component reference is known.
	 */
	private String getComponentVerdictFromReference(final String ref) {
		Map<String, TestComponent> components = this.model.getComponents();
		if (components != null) {
			TestComponent component = components.get(ref);
			if (component != null) {
				return component.getVerdict();
			}
		}
		return Constants.TEST_CASE_VERDICT_NONE;
	}
}
