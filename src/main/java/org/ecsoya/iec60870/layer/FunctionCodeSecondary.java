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

	private final int value;

	private FunctionCodeSecondary(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static FunctionCodeSecondary get(int value) {
		return Arrays.asList(FunctionCodeSecondary.values()).stream().filter(v -> value == v.value).findFirst()
				.orElse(null);
	}
}
