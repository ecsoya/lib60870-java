package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.QualityDescriptor;
import org.ecsoya.iec60870.asdu.ie.value.StatusAndStatusChangeDetection;

public class PackedSinglePointWithSCD extends InformationObject {

	private StatusAndStatusChangeDetection scd;

	private QualityDescriptor qds;

	public PackedSinglePointWithSCD(ApplicationLayerParameters parameters, byte[] msg, int startIndex,
			boolean isSquence) throws ASDUParsingException {
		super(parameters, msg, startIndex, isSquence);

		if (!isSquence)
			startIndex += parameters.getSizeOfIOA(); /* skip IOA */

		if ((msg.length - startIndex) < GetEncodedSize())
			throw new ASDUParsingException("Message too small");

		setScd(new StatusAndStatusChangeDetection(msg, startIndex));
		startIndex += 4;

		setQds(new QualityDescriptor(msg[startIndex++]));
	}

	public PackedSinglePointWithSCD(int objectAddress, StatusAndStatusChangeDetection scd, QualityDescriptor quality) {
		super(objectAddress);
		this.setScd(scd);
		this.setQds(quality);
	}

	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);

		frame.appendBytes(getScd().GetEncodedValue());

		frame.setNextByte(getQds().getEncodedValue());
	}

	/**
	 * @return the qds
	 */
	public QualityDescriptor getQds() {
		return qds;
	}

	/**
	 * @param qds the qds to set
	 */
	public void setQds(QualityDescriptor qds) {
		this.qds = qds;
	}

	/**
	 * @return the scd
	 */
	public StatusAndStatusChangeDetection getScd() {
		return scd;
	}

	/**
	 * @param scd the scd to set
	 */
	public void setScd(StatusAndStatusChangeDetection scd) {
		this.scd = scd;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ecsoya.iec60870.asdu.InformationObject#getSupportsSequence()
	 */
	@Override
	public boolean getSupportsSequence() {
		return true;
	}

	public int GetEncodedSize() {
		return 5;
	}

	public TypeID getType() {
		return TypeID.M_PS_NA_1;
	}
}