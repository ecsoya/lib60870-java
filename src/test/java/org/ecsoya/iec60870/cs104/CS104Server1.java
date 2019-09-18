package org.ecsoya.iec60870.cs104;

import java.time.LocalDateTime;

import org.ecsoya.iec60870.CP56Time2a;
import org.ecsoya.iec60870.asdu.ASDU;
import org.ecsoya.iec60870.asdu.ASDUParsingException;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.CauseOfTransmission;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.ClockSynchronizationCommand;
import org.ecsoya.iec60870.asdu.ie.EndOfInitialization;
import org.ecsoya.iec60870.asdu.ie.MeasuredValueNormalizedWithoutQuality;
import org.ecsoya.iec60870.asdu.ie.MeasuredValueScaled;
import org.ecsoya.iec60870.asdu.ie.MeasuredValueScaledWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.SingleCommand;
import org.ecsoya.iec60870.asdu.ie.SinglePointInformation;
import org.ecsoya.iec60870.asdu.ie.SinglePointWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.value.QualityDescriptor;
import org.ecsoya.iec60870.core.ConnectionException;
import org.ecsoya.iec60870.core.IMasterConnection;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CS104Server1 {
	private static boolean interrogationHandler(Object parameter, IMasterConnection connection, ASDU asdu, byte qoi) {
		System.out.println("Interrogation for group " + qoi);

		ApplicationLayerParameters cp = connection.getApplicationLayerParameters();

		connection.sendACT_CON(asdu, false);

		// send information objects
		ASDU newAsdu = new ASDU(cp, CauseOfTransmission.INTERROGATED_BY_STATION, false, false, (byte) 2, 1, false);

		newAsdu.addInformationObject(new MeasuredValueScaled(100, -1, new QualityDescriptor()));

		newAsdu.addInformationObject(new MeasuredValueScaled(101, 23, new QualityDescriptor()));

		newAsdu.addInformationObject(new MeasuredValueScaled(102, 2300, new QualityDescriptor()));

		connection.sendASDU(newAsdu);

		newAsdu = new ASDU(cp, CauseOfTransmission.INTERROGATED_BY_STATION, false, false, (byte) 3, 1, false);

		newAsdu.addInformationObject(new MeasuredValueScaledWithCP56Time2a(103, 3456, new QualityDescriptor(),
				new CP56Time2a(LocalDateTime.now())));

		connection.sendASDU(newAsdu);

		newAsdu = new ASDU(cp, CauseOfTransmission.INTERROGATED_BY_STATION, false, false, (byte) 2, 1, false);

		newAsdu.addInformationObject(
				new SinglePointWithCP56Time2a(104, true, new QualityDescriptor(), new CP56Time2a(LocalDateTime.now())));

		connection.sendASDU(newAsdu);

		// send sequence of information objects
		newAsdu = new ASDU(cp, CauseOfTransmission.INTERROGATED_BY_STATION, false, false, (byte) 2, 1, true);

		newAsdu.addInformationObject(new SinglePointInformation(200, true, new QualityDescriptor()));
		newAsdu.addInformationObject(new SinglePointInformation(201, false, new QualityDescriptor()));
		newAsdu.addInformationObject(new SinglePointInformation(202, true, new QualityDescriptor()));
		newAsdu.addInformationObject(new SinglePointInformation(203, false, new QualityDescriptor()));
		newAsdu.addInformationObject(new SinglePointInformation(204, true, new QualityDescriptor()));
		newAsdu.addInformationObject(new SinglePointInformation(205, false, new QualityDescriptor()));
		newAsdu.addInformationObject(new SinglePointInformation(206, true, new QualityDescriptor()));
		newAsdu.addInformationObject(new SinglePointInformation(207, false, new QualityDescriptor()));

		connection.sendASDU(newAsdu);

		newAsdu = new ASDU(cp, CauseOfTransmission.INTERROGATED_BY_STATION, false, false, (byte) 2, 1, true);

		newAsdu.addInformationObject(new MeasuredValueNormalizedWithoutQuality(300, -1.0f));
		newAsdu.addInformationObject(new MeasuredValueNormalizedWithoutQuality(301, -0.5f));
		newAsdu.addInformationObject(new MeasuredValueNormalizedWithoutQuality(302, -0.1f));
		newAsdu.addInformationObject(new MeasuredValueNormalizedWithoutQuality(303, .0f));
		newAsdu.addInformationObject(new MeasuredValueNormalizedWithoutQuality(304, 0.1f));
		newAsdu.addInformationObject(new MeasuredValueNormalizedWithoutQuality(305, 0.2f));
		newAsdu.addInformationObject(new MeasuredValueNormalizedWithoutQuality(306, 0.5f));
		newAsdu.addInformationObject(new MeasuredValueNormalizedWithoutQuality(307, 0.7f));
		newAsdu.addInformationObject(new MeasuredValueNormalizedWithoutQuality(308, 0.99f));
		newAsdu.addInformationObject(new MeasuredValueNormalizedWithoutQuality(309, 1f));

		connection.sendASDU(newAsdu);

		connection.sendACT_TERM(asdu);

		return true;
	}

	private static boolean asduHandler(Object parameter, IMasterConnection connection, ASDU asdu) {

		if (asdu.getTypeId() == TypeID.C_SC_NA_1) {
			System.out.println("Single command");

			try {
				SingleCommand sc = (SingleCommand) asdu.getElement(0);

				System.out.println(sc.toString());
			} catch (ASDUParsingException e) {

				e.printStackTrace();
			}
		} else if (asdu.getTypeId() == TypeID.C_CS_NA_1) {

			try {
				ClockSynchronizationCommand qsc = (ClockSynchronizationCommand) asdu.getElement(0);

				System.out.println("Received clock sync command with time " + qsc.getNewTime().toString());
			} catch (ASDUParsingException e) {

				e.printStackTrace();
			}
		}

		return true;
	}

	public static void main(String[] args) throws ConnectionException {
		boolean running = true;

		Server server = new Server();

		server.setDebugOutput(true);

		server.setMaxQueueSize(10);

		server.setInterrogationHandler((Object parameter, IMasterConnection connection, ASDU asdu,
				byte qoi) -> interrogationHandler(parameter, connection, asdu, qoi), null);

		server.setASDUHandler(
				(Object parameter, IMasterConnection connection, ASDU asdu) -> asduHandler(parameter, connection, asdu),
				null);

		server.run();

		ASDU newAsdu = new ASDU(server.getApplicationLayerParameters(), CauseOfTransmission.INITIALIZED, false, false,
				(byte) 0, 1, false);
		EndOfInitialization eoi = new EndOfInitialization((byte) 0);
		newAsdu.addInformationObject(eoi);
		server.enqueueASDU(newAsdu);

		int waitTime = 1000;

		while (running) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (waitTime > 0) {
				waitTime -= 100;
			} else {

				newAsdu = new ASDU(server.getApplicationLayerParameters(), CauseOfTransmission.PERIODIC, false, false,
						(byte) 2, 1, false);

				newAsdu.addInformationObject(new MeasuredValueScaled(110, -1, new QualityDescriptor()));

				server.enqueueASDU(newAsdu);

				waitTime = 1000;
			}
		}

		System.out.println("Stop server");
		server.stop();
	}

}
