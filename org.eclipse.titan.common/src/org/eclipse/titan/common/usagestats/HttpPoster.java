/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.usagestats;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

import org.eclipse.titan.common.utils.MapJoiner;

/**
* This class can create HTTP POST requests with parameters
*/
public class HttpPoster {

	private String host;
	private String page;
	private int port;

	public HttpPoster(String host, String page, int port) {
		this.host = host;
		this.page = page;
		this.port = port;
	}

	public void post(Map<String, String> data) {
		Socket socket = null;
		try {
			socket = new Socket();
			
			try {
				// try binding the first port
				socket.bind(new InetSocketAddress(InetAddress.getLocalHost(), 49555));
			} catch (IOException e) {
				// the first port might be occupied, try the second one
				if (!socket.isBound()) {
					try {
						socket.bind(new InetSocketAddress(InetAddress.getLocalHost(), 59555));
					} catch (IOException e2) {
						// try the third one ...
						if (!socket.isBound()) {
							try {
								socket.bind(new InetSocketAddress(InetAddress.getLocalHost(), 61555));
							} catch (IOException e3) {
								// last ditch effort ... let the system find a free port 
								if (!socket.isBound()) {
									socket.bind(new InetSocketAddress(InetAddress.getLocalHost(), 0));
								} else {
									socket.close();
									throw e3;
								}
							}
						} else {
							socket.close();
							throw e2;
						}
					}					
				} else {
					socket.close();
					throw e;
				}
			}
			
			socket.connect(new InetSocketAddress(host, port));

			String urlParameters = new MapJoiner("&", "=").join(data).toString();
			DataOutputStream wr = new DataOutputStream(socket.getOutputStream());
			wr.writeBytes("POST " + page + " HTTP/1.0\r\n" +
					"Host: " + host + "\r\n" +
					"Content-type: application/x-www-form-urlencoded\r\n" +
					"Content-length: " + Integer.toString(urlParameters.length()) + "\r\n\r\n");
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			
			socket.close();
			return;
		} catch (final IOException e) {
			//unable to connect, silently exit:
			//ErrorReporter.logWarningExceptionStackTrace("IOException while sending information", e);
			//ErrorReporter.logWarning("Cannot send user information to the statistical page");
		} catch (final Exception e) {
			//ErrorReporter.logWarningExceptionStackTrace("Unidentified problem while sending information", e);
		}
		
		if( socket != null ) {
			try {
				socket.close();
			} catch (final IOException e) {
				//ErrorReporter.logWarningExceptionStackTrace("IOException while sending information", e);
				//ErrorReporter.logWarning("Cannot send user information to the statistical page");
			} catch (final Exception e) {
				//ErrorReporter.logWarningExceptionStackTrace("Unidentified problem while sending information", e);
			}
		}
	}
}
