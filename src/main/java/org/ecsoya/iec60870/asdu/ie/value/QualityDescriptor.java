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
package org.ecsoya.iec60870.asdu.ie.value;

public class QualityDescriptor {
	public static QualityDescriptor INVALID() {
		QualityDescriptor qd = new QualityDescriptor();
		qd.setInvalid(true);
		return qd;
	}

	public static QualityDescriptor VALID() {
		return new QualityDescriptor();
	}

	private byte encodedValue;

	public QualityDescriptor() {
		this.encodedValue = 0;
	}

	public QualityDescriptor(byte encodedValue) {
		this.encodedValue = encodedValue;
	}

	public final boolean getBlocked() {
		if ((encodedValue & 0x10) != 0) {
			return true;
		} else {
			return false;
		}
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

	public final boolean getOverflow() {
		if ((encodedValue & 0x01) != 0) {
			return true;
		} else {
			return false;
		}
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

	public final void setOverflow(boolean value) {
		if (value) {
			encodedValue |= 0x01;
		} else {
			encodedValue &= 0xfe;
		}
	}

	public final void setSubstituted(boolean value) {
		if (value) {
			encodedValue |= 0x20;
		} else {
			encodedValue &= 0xdf;
		}
	}

	@Override
	public String toString() {
		return String.format(
				"[QualityDescriptor: Overflow=%1$s, Blocked=%2$s, Substituted=%3$s, NonTopical=%4$s, Invalid=%5$s]",
				getOverflow(), getBlocked(), getSubstituted(), getNonTopical(), getInvalid());
	}
}
