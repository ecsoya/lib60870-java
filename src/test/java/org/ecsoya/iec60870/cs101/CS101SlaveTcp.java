package org.ecsoya.iec60870.cs101;

import java.io.IOException;
import java.util.Scanner;

import org.ecsoya.iec60870.asdu.ASDU;
import org.ecsoya.iec60870.asdu.CauseOfTransmission;
import org.ecsoya.iec60870.asdu.ie.MeasuredValueScaled;
import org.ecsoya.iec60870.asdu.ie.SinglePointInformation;
import org.ecsoya.iec60870.asdu.ie.StepPositionInformation;
import org.ecsoya.iec60870.asdu.ie.value.NameOfFile;
import org.ecsoya.iec60870.asdu.ie.value.QualityDescriptor;
import org.ecsoya.iec60870.core.IMasterConnection;
import org.ecsoya.iec60870.core.file.TransparentFile;
import org.ecsoya.iec60870.layer.LinkLayerMode;
import org.ecsoya.iec60870.layer.LinkLayerParameters;
import org.ecsoya.iec60870.layer.TcpServerVirtualSerialPort;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CS101SlaveTcp {

	private static boolean myInterrogationHandler(Object parameter, IMasterConnection connection, ASDU asdu, byte qoi) {
		System.out.println("Interrogation for group " + qoi);

		connection.sendACT_CON(asdu, false);

		// send information objects
		ASDU newAsdu = new ASDU(connection.getApplicationLayerParameters(), CauseOfTransmission.INTERROGATED_BY_STATION,
				false, false, (byte) 2, 1, false);

		newAsdu.addInformationObject(new MeasuredValueScaled(100, -1, new QualityDescriptor()));

		newAsdu.addInformationObject(new MeasuredValueScaled(101, 23, new QualityDescriptor()));

		newAsdu.addInformationObject(new MeasuredValueScaled(102, 2300, new QualityDescriptor()));

		connection.sendASDU(newAsdu);

		// send sequence of information objects
		newAsdu = new ASDU(connection.getApplicationLayerParameters(), CauseOfTransmission.INTERROGATED_BY_STATION,
				false, false, (byte) 2, 1, true);

		newAsdu.addInformationObject(new SinglePointInformation(200, true, new QualityDescriptor()));
		newAsdu.addInformationObject(new SinglePointInformation(201, false, new QualityDescriptor()));
		newAsdu.addInformationObject(new SinglePointInformation(202, true, new QualityDescriptor()));
		newAsdu.addInformationObject(new SinglePointInformation(203, false, new QualityDescriptor()));
		newAsdu.addInformationObject(new SinglePointInformation(204, true, new QualityDescriptor()));
		newAsdu.addInformationObject(new SinglePointInformation(205, false, new QualityDescriptor()));
		newAsdu.addInformationObject(new SinglePointInformation(206, true, new QualityDescriptor()));
		newAsdu.addInformationObject(new SinglePointInformation(207, false, new QualityDescriptor()));

		connection.sendASDU(newAsdu);

		connection.sendACT_TERM(asdu);

		return true;
	}

	public static void main(String[] args) {
		boolean running = true;

		LinkLayerParameters llParameters = new LinkLayerParameters();
		llParameters.setAddressLength(1);
		llParameters.setTimeoutForACK(500);
		llParameters.setUseSingleCharACK(true);

		TcpServerVirtualSerialPort port = new TcpServerVirtualSerialPort();
		// TcpClientVirtualSerialPort port = new
		// TcpClientVirtualSerialPort("192.168.2.9", 2404);

		port.setDebugOutput(true);
		try {
			port.start();
		} catch (IOException e) {
			e.printStackTrace();
			running = false;
		}

		CS101Slave slave = new CS101Slave(port, llParameters);
		slave.setDebugOutput(true);
		slave.setLinkLayerAddress(1);
		slave.setLinkLayerAddressOtherStation(3);

		slave.setLinkLayerMode(LinkLayerMode.BALANCED);

		slave.setInterrogationHandler((Object parameter, IMasterConnection connection, ASDU asdu,
				byte qoi) -> myInterrogationHandler(parameter, connection, asdu, qoi), null);

		slave.setUserDataQueueSizes(50, 20);

		ASDU asdu = new ASDU(slave.getParameters(), CauseOfTransmission.SPONTANEOUS, false, false, (byte) 0, 1, false);
		asdu.addInformationObject(new StepPositionInformation(301, 1, false, new QualityDescriptor()));
		slave.enqueueUserDataClass1(asdu);

		long lastTimestamp = System.currentTimeMillis();
		int measuredValue = 0;

		TransparentFile file = new TransparentFile(1, 30000, NameOfFile.TRANSPARENT_FILE);

		byte[] fileData = new byte[1025];

		for (int i = 0; i < 1025; i++) {
			fileData[i] = (byte) (i + 1);
		}

		file.addSection(fileData);

		slave.getAvailableFiles().addFile(file);

		while (running) {

			slave.run(); // call the protocol stack

			if ((System.currentTimeMillis() - lastTimestamp) >= 5000) {

				lastTimestamp = System.currentTimeMillis();

				ASDU newAsdu = new ASDU(slave.getParameters(), CauseOfTransmission.PERIODIC, false, false, (byte) 0, 1,
						false);
				newAsdu.addInformationObject(new MeasuredValueScaled(110, measuredValue, new QualityDescriptor()));
				slave.enqueueUserDataClass2(newAsdu);

				measuredValue++;
			}

			Scanner scanner = new Scanner(System.in);
			if (scanner.hasNext()) {
				String keyInfo = scanner.next();

				if (keyInfo.equals("t")) {
					slave.sendLinkLayerTestFunction();
				} else {
					System.out.println("Send spontaneous message");

					boolean value = false;

					if (keyInfo.equals("s")) {
						value = true;
					}

					ASDU newAsdu = new ASDU(slave.getParameters(), CauseOfTransmission.SPONTANEOUS, false, false,
							(byte) 0, 1, false);
					newAsdu.addInformationObject(new SinglePointInformation(100, value, new QualityDescriptor()));

					slave.enqueueUserDataClass1(newAsdu);
				}
			}
			scanner.close();
		}

		try {
			port.stop();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

}
