/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
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

	public HttpPoster(final String host, final String page, final int port) {
		this.host = host;
		this.page = page;
		this.port = port;
	}

	public void post(final Map<String, String> data) {
		final int[] ports = {49555, 59555, 61555, 0};
		final Socket socket = new Socket();

		for (int i = 0; i < ports.length; i++) {
			try {
				// try binding the first port
				socket.bind(new InetSocketAddress(InetAddress.getLocalHost(), ports[i]));
			} catch (IOException e) {
				if (socket.isBound()) {
					try {
						socket.close();
					} catch (final Exception e2) {
						// stay silent
					}
				}
			}
		}

		try {
			socket.connect(new InetSocketAddress(host, port));

			final String urlParameters = new MapJoiner("&", "=").join(data).toString();
			final DataOutputStream wr = new DataOutputStream(socket.getOutputStream());
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
			//unable to connect, silently exit
		} catch (final Exception e) {
			//unable to connect, silently exit
		}
		
		if( socket != null ) {
			try {
				socket.close();
			} catch (final IOException e) {
				//silently exit
			} catch (final Exception e) {
				//silently exit
			}
		}
	}
}
