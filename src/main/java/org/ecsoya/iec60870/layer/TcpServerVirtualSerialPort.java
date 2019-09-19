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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import javax.net.ServerSocketFactory;

import org.ecsoya.iec60870.serial.SerialStream;

public class TcpServerVirtualSerialPort implements SerialStream {
	private int readTimeout = 0;

	private boolean debugOutput = false;
	private boolean running = false;
	private boolean connected = false;

	private String localHostname = "0.0.0.0";
	private int localPort = 2404;
	private ServerSocket listeningSocket;

	Socket conSocket = null;
	OutputStream socketStream = null;
	Thread acceptThread;

	public TcpServerVirtualSerialPort() {
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
	 *
	 * @throws IOException
	 */

	@Override
	public int read(byte[] buffer, int offset, int count, int timeouts) throws IOException {
		if (conSocket != null && conSocket.isConnected()) {
			conSocket.setSoTimeout(timeouts);
			DataInputStream is = new DataInputStream(conSocket.getInputStream());
			return is.read(buffer, offset, count);
		} else {
			return 0;
		}
	}

	@Override
	public byte readByte() throws IOException {
		if (conSocket != null && conSocket.isConnected()) {
			DataInputStream is = new DataInputStream(conSocket.getInputStream());
			return is.readByte();
		} else {
			return 0;
		}
	}

	private Runnable serverAcceptThread() {
		return new Runnable() {

			@Override
			public void run() {

				running = true;

				debugLog("Waiting for connections...");

				while (running) {

					try {

						Socket newSocket = listeningSocket.accept();

						if (newSocket != null) {
							debugLog("New connection");

							SocketAddress ipEndPoint = newSocket.getRemoteSocketAddress();

							debugLog("  from IP: " + ipEndPoint.toString());

							boolean acceptConnection = true;

							if (acceptConnection) {

								conSocket = newSocket;
								socketStream = conSocket.getOutputStream();
								connected = true;

								while (connected) {

									if (!conSocket.isConnected()) {
										break;
									}

									Thread.sleep(10);
								}

								connected = false;
								socketStream = null;
								conSocket = null;

								debugLog("Connection from " + ipEndPoint.toString() + "closed");
							} else {
								newSocket.close();
							}
						}

					} catch (Exception e) {
						running = false;
					}

				}
			}
		};
	}

	/**
	 * @param debugOutput the debugOutput to set
	 */
	public void setDebugOutput(boolean debugOutput) {
		this.debugOutput = debugOutput;
	}

	public void setLocalAddress(String localAddress) {
		localHostname = localAddress;
	}

	public void setTcpPort(int tcpPort) {
		localPort = tcpPort;
	}

	public void connect() throws IOException {
		if (running == false) {
			// Create a TCP/IP socket.
			listeningSocket = ServerSocketFactory.getDefault().createServerSocket();

			listeningSocket.bind(new InetSocketAddress(localHostname, localPort));

//			listeningSocket.Listen(100);

			acceptThread = new Thread(serverAcceptThread());

			acceptThread.start();
		}
	}

	@Override
	public void close() throws IOException {
		if (running == true) {
			running = false;

			if (socketStream != null) {
				socketStream.close();
			}

			listeningSocket.close();

			try {
				acceptThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void write(byte[] buffer, int offset, int length, int timeouts) throws IOException {
		write(buffer, offset, length);
	}

	@Override
	public void write(byte[] buffer, int offset, int count) {
		if (socketStream != null) {
			try {
				socketStream.write(buffer, offset, count);
			} catch (IOException e) {
				connected = false;
			}
		}
	}

	@Override
	public void write(int value) throws IOException {
		if (socketStream != null) {
			try {
				socketStream.write(value);
			} catch (IOException e) {
				connected = false;
			}
		}
	}
}
