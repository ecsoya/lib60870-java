package org.ecsoya.iec60870.asdu.ie.value;

/*
 *  Copyright 2016 MZ Automation GmbH
 *
 *  This file is part of lib60870.NET
 *
 *  lib60870.NET is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  lib60870.NET is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with lib60870.NET.  If not, see <http: //www.gnu.org/licenses/>.
 *
 *  See COPYING file for the complete license text.
 */

public class QualityDescriptor {
	private byte encodedValue;

	public static QualityDescriptor VALID() {
		return new QualityDescriptor();
	}

	public static QualityDescriptor INVALID() {
		QualityDescriptor qd = new QualityDescriptor();
		qd.setInvalid(true);
		return qd;
	}

	public QualityDescriptor() {
		this.encodedValue = 0;
	}

	public QualityDescriptor(byte encodedValue) {
		this.encodedValue = encodedValue;
	}

	public final boolean getOverflow() {
		if ((encodedValue & 0x01) != 0) {
			return true;
		} else {
			return false;
		}
	}

	public final void setOverflow(boolean value) {
		if (value) {
			encodedValue |= 0x01;
		} else {
			encodedValue &= 0xfe;
		}
	}

	public final boolean getBlocked() {
		if ((encodedValue & 0x10) != 0) {
			return true;
		} else {
			return false;
		}
	}

	public final void setBlocked(boolean value) {
		if (value) {
			encodedValue |= 0x10;
		} else {
			encodedValue &= 0xef;
		}
	}

	public final boolean getSubstituted() {
		if ((encodedValue & 0x20) != 0) {
			return true;
		} else {
			return false;
		}
	}

	public final void setSubstituted(boolean value) {
		if (value) {
			encodedValue |= 0x20;
		} else {
			encodedValue &= 0xdf;
		}
	}

	public final boolean getNonTopical() {
		if ((encodedValue & 0x40) != 0) {
			return true;
		} else {
			return false;
		}
	}

	public final void setNonTopical(boolean value) {
		if (value) {
			encodedValue |= 0x40;
		} else {
			encodedValue &= 0xbf;
		}
	}

	public final boolean getInvalid() {
		if ((encodedValue & 0x80) != 0) {
			return true;
		} else {
			return false;
		}
	}

	public final void setInvalid(boolean value) {
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
		return String.format(
				"[QualityDescriptor: Overflow=%1$s, Blocked=%2$s, Substituted=%3$s, NonTopical=%4$s, Invalid=%5$s]",
				getOverflow(), getBlocked(), getSubstituted(), getNonTopical(), getInvalid());
	}
}