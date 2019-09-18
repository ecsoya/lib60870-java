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

	@Override
	public void close() throws IOException {
		if (socketStream != null) {
			socketStream.close();
		}
	}

	private void debugLog(String msg) {
		if (debugOutput) {
			System.out.print("CS101 TCP link layer: ");
			System.out.println(msg);
		}
	}

	public void flush() throws IOException {
		if (socketStream != null)
			socketStream.flush();
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

	public int read(byte[] buffer, int offset, int count) throws IOException {
		if (socketStream != null) {

			if (connected) {
				DataInputStream is = new DataInputStream(conSocket.getInputStream());
				return is.read(buffer, offset, count);
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}

	@Override
	public byte readByte() throws IOException {
		if (socketStream != null) {
			if (connected) {
				DataInputStream is = new DataInputStream(conSocket.getInputStream());
				return is.readByte();
			} else {
				return 0;
			}
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

									if (!conSocket.isConnected())
										break;

									Thread.sleep(10);
								}

								connected = false;
								socketStream = null;
								conSocket = null;

								debugLog("Connection from " + ipEndPoint.toString() + "closed");
							} else
								newSocket.close();
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

	@Override
	public void setReadTimeout(int timeout) {
		this.readTimeout = timeout;
	}

	public void setTcpPort(int tcpPort) {
		localPort = tcpPort;
	}

	public void start() throws IOException {
		if (running == false) {
			// Create a TCP/IP socket.
			listeningSocket = ServerSocketFactory.getDefault().createServerSocket();

			listeningSocket.bind(new InetSocketAddress(localHostname, localPort));

//			listeningSocket.Listen(100);

			acceptThread = new Thread(serverAcceptThread());

			acceptThread.start();
		}
	}

	public void stop() throws IOException {
		if (running == true) {
			running = false;
			listeningSocket.close();

			try {
				acceptThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

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