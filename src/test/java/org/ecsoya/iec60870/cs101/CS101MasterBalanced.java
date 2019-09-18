/**
 * 
 */
package org.ecsoya.iec60870.cs101;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.ecsoya.iec60870.asdu.ASDU;
import org.ecsoya.iec60870.asdu.CauseOfTransmission;
import org.ecsoya.iec60870.layer.LinkLayerMode;
import org.ecsoya.iec60870.layer.LinkLayerParameters;
import org.ecsoya.iec60870.layer.LinkLayerState;
import org.ecsoya.iec60870.layer.TcpServerVirtualSerialPort;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CS101MasterBalanced {
	private static boolean rcvdRawMessageHandler(Object parameter, byte[] message, int messageSize) {
		System.out
				.println("RECV " + ByteBuffer.wrap(message, 0, messageSize).order(ByteOrder.nativeOrder()).toString());
		return true;
	}

	private static void linkLayerStateChanged(Object parameter, int address, LinkLayerState newState) {
		System.out.println("LL state event: " + newState.toString());
	}

	private static boolean asduReceivedHandler(Object parameter, int address, ASDU asdu) {
		System.out.println(asdu.toString());

		return true;
	}

	public static void main(String[] args) {
		boolean running = true;

		// use Ctrl-C to stop the programm
//		Console.CancelKeyPress += delegate(Object sender, ConsoleCancelEventArgs e) {
//			e.Cancel = true;
//			running = false;
//		};

		String portName = "/dev/ttyUSB1";

		if (args.length > 0)
			portName = args[0];

		// Setup serial port
		TcpServerVirtualSerialPort serialPort = new TcpServerVirtualSerialPort();

		// Setup balanced CS101 master
		LinkLayerParameters llParameters = new LinkLayerParameters();
		llParameters.setAddressLength(1);
		llParameters.setUseSingleCharACK(false);

		CS101Master master = new CS101Master(serialPort, LinkLayerMode.BALANCED, llParameters);
		master.setDebug(false);
		master.setOwnAddress(1);
		master.setSlaveAddress(2);
		master.setASDUReceivedHandler(
				(Object parameter, int address, ASDU asdu) -> asduReceivedHandler(parameter, address, asdu), null);
		master.SetLinkLayerStateChangedHandler((Object parameter, int address,
				LinkLayerState newState) -> linkLayerStateChanged(parameter, address, newState), null);
		master.setReceivedRawMessageHandler((Object parameter, byte[] message,
				int messageSize) -> rcvdRawMessageHandler(parameter, message, messageSize), null);

		long lastTimestamp = System.currentTimeMillis();

		// This will start a separate thread!
		// alternativley you can you master.Run() inside the loop
		master.Start();

		while (running) {

			if ((System.currentTimeMillis() - lastTimestamp) >= 5000) {

				lastTimestamp = System.currentTimeMillis();

				if (master.GetLinkLayerState() == LinkLayerState.AVAILABLE) {
					master.sendInterrogationCommand(CauseOfTransmission.ACTIVATION, 1, (byte) 20);
				} else {
					System.out.println("Link layer: " + master.GetLinkLayerState().toString());
				}
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		master.Stop();

//		serialPort.close();
	}

}
