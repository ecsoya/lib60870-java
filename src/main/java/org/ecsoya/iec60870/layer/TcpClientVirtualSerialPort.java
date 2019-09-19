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
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.SocketFactory;

import org.ecsoya.iec60870.serial.SerialStream;

public class TcpClientVirtualSerialPort implements SerialStream {

	private boolean debugOutput = false;
	private boolean connecting = false;
	private boolean running = false;

	private String hostname;
	private int tcpPort;

	private Socket socket = null;
	private DataOutputStream socketStream = null;
	private Thread connectionThread;

	private int connectTimeoutInMs = 1000;
	private int waitRetryConnect = 1000;

	public TcpClientVirtualSerialPort(String hostname, int tcpPort) {
		this.hostname = hostname;
		this.tcpPort = tcpPort;
	}

	@Override
	public void close() throws IOException {
		connecting = false;

		running = false;

		if (socketStream != null) {
			socketStream.close();
		}
		socketStream = null;
		if (socket != null) {
			socket.close();
		}
		socket = null;

		try {
			connectionThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void handleConnection() {
		connecting = true;

		debugLog("Starting connection thread");

		debugLog("Connecting to " + hostname + ":" + tcpPort);

		try {
			connectSocketWithTimeout();

			socketStream = new DataOutputStream(socket.getOutputStream());

			running = true;

		} catch (IOException e) {
			running = false;
			socketStream = null;
			socket = null;
		} finally {
			connecting = false;
		}

		while (running) {

			if (!socket.isConnected()) {
				break;
			}

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		running = false;
		socketStream = null;
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		socket = null;

	}

	private void connectSocketWithTimeout() throws IOException {
		InetSocketAddress ipAddress = new InetSocketAddress(hostname, tcpPort);

		// Create a TCP/IP socket.
		socket = SocketFactory.getDefault().createSocket();

		try {
			socket.setSoTimeout(connectTimeoutInMs);
			socket.connect(ipAddress, connectTimeoutInMs);
		} catch (IOException e) {
			socket.close();
			socket = null;
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
	public int read(byte[] buffer, int offset, int count, int timeouts) throws IOException {
		if (socket != null) {
			socket.setSoTimeout(timeouts);
			DataInputStream is = new DataInputStream(socket.getInputStream());
			return is.read(buffer, offset, count);
		}
		return 0;
	}

	@Override
	public byte readByte() throws IOException {
		if (socket != null) {
			socket.setSoTimeout(0);
			DataInputStream is = new DataInputStream(socket.getInputStream());
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

	public void connect() {
		if (running) {
			return;
		}
		if (!connecting) {
			connectionThread = new Thread(() -> handleConnection());

			connectionThread.start();
		}

		while (!running) {
			try {
				Thread.sleep(waitRetryConnect);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void write(byte[] buffer, int offset, int length, int timeouts) throws IOException {
		connect();
		write(buffer, offset, length);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		connect();
		try {
			if (socketStream != null) {
				socketStream.write(b, off, len);
			}
		} catch (Exception e) {
			running = false;
		}

	}

	@Override
	public void write(int b) throws IOException {
		connect();
		if (socketStream != null && running) {
			socketStream.write(b);
		}
	}
}
