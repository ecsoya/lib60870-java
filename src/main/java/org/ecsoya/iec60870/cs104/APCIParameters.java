package org.ecsoya.iec60870.cs104;

/*
 *  ApplicationLayerParameters.cs
 *
 *  Copyright 2017 MZ Automation GmbH
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
 * Parameters for the CS 104 APCI (Application Protocol Control Information)
 */
public class APCIParameters {

	private int k = 12; /*
						 * number of unconfirmed APDUs in I format (range: 1 .. 32767 (2^15 - 1) -
						 * sender will stop transmission after k unconfirmed I messages
						 */

	private int w = 8; /*
						 * number of unconfirmed APDUs in I format (range: 1 .. 32767 (2^15 - 1) -
						 * receiver will confirm latest after w messages
						 */

	private int t0 = 10; // connection establishment (in s)

	private int t1 = 15; /*
							 * timeout for transmitted APDUs in I/U format (in s) when timeout elapsed
							 * without confirmation the connection will be closed
							 */

	private int t2 = 10; // timeout to confirm messages (in s)

	private int t3 = 20; // time until test telegrams in case of idle connection

	public APCIParameters() {
	}

	public final APCIParameters Clone() {
		APCIParameters copy = new APCIParameters();

		copy.k = k;
		copy.w = w;
		copy.t0 = t0;
		copy.t1 = t1;
		copy.t2 = t2;
		copy.t3 = t3;

		return copy;
	}

	public final int getK() {
		return this.k;
	}

	public final int getT0() {
		return this.t0;
	}

	public final int getT1() {
		return this.t1;
	}

	public final int getT2() {
		return this.t2;
	}

	public final int getT3() {
		return this.t3;
	}

	public final int getW() {
		return this.w;
	}

	public final void setK(int value) {
		k = value;
	}

	public final void setT0(int value) {
		t0 = value;
	}

	public final void setT1(int value) {
		t1 = value;
	}

	public final void setT2(int value) {
		t2 = value;
	}

	public final void setT3(int value) {
		t3 = value;
	}

	public final void setW(int value) {
		w = value;
	}
}