/**
 * 
 */
package org.ecsoya.lib60870.ui.cs104;

import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.core.ConnectionException;
import org.ecsoya.iec60870.cs104.APCIParameters;
import org.ecsoya.iec60870.cs104.Connection;
import org.ecsoya.lib60870.ui.BaseApplication;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CS104ClientApplication extends BaseApplication {

	private Connection connection;

	public CS104ClientApplication() {
		super("IEC 60870-5-104 Client");
	}

	@Override
	public void start() throws ConnectionException {
		connection = new Connection("127.0.0.1", 2404, new APCIParameters(), new ApplicationLayerParameters());
		connection.setDebugOutput((msg) -> console(msg));
		connection.start();
	}

	@Override
	public void stop() throws ConnectionException {
		connection.stop();
	}

	public static void main(String[] args) {
		new CS104ClientApplication().open();
	}

}
