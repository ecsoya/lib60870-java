/**
 * 
 */
package org.ecsoya.lib60870.ui.cs104;

import org.ecsoya.iec60870.asdu.ASDU;
import org.ecsoya.iec60870.core.ConnectionException;
import org.ecsoya.iec60870.core.IMasterCallable;
import org.ecsoya.iec60870.core.handler.ASDUHandler;
import org.ecsoya.iec60870.core.handler.InterrogationHandler;
import org.ecsoya.iec60870.cs104.Server;
import org.ecsoya.lib60870.ui.BaseApplication;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CS104ServerApplication extends BaseApplication implements InterrogationHandler, ASDUHandler {

	private Server server;

	public CS104ServerApplication() {
		super("IEC 60870-5-104 Server");
	}

	@Override
	public void start() throws ConnectionException {
		server = new Server(null, null);
		server.setDebugOutput((msg) -> console(msg));
		server.setMaxQueueSize(10);

		server.start();
	}

	@Override
	public void stop() throws ConnectionException {
		server.stop();
	}

	public static void main(String[] args) {
		CS104ServerApplication server = new CS104ServerApplication();
		server.open();
	}

	@Override
	public boolean invoke(Object parameter, IMasterCallable connection, ASDU asdu) {
		trace("ASDU: " + asdu.getTypeId());
		return true;
	}

	@Override
	public boolean invoke(Object parameter, IMasterCallable connection, ASDU asdu, byte qoi) {
		trace("Interrogation: " + asdu.getTypeId() + " [" + qoi + "]");
		return true;
	}

}
