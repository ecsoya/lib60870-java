package org.ecsoya.iec60870.conn;

import java.util.function.Consumer;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.asdu.ASDU;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.CauseOfTransmission;
import org.ecsoya.iec60870.asdu.ie.FileACK;
import org.ecsoya.iec60870.asdu.ie.FileCallOrSelect;
import org.ecsoya.iec60870.asdu.ie.FileLastSegmentOrSection;
import org.ecsoya.iec60870.asdu.ie.FileReady;
import org.ecsoya.iec60870.asdu.ie.FileSegment;
import org.ecsoya.iec60870.asdu.ie.SectionReady;
import org.ecsoya.iec60870.asdu.ie.value.AcknowledgeQualifier;
import org.ecsoya.iec60870.asdu.ie.value.FileServerState;
import org.ecsoya.iec60870.asdu.ie.value.LastSectionOrSegmentQualifier;
import org.ecsoya.iec60870.asdu.ie.value.SelectAndCallQualifier;
import org.ecsoya.iec60870.conn.FilesAvailable.CS101n104File;

public class FileServer {

	public FileServer(IMasterConnection masterConnection, FilesAvailable availableFiles, Consumer<String> logger) {
		transferState = FileServerState.UNSELECTED_IDLE;
		alParameters = masterConnection.GetApplicationLayerParameters();
		maxSegmentSize = FileSegment.GetMaxDataSize(alParameters);
		this.availableFiles = availableFiles;
		this.logger = logger;
		this.connection = masterConnection;
	}

	private FilesAvailable availableFiles;

	private CS101n104File selectedFile;

	private Consumer<String> logger;

	private ApplicationLayerParameters alParameters;

	private IMasterConnection connection;
	private int maxSegmentSize;

	private byte currentSectionNumber;
	private int currentSectionSize;
	private int currentSectionOffset;
	private byte sectionChecksum = 0;
	private byte fileChecksum = 0;

	private FileServerState transferState;

	private void SendDirectory() {

	}

	public boolean HandleFileAsdu(ASDU asdu) throws ASDUParsingException {
		boolean handled = true;

		switch (asdu.getTypeId()) {

		case F_AF_NA_1: /* 124 - ACK file, ACK section */

			logger("Received file/section ACK F_AF_NA_1");

			if (asdu.getCauseOfTransmission() == CauseOfTransmission.FILE_TRANSFER) {

				if (transferState != FileServerState.UNSELECTED_IDLE) {

					IFileProvider file = selectedFile.provider;

					FileACK ack = (FileACK) asdu.getElement(0);

					if (ack.getAckQualifier() == AcknowledgeQualifier.POS_ACK_FILE) {

						logger("Received positive file ACK");

						if (transferState == FileServerState.WAITING_FOR_FILE_ACK) {

							selectedFile.provider.TransferComplete(true);

							availableFiles.RemoveFile(selectedFile.provider);

							selectedFile = null;

							transferState = FileServerState.UNSELECTED_IDLE;
						} else {
							logger("Unexpected file transfer state --> abort file transfer");

							transferState = FileServerState.SEND_ABORT;
						}

					} else if (ack.getAckQualifier() == AcknowledgeQualifier.NEG_ACK_FILE) {

						logger("Received negative file ACK - stop transfer");

						if (transferState == FileServerState.WAITING_FOR_FILE_ACK) {

							selectedFile.provider.TransferComplete(false);

							selectedFile.selectedBy = null;
							selectedFile = null;

							transferState = FileServerState.UNSELECTED_IDLE;
						} else {
							logger("Unexpected file transfer state --> abort file transfer");

							transferState = FileServerState.SEND_ABORT;
						}

					} else if (ack.getAckQualifier() == AcknowledgeQualifier.NEG_ACK_SECTION) {

						logger("Received negative file section ACK - repeat section");

						if (transferState == FileServerState.WAITING_FOR_SECTION_ACK) {
							currentSectionOffset = 0;
							sectionChecksum = 0;

							ASDU sectionReady = new ASDU(alParameters, CauseOfTransmission.FILE_TRANSFER, false, false,
									(byte) 0, file.GetCA(), false);

							sectionReady.addInformationObject(new SectionReady(selectedFile.provider.GetIOA(),
									selectedFile.provider.GetNameOfFile(), currentSectionNumber, currentSectionSize,
									false));

							connection.SendASDU(sectionReady);

							transferState = FileServerState.TRANSMIT_SECTION;
						} else {
							logger("Unexpected file transfer state --> abort file transfer");

							transferState = FileServerState.SEND_ABORT;
						}

					} else if (ack.getAckQualifier() == AcknowledgeQualifier.POS_ACK_SECTION) {

						if (transferState == FileServerState.WAITING_FOR_SECTION_ACK) {

							currentSectionNumber++;

							int nextSectionSize = selectedFile.provider.GetSectionSize(currentSectionNumber);

							ASDU responseAsdu = new ASDU(alParameters, CauseOfTransmission.FILE_TRANSFER, false, false,
									(byte) 0, file.GetCA(), false);

							if (nextSectionSize == -1) {
								logger("Reveived positive file section ACK - send last section indication");

								responseAsdu.addInformationObject(new FileLastSegmentOrSection(file.GetIOA(),
										file.GetNameOfFile(), (byte) currentSectionNumber,
										LastSectionOrSegmentQualifier.FILE_TRANSFER_WITHOUT_DEACT, fileChecksum));

								transferState = FileServerState.WAITING_FOR_FILE_ACK;
							} else {
								logger("Reveived positive file section ACK - send next section ready indication");

								currentSectionSize = nextSectionSize;

								responseAsdu.addInformationObject(new SectionReady(selectedFile.provider.GetIOA(),
										selectedFile.provider.GetNameOfFile(), currentSectionNumber, currentSectionSize,
										false));

								transferState = FileServerState.WAITING_FOR_SECTION_CALL;
							}

							connection.SendASDU(responseAsdu);

							sectionChecksum = 0;
						} else {
							logger("Unexpected file transfer state --> abort file transfer");

							transferState = FileServerState.SEND_ABORT;
						}
					}
				} else {
					// No file transmission in progress --> what to do?
					logger("Unexpected File ACK message -> ignore");
				}

			} else {
				asdu.setCauseOfTransmission(CauseOfTransmission.UNKNOWN_CAUSE_OF_TRANSMISSION);
				connection.SendASDU(asdu);
			}
			break;

		case F_SC_NA_1: /* 122 - Call/Select directoy/file/section */

			logger("Received call/select F_SC_NA_1");

			if (asdu.getCauseOfTransmission() == CauseOfTransmission.FILE_TRANSFER) {

				FileCallOrSelect sc = (FileCallOrSelect) asdu.getElement(0);

				if (sc.getScq() == SelectAndCallQualifier.SELECT_FILE) {

					if (transferState == FileServerState.UNSELECTED_IDLE) {

						logger("Received SELECT FILE");

						CS101n104File file = availableFiles.GetFile(asdu.getCommonAddress(), sc.getObjectAddress(), sc.getNof());

						if (file == null) {
							asdu.setCauseOfTransmission(CauseOfTransmission.UNKNOWN_INFORMATION_OBJECT_ADDRESS);
							connection.SendASDU(asdu);
						} else {

							ASDU fileReady = new ASDU(alParameters, CauseOfTransmission.FILE_TRANSFER, false, false,
									(byte) 0, asdu.getCommonAddress(), false);

							// check if already selected
							if (file.selectedBy == null) {

								file.selectedBy = this;

								fileReady.addInformationObject(new FileReady(sc.getObjectAddress(), sc.getNof(),
										file.provider.GetFileSize(), true));

							} else {
								fileReady.addInformationObject(
										new FileReady(sc.getObjectAddress(), sc.getNof(), 0, false));
							}

							connection.SendASDU(fileReady);

							selectedFile = file;

							transferState = FileServerState.WAITING_FOR_FILE_CALL;
						}

					} else {
						logger("Unexpected SELECT FILE message");
					}

				} else if (sc.getScq() == SelectAndCallQualifier.DEACTIVATE_FILE) {

					logger("Received DEACTIVATE FILE");

					if (transferState != FileServerState.UNSELECTED_IDLE) {

						if (selectedFile != null) {
							selectedFile.selectedBy = null;
							selectedFile = null;
						}

						transferState = FileServerState.UNSELECTED_IDLE;

					} else {
						logger("Unexpected DEACTIVATE FILE message");
					}

				}

				else if (sc.getScq() == SelectAndCallQualifier.REQUEST_FILE) {

					logger("Received CALL FILE");

					if (transferState == FileServerState.WAITING_FOR_FILE_CALL) {

						if (selectedFile.provider.GetIOA() != sc.getObjectAddress()) {
							logger("Unkown IOA");

							asdu.setCauseOfTransmission(CauseOfTransmission.UNKNOWN_INFORMATION_OBJECT_ADDRESS);
							connection.SendASDU(asdu);
						} else {

							ASDU sectionReady = new ASDU(alParameters, CauseOfTransmission.FILE_TRANSFER, false, false,
									(byte) 0, asdu.getCommonAddress(), false);

							sectionReady.addInformationObject(new SectionReady(sc.getObjectAddress(),
									selectedFile.provider.GetNameOfFile(), (byte) 0, 0, false));

							connection.SendASDU(sectionReady);

							logger("Send SECTION READY");

							currentSectionNumber = 0;
							currentSectionOffset = 0;
							currentSectionSize = selectedFile.provider.GetSectionSize(0);

							transferState = FileServerState.WAITING_FOR_SECTION_CALL;
						}

					} else {
						logger("Unexpected FILE CALL message");
					}

				} else if (sc.getScq() == SelectAndCallQualifier.REQUEST_SECTION) {

					logger("Received CALL SECTION");

					if (transferState == FileServerState.WAITING_FOR_SECTION_CALL) {

						if (selectedFile.provider.GetIOA() != sc.getObjectAddress()) {
							logger("Unkown IOA");

							asdu.setCauseOfTransmission(CauseOfTransmission.UNKNOWN_INFORMATION_OBJECT_ADDRESS);
							connection.SendASDU(asdu);
						} else {

							transferState = FileServerState.TRANSMIT_SECTION;
						}
					} else {
						logger("Unexpected SECTION CALL message");
					}
				}

			} else if (asdu.getCauseOfTransmission() == CauseOfTransmission.REQUEST) {
				logger("Call directory received");

				availableFiles.SendDirectoy(connection, false);

			} else {
				asdu.setCauseOfTransmission(CauseOfTransmission.UNKNOWN_CAUSE_OF_TRANSMISSION);
				connection.SendASDU(asdu);
			}
			break;

		default:
			handled = false;
			break;
		}

		return handled;
	}

	public void HandleFileTransmission() {

		if (transferState != FileServerState.UNSELECTED_IDLE) {

			if (transferState == FileServerState.TRANSMIT_SECTION) {

				if (selectedFile != null) {

					IFileProvider file = selectedFile.provider;

					ASDU fileAsdu = new ASDU(alParameters, CauseOfTransmission.FILE_TRANSFER, false, false, (byte) 0,
							file.GetCA(), false);

					if (currentSectionOffset == currentSectionSize) {

						// send last segment

						fileAsdu.addInformationObject(
								new FileLastSegmentOrSection(file.GetIOA(), file.GetNameOfFile(), currentSectionNumber,
										LastSectionOrSegmentQualifier.SECTION_TRANSFER_WITHOUT_DEACT, sectionChecksum));

						fileChecksum += sectionChecksum;
						sectionChecksum = 0;

						logger("Send LAST SEGMENT");

						connection.SendASDU(fileAsdu);

						transferState = FileServerState.WAITING_FOR_SECTION_ACK;

					} else {

						int currentSegmentSize = currentSectionSize - currentSectionOffset;

						if (currentSegmentSize > maxSegmentSize)
							currentSegmentSize = maxSegmentSize;

						byte[] segmentData = new byte[currentSegmentSize];

						file.GetSegmentData(currentSectionNumber, currentSectionOffset, currentSegmentSize,
								segmentData);

						fileAsdu.addInformationObject(new FileSegment(file.GetIOA(), file.GetNameOfFile(),
								currentSectionNumber, segmentData));

						byte checksum = 0;

						for (byte octet : segmentData) {
							checksum += octet;
						}

						connection.SendASDU(fileAsdu);

						sectionChecksum += checksum;

						logger("Send SEGMENT (CHS=" + sectionChecksum + ")");
						currentSectionOffset += currentSegmentSize;

					}
				}
			}

		}

	}

	private void logger(String msg) {
		this.logger.accept(msg);
	}
}