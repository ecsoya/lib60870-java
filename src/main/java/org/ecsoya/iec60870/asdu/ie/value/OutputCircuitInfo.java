package org.ecsoya.iec60870.asdu.ie.value;

/*
 *  OutputCircuitInfo.cs
 *
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
 * Output circuit information of protection equipment According to IEC
 * 60870-5-101:2003 7.2.6.12
 */
public class OutputCircuitInfo {

	public byte encodedValue;

	public final byte getEncodedValue() {
		return this.encodedValue;
	}

	public final void setEncodedValue(byte value) {
		encodedValue = value;
	}

	public OutputCircuitInfo() {
		this.encodedValue = 0;
	}

	public OutputCircuitInfo(byte encodedValue) {
		this.encodedValue = encodedValue;
	}

	public OutputCircuitInfo(boolean gc, boolean cl1, boolean cl2, boolean cl3) {
		setGC(gc);
		setCL1(cl1);
		setCL2(cl2);
		setCL3(cl3);
	}

	/**
	 * General command to output circuit
	 * 
	 * <value><c>true</c> if set, otherwise, <c>false</c>.</value>
	 */
	public final boolean getGC() {
		return ((encodedValue & 0x01) != 0);
	}

	public final void setGC(boolean value) {
		if (value) {
			encodedValue |= 0x01;
		} else {
			encodedValue &= 0xfe;
		}
	}

	/**
	 * Command to output circuit phase L1
	 * 
	 * <value><c>true</c> if set, otherwise, <c>false</c>.</value>
	 */
	public final boolean getCL1() {
		return ((encodedValue & 0x02) != 0);
	}

	public final void setCL1(boolean value) {
		if (value) {
			encodedValue |= 0x02;
		} else {
			encodedValue &= 0xfd;
		}
	}

	/**
	 * Command to output circuit phase L2
	 * 
	 * <value><c>true</c> if set, otherwise, <c>false</c>.</value>
	 */
	public final boolean getCL2() {
		return ((encodedValue & 0x04) != 0);
	}

	public final void setCL2(boolean value) {
		if (value) {
			encodedValue |= 0x04;
		} else {
			encodedValue &= 0xfb;
		}
	}

	/**
	 * Command to output circuit phase L3
	 * 
	 * <value><c>true</c> if set, otherwise, <c>false</c>.</value>
	 */
	public final boolean getCL3() {
		return ((encodedValue & 0x08) != 0);
	}

	public final void setCL3(boolean value) {
		if (value) {
			encodedValue |= 0x08;
		} else {
			encodedValue &= 0xf7;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(20);

		if (getGC()) {
			sb.append("[GC]");
		}
		if (getCL1()) {
			sb.append("[CL1]");
		}
		if (getCL2()) {
			sb.append("[CL2]");
		}
		if (getCL3()) {
			sb.append("[CL3]");
		}

		return sb.toString();
	}
}