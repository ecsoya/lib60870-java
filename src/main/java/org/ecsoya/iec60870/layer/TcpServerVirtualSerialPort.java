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

	public TcpServerVirtualSerialPort() {
	}

	public void SetLocalAddress(String localAddress) {
		localHostname = localAddress;
	}

	public void SetTcpPort(int tcpPort) {
		localPort = tcpPort;
	}

	private Runnable ServerAcceptThread() {
		return new Runnable() {

			@Override
			public void run() {

				running = true;

				DebugLog("Waiting for connections...");

				while (running) {

					try {

						Socket newSocket = listeningSocket.accept();

						if (newSocket != null) {
							DebugLog("New connection");

							SocketAddress ipEndPoint = newSocket.getRemoteSocketAddress();

							DebugLog("  from IP: " + ipEndPoint.toString());

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

								DebugLog("Connection from " + ipEndPoint.toString() + "closed");
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

	public void Start() throws IOException {
		if (running == false) {
			// Create a TCP/IP socket.
			listeningSocket = ServerSocketFactory.getDefault().createServerSocket();

			listeningSocket.bind(new InetSocketAddress(localHostname, localPort));

//			listeningSocket.Listen(100);

			acceptThread = new Thread(ServerAcceptThread());

			acceptThread.start();
		}
	}

	public void Stop() throws IOException {
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
	public void setReadTimeout(int timeout) {
		this.readTimeout = timeout;
	}

	public void flush() throws IOException {
		if (socketStream != null)
			socketStream.flush();
	}

	@Override
	public void close() throws IOException {
		if (socketStream != null) {
			socketStream.close();
		}
	}
}