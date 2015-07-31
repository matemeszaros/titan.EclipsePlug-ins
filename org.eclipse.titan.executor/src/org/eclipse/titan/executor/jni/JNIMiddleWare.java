/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.jni;

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.executor.TITANConsole;

import java.util.ArrayList;
import java.util.List;

/**
 * The middleware that connects the JNI executor and the MainController.
 * 
 * @author Peter Dimitrov
 * */
public final class JNIMiddleWare {
	private static final String STATUSCHANGE_INDICATOR = "S";
	private static final String ERRORCALLBACK_INDICATOR = "E";
	private static final String NOTIFICATION_INDICATOR = "N";
	private static Exception exception = null;

	private Thread eTh;

	private IJNICallback jniCallback;
	private volatile long userinterface_ptr = -1;

	public JNIMiddleWare(final IJNICallback jniCallback) {
		this.jniCallback = jniCallback;
	}

	public static Exception getException() {
		return exception;
	}

	static {
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			exception = new Exception("Execution in JNI mode is not supported on the Windows operating system.");
		} else {
			try {
				System.loadLibrary("mctrjninative");
			} catch (SecurityException e) {
				exception = new Exception("Security manager does not allow loading of the JNI dinamic library.");
				exception.setStackTrace(e.getStackTrace());
			} catch (UnsatisfiedLinkError e) {
				exception = new Exception("JNI dynamic library could not be loaded.\n\noriginal message: " + e.getMessage() + "\n\njava.library.path = " + System.getProperty("java.library.path"));
				exception.setStackTrace(e.getStackTrace());
			}
		}
	}

	/**
	 * A simple class to receive messages from the MainController and dispatch them to the Executor.
	 * */
	private final class EventDispatcher implements Runnable {

		@Override
		public void run() {
			String s;
			String s2;
			ArrayList<String> notifications;
			while (userinterface_ptr != -1) {
				s = readPipe();
				if (s.startsWith(STATUSCHANGE_INDICATOR)) {
					notifications = new ArrayList<String>();
					while (isPipeReadable()) {
						s2 = readPipe();
						if (s2.startsWith(ERRORCALLBACK_INDICATOR)) {
							processErrorCallbackFast(s2);
						} else if (s2.startsWith(NOTIFICATION_INDICATOR)) {
							notifications.add(s2);
						} else {
							break;
						}
					}
					batchedProcessNotifications(notifications);
					notifications.clear();
					processStatusChangeCallback();
				} else if (s.startsWith(ERRORCALLBACK_INDICATOR)) {
					processErrorCallback(s);
				} else if (s.startsWith(NOTIFICATION_INDICATOR)) {
					processNotifyCallback(s);
				}
			}
		}
	}

	// public Boolean lock = new Boolean(true);

	// *******************************************************
	// MC manipulating native methods
	// *******************************************************
	// returns the value of a pointer of a C++ class
	private native long init(final int par_max_ptcs);

	public native void terminate();

	public native void add_host(final String group_name, final String host_name);

	public native void assign_component(final String host_or_group, final String component_id);

	public native void destroy_host_groups();

	public native void set_kill_timer(final double timer_val);

	public native int start_session(final String local_address, final int tcp_port, final boolean unixdomainsocketenabled);

	public native void shutdown_session();

	public native void configure(final String config_file);

	public native void create_mtc(final int host_index);

	public native void exit_mtc();

	public native void execute_control(final String module_name);

	public native void execute_testcase(final String module_name, final String testcase_name);

	public native void stop_after_testcase(final boolean new_state);

	public native void continue_testcase();

	public native void stop_execution();

	// returns mc_state_enum
	public native McStateEnum get_state();

	public native boolean get_stop_after_testcase();

	public native int get_nof_hosts();

	public native HostStruct get_host_data(final int host_index);

	public native ComponentStruct get_component_data(final int component_reference);

	public native void release_data();

	/**
	 * Transforms the MC's state into a humanly readable text.
	 *
	 * @param state the MC state
	 * @return the name of the state provided
	 * */
	public native String get_mc_state_name(final McStateEnum state);

	/**
	 * Transforms the HC's state into a humanly readable text.
	 *
	 * @param state the HC state
	 * @return the name of the state provided
	 * */
	public native String get_hc_state_name(final HcStateEnum state);

	/**
	 * Transforms the testcase's state into a humanly readable text.
	 *
	 * @param state the testcase state
	 * @return the name of the state provided
	 * */
	public native String get_tc_state_name(final TcStateEnum state);

	/**
	 * Transforms the transportation type into a humanly readable text.
	 *
	 * @param transport the type of transport to use to communicate wit the HCs
	 * @return the name of the state provided
	 * */
	public native String get_transport_name(final TransportTypeEnum transport);

	// *******************************************************
	// Other native methods
	// *******************************************************
	public native void check_mem_leak(final String program_name);

	public native void print_license_info();

	public native int check_license();

	public native String readPipe();

	public native boolean isPipeReadable();

	public static native long getSharedLibraryVersion();

	/**
	 * Initialize the Main Controller.
	 *
	 * @param par_max_ptcs the maximum number of PTCs
	 * 	this Main Controller will be allowed to handle at a time.
	 * */
	public void initialize(final int par_max_ptcs) {
		if (userinterface_ptr == -1) {
			userinterface_ptr = init(par_max_ptcs/* , lock */);
			eTh = new Thread(new EventDispatcher());
			eTh.start();
		} else {
			TITANConsole.getConsole().newMessageStream().println("Main Controller has been already initialized!");
		}
	}

	public void terminate_internal() {
		userinterface_ptr = -1;
		terminate();
	}

	/**
	 * Decodes the MainController sent messages.
	 * Messages sent by the Main Controller are separated with a '|' sign
	 *
	 * @param s the string to split
	 *
	 * @return the resulting string list
	 * */
	private List<String> splitPacket(final String s) {
		final String pdu = s.substring(6);
		final List<String> stringList = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		final int length = pdu.length();
		char ch;
		int i = 0;
		while (i < length) {
			ch = pdu.charAt(i);
			switch (ch) {
			case '|':
				stringList.add(sb.toString());
				sb = new StringBuilder();
				i++;
				break;
			case '\\':
				sb.append(pdu.charAt(i + 1));
				i += 2;
				break;
			default:
				sb.append(ch);
				i++;
				break;
			}
		}
		stringList.add(sb.toString());
		return stringList;
	}

	/**
	 * Transfers the MainController sent status change notification to the executor.
	 * */
	private void processStatusChangeCallback() {
		jniCallback.statusChangeCallback();
	}

	private void processErrorCallbackFast(final String s) {
		// TODO check if array is the same as ppdu, if not increase size to make faster
		final String[] tempArray = new String[1];
		final String[] ppdu = splitPacket(s).toArray(tempArray);

		jniCallback.insertError(Integer.parseInt(ppdu[0]), ppdu[1]);
	}

	private void processErrorCallback(final String s) {
		final String[] tempArray = new String[1];
		final String[] ppdu = splitPacket(s).toArray(tempArray);

		jniCallback.errorCallback(Integer.parseInt(ppdu[0]), ppdu[1]);
	}

	public void batchedProcessNotifications(final ArrayList<String> s) {
		final String[] tempArray = new String[1];
		final ArrayList<String[]> result = new ArrayList<String[]>();
		for (String value : s) {
			result.add(splitPacket(value).toArray(tempArray));
		}
		jniCallback.batchedInsertNotify(result);
	}

	public void processNotifyCallbackFast(final String s) {
		final String[] tempArray = new String[1];
		final String[] ppdu = splitPacket(s).toArray(tempArray);

		jniCallback.insertNotify(new Timeval(Integer.parseInt(ppdu[0]), Integer.parseInt(ppdu[1])), ppdu[2], Integer.parseInt(ppdu[3]), ppdu[4]);
	}

	public void processNotifyCallback(final String s) {
		final String[] tempArray = new String[1];
		final String[] ppdu = splitPacket(s).toArray(tempArray);

		jniCallback.notifyCallback(new Timeval(Integer.parseInt(ppdu[0]), Integer.parseInt(ppdu[1])), ppdu[2], Integer.parseInt(ppdu[3]), ppdu[4]);
	}
}
