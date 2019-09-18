package org.ecsoya.iec60870.asdu;

import java.util.ArrayList;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.BufferFrame;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ie.Bitstring32;
import org.ecsoya.iec60870.asdu.ie.Bitstring32Command;
import org.ecsoya.iec60870.asdu.ie.Bitstring32CommandWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.Bitstring32WithCP24Time2a;
import org.ecsoya.iec60870.asdu.ie.Bitstring32WithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.ClockSynchronizationCommand;
import org.ecsoya.iec60870.asdu.ie.CounterInterrogationCommand;
import org.ecsoya.iec60870.asdu.ie.DelayAcquisitionCommand;
import org.ecsoya.iec60870.asdu.ie.DoubleCommand;
import org.ecsoya.iec60870.asdu.ie.DoubleCommandWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.DoublePointInformation;
import org.ecsoya.iec60870.asdu.ie.DoublePointWithCP24Time2a;
import org.ecsoya.iec60870.asdu.ie.DoublePointWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.EndOfInitialization;
import org.ecsoya.iec60870.asdu.ie.EventOfProtectionEquipment;
import org.ecsoya.iec60870.asdu.ie.EventOfProtectionEquipmentWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.FileACK;
import org.ecsoya.iec60870.asdu.ie.FileCallOrSelect;
import org.ecsoya.iec60870.asdu.ie.FileDirectory;
import org.ecsoya.iec60870.asdu.ie.FileLastSegmentOrSection;
import org.ecsoya.iec60870.asdu.ie.FileReady;
import org.ecsoya.iec60870.asdu.ie.FileSegment;
import org.ecsoya.iec60870.asdu.ie.IntegratedTotals;
import org.ecsoya.iec60870.asdu.ie.IntegratedTotalsWithCP24Time2a;
import org.ecsoya.iec60870.asdu.ie.IntegratedTotalsWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.InterrogationCommand;
import org.ecsoya.iec60870.asdu.ie.MeasuredValueNormalized;
import org.ecsoya.iec60870.asdu.ie.MeasuredValueNormalizedWithCP24Time2a;
import org.ecsoya.iec60870.asdu.ie.MeasuredValueNormalizedWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.MeasuredValueNormalizedWithoutQuality;
import org.ecsoya.iec60870.asdu.ie.MeasuredValueScaled;
import org.ecsoya.iec60870.asdu.ie.MeasuredValueScaledWithCP24Time2a;
import org.ecsoya.iec60870.asdu.ie.MeasuredValueScaledWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.MeasuredValueShort;
import org.ecsoya.iec60870.asdu.ie.MeasuredValueShortWithCP24Time2a;
import org.ecsoya.iec60870.asdu.ie.MeasuredValueShortWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.PackedOutputCircuitInfo;
import org.ecsoya.iec60870.asdu.ie.PackedOutputCircuitInfoWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.PackedSinglePointWithSCD;
import org.ecsoya.iec60870.asdu.ie.PackedStartEventsOfProtectionEquipment;
import org.ecsoya.iec60870.asdu.ie.PackedStartEventsOfProtectionEquipmentWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.ParameterActivation;
import org.ecsoya.iec60870.asdu.ie.ParameterFloatValue;
import org.ecsoya.iec60870.asdu.ie.ParameterNormalizedValue;
import org.ecsoya.iec60870.asdu.ie.ParameterScaledValue;
import org.ecsoya.iec60870.asdu.ie.ReadCommand;
import org.ecsoya.iec60870.asdu.ie.ResetProcessCommand;
import org.ecsoya.iec60870.asdu.ie.SectionReady;
import org.ecsoya.iec60870.asdu.ie.SetpointCommandNormalized;
import org.ecsoya.iec60870.asdu.ie.SetpointCommandNormalizedWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.SetpointCommandScaled;
import org.ecsoya.iec60870.asdu.ie.SetpointCommandScaledWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.SetpointCommandShort;
import org.ecsoya.iec60870.asdu.ie.SetpointCommandShortWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.SingleCommand;
import org.ecsoya.iec60870.asdu.ie.SingleCommandWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.SinglePointInformation;
import org.ecsoya.iec60870.asdu.ie.SinglePointWithCP24Time2a;
import org.ecsoya.iec60870.asdu.ie.SinglePointWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.StepCommand;
import org.ecsoya.iec60870.asdu.ie.StepCommandWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.StepPositionInformation;
import org.ecsoya.iec60870.asdu.ie.StepPositionWithCP24Time2a;
import org.ecsoya.iec60870.asdu.ie.StepPositionWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.TestCommand;
import org.ecsoya.iec60870.asdu.ie.TestCommandWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.value.PrivateInformationObjectTypes;
import org.ecsoya.iec60870.conn.IPrivateIOFactory;

/**
 * This class represents an application layer message. It contains some generic
 * message information and one or more InformationObject instances of the same
 * type. It is used to send and receive messages.
 */
public class ASDU {
	private ApplicationLayerParameters parameters;

	private TypeID typeId = null;
	private boolean hasTypeId;

	private byte variableStructureQualifier; // variable structure qualifier

	private CauseOfTransmission causeOfTransmission = CauseOfTransmission.values()[0]; // cause
	private byte originatorAddress; // originator address
	private boolean isTest; // is message a test message
	private boolean isNegative; // is message a negative confirmation

	private int commonAddress; // Common address
	private int spaceLeft = 0;

	private byte[] payload = null;
	private ArrayList<InformationObject> informationObjects = null;

	private PrivateInformationObjectTypes privateObjectTypes = null;

	public ASDU(ApplicationLayerParameters parameters, byte[] msg, int bufPos, int msgLength)
			throws ASDUParsingException {
		this.parameters = parameters;

		int asduHeaderSize = 2 + parameters.getSizeOfCOT() + parameters.getSizeOfCA();

		if ((msgLength - bufPos) < asduHeaderSize) {
			throw new ASDUParsingException("Message header too small");
		}

		typeId = TypeID.forValue(msg[bufPos++]);
		variableStructureQualifier = msg[bufPos++];

		this.hasTypeId = true;

		byte cotByte = msg[bufPos++];

		if ((cotByte & 0x80) != 0) {
			isTest = true;
		} else {
			isTest = false;
		}

		if ((cotByte & 0x40) != 0) {
			isNegative = true;
		} else {
			isNegative = false;
		}

		causeOfTransmission = CauseOfTransmission.forValue(cotByte & 0x3f);

		if (parameters.getSizeOfCOT() == 2) {
			originatorAddress = msg[bufPos++];
		}

		commonAddress = msg[bufPos++];

		if (parameters.getSizeOfCA() > 1) {
			commonAddress += (msg[bufPos++] * 0x100);
		}

		int payloadSize = msgLength - bufPos;

		// TODO add plausibility check for payload length (using TypeID, getSizeOfIOA(),
		// and
		// VSQ)

		payload = new byte[payloadSize];

		/* save payload */
		System.arraycopy(msg, bufPos, payload, 0, payloadSize);
	}

	/**
	 * Initializes a new instance of the <see cref="lib60870.CS101.ASDU"/> class.
	 * 
	 * @param parameters application layer parameters to be used for
	 *                   encoding/decoding
	 * @param cot        Cause of transmission (COT)
	 * @param isTest     If set to <c>true</c> ASDU is a test ASDU.
	 * @param isNegative If set to <c>true</c> is negative confirmation.
	 * @param oa         originator address (OA)
	 * @param ca         common address of the ASDU (CA)
	 * @param isSequence If set to <c>true</c> is a sequence of information objects.
	 */
	public ASDU(ApplicationLayerParameters parameters, CauseOfTransmission cot, boolean isTest, boolean isNegative,
			byte oa, int ca, boolean isSequence) {
		this(parameters, TypeID.M_SP_NA_1, cot, isTest, isNegative, oa, ca, isSequence);
		this.hasTypeId = false;
	}

	public ASDU(ApplicationLayerParameters parameters, TypeID typeId, CauseOfTransmission cot, boolean isTest,
			boolean isNegative, byte oa, int ca, boolean isSequence) {
		this.parameters = parameters;
		this.typeId = typeId;
		this.causeOfTransmission = cot;
		this.isTest = isTest;
		this.isNegative = isNegative;
		this.originatorAddress = oa;
		this.commonAddress = ca;
		this.spaceLeft = parameters.getMaxAsduLength() - parameters.getSizeOfTypeId() - parameters.getSizeOfVSQ()
				- parameters.getSizeOfCA() - parameters.getSizeOfCOT();

		if (isSequence) {
			this.variableStructureQualifier = (byte) 0x80;
		} else {
			this.variableStructureQualifier = 0;
		}

		this.hasTypeId = true;
	}

	/**
	 * Adds an information object to the ASDU.
	 * 
	 * This function add an information object (InformationObject) to the ASDU.
	 * NOTE: that all information objects have to be of the same type. Otherwise an
	 * ArgumentException will be thrown. The function returns true when the
	 * information object has been added to the ASDU. The function returns false if
	 * there is no space left in the ASDU to add the information object, or when
	 * object cannot be added to a sequence because the IOA does not match.
	 * 
	 * @return <c>true</c>, if information object was added, <c>false</c> otherwise.
	 * @param io The information object to add
	 */
	public final boolean addInformationObject(InformationObject io) {
		if (informationObjects == null) {
			informationObjects = new ArrayList<InformationObject>();
		}

		if (hasTypeId) {
			if (io.getType() != typeId) {
				throw new IllegalArgumentException("Invalid information object type: expected " + typeId.toString()
						+ " was " + io.getType().toString());
			}
		} else {
			typeId = io.getType();
			hasTypeId = true;
		}

		if (informationObjects.size() >= 0x7f) {
			return false;
		}

		int objectSize = io.getEncodedSize();

		if (isSequence() == false) {
			objectSize += parameters.getSizeOfIOA();
		} else {
			if (informationObjects.isEmpty()) // is first object?
			{
				objectSize += parameters.getSizeOfIOA();
			} else {
				if (io.getObjectAddress() != (informationObjects.get(0).getObjectAddress()
						+ informationObjects.size())) {
					return false;
				}
			}
		}

		if (objectSize <= spaceLeft) {

			spaceLeft -= objectSize;
			informationObjects.add(io);

			variableStructureQualifier = (byte) ((variableStructureQualifier & 0x80) | informationObjects.size());

			return true;
		} else {
			return false;
		}
	}

	public byte[] asByteArray() {
		int expectedSize = parameters.getMaxAsduLength() - spaceLeft;

		BufferFrame frame = new BufferFrame(new byte[expectedSize], 0);

		encode(frame, parameters);

		if (frame.getMsgSize() == expectedSize)
			return frame.getBuffer();
		else
			return null;
	}

	public final void encode(Frame frame, ApplicationLayerParameters parameters) {
		frame.setNextByte((byte) typeId.getValue());
		frame.setNextByte(variableStructureQualifier);

		byte cotByte = (byte) causeOfTransmission.getValue();

		if (isTest) {
			cotByte = (byte) (cotByte | 0x80);
		}

		if (isNegative) {
			cotByte = (byte) (cotByte | 0x40);
		}

		frame.setNextByte(cotByte);

		if (parameters.getSizeOfCOT() == 2) {
			frame.setNextByte((byte) originatorAddress);
		}

		frame.setNextByte((byte) (commonAddress % 256));

		if (parameters.getSizeOfCA() > 1) {
			frame.setNextByte((byte) (commonAddress / 256));
		}

		if (payload != null)
			frame.appendBytes(payload);
		else {

			boolean isFirst = true;

			for (InformationObject io : informationObjects) {

				if (isFirst) {
					io.encode(frame, parameters, false);
					isFirst = false;
				} else {
					if (isSequence())
						io.encode(frame, parameters, true);
					else
						io.encode(frame, parameters, false);
				}

			}
		}
	}

	public CauseOfTransmission getCauseOfTransmission() {
		return causeOfTransmission;
	}

	public int getCommonAddress() {
		return commonAddress;
	}

	/// <summary>
	/// Gets the element (information object) with the specified index
	/// </summary>
	/// <returns>the information object at index</returns>
	/// <param name="index">index of the element (starting with 0)</param>
	/// <exception cref="lib60870.ASDUParsingException">Thrown when there is a
	/// problem parsing the ASDU</exception>
	public InformationObject getElement(int index) throws ASDUParsingException {
		if (index >= getNumberOfElements())
			throw new ASDUParsingException("Index out of range");

		InformationObject retVal = null;

		int elementSize;

		switch (typeId) {

		case M_SP_NA_1: /* 1 */

			elementSize = 1;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new SinglePointInformation(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new SinglePointInformation(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_SP_TA_1: /* 2 */

			elementSize = 4;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new SinglePointWithCP24Time2a(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);
			} else
				retVal = new SinglePointWithCP24Time2a(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_DP_NA_1: /* 3 */

			elementSize = 1;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new DoublePointInformation(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new DoublePointInformation(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_DP_TA_1: /* 4 */

			elementSize = 4;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new DoublePointWithCP24Time2a(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new DoublePointWithCP24Time2a(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_ST_NA_1: /* 5 */

			elementSize = 2;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new StepPositionInformation(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new StepPositionInformation(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_ST_TA_1: /* 6 */

			elementSize = 5;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new StepPositionWithCP24Time2a(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new StepPositionWithCP24Time2a(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_BO_NA_1: /* 7 */

			elementSize = 5;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new Bitstring32(parameters, payload, parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new Bitstring32(parameters, payload, index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_BO_TA_1: /* 8 */

			elementSize = 8;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new Bitstring32WithCP24Time2a(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new Bitstring32WithCP24Time2a(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_ME_NA_1: /* 9 */

			elementSize = 3;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new MeasuredValueNormalized(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new MeasuredValueNormalized(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_ME_TA_1: /* 10 */

			elementSize = 6;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new MeasuredValueNormalizedWithCP24Time2a(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new MeasuredValueNormalizedWithCP24Time2a(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_ME_NB_1: /* 11 */

			elementSize = 3;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new MeasuredValueScaled(parameters, payload, parameters.getSizeOfIOA() + (index * elementSize),
						true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new MeasuredValueScaled(parameters, payload, index * (parameters.getSizeOfIOA() + elementSize),
						false);

			break;

		case M_ME_TB_1: /* 12 */

			elementSize = 6;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new MeasuredValueScaledWithCP24Time2a(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new MeasuredValueScaledWithCP24Time2a(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_ME_NC_1: /* 13 */

			elementSize = 5;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new MeasuredValueShort(parameters, payload, parameters.getSizeOfIOA() + (index * elementSize),
						true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new MeasuredValueShort(parameters, payload, index * (parameters.getSizeOfIOA() + elementSize),
						false);

			break;

		case M_ME_TC_1: /* 14 */

			elementSize = 8;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new MeasuredValueShortWithCP24Time2a(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new MeasuredValueShortWithCP24Time2a(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_IT_NA_1: /* 15 */

			elementSize = 5;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new IntegratedTotals(parameters, payload, parameters.getSizeOfIOA() + (index * elementSize),
						true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new IntegratedTotals(parameters, payload, index * (parameters.getSizeOfIOA() + elementSize),
						false);

			break;

		case M_IT_TA_1: /* 16 */

			elementSize = 8;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new IntegratedTotalsWithCP24Time2a(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new IntegratedTotalsWithCP24Time2a(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_EP_TA_1: /* 17 */

			elementSize = 3;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new EventOfProtectionEquipment(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new EventOfProtectionEquipment(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_EP_TB_1: /* 18 */

			elementSize = 7;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new PackedStartEventsOfProtectionEquipment(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new PackedStartEventsOfProtectionEquipment(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_EP_TC_1: /* 19 */

			elementSize = 7;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new PackedOutputCircuitInfo(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new PackedOutputCircuitInfo(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_PS_NA_1: /* 20 */

			elementSize = 5;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new PackedSinglePointWithSCD(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new PackedSinglePointWithSCD(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_ME_ND_1: /* 21 */

			elementSize = 2;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new MeasuredValueNormalizedWithoutQuality(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new MeasuredValueNormalizedWithoutQuality(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		/* 22 - 29 reserved */

		case M_SP_TB_1: /* 30 */

			elementSize = 8;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new SinglePointWithCP56Time2a(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new SinglePointWithCP56Time2a(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_DP_TB_1: /* 31 */

			elementSize = 8;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new DoublePointWithCP56Time2a(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new DoublePointWithCP56Time2a(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_ST_TB_1: /* 32 */

			elementSize = 9;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new StepPositionWithCP56Time2a(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new StepPositionWithCP56Time2a(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_BO_TB_1: /* 33 */

			elementSize = 12;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new Bitstring32WithCP56Time2a(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new Bitstring32WithCP56Time2a(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_ME_TD_1: /* 34 */

			elementSize = 10;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new MeasuredValueNormalizedWithCP56Time2a(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new MeasuredValueNormalizedWithCP56Time2a(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_ME_TE_1: /* 35 */

			elementSize = 10;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new MeasuredValueScaledWithCP56Time2a(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new MeasuredValueScaledWithCP56Time2a(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_ME_TF_1: /* 36 */

			elementSize = 12;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new MeasuredValueShortWithCP56Time2a(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new MeasuredValueShortWithCP56Time2a(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_IT_TB_1: /* 37 */

			elementSize = 12;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new IntegratedTotalsWithCP56Time2a(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new IntegratedTotalsWithCP56Time2a(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_EP_TD_1: /* 38 */

			elementSize = 10;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new EventOfProtectionEquipmentWithCP56Time2a(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new EventOfProtectionEquipmentWithCP56Time2a(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_EP_TE_1: /* 39 */

			elementSize = 11;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new PackedStartEventsOfProtectionEquipmentWithCP56Time2a(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new PackedStartEventsOfProtectionEquipmentWithCP56Time2a(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		case M_EP_TF_1: /* 40 */

			elementSize = 11;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new PackedOutputCircuitInfoWithCP56Time2a(parameters, payload,
						parameters.getSizeOfIOA() + (index * elementSize), true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new PackedOutputCircuitInfoWithCP56Time2a(parameters, payload,
						index * (parameters.getSizeOfIOA() + elementSize), false);

			break;

		/* 41 - 44 reserved */

		case C_SC_NA_1: /* 45 */

			elementSize = parameters.getSizeOfIOA() + 1;

			retVal = new SingleCommand(parameters, payload, index * elementSize);

			break;

		case C_DC_NA_1: /* 46 */

			elementSize = parameters.getSizeOfIOA() + 1;

			retVal = new DoubleCommand(parameters, payload, index * elementSize);

			break;

		case C_RC_NA_1: /* 47 */

			elementSize = parameters.getSizeOfIOA() + 1;

			retVal = new StepCommand(parameters, payload, index * elementSize);

			break;

		case C_SE_NA_1: /* 48 - Set-point command, normalized value */

			elementSize = parameters.getSizeOfIOA() + 3;

			retVal = new SetpointCommandNormalized(parameters, payload, index * elementSize);

			break;

		case C_SE_NB_1: /* 49 - Set-point command, scaled value */

			elementSize = parameters.getSizeOfIOA() + 3;

			retVal = new SetpointCommandScaled(parameters, payload, index * elementSize);

			break;

		case C_SE_NC_1: /* 50 - Set-point command, short floating point number */

			elementSize = parameters.getSizeOfIOA() + 5;

			retVal = new SetpointCommandShort(parameters, payload, index * elementSize);

			break;

		case C_BO_NA_1: /* 51 - Bitstring command */

			elementSize = parameters.getSizeOfIOA() + 4;

			retVal = new Bitstring32Command(parameters, payload, index * elementSize);

			break;

		/* 52 - 57 reserved */

		case C_SC_TA_1: /* 58 - Single command with CP56Time2a */

			elementSize = parameters.getSizeOfIOA() + 8;

			retVal = new SingleCommandWithCP56Time2a(parameters, payload, index * elementSize);

			break;

		case C_DC_TA_1: /* 59 - Double command with CP56Time2a */

			elementSize = parameters.getSizeOfIOA() + 8;

			retVal = new DoubleCommandWithCP56Time2a(parameters, payload, index * elementSize);

			break;

		case C_RC_TA_1: /* 60 - Step command with CP56Time2a */

			elementSize = parameters.getSizeOfIOA() + 8;

			retVal = new StepCommandWithCP56Time2a(parameters, payload, index * elementSize);

			break;

		case C_SE_TA_1: /* 61 - Setpoint command, normalized value with CP56Time2a */

			elementSize = parameters.getSizeOfIOA() + 10;

			retVal = new SetpointCommandNormalizedWithCP56Time2a(parameters, payload, index * elementSize);

			break;

		case C_SE_TB_1: /* 62 - Setpoint command, scaled value with CP56Time2a */

			elementSize = parameters.getSizeOfIOA() + 10;

			retVal = new SetpointCommandScaledWithCP56Time2a(parameters, payload, index * elementSize);

			break;

		case C_SE_TC_1: /* 63 - Setpoint command, short value with CP56Time2a */

			elementSize = parameters.getSizeOfIOA() + 12;

			retVal = new SetpointCommandShortWithCP56Time2a(parameters, payload, index * elementSize);

			break;

		case C_BO_TA_1: /* 64 - Bitstring command with CP56Time2a */

			elementSize = parameters.getSizeOfIOA() + 11;

			retVal = new Bitstring32CommandWithCP56Time2a(parameters, payload, index * elementSize);

			break;

		/* 65 - 69 reserved */

		case M_EI_NA_1: /* 70 - End of initialization */
			elementSize = parameters.getSizeOfCA() + 1;

			retVal = new EndOfInitialization(parameters, payload, index * elementSize);

			break;

		case C_IC_NA_1: /* 100 - Interrogation command */

			elementSize = parameters.getSizeOfIOA() + 1;

			retVal = new InterrogationCommand(parameters, payload, index * elementSize);

			break;

		case C_CI_NA_1: /* 101 - Counter interrogation command */

			elementSize = parameters.getSizeOfIOA() + 1;

			retVal = new CounterInterrogationCommand(parameters, payload, index * elementSize);

			break;

		case C_RD_NA_1: /* 102 - Read command */

			elementSize = parameters.getSizeOfIOA();

			retVal = new ReadCommand(parameters, payload, index * elementSize);

			break;

		case C_CS_NA_1: /* 103 - Clock synchronization command */

			elementSize = parameters.getSizeOfIOA() + 7;

			retVal = new ClockSynchronizationCommand(parameters, payload, index * elementSize);

			break;

		case C_TS_NA_1: /* 104 - Test command */

			elementSize = parameters.getSizeOfIOA() + 2;

			retVal = new TestCommand(parameters, payload, index * elementSize);

			break;

		case C_RP_NA_1: /* 105 - Reset process command */

			elementSize = parameters.getSizeOfIOA() + 1;

			retVal = new ResetProcessCommand(parameters, payload, index * elementSize);

			break;

		case C_CD_NA_1: /* 106 - Delay acquisition command */

			elementSize = parameters.getSizeOfIOA() + 2;

			retVal = new DelayAcquisitionCommand(parameters, payload, index * elementSize);

			break;

		case C_TS_TA_1: /* 107 - Test command with CP56Time2a */

			elementSize = parameters.getSizeOfIOA() + 9;

			retVal = new TestCommandWithCP56Time2a(parameters, payload, index * elementSize);

			break;

		/* C_TS_TA_1 (107) is handled by the stack automatically */

		case P_ME_NA_1: /* 110 - Parameter of measured values, normalized value */

			elementSize = parameters.getSizeOfIOA() + 3;

			retVal = new ParameterNormalizedValue(parameters, payload, index * elementSize);

			break;

		case P_ME_NB_1: /* 111 - Parameter of measured values, scaled value */

			elementSize = parameters.getSizeOfIOA() + 3;

			retVal = new ParameterScaledValue(parameters, payload, index * elementSize);

			break;

		case P_ME_NC_1: /* 112 - Parameter of measured values, short floating point number */

			elementSize = parameters.getSizeOfIOA() + 5;

			retVal = new ParameterFloatValue(parameters, payload, index * elementSize);

			break;

		case P_AC_NA_1: /* 113 - Parameter for activation */

			elementSize = parameters.getSizeOfIOA() + 1;

			retVal = new ParameterActivation(parameters, payload, index * elementSize);

			break;

		case F_FR_NA_1: /* 120 - File ready */

			retVal = new FileReady(parameters, payload, 0, false);

			break;

		case F_SR_NA_1: /* 121 - Section ready */

			retVal = new SectionReady(parameters, payload, 0, false);

			break;

		case F_SC_NA_1: /* 122 - Call directory, select file, call file, call section */

			retVal = new FileCallOrSelect(parameters, payload, 0, false);

			break;

		case F_LS_NA_1: /* 123 - Last section, last segment */

			retVal = new FileLastSegmentOrSection(parameters, payload, 0, false);

			break;

		case F_AF_NA_1: /* 124 - ACK file, ACK section */

			retVal = new FileACK(parameters, payload, 0, false);

			break;

		case F_SG_NA_1: /* 125 - Segment */

			retVal = new FileSegment(parameters, payload, 0, false);

			break;

		case F_DR_TA_1: /* 126 - Directory */

			elementSize = 13;

			if (isSequence()) {
				int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

				retVal = new FileDirectory(parameters, payload, parameters.getSizeOfIOA() + (index * elementSize),
						true);

				retVal.setObjectAddress(ioa + index);

			} else
				retVal = new FileDirectory(parameters, payload, index * (parameters.getSizeOfIOA() + elementSize),
						false);

			break;

		/* 114 - 119 reserved */

		default:
			if (privateObjectTypes != null) {

				IPrivateIOFactory ioFactory = privateObjectTypes.getFactory(typeId);

				if (ioFactory != null) {

					elementSize = parameters.getSizeOfIOA() + ioFactory.getEncodedSize();

					if (isSequence()) {

						int ioa = InformationObject.ParseInformationObjectAddress(parameters, payload, 0);

						retVal = ioFactory.decode(parameters, payload, index * elementSize, true);

						retVal.setObjectAddress(ioa + index);
					} else
						retVal = ioFactory.decode(parameters, payload, index * elementSize, false);

				}
			}
			break;
		}

		if (retVal == null)
			throw new ASDUParsingException("Unknown ASDU type id:" + typeId);

		return retVal;
	}

	/// <summary>
	/// Gets the element (information object) with the specified index. This
	/// function supports private information object types.
	/// </summary>
	/// <returns>the information object at index</returns>
	/// <param name="index">index of the element (starting with 0)</param>
	/// <param name="privateObjectTypes">known private information object
	/// types</param>
	/// <exception cref="lib60870.ASDUParsingException">Thrown when there is a
	/// problem parsing the ASDU</exception>
	public InformationObject getElement(int index, PrivateInformationObjectTypes privateObjectTypes)
			throws ASDUParsingException {
		this.privateObjectTypes = privateObjectTypes;

		return getElement(index);
	}

	/// <summary>
	/// Gets the number of elements (information objects) of the ASDU
	/// </summary>
	/// <value>The number of information objects.</value>
	public int getNumberOfElements() {
		return (variableStructureQualifier & 0x7f);
	}

	/**
	 * @return the oa
	 */
	public byte getOriginatorAddress() {
		return originatorAddress;
	}

	/// <summary>
	/// Gets the type identifier (TI).
	/// </summary>
	/// <value>The type identifier.</value>
	public TypeID getTypeId() {
		return this.typeId;
	}

	/**
	 * @return the isNegative
	 */
	public boolean isNegative() {
		return isNegative;
	}

	/// <summary>
	/// Gets a value indicating whether this instance is a sequence of information
	/// objects
	/// </summary>
	/// A sequence of information objects contains multiple information objects with
	/// successive
	/// information object addresses (IOA).
	/// <value><c>true</c> if this instance is a sequence; otherwise,
	/// <c>false</c>.</value>
	public boolean isSequence() {
		if ((variableStructureQualifier & 0x80) != 0)
			return true;
		else
			return false;
	}

	/**
	 * @return the isTest
	 */
	public boolean isTest() {
		return isTest;
	}

	public void setCauseOfTransmission(CauseOfTransmission cot) {
		this.causeOfTransmission = cot;
	}

	public void setCommonAddress(int commonAddress) {
		this.commonAddress = commonAddress;
	}

	/**
	 * @param isNegative the isNegative to set
	 */
	public void setNegative(boolean isNegative) {
		this.isNegative = isNegative;
	}

	/**
	 * @param originatorAddress the oa to set
	 */
	public void setOriginatorAddress(byte originatorAddress) {
		this.originatorAddress = originatorAddress;
	}

	public String

			toString() {
		String ret;

		ret = "TypeID: " + typeId.toString() + " COT: " + causeOfTransmission.toString();

		if (parameters.getSizeOfCOT() == 2)
			ret += " OA: " + originatorAddress;

		if (isTest)
			ret += " [TEST]";

		if (isNegative)
			ret += " [NEG]";

		if (isSequence())
			ret += " [SEQ]";

		ret += " elements: " + getNumberOfElements();

		ret += " CA: " + commonAddress;

		return ret;
	}

}