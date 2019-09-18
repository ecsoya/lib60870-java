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