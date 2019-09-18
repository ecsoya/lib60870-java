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

/**
 * QDP - Quality descriptor for events of protection equipment according to IEC
 * 60870-5-101:2003 7.2.6.4
 */
public class QualityDescriptorP {
	private byte encodedValue;

	public QualityDescriptorP() {
		this.encodedValue = 0;
	}

	public QualityDescriptorP(byte encodedValue) {
		this.encodedValue = encodedValue;
	}

	public final boolean getBlocked() {
		if ((encodedValue & 0x10) != 0) {
			return true;
		} else {
			return false;
		}
	}

	public final boolean getElapsedTimeInvalid() {
		return ((encodedValue & 0x08) == 0x08);
	}

	public final byte getEncodedValue() {
		return this.encodedValue;
	}

	public final boolean getInvalid() {
		if ((encodedValue & 0x80) != 0) {
			return true;
		} else {
			return false;
		}
	}

	public final boolean getNonTopical() {
		if ((encodedValue & 0x40) != 0) {
			return true;
		} else {
			return false;
		}
	}

	public final boolean getReserved() {
		return ((encodedValue & 0x04) == 0x04);
	}

	public final boolean getSubstituted() {
		if ((encodedValue & 0x20) != 0) {
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

	public final void setElapsedTimeInvalid(boolean value) {
		if (value) {
			encodedValue |= 0x08;
		} else {
			encodedValue &= 0xf7;
		}
	}

	public final void setEncodedValue(byte value) {
		encodedValue = value;
	}

	public final void setInvalid(boolean value) {
		if (value) {
			encodedValue |= 0x80;
		} else {
			encodedValue &= 0x7f;
		}
	}

	public final void setNonTopical(boolean value) {
		if (value) {
			encodedValue |= 0x40;
		} else {
			encodedValue &= 0xbf;
		}
	}

	public final void setReserved(boolean value) {
		if (value) {
			encodedValue |= 0x04;
		} else {
			encodedValue &= 0xfb;
		}
	}

	public final void setSubstituted(boolean value) {
		if (value) {
			encodedValue |= 0x20;
		} else {
			encodedValue &= 0xdf;
		}
	}
}