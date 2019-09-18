package org.ecsoya.iec60870.asdu.ie.value;

import java.util.HashMap;

import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.conn.IPrivateIOFactory;

/**
 * Hold a list of private information object (IO) types to be used for parsing
 */
public class PrivateInformationObjectTypes {

	private HashMap<TypeID, IPrivateIOFactory> privateTypes = new HashMap<TypeID, IPrivateIOFactory>();

	public final void addPrivateInformationObjectType(TypeID typeId, IPrivateIOFactory iot) {
		privateTypes.put(typeId, iot);
	}

	public final IPrivateIOFactory getFactory(TypeID typeId) {
		IPrivateIOFactory factory = null;

		factory = privateTypes.get(typeId);

		return factory;
	}
}