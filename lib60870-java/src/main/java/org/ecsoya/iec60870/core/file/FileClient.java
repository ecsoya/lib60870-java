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
package org.ecsoya.iec60870.core.file;

import java.util.function.Consumer;

import org.ecsoya.iec60870.asdu.ASDU;
import org.ecsoya.iec60870.asdu.ASDUParsingException;
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
import org.ecsoya.iec60870.core.ConnectionException;
import org.ecsoya.iec60870.core.Master;

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

	private void abortFileTransfer(FileErrorCode errorCode) throws ConnectionException {
		ASDU deactivateFile = newAsdu(new FileCallOrSelect(ioa, nof, (byte) 0, SelectAndCallQualifier.DEACTIVATE_FILE));

		master.sendASDU(deactivateFile);

		if (fileReceiver != null) {
			fileReceiver.finished(errorCode);
		}

		resetStateToIdle();
	}

	private void debugLog(String string) {
		DebugLog.accept(string);
	}

	public boolean handleFileAsdu(ASDU asdu) throws ASDUParsingException, ConnectionException {
		boolean asduHandled = true;

		switch (asdu.getTypeId()) {

		case F_SC_NA_1: /* File/Section/Directory Call/Select */

			debugLog("Received SELECT/CALL");

			if (state == FileClientState.WAITING_FOR_FILE_READY) {

				if (asdu.getCauseOfTransmission() == CauseOfTransmission.UNKNOWN_TYPE_ID) {

					if (fileReceiver != null) {
						fileReceiver.finished(FileErrorCode.UNKNOWN_SERVICE);
					}
				} else if (asdu.getCauseOfTransmission() == CauseOfTransmission.UNKNOWN_COMMON_ADDRESS_OF_ASDU) {

					if (fileReceiver != null) {
						fileReceiver.finished(FileErrorCode.UNKNOWN_CA);
					}
				} else if (asdu.getCauseOfTransmission() == CauseOfTransmission.UNKNOWN_INFORMATION_OBJECT_ADDRESS) {

					if (fileReceiver != null) {
						fileReceiver.finished(FileErrorCode.UNKNOWN_IOA);
					}
				} else {
					if (fileReceiver != null) {
						fileReceiver.finished(FileErrorCode.PROTOCOL_ERROR);
					}
				}
			} else {
				if (fileReceiver != null) {
					fileReceiver.finished(FileErrorCode.PROTOCOL_ERROR);
				}
			}

			resetStateToIdle();

			break;

		case F_FR_NA_1: /* File ready */

			debugLog("Received FILE READY");

			if (state == FileClientState.WAITING_FOR_FILE_READY) {

				// TODO check ca, ioa, nof

				FileReady fileReady = (FileReady) asdu.getElement(0);

				if (fileReady.isPositive()) {

					ASDU callFile = newAsdu(
							new FileCallOrSelect(ioa, nof, (byte) 0, SelectAndCallQualifier.REQUEST_FILE));
					master.sendASDU(callFile);

					debugLog("Send CALL FILE");

					state = FileClientState.WAITING_FOR_SECTION_READY;

				} else {
					if (fileReceiver != null) {
						fileReceiver.finished(FileErrorCode.FILE_NOT_READY);
					}

					resetStateToIdle();
				}

			} else if (state == FileClientState.IDLE) {

				// TODO call user callback to

				// TODO send positive or negative ACK

				state = FileClientState.WAITING_FOR_SECTION_READY;

			} else {
				abortFileTransfer(FileErrorCode.PROTOCOL_ERROR);
			}

			break;

		case F_SR_NA_1: /* Section ready */

			debugLog("Received SECTION READY");

			if (state == FileClientState.WAITING_FOR_SECTION_READY) {

				SectionReady sc = (SectionReady) asdu.getElement(0);

				if (!sc.isNotReady()) {

					ASDU callSection = newAsdu(
							new FileCallOrSelect(ioa, nof, (byte) 0, SelectAndCallQualifier.REQUEST_SECTION));
					master.sendASDU(callSection);

					debugLog("Send CALL SECTION");

					segmentOffset = 0;
					state = FileClientState.RECEIVING_SECTION;

				} else {
					abortFileTransfer(FileErrorCode.SECTION_NOT_READY);
				}

			} else if (state == FileClientState.IDLE) {
			} else {
				if (fileReceiver != null) {
					fileReceiver.finished(FileErrorCode.PROTOCOL_ERROR);
				}

				resetStateToIdle();
			}

			break;

		case F_SG_NA_1: /* Segment */

			debugLog("Received SEGMENT");

			if (state == FileClientState.RECEIVING_SECTION) {

				FileSegment segment = (FileSegment) asdu.getElement(0);

				if (fileReceiver != null) {
					fileReceiver.segmentReceived(segment.getNameOfSection(), segmentOffset, segment.getLos(),
							segment.getData());
				}

				segmentOffset += segment.getLos();
			} else if (state == FileClientState.IDLE) {
			} else {
				abortFileTransfer(FileErrorCode.PROTOCOL_ERROR);
			}

			break;

		case F_LS_NA_1: /* Last segment or section */

			debugLog("Received LAST SEGMENT/SECTION");

			if (state != FileClientState.IDLE) {

				FileLastSegmentOrSection lastSection = (FileLastSegmentOrSection) asdu.getElement(0);

				if (lastSection.getLsq() == LastSectionOrSegmentQualifier.SECTION_TRANSFER_WITHOUT_DEACT) {

					if (state == FileClientState.RECEIVING_SECTION) {

						ASDU segmentAck = newAsdu(new FileACK(ioa, nof, lastSection.getNameOfSection(),
								AcknowledgeQualifier.POS_ACK_SECTION, FileError.DEFAULT));

						master.sendASDU(segmentAck);

						debugLog("Send SEGMENT ACK");

						state = FileClientState.WAITING_FOR_SECTION_READY;
					} else {
						abortFileTransfer(FileErrorCode.PROTOCOL_ERROR);
					}
				} else if (lastSection.getLsq() == LastSectionOrSegmentQualifier.FILE_TRANSFER_WITH_DEACT) {
					/* slave aborted transfer */

					if (fileReceiver != null) {
						fileReceiver.finished(FileErrorCode.ABORTED_BY_REMOTE);
					}

					resetStateToIdle();
				} else if (lastSection.getLsq() == LastSectionOrSegmentQualifier.FILE_TRANSFER_WITHOUT_DEACT) {

					if (state == FileClientState.WAITING_FOR_SECTION_READY) {
						ASDU fileAck = newAsdu(new FileACK(ioa, nof, lastSection.getNameOfSection(),
								AcknowledgeQualifier.POS_ACK_FILE, FileError.DEFAULT));

						master.sendASDU(fileAck);

						debugLog("Send FILE ACK");

						if (fileReceiver != null) {
							fileReceiver.finished(FileErrorCode.SUCCESS);
						}

						resetStateToIdle();
					} else {

						debugLog("Illegal state: " + state.toString());

						abortFileTransfer(FileErrorCode.PROTOCOL_ERROR);
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

	public void handleFileService() {
		// TODO timeout handling
	}

	private ASDU newAsdu(InformationObject io) {
		ASDU asdu = new ASDU(master.getApplicationLayerParameters(), CauseOfTransmission.FILE_TRANSFER, false, false,
				(byte) 0, ca, false);

		asdu.addInformationObject(io);

		return asdu;
	}

	public void requestFile(int ca, int ioa, NameOfFile nof, IFileReceiver fileReceiver) throws ConnectionException {
		this.ca = ca;
		this.ioa = ioa;
		this.nof = nof;
		this.fileReceiver = fileReceiver;

		ASDU selectFile = newAsdu(new FileCallOrSelect(ioa, nof, (byte) 0, SelectAndCallQualifier.SELECT_FILE));

		master.sendASDU(selectFile);

		state = FileClientState.WAITING_FOR_FILE_READY;
	}

	private void resetStateToIdle() {
		fileReceiver = null;
		state = FileClientState.IDLE;
	}

}
