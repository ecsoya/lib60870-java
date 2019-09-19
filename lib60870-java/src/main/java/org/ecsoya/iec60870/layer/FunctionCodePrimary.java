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
public enum FunctionCodePrimary {
	RESET_REMOTE_LINK(0), /* Reset CU (communication unit) */
	RESET_USER_PROCESS(1), //
	TEST_FUNCTION_FOR_LINK(2), //
	USER_DATA_CONFIRMED(3), //
	USER_DATA_NO_REPLY(4), //
	RESET_FCB(7), /* required/only for CS103 */
	REQUEST_FOR_ACCESS_DEMAND(8), //
	REQUEST_LINK_STATUS(9), //
	REQUEST_USER_DATA_CLASS_1(10), //
	REQUEST_USER_DATA_CLASS_2(11);//

	/**
	 * @param fc
	 * @return
	 */
	public static FunctionCodePrimary get(int value) {
		return Arrays.asList(FunctionCodePrimary.values()).stream().filter(v -> value == v.value).findFirst()
				.orElse(null);
	}

	private final int value;

	/**
	 *
	 */
	private FunctionCodePrimary(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
