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
package org.ecsoya.iec60870.layer;

import java.util.Arrays;

/* Function codes for unbalanced transmission */
public enum FunctionCodeSecondary {
	ACK(0),
	NACK(1),
	RESP_USER_DATA(8),
	RESP_NACK_NO_DATA(9),
	STATUS_OF_LINK_OR_ACCESS_DEMAND(11),
	LINK_SERVICE_NOT_FUNCTIONING(14),
	LINK_SERVICE_NOT_IMPLEMENTED(15);

	public static FunctionCodeSecondary get(int value) {
		return Arrays.asList(FunctionCodeSecondary.values()).stream().filter(v -> value == v.value).findFirst()
				.orElse(null);
	}

	private final int value;

	private FunctionCodeSecondary(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
