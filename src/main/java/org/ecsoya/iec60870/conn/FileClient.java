package org.ecsoya.iec60870.conn;

import java.util.function.Consumer;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.ConnectionException;
import org.ecsoya.iec60870.asdu.ASDU;
import org.ecsoya.iec60870.asdu.CauseOfTransmission;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.ie.FileACK;
import org.ecsoya.iec60870.asdu.ie.FileCallOrSelect;
import org.ecsoya.iec60870.asdu.ie.FileLastSegmentOrSection;
import org.ecsoya.iec60870.asdu.ie.FileReady;
import org.ecsoya.iec60870.asdu.ie.FileSegment;
import org.ecsoya.iec60870.asdu.ie.SectionReady;
import org.ecsoya.iec60870.asdu.ie.value.AcknowledgeQualifier;
import org.ecsoya.iec60870.asdu.ie.value.FileClientState;
import org.ecsoya.iec60870.asdu.ie.value.FileError;
import org.ecsoya.iec60870.asdu.ie.value.FileErrorCode;
import org.ecsoya.iec60870.asdu.ie.value.LastSectionOrSegmentQualifier;
import org.ecsoya.iec60870.asdu.ie.value.NameOfFile;
import org.ecsoya.iec60870.asdu.ie.value.SelectAndCallQualifier;

public class FileClient {
	private FileClientState state = FileClientState.IDLE;
	private Master master;

	private int ca;
	private int ioa;
	private NameOfFile nof;
	private IFileReceiver fileReceiver = null;

	private Consumer<String> DebugLog;

	private int segmentOffset = 0;

	public FileClient(Master master, Consumer<String> debugLog) {
		this.master = master;
		DebugLog = debugLog;
	}

	private ASDU NewAsdu(InformationObject io) {
		ASDU asdu = new ASDU(master.getApplicationLayerParameters(), CauseOfTransmission.FILE_TRANSFER, false, false,
				(byte) 0, ca, false);

		asdu.addInformationObject(io);

		return asdu;
	}

	private void ResetStateToIdle() {
		fileReceiver = null;
		state = FileClientState.IDLE;
	}

	private void AbortFileTransfer(FileErrorCode errorCode) throws ConnectionException {
		ASDU deactivateFile = NewAsdu(new FileCallOrSelect(ioa, nof, (byte) 0, SelectAndCallQualifier.DEACTIVATE_FILE));

		master.sendASDU(deactivateFile);

		if (fileReceiver != null)
			fileReceiver.Finished(errorCode);

		ResetStateToIdle();
	}

	public boolean HandleFileAsdu(ASDU asdu) throws ASDUParsingException, ConnectionException {
		boolean asduHandled = true;

		switch (asdu.getTypeId()) {

		case F_SC_NA_1: /* File/Section/Directory Call/Select */

			DebugLog("Received SELECT/CALL");

			if (state == FileClientState.WAITING_FOR_FILE_READY) {

				if (asdu.getCauseOfTransmission() == CauseOfTransmission.UNKNOWN_TYPE_ID) {

					if (fileReceiver != null)
						fileReceiver.Finished(FileErrorCode.UNKNOWN_SERVICE);
				} else if (asdu.getCauseOfTransmission() == CauseOfTransmission.UNKNOWN_COMMON_ADDRESS_OF_ASDU) {

					if (fileReceiver != null)
						fileReceiver.Finished(FileErrorCode.UNKNOWN_CA);
				} else if (asdu.getCauseOfTransmission() == CauseOfTransmission.UNKNOWN_INFORMATION_OBJECT_ADDRESS) {

					if (fileReceiver != null)
						fileReceiver.Finished(FileErrorCode.UNKNOWN_IOA);
				} else {
					if (fileReceiver != null)
						fileReceiver.Finished(FileErrorCode.PROTOCOL_ERROR);
				}
			} else {
				if (fileReceiver != null)
					fileReceiver.Finished(FileErrorCode.PROTOCOL_ERROR);
			}

			ResetStateToIdle();

			break;

		case F_FR_NA_1: /* File ready */

			DebugLog("Received FILE READY");

			if (state == FileClientState.WAITING_FOR_FILE_READY) {

				// TODO check ca, ioa, nof

				FileReady fileReady = (FileReady) asdu.getElement(0);

				if (fileReady.isPositive()) {

					ASDU callFile = NewAsdu(
							new FileCallOrSelect(ioa, nof, (byte) 0, SelectAndCallQualifier.REQUEST_FILE));
					master.sendASDU(callFile);

					DebugLog("Send CALL FILE");

					state = FileClientState.WAITING_FOR_SECTION_READY;

				} else {
					if (fileReceiver != null)
						fileReceiver.Finished(FileErrorCode.FILE_NOT_READY);

					ResetStateToIdle();
				}

			} else if (state == FileClientState.IDLE) {

				// TODO call user callback to

				// TODO send positive or negative ACK

				state = FileClientState.WAITING_FOR_SECTION_READY;

			} else {
				AbortFileTransfer(FileErrorCode.PROTOCOL_ERROR);
			}

			break;

		case F_SR_NA_1: /* Section ready */

			DebugLog("Received SECTION READY");

			if (state == FileClientState.WAITING_FOR_SECTION_READY) {

				SectionReady sc = (SectionReady) asdu.getElement(0);

				if (!sc.isNotReady()) {

					ASDU callSection = NewAsdu(
							new FileCallOrSelect(ioa, nof, (byte) 0, SelectAndCallQualifier.REQUEST_SECTION));
					master.sendASDU(callSection);

					DebugLog("Send CALL SECTION");

					segmentOffset = 0;
					state = FileClientState.RECEIVING_SECTION;

				} else {
					AbortFileTransfer(FileErrorCode.SECTION_NOT_READY);
				}

			} else if (state == FileClientState.IDLE) {
			} else {
				if (fileReceiver != null)
					fileReceiver.Finished(FileErrorCode.PROTOCOL_ERROR);

				ResetStateToIdle();
			}

			break;

		case F_SG_NA_1: /* Segment */

			DebugLog("Received SEGMENT");

			if (state == FileClientState.RECEIVING_SECTION) {

				FileSegment segment = (FileSegment) asdu.getElement(0);

				if (fileReceiver != null) {
					fileReceiver.SegmentReceived(segment.getNameOfSection(), segmentOffset, segment.getLos(),
							segment.getData());
				}

				segmentOffset += segment.getLos();
			} else if (state == FileClientState.IDLE) {
			} else {
				AbortFileTransfer(FileErrorCode.PROTOCOL_ERROR);
			}

			break;

		case F_LS_NA_1: /* Last segment or section */

			DebugLog("Received LAST SEGMENT/SECTION");

			if (state != FileClientState.IDLE) {

				FileLastSegmentOrSection lastSection = (FileLastSegmentOrSection) asdu.getElement(0);

				if (lastSection.getLsq() == LastSectionOrSegmentQualifier.SECTION_TRANSFER_WITHOUT_DEACT) {

					if (state == FileClientState.RECEIVING_SECTION) {

						ASDU segmentAck = NewAsdu(new FileACK(ioa, nof, lastSection.getNameOfSection(),
								AcknowledgeQualifier.POS_ACK_SECTION, FileError.DEFAULT));

						master.sendASDU(segmentAck);

						DebugLog("Send SEGMENT ACK");

						state = FileClientState.WAITING_FOR_SECTION_READY;
					} else {
						AbortFileTransfer(FileErrorCode.PROTOCOL_ERROR);
					}
				} else if (lastSection.getLsq() == LastSectionOrSegmentQualifier.FILE_TRANSFER_WITH_DEACT) {
					/* slave aborted transfer */

					if (fileReceiver != null)
						fileReceiver.Finished(FileErrorCode.ABORTED_BY_REMOTE);

					ResetStateToIdle();
				} else if (lastSection.getLsq() == LastSectionOrSegmentQualifier.FILE_TRANSFER_WITHOUT_DEACT) {

					if (state == FileClientState.WAITING_FOR_SECTION_READY) {
						ASDU fileAck = NewAsdu(new FileACK(ioa, nof, lastSection.getNameOfSection(),
								AcknowledgeQualifier.POS_ACK_FILE, FileError.DEFAULT));

						master.sendASDU(fileAck);

						DebugLog("Send FILE ACK");

						if (fileReceiver != null)
							fileReceiver.Finished(FileErrorCode.SUCCESS);

						ResetStateToIdle();
					} else {

						DebugLog("Illegal state: " + state.toString());

						AbortFileTransfer(FileErrorCode.PROTOCOL_ERROR);
					}
				}
			}

			break;

		default:

			asduHandled = false;
			break;
		}

		return asduHandled;
	}

	private void DebugLog(String string) {
		DebugLog.accept(string);
	}

	public void HandleFileService() {
		// TODO timeout handling
	}

	public void RequestFile(int ca, int ioa, NameOfFile nof, IFileReceiver fileReceiver) throws ConnectionException {
		this.ca = ca;
		this.ioa = ioa;
		this.nof = nof;
		this.fileReceiver = fileReceiver;

		ASDU selectFile = NewAsdu(new FileCallOrSelect(ioa, nof, (byte) 0, SelectAndCallQualifier.SELECT_FILE));

		master.sendASDU(selectFile);

		state = FileClientState.WAITING_FOR_FILE_READY;
	}

}