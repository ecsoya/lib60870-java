package org.ecsoya.iec60870.cs104;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.ecsoya.iec60870.asdu.ASDU;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.conn.Slave;

/// <summary>
/// This class represents a single IEC 60870-5 server (slave or controlled station). It is also the
/// main access to the server API.
/// </summary>
public class Server extends Slave {

	private String localHostname = "0.0.0.0";
	private int localPort = 2404;

	private boolean running = false;

	private ServerSocket listeningSocket;

	private int maxQueueSize = 1000;
	private int maxOpenConnections = 10;

	// only required for single redundancy group mode
	private ASDUQueue asduQueue = null;

	private ServerMode serverMode;

	private APCIParameters apciParameters;

	private ApplicationLayerParameters alParameters;

	private TlsSecurityInformation securityInfo = null;

	// List of all open connections
	private List<ClientConnection> allOpenConnections = new ArrayList<ClientConnection>();

	public ConnectionRequestHandler connectionRequestHandler = null;

	public Object connectionRequestHandlerParameter = null;
	private ConnectionEventHandler connectionEventHandler = null;

	private Object connectionEventHandlerParameter = null;

	/// <summary>
	/// Create a new server using default connection parameters
	/// </summary>
	public Server() {
		this.apciParameters = new APCIParameters();
		this.alParameters = new ApplicationLayerParameters();
	}

	/// <summary>
	/// Create a new server using the provided connection parameters.
	/// </summary>
	/// <param name="parameters">Connection parameters</param>
	public Server(APCIParameters apciParameters, ApplicationLayerParameters alParameters) {
		this.apciParameters = apciParameters;
		this.alParameters = alParameters;
	}

	public Server(APCIParameters apciParameters, ApplicationLayerParameters alParameters,
			TlsSecurityInformation securityInfo) {
		this.apciParameters = apciParameters;
		this.alParameters = alParameters;
		this.securityInfo = securityInfo;

		if (securityInfo != null)
			this.localPort = 19998;
	}

	public Server(TlsSecurityInformation securityInfo) {
		this.apciParameters = new APCIParameters();
		this.alParameters = new ApplicationLayerParameters();

		this.securityInfo = securityInfo;

		if (securityInfo != null)
			this.localPort = 19998;
	}

	void Activated(ClientConnection activeConnection) {
		if (connectionEventHandler != null)
			connectionEventHandler.invoke(connectionEventHandlerParameter, activeConnection,
					ClientConnectionEvent.ACTIVE);

		// deactivate all other connections

		for (ClientConnection connection : allOpenConnections) {
			if (connection != activeConnection) {

				if (connection.isActive()) {

					if (connectionEventHandler != null)
						connectionEventHandler.invoke(connectionEventHandlerParameter, connection,
								ClientConnectionEvent.INACTIVE);

					connection.setActive(false);
				}
			}
		}
	}

	void Deactivated(ClientConnection activeConnection) {
		if (connectionEventHandler != null)
			connectionEventHandler.invoke(connectionEventHandlerParameter, activeConnection,
					ClientConnectionEvent.INACTIVE);
	}

	private void DebugLog(String msg) {
		if (debugOutput) {
			System.out.print("CS104 SLAVE: ");
			System.out.println(msg);
		}
	}

	/// <summary>
	/// Enqueues the ASDU to the transmission queue.
	/// </summary>
	/// If an active connection exists the ASDU will be sent to the active client
	/// immediately. Otherwhise
	/// the ASDU will be added to the transmission queue for later transmission.
	/// <param name="asdu">ASDU to be sent</param>
	public void EnqueueASDU(ASDU asdu) {
		if (serverMode == ServerMode.SINGLE_REDUNDANCY_GROUP) {
			asduQueue.enqueueAsdu(asdu);

			for (ClientConnection connection : allOpenConnections) {
				if (connection.isActive())
					connection.ASDUReadyToSend();
			}
		} else {
			for (ClientConnection connection : allOpenConnections) {
				if (connection.isActive()) {
					connection.GetASDUQueue().enqueueAsdu(asdu);
					connection.ASDUReadyToSend();
				}
			}
		}
	}

	public ApplicationLayerParameters GetApplicationLayerParameters() {
		return alParameters;
	}

	public int getMaxQueueSize() {
		return maxQueueSize;
	}

	/// <summary>
	/// Gets the number of connected master/client stations.
	/// </summary>
	/// <value>The number of open connections.</value>
	public int getOpenConnectionsCount() {
		return this.allOpenConnections.size();
	}

	public ServerMode getServerMode() {
		return serverMode;
	}

	void MarkASDUAsConfirmed(int index, long timestamp) {
		if (asduQueue != null)
			asduQueue.markASDUAsConfirmed(index, timestamp);
	}

	public void Remove(ClientConnection connection) {
		if (connectionEventHandler != null)
			connectionEventHandler.invoke(connectionEventHandlerParameter, connection, ClientConnectionEvent.CLOSED);

		allOpenConnections.remove(connection);
	}

	private void ServerAcceptThread() {
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

					if (getOpenConnectionsCount() >= maxOpenConnections)
						acceptConnection = false;

					if (acceptConnection && (connectionRequestHandler != null)) {
						acceptConnection = connectionRequestHandler.invoke(connectionRequestHandlerParameter,
								ipEndPoint);
					}

					if (acceptConnection) {

						ClientConnection connection;

						if (serverMode == ServerMode.SINGLE_REDUNDANCY_GROUP)
							connection = new ClientConnection(newSocket, securityInfo, apciParameters, alParameters,
									this, asduQueue, debugOutput);
						else
							connection = new ClientConnection(newSocket, securityInfo, apciParameters, alParameters,
									this, new ASDUQueue(maxQueueSize, alParameters, (msg) -> DebugLog(msg)),
									debugOutput);

						allOpenConnections.add(connection);

						if (connectionEventHandler != null)
							connectionEventHandler.invoke(connectionEventHandlerParameter, connection,
									ClientConnectionEvent.OPENED);

					} else
						newSocket.close();
				}

			} catch (Exception e) {
				running = false;
			}

		}
	}

	/// <summary>
	/// Sets the connection event handler. The connection event handler will be
	/// called whenever a new
	/// connection was opened, closed, activated, or inactivated.
	/// </summary>
	/// <param name="handler">Handler.</param>
	/// <param name="parameter">Parameter.</param>
	public void SetConnectionEventHandler(ConnectionEventHandler handler, Object parameter) {
		this.connectionEventHandler = handler;
		this.connectionEventHandlerParameter = parameter;
	}

	/// <summary>
	/// Sets a callback handler for connection request. The user can allow
	/// (returning true) or deny (returning false)
	/// the connection attempt. If no handler is installed every new connection will
	/// be accepted.
	/// </summary>
	/// <param name="handler">Handler.</param>
	/// <param name="parameter">Parameter.</param>
	public void SetConnectionRequestHandler(ConnectionRequestHandler handler, Object parameter) {
		this.connectionRequestHandler = handler;
		this.connectionRequestHandlerParameter = parameter;
	}

	/// <summary>
	/// Sets the local IP address to bind the server. Default is "0.0.0.0" for
	/// all interfaces
	/// </summary>
	/// <param name="localAddress">Local IP address or hostname to bind.</param>
	public void SetLocalAddress(String localAddress) {
		this.localHostname = localAddress;
	}

	/// <summary>
	/// Sets the local TCP port to bind to. Default is 2404.
	/// </summary>
	/// <param name="tcpPort">Local TCP port to bind.</param>
	public void SetLocalPort(int tcpPort) {
		this.localPort = tcpPort;
	}

	public void setMaxQueueSize(int maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
	}

	public void setServerMode(ServerMode serverMode) {
		this.serverMode = serverMode;
	}

	/// <summary>
	/// Start the server. Listen to client connections.
	/// </summary>
	public void Start() {
		try {
			InetAddress ipAddress = InetAddress.getByName(localHostname);
			SocketAddress localEP = new InetSocketAddress(ipAddress, localPort);

			// Create a TCP/IP socket.
			listeningSocket = new ServerSocket();

			listeningSocket.bind(localEP);

//		listeningSocket.Listen(100);

			Thread acceptThread = new Thread(() -> ServerAcceptThread());

			if (serverMode == ServerMode.SINGLE_REDUNDANCY_GROUP)
				asduQueue = new ASDUQueue(maxQueueSize, alParameters, (msg) -> DebugLog(msg));

			acceptThread.start();
		} catch (UnknownHostException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	/// <summary>
	/// Stop the server. Close all open client connections.
	/// </summary>
	public void Stop() {
		running = false;

		try {
			listeningSocket.close();

			// close all open connection
			for (ClientConnection connection : allOpenConnections) {
				connection.Close();
			}

		} catch (Exception e) {
			System.out.println(e);
		}

		try {
			listeningSocket.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	void UnmarkAllASDUs() {
		if (asduQueue != null)
			asduQueue.unmarkAllASDUs();
	}
}
