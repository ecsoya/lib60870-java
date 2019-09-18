//====================================================================================================
//The Free Edition of C# to Java Converter limits conversion output to 100 lines per file.

//To subscribe to the Premium Edition, visit our website:
//https://www.tangiblesoftwaresolutions.com/order/order-csharp-to-java.html
//====================================================================================================

package org.ecsoya.iec60870.asdu.ie.value;

/**
 * SPE - Start events of protection equipment according to IEC 60870-5-101:2003
 * 7.2.6.11
 */
public class StartEvent {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: private byte encodedValue;
	private byte encodedValue;

	public StartEvent() {
		this.encodedValue = 0;
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public StartEvent(byte encodedValue)
	public StartEvent(byte encodedValue) {
		this.encodedValue = encodedValue;
	}

	/**
	 * General start of operation
	 * 
	 * <value><c>true</c> if started; otherwise, <c>false</c>.</value>
	 */
	public final boolean getGS() {
		if ((encodedValue & 0x01) != 0) {
			return true;
		} else {
			return false;
		}
	}

	public final void setGS(boolean value) {
		if (value) {
			encodedValue |= 0x01;
		} else {
			encodedValue &= 0xfe;
		}
	}

	/**
	 * Start of operation phase L1
	 * 
	 * <value><c>true</c> if started; otherwise, <c>false</c>.</value>
	 */
	public final boolean getSL1() {
		if ((encodedValue & 0x02) != 0) {
			return true;
		} else {
			return false;
		}
	}

	public final void setSL1(boolean value) {
		if (value) {
			encodedValue |= 0x02;
		} else {
			encodedValue &= 0xfd;
		}
	}

	/**
	 * Start of operation phase L2
	 * 
	 * <value><c>true</c> if started; otherwise, <c>false</c>.</value>
	 */
	public final boolean getSL2() {
		if ((encodedValue & 0x04) != 0) {
			return true;
		} else {
			return false;
		}
	}

	public final void setSL2(boolean value) {
		if (value) {
			encodedValue |= 0x04;
		} else {
			encodedValue &= 0xfb;
		}
	}

	/**
	 * Start of operation phase L3
	 * 
	 * <value><c>true</c> if started; otherwise, <c>false</c>.</value>
	 */
	public final boolean getSL3() {
		if ((encodedValue & 0x08) != 0) {
			return true;
		} else {
			return false;
		}
	}

	public final void setSL3(boolean value) {
		if (value) {
			encodedValue |= 0x08;
		} else {
			encodedValue &= 0xf7;
		}
	}

	/**
	 * Start of operation IE (earth current)
	 * 
	 * <value><c>true</c> if started; otherwise, <c>false</c>.</value>
	 */
	public final boolean getSIE() {
		if ((encodedValue & 0x10) != 0) {
			return true;
		} else {
			return false;
		}
	}

	public final void setSIE(boolean value) {
		if (value) {
			encodedValue |= 0x10;
		} else {
			encodedValue &= 0xef;
		}
	}

	/**
	 * Start of operation in reverse direction
	 * 
	 * <value><c>true</c> if started; otherwise, <c>false</c>.</value>
	 */
	public final boolean getSRD() {
		if ((encodedValue & 0x20) != 0) {
			return true;
		} else {
			return false;
		}
	}

	public final void setSRD(boolean value) {
		if (value) {
			encodedValue |= 0x20;
		} else {
			encodedValue &= 0xdf;
		}
	}

	public final boolean getRES1() {
		if ((encodedValue & 0x40) != 0) {
			return true;
		} else {
			return false;
		}
	}

	public final void setRES1(boolean value) {
		if (value) {
			encodedValue |= 0x40;
		} else {
			encodedValue &= 0xbf;
		}
	}

	public final boolean getRES2() {
		if ((encodedValue & 0x80) != 0) {
			return true;
		} else {
			return false;
		}
	}

	public final void setRES2(boolean value) {
		if (value) {
			encodedValue |= 0x80;
		} else {
			encodedValue &= 0x7f;
		}
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public byte getEncodedValue()
	public final byte getEncodedValue() {
		return this.encodedValue;
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public void setEncodedValue(byte value)
	public final void setEncodedValue(byte value) {
		encodedValue = value;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(30);

		if (getGS()) {
			sb.append("[GS]");
		}
		if (getSL1()) {
			sb.append("[SL1]");
		}
		if (getSL2()) {
			sb.append("[SL2]");
		}
		if (getSL3())
			sb.append("[SL3]");
		if (getSIE())
			sb.append("[SIE]");
		if (getSRD())
			sb.append("[SRD]");
		if (getRES1())
			sb.append("[RES1]");
		if (getRES2())
			sb.append("[RES2]");

		return sb.toString();
	}
}