/*******************************************************************************
 * Copyright (C) 2019 Ecsoya (jin.liu@soyatec.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.ecsoya.iec60870.layer;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.SocketFactory;

import org.ecsoya.iec60870.serial.SerialStream;

public class TcpClientVirtualSerialPort implements SerialStream {
	private int readTimeout = 0;

	private boolean debugOutput = false;
	private boolean running = false;
	private boolean connected = false;

	private String hostname;
	private int tcpPort;

	Socket conSocket = null;
	DataOutputStream socketStream = null;
	Thread connectionThread;

	private int connectTimeoutInMs = 1000;
	private int waitRetryConnect = 1000;

	public TcpClientVirtualSerialPort(String hostname, int tcpPort) {
		this.hostname = hostname;
		this.tcpPort = tcpPort;
	}

	@Override
	public void close() throws IOException {
		if (socketStream != null) {
			socketStream.close();
		}
	}

	private Runnable connectionThread() {
		return new Runnable() {

			@Override
			public void run() {
				running = true;

				debugLog("Starting connection thread");

				while (running) {

					try {
						debugLog("Connecting to " + hostname + ":" + tcpPort);

						connectSocketWithTimeout();

						socketStream = new DataOutputStream(new BufferedOutputStream(conSocket.getOutputStream()));

						connected = true;

						while (connected) {

							if (conSocket.isConnected()) {
								break;
							}

							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}

						connected = false;
						socketStream = null;
						conSocket.close();
						conSocket = null;

					} catch (IOException e) {
						connected = false;
						socketStream = null;
						conSocket = null;
					}

					try {
						Thread.sleep(waitRetryConnect);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};

	}

	private void connectSocketWithTimeout() throws IOException {
		InetSocketAddress

		ipAddress = new InetSocketAddress(hostname, tcpPort);

		// Create a TCP/IP socket.
		conSocket = SocketFactory.getDefault().createSocket();

		try {
			conSocket.connect(ipAddress, connectTimeoutInMs);
		} catch (IOException e) {
			conSocket.close();
			conSocket = null;
			throw e;
		}
	}

	private void debugLog(String msg) {
		if (debugOutput) {
			System.out.print("CS101 TCP link layer: ");
			System.out.println(msg);
		}
	}

	@Override
	public void flush() throws IOException {
		if (socketStream != null) {
			socketStream.flush();
		}
	}

	/**
	 * @return the debugOutput
	 */
	public boolean isDebugOutput() {
		return debugOutput;
	}

	/*************************
	 * Stream implementation
	 */

	@Override
	public int read(byte[] buffer, int offset, int count) throws IOException {
		if (conSocket != null) {
			conSocket.setSoTimeout(readTimeout);
			DataInputStream is = new DataInputStream(conSocket.getInputStream());
			return is.read(buffer, offset, count);
		}
		return 0;
	}

	@Override
	public byte readByte() throws IOException {
		if (conSocket != null) {
			conSocket.setSoTimeout(readTimeout);
			DataInputStream is = new DataInputStream(conSocket.getInputStream());
			return is.readByte();
		}
		return 0;
	}

	/**
	 * @param debugOutput the debugOutput to set
	 */
	public void setDebugOutput(boolean debugOutput) {
		this.debugOutput = debugOutput;
	}

	@Override
	public void setReadTimeout(int timeout) {
		this.readTimeout = timeout;
	}

	public void start() {
		if (running == false) {
			connectionThread = new Thread(connectionThread());

			connectionThread.start();
		}
	}

	public void stop() throws IOException {
		if (running == true) {
			running = false;

			if (conSocket != null) {
				conSocket.close();
			}

			try {
				connectionThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {

		try {
			if (socketStream != null) {
				socketStream.write(b, off, len);
			}
		} catch (Exception e) {
			connected = false;
		}

	}

	@Override
	public void write(int b) throws IOException {
		if (socketStream != null && connected) {
			socketStream.write(b);
		}
	}
}
