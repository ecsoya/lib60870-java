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

	private void DebugLog(String msg) {
		if (debugOutput) {
			System.out.print("CS101 TCP link layer: ");
			System.out.println(msg);
		}
	}

	/**
	 * @param debugOutput the debugOutput to set
	 */
	public void setDebugOutput(boolean debugOutput) {
		this.debugOutput = debugOutput;
	}

	/**
	 * @return the debugOutput
	 */
	public boolean isDebugOutput() {
		return debugOutput;
	}

	public TcpClientVirtualSerialPort(String hostname, int tcpPort) {
		this.hostname = hostname;
		this.tcpPort = tcpPort;
	}

	private void ConnectSocketWithTimeout() throws IOException {
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

	private Runnable ConnectionThread() {
		return new Runnable() {

			@Override
			public void run() {
				running = true;

				DebugLog("Starting connection thread");

				while (running) {

					try {
						DebugLog("Connecting to " + hostname + ":" + tcpPort);

						ConnectSocketWithTimeout();

						socketStream = new DataOutputStream(new BufferedOutputStream(conSocket.getOutputStream()));

						connected = true;

						while (connected) {

							if (conSocket.isConnected())
								break;

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

	public void Start() {
		if (running == false) {
			connectionThread = new Thread(ConnectionThread());

			connectionThread.start();
		}
	}

	public void Stop() throws IOException {
		if (running == true) {
			running = false;

			if (conSocket != null)
				conSocket.close();

			try {
				connectionThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/*************************
	 * Stream implementation
	 */

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

	@Override
	public void setReadTimeout(int timeout) {
		this.readTimeout = timeout;
	}

	public void Write(byte[] buffer, int offset, int count) {
		if (socketStream != null) {
			try {
				socketStream.write(buffer, offset, count);
			} catch (IOException e) {
				connected = false;
			}
		}
	}

	@Override
	public void flush() throws IOException {
		if (socketStream != null) {
			socketStream.flush();
		}
	}

	@Override
	public void write(int b) throws IOException {
		if (socketStream != null && connected) {
			socketStream.write(b);
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
	public void close() throws IOException {
		if (socketStream != null) {
			socketStream.close();
		}
	}
}