package org.ecsoya.iec60870.cs104;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Scanner;

import org.ecsoya.iec60870.CP56Time2a;
import org.ecsoya.iec60870.LibraryCommon;
import org.ecsoya.iec60870.asdu.ASDU;
import org.ecsoya.iec60870.asdu.CauseOfTransmission;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.DoubleCommand;
import org.ecsoya.iec60870.asdu.ie.FileDirectory;
import org.ecsoya.iec60870.asdu.ie.MeasuredValueNormalizedWithoutQuality;
import org.ecsoya.iec60870.asdu.ie.MeasuredValueScaled;
import org.ecsoya.iec60870.asdu.ie.MeasuredValueScaledWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.MeasuredValueShort;
import org.ecsoya.iec60870.asdu.ie.MeasuredValueShortWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.SingleCommand;
import org.ecsoya.iec60870.asdu.ie.SingleCommandWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.SinglePointInformation;
import org.ecsoya.iec60870.asdu.ie.SinglePointWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.StepCommand;
import org.ecsoya.iec60870.asdu.ie.value.FileErrorCode;
import org.ecsoya.iec60870.asdu.ie.value.NameOfFile;
import org.ecsoya.iec60870.asdu.ie.value.QualifierOfInterrogation;
import org.ecsoya.iec60870.asdu.ie.value.StepCommandValue;
import org.ecsoya.iec60870.core.ConnectionException;
import org.ecsoya.iec60870.core.file.IFileReceiver;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CS104Client1 {

	public static class Receiver implements IFileReceiver {

		@Override
		public void finished(FileErrorCode result) {
			System.out.println("File download finished - code: " + result);
		}

		@Override
		public void segmentReceived(byte sectionName, int offset, int size, byte[] data) {
			System.out.println(
					String.format("File segment - sectionName: {0} offset: {1} size: {2}", sectionName, offset, size));
		}
	}

	private static boolean asduReceivedHandler(Object parameter, int address, ASDU asdu) {
		System.out.println(asdu);

		try {

			if (asdu.getTypeId() == TypeID.M_SP_NA_1) {

				for (int i = 0; i < asdu.getNumberOfElements(); i++) {

					SinglePointInformation val = (SinglePointInformation) asdu.getElement(i);

					System.out.println("  IOA: " + val.getObjectAddress() + " SP value: " + val.getValue());
					System.out.println("   " + val.getQuality());
				}
			} else if (asdu.getTypeId() == TypeID.M_ME_TE_1) {

				for (int i = 0; i < asdu.getNumberOfElements(); i++) {

					MeasuredValueScaledWithCP56Time2a msv = (MeasuredValueScaledWithCP56Time2a) asdu.getElement(i);

					System.out.println("  IOA: " + msv.getObjectAddress() + " scaled value: " + msv.getScaledValue());
					System.out.println("   " + msv.getQuality());
					System.out.println("   " + msv.getTimestamp());
				}

			} else if (asdu.getTypeId() == TypeID.M_ME_TF_1) {

				for (int i = 0; i < asdu.getNumberOfElements(); i++) {
					MeasuredValueShortWithCP56Time2a mfv = (MeasuredValueShortWithCP56Time2a) asdu.getElement(i);

					System.out.println("  IOA: " + mfv.getObjectAddress() + " float value: " + mfv.getValue());
					System.out.println("   " + mfv.getQuality());
					System.out.println("   " + mfv.getTimestamp());
					System.out.println("   " + mfv.getTimestamp().getDateTime().toString());
				}
			} else if (asdu.getTypeId() == TypeID.M_SP_TB_1) {

				for (int i = 0; i < asdu.getNumberOfElements(); i++) {

					SinglePointWithCP56Time2a val = (SinglePointWithCP56Time2a) asdu.getElement(i);

					System.out.println("  IOA: " + val.getObjectAddress() + " SP value: " + val.getValue());
					System.out.println("   " + val.getQuality());
					System.out.println("   " + val.getTimestamp());
				}
			} else if (asdu.getTypeId() == TypeID.M_ME_NC_1) {

				for (int i = 0; i < asdu.getNumberOfElements(); i++) {
					MeasuredValueShort mfv = (MeasuredValueShort) asdu.getElement(i);

					System.out.println("  IOA: " + mfv.getObjectAddress() + " float value: " + mfv.getValue());
					System.out.println("   " + mfv.getQuality());
				}
			} else if (asdu.getTypeId() == TypeID.M_ME_NB_1) {

				for (int i = 0; i < asdu.getNumberOfElements(); i++) {

					MeasuredValueScaled msv = (MeasuredValueScaled) asdu.getElement(i);

					System.out.println("  IOA: " + msv.getObjectAddress() + " scaled value: " + msv.getScaledValue());
					System.out.println("   " + msv.getQuality());
				}

			} else if (asdu.getTypeId() == TypeID.M_ME_ND_1) {

				for (int i = 0; i < asdu.getNumberOfElements(); i++) {

					MeasuredValueNormalizedWithoutQuality msv = (MeasuredValueNormalizedWithoutQuality) asdu
							.getElement(i);

					System.out
							.println("  IOA: " + msv.getObjectAddress() + " scaled value: " + msv.getNormalizedValue());
				}

			} else if (asdu.getTypeId() == TypeID.C_IC_NA_1) {
				if (asdu.getCauseOfTransmission() == CauseOfTransmission.ACTIVATION_CON) {
					System.out.println(
							(asdu.isNegative() ? "Negative" : "Positive") + "confirmation for interrogation command");
				} else if (asdu.getCauseOfTransmission() == CauseOfTransmission.ACTIVATION_TERMINATION) {
					System.out.println("Interrogation command terminated");
				}
			} else if (asdu.getTypeId() == TypeID.F_DR_TA_1) {
				System.out.println("Received file directory:\n------------------------");
				int ca = asdu.getCommonAddress();

				for (int i = 0; i < asdu.getNumberOfElements(); i++) {
					FileDirectory fd = (FileDirectory) asdu.getElement(i);

					System.out.print(fd.isFOR() ? "DIR:  " : "FILE: ");

					System.out.println(
							String.format("CA: {0} IOA: {1} Type: {2}", ca, fd.getObjectAddress(), fd.getNof()));
				}

			} else {
				System.out.println("Unknown message type!");
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static void ConnectionHandler(Object parameter, ConnectionEvent connectionEvent) {
		switch (connectionEvent) {
		case OPENED:
			System.out.println("Connected");
			break;
		case CLOSED:
			System.out.println("Connection closed");
			break;
		case STARTDT_CON_RECEIVED:
			System.out.println("STARTDT CON received");
			break;
		case STOPDT_CON_RECEIVED:
			System.out.println("STOPDT CON received");
			break;
		default:
			break;
		}
	}

	public static void main(String[] args) throws ConnectionException, InterruptedException, IOException {
		System.out.println("Using lib60870.Java version " + LibraryCommon.getLibraryVersionString());

		Connection con = new Connection("127.0.0.1");

		con.setDebugOutput((msg) -> System.out.println(msg));

		con.setASDUReceivedHandler(
				(Object parameter, int address, ASDU asdu) -> asduReceivedHandler(parameter, address, asdu), null);
		con.setConnectionHandler(
				(Object parameter, ConnectionEvent connectionEvent) -> ConnectionHandler(parameter, connectionEvent),
				null);

		con.start();

		con.getDirectory(1);

		con.getFile(1, 30000, NameOfFile.TRANSPARENT_FILE, new Receiver());

		Thread.sleep(50000);

		con.sendTestCommand(1);

		con.sendInterrogationCommand(CauseOfTransmission.ACTIVATION, 1, QualifierOfInterrogation.STATION);

		Thread.sleep(5000);

		con.sendControlCommand(CauseOfTransmission.ACTIVATION, 1, new SingleCommand(5000, true, false, 0));

		con.sendControlCommand(CauseOfTransmission.ACTIVATION, 1, new DoubleCommand(5001, DoubleCommand.ON, false, 0));

		con.sendControlCommand(CauseOfTransmission.ACTIVATION, 1,
				new StepCommand(5002, StepCommandValue.HIGHER.getValue(), false, 0));

		con.sendControlCommand(CauseOfTransmission.ACTIVATION, 1,
				new SingleCommandWithCP56Time2a(5000, false, false, 0, new CP56Time2a(LocalDateTime.now())));

		/* Synchronize clock of the controlled station */
		con.sendClockSyncCommand(1 /* CA */, new CP56Time2a(LocalDateTime.now()));

		System.out.println("CLOSE");

		con.stop();

		System.out.println("RECONNECT");

		con.start();

		Thread.sleep(5000);

		System.out.println("CLOSE 2");

		con.stop();

		System.out.println("Press any key to terminate...");

		Scanner in = new Scanner(System.in);
		while (in.hasNext()) {
			String string = in.next();
			break;
		}
	}

}
